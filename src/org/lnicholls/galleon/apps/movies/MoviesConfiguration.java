package org.lnicholls.galleon.apps.movies;

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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.lnicholls.galleon.app.AppConfiguration;

public class MoviesConfiguration implements AppConfiguration {

	public String getId() {
		return mId;
	}

	public void setId(String value) {
		if (mId != null && !mId.equals(value))
			mModified = true;
		mId = value;
	}

	public String getName() {
		return mName;
	}

	public void setName(String value) {
		if (mName != null && !mName.equals(value))
			mModified = true;
		mName = value;
	}

	public String getCity() {
		return mCity;
	}

	public void setCity(String value) {
		if (mCity != null && !mCity.equals(value))
			mModified = true;
		mCity = value;
	}

	public String getState() {
		return mState;
	}

	public void setState(String value) {
		if (mState != null && !mState.equals(value))
			mModified = true;
		mState = value;
	}

	public String getZip() {
		return mZip;
	}

	public void setZip(String value) {
		if (mZip != null && !mZip.equals(value))
			mModified = true;
		mZip = value;
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

	private String mId;

	private String mName;

	private String mCity;

	private String mState;

	private String mZip;

	private boolean mModified;
}