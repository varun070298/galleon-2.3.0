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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * @author Owner
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class ToGoDialog extends JDialog implements ActionListener {

    public ToGoDialog(MainFrame frame, ServerConfiguration serverConfiguration) {
        super(frame, "ToGo", true);
        mMainFrame = frame;
        mServerConfiguration = serverConfiguration;

        mTabbedPane = new JTabbedPane(SwingConstants.TOP);
        mTabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (mTabbedPane.getSelectedIndex() == 0) {
                    mTiVoPanel.activate();
                }
                else
                if (mTabbedPane.getSelectedIndex() == 1) {
                    mDownloadedPanel.activate();
                }                
                else
                if (mTabbedPane.getSelectedIndex() == 2) {
                    mRecordedPanel.activate();
                }                
            }
        });
        mTiVoPanel = new TiVoPanel();
        mDownloadedPanel = new DownloadedPanel();
        mRecordedPanel = new RecordedPanel();
        mRulesPanel = new RulesPanel(mRecordedPanel);
        mTabbedPane.add(mTiVoPanel, "TiVo's");
        mTabbedPane.add(mDownloadedPanel, "Downloaded");
        mTabbedPane.add(mRecordedPanel, "Recorded");
        mTabbedPane.add(mRulesPanel, "Rules");
        getContentPane().add(mTabbedPane, BorderLayout.CENTER);
        mTabbedPane.setSelectedIndex(0);

        JButton[] array = new JButton[2];
        array[0] = new JButton("Close");
        array[0].setActionCommand("close");
        array[0].addActionListener(this);
        array[1] = new JButton("Help");
        array[1].setActionCommand("help");
        array[1].addActionListener(this);
        JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(buttons, "South");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }

            public void windowActivated(WindowEvent e) {
                //mTabbedPane.setSelectedIndex(0);
            }
        });

        setSize(800, 450);
        setLocationRelativeTo(frame);
    }

    public void actionPerformed(ActionEvent e) {
        if ("help".equals(e.getActionCommand())) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
				BrowserLauncher.openURL("http://galleon.tv/content/view/14/29/");
			} catch (Exception ex) {
			}
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }

        this.setVisible(false);
    }

    private JTabbedPane mTabbedPane;

    private ServerConfiguration mServerConfiguration;

    private TiVoPanel mTiVoPanel;

    private DownloadedPanel mDownloadedPanel;

    private RecordedPanel mRecordedPanel;

    private RulesPanel mRulesPanel;

    private HelpDialog mHelpDialog;
    
    private MainFrame mMainFrame;
}