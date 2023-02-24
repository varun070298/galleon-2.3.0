package org.lnicholls.galleon.apps.upcoming;

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

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;
import org.lnicholls.galleon.gui.Galleon;
import org.lnicholls.galleon.gui.OptionsTable;
import org.lnicholls.galleon.util.NameValue;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class UpcomingOptionsPanel extends AppConfigurationPanel {
	private static Logger log = Logger.getLogger(UpcomingOptionsPanel.class.getName());

	public UpcomingOptionsPanel(AppConfiguration appConfiguration) {
		super(appConfiguration);
		setLayout(new GridLayout(0, 1));

		UpcomingConfiguration upcomingConfiguration = (UpcomingConfiguration) appConfiguration;

		mTitleField = new JTextField(upcomingConfiguration.getName());
		mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(upcomingConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
		mNameField = new JTextField("");
		mCountryCombo = new JComboBox();
		mCountryCombo.addItem(new ComboWrapper("", ""));
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			mCountries = Galleon.getUpcomingCountries();
			if (mCountries != null) {
				Iterator iterator = mCountries.iterator();
				while (iterator.hasNext()) {
					NameValue nameValue = (NameValue) iterator.next();
					mCountryCombo.addItem(new ComboWrapper(nameValue.getName(), nameValue.getName()));
				}
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} catch (Exception ex) {
		}
		defaultCombo(mCountryCombo, ""); // United States = 1
		mCountryCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				mStateCombo.removeAllItems();
				mStateCombo.addItem(new ComboWrapper("", ""));
				mStateCombo.disable();
				mMetroCombo.removeAllItems();
				mMetroCombo.addItem(new ComboWrapper("", ""));
				mMetroCombo.disable();
				try {
					String country = ((NameValue) mCountryCombo.getSelectedItem()).getValue();
					String countryId = null;
					Iterator iterator = mCountries.iterator();
					while (iterator.hasNext()) {
						NameValue nameValue = (NameValue) iterator.next();
						if (nameValue.getName().equals(country)) {
							countryId = nameValue.getValue();
							break;
						}
					}
					if (countryId != null && countryId.trim().length() > 0) {
						mStates = Galleon.getUpcomingStates(countryId);
						if (mStates != null) {
							iterator = mStates.iterator();
							while (iterator.hasNext()) {
								NameValue nameValue = (NameValue) iterator.next();
								if (nameValue.getName().trim().length() > 0 && nameValue.getValue().trim().length() > 0)
									mStateCombo.addItem(new ComboWrapper(nameValue.getName(), nameValue.getName()));
							}
						}
					}
				} catch (Exception ex) {
				}
				mStateCombo.enable();
				defaultCombo(mStateCombo, "");
				mMetroCombo.enable();
				defaultCombo(mMetroCombo, "");
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});

		mStateCombo = new JComboBox();
		mStateCombo.addItem(new ComboWrapper("", ""));
		defaultCombo(mStateCombo, "");
		mStateCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				mMetroCombo.removeAllItems();
				mMetroCombo.addItem(new ComboWrapper("", ""));
				mMetroCombo.disable();
				try {
					String state = ((NameValue) mStateCombo.getSelectedItem()).getValue();
					String stateId = null;
					Iterator iterator = mStates.iterator();
					while (iterator.hasNext()) {
						NameValue nameValue = (NameValue) iterator.next();
						if (nameValue.getName().equals(state)) {
							stateId = nameValue.getValue();
							break;
						}
					}
					if (stateId.trim().length() > 0) {
						mMetros = Galleon.getUpcomingMetros(stateId);
						if (mMetros != null) {
							iterator = mMetros.iterator();
							while (iterator.hasNext()) {
								NameValue nameValue = (NameValue) iterator.next();
								if (nameValue.getName().trim().length() > 0 && nameValue.getValue().trim().length() > 0)
									mMetroCombo.addItem(new ComboWrapper(nameValue.getName(), nameValue.getName()));
							}
						}
					}
				} catch (Exception ex) {
				}
				mMetroCombo.enable();
				defaultCombo(mMetroCombo, "");
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});

		mMetroCombo = new JComboBox();
		mMetroCombo.addItem(new ComboWrapper("", ""));
		defaultCombo(mMetroCombo, "");

		FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + "9dlu, " + "pref, "
				+ // title
				"3dlu, " + "pref, " + // share
				"9dlu, " + "pref, " + // Metros
				"9dlu, " + "pref, " + // country
				"3dlu, " + "pref, " + // state
				"3dlu, " + "pref, " + // metro
				"3dlu, " + "pref");

		PanelBuilder builder = new PanelBuilder(layout);
		// DefaultFormBuilder builder = new DefaultFormBuilder(new
		// FormDebugPanel(), layout);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xyw(1, 1, 4));
		builder.addLabel("Title", cc.xy(1, 3));
		builder.add(mTitleField, cc.xyw(3, 3, 1));
		builder.add(mSharedField, cc.xyw(3, 5, 1));
		builder.addSeparator("Metros", cc.xyw(1, 7, 4));

		builder.addLabel("Country", cc.xy(1, 9));
		builder.add(mCountryCombo, cc.xyw(3, 9, 1));
		builder.addLabel("State", cc.xy(1, 11));
		builder.add(mStateCombo, cc.xyw(3, 11, 1));
		builder.addLabel("Metro", cc.xy(1, 13));
		builder.add(mMetroCombo, cc.xyw(3, 13, 1));

		mColumnValues = new ArrayList();
		int counter = 0;
		for (Iterator i = upcomingConfiguration.getLocations().iterator(); i.hasNext();) {
			UpcomingConfiguration.Location value = (UpcomingConfiguration.Location) i.next();
			ArrayList values = new ArrayList();
			values.add(0, value.getCountry());
			values.add(1, value.getState());
			values.add(2, value.getMetro());
			mColumnValues.add(counter++, values);
		}

		ArrayList columnNames = new ArrayList();
		columnNames.add(0, "Country");
		columnNames.add(1, "State");
		columnNames.add(2, "Metro");
		ArrayList fields = new ArrayList();
		fields.add(mCountryCombo);
		fields.add(mStateCombo);
		fields.add(mMetroCombo);
		mOptionsTable = new OptionsTable(this, columnNames, mColumnValues, fields);
		builder.add(mOptionsTable, cc.xyw(1, 15, 4));

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
			JOptionPane.showMessageDialog(this, "No metros configured.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public void load() {
	}

	public void save() {
		UpcomingConfiguration upcomingConfiguration = (UpcomingConfiguration) mAppConfiguration;
		upcomingConfiguration.setName(mTitleField.getText());
		ArrayList newItems = new ArrayList();
		Iterator iterator = mColumnValues.iterator();
		while (iterator.hasNext()) {
			ArrayList rows = (ArrayList) iterator.next();
			newItems.add(new UpcomingConfiguration.Location((String) rows.get(0), (String) rows.get(1), (String) rows
					.get(2)));
		}
		upcomingConfiguration.setLocations(newItems);
		upcomingConfiguration.setShared(mSharedField.isSelected());
	}

	private JTextComponent mTitleField;

	private JTextComponent mNameField;

	private JComboBox mCountryCombo;

	private JComboBox mStateCombo;

	private JComboBox mMetroCombo;

	private OptionsTable mOptionsTable;

	private ArrayList mColumnValues;

	private List mCountries;

	private List mStates;

	private List mMetros;
	
	private JCheckBox mSharedField;
}