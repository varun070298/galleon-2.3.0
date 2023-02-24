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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.MaskFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppDescriptor;
import org.lnicholls.galleon.app.ConfigurationPanel;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.DataConfiguration;
import org.lnicholls.galleon.server.GoBackConfiguration;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.gui.FileOptionsTable;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.debug.*;
import com.jgoodies.plaf.BorderStyle;
import com.jgoodies.plaf.HeaderStyle;

import edu.stanford.ejalbert.BrowserLauncher;

public class MainFrame extends JFrame {
	private static Logger log = Logger.getLogger(MainFrame.class.getName());
	

	public MainFrame(String version) {
		super("Galleon " + version);
		
		setDefaultCloseOperation(0);

		JMenuBar menuBar = new JMenuBar();
		menuBar.putClientProperty("jgoodies.headerStyle", HeaderStyle.BOTH);
		menuBar.putClientProperty("jgoodies.windows.borderStyle", BorderStyle.SEPARATOR);
		menuBar.putClientProperty("Plastic.borderStyle", BorderStyle.SEPARATOR);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		fileMenu.add(new MenuAction("New App...", null, "", new Integer(KeyEvent.VK_N)) {

			public void actionPerformed(ActionEvent event) {
				new AddAppDialog(Galleon.getMainFrame()).setVisible(true);
			}

		});
		fileMenu.addSeparator();
		fileMenu.add(new MenuAction("Properties...", null, "", new Integer(KeyEvent.VK_P)) {

			public void actionPerformed(ActionEvent event) {
				new ServerDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
			}

		});
		/*
		fileMenu.add(new MenuAction("Galleon.tv Account...", null, "", new Integer(KeyEvent.VK_A)) {

			public void actionPerformed(ActionEvent event) {
				new DataDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
			}

		});
		*/
		fileMenu.add(new MenuAction("Download Manager...", null, "", new Integer(KeyEvent.VK_D)) {

			public void actionPerformed(ActionEvent event) {
				new DownloadManagerDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
			}

		});		
		fileMenu.add(new MenuAction("GoBack...", null, "", new Integer(KeyEvent.VK_G)) {

			public void actionPerformed(ActionEvent event) {
				new GoBackDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
			}

		});		
		fileMenu.add(new MenuAction("Music Player...", null, "", new Integer(KeyEvent.VK_M)) {

			public void actionPerformed(ActionEvent event) {
				new MusicPlayerDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
			}

		});
		fileMenu.add(new MenuAction("ToGo...", null, "", new Integer(KeyEvent.VK_T)) {

			public void actionPerformed(ActionEvent event) {
				new ToGoDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
			}

		});
		fileMenu.addSeparator();
		fileMenu.add(new MenuAction("Exit", null, "", new Integer(KeyEvent.VK_X)) {

			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}

		});

		menuBar.add(fileMenu);

