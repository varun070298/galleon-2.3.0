package org.lnicholls.galleon.apps.movies;

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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;
import org.lnicholls.galleon.util.NameValue;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.stanford.ejalbert.BrowserLauncher;

public class MoviesOptionsPanel extends AppConfigurationPanel {
    private static Logger log = Logger.getLogger(MoviesOptionsPanel.class.getName());

    public MoviesOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        MoviesConfiguration moviesConfiguration = (MoviesConfiguration) appConfiguration;

        mTitleField = new JTextField(moviesConfiguration.getName());
        mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(moviesConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
        mCityField = new JTextField(moviesConfiguration.getCity());
        mStateCombo = new JComboBox();
        mStateCombo.addItem(new ComboWrapper("Alabama", "AL"));
        mStateCombo.addItem(new ComboWrapper("Alaska", "AK"));
        mStateCombo.addItem(new ComboWrapper("Arizona", "AZ"));
        mStateCombo.addItem(new ComboWrapper("Arkansas", "AR"));
        mStateCombo.addItem(new ComboWrapper("California", "CA"));
        mStateCombo.addItem(new ComboWrapper("Colorado", "CO"));
        mStateCombo.addItem(new ComboWrapper("Connecticut", "CT"));
        mStateCombo.addItem(new ComboWrapper("Delaware", "DE"));
        mStateCombo.addItem(new ComboWrapper("Wash. D.C.", "DC"));
        mStateCombo.addItem(new ComboWrapper("Florida", "FL"));
        mStateCombo.addItem(new ComboWrapper("Georgia", "GA"));
        mStateCombo.addItem(new ComboWrapper("Hawaii", "HI"));
        mStateCombo.addItem(new ComboWrapper("Idaho", "ID"));
        mStateCombo.addItem(new ComboWrapper("Illinois", "IL"));
        mStateCombo.addItem(new ComboWrapper("Indiana", "IN"));
        mStateCombo.addItem(new ComboWrapper("Iowa", "IA"));
        mStateCombo.addItem(new ComboWrapper("Kansas", "KS"));
        mStateCombo.addItem(new ComboWrapper("Kentucky", "KY"));
        mStateCombo.addItem(new ComboWrapper("Louisiana", "LA"));
        mStateCombo.addItem(new ComboWrapper("Maine", "ME"));
        mStateCombo.addItem(new ComboWrapper("Maryland", "MD"));
        mStateCombo.addItem(new ComboWrapper("Massachusetts", "MA"));
        mStateCombo.addItem(new ComboWrapper("Michigan", "MI"));
        mStateCombo.addItem(new ComboWrapper("Minnesota", "MN"));
        mStateCombo.addItem(new ComboWrapper("Mississippi", "MS"));
        mStateCombo.addItem(new ComboWrapper("Missouri", "MO"));
        mStateCombo.addItem(new ComboWrapper("Montana", "MT"));
        mStateCombo.addItem(new ComboWrapper("Nebraska", "NE"));
        mStateCombo.addItem(new ComboWrapper("Nevada", "NV"));
        mStateCombo.addItem(new ComboWrapper("New Hampshire", "NH"));
        mStateCombo.addItem(new ComboWrapper("New Jersey", "NJ"));
        mStateCombo.addItem(new ComboWrapper("New Mexico", "NM"));
        mStateCombo.addItem(new ComboWrapper("New York", "NY"));
        mStateCombo.addItem(new ComboWrapper("North Carolina", "NC"));
        mStateCombo.addItem(new ComboWrapper("North Dakota", "ND"));
        mStateCombo.addItem(new ComboWrapper("Ohio", "OH"));
        mStateCombo.addItem(new ComboWrapper("Oklahoma", "OK"));
        mStateCombo.addItem(new ComboWrapper("Oregon", "OR"));
        mStateCombo.addItem(new ComboWrapper("Pennsylvania", "PA"));
        mStateCombo.addItem(new ComboWrapper("Rhode Island", "RI"));
        mStateCombo.addItem(new ComboWrapper("So. Carolina", "SC"));
        mStateCombo.addItem(new ComboWrapper("So. Dakota", "SD"));
        mStateCombo.addItem(new ComboWrapper("Tennessee", "TN"));
        mStateCombo.addItem(new ComboWrapper("Texas", "TX"));
        mStateCombo.addItem(new ComboWrapper("Utah", "UT"));
        mStateCombo.addItem(new ComboWrapper("Vermont", "VT"));
        mStateCombo.addItem(new ComboWrapper("Virginia", "VA"));
        mStateCombo.addItem(new ComboWrapper("Washington", "WA"));
        mStateCombo.addItem(new ComboWrapper("West Virginia", "WV"));
        mStateCombo.addItem(new ComboWrapper("Wisconsin", "WI"));
        mStateCombo.addItem(new ComboWrapper("Wyoming", "WY"));
        defaultCombo(mStateCombo, moviesConfiguration.getState());
        mZipField = new JTextField(moviesConfiguration.getZip());

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + // general
                "9dlu, " + "pref, " + // title
                "3dlu, " + "pref, " + // share
                "9dlu, " + "pref, " + // location
                "9dlu, " + "pref, " + // city
                "3dlu, " + "pref, " + // state
                "3dlu, " + "pref"); // zip

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 4));
        builder.addLabel("Title", cc.xy(1, 3));
        builder.add(mTitleField, cc.xyw(3, 3, 1));
        builder.add(mSharedField, cc.xyw(3, 5, 1));
        builder.addSeparator("Location", cc.xyw(1, 7, 4));
        builder.addLabel("City", cc.xy(1, 9));
        builder.add(mCityField, cc.xyw(3, 9, 1));
        builder.addLabel("State", cc.xy(1, 11));
        builder.add(mStateCombo, cc.xyw(3, 11, 1));
        builder.addLabel("Zip", cc.xy(1, 13));
        builder.add(mZipField, cc.xyw(3, 13, 1));

        JPanel panel = builder.getPanel();
        //FormDebugUtils.dumpAll(panel);
        add(panel);
    }

    public boolean valid() {
        if (mTitleField.getText().trim().length()==0)
        {
            JOptionPane.showMessageDialog(this, "Invalid title.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (mCityField.getText().trim().length()==0)
        {
            JOptionPane.showMessageDialog(this, "Invalid city.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }        
        if (mZipField.getText().trim().length()==0)
        {
            JOptionPane.showMessageDialog(this, "Invalid zip.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }        
        
        return true;
    }

    public void load() {
    }

    public void save() {
        MoviesConfiguration moviesConfiguration = (MoviesConfiguration) mAppConfiguration;
        moviesConfiguration.setId(mId);
        moviesConfiguration.setName(mTitleField.getText());
        moviesConfiguration.setCity(mCityField.getText());
        moviesConfiguration.setState(((NameValue) mStateCombo.getSelectedItem()).getValue());
        moviesConfiguration.setZip(mZipField.getText());
        moviesConfiguration.setShared(mSharedField.isSelected());
    }
    
    private String mId;

    private JTextComponent mTitleField;

    private JTextComponent mCityField;

    private JComboBox mStateCombo;

    private JTextComponent mZipField;
    
    private JCheckBox mSharedField;
}