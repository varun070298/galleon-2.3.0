package org.lnicholls.galleon.goback;

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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.Constants;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;

/*
 * Utility class to parse URL parameters
 */

public class ItemURL implements Constants {
	private static Logger log = Logger.getLogger(ItemURL.class.getName());

	public ItemURL(String url) throws MalformedURLException {
		mParameters = new LinkedList();
		try {
			if (url.startsWith("//"))
				url = url.substring(1);

			if (!url.startsWith("http"))
				url = "http://127.0.0.1" + url;
			mURL = new URL(url.replace('\\', '/'));
			parseQuery();
		} catch (MalformedURLException ex) {
			Tools.logException(ItemURL.class, ex);
			throw ex;
		}
	}

	public String getQuery() {
		return mURL != null ? mURL.getQuery() : "";
	}

	public String getPath() {
		return mPath != null ? Tools.unEscapeXMLChars(mPath) : "";
	}

	public void setPath(String value) {
		mPath = value;
	}

	public String getUserInfo() {
		return mURL != null ? mURL.getUserInfo() : "";
	}

	public String getAuthority() {
		return mURL != null ? mURL.getAuthority() : "";
	}

	public int getPort() {
		return mURL != null ? mURL.getPort() : -1;
	}

	public int getDefaultPort() {
		return mURL != null ? mURL.getDefaultPort() : -1;
	}

	public String getProtocol() {
		return mURL != null ? mURL.getProtocol() : "";
	}

	public String getHost() {
		return mURL != null ? mURL.getHost() : "";
	}

	public String getFile() {
		return mURL != null ? mURL.getFile() : "";
	}

	public String getRef() {
		return mURL != null ? mURL.getRef() : "";
	}

	private void parseQuery() {
		try {
			if (mURL != null) {
				if (mURL.getQuery() != null) {
					StringTokenizer query = new StringTokenizer(mURL.getQuery(), "&");
					while (query.hasMoreTokens()) {
						StringTokenizer parameters = new StringTokenizer(query.nextToken(), "=");
						while (parameters.hasMoreTokens()) {
							String name = parameters.nextToken();
							String value = "";
							if (parameters.hasMoreTokens())
								value = parameters.nextToken();
							NameValue nameValue;
							// Note: this used to double-decode the parameter
							// field; that caused
							// bugs when parameters included escape characters
							// like &. Changed
							// it to single decode both -- mwk88 11/13/03
							nameValue = new NameValue(URLDecoder.decode(name, ENCODING), URLDecoder.decode(value,
									ENCODING));
							mParameters.add(nameValue);
						}
					}
				}

				mPath = URLDecoder.decode(mURL.getPath(), ENCODING);
				if (mPath.length() == 0) {
					mPath = URLDecoder.decode(getFile(), ENCODING);
				}
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Do something with this Exception
		}
	}

	public Iterator getParameterIterator() {
		return mParameters.iterator();
	}

	public String getParameter(String parameter) {
		Iterator iterator = mParameters.iterator();
		while (iterator.hasNext()) {
			NameValue nameValue = (NameValue) iterator.next();
			if (nameValue.getName().equals(parameter)) {
				// Hack to fix anchoritem parameter parameter values that are
				// double encoded
				String value = nameValue.getValue();
				try {
					if (parameter.equals(PARAMETER_ANCHOR_ITEM) && !value.startsWith("http"))
						value = URLDecoder.decode(nameValue.getValue(), ENCODING);
				} catch (UnsupportedEncodingException e) {
					// TODO Do something with this exception
				}
				return Tools.unEscapeXMLChars(value);
			}
		}
		return null;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		synchronized (buffer) {
			buffer.append("Authority: " + getAuthority() + "\n");
			buffer.append("Port: " + getPort() + "\n");
			buffer.append("DefaultPort: " + getDefaultPort() + "\n");
			buffer.append("Protocol: " + getProtocol() + "\n");
			buffer.append("Host: " + getHost() + "\n");
			buffer.append("File: " + getFile() + "\n");
			buffer.append("Ref: " + getRef() + "\n");
			buffer.append("UserInfo: " + getUserInfo() + "\n");
			buffer.append("Path: " + getPath() + "\n");
			buffer.append("Query: " + getQuery() + "\n");
			buffer.append("Parameters: \n");
			Iterator iterator = getParameterIterator();
			while (iterator.hasNext()) {
				buffer.append((NameValue) iterator.next() + "\n");
			}
		}
		return buffer.toString();
	}

	private URL mURL;

	private String mPath;

	private LinkedList mParameters;
}