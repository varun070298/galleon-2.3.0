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

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BView;

public class DefaultList extends BList {

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
    public DefaultList(BView parent, int x, int y, int width, int height, int rowHeight) {
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
    public DefaultList(BView parent, int x, int y, int width, int height, int rowHeight, boolean visible) {
        super(parent, x, y, width, height, rowHeight, visible);
    }

    protected void createRow(BView parent, int index) {

    }

    public boolean handleKeyPress(int code, long rawcode) {
        switch (code) {
        case KEY_ADVANCE:
            if (getFocus()==(size()-1))
            {
                getBApp().play("pageup.snd");
                getBApp().flush();
                setFocus(0,false);
            }
            else
            {
                getBApp().play("pagedown.snd");
                getBApp().flush();
                setFocus(size()-1,false);
            }
            return true;
        }
        return super.handleKeyPress(code, rawcode);
    }

    public void clearViews() {
        for (int i = 0; i < getRows().size(); i++) {
            BView view = (BView) getRows().get(i);
            if (view != null)            {            	view.flush();
                view.remove();            }
            getRows().setElementAt(null, i);
        }
    }

    public void init() {
        refresh();
        setFocus(getFocus(),false);
    }

    public void flash()
    {
    	if (getFocus()!=-1)
    	{
    		BView row = getRow(getFocus());
    		if (row.getChildCount()>0)
    		{
    			BView icon = (BView) row.getChild(0);
    			if (icon!=null)
    			{
    				icon.setTransparency(1.0f);
    				icon.setTransparency(0.0f, getResource("*500"));
    				icon.flush();
    			}
    		}
    	}
    }
}