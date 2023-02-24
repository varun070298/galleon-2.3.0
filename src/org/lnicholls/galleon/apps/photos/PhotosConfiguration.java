package org.lnicholls.galleon.apps.photos;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.util.NameValue;

public class PhotosConfiguration implements AppConfiguration {

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        if (mName != null && !mName.equals(value))
            mModified = true;
        mName = value;
    }
    
    public String getEffect() {
        return mEffect;
    }

    public void setEffect(String value) {
        if (mEffect!=null && !mEffect.equals(value))
            mModified = true;
        mEffect = value;
    }    

    public List getPaths() {
        return mPaths;
    }

    public void setPaths(List value) {
        mModified = true;
        mPaths = value;
    }

    public void addPath(NameValue nameValue) {
        mModified = true;
        mPaths.add(nameValue);
    }
    
    public void setDisplayTime(int value) {
        mDisplayTime = value;
    }

    public int getDisplayTime() {
        return mDisplayTime;
    }    
    
    public void setTransitionTime(int value) {
        mTransitionTime = value;
    }

    public int getTransitionTime() {
        return mTransitionTime;
    }
    
    public void setUseSafe(boolean value) {
        mUseSafe = value;
    }

    public boolean isUseSafe() {
        return mUseSafe;
    }    

    public void setModified(boolean value) {
        mModified = value;
    }

    public boolean isModified() {
        return mModified;
    }
    
    public void setRandomPlayFolders(boolean value) {
        mRandomPlayFolders = value;
    }

    public boolean isRandomPlayFolders() {
        return mRandomPlayFolders;
    }    

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public boolean isShared()
    {
    	return mShared;
    }
    
    public void setShared(boolean value)
    {
    	mShared = value;
    }
    
    private boolean mShared;    

    private String mName;
    
    private String mEffect = "Random";
    
    private int mDisplayTime = 5;  // seconds
    
    private int mTransitionTime = 2; // seconds
    
    private boolean mUseSafe;

    private boolean mModified;

    private List mPaths = new ArrayList();
    
    private boolean mRandomPlayFolders = true;
}