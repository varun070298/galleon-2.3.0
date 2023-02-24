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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;

/**
 * App descriptor class extracts the manifest file from the app jar and
 * determines the app properties
 */
public class AppDescriptor implements Serializable {
	private static Logger log = Logger.getLogger(AppDescriptor.class.getName());

	private static final String TITLE = "Title";

	private static final String RELEASE_DATE = "ReleaseDate";

	private static final String DESCRIPTION = "Description";

	private static final String DOCUMENTATION = "Documentation";

	private static final String AUTHOR_NAME = "Author";

	private static final String AUTHOR_EMAIL = "Email";

	private static final String AUTHOR_HOMEPAGE = "Homepage";

	private static final String VERSION = "Version";

	private static final String CONFIGURATION_PANEL = "ConfigurationPanel";

	private static final String CONFIGURATION = "Configuration";

	private static final String TAGS = "Tags";

	public AppDescriptor(File jar) throws IOException, AppException {
		mJar = jar;

		JarFile zipFile = new JarFile(jar);
		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String name = entry.getName();
			if (name.toUpperCase().equals(JarFile.MANIFEST_NAME)) {
				InputStream in = null;
				try {
					in = zipFile.getInputStream(entry);
					Manifest manifest = new Manifest(in);
					Attributes attributes = manifest.getMainAttributes();
					if (attributes.getValue("Main-Class") != null) {
						mClassName = (String) attributes.getValue("Main-Class");
						if (attributes.getValue("HME-Arguments") != null)
							mArguments = (String) attributes.getValue("HME-Arguments");
						if (attributes.getValue(TITLE) != null)
							mTitle = (String) attributes.getValue(TITLE);
						if (attributes.getValue(RELEASE_DATE) != null)
							mReleaseDate = (String) attributes.getValue(RELEASE_DATE);
						if (attributes.getValue(DESCRIPTION) != null)
							mDescription = (String) attributes.getValue(DESCRIPTION);
						if (attributes.getValue(DOCUMENTATION) != null)
							mDocumentation = (String) attributes.getValue(DOCUMENTATION);
						if (attributes.getValue(AUTHOR_NAME) != null)
							mAuthorName = (String) attributes.getValue(AUTHOR_NAME);
						if (attributes.getValue(AUTHOR_EMAIL) != null)
							mAuthorEmail = (String) attributes.getValue(AUTHOR_EMAIL);
						if (attributes.getValue(AUTHOR_HOMEPAGE) != null)
							mAuthorHomepage = (String) attributes.getValue(AUTHOR_HOMEPAGE);
						if (attributes.getValue(VERSION) != null)
							mVersion = (String) attributes.getValue(VERSION);
						if (attributes.getValue(CONFIGURATION) != null)
							mConfiguration = (String) attributes.getValue(CONFIGURATION);
						if (attributes.getValue(CONFIGURATION_PANEL) != null)
							mConfigurationPanel = (String) attributes.getValue(CONFIGURATION_PANEL);
						if (attributes.getValue(TAGS) != null)
							mTags = (String) attributes.getValue(TAGS);
					}

					if (mTitle == null) {
						mTitle = jar.getName().substring(0, jar.getName().indexOf('.'));
					}
				} catch (Exception ex) {
					Tools.logException(AppDescriptor.class, ex, "Cannot get descriptor: " + jar.getAbsolutePath());
				} finally {
					if (in != null) {
						try {
							in.close();
							in = null;
						} catch (Exception ex) {
						}
					}
				}
				break;
			}
		}
		zipFile.close();
	}

	public void setTitle(String value) {
		mTitle = value;
	}

	public String getTitle() {
		if (mTitle == null) {
			StringTokenizer tokenizer = new StringTokenizer(mClassName, ".");
			if (tokenizer.countTokens() > 0) {
				String lastToken = null;
				while (tokenizer.hasMoreTokens())
					lastToken = tokenizer.nextToken();
				if (lastToken != null) {
					return lastToken;
				}
			}
		}
		return mTitle;
	}

	public void setClassName(String value) {
		mClassName = value;
	}

	public String getClassName() {
		return mClassName;
	}

	public void setArguments(String value) {
		mArguments = value;
	}

	public String getArguments() {
		return mArguments;
	}

	public void setVersion(String value) {
		mVersion = value;
	}

	public String getVersion() {
		return mVersion;
	}

	public void setReleaseDate(String value) {
		mReleaseDate = value;
	}

	public String getReleaseDate() {
		return mReleaseDate;
	}

	public void setDescription(String value) {
		mDescription = value;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDocumentation(String value) {
		mDocumentation = value;
	}

	public String getDocumentation() {
		return mDocumentation;
	}

	public void setAuthorName(String value) {
		mAuthorName = value;
	}

	public String getAuthorName() {
		return mAuthorName;
	}

	public void setAuthorEmail(String value) {
		mAuthorEmail = value;
	}

	public String getAuthorEmail() {
		return mAuthorEmail;
	}

	public void setAuthorHomepage(String value) {
		mAuthorHomepage = value;
	}

	public String getAuthorHomepage() {
		return mAuthorHomepage;
	}

	public void setConfiguration(String value) {
		mConfiguration = value;
	}

	public String getConfiguration() {
		if (mConfiguration == null) {
			StringTokenizer tokenizer = new StringTokenizer(mClassName, ".");
			if (tokenizer.countTokens() > 0) {
				String lastToken = null;
				while (tokenizer.hasMoreTokens())
					lastToken = tokenizer.nextToken();
				if (lastToken != null) {
					return mClassName + "$" + lastToken + "Configuration";
				}
			}
			return mClassName + "$Configuration";
		} else
			return mConfiguration;
	}

	public void setConfigurationPanel(String value) {
		mConfigurationPanel = value;
	}

	public String getConfigurationPanel() {
		if (mConfigurationPanel == null) {
			StringTokenizer tokenizer = new StringTokenizer(mClassName, ".");
			if (tokenizer.countTokens() > 0) {
				String lastToken = null;
				while (tokenizer.hasMoreTokens())
					lastToken = tokenizer.nextToken();
				if (lastToken != null) {
					return mClassName + "$" + lastToken + "ConfigurationPanel";
				}
			}
			return mClassName + "$ConfigurationPanel";
		} else
			return mConfigurationPanel;
	}

	public void setHME(boolean value) {
		mIsHME = value;
	}

	public boolean isHME() {
		return mIsHME;
	}

	public void setTags(String value) {
		mTags = value;
	}

	public String getTags() {
		return mTags;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	private File mJar;

	private String mTitle;

	private String mClassName;

	private String mArguments;

	private String mVersion;

	private String mReleaseDate;

	private String mDescription;

	private String mDocumentation;

	private String mAuthorName;

	private String mAuthorEmail;

	private String mAuthorHomepage;

	private String mConfiguration;

	private String mConfigurationPanel;

	private String mTags;

	private boolean mIsHME;
}