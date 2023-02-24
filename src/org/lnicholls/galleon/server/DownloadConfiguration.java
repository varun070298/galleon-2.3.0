package org.lnicholls.galleon.server;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

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

public class DownloadConfiguration implements Serializable {

	public DownloadConfiguration() {
	}

	public int getCPU() {
		return mCPU;
	}

	public void setCPU(int value) {
		mModified = true;
		mCPU = value;
	}

	public int getBandwidth() {
		return mBandwidth;
	}

	public void setBandwidth(int value) {
		mModified = true;
		mBandwidth = value;
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

	private int mCPU = 1;

	private int mBandwidth = 1;

	private boolean mModified;
}