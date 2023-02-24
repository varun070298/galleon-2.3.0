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

public class ImageView extends View {

    public ImageView(View parent, int x, int y, Image[] images) {
        super(parent, x, y, images[0].getWidth(null), images[0].getHeight(null));
        mViews = new View[images.length];
        for (int i = 0; i < images.length; i++) {
            mViews[i] = new View(this, 0, 0, getWidth(), getHeight());
            mViews[i].setVisible(false);
            //mViews[i].setResource(createImage(Util.cropImage(images[i], 0, 0, width, height)));
            mViews[i].setResource(createImage(images[i]));
        }
    }

    public void setImage(int image) {
        if (image >= 0 && image < mViews.length && mImage != image) {
            if (mImage != -1)
                mViews[mImage].setVisible(false);
            mImage = image;
            mViews[mImage].setVisible(true);
        }
    }

    public int getImage() {
        return mImage;
    }

    private int mImage = -1;

    private View[] mViews;
}