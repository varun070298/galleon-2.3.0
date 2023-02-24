package org.lnicholls.galleon.apps.rss;

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
import org.lnicholls.galleon.app.AppConfigurationPanel.ComboWrapper;
import org.lnicholls.galleon.gui.OptionsTable;
import org.lnicholls.galleon.util.NameValue;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RSSOptionsPanel extends AppConfigurationPanel {
    private static Logger log = Logger.getLogger(RSSOptionsPanel.class.getName());

    public RSSOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        RSSConfiguration rssConfiguration = (RSSConfiguration) appConfiguration;

        mTitleField = new JTextField(rssConfiguration.getName());
        mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(rssConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
        mSortedField = new JCheckBox("Sort");
        mSortedField.setSelected(rssConfiguration.isSorted());
        mSortedField.setToolTipText("Sort the list");
        mNameField = new JTextField("");
        mFeedField = new JTextField("");
        //mDescriptionField = new JTextField("");
        //mTagsField = new JTextField("");
        //mPrivacyCombo = new JComboBox();
        //mPrivacyCombo.addItem(new ComboWrapper(RSSConfiguration.SharedFeed.PRIVATE, RSSConfiguration.SharedFeed.PRIVATE));
        //mPrivacyCombo.addItem(new ComboWrapper(RSSConfiguration.SharedFeed.PUBLIC, RSSConfiguration.SharedFeed.PUBLIC));
        //mPrivacyCombo.addItem(new ComboWrapper(RSSConfiguration.SharedFeed.FRIENDS, RSSConfiguration.SharedFeed.FRIENDS));
        //defaultCombo(mPrivacyCombo, RSSConfiguration.SharedFeed.PRIVATE);

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + "9dlu, " + "pref, "
                + // title
                "3dlu, " + "pref, " + // share
                "3dlu, " + "pref, " + // sort
                "9dlu, " + "pref, " + // directories
                "9dlu, " + "pref, " + // name
                "3dlu, " + "pref, " + // feed
                //"3dlu, " + "pref, " + // description
                //"3dlu, " + "pref, " + // tags
                //"3dlu, " + "pref, " + // privacy
                "3dlu, " + "pref");

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 4));
        builder.addLabel("Title", cc.xy(1, 3));
        builder.add(mTitleField, cc.xyw(3, 3, 1));
        builder.add(mSharedField, cc.xyw(3, 5, 1));
        builder.add(mSortedField, cc.xyw(3, 7, 1));
        builder.addSeparator("Feeds", cc.xyw(1, 9, 4));

        builder.addLabel("Name", cc.xy(1, 11));
        builder.add(mNameField, cc.xyw(3, 11, 1));
        builder.addLabel("URL", cc.xy(1, 13));
        builder.add(mFeedField, cc.xyw(3, 13, 1));
        //builder.addLabel("Description", cc.xy(1, 15));
        //builder.add(mDescriptionField, cc.xyw(3, 15, 1));
        //builder.addLabel("Tags", cc.xy(1, 17));
        //builder.add(mTagsField, cc.xyw(3, 17, 1));
        //builder.addLabel("Privacy", cc.xy(1, 19));
        //builder.add(mPrivacyCombo, cc.xyw(3, 19, 1));

        mColumnValues = new ArrayList();
        int counter = 0;
        for (Iterator i = rssConfiguration.getSharedFeeds().iterator(); i.hasNext();) {
        	RSSConfiguration.SharedFeed value = (RSSConfiguration.SharedFeed) i.next();
            ArrayList values = new ArrayList();
            values.add(0, value.getName());
            values.add(1, value.getValue());
            //values.add(2, value.getDescription());
            //values.add(3, value.getTags());
            //values.add(4, value.getPrivacy());
            mColumnValues.add(counter++, values);
        }

        ArrayList columnNames = new ArrayList();
        columnNames.add(0, "Name");
        columnNames.add(1, "URL");
        //columnNames.add(2, "Description");
        //columnNames.add(3, "Tags");
        //columnNames.add(4, "Privacy");
        ArrayList fields = new ArrayList();
        fields.add(mNameField);
        fields.add(mFeedField);
        //fields.add(mDescriptionField);
        //fields.add(mTagsField);
        //fields.add(mPrivacyCombo);
        mOptionsTable = new OptionsTable(this, columnNames, mColumnValues, fields);
        builder.add(mOptionsTable, cc.xyw(1, 15, 4));

        JPanel panel = builder.getPanel();
        //FormDebugUtils.dumpAll(panel);
        add(panel);

    }

    public boolean valid() {
        if (mTitleField.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Invalid title.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (mColumnValues.size() == 0) {
            JOptionPane.showMessageDialog(this, "No URLs configured.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }    

    public void load() {
    }

    public void save() {
        RSSConfiguration rssConfiguration = (RSSConfiguration) mAppConfiguration;
        rssConfiguration.setName(mTitleField.getText());
        ArrayList newItems = new ArrayList();
        Iterator iterator = mColumnValues.iterator();
        while (iterator.hasNext()) {
            ArrayList rows = (ArrayList) iterator.next();
            //newItems.add(new RSSConfiguration.SharedFeed((String) rows.get(0), (String) rows.get(1), (String) rows.get(2), (String) rows.get(3), (String) rows.get(4)));            newItems.add(new RSSConfiguration.SharedFeed((String) rows.get(0), (String) rows.get(1), "", "", RSSConfiguration.SharedFeed.PRIVATE));
        }
        rssConfiguration.setSharedFeeds(newItems);
        rssConfiguration.setShared(mSharedField.isSelected());
    }

    private JTextComponent mTitleField;

    private JTextComponent mNameField;

    private JTextComponent mFeedField;
    
    //private JTextComponent mDescriptionField;
    
    //private JTextComponent mTagsField;
    
    //private JComboBox mPrivacyCombo;

    private OptionsTable mOptionsTable;

    private ArrayList mColumnValues;
    
    private JCheckBox mSharedField;
    
    private JCheckBox mSortedField;
}