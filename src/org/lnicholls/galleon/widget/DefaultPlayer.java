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

import org.lnicholls.galleon.widget.DefaultApplication.ApplicationEvent;
import org.lnicholls.galleon.widget.DefaultApplication.ApplicationEventListener;

import com.tivo.hme.bananas.BView;

public abstract class DefaultPlayer extends BView implements ApplicationEventListener {

    public DefaultPlayer(DefaultScreen parent, int x, int y, int width, int height, boolean visible) {
        super(parent, x, y, width, height, visible);
    }
    
    public boolean handleFocus(boolean isGained, BView gained, BView lost)
    {
        /*
        System.out.println("handleFocus: isGained="+isGained);
        if (isGained)
            DefaultApplication.addApplicationEventListener(this);
        else
            DefaultApplication.removeApplicationEventListener(this);
            */
        
        return super.handleFocus(isGained, gained, lost);
    }

    public abstract void updatePlayer();

    public abstract void stopPlayer();

    public void handleApplicationEvent(ApplicationEvent appEvent) {
        //System.out.println("handleApplicationEvent");
    }
    
    public boolean handleAction(BView view, java.lang.Object action)
    {
        //System.out.println("handleAction: "+action);
        return super.handleAction(view, action);
    }
    
    public boolean handleEvent(com.tivo.hme.sdk.HmeEvent event)
    {
        //System.out.println("handleEvent: "+event);
        return super.handleEvent(event);
    }
}