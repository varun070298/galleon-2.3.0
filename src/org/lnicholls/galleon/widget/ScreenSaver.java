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

import org.lnicholls.galleon.util.Tools;

import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public class ScreenSaver extends Thread {
    public ScreenSaver(DefaultScreen defaultScreen) {
        mDefaultScreen = defaultScreen;
    }

    public void run() {
        while (mDefaultScreen.getApp().getContext() != null) {
            try {
                sleep(1000 * 5 * 60);
                synchronized (this) {
                	((DefaultApplication)mDefaultScreen.getApp()).setHandleTimeout(true);
                	if (mShades == null) {
                        Resource resource = mDefaultScreen.getResource("*30000");
                        mShades = new View(mDefaultScreen, 0, 0, mDefaultScreen.getWidth(), mDefaultScreen.getHeight());
                        mShades.setResource(Color.BLACK);
                        mShades.setTransparency(1.0f);
                        mShades.setTransparency(0.15f, resource);
                        mDefaultScreen.flush();
                    }
                }
            } catch (InterruptedException ex) {
                return;
            } catch (Exception ex2) {
                Tools.logException(ScreenSaver.class, ex2);
                break;
            }
        }
    	if (mDefaultScreen.getApp().getContext()!=null)
    		((DefaultApplication)mDefaultScreen.getApp()).setHandleTimeout(false);
    }

    public void interrupt() {
        synchronized (this) {
            super.interrupt();
        }

        ((DefaultApplication)mDefaultScreen.getApp()).setHandleTimeout(false);
    }

    public void restore() {
        if (mShades != null) {
            mShades.setVisible(false);
            mShades.flush();            mShades.remove();
            mShades = null;
            ((DefaultApplication)mDefaultScreen.getApp()).setHandleTimeout(false);
            mDefaultScreen.flush();
        }
    }

    public boolean handleKeyPress(int code, long rawcode) {
        if (code != DefaultScreen.KEY_VOLUMEDOWN && code != DefaultScreen.KEY_VOLUMEUP) {
            restore();
        }
        return false;
    }

    private DefaultScreen mDefaultScreen;

    private View mShades;
}