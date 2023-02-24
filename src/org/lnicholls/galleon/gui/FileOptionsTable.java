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

import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.app.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JTable used in GUI
 */
public class FileOptionsTable extends JPanel implements ActionListener {
    private static Logger log = Logger.getLogger(OptionsTable.class.getName());

    static class FileTransferHandler extends TransferHandler {

        public FileTransferHandler(boolean directories, OptionsTable optionsTable) {
            mDirectories = directories;
            mOptionsTable = optionsTable;
        }

        public boolean importData(JComponent c, Transferable t) {
            log.debug("importData:");
            if (!canImport(c, t.getTransferDataFlavors())) {
                return false;
            }
            try {
                if (hasFileFlavor(t.getTransferDataFlavors())) {
                    String str = null;
                    java.util.List files = (java.util.List) t.getTransferData(mFileFlavor);
                    for (int i = 0; i < files.size(); i++) {
                        File file = (File) files.get(i);
                        if (mDirectories) {
                            if (!file.isDirectory())
                                return false;
                        } else {
                            if (!file.isFile())
                                return false;
                        }
                        int rows = mOptionsTable.getModel().getRowCount();
                        String name = file.getName();
                        if (name.lastIndexOf(".") != -1)
                            name = name.substring(0, name.lastIndexOf("."));
                        mOptionsTable.getModel().setValueAt(name, rows, 0);
                        mOptionsTable.getModel().setValueAt(file.getAbsolutePath(), rows, 1);
                    }
                    return true;
                }
            } catch (UnsupportedFlavorException ufe) {
                log.error("importData: unsupported data flavor");
            } catch (IOException ieo) {
                log.error("importData: I/O exception");
            }
            return false;
        }

        protected Transferable createTransferable(JComponent c) {
            return null;
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }

        protected void exportDone(JComponent c, Transferable data, int action) {
        }

        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            log.debug("canImport:");
            return hasFileFlavor(flavors);
        }

        private boolean hasFileFlavor(DataFlavor[] flavors) {
            log.debug("hasFileFlavor:");
            for (int i = 0; i < flavors.length; i++) {
                if (mFileFlavor.equals(flavors[i])) {
                    return true;
                }
            }
            log.debug("hasFileFlavor: false");
            return false;
        }

        private boolean mDirectories;

        private DataFlavor mFileFlavor = DataFlavor.javaFileListFlavor;

        private OptionsTable mOptionsTable;
    }

    public FileOptionsTable(boolean directories, JPanel optionsPanel, ArrayList paths) {
        super();
        setLayout(new GridLayout(0, 1));

        mDirectories = directories;

        ArrayList columnNames = new ArrayList();
        columnNames.add(0, "Name");
        columnNames.add(1, "Path");

        mNameField = new JTextField("");
        mPathField = new JTextField("");

        ArrayList fields = new ArrayList();
        fields.add(mNameField);
        fields.add(mPathField);

        mOptionsTable = new OptionsTable(optionsPanel, columnNames, paths, fields);

        JButton button = new JButton("...");
        button.setActionCommand("pick");
        button.addActionListener(this);

        FormLayout layout = new FormLayout("right:pref, 3dlu, 100dlu:g, 3dlu, left:pref:grow",
                "pref, 3dlu, pref, 3dlu, pref");

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);

        CellConstraints cc = new CellConstraints();

        builder.addLabel("Name", cc.xy(1, 1));
        builder.add(mNameField, cc.xy(3, 1));
        builder.addLabel("Path", cc.xy(1, 3));
        builder.add(mPathField, cc.xy(3, 3));
        builder.add(button, cc.xy(5, 3));

        builder.add(mOptionsTable, cc.xyw(1, 5, 5));

        JPanel panel = builder.getPanel();

        add(panel);

        this.setTransferHandler(new FileTransferHandler(mDirectories, mOptionsTable));
    }

    public void actionPerformed(ActionEvent e) {
        if ("pick".equals(e.getActionCommand())) {
            final JFileChooser fc = new JFileChooser();
            if (mDirectories)
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            else
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            fc.addChoosableFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }

                    if (!mDirectories) {
                        // TODO Remove this hardcoded logic
                        if (f.getName().toLowerCase().endsWith(".m3u") || f.getName().toLowerCase().endsWith(".pls"))
                            return true;
                    }

                    return false;
                }

                public String getDescription() {
                    if (mDirectories)
                        return "Directories";
                    else
                        return "Files";
                }
            });

            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                mPathField.setText(file.getAbsolutePath());
                mOptionsTable.checkButtonStates();
            }
        }
    }

    private JTextComponent mNameField;

    private JTextComponent mPathField;

    private OptionsTable mOptionsTable;

    private boolean mDirectories;
}