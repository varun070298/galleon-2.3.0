package org.lnicholls.galleon.winamp;

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

import java.awt.Image;

import com.tivo.hme.sdk.View;

public class TextControl extends View {

    public TextControl(View parent, int x, int y, int width, int height, String characters, Image fonts, int fontWidth,
            int fontHeight, int ySpacing) {
        super(parent, x, y, width, height);
        mCharacters = characters;
        mFonts = fonts;
        mFontWidth = fontWidth;
        mFontHeight = fontHeight;
        mYSpacing = ySpacing;
    }

    public void setText(String text) {
        if (!text.equals(mText)) {
            if (text.trim().length() == 0)
                text = " ";

            clearResource();
            mText = text.toUpperCase();
            Image banner = Util.createBanner(mCharacters, mFonts, mFontWidth, mFontHeight, 0, mText.toUpperCase());
            setResource(createImage(Util.cropImage(banner, 0, 0, getWidth(), getHeight())));
        }
    }

    public String getText() {
        return mText;
    }

    private String mText;

    private String mCharacters;

    private Image mFonts;

    private int mFontWidth;

    private int mFontHeight;

    private int mYSpacing;
}