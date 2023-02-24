package org.lnicholls.galleon.apps.weather;

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
import org.lnicholls.galleon.apps.weather.WeatherData.Location;
import org.lnicholls.galleon.util.NameValue;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.stanford.ejalbert.BrowserLauncher;

public class WeatherOptionsPanel extends AppConfigurationPanel {
    private static Logger log = Logger.getLogger(WeatherOptionsPanel.class.getName());

    public WeatherOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        WeatherConfiguration weatherConfiguration = (WeatherConfiguration) appConfiguration;

        mTitleField = new JTextField(weatherConfiguration.getName());
        mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(weatherConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
        mCityField = new JTextField(weatherConfiguration.getCity());
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
        defaultCombo(mStateCombo, weatherConfiguration.getState());
        mZipField = new JTextField(weatherConfiguration.getZip());

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + // general
                "9dlu, " + "pref, " + // title
                "3dlu, " + "pref, " + // share
                "9dlu, " + "pref, " + // location
                "9dlu, " + "pref, " + // city
                "3dlu, " + "pref, " + // state
                "3dlu, " + "pref, " + // zip
                "9dlu, " + "pref, " + // links
                "3dlu, " + "pref, " + // 1
                "3dlu, " + "pref, " + // 2
                "3dlu, " + "pref, " + // 3
                "3dlu, " + "pref");

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
        builder.addSeparator("Featured on weather.com®", cc.xyw(1, 15, 4));
        // TODO Add location id parameter
        // TODO Determine list dynamically
        JLabel label = new JLabel("Pollen Reports");
        label.setForeground(Color.blue);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText("Open site in web browser");
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.weather.com/outlook/health/allergies/"+mZipField.getText()+"?par=xoap");
                } catch (Exception ex) {
                }
            }
        });
        builder.add(label, cc.xyw(1, 17, 3));
        label = new JLabel("Airport Delays");
        label.setForeground(Color.blue);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText("Open site in web browser");
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.weather.com/outlook/travel/flights/citywx/"+mZipField.getText()+"?par=xoap");
                } catch (Exception ex) {
                }
            }
        });
        builder.add(label, cc.xyw(1, 19, 3));
        label = new JLabel("Special Events");
        label.setForeground(Color.blue);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText("Open site in web browser");
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.weather.com/outlook/events/special/result/"+mZipField.getText()+"?par=xoap");
                } catch (Exception ex) {
                }
            }
        });
        builder.add(label, cc.xyw(1, 21, 3));
        label = new JLabel("Weather data provided by weather.com®");
        label.setForeground(Color.blue);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText("Open site in web browser");
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.weather.com");
                } catch (Exception ex) {
                }
            }
        });
        builder.add(label, cc.xyw(1, 23, 3));

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
        
        List locations = WeatherData.getLocations(mCityField.getText(), ((NameValue) mStateCombo.getSelectedItem()).getValue(), mZipField.getText());
        if (locations!=null && locations.size()>0)
        {
            if (locations.size()>1)
            {
                Object[] values = new String[locations.size()];
                for (int i=0;i<locations.size();i++)
                {
                    Location location = (Location)locations.get(i);
                    values[i] = location.getValue();
                }
                
                String selectedValue = (String)JOptionPane.showInputDialog(this,
                            "Choose one", "Location",
                            JOptionPane.INFORMATION_MESSAGE, null,
                            values, values[0]);
                if (selectedValue==null)
                    return false;
                else
                {
                    for (int i=0;i<locations.size();i++)
                    {
                        Location location = (Location)locations.get(i);
                        if (location.getValue().equals(selectedValue))
                        {
                            mId = location.getId();
                            return true;
                        }
                    }
                }
            }
            else
            {
                Location location = (Location)locations.get(0);
                mId = location.getId();
                return true;
            }
        }
        
        JOptionPane.showMessageDialog(this, "Invalid weather location.", "Error", JOptionPane.ERROR_MESSAGE);

        return false;
    }

    public void load() {
    }

    public void save() {
        WeatherConfiguration weatherConfiguration = (WeatherConfiguration) mAppConfiguration;
        weatherConfiguration.setId(mId);
        weatherConfiguration.setName(mTitleField.getText());
        weatherConfiguration.setCity(mCityField.getText());
        weatherConfiguration.setState(((NameValue) mStateCombo.getSelectedItem()).getValue());
        weatherConfiguration.setZip(mZipField.getText());
        weatherConfiguration.setShared(mSharedField.isSelected());
    }
    
    private String mId;

    private JTextComponent mTitleField;

    private JComboBox mReloadCombo;

    private JTextComponent mCityField;

    private JComboBox mStateCombo;

    private JTextComponent mZipField;
    
    private JCheckBox mSharedField;
}