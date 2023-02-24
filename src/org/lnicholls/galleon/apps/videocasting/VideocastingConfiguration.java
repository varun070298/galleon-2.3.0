package org.lnicholls.galleon.apps.videocasting;

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

public class VideocastingConfiguration implements AppConfiguration {

	public static int ALL = -1;

	public String getName() {
		return mName;
	}

	public void setName(String value) {
		if (mName != null && !mName.equals(value))
			mModified = true;
		mName = value;
	}

	public int getDownload() {
		return mDownload;
	}

	public void setDownload(int value) {
		if (mDownload != value)
			mModified = true;
		mDownload = value;
	}

	public List getDirectorys() {
		if (mDirectories == null) {
			mDirectories = new ArrayList();
			mDirectories.add(new NameValue("FireAnt Directory", "http://api.antisnottv.net/feeds"));
			mDirectories.add(new NameValue("blip.tv", "http://blip.tv/?1=1&&skin=rss"));
			mDirectories.add(new NameValue("GlitchTV", "http://blip.tv/posts/?skin=rss&topic_name=glitchtv"));
			mDirectories.add(new NameValue("Freelog.org Tutorials", "http://feeds.feedburner.com/freevlog/tutorials"));
			mDirectories.add(new NameValue("Freelog.org Classic", "http://feeds.feedburner.com/freevlog/classics"));
			mDirectories.add(new NameValue("Freelog.org Classic Videoblogs", "http://feeds.feedburner.com/freevlog/classics"));
			mDirectories.add(new NameValue("Freelog.org New Vlogs", "http://feeds.feedburner.com/freevlog/newvloggers"));
			mDirectories.add(new NameValue("Mefeedia.com Videoblog", "http://mefeedia.com/feeds/opml.xml"));
			mDirectories.add(new NameValue("Ourmedia.org Recent Videos", "http://www.ourmedia.org/mediarss/videomedia"));
			mDirectories.add(new NameValue("MedicineFilms.com", "http://feeds.feedburner.com/medicinefeed"));
			mDirectories.add(new NameValue("Xolo.TV", "http://feeds.feedburner.com/xolotv"));
			mDirectories.add(new NameValue("MobuzzTV", "http://feeds.feedburner.com/mobuzztv-uk-wmv"));
			//http://www.vodcast.nl/feeds/jsdemo.xml
		}
		return mDirectories;
	}

	public void setDirectorys(List value) {
		mModified = true;
		mDirectories = value;
	}

	public void addDirectory(NameValue nameValue) {
		mModified = true;
		mDirectories.add(nameValue);
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

	private int mDownload = 1;

	private boolean mModified;

	private List mDirectories;
}