package org.lnicholls.galleon.widget;

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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;

import org.lnicholls.galleon.util.Tools;

public class LabelText extends BView {

    private static Logger log = Logger.getLogger(LabelText.class.getName());
    
    private static Font DEFAULT_FONT = null;

    private static BufferedImage buffer = null;
    static {
        try {
            DEFAULT_FONT = Font.createFont(Font.TRUETYPE_FONT, ScrollText.class.getClassLoader().getResourceAsStream(
                    ScrollText.class.getPackage().getName().replace('.', '/') + "/" + "FreeSans.ttf"));

            buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public LabelText(BView parent, int x, int y, int width, int height, boolean visible) {
        super(parent, x, y, width, height, visible);

        mLabelText = new BText(this, 0, 0, width, height);
        mLabelText.setColor(Tools.darken(Color.WHITE));
        //mLabelText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
        mValueText = new BText(this, 0, 0, width, height);
        
        mFont = DEFAULT_FONT == null ? null : DEFAULT_FONT.deriveFont(Font.PLAIN, height-2);
    }

    public void setFlags(int flags) {
        //mLabelText.setFlags(flags);
        //mValueText.setFlags(flags);
        mFlags = flags;
    }

    public void setFont(Object font) {
        mLabelText.setFont(font);
        mValueText.setFont(font);
    }

    public void setColor(Object color) {
        mLabelText.setColor(Tools.darken((Color)color));
        mValueText.setColor(color);
    }
    
    public void setShadow(boolean shadow) {
        mLabelText.setShadow(shadow);
        mValueText.setShadow(shadow);
    }
    
    public void setLabel(String label)
    {
        mLabel = label;
        calculateText();
    }
    
    public void setValue(String value)
    {
        mValue = value;
        calculateText();
    }
    
    private void calculateText() {
        int labelWidth = 0;
        int valueWidth = 0;
        FontMetrics fontMetrics = null;
        if (mFont != null) {
            Graphics2D graphics2D = (Graphics2D) buffer.getGraphics();
            try {
                fontMetrics = graphics2D.getFontMetrics(mFont);
                labelWidth = fontMetrics.stringWidth(mLabel+" ");
                valueWidth = fontMetrics.stringWidth(mValue+" ");
            } finally {
                graphics2D.dispose();
            }
        }
        
        mLabelText.setValue(mLabel);
        mLabelText.setFlags(mFlags);
        mValueText.setValue(mValue);
        mValueText.setFlags(mFlags);
        
        if ((mFlags & BText.RSRC_HALIGN_LEFT) == 1)
        {
            mValueText.setLocation(labelWidth,0);            
        }
        else
        {
            mLabelText.setLocation(-valueWidth,0);
        }
        
        flush();
    }

    private BText mLabelText;

    private BText mValueText;
    
    private String mLabel = "";
    
    private String mValue = "";
    
    private Font mFont;
    
    private int mFlags;
}