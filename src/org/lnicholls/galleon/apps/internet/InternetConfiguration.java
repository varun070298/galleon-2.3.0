package org.lnicholls.galleon.apps.internet;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.SharedNameValue;

public class InternetConfiguration implements AppConfiguration {

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        if (mName != null && !mName.equals(value))
            mModified = true;
        mName = value;
    }

    public int getReload() {
        return mReload;
    }

    public void setReload(int value) {
        if (mReload != value)
            mModified = true;
        mReload = value;
    }

    public List getUrls() {
        return null;
    }

    public void setUrls(List value) {
    	mSharedUrls.clear();
    	Iterator iterator = value.iterator();
        while (iterator.hasNext())
        {
        	NameValue nameValue = (NameValue)iterator.next();
        	SharedUrl sharedUrl = new SharedUrl(nameValue.getName(), nameValue.getValue(), "", "", SharedUrl.PRIVATE);
        	mSharedUrls.add(sharedUrl);
        }
    }

    public void addUrl(NameValue value) {
    	SharedUrl sharedUrl = new SharedUrl(value.getName(), value.getValue(), "", "", SharedUrl.PRIVATE);
    	mSharedUrls.add(sharedUrl);
    }
    
    public List getSharedUrls() {
        return mSharedUrls;
    }

    public void setSharedUrls(List value) {
        mSharedUrls = value;
    }

    public void addSharedUrl(SharedUrl value) {
        mSharedUrls.add(value);
    }

    public void setModified(boolean value) {
        mModified = value;
    }

    public boolean isModified() {
        return mModified;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public static class SharedUrl extends SharedNameValue implements Serializable {
    	public SharedUrl()
    	{
    		
    	}
    	
    	public SharedUrl(String name, String value, String description, String tags, String privacy) {
    		super(name, value, description, tags, privacy);
    	}
    }
    
    public boolean isShared()
    {
    	return mShared;
    }
    
    public void setShared(boolean value)
    {
    	mShared = value;
    }
    
    public boolean isSorted()
    {
    	return mSorted;
    }
    
    public void setSorted(boolean value)
    {
    	mSorted = value;
    }        
    
    private boolean mShared;    
    
    private boolean mSorted;

    private String mName;

    private int mReload;

    private boolean mModified;

    private List mSharedUrls = new ArrayList();
}