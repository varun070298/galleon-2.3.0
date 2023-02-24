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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.lnicholls.galleon.util.NameValue;

public class GoBackConfiguration implements Serializable {

	public GoBackConfiguration() {
	}

	public void setEnabled(boolean value) {
		if (mEnabled != value)
			mModified = true;
		mEnabled = value;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setPublishTiVoRecordings(boolean value) {
		if (mPublishTiVoRecordings != value)
			mModified = true;
		mPublishTiVoRecordings = value;
	}

	public boolean isPublishTiVoRecordings() {
		return mPublishTiVoRecordings;
	}	public void setConvertVideo(boolean value) {
		if (mConvertVideo != value)
			mModified = true;
		mConvertVideo = value;
	}

	public boolean isConvertVideo() {
		return mConvertVideo;
	}

	public String getConversionTool() {
		return mConversionTool;
	}

	public void setConversionTool(String value) {
		if (mConversionTool == null)
			mModified = true;
		else if (!mConversionTool.equals(value))
			mModified = true;
		mConversionTool = value;
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

	public void setModified(boolean value) {
		mModified = value;
	}

	public boolean isModified() {
		return mModified;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}	public void setGroupByShow(boolean value) {		if (mGroupByShow != value)			mModified = true;		mGroupByShow = value;	}	public boolean isGroupByShow() {		return mGroupByShow;	}
	private boolean mEnabled = true;

	private boolean mPublishTiVoRecordings = true;	private boolean mGroupByShow = false;	private boolean mConvertVideo = true;

	private String mConversionTool;

	private List mPaths = new ArrayList();

	private boolean mModified;
}