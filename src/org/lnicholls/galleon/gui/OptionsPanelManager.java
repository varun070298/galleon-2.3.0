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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.app.*;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.stanford.ejalbert.BrowserLauncher;

public final class OptionsPanelManager extends InternalFrame implements ActionListener {
	private static Logger log = Logger.getLogger(OptionsPanelManager.class.getName());

	public OptionsPanelManager(MainFrame mainFrame) {
		super("Options");
		mMainFrame = mainFrame;
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());

		JPanel defaultPanel = new JPanel();
		defaultPanel.setLayout(new GridLayout(0, 1));
		defaultPanel.add(new JLabel(
				"Use the File/Properies menu to configure Galleon. Use the File/New App menu to add new apps.",
				SwingConstants.CENTER));
		mScrollPane = MainFrame.createScrollPane(defaultPanel);

		content.add(mScrollPane, "Center");

		JButton[] array = new JButton[3];
		mApplyButton = new JButton("Apply");
		array[0] = mApplyButton;
		array[0].setActionCommand("apply");
		array[0].addActionListener(this);
		mDeleteButton = new JButton("Delete");
		array[1] = mDeleteButton;
		array[1].setActionCommand("delete");
		array[1].addActionListener(this);
		mHelpButton = new JButton("Help");
		array[2] = mHelpButton;
		array[2].setActionCommand("help");
		array[2].addActionListener(this);
		JPanel buttons = ButtonBarFactory.buildRightAlignedBar(array);
		mApplyButton.setVisible(false);
		mDeleteButton.setVisible(false);
		mHelpButton.setVisible(false);

		buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		content.add(buttons, "South");

		setContent(content);
	}

	static class EmptyOptionsPanel extends JPanel implements ConfigurationPanel {
		public EmptyOptionsPanel() {
		}

		public boolean valid() {
			return true;
		}

		public void load() {
		};

		public void save() {
		};
	}

	public void setSelectedOptionsPanel(AppNode appNode) {
		mAppNode = appNode;

		ConfigurationPanel options = null;
		try {
			options = appNode.getConfigurationPanel();
		} catch (Exception ex) {
			Tools.logException(OptionsPanelManager.class, ex, "Could not create app options panel: "
					+ mAppNode.getAppContext().getDescriptor());
		}
		if (options == null) {
			log.info("No options panel found for: " + mAppNode.getAppContext().getDescriptor());

			options = new EmptyOptionsPanel();
			mApplyButton.setVisible(false);
			mDeleteButton.setVisible(false);
			mHelpButton.setVisible(false);
		} else {
			mApplyButton.setVisible(true);
			mDeleteButton.setVisible(true);
			mHelpButton.setVisible(true);
		}
		setFrameIcon(appNode.getIcon());
		setTitle(appNode.getAppContext().getDescriptor().getTitle() + " App ("
				+ appNode.getAppContext().getDescriptor().getVersion() + ")");
		mScrollPane.setViewportView((Component) options);
	}

	public void actionPerformed(ActionEvent e) {
		if (mAppNode != null) {
			if ("apply".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (mAppNode.getConfigurationPanel().valid())
						mAppNode.getConfigurationPanel().save();
					else {
						this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						return;
					}
				} catch (Exception ex) {
					Tools.logException(OptionsPanelManager.class, ex, "Could not save app : "
							+ mAppNode.getAppContext().getConfiguration());
				}
				Galleon.updateApp(mAppNode.getAppContext());
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else if ("help".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String documentation = mAppNode.getAppContext().getDescriptor().getDocumentation();
				if (documentation!=null)
				{
					if (documentation.startsWith("http://"))
					{
						try {
							BrowserLauncher.openURL(documentation);
						} catch (Exception ex) {
							Tools.logException(OptionsPanelManager.class, ex, "Could not help app : "
									+ documentation);
							JOptionPane.showMessageDialog(mMainFrame, "No help available for this app.", "Help",
									JOptionPane.INFORMATION_MESSAGE);
						}
					}
					else
					{
						try {
							String path = mAppNode.getAppContext().getDescriptor().getClassName();
							path = path.substring(0, path.lastIndexOf("."));
							path = path.replaceAll("\\.", "/");
							URL url = mAppNode.getConfigurationPanel().getClass().getClassLoader().getResource(
									path + "/" + documentation);
							mMainFrame.displayHelp(mMainFrame, url);
						} catch (Exception ex) {
							Tools.logException(OptionsPanelManager.class, ex, "Could not help app : "
									+ documentation);
							JOptionPane.showMessageDialog(mMainFrame, "No help available for this app.", "Help",
									JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else if ("delete".equals(e.getActionCommand())) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				boolean deleted = false;
				try {
					Object[] options = { "Yes", "No" };
					int n = JOptionPane.showOptionDialog(mMainFrame, "Are you sure you want to delete this app?",
							"Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, // don't
							// use a
							// custom
							// Icon
							options, // the titles of buttons
							options[1]); // default button title
					if (n == JOptionPane.YES_OPTION) {
						// TODO ??
						mMainFrame.removeApp(mAppNode.getAppContext());
						deleted = true;
					}
				} catch (Exception ex) {
					Tools.logException(OptionsPanelManager.class, ex, "Could not delete app : "
							+ mAppNode.getAppContext().getConfiguration());
				}
				if (deleted && false) {
					mAppNode = null;
					mScrollPane.setViewportView((Component) new EmptyOptionsPanel());
					setFrameIcon(null);
					setTitle("Options");
				}
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	private MainFrame mMainFrame;

	private JScrollPane mScrollPane;

	private AppNode mAppNode;

	private JButton mApplyButton;

	private JButton mDeleteButton;

	private JButton mHelpButton;
}