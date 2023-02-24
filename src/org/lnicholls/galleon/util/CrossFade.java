package org.lnicholls.galleon.util;

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

import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public class CrossFade extends Effect {
    public void apply(View view, Image image) {
        View view2 = new View(view.getParent(), view.getX(), view.getY(), view.getWidth(), view.getHeight());
        view2.setResource(view.getResource());
        view.setResource(view.createImage(image));
        image.flush(); 
        image = null;

        Resource anim = view.getResource("*" + getDelay());
        view2.setTransparency(1.0f, anim);
        view.setTransparency(1.0f);
        view.setTransparency(0.0f, anim);
        
        wait(view2, anim);
    }
}