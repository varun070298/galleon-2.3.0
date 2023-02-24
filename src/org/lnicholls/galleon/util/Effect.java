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

import org.lnicholls.galleon.widget.Callback;
import org.lnicholls.galleon.widget.DefaultApplication;

import com.tivo.hme.sdk.Application;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public abstract class Effect implements Callback {
	public abstract void apply(View view, Image image);

	public void wait(View view, Resource anim) {
		mCurrentView = view;

		HmeEvent evt = new HmeEvent.Key(mCurrentView.getApp().getID(), 0, Application.KEY_TIVO, mCurrentView.getID());
		mCurrentView.getApp().sendEvent(evt, anim);
		mCurrentView.getApp().flush();
		((DefaultApplication) mCurrentView.getApp()).addCallback(this);
		try {
			// System.gc();
			wait();
		} catch (Exception ex) {

		}
	}

	public synchronized boolean handleEvent(HmeEvent event) {
		if (mCurrentView != null) {
			switch (event.getOpCode()) {
			case Application.EVT_KEY: {
				HmeEvent.Key e = (HmeEvent.Key) event;
				switch (e.getCode()) {
				case Application.KEY_TIVO:
					int code = (int) e.getRawCode();
					if ((code) == mCurrentView.getID()) {
						if (mCurrentView.getResource() != null)
						{
							mCurrentView.getResource().flush();
							mCurrentView.getResource().remove();
						}
						Application app = mCurrentView.getApp();
						mCurrentView.flush();
						mCurrentView.remove();
						mCurrentView = null;
						app.flush();
						notifyAll();
						return true;
					}
				}
				break;
			}
			}
		}
		return false;
	}

	public int getDelay() {
		return mDelay;
	}

	public void setDelay(int value) {
		mDelay = value;
	}

	private int mDelay = 5000;

	private View mCurrentView;

}