		JMenu tutorialMenu = new JMenu("Tutorials");
		tutorialMenu.setMnemonic('T');
		tutorialMenu.putClientProperty("jgoodies.noIcons", Boolean.TRUE);
		tutorialMenu.add(new MenuAction("Properties", null, "", new Integer(KeyEvent.VK_P)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/88/48/");
				} catch (Exception ex) {
				}
			}

		});
		tutorialMenu.add(new MenuAction("Music Player", null, "", new Integer(KeyEvent.VK_M)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/88/48/");
				} catch (Exception ex) {
				}
			}

		});
		tutorialMenu.addSeparator();
		tutorialMenu.add(new MenuAction("Email", null, "", new Integer(KeyEvent.VK_E)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/88/48/");
				} catch (Exception ex) {
				}
			}

		});
		tutorialMenu.add(new MenuAction("Music", null, "", new Integer(KeyEvent.VK_U)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/88/48/");
				} catch (Exception ex) {
				}
			}

		});
		tutorialMenu.add(new MenuAction("Podcasting", null, "", new Integer(KeyEvent.VK_O)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/88/48/");
				} catch (Exception ex) {
				}
			}

		});
		tutorialMenu.add(new MenuAction("ToGo", null, "", new Integer(KeyEvent.VK_T)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/88/48/");
				} catch (Exception ex) {
				}
			}

		});
		
		menuBar.add(tutorialMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		helpMenu.putClientProperty("jgoodies.noIcons", Boolean.TRUE);
		helpMenu.add(new MenuAction("Homepage", null, "", new Integer(KeyEvent.VK_H)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv");
				} catch (Exception ex) {
				}
			}

		});
		helpMenu.add(new MenuAction("Configuration", null, "", new Integer(KeyEvent.VK_C)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/93/52/");
				} catch (Exception ex) {
				}
			}

		});
		helpMenu.add(new MenuAction("FAQ", null, "", new Integer(KeyEvent.VK_F)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/section/3/47/");
				} catch (Exception ex) {
				}
			}

		});
		/*
		helpMenu.add(new MenuAction("TiVo Community Forum", null, "", new Integer(KeyEvent.VK_T)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://www.tivocommunity.com/tivo-vb/forumdisplay.php?f=35");
				} catch (Exception ex) {
				}
			}

		});
		*/
		helpMenu.add(new MenuAction("Galleon Forum", null, "", new Integer(KeyEvent.VK_G)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/component/option,com_joomlaboard/Itemid,26/");
				} catch (Exception ex) {
				}
			}

		});
		helpMenu.add(new MenuAction("File a bug", null, "", new Integer(KeyEvent.VK_B)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://sourceforge.net/tracker/?atid=705256&group_id=126291&func=browse");
				} catch (Exception ex) {
				}
			}

		});
		helpMenu.add(new MenuAction("Request a feature", null, "", new Integer(KeyEvent.VK_E)) {

			public void actionPerformed(ActionEvent event) {
				try {
					BrowserLauncher.openURL("http://sourceforge.net/tracker/?atid=705259&group_id=126291&func=browse");
				} catch (Exception ex) {
				}
			}

		});
		helpMenu.addSeparator();
		helpMenu.add(new MenuAction("About...", null, "", new Integer(KeyEvent.VK_A)) {

			public void actionPerformed(ActionEvent event) {
				JOptionPane
						.showMessageDialog(
								Galleon.getMainFrame(),
								"Galleon Version "
										+ Tools.getVersion()
										+ "\nJava Version "
										+ System.getProperty("java.vm.version")
										+ "\nPublishing Port "
										+ Galleon.getHttpPort()
										+ "\nApplication Port "
										+ Galleon.getPort()
										+ "\nhttp://galleon.tv\njavahmo@users.sourceforge.net\nCopyright \251 2005, 2006 Leon Nicholls. All Rights Reserved.",
								"About", JOptionPane.INFORMATION_MESSAGE);
			}

		});
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);
		JComponent content = createContentPane();
		setContentPane(content);

		pack();
		Dimension paneSize = getSize();
		Dimension screenSize = getToolkit().getScreenSize();
		setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);

		URL url = getClass().getClassLoader().getResource("guiicon.gif");

		ImageIcon logo = new ImageIcon(url);
		if (logo != null)
			setIconImage(logo.getImage());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

		});
	}

	protected JComponent createContentPane() {
		JPanel panel = new JPanel(new BorderLayout());

		mOptionsPanelManager = new OptionsPanelManager(this);
		mOptionsPanelManager.setMinimumSize(new Dimension(200, 100));
		mOptionsPanelManager.setPreferredSize(new Dimension(400, 250));

		InternalFrame navigator = new InternalFrame("Apps");
		mAppTree = new AppTree(this, getAppsModel());
		navigator.setContent(createScrollPane(mAppTree));
		navigator.setSelected(true);
		navigator.setMinimumSize(new Dimension(100, 100));
		navigator.setPreferredSize(new Dimension(150, 400));

		JSplitPane mainSplitPane = createSplitPane(1, navigator, mOptionsPanelManager, 0.25D);
		mainSplitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		panel.add(mainSplitPane, "Center");

		JLabel statusField = new JLabel("Copyright \251 2005, 2006 Leon Nicholls");
		statusField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(statusField, "West");
		panel.add(statusPanel, "South");

		panel.setPreferredSize(new Dimension(700, 450));
		return panel;
	}

	public static JScrollPane createScrollPane(Component component) {
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	public static JSplitPane createSplitPane(int orientation, Component comp1, Component comp2, double resizeWeight) {
		JSplitPane split = new JSplitPane(1, false, comp1, comp2);
		split.setBorder(new EmptyBorder(0, 0, 0, 0));
		split.setOneTouchExpandable(false);
		split.setResizeWeight(resizeWeight);
		return split;
	}

	public void handleAppSelection(AppNode appNode) {
		mOptionsPanelManager.setSelectedOptionsPanel(appNode);
	}

	public DefaultTreeModel getAppsModel() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
		if (Galleon.getApps() != null) {
			Iterator iterator = Galleon.getApps().iterator();
			while (iterator.hasNext()) {
				AppContext app = (AppContext) iterator.next();
				root.add(new DefaultMutableTreeNode(getAppNode(app)));
			}
		}
		return new DefaultTreeModel(root);
	}

	private AppNode getAppNode(AppContext app) {
		AppDescriptor appDescriptor = app.getDescriptor();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		ImageIcon icon = null;
		try {
			String pkg = Tools.getPackage(appDescriptor.getClassName());
			URL url = classLoader.getResource(pkg + "/icon.png");
			if (url == null)
				url = classLoader.getResource("icon.png");
			icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		} catch (Exception ex) {
			Tools.logException(OptionsPanelManager.class, ex, "Could not load icon " + " for app "
					+ appDescriptor.getClassName());
		}

		if (!appDescriptor.isHME()) {
			AppConfigurationPanel appConfigurationPanel = null;
			try {
				Class configurationPanel = classLoader.loadClass(appDescriptor.getConfigurationPanel());

				Class[] parameters = new Class[1];
				parameters[0] = AppConfiguration.class;
				Constructor constructor = configurationPanel.getConstructor(parameters);
				AppConfiguration[] values = new AppConfiguration[1];
				values[0] = (AppConfiguration) app.getConfiguration();

				appConfigurationPanel = (AppConfigurationPanel) constructor.newInstance((Object[]) values);
			} catch (Exception ex) {
				ex.printStackTrace();
				Tools.logException(OptionsPanelManager.class, ex, "Could not load configuration panel "
						+ appDescriptor.getConfigurationPanel() + " for app " + appDescriptor.getClassName());
			}

			AppNode appNode = new AppNode(app, icon, appConfigurationPanel);
			return appNode;
		} else {
			return new AppNode(app, icon, new HMEConfigurationPanel(app.getConfiguration()));
		}
	}

	public void addApp(AppContext app) {
		if (log.isDebugEnabled())
			log.debug("addApp: " + app);
		mAppTree.addApp(getAppNode(app));
	}

	public void removeApp(AppContext app) {
		if (log.isDebugEnabled())
			log.debug("removeApp: " + app);
		AppNode appNode = getAppNode(app);
		Galleon.removeApp(app);
		mAppTree.removeApp(appNode);
	}

	public void refresh() {
		mAppTree.refresh();
	}

	class MenuAction extends AbstractAction {
		public MenuAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	public class AddAppDialog extends JDialog implements ActionListener, ItemListener, KeyListener {

		class AppDescriptorWrapper {
			public AppDescriptorWrapper(AppDescriptor appDescriptor) {
				mAppDescriptor = appDescriptor;
			}

			public String toString() {
				return mAppDescriptor.getTitle();
			}

			AppDescriptor mAppDescriptor;
		}

		private AddAppDialog(MainFrame frame) {
			super(frame, "New App", true);
			mMainFrame = frame;

			mOKButton = new JButton("OK");
			mNameField = new JTextField();
			mNameField.addKeyListener(this);
			mVersionField = new JTextField();
			mVersionField.setEditable(false);
			mReleaseDateField = new JTextField();
			mReleaseDateField.setEditable(false);
			mHomepageField = new JTextField();
			mHomepageField.setEditable(false);
			mHomepageField.setToolTipText("Open site in web browser");
			mHomepageField.setForeground(Color.blue);
			mHomepageField.setCursor(new Cursor(Cursor.HAND_CURSOR));
			mHomepageField.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					try {
						BrowserLauncher.openURL(mHomepageField.getText());
					} catch (Exception ex) {
					}
				}
			});
			mAuthorNameField = new JTextField();
			mAuthorNameField.setEditable(false);
			mAuthorEmailField = new JTextField();
			mAuthorEmailField.setEditable(false);
			mAuthorHomeField = new JTextField();
			mAuthorHomeField.setEditable(false);
			mAuthorHomeField.setToolTipText("Open site in web browser");
			mAuthorHomeField.setForeground(Color.blue);
			mAuthorHomeField.setCursor(new Cursor(Cursor.HAND_CURSOR));
			mAuthorHomeField.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					try {
						BrowserLauncher.openURL(mAuthorHomeField.getText());
					} catch (Exception ex) {
					}
				}
			});
			mDocumentationField = new JTextPane();
			mDocumentationField.setEditable(false);
			mAppsCombo = new JComboBox();
			mAppsCombo.addItemListener(this);

			JScrollPane paneScrollPane = new JScrollPane(mDocumentationField);
			paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			paneScrollPane.setPreferredSize(new Dimension(250, 150));
			paneScrollPane.setMinimumSize(new Dimension(10, 10));

			AppDescriptor[] appDescriptors = (AppDescriptor[]) Galleon.getAppDescriptors()
					.toArray(new AppDescriptor[0]);
			Arrays.sort(appDescriptors, new Comparator() {
				public int compare(Object o1, Object o2) {
					AppDescriptor appDescriptor1 = (AppDescriptor) o1;
					AppDescriptor appDescriptor2 = (AppDescriptor) o2;

					return appDescriptor1.getTitle().toLowerCase().compareTo(appDescriptor2.getTitle().toLowerCase());
				}
			});
			for (int i = 0; i < appDescriptors.length; i++) {
				AppDescriptor appDescriptor = appDescriptors[i];
				mAppsCombo.addItem(new AppDescriptorWrapper(appDescriptor));
			}
			getContentPane().setLayout(new BorderLayout());

			FormLayout layout = new FormLayout("right:pref, 3dlu, 220dlu:g, 3dlu, right:pref:grow", "pref, " + // name
					"9dlu, " + "pref, " + // apps
					"3dlu, " + "pref, " + // type
					"9dlu, " + "pref, " + // description
					"3dlu, " + "pref, " + // version
					"3dlu, " + "pref, " + // release date
					"3dlu, " + "pref, " + // homepage
					"9dlu, " + "pref, " + // author
					"3dlu, " + "pref, " + // name
					"3dlu, " + "pref, " + // email
					"9dlu, " + "pref, " + // apps
					"3dlu, " + "pref, " // homepage
			);

			PanelBuilder builder = new PanelBuilder(layout);
			// DefaultFormBuilder builder = new DefaultFormBuilder(new
			// FormDebugPanel(), layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.addSeparator("Apps", cc.xyw(1, 1, 5));
			builder.addLabel("Type", cc.xy(1, 3));
			builder.add(mAppsCombo, cc.xy(3, 3));
			builder.addSeparator("Description", cc.xyw(1, 5, 5));
			builder.add(paneScrollPane, cc.xywh(5, 7, 1, 11, CellConstraints.RIGHT, CellConstraints.TOP));
			builder.addLabel("Version", cc.xy(1, 7));
			builder.add(mVersionField, cc.xy(3, 7));
			builder.addLabel("Release Date", cc.xy(1, 9));
			builder.add(mReleaseDateField, cc.xy(3, 9));
			builder.addLabel("Homepage", cc.xy(1, 11));
			builder.add(mHomepageField, cc.xy(3, 11));
			builder.addSeparator("Author", cc.xyw(1, 13, 3));
			builder.addLabel("Name", cc.xy(1, 15));
			builder.add(mAuthorNameField, cc.xy(3, 15));
			builder.addLabel("Email", cc.xy(1, 17));
			builder.add(mAuthorEmailField, cc.xy(3, 17));
			builder.addLabel("Homepage", cc.xy(1, 19));
			builder.add(mAuthorHomeField, cc.xy(3, 19));
			builder.addSeparator("Custom", cc.xyw(1, 21, 5));
			builder.addLabel("Title", cc.xy(1, 23));
			builder.add(mNameField, cc.xy(3, 23));

			getContentPane().add(builder.getPanel(), "Center");

			JButton[] array = new JButton[3];
			array[0] = mOKButton;
			array[0].setActionCommand("ok");
			array[0].addActionListener(this);
			//array[0].setEnabled(false);
			array[1] = new JButton("Cancel");
			array[1].setActionCommand("cancel");
			array[1].addActionListener(this);
			array[2] = new JButton("Help");
			array[2].setActionCommand("help");
			array[2].addActionListener(this);
			JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttons, "South");
			pack();
			setLocationRelativeTo(frame);
		}

		public void actionPerformed(ActionEvent e) {
			if ("ok".equals(e.getActionCommand())) {
				AppDescriptor appDescriptor = ((AppDescriptorWrapper) mAppsCombo.getSelectedItem()).mAppDescriptor;
				try {
					// AppContext app = new AppContext(appDescriptor);
					AppContext app = Galleon.createAppContext(appDescriptor);
					app.setTitle(mNameField.getText());
					addApp(app);
				} catch (Exception ex) {
					ex.printStackTrace();
					Tools.logException(AddAppDialog.class, ex, "Could not add app : " + appDescriptor);
				}
			} else if ("help".equals(e.getActionCommand())) {
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/98/27/");
				} catch (Exception ex) {
				}
				return;
			}
			this.setVisible(false);
		}

		public void itemStateChanged(ItemEvent e) {
			int state = e.getStateChange();
			if (state == ItemEvent.SELECTED) {
				AppDescriptor appDescriptor = ((AppDescriptorWrapper) mAppsCombo.getSelectedItem()).mAppDescriptor;
				mVersionField.setText(appDescriptor.getVersion());
				mReleaseDateField.setText(appDescriptor.getReleaseDate());
				mHomepageField.setText(appDescriptor.getDocumentation());
				mAuthorNameField.setText(appDescriptor.getAuthorName());
				mAuthorEmailField.setText(appDescriptor.getAuthorEmail());
				mAuthorHomeField.setText(appDescriptor.getAuthorHomepage());
				mDocumentationField.setText(appDescriptor.getDescription());
				
				mNameField.setText(appDescriptor.getTitle());
				checkDups();
			} else {
				mVersionField.setText("");
				mReleaseDateField.setText("");
				mHomepageField.setText("");
				mAuthorNameField.setText("");
				mAuthorEmailField.setText("");
				mAuthorHomeField.setText("");
				mDocumentationField.setText("");
				
				mNameField.setText("");
			}
		}

		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			checkDups();
		}
		
		private void checkDups()
		{
			String name = mNameField.getText();
			if (name.length() > 0) {
				if (mApps==null)
					mApps = Galleon.getApps();
				if (mApps != null) {
					Iterator iterator = mApps.iterator();
					while (iterator.hasNext()) {
						AppContext app = (AppContext) iterator.next();
						if (app.getTitle()!=null && app.getTitle().equals(name)) {
							mOKButton.setEnabled(false);
							return;
						}
					}
				}
				mOKButton.setEnabled(true);
				return;
			}
			mOKButton.setEnabled(false);
		}

		private JTextField mNameField;

		private JComboBox mAppsCombo;

		private JTextField mVersionField;

		private JTextField mReleaseDateField;
		
		private JTextField mHomepageField;

		private JTextField mAuthorNameField;

		private JTextField mAuthorEmailField;

		private JTextField mAuthorHomeField;

		private JTextPane mDocumentationField;

		private JButton mOKButton;
		
		private MainFrame mMainFrame;
		
		private List mApps;
	}

	public class ServerDialog extends JDialog implements ActionListener {

		class NameValueWrapper extends NameValue {
			public NameValueWrapper(String name, String value) {
				super(name, value);
			}

			public String toString() {
				return getName();
			}
		}

		private ServerDialog(MainFrame frame, ServerConfiguration serverConfiguration) {
			super(frame, "Server Properties", true);
			mMainFrame = frame;
			mServerConfiguration = serverConfiguration;

			// enable debug logging

			mNameField = new JTextField();
			mNameField.setText(serverConfiguration.getName());
			mVersionField = new JTextField();
			mVersionField.setEditable(false);
			mVersionField.setText(serverConfiguration.getVersion());
			mReloadCombo = new JComboBox();
			mReloadCombo.addItem(new NameValueWrapper("5 minutes", "5"));
			mReloadCombo.addItem(new NameValueWrapper("10 minutes", "10"));
			mReloadCombo.addItem(new NameValueWrapper("20 minutes", "20"));
			mReloadCombo.addItem(new NameValueWrapper("30 minutes", "30"));
			mReloadCombo.addItem(new NameValueWrapper("1 hour", "60"));
			mReloadCombo.addItem(new NameValueWrapper("2 hours", "120"));
			mReloadCombo.addItem(new NameValueWrapper("4 hours", "240"));
			mReloadCombo.addItem(new NameValueWrapper("6 hours", "720"));
			mReloadCombo.addItem(new NameValueWrapper("24 hours", "1440"));
			defaultCombo(mReloadCombo, Integer.toString(serverConfiguration.getReload()));
			mSkinCombo = new JComboBox();
			mSkinCombo.setToolTipText("Select a skin for the Galleon apps");
			List skins = Galleon.getSkins();
			Iterator iterator = skins.iterator();
			String defaultSkin = "";
			while (iterator.hasNext()) {
				File file = (File) iterator.next();
				try {
					String name = Tools.extractName(file.getCanonicalPath());
					mSkinCombo.addItem(new NameValueWrapper(name, file.getCanonicalPath()));
					if (defaultSkin.length() == 0)
						defaultSkin = file.getCanonicalPath();
				} catch (Exception ex) {
				}
			}
			defaultCombo(mSkinCombo, serverConfiguration.getSkin().length() == 0 ? defaultSkin : serverConfiguration
					.getSkin());
			mGenerateThumbnails = new JCheckBox("Generate Thumbnails");
			mGenerateThumbnails.setSelected(serverConfiguration.getGenerateThumbnails());
			mShuffleItems = new JCheckBox("Shuffle Items");
			mShuffleItems.setSelected(serverConfiguration.getShuffleItems());
			mDebug = new JCheckBox("Debug logging");
			mDebug.setSelected(serverConfiguration.isDebug());
			mDisableTimeout = new JCheckBox("Disable Timeout");
			mDisableTimeout.setSelected(serverConfiguration.isDisableTimeout());
			mMenu = new JCheckBox("Menu");
			mMenu.setSelected(serverConfiguration.isMenu());
			mPort = new JTextField();
			mPort.setText(String.valueOf(serverConfiguration.getPort()));
			mHTTPPort = new JTextField();
			mHTTPPort.setText(String.valueOf(serverConfiguration.getHttpPort()));
			mIPAddress = new JComboBox();
			mIPAddress.addItem(new NameValueWrapper("Default", ""));
			try {
				Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
				while (enumeration.hasMoreElements()) {
					NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
					Enumeration inetAddressEnumeration = networkInterface.getInetAddresses();
					while (inetAddressEnumeration.hasMoreElements()) {
						InetAddress inetAddress = (InetAddress) inetAddressEnumeration.nextElement();
						if (!inetAddress.getHostAddress().equals("127.0.0.1"))
							mIPAddress.addItem(new NameValueWrapper(inetAddress.getHostAddress(), inetAddress
									.getHostAddress()));
					}
				}
			} catch (Exception ex) {
				Tools.logException(MainFrame.class, ex, "Could not get network interfaces");
			}
			defaultCombo(mIPAddress, serverConfiguration.getIPAddress());
			mRecordingsPath = new JTextField();
			mRecordingsPath.setText(serverConfiguration.getRecordingsPath());
			mMediaAccessKey = new JTextField();
			mMediaAccessKey.setText(Tools.decrypt(serverConfiguration.getMediaAccessKey()));
			mPasswordField = new JPasswordField();
			mPasswordField.setText(Tools.decrypt(serverConfiguration.getPassword()));
			mPublicIPAddressField = new JTextField();
			mPublicIPAddressField.setText(serverConfiguration.getPublicIPAddress());
			// TODO Only digits
			mPINField = new JPasswordField();
			mPINField.setText(Tools.decrypt(serverConfiguration.getPin()));

			getContentPane().setLayout(new BorderLayout());

			FormLayout layout = new FormLayout("right:pref, 3dlu, pref, left:pref, 3dlu, right:pref:grow", "pref, " + // settings
					"6dlu, " + "pref, " + // name
					"3dlu, " + "pref, " + // version
					"3dlu, " + "pref, " + // reload
					"3dlu, " + "pref, " + // reload
					"3dlu, " + "pref, " + // generatethumbnails
					"3dlu, " + "pref, " + // menu
					// streamingproxy
					"3dlu, " + "pref, " + // debug
					"3dlu, " + "pref, " + // timeout
					"3dlu, " + "pref, " + // recordings path
					"3dlu, " + "pref, " + // media access key
					"3dlu, " + "pref, " + // password
					"9dlu, " + "pref, " + // network
					"6dlu, " + "pref, " + // port
					"6dlu, " + "pref, " + // http port
					"3dlu, " + "pref, " + // address
					"3dlu, " + "pref, " + // public ip
					"3dlu, " + "pref" // pin
			);

			PanelBuilder builder = new PanelBuilder(layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.addSeparator("Settings", cc.xyw(1, 1, 6));
			builder.addLabel("Name", cc.xy(1, 3));
			builder.add(mNameField, cc.xyw(3, 3, 2));
			builder.addLabel("Version", cc.xy(1, 5));
			builder.add(mVersionField, cc.xyw(3, 5, 2));
			builder.addLabel("Reload", cc.xy(1, 7));
			builder.add(mReloadCombo, cc.xyw(3, 7, 2));
			builder.addLabel("Skin", cc.xy(1, 9));
			builder.add(mSkinCombo, cc.xyw(3, 9, 2));
			// TODO Only show for Windows
			builder.add(mGenerateThumbnails, cc.xy(3, 11));
			builder.add(mDebug, cc.xy(3, 13));
			builder.add(mDisableTimeout, cc.xy(3, 15));
			builder.add(mMenu, cc.xy(3, 17));
			JButton button = new JButton("...");
			button.setActionCommand("pick");
			button.addActionListener(this);
			builder.addLabel("Recordings Path", cc.xy(1, 19));
			builder.add(mRecordingsPath, cc.xyw(3, 19, 2));
			builder.add(button, cc.xyw(6, 19, 1));
			builder.addLabel("Media Access Key", cc.xy(1, 21));
			builder.add(mMediaAccessKey, cc.xyw(3, 21, 2));
			builder.addLabel("Parental Controls Password", cc.xy(1, 23));
			builder.add(mPasswordField, cc.xyw(3, 23, 2));

			builder.addSeparator("Network", cc.xyw(1, 25, 6));
			builder.addLabel("PC Application Port", cc.xy(1, 27));
			builder.add(mPort, cc.xy(3, 27));
			if (serverConfiguration.getPort() != Galleon.getPort()) {
				builder.addLabel("(" + Galleon.getPort() + ")", cc.xy(4, 27));
			}
			builder.addLabel("PC Publishing Port", cc.xy(1, 29));
			builder.add(mHTTPPort, cc.xy(3, 29));
			if (serverConfiguration.getHttpPort() != Galleon.getHTTPPort()) {
				builder.addLabel("(" + Galleon.getHTTPPort() + ")", cc.xy(4, 29));
			}
			builder.addLabel("PC IP Address", cc.xy(1, 31));
			builder.add(mIPAddress, cc.xy(3, 31));
			button = new JButton("<< Test...");
			button.setActionCommand("network");
			button.addActionListener(this);
			builder.add(button, cc.xyw(5, 31, 2));
			builder.addLabel("Public IP Address", cc.xy(1, 33));
			builder.add(mPublicIPAddressField, cc.xyw(3, 33, 2));
			builder.addLabel("PIN", cc.xy(1, 35));
			builder.add(mPINField, cc.xyw(3, 35, 2));

			getContentPane().add(builder.getPanel(), "Center");

			JButton[] array = new JButton[3];
			array[0] = new JButton("OK");
			array[0].setActionCommand("ok");
			array[0].addActionListener(this);
			array[1] = new JButton("Cancel");
			array[1].setActionCommand("cancel");
			array[1].addActionListener(this);
			array[2] = new JButton("Help");
			array[2].setActionCommand("help");
			array[2].addActionListener(this);
			JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttons, "South");
			pack();
			setLocationRelativeTo(frame);
		}

		public void defaultCombo(JComboBox combo, String value) {
			for (int i = 0; i < combo.getItemCount(); i++) {
				if (((NameValue) combo.getItemAt(i)).getValue().equals(value)) {
					combo.setSelectedIndex(i);
					return;
				}
			}
		}
		
		public String valid()
		{
			String port = mPort.getText().trim();
			for (int i=0;i<port.length();i++)
			{
				if (!Character.isDigit(port.charAt(i)))
						return "PC Application Port can only contain digits";
			}
			
			String httpPort = mHTTPPort.getText().trim();
			for (int i=0;i<httpPort.length();i++)
			{
				if (!Character.isDigit(httpPort.charAt(i)))
						return "PC Publishing Port can only contain digits";
			}
			
			String password = mPasswordField.getText().trim();
			if (password.length()>0)
			{
				if (password.length()!=4)
					return "Parental Controls Password must be 4 digits";
				for (int i=0;i<password.length();i++)
				{
					if (!Character.isDigit(password.charAt(i)))
							return "Parental Controls Password can only contain digits";
				}
			}
			
			String pin = mPINField.getText().trim();
			if (pin.length()>0)
			{
				if (pin.length()<4)
					return "PIN must be at least 4 digits";
				for (int i=0;i<pin.length();i++)
				{
					if (!Character.isDigit(pin.charAt(i)))
							return "PIN can only contain digits";
				}
			}
			
			return null;
		}

		public void actionPerformed(ActionEvent e) {
			if ("ok".equals(e.getActionCommand())) {
				String error = valid();
				if (error!=null)
				{
					JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					mServerConfiguration.setName(mNameField.getText());
					mServerConfiguration.setReload(Integer.parseInt(((NameValue) mReloadCombo.getSelectedItem())
							.getValue()));
					mServerConfiguration.setSkin(((NameValue) mSkinCombo.getSelectedItem()).getValue());
					try {
						mServerConfiguration.setPort(Integer.parseInt(mPort.getText()));
					} catch (NumberFormatException ex) {
						Tools.logException(MainFrame.class, ex, "Invalid port: " + mPort.getText());
					}
					try {
						mServerConfiguration.setHttpPort(Integer.parseInt(mHTTPPort.getText()));
					} catch (NumberFormatException ex) {
						Tools.logException(MainFrame.class, ex, "Invalid http port: " + mHTTPPort.getText());
					}
					mServerConfiguration.setIPAddress(((NameValue) mIPAddress.getSelectedItem()).getValue());
					mServerConfiguration.setShuffleItems(mShuffleItems.isSelected());
					mServerConfiguration.setGenerateThumbnails(mGenerateThumbnails.isSelected());
					mServerConfiguration.setRecordingsPath(mRecordingsPath.getText());
					mServerConfiguration.setMediaAccessKey(Tools.encrypt(mMediaAccessKey.getText().trim()));
					mServerConfiguration.setPassword(Tools.encrypt(mPasswordField.getText()));
					mServerConfiguration.setDebug(mDebug.isSelected());
					mServerConfiguration.setDisableTimeout(mDisableTimeout.isSelected());
					mServerConfiguration.setMenu(mMenu.isSelected());
					mServerConfiguration.setPublicIPAddress(mPublicIPAddressField.getText());
					mServerConfiguration.setPin(Tools.encrypt(mPINField.getText()));

					Galleon.updateServerConfiguration(mServerConfiguration);
				} catch (Exception ex) {
					Tools.logException(MainFrame.class, ex, "Could not configure server");
					
					JOptionPane.showMessageDialog(this, "Could not connect to server. "+Galleon.getErrorMessageSuffix(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				// JOptionPane.showMessageDialog(this,
				// "You need to restart Galleon for any changes in the server
				// properties to take effect.",
				// "Warning", JOptionPane.WARNING_MESSAGE);

				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else if ("help".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/93/52/");
				} catch (Exception ex) {
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			} else if ("pick".equals(e.getActionCommand())) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.addChoosableFileFilter(new FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						return false;
					}

					public String getDescription() {
						return "Directories";
					}
				});

				int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					mRecordingsPath.setText(file.getAbsolutePath());
				}
				this.toFront();
				return;
			} else if ("network".equals(e.getActionCommand())) {
				new NetworkDialog(this).setVisible(true);
				this.toFront();
				return;
			}

			this.setVisible(false);
		}

		private MainFrame mMainFrame;
		
		private JTextField mNameField;

		private JTextField mVersionField;

		private JComboBox mReloadCombo;

		private JComboBox mSkinCombo;

		private JCheckBox mGenerateThumbnails;

		private JCheckBox mShuffleItems;

		private JCheckBox mDebug;
		
		private JCheckBox mDisableTimeout;
		
		private JCheckBox mMenu;

		private JTextField mPort;
		
		private JTextField mHTTPPort;

		private JComboBox mIPAddress;

		private JTextField mRecordingsPath;

		private JTextField mMediaAccessKey;
		
		private JTextField mPublicIPAddressField;
		
		private JPasswordField mPINField;
		
		private JPasswordField mPasswordField;

		private ServerConfiguration mServerConfiguration;
	}

	public class MusicPlayerDialog extends JDialog implements ActionListener {

		class MusicWrapper extends NameValue {
			public MusicWrapper(String name, String value) {
				super(name, value);
			}

			public String toString() {
				return getName();
			}
		}

		private MusicPlayerDialog(MainFrame frame, ServerConfiguration serverConfiguration) {
			super(frame, "Music Player", true);
			mMainFrame = frame;
			mServerConfiguration = serverConfiguration;

			MusicPlayerConfiguration musicPlayerConfiguration = mServerConfiguration.getMusicPlayerConfiguration();

			mPlayersField = new JComboBox();
			mPlayersField.setToolTipText("Select a music player");
			mPlayersField.addItem(new MusicWrapper(StringUtils.capitalize(MusicPlayerConfiguration.CLASSIC),
					MusicPlayerConfiguration.CLASSIC));
			mPlayersField.addItem(new MusicWrapper(StringUtils.capitalize(MusicPlayerConfiguration.WINAMP),
					MusicPlayerConfiguration.WINAMP));
			defaultCombo(mPlayersField, musicPlayerConfiguration.getPlayer());

			List skins = Galleon.getWinampSkins();

			mSkinsField = new JComboBox();
			mSkinsField.setPreferredSize(new Dimension(400, 20));
			mSkinsField.setToolTipText("Select a Winamp classic skin for music player");
			Iterator iterator = skins.iterator();
			while (iterator.hasNext()) {
				File file = (File) iterator.next();
				try {
					String name = Tools.extractName(file.getCanonicalPath());
					mSkinsField.addItem(new MusicWrapper(name, file.getCanonicalPath()));
				} catch (Exception ex) {
				}
			}
			defaultCombo(mSkinsField, musicPlayerConfiguration.getSkin());
			mUseAmazonField = new JCheckBox("Use Amazon.com ");
			mUseAmazonField.setToolTipText("Check to specify that Amazon.com should be used for album art");
			mUseAmazonField.setSelected(musicPlayerConfiguration.isUseAmazon());
			mUseAmazonField.setForeground(Color.blue);
			mUseAmazonField.setCursor(new Cursor(Cursor.HAND_CURSOR));
			mUseAmazonField.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					try {
						BrowserLauncher
								.openURL("http://www.amazon.com/exec/obidos/tg/browse/-/5174/ref%3Dtab%5Fm%5Fm%5F9/104-1230741-3818310");
					} catch (Exception ex) {
					}
				}
			});

			mUseFileField = new JCheckBox("Use Folder.jpg          ");
			mUseFileField.setToolTipText("Check to specify that the Folder.jpg file should be used for album art");
			mUseFileField.setSelected(musicPlayerConfiguration.isUseFile());

			mShowImagesField = new JCheckBox("Show web images        ");
			mShowImagesField.setToolTipText("Check to specify that web images of the artist should be shown");
			mShowImagesField.setSelected(musicPlayerConfiguration.isShowImages());
			mShowImagesField.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (mShowImagesField.isSelected()) {
						JOptionPane
								.showMessageDialog(
										MainFrame.this,
										"All search engine queries for images are configured to filter out adult content by default.\nHowever, it is still possible that undesirable content might be returned in these search results.",
										"Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
			});

			mRandomPlayFoldersField = new JCheckBox("Random play folders          ");
			mRandomPlayFoldersField.setToolTipText("Check to specify that music in folders should be played randomly");
			mRandomPlayFoldersField.setSelected(musicPlayerConfiguration.isRandomPlayFolders());

			mScreensaverField = new JCheckBox("Screensaver          ");
			mScreensaverField.setToolTipText("Check to enable the screensaver");
			mScreensaverField.setSelected(musicPlayerConfiguration.isScreensaver());

			FormLayout layout = new FormLayout(
					"right:pref, 3dlu, 100dlu:g, right:pref:grow",
					"pref, 3dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref, 9dlu, pref");

			PanelBuilder builder = new PanelBuilder(layout);
			// DefaultFormBuilder builder = new DefaultFormBuilder(new
			// FormDebugPanel(), layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.addSeparator("General", cc.xyw(1, 1, 4));
			builder.addLabel("Player", cc.xy(1, 5));
			builder.add(mPlayersField, cc.xyw(3, 5, 1));
			JLabel label = new JLabel("Winamp Classic Skin");
			label.setToolTipText("Open Winamp site in web browser");
			label.setForeground(Color.blue);
			label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					try {
						BrowserLauncher.openURL("http://www.winamp.com/skins");
					} catch (Exception ex) {
					}
				}
			});
			builder.add(label, cc.xy(1, 7));
			builder.add(mSkinsField, cc.xyw(3, 7, 1));
			builder.add(mRandomPlayFoldersField, cc.xyw(1, 9, 3));
			builder.add(mScreensaverField, cc.xyw(1, 11, 3));
			builder.addSeparator("Album Art", cc.xyw(1, 13, 4));
			builder.add(mUseAmazonField, cc.xyw(1, 15, 3));
			builder.add(mUseFileField, cc.xyw(1, 17, 3));
			builder.add(mShowImagesField, cc.xyw(1, 19, 3));

			getContentPane().add(builder.getPanel(), "Center");

			JButton[] array = new JButton[3];
			array[0] = new JButton("OK");
			array[0].setActionCommand("ok");
			array[0].addActionListener(this);
			array[1] = new JButton("Cancel");
			array[1].setActionCommand("cancel");
			array[1].addActionListener(this);
			array[2] = new JButton("Help");
			array[2].setActionCommand("help");
			array[2].addActionListener(this);
			JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttons, "South");
			pack();
			setLocationRelativeTo(frame);
		}

		public void defaultCombo(JComboBox combo, String value) {
			for (int i = 0; i < combo.getItemCount(); i++) {
				if (((NameValue) combo.getItemAt(i)).getValue().equals(value)) {
					combo.setSelectedIndex(i);
					return;
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			if ("ok".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					MusicPlayerConfiguration musicPlayerConfiguration = mServerConfiguration
							.getMusicPlayerConfiguration();
					musicPlayerConfiguration.setPlayer(((NameValue) mPlayersField.getSelectedItem()).getValue());
					musicPlayerConfiguration.setSkin(((NameValue) mSkinsField.getSelectedItem()).getValue());
					musicPlayerConfiguration.setUseAmazon(mUseAmazonField.isSelected());
					musicPlayerConfiguration.setUseFile(mUseFileField.isSelected());
					musicPlayerConfiguration.setShowImages(mShowImagesField.isSelected());
					musicPlayerConfiguration.setRandomPlayFolders(mRandomPlayFoldersField.isSelected());
					musicPlayerConfiguration.setScreensaver(mScreensaverField.isSelected());

					Galleon.updateMusicPlayerConfiguration(musicPlayerConfiguration);
				} catch (Exception ex) {
					Tools.logException(MainFrame.class, ex, "Could not configure server");
					
					JOptionPane.showMessageDialog(this, "Could not connect to server. "+Galleon.getErrorMessageSuffix(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else if ("help".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/99/37/");
				} catch (Exception ex) {
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			this.setVisible(false);
		}

		private JComboBox mPlayersField;

		private JComboBox mSkinsField;

		private JCheckBox mUseFileField;

		private JCheckBox mUseAmazonField;

		private JCheckBox mShowImagesField;

		private JCheckBox mRandomPlayFoldersField;

		private JCheckBox mScreensaverField;

		private ServerConfiguration mServerConfiguration;
		
		private MainFrame mMainFrame;
	}

	public void displayHelp(Frame frame, URL url) {
		if (mHelpDialog != null) {
			mHelpDialog.setVisible(false);
			mHelpDialog.dispose();
		}

		mHelpDialog = new HelpDialog(frame, url);
		mHelpDialog.setVisible(true);
	}

	public class NetworkDialog extends JDialog implements ActionListener {

		private NetworkDialog(final ServerDialog serverDialog) {
			super(serverDialog, "Network Wizard", true);
			mServerDialog = serverDialog;

			// enable debug logging

			mProgressBar = new JProgressBar(0, 30);
			mProgressBar.setValue(0);
			// mProgressBar.setStringPainted(true);
			mResultsField = new JTextArea(3, 60);
			mResultsField.setEditable(false);

			getContentPane().setLayout(new BorderLayout());

			FormLayout layout = new FormLayout("right:pref, 3dlu, pref, right:pref:grow", "pref, " + // progress
					"3dlu, " + "pref" // results
			);

			PanelBuilder builder = new PanelBuilder(layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.add(mProgressBar, cc.xyw(1, 1, 4));
			builder.add(mResultsField, cc.xyw(1, 3, 4));

			getContentPane().add(builder.getPanel(), "Center");

			JButton[] array = new JButton[2];
			array[0] = new JButton("Close");
			array[0].setActionCommand("cancel");
			array[0].addActionListener(this);
			array[1] = new JButton("Help");
			array[1].setActionCommand("help");
			array[1].addActionListener(this);
			JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttons, "South");
			pack();
			setLocationRelativeTo(serverDialog);

			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				int counter = 0;

				public void run() {
					if (counter == 0) {
						mResultsField.setText("Searching...");
						mTiVoListener = new TiVoListener(((NameValue) serverDialog.mIPAddress.getSelectedItem())
								.getValue());
					}
					mProgressBar.setValue(counter);
					if (counter++ > 30) {
						Toolkit.getDefaultToolkit().beep();
						mProgressBar.setValue(mProgressBar.getMinimum());
						mProgressBar.setString("");
						mTiVoListener.stop();
						if (!mTiVoListener.found())
							mResultsField.setText("No TiVos found on this network interface");
						this.cancel();
					}
				}
			}, 0, 1000);
		}

		public void actionPerformed(ActionEvent e) {
			if ("cancel".equals(e.getActionCommand())) {
				mTiVoListener.stop();
				mTimer.cancel();
			} else if ("help".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/100/37/");
				} catch (Exception ex) {
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}
			this.setVisible(false);
		}

		private final class TiVoListener implements ServiceListener, ServiceTypeListener {
			// TiVo with 7.1 software supports rendevouz and has a web server
			private final static String HTTP_SERVICE = "_http._tcp.local.";

			private final static String TIVO_PLATFORM = "platform";

			private final static String TIVO_PLATFORM_PREFIX = "tcd"; // platform=tcd/Series2

			private final static String TIVO_TSN = "TSN";

			private final static String TIVO_SW_VERSION = "swversion";

			private final static String TIVO_PATH = "path";

			public TiVoListener(String address) {
				try {
					InetAddress inetAddress = null;
					if (address.equals("Default"))
						inetAddress = InetAddress.getLocalHost();
					else
						inetAddress = InetAddress.getByName(address);

					mJmDNS = new JmDNS(inetAddress);
					mJmDNS.addServiceListener(HTTP_SERVICE, this);
					log.debug("Interface: " + mJmDNS.getInterface());
				} catch (IOException ex) {
					Tools.logException(TiVoListener.class, ex);
				}
			}

			public void addService(JmDNS jmdns, String type, String name) {
				if (name.endsWith("." + type)) {
					name = name.substring(0, name.length() - (type.length() + 1));
				}
				log.debug("addService: " + name);

				ServiceInfo service = jmdns.getServiceInfo(type, name);
				if (service == null) {
					log.error("Service not found: " + type + " (" + name + ")");
				} else {
					if (!name.endsWith(".")) {
						name = name + "." + type;
					}
					jmdns.requestServiceInfo(type, name);
				}
			}

			public void removeService(JmDNS jmdns, String type, String name) {
				if (name.endsWith("." + type)) {
					name = name.substring(0, name.length() - (type.length() + 1));
				}
				log.debug("removeService: " + name);
			}

			public void addServiceType(JmDNS jmdns, String type) {
				log.debug("addServiceType: " + type);
			}

			public void resolveService(JmDNS jmdns, String type, String name, ServiceInfo info) {
				log.debug("resolveService: " + type + " (" + name + ")");

				if (type.equals(HTTP_SERVICE)) {
					if (info == null) {
						log.error("Service not found: " + type + "(" + name + ")");
					} else {
						mFound = info.getPropertyString(TIVO_PLATFORM)!=null && info.getPropertyString(TIVO_TSN)!=null;  
						/*
						for (Enumeration names = info.getPropertyNames(); names.hasMoreElements();) {
							String prop = (String) names.nextElement();
							if (prop.equals(TIVO_PLATFORM)) {
								if (info.getPropertyString(prop).startsWith(TIVO_PLATFORM_PREFIX)) {
									mFound = true;
								}
							}
						}
						*/

						if (mFound) {
							mResultsField.setText((mResultsField.getText().equals("Searching...") ? "" : mResultsField
									.getText()
									+ ", ")
									+ name.substring(0, name.length() - (type.length() + 1)));
						}
					}
				}
			}

			public void stop() {
				if (mJmDNS!=null)
					mJmDNS.removeServiceListener(this);
			}

			public boolean found() {
				return mFound;
			}

			private JmDNS mJmDNS;
		}

		private JProgressBar mProgressBar;

		private JTextArea mResultsField;

		private ServerConfiguration mServerConfiguration;

		private Timer mTimer;

		private TiVoListener mTiVoListener;

		private boolean mFound;
		
		private ServerDialog mServerDialog;
	}
	
	public class GoBackDialog extends JDialog implements ActionListener {

		private GoBackDialog(MainFrame frame, ServerConfiguration serverConfiguration) {
			super(frame, "GoBack", true);
			mMainFrame = frame;
			mServerConfiguration = serverConfiguration;
			
			final GoBackConfiguration goBackConfiguration = mServerConfiguration.getGoBackConfiguration();
			
			mEnabled = new JCheckBox("Enabled");
			mEnabled.setSelected(goBackConfiguration.isEnabled());
			mPublishTiVoRecordings = new JCheckBox("Publish ToGo Recordings");
			mPublishTiVoRecordings.setSelected(goBackConfiguration.isPublishTiVoRecordings());
			mConvertVideo = new JCheckBox("Convert Video");
			mConvertVideo.setSelected(goBackConfiguration.isConvertVideo());
			mConvertVideo.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (mConvertVideo.isSelected()) {
						JOptionPane
								.showMessageDialog(
										MainFrame.this,
										"Converting video is a CPU intensive operation which can last for an extended period of time.",
										"Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
			});
			mConversionTool = new JTextField(50);
			mConversionTool.setText(goBackConfiguration.getConversionTool());

			getContentPane().setLayout(new BorderLayout());

			FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref:grow", 
					"pref, " + // general
					"6dlu, " + "pref, " + // enabled
					"3dlu, " + "pref, " + // publish
					"3dlu, " + "pref, " + // convert
					"3dlu, " + "pref, " + // tool
					"3dlu, " + "pref, " + // directories
					"3dlu, " + "pref " 
			);

			PanelBuilder builder = new PanelBuilder(layout);
			//DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.addSeparator("General", cc.xyw(1, 1, 5));
			builder.add(mEnabled, cc.xyw(3, 3, 3));
			builder.add(mPublishTiVoRecordings, cc.xyw(3, 5, 3));
			builder.add(mConvertVideo, cc.xyw(3, 7, 3));
			builder.addLabel("Conversion Tool", cc.xy(1, 9));
			builder.add(mConversionTool, cc.xy(3, 9));
			
			builder.addSeparator("Directories", cc.xyw(1, 11, 5));

	        mColumnValues = new ArrayList();
	        int counter = 0;
	        for (Iterator i = goBackConfiguration.getPaths().iterator(); i.hasNext(); /* Nothing */) {
	            NameValue value = (NameValue) i.next();
	            ArrayList values = new ArrayList();
	            values.add(0, value.getName());
	            values.add(1, value.getValue());
	            mColumnValues.add(counter++, values);
	        }

	        JPanel panel = builder.getPanel();
	        mFileOptionsTable = new FileOptionsTable(true, panel, mColumnValues);
	        ArrayList columnNames = new ArrayList();
	        columnNames.add(0, "Name");
	        columnNames.add(1, "Path");
	        builder.add(mFileOptionsTable, cc.xyw(3, 13, 3));			

			getContentPane().add(panel, "Center");

			JButton[] array = new JButton[3];
			array[0] = new JButton("OK");
			array[0].setActionCommand("ok");
			array[0].addActionListener(this);
			array[1] = new JButton("Cancel");
			array[1].setActionCommand("cancel");
			array[1].addActionListener(this);
			array[2] = new JButton("Help");
			array[2].setActionCommand("help");
			array[2].addActionListener(this);
			JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttons, "South");
			pack();
			setLocationRelativeTo(frame);
		}

		public void actionPerformed(ActionEvent e) {
			if ("ok".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				GoBackConfiguration goBackConfiguration = new GoBackConfiguration();
				try {
					goBackConfiguration.setEnabled(mEnabled.isSelected());
					goBackConfiguration.setPublishTiVoRecordings(mPublishTiVoRecordings.isSelected());
					goBackConfiguration.setConvertVideo(mConvertVideo.isSelected());
					goBackConfiguration.setConversionTool(mConversionTool.getText());
					
					ArrayList newItems = new ArrayList();
			        Iterator iterator = mColumnValues.iterator();
			        while (iterator.hasNext()) {
			            ArrayList rows = (ArrayList) iterator.next();
			            log.debug("Path=" + rows.get(0));
			            newItems.add(new NameValue((String) rows.get(0), (String) rows.get(1)));
			        }
			        goBackConfiguration.setPaths(newItems);
					
					Galleon.updateGoBackConfiguration(goBackConfiguration);
				} catch (Exception ex) {
					Tools.logException(MainFrame.class, ex, "Could not configure goback");
					
					JOptionPane.showMessageDialog(
							this,
							Tools.getCause(ex),
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				finally
				{
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));	
				}
			} else if ("help".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					BrowserLauncher.openURL("http://galleon.tv/content/view/89/49/");
				} catch (Exception ex) {
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}
			this.setVisible(false);
		}
		
		private JCheckBox mEnabled;

		private JCheckBox mPublishTiVoRecordings;
		
		private JCheckBox mConvertVideo;
		
		private JTextField mConversionTool;
		
		private FileOptionsTable mFileOptionsTable;

	    private ArrayList mColumnValues;		

		private ServerConfiguration mServerConfiguration;
		
		private MainFrame mMainFrame;
	}	
	
	public class DataDialog extends JDialog implements ActionListener {

		private DataDialog(MainFrame frame, ServerConfiguration serverConfiguration) {
			super(frame, "Galleon.tv", true);
			mMainFrame = frame;
			mServerConfiguration = serverConfiguration;
			
			final DataConfiguration dataConfiguration = mServerConfiguration.getDataConfiguration();
			
			mUsernameField = new JTextField(20);
			mUsernameField.setText(Tools.decrypt(dataConfiguration.getUsername()));
			mPasswordField = new JPasswordField(20);
			mPasswordField.setText(Tools.decrypt(dataConfiguration.getPassword()));

			getContentPane().setLayout(new BorderLayout());

			FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref:grow", 
					"pref, " + // settings
					"6dlu, " + "pref, " + // username
					"3dlu, " + "pref " // password
			);

			PanelBuilder builder = new PanelBuilder(layout);
			//DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
			builder.setDefaultDialogBorder();

			CellConstraints cc = new CellConstraints();

			builder.addSeparator("User", cc.xyw(1, 1, 5));
			builder.addLabel("Username", cc.xy(1, 3));
			builder.add(mUsernameField, cc.xy(3, 3));
			builder.addLabel("Password", cc.xy(1, 5));
			builder.add(mPasswordField, cc.xy(3, 5));

			getContentPane().add(builder.getPanel(), "Center");

			JButton[] array = new JButton[3];
			array[0] = new JButton("OK");
			array[0].setActionCommand("ok");
			array[0].addActionListener(this);
			array[1] = new JButton("Cancel");
			array[1].setActionCommand("cancel");
			array[1].addActionListener(this);
			array[2] = new JButton("Help");
			array[2].setActionCommand("help");
			array[2].addActionListener(this);
			JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttons, "South");
			pack();
			setLocationRelativeTo(frame);
		}

		public void actionPerformed(ActionEvent e) {
			if ("ok".equals(e.getActionCommand())) {
				String username = mUsernameField.getText().trim();
				String password = mPasswordField.getText().trim();
				
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				DataConfiguration dataConfiguration = new DataConfiguration();
				try {
					dataConfiguration.setUsername(Tools.encrypt(username));
					dataConfiguration.setPassword(Tools.encrypt(password));
					Galleon.updateDataConfiguration(dataConfiguration);
				} catch (Exception ex) {
					Tools.logException(MainFrame.class, ex, "Could not configure data");
					
					JOptionPane.showMessageDialog(
							this,
							Tools.getCause(ex),
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				finally
				{
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));	
				}
			} else if ("help".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					BrowserLauncher.openURL("http://galleon.tv/component/option,com_comprofiler/task,registers/");
				} catch (Exception ex) {
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}
			this.setVisible(false);
		}

		private JTextField mUsernameField;

		private JPasswordField mPasswordField;
		
		private ServerConfiguration mServerConfiguration;
		
		private MainFrame mMainFrame;
	}	

	private AppTree mAppTree;

	private OptionsPanelManager mOptionsPanelManager;

	private HelpDialog mHelpDialog;
}