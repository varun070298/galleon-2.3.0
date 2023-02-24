package org.lnicholls.galleon.apps.rss;

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
import java.util.*;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.util.*;

public class RSSConfiguration implements AppConfiguration {

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        if (mName != null && !mName.equals(value))
            mModified = true;
        mName = value;
    }

    public List getFeeds() {
        return null;
    }

    public void setFeeds(List value) {
    	mSharedFeeds.clear();
    	Iterator iterator = value.iterator();
        while (iterator.hasNext())
        {
        	NameValue nameValue = (NameValue)iterator.next();
        	SharedFeed sharedFeed = new SharedFeed(nameValue.getName(), nameValue.getValue(), "", "", SharedFeed.PRIVATE);
        	mSharedFeeds.add(sharedFeed);
        }
    }

    public void addFeed(NameValue value) {
    	SharedFeed sharedFeed = new SharedFeed(value.getName(), value.getValue(), "", "", SharedFeed.PRIVATE);
    	mSharedFeeds.add(sharedFeed);
    }
    
    public List getSharedFeeds() {
        return mSharedFeeds;
    }

    public void setSharedFeeds(List value) {
        mSharedFeeds = value;
    }

    public void addSharedFeed(SharedFeed value) {
        mSharedFeeds.add(value);
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
    
    public static class SharedFeed extends SharedNameValue implements Serializable {
    	public SharedFeed()
    	{
    		
    	}
    	
    	public SharedFeed(String name, String value, String description, String tags, String privacy) {
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

    private List mSharedFeeds = new ArrayList();

    private boolean mModified;
}