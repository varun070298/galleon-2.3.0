package org.lnicholls.galleon.app;

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

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.NameValue;

/*
 * GUI for configuring the app
 */

public abstract class AppConfigurationPanel extends JPanel implements ConfigurationPanel {
	private static Logger log = Logger.getLogger(AppConfigurationPanel.class.getName());

	public static class ComboWrapper extends NameValue {
		public ComboWrapper(String name, String value) {
			super(name, value);
		}

		public String toString() {
			return getName();
		}
	}

	public AppConfigurationPanel(AppConfiguration appConfiguration) {
		super();
		mAppConfiguration = appConfiguration;
	}

	public static void defaultCombo(JComboBox combo, String value) {
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (((NameValue) combo.getItemAt(i)).getValue().equals(value)) {
				combo.setSelectedIndex(i);
				return;
			}
		}
	}

	public boolean valid() {
		return true;
	}

	public abstract void load();

	public abstract void save();

	protected AppConfiguration mAppConfiguration;
}