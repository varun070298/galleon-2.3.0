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

import java.util.List;

import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BRect;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.View;

/*
 * Based on TiVo HME Bananas BList by Adam Doppelt
 */

public abstract class Grid extends BList {

    /**
     * Creates a new BList instance. To avoid drawing partial rows, the list height should be a multiple of the
     * rowHeight.
     * 
     * @param parent
     *            parent
     * @param x
     *            x
     * @param y
     *            y
     * @param width
     *            width
     * @param height
     *            height
     * @param rowHeight
     *            the height of each row contained in the list.
     */
    public Grid(BView parent, int x, int y, int width, int height, int rowHeight) {
        this(parent, x, y, width, height, rowHeight, true);
    }

    /**
     * Creates a new BList instance. To avoid drawing partial rows, the list height should be a multiple of the
     * rowHeight.
     * 
     * @param parent
     *            parent
     * @param x
     *            x
     * @param y
     *            y
     * @param width
     *            width
     * @param height
     *            height
     * @param rowHeight
     *            the height of each row contained in the list.
     * @param visible
     *            if true, make the view visibile
     */
    public Grid(BView parent, int x, int y, int width, int height, int rowHeight, boolean visible) {
        super(parent, x, y, width, height, rowHeight, visible);

        mMarker = new View(this, 0, 0, width / 3, rowHeight);
        mMarker.setResource(createImage("org/lnicholls/galleon/widget/marker.png")); // TODO Make configurable
    }

    /**
     * Create a row for the given element. For example, you could create a view with the given parent and then set its
     * resource to be a text resource based on the element at index.
     * 
     * @param parent
     *            use this as the parent for your new view
     * @param index
     *            the index for the row
     * @return the new row
     */
    protected void createRow(BView parent, int index) {
        for (int i = 0; i < 3; i++) {
            BView view = new BView(parent, i * parent.getWidth() / 3 + 3, 0, parent.getWidth() / 3 - 6, getRowHeight() - 6);
            createCell(view, index, i, index == getFocus());
        }
    }

    public abstract void createCell(BView parent, int row, int column, boolean selected);

    public boolean handleKeyPress(int code, long rawcode) {
        try {
            if (size() > 0) {
                List cells = (List) get(getFocus());
                int newColumn = mColumn;

                switch (code) {
                case KEY_RIGHT:
                    newColumn = Math.max(Math.min(mColumn + 1, cells.size() - 1), 0);
                    if (mColumn == newColumn) {
                        getBApp().play("bonk.snd");
                        getBApp().flush();
                        return false;
                    } else {
                        mColumn = newColumn;
                        getBApp().play("updown.snd");
                        getBApp().flush();
                        return true;
                    }
                case KEY_LEFT:
                    newColumn = Math.max(Math.min(mColumn - 1, cells.size() - 1), 0);
                    if (mColumn == newColumn) {
                        return false;
                    } else {
                        mColumn = newColumn;
                        getBApp().play("updown.snd");
                        getBApp().flush();
                        return true;
                    }
                case KEY_ADVANCE:
                    if (getFocus() == (size() - 1)) {
                        getBApp().play("pageup.snd");
                        getBApp().flush();
                        setFocus(0, false);
                    } else {
                        getBApp().play("pagedown.snd");
                        getBApp().flush();
                        setFocus(size() - 1, false);
                    }
                    return true;
                default:
                    return super.handleKeyPress(code, rawcode);
                }
            } else
                return super.handleKeyPress(code, rawcode);
        } finally {
            if (size() > 0) {
                List cells = (List) get(getFocus());
                mColumn = Math.max(Math.min(mColumn, cells.size() - 1), 0);
            }
            updateMarker();
        }
    }

    private void updateMarker() {
        BRect markerBounds = new BRect(mColumn * (getWidth() / 3), getFocus() * getRowHeight(), getWidth() / 3, getRowHeight());
        mMarker.setBounds(markerBounds.x, markerBounds.y, markerBounds.width, markerBounds.height);
    }

    public boolean handleKeyRepeat(int code, long rawcode) {
        switch (code) {
        case KEY_UP:
        case KEY_DOWN:
        case KEY_CHANNELUP:
        case KEY_CHANNELDOWN:
            return handleKeyPress(code, rawcode);
        }
        return super.handleKeyRepeat(code, rawcode);
    }

    public int getPos() {
        return getFocus() * 3 + mColumn;
    }

    public void setPos(int value) {
        setFocus(value / 3, false);
        mColumn = value % 3;
        updateMarker();
        refresh();
    }

    private int mColumn;

    private View mMarker;
}