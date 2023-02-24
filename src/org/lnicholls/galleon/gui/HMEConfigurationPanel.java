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

package org.lnicholls.galleon.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;

import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.WordUtils;

import org.apache.log4j.Logger;

import org.lnicholls.galleon.util.Tools;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.debug.*;

import org.lnicholls.galleon.app.ConfigurationPanel;

public class HMEConfigurationPanel extends JPanel implements ConfigurationPanel {

	private static Logger log = Logger.getLogger(HMEConfigurationPanel.class.getName());

	public HMEConfigurationPanel(Object bean) {
		setLayout(new GridLayout(0, 1));
		target = bean;

		try {
			BeanInfo bi = Introspector.getBeanInfo(target.getClass());
			properties = bi.getPropertyDescriptors();
		} catch (IntrospectionException ex) {
			Tools.logException(HMEConfigurationPanel.class, ex, "PropertySheet: Couldn't introspect");
			return;
		}

		editors = new PropertyEditor[properties.length];
		values = new Object[properties.length];
		views = new Component[properties.length];
		labels = new JLabel[properties.length];

		for (int i = 0; i < properties.length; i++) {

			// Don't display hidden or expert properties.
			if (properties[i].isHidden() || properties[i].isExpert()) {
				continue;
			}

			String name = properties[i].getDisplayName();
			Class type = properties[i].getPropertyType();
			Method getter = properties[i].getReadMethod();
			Method setter = properties[i].getWriteMethod();

			// Only display read/write properties.
			if (getter == null || setter == null) {
				continue;
			}

			Component view = null;

			try {
				Object args[] = {};
				Object value = getter.invoke(target, args);
				values[i] = value;

				PropertyEditor editor = null;
				Class pec = properties[i].getPropertyEditorClass();
				if (pec != null) {
					try {
						editor = (PropertyEditor) pec.newInstance();
					} catch (Exception ex) {
						// Drop through.
					}
				}
				if (editor == null) {
					editor = PropertyEditorManager.findEditor(type);
				}
				editors[i] = editor;

				// If we can't edit this component, skip it.
				if (editor == null) {
					// If it's a user-defined property we give a warning.
					String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
					if (getterClass.indexOf("java.") != 0) {
						log.error("Warning: Can't find public property editor for property \"" + name
								+ "\".  Skipping.");
					}
					continue;
				}

				// Don't try to set null values:
				if (value == null) {
					// If it's a user-defined property we give a warning.
					String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
					if (getterClass.indexOf("java.") != 0) {
						log.error("Warning: Property \"" + name + "\" has null initial value.  Skipping.");
					}
					continue;
				}

				editor.setValue(value);
				// editor.addPropertyChangeListener(adaptor);

				// Now figure out how to display it...
				if (editor.isPaintable() && editor.supportsCustomEditor()) {
					view = new PropertyCanvas(frame, editor);
				} else if (editor.getTags() != null) {
					view = new PropertySelector(editor);
				} else if (editor.getAsText() != null) {
					String init = editor.getAsText();
					view = new PropertyText(editor);
				} else {
					log.error("Warning: Property \"" + name + "\" has non-displayabale editor.  Skipping.");
					continue;
				}

			} catch (InvocationTargetException ex) {
				Tools.logException(HMEConfigurationPanel.class, ex.getTargetException(), "Skipping property " + name);
				continue;
			} catch (Exception ex) {
				Tools.logException(HMEConfigurationPanel.class, ex, "Skipping property " + name);
				continue;
			}

			labels[i] = new JLabel(WordUtils.capitalize(name), JLabel.RIGHT);
			// add(labels[i]);

			views[i] = view;
			// add(views[i]);
		}

		int validCounter = 0;
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null)
				validCounter++;
		}

		String rowStrings = ""; // general
		if (validCounter > 0)
			rowStrings = "pref, ";
		else
			rowStrings = "pref";

		int counter = 0;
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null) {
				if (++counter == (validCounter))
					rowStrings = rowStrings + "9dlu, " + "pref";
				else
					rowStrings = rowStrings + "9dlu, " + "pref, ";
			}
		}

		FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", rowStrings);

		PanelBuilder builder = new PanelBuilder(layout);
		//DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xyw(1, 1, 4));

		counter = 0;
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null) {
				counter++;
				builder.add(labels[i], cc.xy(1, counter * 2 + 1));
				builder.add(views[i], cc.xy(3, counter * 2 + 1));
			}
		}

		JPanel panel = builder.getPanel();
		//FormDebugUtils.dumpAll(panel);
		add(panel);

	}

	public boolean valid() {
		return true;
	}

	public void load() {

	}

	public void save() {
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null) {
				Method writeMethod = properties[i].getWriteMethod();
				Object[] parameters = new Object[1];
				parameters[0] = editors[i].getValue();
				try
				{
					writeMethod.invoke(target, parameters);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	private JPanel frame;

	private Object target;

	private PropertyDescriptor properties[];

	private PropertyEditor editors[];

	private Object values[];

	private Component views[];

	private JLabel labels[];

	private boolean processEvents;

	private static int hPad = 4;

	private static int vPad = 4;

	private int maxHeight = 500;

	private int maxWidth = 300;
}

class PropertyCanvas extends Canvas implements MouseListener {

	PropertyCanvas(JPanel frame, PropertyEditor pe) {
		this.frame = frame;
		editor = pe;
		addMouseListener(this);
	}

	public void paint(Graphics g) {
		Rectangle box = new Rectangle(2, 2, getSize().width - 4, getSize().height - 4);
		editor.paintValue(g, box);
	}

	private static boolean ignoreClick = false;

	public void mouseClicked(MouseEvent evt) {
		if (!ignoreClick) {
			try {
				ignoreClick = true;
				int x = frame.getLocation().x - 30;
				int y = frame.getLocation().y + 50;
				// new PropertyDialog(frame, editor, x, y);
			} finally {
				ignoreClick = false;
			}
		}
	}

	public void mousePressed(MouseEvent evt) {
	}

	public void mouseReleased(MouseEvent evt) {
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	private JPanel frame;

	private PropertyEditor editor;
}

class PropertySelector extends Choice implements ItemListener {

	PropertySelector(PropertyEditor pe) {
		editor = pe;
		String tags[] = editor.getTags();
		for (int i = 0; i < tags.length; i++) {
			addItem(tags[i]);
		}
		select(0);
		// This is a noop if the getAsText is not a tag.
		select(editor.getAsText());
		addItemListener(this);
	}

	public void itemStateChanged(ItemEvent evt) {
		String s = getSelectedItem();
		editor.setAsText(s);
	}

	public void repaint() {
		select(editor.getAsText());
	}

	PropertyEditor editor;
}

class PropertyText extends JTextField implements KeyListener, FocusListener {

	PropertyText(PropertyEditor pe) {
		super(pe.getAsText());
		editor = pe;
		addKeyListener(this);
		addFocusListener(this);
	}

	/*
	 * public void repaint() { setText(editor.getAsText()); }
	 */

	protected void updateEditor() {
		try {
			editor.setAsText(getText());
		} catch (IllegalArgumentException ex) {
			// Quietly ignore.
		}
	}

	// ----------------------------------------------------------------------
	// Focus listener methods.

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		updateEditor();
	}

	// ----------------------------------------------------------------------
	// Keyboard listener methods.

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			updateEditor();
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	// ----------------------------------------------------------------------
	private PropertyEditor editor;
}
