package org.lnicholls.galleon.apps.traffic;

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
import org.lnicholls.galleon.gui.OptionsTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TrafficOptionsPanel extends AppConfigurationPanel {
	private static Logger log = Logger.getLogger(TrafficOptionsPanel.class.getName());

	public TrafficOptionsPanel(AppConfiguration appConfiguration) {
		super(appConfiguration);
		setLayout(new GridLayout(0, 1));

		TrafficConfiguration trafficConfiguration = (TrafficConfiguration) appConfiguration;

		mTitleField = new JTextField(trafficConfiguration.getName());
		mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(trafficConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
		mStreetField = new JTextField();
		mCityField = new JTextField();
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
		mZipField = new JTextField();
		mRadiusCombo = new JComboBox();
		mRadiusCombo.addItem(new ComboWrapper("10 miles", "10"));
		mRadiusCombo.addItem(new ComboWrapper("20 miles", "20"));
		mRadiusCombo.addItem(new ComboWrapper("30 miles", "30"));
		mRadiusCombo.addItem(new ComboWrapper("40 miles", "40"));
		mRadiusCombo.addItem(new ComboWrapper("50 miles", "50"));
		mRadiusCombo.addItem(new ComboWrapper("60 miles", "60"));
		mRadiusCombo.addItem(new ComboWrapper("70 miles", "70"));
		mRadiusCombo.addItem(new ComboWrapper("80 miles", "80"));
		mRadiusCombo.addItem(new ComboWrapper("90 miles", "90"));
		mRadiusCombo.addItem(new ComboWrapper("100 miles", "100"));

		FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + // general
				"9dlu, " + "pref, " + // title
				"3dlu, " + "pref, " + // share
				"9dlu, " + "pref, " + // location
				"9dlu, " + "pref, " + // street
				"9dlu, " + "pref, " + // city
				"3dlu, " + "pref, " + // state
				"3dlu, " + "pref, " + // zip
				"3dlu, " + "pref, " + // radius
				"3dlu, " + "pref"); // list

		PanelBuilder builder = new PanelBuilder(layout);
		// DefaultFormBuilder builder = new DefaultFormBuilder(new
		// FormDebugPanel(), layout);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xyw(1, 1, 4));
		builder.addLabel("Title", cc.xy(1, 3));
		builder.add(mTitleField, cc.xyw(3, 3, 1));
		builder.add(mSharedField, cc.xyw(3, 5, 1));
		builder.addSeparator("Location", cc.xyw(1, 7, 4));
		builder.addLabel("Street", cc.xy(1, 9));
		builder.add(mStreetField, cc.xyw(3, 9, 1));
		builder.addLabel("City", cc.xy(1, 11));
		builder.add(mCityField, cc.xyw(3, 11, 1));
		builder.addLabel("State", cc.xy(1, 13));
		builder.add(mStateCombo, cc.xyw(3, 13, 1));
		builder.addLabel("Zip", cc.xy(1, 15));
		builder.add(mZipField, cc.xyw(3, 15, 1));
		builder.addLabel("Radius", cc.xy(1, 17));
		builder.add(mRadiusCombo, cc.xyw(3, 17, 1));

		mColumnValues = new ArrayList();
		int counter = 0;
		for (Iterator i = trafficConfiguration.getLocations().iterator(); i.hasNext();) {
			TrafficConfiguration.Location value = (TrafficConfiguration.Location) i.next();
			ArrayList values = new ArrayList();
			values.add(0, value.getStreet());
			values.add(1, value.getCity());
			values.add(2, value.getState());
			values.add(3, value.getZip());
			values.add(4, value.getRadius());
			mColumnValues.add(counter++, values);
		}

		ArrayList columnNames = new ArrayList();
		columnNames.add(0, "Street");
		columnNames.add(1, "City");
		columnNames.add(2, "State");
		columnNames.add(3, "Zip");
		columnNames.add(4, "Radius");
		ArrayList fields = new ArrayList();
		fields.add(mStreetField);
		fields.add(mCityField);
		fields.add(mStateCombo);
		fields.add(mZipField);
		fields.add(mRadiusCombo);
		mOptionsTable = new OptionsTable(this, columnNames, mColumnValues, fields);
		builder.add(mOptionsTable, cc.xyw(1, 19, 4));

		JPanel panel = builder.getPanel();
		// FormDebugUtils.dumpAll(panel);
		add(panel);
	}

	public boolean valid() {
		if (mTitleField.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "Invalid title.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (mColumnValues.size() == 0) {
			JOptionPane.showMessageDialog(this, "No locations configured.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public void load() {
	}

	public void save() {
		TrafficConfiguration trafficConfiguration = (TrafficConfiguration) mAppConfiguration;
		trafficConfiguration.setName(mTitleField.getText());
		ArrayList newItems = new ArrayList();
		Iterator iterator = mColumnValues.iterator();
		while (iterator.hasNext()) {
			ArrayList rows = (ArrayList) iterator.next();
			newItems.add(new TrafficConfiguration.Location((String) rows.get(0), (String) rows.get(1), (String) rows
					.get(2), (String) rows.get(3), (String) rows.get(4)));
		}
		trafficConfiguration.setLocations(newItems);
		trafficConfiguration.setShared(mSharedField.isSelected());
	}

	private String mId;

	private JTextComponent mTitleField;

	private JTextComponent mStreetField;

	private JTextComponent mCityField;

	private JComboBox mStateCombo;

	private JTextComponent mZipField;

	private JComboBox mRadiusCombo;

	private OptionsTable mOptionsTable;

	private ArrayList mColumnValues;
	
	private JCheckBox mSharedField;
}