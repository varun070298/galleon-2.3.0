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

public class DataConfiguration implements Serializable {

	public DataConfiguration() {
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String value) {
		if (mUsername == null)
			mModified = true;
		else if (!mUsername.equals(value))
			mModified = true;
		mUsername = value;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String value) {
		if (mPassword == null)
			mModified = true;
		else if (!mPassword.equals(value))
			mModified = true;
		mPassword = value;
	}
	
	public boolean isConfigured() {
		return mPassword!=null && mUsername!=null & mUsername.trim().length()>0 && mPassword.trim().length()>0;
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

	private String mUsername;

	private String mPassword;

	private boolean mModified;
}