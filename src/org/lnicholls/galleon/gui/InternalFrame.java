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
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;

import org.apache.log4j.Logger;

public class InternalFrame extends JPanel {
    private static Logger log = Logger.getLogger(InternalFrame.class.getName());

    private static class mGradientPanel extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isOpaque()) {
                return;
            } else {
                int width = getWidth();
                int height = getHeight();
                Graphics2D g2 = (Graphics2D) g;
                Paint storedPaint = g2.getPaint();
                g2.setPaint(new GradientPaint(0.0F, 0.0F, getBackground(), width, 0.0F, UIManager.getColor("control")));
                g2.fillRect(0, 0, width, height);
                g2.setPaint(storedPaint);
                return;
            }
        }

        private mGradientPanel(LayoutManager layoutManager, Color background) {
            super(layoutManager);
            setBackground(background);
        }

    }

    public InternalFrame(String title) {
        super(new BorderLayout());
        mIsSelected = false;
        mTitleLabel = new JLabel(title, null, 10);
        JPanel header = buildHeader(mTitleLabel);
        add(header, "North");
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setSelected(true);
        updateHeader();
    }

    public Icon getFrameIcon() {
        return mTitleLabel.getIcon();
    }

    public void setFrameIcon(Icon icon) {
        mTitleLabel.setIcon(icon);
    }

    public String getTitle() {
        return mTitleLabel.getText();
    }

    public void setTitle(String text) {
        mTitleLabel.setText(text);
    }

    public Component getContent() {
        return (getComponentCount() > 1) ? getComponent(1) : null;
    }

    public void setContent(Component content) {
        if (getComponentCount() > 1)
            remove(getContent());
        add(content, "Center");
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
        updateHeader();
    }

    private JPanel buildHeader(JLabel label) {
        mGradientPanel = new mGradientPanel(new BorderLayout(), UIManager
                .getColor("InternalFrame.activeTitleBackground"));
        label.setOpaque(false);
        mGradientPanel.add(label, "West");
        mGradientPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mHeaderPanel = new JPanel(new BorderLayout());
        mHeaderPanel.add(mGradientPanel, "Center");
        mHeaderPanel.setOpaque(false);
        return mHeaderPanel;
    }

    private void updateHeader() {
        mGradientPanel.setBackground(UIManager.getColor("InternalFrame.activeTitleBackground"));
        mGradientPanel.setOpaque(mIsSelected);
        mTitleLabel.setForeground(UIManager.getColor(mIsSelected ? "InternalFrame.activeTitleForeground"
                : "Label.foreground"));
        mHeaderPanel.repaint();
    }

    public void updateUI() {
        super.updateUI();
        if (mTitleLabel != null)
            updateHeader();
    }

    private JLabel mTitleLabel;

    private mGradientPanel mGradientPanel;

    private JPanel mHeaderPanel;

    private boolean mIsSelected;
}