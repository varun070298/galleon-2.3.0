package org.lnicholls.galleon.server;

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

import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.FileFilter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.StringTokenizer;
import java.rmi.RMISecurityManager;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.xml.DOMConfigurator;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppDescriptor;
import org.lnicholls.galleon.app.AppHost;
import org.lnicholls.galleon.app.AppManager;
import org.lnicholls.galleon.database.HibernateUtil;
import org.lnicholls.galleon.database.NetworkServerManager;
import org.lnicholls.galleon.database.PodcastManager;
import org.lnicholls.galleon.database.VideocastManager;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.database.VideoManager;
import org.lnicholls.galleon.skins.Skin;
import org.lnicholls.galleon.togo.DownloadThread;
import org.lnicholls.galleon.togo.ToGoThread;
import org.lnicholls.galleon.util.Configurator;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.data.*;
import org.lnicholls.galleon.goback.*;
import org.lnicholls.galleon.downloads.*;
import org.lnicholls.galleon.widget.ScrollText;

import org.tanukisoftware.wrapper.WrapperManager;

import com.tivo.hme.host.util.Config;

/*
 * Main class. Called by service wrapper to initialise and start Galleon.
 */

public class Server {
	public Server() throws Exception {
		if (mServer!=null)
			throw new IllegalAccessException();

		mServer = this;

		//System.setSecurityManager(new CustomSecurityManager());

		mShortTermTasks = new ArrayList();
		mLongTermTasks = new ArrayList();
		mDataTasks = new ArrayList();

		try {
			System.out.println("Galleon " + Tools.getVersion() + " is starting...");

			ArrayList errors = new ArrayList();
			setup(errors);

			log = setupLog(Server.class.getName());

			for (int i = 0; i < errors.size(); i++)
				log.error(errors.get(i));

			createAppClassLoader();

			Thread.currentThread().setContextClassLoader(mAppClassLoader);

			mServerConfiguration = new ServerConfiguration();

			// Log the system properties
			printSystemProperties();
			printServerProperties();

			// Redirect standard out; some third-party libraries use this for
			// error logging
			// TODO
			// Tools.redirectStandardStreams();

			preLoadFonts();

		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
	}

	public static void setup(ArrayList errors) {
		System.setProperty("os.user.home", System.getProperty("user.home"));

		System.setProperty("http.agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

		try {
			File file = new File(".");
			String root = file.getAbsolutePath() + "/..";

			if (System.getProperty("root") == null)
				System.setProperty("root", root);
			else
				root = System.getProperty("root");

			File check = new File(System.getProperty("root"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: root=" + System.getProperty("root"));

			System.setProperty("user.home", System.getProperty("root"));

			if (System.getProperty("bin") == null)
				System.setProperty("bin", root + "/bin");

			check = new File(System.getProperty("bin"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: bin=" + System.getProperty("bin"));

			if (System.getProperty("conf") == null)
				System.setProperty("conf", root + "/conf");

			check = new File(System.getProperty("conf"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: conf=" + System.getProperty("conf"));

			if (System.getProperty("cache") == null)
				System.setProperty("cache", root + "/data");

			check = new File(System.getProperty("cache"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: cache=" + System.getProperty("cache"));

			if (System.getProperty("data") == null)
				System.setProperty("data", root + "/data");

			check = new File(System.getProperty("data"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: data=" + System.getProperty("data"));

			if (System.getProperty("apps") == null)
				System.setProperty("apps", root + "/apps");

			check = new File(System.getProperty("apps"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: apps=" + System.getProperty("apps"));

			if (System.getProperty("hme") == null)
				System.setProperty("hme", root + "/hme");

			check = new File(System.getProperty("hme"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: hme=" + System.getProperty("hme"));

			if (System.getProperty("skins") == null)
				System.setProperty("skins", root + "/skins");

			check = new File(System.getProperty("skins"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: skins=" + System.getProperty("skins"));

			if (System.getProperty("logs") == null)
				System.setProperty("logs", root + "/logs");

			check = new File(System.getProperty("logs"));
			if (!check.exists() || !check.isDirectory())
				errors.add("Invalid system propery: logs=" + System.getProperty("logs"));

			if (System.getProperty("demo") != null)
				mDemoMode = Boolean.valueOf(System.getProperty("demo")).booleanValue();

			if (System.getProperty("remoteHost") != null)
			{
				System.setSecurityManager (new RMISecurityManager() {
				    public void checkConnect (String host, int port) {
				    	if (host!=null && host.equals(System.getProperty("remoteHost")))
				    		return;
				    	throw new SecurityException("Invalid remote host: "+host);
				    }
				    public void checkConnect (String host, int port, Object context) {
				    	if (host!=null && host.equals(System.getProperty("remoteHost")))
				    		return;
				    	throw new SecurityException("Invalid remote host: "+host);
				    }
				  });
			}
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
	}

	public static Logger setupLog(String name) {
		/* Make a logs directory if one doesn't exist */
		File dir = new File(System.getProperty("logs"));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			DOMConfigurator.configureAndWatch(System.getProperty("conf") + "/log4j.xml", 60000);
		} catch (Exception ex) {
		}

		// Start with a new log file with each restart
		Logger root = Logger.getRootLogger();
		rollOverLog(root, "AsyncTrace", "TraceFile", Constants.LOG_FILE);
		rollOverLog(root, "GuiTrace", "GuiFile", Constants.GUI_LOG_FILE);

		Logger log = Logger.getLogger(name);

		return log;
	}

	public static Logger setupLog(String name, String appenderName, String appenderRefName, String filename) {
		/* Make a logs directory if one doesn't exist */
		File dir = new File(System.getProperty("logs"));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			DOMConfigurator.configureAndWatch(System.getProperty("conf") + "/log4j.xml", 60000);
		} catch (Exception ex) {
		}

		// Start with a new log file with each restart
		Logger root = Logger.getRootLogger();
		rollOverLog(root, appenderName, appenderRefName, filename);

		Logger log = Logger.getLogger(name);

		return log;
	}

	private static void rollOverLog(Logger root, String appenderName, String appenderRefName, String filename)
	{
		AsyncAppender asyncAppender = (AsyncAppender) root.getAppender(appenderName);
		if (asyncAppender != null) {
			RollingFileAppender rollingFileAppender = (RollingFileAppender) asyncAppender.getAppender(appenderRefName);
			if (rollingFileAppender != null) {
				if (!SystemUtils.IS_OS_WINDOWS) {
					if (rollingFileAppender.getFile()!=null) {
						File file = new File(rollingFileAppender.getFile());
						if (!file.exists())
							rollingFileAppender.setFile(System.getProperty("logs") + "/" + filename);
					}
					else
						rollingFileAppender.setFile(System.getProperty("logs") + "/" + filename);
				}
				rollingFileAppender.rollOver();
			}
		}
	}

	private void setDebugLogging(boolean debug) {
		Logger logger = Logger.getLogger("org.lnicholls.galleon.gui");
		if (debug)
			logger.setLevel(Level.DEBUG);
		else
			logger.setLevel(Level.INFO);

		logger = Logger.getLogger("org.lnicholls.galleon");
		if (debug)
			logger.setLevel(Level.DEBUG);
		else
			logger.setLevel(Level.INFO);
	}

	// Service wrapper required method
	public Integer start() {
		if (log.isDebugEnabled())
			log.debug("start()");
		try {
			// Start the database
			NetworkServerManager.initialize();

			HibernateUtil.initialize();
			if (NetworkServerManager.findSchema())
				HibernateUtil.updateSchema();
			else
				HibernateUtil.createSchema();

			// Start time task for period operations such as internet downloads
			mLongTermTimer = new Timer();
			mShortTermTimer = new Timer();
			mDataTimer = new Timer();

			mDownloadManager = new DownloadManager();

			// Load apps
			mAppManager = new AppManager(mAppClassLoader);

			// Read the conf/configure.xml file
			mConfigurator = new Configurator();
			mConfigurator.load(mAppManager);

			setDebugLogging(mServerConfiguration.isDebug());

			mTiVoListener = new TiVoListener();

			mAppManager.loadApps();

			// mAppManager.startPlugins();

			// Start the Media Manager refresh thread
			// scheduleLongTerm(new MediaManager.RefreshTask(), 3); //60*24);
			// MediaManager.addPath(new
			// MediaRefreshThread.PathInfo("d:/download/mp3",FileFilters.audioFilter));

			try {
				mToGoThread = new ToGoThread(this);
				mToGoThread.start();
				mDownloadThread = new DownloadThread(this);
				mDownloadThread.start();
			} catch (Exception ex) {
				Tools.logException(Server.class, ex);

				mToGoThread = null;
				mDownloadThread = null;
			}

			try {
				// Is there already a RMI server?
				mRegistry = LocateRegistry.getRegistry(1099);
				String[] names = mRegistry.list();
				log.info("Using RMI port " + 1099);
			} catch (Exception ex) {
				int port = Tools.findAvailablePort(1099);
				if (port != 1099) {
					log.info("Changed RMI port to " + port);
				}
				else
					log.info("Using RMI port " + 1099);

				mRegistry = LocateRegistry.createRegistry(port);
			}

			mRegistry.bind("serverControl", new ServerControlImpl());

			mTCMs = new LinkedList();
			int port = mServerConfiguration.getHttpPort();
			mHMOPort = Tools.findAvailablePort(port);
			if (mHMOPort != port) {
				log.info("Changed PC publishing port to " + mHMOPort);
			}
			else
				log.info("Using PC publishing port " + mHMOPort);

			Config config = new Config();
	        config.put("http.ports", String.valueOf(mHMOPort));
	        config.put("http.interfaces", mServerConfiguration.getIPAddress());
	        mVideoServer = new VideoServer(config);

	        if (System.getProperty("disableBeacon") != null && System.getProperty("disableBeacon").equals("true"))
	        {
	        	log.debug("Beacon disabled");
	        }
	        else
			{
		        // TiVo Beacon API
	            publishTiVoBeacon();

	            if (!mPublished) {
	                mBeaconPort = Constants.TIVO_PORT;
	                // TODO
	                int counter = 0;
	                while (counter++<100) {
	                    try {
	                        if (log.isDebugEnabled())
	                            log.debug("Using beacon port=" + mBeaconPort);
	                        mBroadcastThread = new BroadcastThread(this, mBeaconPort);
	                        mBroadcastThread.start();
	                        break;
	                    } catch (Throwable ex) {
	                        Tools.logException(Server.class, ex);

	                        if (mBroadcastThread != null)
	                            mBroadcastThread.interrupt();

	                        mBroadcastThread = null;
	                        mBeaconPort = mBeaconPort + 1;
	                    }
	                }

	                if (mBroadcastThread!=null)
	                {
		                log.info("Broadcast port=" + mBeaconPort);

		                mListenThread = new ListenThread(this);
		                mListenThread.start();
	                }
	                else
	                	log.error("Cannot broadcast");

	                mConnectionThread = new ConnectionThread(this);
	                mConnectionThread.start();
	            }
			}

            //mDataUpdateThread = new DataUpdateThread();
            //mDataUpdateThread.start();

			System.out.println("Galleon is ready.");
			mReady = true;

			Iterator iterator = mShortTermTasks.iterator();
			while (iterator.hasNext()) {
				TaskInfo taskInfo = (TaskInfo) iterator.next();
				scheduleShortTerm(taskInfo.task, 0, taskInfo.time);
			}
			mShortTermTasks.clear();
			iterator = mLongTermTasks.iterator();
			while (iterator.hasNext()) {
				TaskInfo taskInfo = (TaskInfo) iterator.next();
				scheduleLongTerm(taskInfo.task, 30, taskInfo.time);
			}
			mLongTermTasks.clear();
			iterator = mDataTasks.iterator();
			while (iterator.hasNext()) {
				TaskInfo taskInfo = (TaskInfo) iterator.next();
				scheduleData(taskInfo.task, 0, taskInfo.time);
			}
			mDataTasks.clear();

			iterator = mPublishedVideos.iterator();
			while (iterator.hasNext()) {
				publishVideo((NameValue)iterator.next());
			}
			mPublishedVideos.clear();
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
			return new Integer(1);
		}
		return null;
	}

	// Service wrapper required method
	public void stop()  throws Exception {
		if (log.isDebugEnabled())
			log.debug("stop()");
		try {
			/*
			 * if (mPluginManager != null) { mPluginManager.destroyAllPlugins();
			 * mPluginManager = null; }
			 */

			if (mDownloadManager != null) {
                try
                {
                	for (Iterator iterator = mDownloadManager.getDownloads().iterator();iterator.hasNext();)
                	{
                		Download download = (Download)iterator.next();
                		mDownloadManager.stopDownload(download);
                	}
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mDownloadManager = null;
            }

			if (mAppManager != null) {
                try
                {
                	for (Iterator iterator = mAppManager.getApps().iterator();iterator.hasNext();)
                	{
                		AppContext appContext = (AppContext)iterator.next();
                		mAppManager.removeApp(appContext);
                	}
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }

                mAppManager = null;
            }

			if (mTiVoListener != null) {
                try
                {
                	mTiVoListener.close();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }

                mTiVoListener = null;
            }

			if (mLongTermTimer != null) {
                try
                {
                	mLongTermTimer.cancel();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mLongTermTimer = null;
            }

			if (mShortTermTimer != null) {
                try
                {
                	mShortTermTimer.cancel();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mShortTermTimer = null;
            }

			if (mDataTimer != null) {
                try
                {
                	mDataTimer.cancel();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mDataTimer = null;
            }

			if (mRegistry != null) {
                try
                {
                	mRegistry.unbind("serverControl");
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mRegistry = null;
            }

			if (mTiVoBeacon != null) {
                try
                {
                	mTiVoBeacon.RevokeMediaServer(getPort());
                	mTiVoBeacon.Release();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mTiVoBeacon = null;
            }

            if (mConnectionThread != null) {
            	try
                {
            		mConnectionThread.interrupt();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mConnectionThread = null;
            }

            if (mListenThread != null) {
            	try
                {
            		mListenThread.interrupt();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
                mListenThread = null;
            }

            if (mBroadcastThread != null) {
            	try
                {
            		mBroadcastThread.interrupt();
            		mBroadcastThread.sendPackets(true);
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }

                mBroadcastThread = null;
            }

            if (mVideoServer != null) {
            	try
                {
            		mVideoServer.drain();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }

                mVideoServer = null;
            }

			if (mToGoThread != null) {
				try
                {
					mToGoThread.interrupt();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
				mToGoThread = null;
			}

			if (mDownloadThread != null) {
				try
                {
					mDownloadThread.interrupt();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
				mDownloadThread = null;
			}

			if (mVideoServer != null) {
				try
                {
					mVideoServer.drain();
                }
                catch (Throwable ex) {
                	Tools.logException(Server.class, ex);
                }
				mVideoServer = null;
			}

            try
            {
            	NetworkServerManager.shutdown();
            }
            catch (Throwable ex) {
            	Tools.logException(Server.class, ex);
            }

            mServer = null;
            mStartMain = false;

		} catch (Exception ex) {
			//System.runFinalization();
			//Tools.logException(Server.class, ex);
            throw ex;
		}
		finally {
			mToGoThread = null;
			mDownloadThread = null;
			mBroadcastThread = null;
            mListenThread = null;
            mConnectionThread = null;
            mTiVoBeacon = null;
		}
	}

	public void setReload(int reload) {
		mServerConfiguration.setReload(reload);
	}

	public int getReload() {
		return mServerConfiguration.getReload();
	}

	// Used by FileSystemTiVoContainer to determine if items or container should
	// be shuffled
	public void setShuffleItems(boolean shuffleItems) {
		mServerConfiguration.setShuffleItems(shuffleItems);
	}

	public boolean getShuffleItems() {
		return mServerConfiguration.getShuffleItems();
	}

	// Used by ImageTiVoContainer to determine if thumbnails should be generated
	// at startup
	public void setGenerateThumbnails(boolean generateThumbnails) {
		mServerConfiguration.setGenerateThumbnails(generateThumbnails);
	}

	public boolean getGenerateThumbnails() {
		return mServerConfiguration.getGenerateThumbnails();
	}

	private void printSystemProperties() {
		Properties properties = System.getProperties();
		Enumeration Enumeration = properties.propertyNames();
		for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
			String propertyName = (String) e.nextElement();
			log.info(propertyName + "=" + System.getProperty(propertyName));
		}
	}

	private void printServerProperties() {
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();

			log.info("Galleon Version=" + getVersion());
			log.info("Local IP=" + Tools.getLocalIpAddress());
			log.info("Host=" + inetAddress.getHostName());

			Tools.logMemory();
		} catch (UnknownHostException ex) {
			Tools.logException(Server.class, ex);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void reset() {
		if (log.isDebugEnabled())
			log.debug("reset()");
		log.info("Server update");
		try {
			if (mLongTermTimer != null) {
				mLongTermTimer.cancel();
				mLongTermTimer = null;
			}
		} catch (IllegalStateException ex) {
			Tools.logException(Server.class, ex);
		}
		mLongTermTimer = new Timer();
		try {
			if (mShortTermTimer != null) {
				mShortTermTimer.cancel();
				mShortTermTimer = null;
			}
		} catch (IllegalStateException ex) {
			Tools.logException(Server.class, ex);
		}
		mShortTermTimer = new Timer();
		try {
			if (mDataTimer != null) {
				mDataTimer.cancel();
				mDataTimer = null;
			}
		} catch (IllegalStateException ex) {
			Tools.logException(Server.class, ex);
		}
		mDataTimer = new Timer();
	}

	public synchronized void reconfigure() {
		if (log.isDebugEnabled())
			log.debug("reconfigure()");
		// Reset timer
		reset();

		// Load plugins
		// mPluginManager.destroyAllPlugins();
		// mPluginManager = null;
		// mPluginManager = new PluginManager(true);

		mServerConfiguration = null;
		mServerConfiguration = new ServerConfiguration();

		// Read the conf/configure.xml file
		mConfigurator = null;
		mConfigurator = new Configurator();
		mConfigurator.load(mAppManager);

		// mAppManager.startPlugins();
	}

	public void setIPAddress(String ipaddress) {
		mServerConfiguration.setIPAddress(ipaddress);
	}

	public String getIPAddress() {
		return mServerConfiguration.getIPAddress();
	}

	public void setPort(int value) {
		mServerConfiguration.setPort(value);
	}

	public int getPort() {
		if (mPort == -1) {
			mPort = Tools.findAvailablePort(mServerConfiguration.getPort());
			if (mPort != mServerConfiguration.getPort()) {
				log.info("Changed port to " + mPort);
			}
			else
				log.info("Using port " + mPort);
		}
		return mPort;
	}

	public int getHMOPort()
	{
		return mHMOPort;
	}

	public int getBeaconPort()
	{
		return mBeaconPort;
	}

	public void setName(String name) {
		mServerConfiguration.setName(name);
	}

	public String getName() {
		return mServerConfiguration.getName();
	}

	public void setVersion(String version) {
		mServerConfiguration.setVersion(version);
	}

	public Skin getSkin() {
		if (mSkin == null || !mSkin.getPath().equals(mServerConfiguration.getSkin())) {
			String skin = null;
			if (mServerConfiguration.getSkin().length() == 0) {
				try {
					File file = (File) getSkins().get(0);
					skin = file.getCanonicalPath();
				} catch (Exception ex) {
				}
			} else
				skin = mServerConfiguration.getSkin();
			if (skin != null)
				mSkin = new Skin(skin);
			else
				log.error("No skin configured.");
		}
		return mSkin;
	}

	public String getVersion() {
		return mServerConfiguration.getVersion();
	}

	// Singleton pattern
	public static synchronized Server getServer() {
		if (mServer == null) {
			try
			{
				mServer = new Server();
				mServer.start();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (true || System.getSecurityManager() instanceof CustomSecurityManager)
			return mServer;
		else
			return null;
	}

	public void save() {
		if (log.isDebugEnabled())
			log.debug("save()");
		try {
			mConfigurator.save(mAppManager);
			mLastModified = System.currentTimeMillis();
		} catch (Exception e) {
			Tools.logException(Server.class, e);
		}
	}

	class TaskInfo {
		TaskInfo(TimerTask task, long time) {
			this.task = task;
			this.time = time;
		}

		TimerTask task;

		long time;
	}

	public synchronized void scheduleLongTerm(TimerTask task, long time) {
		if (mReady)
			scheduleLongTerm(task, 0, time);
		else
			mLongTermTasks.add(new TaskInfo(task, time));
	}

	public synchronized void scheduleLongTerm(TimerTask task, long delay, long time) {
		if (log.isDebugEnabled())
			log.debug("Server schedule long term: " + task + " for " + time);
		if (time <= 0)
			time = getReload();
		try {
			mLongTermTimer.schedule(task, 1000 * delay, time * 1000 * 60);
		} catch (IllegalStateException ex) {
			Tools.logException(Server.class, ex);
			// Try again...
			reset();
			try {
				mLongTermTimer.schedule(task, 1000 * delay * 2, time * 1000 * 60);
			} catch (IllegalStateException ex2) {
				Tools.logException(Server.class, ex2);
			}
		}
	}

	public synchronized void scheduleShortTerm(TimerTask task, long time) {
		if (mReady)
			scheduleShortTerm(task, 0, time);
		else
			mShortTermTasks.add(new TaskInfo(task, time));
	}

	public synchronized void scheduleShortTerm(TimerTask task, long delay, long time) {
		if (log.isDebugEnabled())
			log.debug("Server schedule short term: " + task + " for " + time);
		if (time <= 0)
			time = getReload();
		try {
			mShortTermTimer.schedule(task, 1000 * delay, time * 1000 * 60);
		} catch (IllegalStateException ex) {
			Tools.logException(Server.class, ex);
			// Try again...
			reset();
			try {
				mShortTermTimer.schedule(task, 1000 * delay * 2, time * 1000 * 60);
			} catch (IllegalStateException ex2) {
				Tools.logException(Server.class, ex2);
			}
		}
	}

	public synchronized void scheduleData(TimerTask task, long time) {
		if (mReady)
			scheduleData(task, 0, time);
		else
			mDataTasks.add(new TaskInfo(task, time));
	}

	public synchronized void scheduleData(TimerTask task, long delay, long time) {
		if (log.isDebugEnabled())
			log.debug("Server schedule data: " + task + " for " + time);
		if (time <= 0)
			time = getReload();
		try {
			mDataTimer.schedule(task, 1000 * delay, time * 1000 * 60);
		} catch (IllegalStateException ex) {
			Tools.logException(Server.class, ex);
			// Try again...
			reset();
			try {
				mDataTimer.schedule(task, 1000 * delay * 2, time * 1000 * 60);
			} catch (IllegalStateException ex2) {
				Tools.logException(Server.class, ex2);
			}
		}
	}

	private void preLoadFonts() {
		if (log.isDebugEnabled())
			log.debug("preLoadFonts()");
		try {
			Font.createFont(Font.TRUETYPE_FONT, Server.class.getClassLoader().getResourceAsStream(
					ScrollText.class.getPackage().getName().replace('.', '/') + "/" + "default.ttf"));
		} catch (Throwable e) {
		}
	}

	/**
	 * @return last date configuration was modified
	 */
	public long getLastModified() {
		return mLastModified;
	}

	public AppManager getk() {
		return mAppManager;
	}

	public ToGoThread getToGoThread() {
		return mToGoThread;
	}

	public DownloadThread getDownloadThread() {
		return mDownloadThread;
	}

	public ServerConfiguration getServerConfiguration() {
		return mServerConfiguration;
	}

	public void updateServerConfiguration(ServerConfiguration serverConfiguration) throws Exception {
		boolean needRestart = true;

		try
		{
			/*
			 * if (mServerConfiguration.getIPAddress() == null ||
			 * serverConfiguration.getIPAddress() == null ||
			 * !mServerConfiguration.getIPAddress().equals(serverConfiguration.getIPAddress()) ||
			 * mServerConfiguration.getPort() != serverConfiguration.getPort())
			 * needRestart = true;
			 */
			mServerConfiguration = serverConfiguration;
			setDebugLogging(mServerConfiguration.isDebug());
			/*
			 * try { PropertyUtils.copyProperties(mServerConfiguration,
			 * serverConfiguration); } catch (Exception ex) { log.error("Server
			 * configuration update failed", ex); }
			 */

			save();
			// mToGoThread.interrupt();
			if (!mStartMain && needRestart) {
				log.info("Restarting for server configuration change");
				new Thread() {
					public void run() {
						WrapperManager.restart();
					}
				}.start();
			}
		}
		catch (Exception ex)
		{
			Tools.logException(Server.class, ex);
			throw ex;
		}
	}

	public void setDisableTimeout(boolean value)
	{
		try
		{
			mServerConfiguration.setDisableTimeout(value);
			save();
		}
		catch (Exception ex)
		{
			Tools.logException(Server.class, ex);
		}
	}

	public MusicPlayerConfiguration getMusicPlayerConfiguration() {
		return mServerConfiguration.getMusicPlayerConfiguration();
	}

	public void updateMusicPlayerConfiguration(MusicPlayerConfiguration musicPlayerConfiguration) throws Exception {
		try
		{
			if (musicPlayerConfiguration.isModified())
			{
				mServerConfiguration.setMusicPlayerConfiguration(musicPlayerConfiguration);
				save();
			}
		}
		catch (Exception ex)
		{
			Tools.logException(Server.class, ex);
			throw ex;
		}
	}

	public DataConfiguration getDataConfiguration() {
		return mServerConfiguration.getDataConfiguration();
	}

	public void updateDataConfiguration(DataConfiguration dataConfiguration) throws Exception {
		try
		{
			Users.login(dataConfiguration);
			Users.logout(dataConfiguration);

			mServerConfiguration.setDataConfiguration(dataConfiguration);
			save();
		}
		catch (Exception ex)
		{
			Tools.logException(Server.class, ex);
			throw ex;
		}
	}

	public GoBackConfiguration getGoBackConfiguration() {
		return mServerConfiguration.getGoBackConfiguration();
	}

	public void updateGoBackConfiguration(GoBackConfiguration goBackConfiguration) throws Exception {
		try
		{
			if (goBackConfiguration.isModified())
			{
				mServerConfiguration.setGoBackConfiguration(goBackConfiguration);
				save();
				if (mBroadcastThread!=null)
					mBroadcastThread.enableHighFrequency();
			}
		}
		catch (Exception ex)
		{
			Tools.logException(Server.class, ex);
			throw ex;
		}
	}

	public DownloadConfiguration getDownloadConfiguration() {
		return mServerConfiguration.getDownloadConfiguration();
	}

	public void updateDownloadConfiguration(DownloadConfiguration downloadConfiguration) throws Exception {
		try
		{
			if (downloadConfiguration.isModified())
			{
				mServerConfiguration.setDownloadConfiguration(downloadConfiguration);
				save();
			}
		}
		catch (Exception ex)
		{
			Tools.logException(Server.class, ex);
			throw ex;
		}
	}

	public List getAppDescriptors() {
		return mAppManager.getAppDescriptors();
	}

	public List getApps() {
		return mAppManager.getApps();
	}

	public List getTiVos() {
		// return mTiVoListener.getTiVos();
		return mServerConfiguration.getTiVos();
	}

	public void updateTiVos(List tivos) {
		mServerConfiguration.setTiVos(tivos);
		save();
		// mToGoThread.interrupt();
	}

	public void removeApp(AppContext app) {
		mAppManager.removeApp(app);
		save();
	}

	public void updateApp(AppContext app) {
		mAppManager.updateApp(app);
		save();
	}

	public void updateVideo(Video video) {
		try {
			VideoManager.updateVideo(video);
		} catch (Exception ex) {
			log.error("Video update failed", ex);
		}
		mDownloadThread.updateVideo(video);
	}

	public Video retrieveVideo(Video video) {
		try {
			return VideoManager.retrieveVideo(video);
		} catch (Exception ex) {
			log.error("Video retrieve failed", ex);
		}
		return null;
	}

	public void removeVideo(Video video) {
		try {
			VideoManager.deleteVideo(video);
		} catch (Exception ex) {
			log.error("Video delete failed", ex);
		}
	}

	public AppContext createAppContext(AppDescriptor appDescriptor) {
		return new AppContext(appDescriptor);
	}

	public List getRules() {
		return mServerConfiguration.getRules();
	}

	public void updateRules(List rules) {
		mServerConfiguration.setRules(rules);
		save();
		// mToGoThread.interrupt();
	}

	public List getWinampSkins() {
		File skinsDirectory = new File(System.getProperty("root") + "/media/winamp");
		if (skinsDirectory.isDirectory() && !skinsDirectory.isHidden()) {
			File[] files = skinsDirectory.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".wsz"); // Winamp skins
				}
			});
			return Arrays.asList(files);
		}

		return new ArrayList();
	}

	public List getSkins() {
		File skinsDirectory = new File(System.getProperty("skins"));
		if (skinsDirectory.isDirectory() && !skinsDirectory.isHidden()) {
			File[] files = skinsDirectory.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".gln"); // Galleon skins
				}
			});
			return Arrays.asList(files);
		}

		return new ArrayList();
	}

	public List getPodcasts() throws RemoteException {
		try {
			return PodcastManager.getPodcasts();
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
		return null;
	}

	public void setPodcasts(List list) throws RemoteException {
		try {
			PodcastManager.setPodcasts(list);
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
	}

	public List getVideocasts() throws RemoteException {
		try {
			return VideocastManager.getVideocasts();
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
		return null;
	}

	public void setVideocasts(List list) throws RemoteException {
		try {
			VideocastManager.setVideocasts(list);
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
	}

	public boolean isCurrentVersion() throws RemoteException {
		VersionCheck versionCheck = new VersionCheck();
		return versionCheck.isCurrentSourceforgeVersion();
	}

	private void createAppClassLoader() {
		ArrayList urls = new ArrayList();
		File directory = new File(System.getProperty("apps"));
		if (directory.exists())
		{
			// TODO Handle reloading; what if list changes?
			File[] files = directory.listFiles(new FileFilter() {
				public final boolean accept(File file) {
					return !file.isDirectory() && !file.isHidden() && file.getName().toLowerCase().endsWith(".jar");
				}
			});
			if (files!=null)
			{
				for (int i = 0; i < files.length; ++i) {
					try {
						URL url = files[i].toURI().toURL();
						urls.add(url);
						log.debug("Found app: " + url);
					} catch (Exception ex) {
						// should never happen
					}
				}
			}
		}

		directory = new File(System.getProperty("hme"));
		if (directory.exists())
		{
			// TODO Handle reloading; what if list changes?
			File[] files = directory.listFiles(new FileFilter() {
				public final boolean accept(File file) {
					return !file.isDirectory() && !file.isHidden() && file.getName().toLowerCase().endsWith(".jar");
				}
			});
			if (files!=null)
			{
				for (int i = 0; i < files.length; ++i) {
					try {
						URL url = files[i].toURI().toURL();
						urls.add(url);
						log.debug("Found HME app: " + url);
					} catch (Exception ex) {
						// should never happen
					}
				}
			}
		}

		mAppClassLoader = new URLClassLoader((URL[]) urls.toArray(new URL[0]));
	}

    public BroadcastThread getBroadcastThread() {
        return mBroadcastThread;
    }

    public ListenThread getListenThread() {
        return mListenThread;
    }

    public ConnectionThread getConnectionThread() {
        return mConnectionThread;
    }

    public void addTCM(TCM tcm) {
        mTCMs.add(tcm);
        if (!tcm.getBeacon().getIdentity().equals(Beacon.guid)
                && mServerConfiguration.addTiVo(new TiVo(tcm.getBeacon().getMachine(), tcm.getAddress()
                        .getHostAddress())))
            save();
    }

    public void removeTCM(TCM tcm) {
        mTCMs.remove(tcm);
    }

    public TCM getTCM(Beacon beacon) {
        Iterator iterator = mTCMs.iterator();
        while (iterator.hasNext()) {
            TCM tcm = (TCM) iterator.next();
            try {
                if (tcm.getBeacon().getIdentity().equals(beacon.getIdentity()))
                    return tcm;
            } catch (Exception ex) {
                Tools.logException(Server.class, ex);
            }
        }
        return null;
    }

    public TCM getTCM(InetAddress address) {
        Iterator iterator = mTCMs.iterator();
        while (iterator.hasNext()) {
            TCM tcm = (TCM) iterator.next();
            if (tcm.getAddress().equals(address))
                return tcm;
        }
        return null;
    }

    public Iterator getTCMIterator() {
        return mTCMs.iterator();
    }

    private void publishTiVoBeacon() {
        // TiVo Beacon API
        mPublished = false;
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
            	mTiVoBeacon = new TiVoBeacon(TiVoBeacon.CLSID);
                mTiVoBeacon.PublishMediaServer(mHMOPort);
                mPublished = true;
                log.info("Using TiVo Beacon service");
            } catch (Throwable ex) {
            	if (ex.getCause()!=null)
            		Tools.logException(Server.class, ex, ex.getCause().toString());
            	else
            		Tools.logException(Server.class, ex);
                mTiVoBeacon = null;
                log.info("Could not find TiVo Beacon service");
            }
        }
    }

    public void publishVideo(NameValue nameValue)
    {
    	if (mVideoServer!=null)
    	{
    		mVideoServer.publish(nameValue);
    	}
    	else
    	{
    		mPublishedVideos.add(nameValue);
    	}
    }

    public void addDownload(Download download, StatusListener statusListener)
    {
    	mDownloadManager.addDownload(download, statusListener);
    }

    public List getDownloads() throws RemoteException {
		try {
			return mDownloadManager.getDownloads();
		} catch (Exception ex) {
			Tools.logException(Server.class, ex);
		}
		return null;
	}

    public void pauseDownload(Download download) throws RemoteException
    {
    	mDownloadManager.pauseDownload(download);
    }

	public void resumeDownload(Download download) throws RemoteException
	{
		mDownloadManager.resumeDownload(download);
	}

	public void stopDownload(Download download) throws RemoteException
	{
		mDownloadManager.stopDownload(download);
	}

	public boolean isFileExists(String path)
	{
		File file = new File(path);
		return file.exists();
	}

	public void deleteFile(String path)
	{
		File file = new File(path);
		boolean found = false;
		// Try to resync video file with database if moved
		try {

			List list = VideoManager.findByFilename(file.getName());

			if (list != null && list.size() == 1) {

				for (int j=0;j<list.size();j++)
				{
					Video video = (Video) list.get(j);
					if (!video.getPath().equals(file.getCanonicalPath()))
					{
						video.setPath(file.getCanonicalPath());
						VideoManager.updateVideo(video);
						found = true;
						break;
					}
				}
			}

		} catch (Exception ex) {

			log.error("Video find failed", ex);
		}

		if (!found && file.exists())
			file.delete();
	}

	public List getAppUrls(boolean shared)
	{
		return mAppManager.getAppUrls(shared);
	}

	public boolean isDemoMode()
	{
		return mDemoMode;
	}

	public static void main(String args[]) {
		mStartMain = true;
		try
		{
			Server server = getServer();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private Logger log;

	private static Server mServer;

	private Timer mLongTermTimer;

	private Timer mShortTermTimer;

	private Timer mDataTimer;

	private Configurator mConfigurator;

	private long mLastModified = System.currentTimeMillis();

	private ServerConfiguration mServerConfiguration;

	private AppManager mAppManager;

	private ToGoThread mToGoThread;

	private DownloadThread mDownloadThread;

	private static Registry mRegistry;

	private static TiVoListener mTiVoListener;

	private static URLClassLoader mAppClassLoader;

	private static Skin mSkin;

	private static boolean mStartMain;

	private static boolean mReady;

	private int mPort = -1;

	private int mHMOPort = -1;

	private int mBeaconPort = -1;

	private ArrayList mShortTermTasks;

	private ArrayList mLongTermTasks;

	private ArrayList mDataTasks;

	private BroadcastThread mBroadcastThread;

    private TiVoBeacon mTiVoBeacon;

    private ListenThread mListenThread;

    private ConnectionThread mConnectionThread;

    private LinkedList mTCMs;

    private boolean mPublished;

    private VideoServer mVideoServer;

    private List mPublishedVideos = new ArrayList();

    private DownloadManager mDownloadManager;

    //private DataUpdateThread mDataUpdateThread;

    private static boolean mDemoMode;
}