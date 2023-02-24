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

import org.lnicholls.galleon.util.Tools;

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BHighlights;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;

public class DefaultMenuScreen extends DefaultScreen {

	public DefaultMenuScreen(DefaultApplication app, String title) {
		super(app);
		setTitle(title);

		mMenuList = createMenuList();
		BHighlights h = mMenuList.getHighlights();
		h.setPageHint(H_PAGEUP, A_RIGHT + 13, A_TOP - 25);
		h.setPageHint(H_PAGEDOWN, A_RIGHT + 13, A_BOTTOM + 30);

		setFocusDefault(mMenuList);
	}

	public DefaultList createMenuList() {
		return new MenuList(this, SAFE_TITLE_H + 10, (getHeight() - SAFE_TITLE_V) - 290, getWidth()
				- ((SAFE_TITLE_H * 2) + 32), 280, 35);
	}

	public DefaultList getMenuList() {
		return mMenuList;
	}

	public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
		mMenuList.init();
		if (mFocus > 0)
			mMenuList.setFocus(mFocus, false);

		/*
		 * mAnimationThread = new Thread(){ public void run() { int ease = 1; //
		 * The current ease value int animTime = 10000; // Animation time try {
		 * boolean parity = false; while (getContext() != null) { parity =
		 * !parity;
		 * 
		 * Resource anim = getResource("*" + animTime + "," + ((float)ease /
		 * 100f));
		 * 
		 * mTitleAnimation.setLocation(-mTitleAnimation.getWidth(),0);
		 * mTitleAnimation.setTransparency(0.7f);
		 * mTitleAnimation.setVisible(true);
		 * 
		 * mTitleAnimation.setLocation(getWidth(),0, anim);
		 * mTitleAnimation.setTransparency(1.0f, anim); mTitleAnimation.flush();
		 * 
		 * try { synchronized (this) { wait(animTime); } } catch
		 * (InterruptedException e) { return; } } } catch (Exception ex) {
		 * Tools.logException(DefaultMenuScreen.class, ex); } }
		 * 
		 * public void interrupt() { synchronized (this) { super.interrupt(); } } };
		 * mAnimationThread.start();
		 */

		return super.handleEnter(arg, isReturn);
	}

	public boolean handleExit() {
		if (mAnimationThread != null && mAnimationThread.isAlive()) {
			mAnimationThread.interrupt();
			mAnimationThread = null;
		}

		mFocus = mMenuList.getFocus();
		mMenuList.clearViews();
		return super.handleExit();
	}

	public void load() {
		if (mMenuList.getFocus() != -1) {
			BView row = mMenuList.getRow(mMenuList.getFocus());
			BView icon = (BView) row.getChild(0);
			mRowResource = icon.getResource();
			icon.setResource(((DefaultApplication) getApp()).getBusyIcon());
			icon.flush();

			getBApp().play("select.snd");
			getBApp().flush();
		}
	}

	public void unload() {
		if (mMenuList.getFocus() != -1) {
			BView row = mMenuList.getRow(mMenuList.getFocus());
			BView icon = (BView) row.getChild(0);
			icon.setResource(mRowResource);
			icon.flush();

			getBApp().play("bonk.snd");
			getBApp().flush();
		}
	}

	protected void createRow(BView parent, int index) {

	}

	public class MenuList extends DefaultList {
		public MenuList(DefaultMenuScreen defaultMenuScreen, int x, int y, int width, int height, int rowHeight) {
			super(defaultMenuScreen.getNormal(), x, y, width, height, rowHeight);
			setBarAndArrows(BAR_HANG, BAR_DEFAULT, null, "push");
			mDefaultMenuScreen = defaultMenuScreen;
		}

		protected void createRow(BView parent, int index) {
			try {
				mDefaultMenuScreen.createRow(parent, index);
			} catch (Throwable ex) {
				Tools.logException(DefaultMenuScreen.class, ex, "Could not disconnect jabber");
			}
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_PLAY:
				postEvent(new BEvent.Action(this, "play"));
				return true;
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "push"));
				return true;
			case KEY_CHANNELUP:
			case KEY_CHANNELDOWN:
				boolean result = super.handleKeyPress(code, rawcode);
				if (!result) {
					getBApp().play("bonk.snd");
					getBApp().flush();
				}
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		DefaultMenuScreen mDefaultMenuScreen;
	}

	protected DefaultList mMenuList;

	protected int mFocus;

	private Resource mRowResource;

	private Thread mAnimationThread;
}