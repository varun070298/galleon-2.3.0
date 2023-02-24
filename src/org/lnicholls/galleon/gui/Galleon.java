package org.lnicholls.galleon.gui;

/*
 * Copyright (C) 2005 Leon Nicholls
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.UIManager;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppDescriptor;
import org.lnicholls.galleon.app.AppManager;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.downloads.Download;
import org.lnicholls.galleon.server.Constants;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.DataConfiguration;
import org.lnicholls.galleon.server.GoBackConfiguration;
import org.lnicholls.galleon.server.DownloadConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.lnicholls.galleon.server.ServerControl;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.UpcomingServices;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.plaf.FontSizeHints;
import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.Options;

import edu.stanford.ejalbert.BrowserLauncher;

public final class Galleon implements Constants {

	private static String mErrorMessageSuffix = "";

	static
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			mErrorMessageSuffix = "Check that the Galleon service is running. Make sure that the ports required by Galleon listed in the FAQ is configured in your PC firewall software.";
		}
	}

	static class SplashWindow extends JWindow {
		public SplashWindow() {
			super((Frame) null);

			// Image image = Tools.getResourceAsImage(Galleon.class,
			// "galleon.png");
			// image = Tools.getImage(image);

			URL url = getClass().getClassLoader().getResource("galleon.png");
			ImageIcon logo = new ImageIcon(url);
			// ImageIcon logo = new ImageIcon(image);

			JLabel l = new JLabel(logo);
			l.setBackground(java.awt.Color.black);
			getContentPane().add(l, BorderLayout.CENTER);

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension labelSize = l.getPreferredSize();
			setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));
			pack();
		}
	}

	private static SplashWindow splashWindow = new SplashWindow();

	public Galleon() {
		createAndShowGUI();
	}

	private static void createAndShowGUI() {
		try {
			System.setProperty("os.user.home", System.getProperty("user.home"));

			ArrayList errors = new ArrayList();
			Server.setup(errors);
			log = Server.setupLog("org.lnicholls.galleon.gui.Galleon", "GuiTrace", "GuiFile", Constants.GUI_LOG_FILE);
			// log = Logger.getLogger(Galleon.class.getName());
			printSystemProperties();

			UIManager.put("ClassLoader", (com.jgoodies.plaf.LookUtils.class).getClassLoader());
			UIManager.put("Application.useSystemFontSettings", Boolean.TRUE);
			Options.setGlobalFontSizeHints(FontSizeHints.MIXED2);
			Options.setDefaultIconSize(new Dimension(18, 18));
			try {
				UIManager.setLookAndFeel(LookUtils.IS_OS_WINDOWS_XP ? "com.jgoodies.plaf.plastic.PlasticXPLookAndFeel"
						: Options.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				Tools.logException(Galleon.class, e);
			}

			/*
			 * mServerConfiguration = new ServerConfiguration(); if
			 * (mConfigureDir != null) { log.info("Configuration Dir=" +
			 * mConfigureDir.getAbsolutePath()); new
			 * Configurator(mServerConfiguration).load(mAppManager,
			 * mConfigureDir); } else new
			 * Configurator(mServerConfiguration).load(mAppManager); mAddress =
			 * mServerConfiguration.getIPAddress(); if (mAddress == null ||
			 * mAddress.length() == 0) mAddress = "127.0.0.1";
			 */
			log.info("Server address: " + mServerAddress);
			for (int i = 0; i < 100; i++) {
				try {
					mRegistry = LocateRegistry.getRegistry(mServerAddress, 1099 + i);
					String[] names = mRegistry.list();
					ServerControl serverControl = (ServerControl) mRegistry.lookup("serverControl");
					log.info("Found server at: " + mServerAddress + " on " + (1099 + i));
					break;
				} catch (Throwable ex) {
					if (log.isDebugEnabled())
						Tools.logException(Galleon.class, ex);
				}
			}

			File directory = new File(System.getProperty("apps"));
			if (!directory.exists() || !directory.isDirectory()) {
				String message = "App Class Loader directory not found: " + System.getProperty("apps");
				InstantiationException exception = new InstantiationException(message);
				log.error(message, exception);
				throw exception;
			}

			File[] files = directory.listFiles(new FileFilter() {
				public final boolean accept(File file) {
					return !file.isDirectory() && !file.isHidden() && file.getName().toLowerCase().endsWith(".jar");
				}
			});
			ArrayList urls = new ArrayList();
			for (int i = 0; i < files.length; ++i) {
				try {
					URL url = files[i].toURI().toURL();
					urls.add(url);
					log.debug(url);
				} catch (Exception ex) {
					// should never happen
				}
			}

			directory = new File(System.getProperty("hme"));
			if (directory.exists()) {
				files = directory.listFiles(new FileFilter() {
					public final boolean accept(File file) {
						return !file.isDirectory() && !file.isHidden() && file.getName().toLowerCase().endsWith(".jar");
					}
				});
				for (int i = 0; i < files.length; ++i) {
					try {
						URL url = files[i].toURI().toURL();
						urls.add(url);
						log.debug(url);
					} catch (Exception ex) {
						// should never happen
					}
				}
			}

			File currentDirectory = new File(".");
			directory = new File(currentDirectory.getAbsolutePath() + "/../lib");
			// TODO Handle reloading; what if list changes?
			files = directory.listFiles(new FileFilter() {
				public final boolean accept(File file) {
					return !file.isDirectory() && !file.isHidden() && file.getName().toLowerCase().endsWith(".jar");
				}
			});
			for (int i = 0; i < files.length; ++i) {
				try {
					URL url = files[i].toURI().toURL();
					urls.add(url);
					log.debug(url);
				} catch (Exception ex) {
					// should never happen
				}
			}

			URLClassLoader classLoader = new URLClassLoader((URL[]) urls.toArray(new URL[0]));
			Thread.currentThread().setContextClassLoader(classLoader);

			mMainFrame = new MainFrame(Tools.getVersion());

			splashWindow.setVisible(false);
			mMainFrame.setVisible(true);

			/*
			 * javax.swing.SwingUtilities.invokeLater(new Runnable() { public
			 * void run() { mToGo.getRecordings(); } });
			 */

			if (!isCurrentVersion()) {
				if (JOptionPane.showConfirmDialog(mMainFrame,
						"A new version of Galleon is available. Do you want to download the latest version?", "New Version",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					try {
						BrowserLauncher
								.openURL("http://galleon.tv");
					} catch (Exception ex) {
						Tools.logException(Galleon.class, ex);
					}
				}
			}

		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			mServerAddress = args[0];
		}

		splashWindow.setVisible(true);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void printSystemProperties() {
		Properties properties = System.getProperties();
		Enumeration Enumeration = properties.propertyNames();
		for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
			String propertyName = (String) e.nextElement();
			log.info(propertyName + "=" + System.getProperty(propertyName));
		}
		Runtime runtime = Runtime.getRuntime();
		log.info("Max Memory: " + runtime.maxMemory());
		log.info("Total Memory: " + runtime.totalMemory());
		log.info("Free Memory: " + runtime.freeMemory());
	}

	public static MainFrame getMainFrame() {
		return mMainFrame;
	}

	public static void save(boolean reload) {
		if (log.isDebugEnabled())
			log.debug("save: " + reload);
		if (reload) {
			// Connect to server and save config
			try {
				ServerControl serverControl = getServerControl();
				serverControl.reset();
			} catch (Exception ex) {
				Tools.logException(Galleon.class, ex, "Could not update server: " + mServerAddress);

				JOptionPane.showMessageDialog(mMainFrame, "Could not update Galleon server. "+getErrorMessageSuffix(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		mMainFrame.refresh();
	}

	public static List getRecordings() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getRecordings();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get recordings from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static List getTiVos() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getTiVos();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get TiVo's from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void updateTiVos(List tivos) {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.updateTiVos(tivos);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update TiVo's on server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static List getApps() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getApps();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get apps from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static List getAppDescriptors() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getAppDescriptors();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get app descriptors from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void removeApp(AppContext app) {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.removeApp(app);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not remove app from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void updateApp(AppContext app) {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.updateApp(app);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update app from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static ServerConfiguration getServerConfiguration() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getServerConfiguration();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get app server configuration from server: "
					+ mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void updateServerConfiguration(ServerConfiguration serverConfiguration) throws Exception {
		ServerControl serverControl = getServerControl();
		serverControl.updateServerConfiguration(serverConfiguration);

		if (System.getProperty("java.vm.name").equals("Excelsior JET"))
			JOptionPane.showMessageDialog(mMainFrame, "Please restart the Galleon service using Control Panel/Adminstrative Tools/Services", "Info",
					JOptionPane.INFORMATION_MESSAGE);
	}

	public void setDisableTimeout(boolean value) throws Exception
	{
		ServerControl serverControl = getServerControl();
		serverControl.setDisableTimeout(value);
	}

	public static void updateMusicPlayerConfiguration(MusicPlayerConfiguration musicPlayerConfiguration) throws Exception {
		ServerControl serverControl = getServerControl();
		serverControl.updateMusicPlayerConfiguration(musicPlayerConfiguration);
	}

	public static MusicPlayerConfiguration getMusicPlayerConfiguration() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getMusicPlayerConfiguration();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get app server data configuration from server: "
					+ mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void updateDataConfiguration(DataConfiguration dataConfiguration) throws Exception {
		ServerControl serverControl = getServerControl();
		serverControl.updateDataConfiguration(dataConfiguration);
	}

	public static DataConfiguration getDataConfiguration() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getDataConfiguration();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get app server data configuration from server: "
					+ mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void updateGoBackConfiguration(GoBackConfiguration goBackConfiguration) throws Exception {
		ServerControl serverControl = getServerControl();
		serverControl.updateGoBackConfiguration(goBackConfiguration);
	}

	public static GoBackConfiguration getGoBackConfiguration() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getGoBackConfiguration();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get app server goback configuration from server: "
					+ mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}


	public static void updateDownloadConfiguration(DownloadConfiguration downloadConfiguration) throws Exception {
		ServerControl serverControl = getServerControl();
		serverControl.updateDownloadConfiguration(downloadConfiguration);
	}

	public static DownloadConfiguration getDownloadConfiguration() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getDownloadConfiguration();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get app server download configuration from server: "
					+ mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void updateVideo(Video video) {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.updateVideo(video);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update video at server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static Video retrieveVideo(Video video) {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.retrieveVideo(video);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update video at server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static AppContext createAppContext(AppDescriptor appDescriptor) {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.createAppContext(appDescriptor);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not create app context at server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static List getRules() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getRules();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get rules from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void updateRules(List tivos) {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.updateRules(tivos);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update rules on server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static List getWinampSkins() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getWinampSkins();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get Winamp skins from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static List getSkins() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getSkins();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get skins from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static int getPort() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getPort();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get port from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return -1;
	}

	public static int getHTTPPort() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getHttpPort();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get http port from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return -1;
	}

	public static int getHttpPort() {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getHttpPort();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get port from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return -1;
	}

	public static List getPodcasts() throws RemoteException {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getPodcasts();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get podcasts from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void setPodcasts(List list) throws RemoteException {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.setPodcasts(list);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update podcasts to server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static List getVideocasts() throws RemoteException {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getVideocasts();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get videocasts from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void setVideocasts(List list) throws RemoteException {
		try {
			ServerControl serverControl = getServerControl();
			serverControl.setVideocasts(list);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not update videocasts to server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static boolean isCurrentVersion() throws RemoteException {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.isCurrentVersion();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not retrieve current version from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return true;
	}

	public static List getDownloads() throws RemoteException {
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getDownloads();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get downloads from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static void pauseDownload(Download download) throws RemoteException
    {
    	try {
			ServerControl serverControl = getServerControl();
			serverControl.pauseDownload(download);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get pause downloads from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
    }

	public static void resumeDownload(Download download) throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			serverControl.resumeDownload(download);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get resume downloads from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void stopDownload(Download download) throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			serverControl.stopDownload(download);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get stop downloads from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static boolean isFileExists(String path) throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.isFileExists(path);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get file from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	public static void deleteFile(String path) throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			serverControl.deleteFile(path);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get file from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static List getUpcomingCountries() throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getUpcomingCountries();
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get upcoming countries from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static List getUpcomingStates(String countryId) throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getUpcomingStates(countryId);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get upcoming states from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static List getUpcomingMetros(String stateId) throws RemoteException
	{
		try {
			ServerControl serverControl = getServerControl();
			return serverControl.getMetros(stateId);
		} catch (Exception ex) {
			Tools.logException(Galleon.class, ex, "Could not get upcoming metros from server: " + mServerAddress);

			JOptionPane.showMessageDialog(mMainFrame, "Could not connect to server. "+getErrorMessageSuffix(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static class ConnectionDialog extends JDialog {

		private ConnectionDialog() {
			super(mMainFrame, "Connecting to server", true);

			// setUndecorated(true);
			// getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

			// enable debug logging

			mProgressBar = new JProgressBar(0, 60);
			mProgressBar.setValue(0);
			// mProgressBar.setStringPainted(true);

			getContentPane().setLayout(new BorderLayout());

			FormLayout layout = new FormLayout("right:pref, 130dlu, right:pref:grow", "pref");

			PanelBuilder builder = new PanelBuilder(layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.add(mProgressBar, cc.xyw(1, 1, 3));

			getContentPane().add(builder.getPanel(), "Center");

			pack();
			setLocationRelativeTo(mMainFrame);

			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				int counter = 0;

				public void run() {
					mProgressBar.setValue(counter);

					try {
						mServerControl = (ServerControl) mRegistry.lookup("serverControl");
					} catch (Exception ex) {
						mException = ex;
					}

					if (mServerControl != null || counter++ == 60) {
						// Toolkit.getDefaultToolkit().beep();
						mProgressBar.setValue(mProgressBar.getMinimum());
						mProgressBar.setString("");
						this.cancel();
						ConnectionDialog.this.setVisible(false);
					}
				}
			}, 0, 1000);

			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					mTimer.cancel();
					ConnectionDialog.this.setVisible(false);
				}
			});
		}

		private JProgressBar mProgressBar;

		private Timer mTimer;

		private boolean mFound;

		private ServerControl mServerControl;

		private Exception mException;
	}

	private static ServerControl getServerControl() {
		ConnectionDialog connectionDialog = null;
		try {
			connectionDialog = new ConnectionDialog();
			ServerControl serverControl = (ServerControl) mRegistry.lookup("serverControl");
			return serverControl;
		} catch (Throwable ex) {
			if (connectionDialog!=null)
			{
				connectionDialog.setVisible(true);
				if (connectionDialog.mServerControl != null)
					return connectionDialog.mServerControl;
				else {
					Tools.logException(Galleon.class, connectionDialog.mException, "Could connect to server: "
							+ mServerAddress);
				}
			}
			else
				Tools.logException(Galleon.class, ex, "Could connect to server: " + mServerAddress);
			return null;
		}
	}

	public static String getErrorMessageSuffix()
	{
		return mErrorMessageSuffix;
	}

	private static Logger log;

	private static AppManager mAppManager;

	private static MainFrame mMainFrame;

	private static String mAddress;

	private static Registry mRegistry;

	private static String mServerAddress = "localhost";
}