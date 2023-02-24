package org.lnicholls.galleon.server;

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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class MusicPlayerConfiguration implements Serializable {
    
    public static String CLASSIC = "classic";
    
    public static String WINAMP = "winamp";

    public MusicPlayerConfiguration() {

    }

    public String getSkin() {
        return mSkin;
    }

    public void setSkin(String value) {
    	if (mSkin == null)
    		mModified = true;
    	else
    	if (mSkin != null && !mSkin.equals(value))
            mModified = true;
        mSkin = value;
    }

    public void setUseFile(boolean value) {
    	if (mUseFile!=value)
            mModified = true;
        mUseFile = value;
    }

    public boolean isUseFile() {
        return mUseFile;
    }

    public void setUseAmazon(boolean value) {
    	if (mUseAmazon!=value)
            mModified = true;
        mUseAmazon = value;
    }

    public boolean isUseAmazon() {
        return mUseAmazon;
    }

    public void setShowImages(boolean value) {
    	if (mShowImages!=value)
            mModified = true;
        mShowImages = value;
    }

    public boolean isShowImages() {
        return mShowImages;
    }
    
    public void setScreensaver(boolean value) {
    	if (mScreensaver!=value)
            mModified = true;
        mScreensaver = value;
    }

    public boolean isScreensaver() {
        return mScreensaver;
    }

    public void setModified(boolean value) {
        mModified = value;
    }

    public boolean isModified() {
        return mModified;
    }
    
    public String getPlayer() {
        return mPlayer;
    }

    public void setPlayer(String value) {
    	if (mPlayer == null)
    		mModified = true;
    	else
    	if (!mPlayer.equals(value))
            mModified = true;
        mPlayer = value;
    }
    
    public void setRandomPlayFolders(boolean value) {
    	if (mRandomPlayFolders!=value)
            mModified = true;
        mRandomPlayFolders = value;
    }

    public boolean isRandomPlayFolders() {
        return mRandomPlayFolders;
    }    

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private String mSkin;

    private boolean mUseFile = true;

    private boolean mUseAmazon = true;

    private boolean mShowImages = true;
    
    private boolean mScreensaver = true;
    
    private String mPlayer = CLASSIC;
    
    private boolean mRandomPlayFolders = true;

    private boolean mModified;
}