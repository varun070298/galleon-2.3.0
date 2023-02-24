package org.lnicholls.galleon.apps.musicOrganizer;

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

public class MusicOrganizerConfiguration implements AppConfiguration {

    private static final ArrayList mDefaultCategories = new ArrayList(22);

    /*
     * Rough guide on the category syntax: Each category expression describes the format of the menu hierarchy. The ID3
     * tags of each MP3 file is categorized by using the expression syntax. Each expression consists of one or more
     * sections separated by \\ Each section consists of a combination of keywords that match ID3 tag names. The first
     * section is the top-most menu name. Each subsequent section is used as criteria to group the songs. Each section
     * syntax describes the format used to combine the tag information. Special tags such as {A-Z} are used to qualify
     * the syntax for operations such as alphabetical grouping. The section syntax format is used to label each menu
     * item.
     */

    static {
        mDefaultCategories.add("Rating\\Rating{Rating}\\Song [Artist]");
        //mDefaultCategories.add("Rating, Genre\\Rating{Rating}\\Genre\\Song [Artist]");
        //mDefaultCategories.add("Rating, Year\\Rating{Rating}\\Year\\Song [Artist]");
        //mDefaultCategories.add("Rating, Decade\\Rating{Rating}\\Year{Decade}\\Song [Artist]");
        mDefaultCategories.add("Play Count\\PlayCount{PlayCount}\\Song [Artist]");
        mDefaultCategories.add("Recently Played\\DatePlayed{RecentlyPlayed}\\Song [Artist]");
        mDefaultCategories.add("Album (Grouped)\\Album{ABC-XYZ}\\Album{A-Z}\\Album\\Song [Artist]");
        mDefaultCategories.add("Album\\Album [Artist]\\Track. Song");
        mDefaultCategories.add("Artists\\Artist\\Song [Album]");
        mDefaultCategories.add("Artist\\Artist{A-Z}\\Artist\\Song [Album]");
        mDefaultCategories.add("Artist (Grouped)\\Artist{ABC-XYZ}\\Artist{A-Z}\\Artist\\Song [Album]");
        mDefaultCategories.add("Artist, Album\\Artist{A-Z}\\Artist\\Album\\Track. Song");
        mDefaultCategories.add("Artist, Year\\Artist{A-Z}\\Artist\\Year\\Song [Album]");
        mDefaultCategories.add("Artist (Grouped), Album\\Artist{ABC-XYZ}\\Artist{A-Z}\\Artist\\Album [Year]\\Track. Song");
        mDefaultCategories.add("Artist (Grouped), Year\\Artist{ABC-XYZ}\\Artist{A-Z}\\Artist\\Year\\Song [Album]");
        mDefaultCategories.add("Bitrate\\Bitrate{0-9}\\Song [Artist]");
        mDefaultCategories.add("Decade\\Year{Decade}\\Song [Artist]");
        mDefaultCategories.add("Decade, Artist\\Year{Decade}\\Artist\\Song [Album]");
        mDefaultCategories.add("Decade, Artist (Grouped)\\Year{Decade}\\Artist{ABC-XYZ}\\Artist{A-Z}\\Song [Artist]");
        mDefaultCategories.add("Decade, Genre\\Year{Decade}\\Genre\\Song [Artist]");
        mDefaultCategories.add("Duration\\Duration{Duration}\\Song [Artist]");
        mDefaultCategories.add("Genre\\Genre\\Song [Artist]");
        mDefaultCategories.add("Genre, Album\\Genre\\Album\\Song [Artist]");
        mDefaultCategories.add("Genre, Artist\\Genre\\Artist{A-Z}\\Artist\\Song [Album]");
        mDefaultCategories.add("Genre, Artist (Grouped)\\Genre\\Artist{ABC-XYZ}\\Artist{A-Z}\\Artist\\Song [Album]");
        mDefaultCategories.add("Genre, Artist, Album\\Genre\\Artist\\Album\\Track. Song");
        mDefaultCategories.add("Genre, Artist (Grouped), Album\\Genre\\Artist{ABC-XYZ}\\Artist{A-Z}\\Artist\\Album\\Track. Song");
        mDefaultCategories.add("Genre, Decade\\Genre\\Year{Decade}\\Song [Artist]");
        mDefaultCategories.add("Song\\Song{A-Z}\\Song [Artist]");
        mDefaultCategories.add("Song (Grouped)\\Song{ABC-XYZ}\\Song{A-Z}\\Song [Artist]");
        mDefaultCategories.add("Year\\Year{0-9}\\Song [Artist]");
        mDefaultCategories.add("Year, Genre\\Year{0-9}\\Genre\\Song [Artist]");
    }

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        if (mName != null && !mName.equals(value))
            mModified = true;
        mName = value;
    }

    public List getPaths() {
        return mPaths;
    }

    public void setPaths(List value) {
        mModified = true;
        mPaths = value;
    }

    public void addPath(String value) {
        mModified = true;
        mPaths.add(value);
    }

    public void setModified(boolean value) {
        mModified = value;
    }

    public boolean isModified() {
        return mModified;
    }

    public List getGroups() {
        return mGroups;
    }

    public void setGroups(List value) {
        mGroups = value;
    }

    public void addGroup(String value) {
        mGroups.add(value);
    }

    public ArrayList getDefaultCategories() {
        return mDefaultCategories;
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

    private boolean mModified;

    private List mPaths = new ArrayList();

    private List mGroups = new ArrayList();
}