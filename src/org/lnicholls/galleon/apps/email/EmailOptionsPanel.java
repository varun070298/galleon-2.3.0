package org.lnicholls.galleon.apps.email;

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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;
import org.lnicholls.galleon.apps.email.EmailConfiguration.Account;
import org.lnicholls.galleon.gui.OptionsTable;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class EmailOptionsPanel extends AppConfigurationPanel {
    private static Logger log = Logger.getLogger(EmailOptionsPanel.class.getName());

    public EmailOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        EmailConfiguration emailConfiguration = (EmailConfiguration) appConfiguration;

        mTitleField = new JTextField(emailConfiguration.getName());
        mSharedField = new JCheckBox("Share");
        mSharedField.setSelected(emailConfiguration.isShared());
        mSharedField.setToolTipText("Share this app");
        mReloadCombo = new JComboBox();
        mReloadCombo.addItem(new ComboWrapper("5 minutes", "5"));
        mReloadCombo.addItem(new ComboWrapper("10 minutes", "10"));
        mReloadCombo.addItem(new ComboWrapper("20 minutes", "20"));
        mReloadCombo.addItem(new ComboWrapper("30 minutes", "30"));
        mReloadCombo.addItem(new ComboWrapper("1 hour", "60"));
        mReloadCombo.addItem(new ComboWrapper("2 hours", "120"));
        mReloadCombo.addItem(new ComboWrapper("4 hours", "240"));
        mReloadCombo.addItem(new ComboWrapper("6 hours", "720"));
        mReloadCombo.addItem(new ComboWrapper("24 hours", "1440"));
        defaultCombo(mReloadCombo, Integer.toString(emailConfiguration.getReload()));
        mLimitCombo = new JComboBox();
        mLimitCombo.addItem(new ComboWrapper("Unlimited", "-1"));
        mLimitCombo.addItem(new ComboWrapper("1", "1"));
        mLimitCombo.addItem(new ComboWrapper("2", "2"));
        mLimitCombo.addItem(new ComboWrapper("3", "3"));
        mLimitCombo.addItem(new ComboWrapper("4", "4"));
        mLimitCombo.addItem(new ComboWrapper("5", "5"));
        mLimitCombo.addItem(new ComboWrapper("10", "10"));
        mLimitCombo.addItem(new ComboWrapper("20", "20"));
        mLimitCombo.addItem(new ComboWrapper("40", "40"));
        mLimitCombo.addItem(new ComboWrapper("60", "60"));
        defaultCombo(mLimitCombo, Integer.toString(emailConfiguration.getLimit()));        
        mNameField = new JTextField();
        mProtocolCombo = new JComboBox();
        mProtocolCombo.addItem(new ComboWrapper("POP3", "pop3"));
        mProtocolCombo.addItem(new ComboWrapper("POP3S", "pop3s"));
        mProtocolCombo.addItem(new ComboWrapper("IMAP", "imap"));
        mProtocolCombo.addItem(new ComboWrapper("IMAPS", "imaps"));
        mServerField = new JTextField();
        mUsernameField = new JTextField();
        mPasswordField = new JPasswordField();

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + // general
                "9dlu, " + "pref, " + // title
                "3dlu, " + "pref, " + // share
                "3dlu, " + "pref, " + // reload
                "3dlu, " + "pref, " + // limit
                "9dlu, " + "pref, " + // account
                "9dlu, " + "pref," + // name
                "9dlu, " + "pref," + // protocol
                "3dlu, " + "pref, " + // server
                "3dlu, " + "pref, " + // username
                "3dlu, " + "pref, " + // password
                "3dlu, " + "pref " // table
        );

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 4));
        builder.addLabel("Title", cc.xy(1, 3));
        builder.add(mTitleField, cc.xyw(3, 3, 1));
        builder.add(mSharedField, cc.xyw(3, 5, 1));
        builder.addLabel("Reload", cc.xy(1, 7));
        builder.add(mReloadCombo, cc.xyw(3, 7, 1));
        builder.addLabel("Download Limit", cc.xy(1, 9));
        builder.add(mLimitCombo, cc.xyw(3, 9, 1));

        builder.addSeparator("Accounts", cc.xyw(1, 11, 4));
        builder.addLabel("Name", cc.xy(1, 13));
        builder.add(mNameField, cc.xyw(3, 13, 1));
        builder.addLabel("Protocol", cc.xy(1, 15));
        builder.add(mProtocolCombo, cc.xyw(3, 15, 1));
        builder.addLabel("Server", cc.xy(1, 17));
        builder.add(mServerField, cc.xyw(3, 17, 1));
        builder.addLabel("Username", cc.xy(1, 19));
        builder.add(mUsernameField, cc.xyw(3, 19, 1));
        builder.addLabel("Password", cc.xy(1, 21));
        builder.add(mPasswordField, cc.xyw(3, 21, 1));

        mColumnValues = new ArrayList();
        int counter = 0;
        for (Iterator i = emailConfiguration.getAccounts().iterator(); i.hasNext();) {
            Account server = (Account) i.next();
            ArrayList values = new ArrayList();
            values.add(0, server.getName());
            values.add(1, server.getProtocol());
            values.add(2, server.getServer());
            values.add(3, Tools.decrypt(server.getUsername()));
            values.add(4, Tools.decrypt(server.getPassword()));
            mColumnValues.add(counter++, values);
        }

        ArrayList columnNames = new ArrayList();
        columnNames.add(0, "Name");
        columnNames.add(1, "Protocol");
        columnNames.add(2, "Server");
        columnNames.add(3, "Username");
        columnNames.add(4, "Password");
        ArrayList fields = new ArrayList();
        fields.add(mNameField);
        fields.add(mProtocolCombo);
        fields.add(mServerField);
        fields.add(mUsernameField);
        fields.add(mPasswordField);
        mOptionsTable = new OptionsTable(this, columnNames, mColumnValues, fields);
        builder.add(mOptionsTable, cc.xyw(1, 23, 4));

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
            JOptionPane.showMessageDialog(this, "No email accounts configured.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    public void load() {
    }

    public void save() {
        EmailConfiguration emailConfiguration = (EmailConfiguration) mAppConfiguration;
        emailConfiguration.setName(mTitleField.getText());
        emailConfiguration.setReload(Integer.parseInt(((NameValue) mReloadCombo.getSelectedItem()).getValue()));
        emailConfiguration.setLimit(Integer.parseInt(((NameValue) mLimitCombo.getSelectedItem()).getValue()));

        ArrayList newItems = new ArrayList();
        Iterator iterator = mColumnValues.iterator();
        while (iterator.hasNext()) {
            ArrayList rows = (ArrayList) iterator.next();
            Account server = new Account();
            server.setName((String) rows.get(0));
            server.setProtocol((String) rows.get(1));
            server.setServer((String) rows.get(2));
            server.setUsername(Tools.encrypt((String) rows.get(3)));
            server.setPassword(Tools.encrypt((String) rows.get(4)));
            newItems.add(server);
        }
        emailConfiguration.setAccounts(newItems);
        emailConfiguration.setShared(mSharedField.isSelected());
    }

    private JTextComponent mTitleField;

    private JComboBox mReloadCombo;
    
    private JComboBox mLimitCombo;

    private JTextComponent mNameField;

    private JComboBox mProtocolCombo;

    private JTextComponent mServerField;

    private JTextField mUsernameField;

    private JPasswordField mPasswordField;

    private OptionsTable mOptionsTable;

    private ArrayList mColumnValues;
    
    private JCheckBox mSharedField;
}