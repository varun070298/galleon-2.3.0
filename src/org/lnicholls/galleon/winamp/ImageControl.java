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

public class ImageControl extends View {

    public ImageControl(View parent, int x, int y, int width, int height, Image selectedImage, Image unselectedImage) {
        super(parent, x, y, width, height);
        mSelectedImage = selectedImage;
        mUnselectedImage = unselectedImage;
        setResource(mUnselectedImage);
    }

    public void setSelected(boolean selected) {
        if (selected != mSelected) {
            mSelected = selected;
            clearResource();
            if (mSelected)
                setResource(createImage(mSelectedImage));
            else
                setResource(createImage(mUnselectedImage));
        }
    }

    public boolean getSelected() {
        return mSelected;
    }

    private boolean mSelected;

    private Image mSelectedImage;

    private Image mUnselectedImage;
}