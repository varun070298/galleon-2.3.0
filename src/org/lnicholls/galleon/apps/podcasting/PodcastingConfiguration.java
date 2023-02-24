package org.lnicholls.galleon.apps.podcasting;

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

public class PodcastingConfiguration implements AppConfiguration {

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
			mDirectories.add(new NameValue("iPodder.org", "http://www.ipodder.org/discuss/reader$4.opml"));
			/*
			mDirectories.add(new NameValue("iPodderX Top Picks",
					"http://directory.ipodderx.com/opml/iPodderX_Picks.opml"));
			mDirectories.add(new NameValue("iPodderX Most Popular",
					"http://directory.ipodderx.com/opml/iPodderX_Popular.opml"));
					*/
			//mDirectories.add(new NameValue("Podfeed.net", "http://www.podfeed.net/opml/directory.opml"));
			mDirectories
					.add(new NameValue("Podcast Alley Top 50", "http://www.podcastalley.com/PodcastAlleyTop50.opml"));
			mDirectories.add(new NameValue("Podcast Alley 10 Newest",
					"http://www.podcastalley.com/PodcastAlley10Newest.opml"));
			mDirectories.add(new NameValue("PodcastDirectory.com", "http://www.podcastdirectory.com/popular/pmon.opml"));
			mDirectories.add(new NameValue("DigitalPodcast.com", "http://www.digitalpodcast.com/opml/digitalpodcast.opml"));
			mDirectories.add(new NameValue("Podfeeder.com", "http://podfeeder.com/allshows.opml"));
			mDirectories.add(new NameValue("TechPodcasts.com", "http://www.techpodcasts.com/dir/tech.opml"));
			mDirectories.add(new NameValue("GigaDial 25 Latest", "http://www.gigadial.net/public/opml/dial25.opml"));
			mDirectories.add(new NameValue("GigaDial All", "http://www.gigadial.net/public/opml/dial.opml"));
			mDirectories.add(new NameValue("Sports Podcast Network", "http://sportspodnet.com/opml/spn.opml"));
			mDirectories.add(new NameValue("Sportspod", "http://thesportspod.com/opml/sports.opml"));
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