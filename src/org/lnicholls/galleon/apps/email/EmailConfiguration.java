package org.lnicholls.galleon.apps.email;

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
import org.lnicholls.galleon.app.AppConfiguration;

public class EmailConfiguration implements AppConfiguration {

	public String getName() {
		return mName;
	}

	public void setName(String value) {
		if (mName != null && !mName.equals(value))
			mModified = true;
		mName = value;
	}

	public List getAccounts() {
		return mAccounts;
	}

	public void setAccounts(List value) {
		mAccounts = value;
	}

	public void addAccount(Account value) {
		mAccounts.add(value);
	}

	public int getReload() {
		return mReload;
	}

	public void setReload(int value) {
		mReload = value;
	}

	public int getLimit() {
		return mLimit;
	}

	public void setLimit(int value) {
		mLimit = value;
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

	public static class Account implements Serializable {
		public Account() {
		}

		public String getName() {
			return mName;
		}

		public void setName(String value) {
			mName = value;
		}

		public String getProtocol() {
			return mProtocol;
		}

		public void setProtocol(String value) {
			mProtocol = value;
		}

		public String getServer() {
			return mServer;
		}

		public void setServer(String value) {
			mServer = value;
		}

		public String getUsername() {
			return mUsername;
		}

		public void setUsername(String value) {
			mUsername = value;
		}

		public String getPassword() {
			return mPassword;
		}

		public void setPassword(String value) {
			mPassword = value;
		}

		public boolean valid() {
			return mValid;
		}

		public void setValid(boolean value) {
			mValid = value;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		private String mName;

		private String mProtocol;

		private String mServer;

		private String mUsername;

		private String mPassword;

		private boolean mValid = true;
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

	private int mReload;

	private int mLimit;

	private List mAccounts = new ArrayList();

	private boolean mModified;
}