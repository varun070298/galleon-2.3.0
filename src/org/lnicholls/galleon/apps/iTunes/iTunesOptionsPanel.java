package org.lnicholls.galleon.apps.iTunes;

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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class iTunesOptionsPanel extends AppConfigurationPanel implements ActionListener {
    private static Logger log = Logger.getLogger(iTunesOptionsPanel.class.getName());

    public iTunesOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        iTunesConfiguration iTunesConfiguration = (iTunesConfiguration) appConfiguration;

        mTitleField = new JTextField(iTunesConfiguration.getName());
        mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(iTunesConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");

        String playlistPath = iTunesConfiguration.getPlaylistPath();
        if (playlistPath == null) {
            if (SystemUtils.IS_OS_MAC_OSX)
                playlistPath = "/Users/" + System.getProperty("user.name") + "/Music/iTunes/iTunes Music Library.xml";
            else
                playlistPath = "C:\\Documents and Settings\\" + System.getProperty("user.name")
                        + "\\My Documents\\My Music\\iTunes\\iTunes Music Library.xml";
        }
        mPlaylistPathField = new JTextField(playlistPath);

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, 3dlu, pref, right:pref:grow", "pref, " + // general
                "9dlu, " + "pref, " + // title
                "3dlu, " + "pref, " + // share
                "3dlu, " + "pref, " // username
        );

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 4));
        builder.addLabel("Title", cc.xy(1, 3));
        builder.add(mTitleField, cc.xyw(3, 3, 1));
        builder.add(mSharedField, cc.xyw(3, 5, 1));

        builder.addLabel("Playlist Path", cc.xy(1, 7));
        builder.add(mPlaylistPathField, cc.xyw(3, 7, 1));
        JButton button = new JButton("...");
        button.setActionCommand("pick");
        button.addActionListener(this);
        builder.add(button, cc.xyw(5, 7, 1));

        JPanel panel = builder.getPanel();
        //FormDebugUtils.dumpAll(panel);
        add(panel);
    }

    public void actionPerformed(ActionEvent e) {
        if ("pick".equals(e.getActionCommand())) {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.addChoosableFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    } else if (f.isFile() && f.getName().toLowerCase().endsWith("xml")) {
                        return true;
                    }

                    return false;
                }

                //The description of this filter
                public String getDescription() {
                    return "Playlist Library";
                }
            });

            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                mPlaylistPathField.setText(file.getAbsolutePath());
            }
        }
    }
    
    public boolean valid() {
        if (mTitleField.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Invalid title.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        File file = new File(mPlaylistPathField.getText());
        if (file.exists())
            return true;
        else
            JOptionPane.showMessageDialog(this, "Invalid playlist path", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public void load() {
    }

    public void save() {
        log.debug("save()");
        iTunesConfiguration iTunesConfiguration = (iTunesConfiguration) mAppConfiguration;
        iTunesConfiguration.setName(mTitleField.getText());
        iTunesConfiguration.setPlaylistPath(mPlaylistPathField.getText());
        iTunesConfiguration.setShared(mSharedField.isSelected());

        JOptionPane
                .showMessageDialog(
                        this,
                        "Depending on the size of your iTunes collection, it will take some time to import your collection.\nYou will be able to use the application immediately and the playlists will grow over time.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private JTextComponent mTitleField;

    private JTextComponent mPlaylistPathField;
    
    private JCheckBox mSharedField;
}