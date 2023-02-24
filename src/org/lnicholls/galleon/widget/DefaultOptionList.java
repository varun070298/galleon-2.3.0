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

import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;

public class DefaultOptionList extends BList {
    public DefaultOptionList(BView parent, int x, int y, int width, int height, int rowHeight) {
        super(parent, x, y, width, height, rowHeight);

        setBarAndArrows(BAR_HANG, BAR_DEFAULT, null, "push");
    }

    protected void createRow(BView parent, int index) {
        BText text = new BText(parent, 10, 4, parent.getWidth() - 40, parent.getHeight() - 4);
        text.setShadow(true);
        text.setFlags(RSRC_HALIGN_LEFT);
        text.setValue(get(index).toString());
    }

    public boolean handleKeyPress(int code, long rawcode) {
        switch (code) {
        case KEY_SELECT:
		case KEY_RIGHT:
		case KEY_PLAY:
        case KEY_CHANNELUP:
        case KEY_CHANNELDOWN:
            return getParent().handleKeyPress(code, rawcode);
        }
        return super.handleKeyPress(code, rawcode);
    }
}