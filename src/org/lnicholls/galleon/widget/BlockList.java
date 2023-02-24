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

import com.tivo.hme.sdk.*;

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.*;

public class BlockList extends DefaultList {

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
    public BlockList(BView parent, int x, int y, int width, int height, int rowHeight) {
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
    public BlockList(BView parent, int x, int y, int width, int height, int rowHeight, boolean visible) {
        super(parent, x, y, width, height, rowHeight, visible);

        getRowHighlights().set(new CustomHighlight("Custom", "push", 0, 0, width, rowHeight));
    }

    protected void createRow(BView parent, int index) {
    }

    public BHighlights getRowHighlights()
    {
        if (mHighlights==null)
            this.mHighlights = new BHighlights(new Layout());
        return mHighlights;
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

    //
    // Highlights layout for the rows
    //

    class Layout implements IHighlightsLayout
    {
        public BScreen getScreen()
        {
            return BlockList.this.getScreen();
        }

        public BRect getHighlightBounds()
        {
            BView row = getRow();
            if (row != null) {
                return row.getHighlightBounds();
            }
            return toScreenBounds(new BRect(0, 0, getWidth(), getRowHeight()));
        }

        public boolean getHighlightIsVisible(int visible)
        {
            BView row = getRow();
            return (row != null) ? row.getHighlightIsVisible(visible) : false;
        }

        protected BView getRow()
        {
            return (getFocus() != -1) ? (BView)getRows().elementAt(getFocus()) : null;
        }
    }

    class CustomHighlight extends BHighlight
    {
    	private final static String ANIM = "*500";

    	public CustomHighlight(String name, Object action, int dx, int dy, int width, int height)
    	{
    		super(name, action, dx, dy);
    		mWidth = width;
    		mHeight = height;
    		mAnim = getResource(ANIM);
    	}

    	public void refresh(BHighlights h, BScreen s, BRect r, Resource animation)
        {
            int x = r.x + getDx();
            int y = r.y + getDy();

            if (h.getLayout().getHighlightIsVisible(getVisible())) {
                if (getView() == null) {
                    //BView parent = isAbove() ? s.getAbove() : s.getBelow();
                	BView parent = s.getAbove();
                	BView view = new BView(parent, x, y, mWidth, mHeight);
                	Color color = Color.BLUE;
                	BView top = new BView(view, 0, 0, mWidth, 10);
                	top.setResource(color);
                	BView bottom = new BView(view, 0, mHeight-10, mWidth, 10);
                	bottom.setResource(color);
                	BView left = new BView(view, 0, 10, 10, mHeight-20);
                	left.setResource(color);
                	BView right = new BView(view, mWidth-10, 10, 10, mHeight-20);
                	right.setResource(color);
                    setView(view);
                } else {
                	//getView().setTransparency(1.0f);
                	getView().setVisible(true);
                	//getView().setTransparency(0.0f, mAnim);
                	getView().setTransparency(0.0f);
                	getView().setLocation(x, y, animation);
                    if (getWidth() != -1 && getWidth() != getView().getWidth()) {
                    	getView().setSize(getWidth(), getView().getHeight(), animation);
                    }
                }
            } else if (getView() != null) {
            	getView().setVisible(false, animation);
            	getView().setLocation(x, y, animation);
            }
        }

    	private int mWidth;
    	private int mHeight;
    }

    private BHighlights mHighlights;

    private Resource mAnim;
}