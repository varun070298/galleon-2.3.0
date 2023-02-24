package org.lnicholls.galleon.app;

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
import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;

public class AppContext implements Serializable {
	private static Logger log = Logger.getLogger(AppContext.class.getName());

	public AppContext(AppDescriptor appDescriptor) {
		mId = ++mCounter;
		mAppDescriptor = appDescriptor;

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try {
			Class configuration = classLoader.loadClass(appDescriptor.getConfiguration());
			mAppConfiguration = configuration.newInstance();
		} catch (Exception ex) {
			Tools.logException(AppContext.class, ex, "Could not create app configuration");
			mAppConfiguration = new AppConfiguration() {
				public String getName() {
					return mName;
				}

				public void setName(String value) {
					mName = value;
				}

				public boolean isShared() {
					return mShared;
				}

				public void setShared(boolean value) {
					mShared = value;
				}

				private boolean mShared;

				private String mName;
			};
		}
	}

	public void setConfiguration(Object appConfiguration) {
		mAppConfiguration = appConfiguration;
	}

	public Object getConfiguration() {
		return mAppConfiguration;
	}

	public void setDescriptor(AppDescriptor appDescriptor) {
		mAppDescriptor = appDescriptor;
	}

	public AppDescriptor getDescriptor() {
		return mAppDescriptor;
	}

	public int getId() {
		return mId;
	}

	public String getTitle() {
		if (mAppConfiguration instanceof AppConfiguration)
			return ((AppConfiguration) mAppConfiguration).getName();
		else if (mTitle == null)
			return mAppDescriptor.getTitle();
		else
			return mTitle;
	}

	public void setTitle(String value) {
		if (mAppConfiguration instanceof AppConfiguration)
			((AppConfiguration) mAppConfiguration).setName(value);
		else
			mTitle = value;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	private Object mAppConfiguration;

	private AppDescriptor mAppDescriptor;

	private int mId;

	private static int mCounter;

	private String mTitle;
}