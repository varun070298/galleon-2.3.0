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

import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public class ScrollTextControl extends View {

    public ScrollTextControl(View parent, int x, int y, int width, int height, String characters, Image fonts,
            int fontWidth, int fontHeight, int ySpacing) {
        super(parent, x, y, width, height);
        mCharacters = characters;
        mFonts = fonts;
        mFontWidth = fontWidth;
        mFontHeight = fontHeight;
        mYSpacing = ySpacing;
        mScrollingText = new View(this, 0, 0, width, height);
    }

    public void setText(String text) {
        setText(text, 0);
    }

    public void setText(String text, int start) {
        clearResource();
        mText = text.toUpperCase();
        if (!mText.endsWith(" "))
            mText = mText + " ";

        int value = (int) Math.round((start + mFontWidth * mText.length()) / (float) mFontWidth * 500);
        Resource anim = getResource("*" + value);
        
        Image banner = Util.createBanner(mCharacters, mFonts, mFontWidth, mFontHeight, 0, mText.toUpperCase());

        mScrollingText.flush();
        mScrollingText.clearResource();
        mScrollingText.setBounds(start, 0, banner.getWidth(null), banner.getHeight(null));
        mScrollingText.setResource(createImage(banner));
        mScrollingText.setLocation(-mFontWidth * mText.length(), 0, anim);
        HmeEvent evt = new HmeEvent.Key(getApp().getID(), 0, KEY_TIVO, mCounter++);
        getApp().sendEvent(evt, anim);
    }

    public String getText() {
        return mText.trim();
    }

    public int getScrollPosition() {
        return mScrollingText.getX();
    }

    public boolean handleEvent(HmeEvent event) {
        switch (event.getOpCode()) {
        case EVT_KEY: {
            HmeEvent.Key e = (HmeEvent.Key) event;
            switch (e.getCode()) {
            case KEY_TIVO:
                int code = (int) e.getRawCode();
                if ((code + 1) >= mCounter)
                    setText(getText(), getWidth());
                return true;
            }
            break;
        }
        }
        return false;
    }

    private String mText = " ";

    private String mCharacters;

    private Image mFonts;

    private int mFontWidth;

    private int mFontHeight;

    private int mYSpacing;

    private View mScrollingText;

    private int mCounter;
}