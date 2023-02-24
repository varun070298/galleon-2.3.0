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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;

/**
 * @author Owner
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class HelpDialog extends JDialog {

    public HelpDialog(JDialog dialog, URL url) {
        super(dialog, "Help", false);
        initialize(dialog, url);
    }

    public HelpDialog(Frame frame, URL url) {
        super(frame, "Help", false);
        initialize(frame, url);
    }

    private void initialize(Component component, URL url) {
        JTextPane documentationField = new HelpPane();
        documentationField.setEditable(false);
        if (url!=null)
        {
            try {
                documentationField.setPage(url);
            } catch (Exception ex) {
                Tools.logException(HelpDialog.class, ex, "Invalid help url: " + url.toExternalForm());
            }
        }

        JScrollPane paneScrollPane = new JScrollPane(documentationField);
        //paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        paneScrollPane.setPreferredSize(new Dimension(500, 400));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(paneScrollPane, "Center");

        pack();
        setLocationRelativeTo(component);
    }

    static class HelpPane extends JTextPane {
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paint(graphics);
        }
    }
}