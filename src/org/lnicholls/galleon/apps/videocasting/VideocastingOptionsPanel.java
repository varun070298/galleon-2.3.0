package org.lnicholls.galleon.apps.videocasting;

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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;
import org.lnicholls.galleon.gui.FileOptionsTable;
import org.lnicholls.galleon.gui.OptionsTable;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.gui.Galleon;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class VideocastingOptionsPanel extends AppConfigurationPanel {
    private static Logger log = Logger.getLogger(VideocastingOptionsPanel.class.getName());

    public VideocastingOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        VideocastingConfiguration videocastingConfiguration = (VideocastingConfiguration) appConfiguration;

        mTitleField = new JTextField(videocastingConfiguration.getName());
        mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(videocastingConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
        mDownloadCombo = new JComboBox();
        mDownloadCombo.addItem(new ComboWrapper("All", "-1"));
        mDownloadCombo.addItem(new ComboWrapper("1", "1"));
        mDownloadCombo.addItem(new ComboWrapper("2", "2"));
        mDownloadCombo.addItem(new ComboWrapper("3", "3"));
        mDownloadCombo.addItem(new ComboWrapper("4", "4"));
        mDownloadCombo.addItem(new ComboWrapper("5", "5"));
        defaultCombo(mDownloadCombo, Integer.toString(videocastingConfiguration.getDownload()));
        mSubscriptionsNameField = new JTextField("");
        mSubscriptionsUrlField = new JTextField("");
        
        mDirectoriesNameField = new JTextField("");
        mDirectoriesUrlField = new JTextField("");

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + // general
                "9dlu, pref, " + // title
                "3dlu, pref, " + // share
                "9dlu, pref, " + // download
                "9dlu, pref, " + // directories
                "9dlu, pref, " + // name
                "9dlu, pref, " + // url
                "9dlu, pref, " + // table                
                "9dlu, pref, " + // directories
                "9dlu, pref, " + // name
                "9dlu, pref, " + // url
                "9dlu, pref");

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 4));
        builder.addLabel("Title", cc.xy(1, 3));
        builder.add(mTitleField, cc.xyw(3, 3, 1));
        builder.add(mSharedField, cc.xyw(3, 5, 1));
        builder.addLabel("Download", cc.xy(1, 7));
        builder.add(mDownloadCombo, cc.xyw(3, 7, 1));

        builder.addSeparator("Subscriptions", cc.xyw(1, 9, 4));
        
        builder.addLabel("Name", cc.xy(1, 11));
        builder.add(mSubscriptionsNameField, cc.xyw(3, 11, 1));
        builder.addLabel("URL", cc.xy(1, 13));
        builder.add(mSubscriptionsUrlField, cc.xyw(3, 13, 1));

        mSubscriptionsColumnValues = new ArrayList();
        int counter = 0;
        try
        {
	        if (Galleon.getVideocasts()!=null)
	        {
		        for (Iterator i = Galleon.getVideocasts().iterator(); i.hasNext(); /* Nothing */) {
		            NameValue value = (NameValue) i.next();
		            ArrayList values = new ArrayList();
		            values.add(0, value.getName());
		            values.add(1, value.getValue());
		            mSubscriptionsColumnValues.add(counter++, values);
		        }
	        }
        }
        catch (Exception ex)
        {
        	Tools.logException(VideocastingOptionsPanel.class, ex);
        }
        
        ArrayList columnNames = new ArrayList();
        columnNames.add(0, "Name");
        columnNames.add(1, "URL");
        ArrayList fields = new ArrayList();
        fields.add(mSubscriptionsNameField);
        fields.add(mSubscriptionsUrlField);
        mSubscriptionsOptionsTable = new OptionsTable(this, columnNames, mSubscriptionsColumnValues, fields);
        builder.add(mSubscriptionsOptionsTable, cc.xyw(1, 15, 4));        
        
        builder.addSeparator("Directories", cc.xyw(1, 17, 4));
        
        builder.addLabel("Name", cc.xy(1, 19));
        builder.add(mDirectoriesNameField, cc.xyw(3, 19, 1));
        builder.addLabel("URL", cc.xy(1, 21));
        builder.add(mDirectoriesUrlField, cc.xyw(3, 21, 1));

        mDirectoriesColumnValues = new ArrayList();
        counter = 0;
        for (Iterator i = videocastingConfiguration.getDirectorys().iterator(); i.hasNext(); /* Nothing */) {
            NameValue value = (NameValue) i.next();
            ArrayList values = new ArrayList();
            values.add(0, value.getName());
            values.add(1, value.getValue());
            mDirectoriesColumnValues.add(counter++, values);
        }
        
        columnNames = new ArrayList();
        columnNames.add(0, "Name");
        columnNames.add(1, "URL");
        fields = new ArrayList();
        fields.add(mDirectoriesNameField);
        fields.add(mDirectoriesUrlField);
        mDirectoriesOptionsTable = new OptionsTable(this, columnNames, mDirectoriesColumnValues, fields);
        builder.add(mDirectoriesOptionsTable, cc.xyw(1, 23, 4));        

        JPanel panel = builder.getPanel();
        //FormDebugUtils.dumpAll(panel);
        add(panel);
    }

    public void load() {
    }
    
    public boolean valid() {
        if (mTitleField.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Invalid title.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (mDirectoriesColumnValues.size() == 0) {
            JOptionPane.showMessageDialog(this, "No directories configured.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }        

    public void save() {
        log.debug("save()");
        VideocastingConfiguration videocastConfiguration = (VideocastingConfiguration) mAppConfiguration;
        videocastConfiguration.setName(mTitleField.getText());
        videocastConfiguration.setDownload(Integer.parseInt(((NameValue) mDownloadCombo.getSelectedItem()).getValue()));
        ArrayList newItems = new ArrayList();
        Iterator iterator = mSubscriptionsColumnValues.iterator();
        while (iterator.hasNext()) {
            ArrayList rows = (ArrayList) iterator.next();
            log.debug("Path=" + rows.get(0));
            newItems.add(new NameValue((String) rows.get(0), (String) rows.get(1)));
        }
        try
        {
        	Galleon.setVideocasts(newItems);
        }
        catch (Exception ex)
        {
        	Tools.logException(VideocastingOptionsPanel.class, ex);
        }
        newItems = new ArrayList();
        iterator = mDirectoriesColumnValues.iterator();
        while (iterator.hasNext()) {
            ArrayList rows = (ArrayList) iterator.next();
            log.debug("Path=" + rows.get(0));
            newItems.add(new NameValue((String) rows.get(0), (String) rows.get(1)));
        }
        videocastConfiguration.setDirectorys(newItems);
        videocastConfiguration.setShared(mSharedField.isSelected());
    }

    private JTextComponent mTitleField;

    private JComboBox mDownloadCombo;
    
    private JTextComponent mSubscriptionsNameField;

    private JTextComponent mSubscriptionsUrlField;

    private OptionsTable mSubscriptionsOptionsTable;

    private ArrayList mSubscriptionsColumnValues;    
    
    private JTextComponent mDirectoriesNameField;

    private JTextComponent mDirectoriesUrlField;

    private OptionsTable mDirectoriesOptionsTable;

    private ArrayList mDirectoriesColumnValues;
    
    private JCheckBox mSharedField;
}