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

package org.lnicholls.galleon.data;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.lnicholls.galleon.server.DataConfiguration;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.lnicholls.galleon.util.GalleonException;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.database.*;

public class Users {

	private static String HOST = "localhost";
	//private static String HOST = "galleon.tv";
	
	private static Logger log = Logger.getLogger(Users.class.getName());

	private static HttpClient httpclient = new HttpClient();

	static {
		httpclient.getParams().setParameter("http.socket.timeout", new Integer(30000));
		httpclient.getParams().setParameter("http.useragent", "Galleon " + Tools.getVersion());
	}
	
	private static SimpleDateFormat mDateFormat;
	
	static
	{
		mDateFormat = new SimpleDateFormat();
		mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
	}
	
	public static synchronized boolean login(DataConfiguration dataConfiguration) throws Exception {
		log.debug("login: ");
		if (dataConfiguration.isConfigured())
		{
			String protocol = "https";
			if (HOST.equals("localhost"))
				protocol = "http";
			
			protocol = "http";
				
			PostMethod post = new PostMethod(protocol+"://" + HOST + "/galleon/login.php");
			post.setFollowRedirects(false);
			NameValuePair username = new NameValuePair("username", Tools.decrypt(dataConfiguration.getUsername()));
			NameValuePair password = new NameValuePair("password", Tools.decrypt(dataConfiguration.getPassword()));
			post.addParameter(username);
			post.addParameter(password);
	
			//log.debug(post.getQueryString());
	
			try {
				if (httpclient.executeMethod(post) == 200) {
					String strGetResponseBody = post.getResponseBodyAsString();
					log.debug(strGetResponseBody);
	
					SAXReader saxReader = new SAXReader();
					StringReader stringReader = new StringReader(strGetResponseBody);
					Document document = saxReader.read(stringReader);
	
					Element root = document.getRootElement();
					Element error = root.element("error");
					if (error != null) {
						String code = Tools.getAttribute(error, "code");
						String reason = Tools.getAttribute(error, "reason");
						throw new GalleonException(reason, Integer.parseInt(code));
					}
	
					if (root.elements().size() > 0) {
						throw new GalleonException("Server error");
					}
					
					return true;
				}
			} catch (GalleonException ex) {
				log.error("Could not login", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not login", ex);
				throw new GalleonException("Server error");
			} finally {
				post.releaseConnection();
			}
		}
		return false;
	}
	
	public static synchronized boolean logout(DataConfiguration dataConfiguration) throws Exception {
		log.debug("logout: ");
		if (dataConfiguration.isConfigured())
		{
			PostMethod post = new PostMethod("http://" + HOST + "/galleon/logout.php");
			post.setFollowRedirects(false);
	
			try {
				if (httpclient.executeMethod(post) == 200) {
					String strGetResponseBody = post.getResponseBodyAsString();
					log.debug(strGetResponseBody);
	
					SAXReader saxReader = new SAXReader();
					StringReader stringReader = new StringReader(strGetResponseBody);
					Document document = saxReader.read(stringReader);
	
					Element root = document.getRootElement();
					Element error = root.element("error");
					if (error != null) {
						String code = Tools.getAttribute(error, "code");
						String reason = Tools.getAttribute(error, "reason");
						throw new GalleonException(reason, Integer.parseInt(code));
					}
	
					if (root.elements().size() > 0) {
						throw new GalleonException("Server error");
					}
					
					return true;
				}
			} catch (GalleonException ex) {
				log.error("Could not logout", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not logout", ex);
				throw new GalleonException("Server error");
			} finally {
				post.releaseConnection();
			}
		}
		return false;
	}
	
	private static synchronized boolean update(DataConfiguration dataConfiguration, String payload, String url) throws Exception {
		log.debug("update: ");
		if (dataConfiguration.isConfigured())
		{
			PostMethod post = new PostMethod("http://" + HOST + url);
			post.setFollowRedirects(false);
	
			try {
				if (login(dataConfiguration))
				{
					log.debug(payload);
					
					NameValuePair data = new NameValuePair("data", payload);
					post.addParameter(data);
					
					if (httpclient.executeMethod(post) == 200) {
						String strGetResponseBody = post.getResponseBodyAsString();
						log.debug(strGetResponseBody);
		
						SAXReader saxReader = new SAXReader();
						StringReader stringReader = new StringReader(strGetResponseBody);
						Document document = saxReader.read(stringReader);
		
						Element root = document.getRootElement();
						Element error = root.element("error");
						if (error != null) {
							String code = Tools.getAttribute(error, "code");
							String reason = Tools.getAttribute(error, "reason");
							throw new GalleonException(reason, Integer.parseInt(code));
						}
						
						if (root.elements().size() > 0) {
							throw new GalleonException("Server error");
						}
		
						/*
						for (Iterator i = root.elementIterator(); i.hasNext();) {
							Element element = (Element) i.next();
							System.out.println("element=" + element);
						}
						*/
						
						return true;
					}
				}
			} catch (GalleonException ex) {
				log.error("Could not update user", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
				post.releaseConnection();
				
				logout(dataConfiguration);
			}
		}
		return false;
	}	
	
	public static synchronized boolean updateApplications(DataConfiguration dataConfiguration) throws Exception {
		log.debug("updateApplications: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				List list = ApplicationManager.listAll();
				if (list!=null)
				{
					StringBuffer buffer = new StringBuffer();
					synchronized(buffer)
					{
						buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
						buffer.append("<data>\n");
						buffer.append("<applications>\n");
						Iterator iterator = list.iterator();
						while (iterator.hasNext())
						{
							buffer.append("<application");
							Application application = (Application)iterator.next();
							buffer.append(" name=\""+Tools.escapeXMLChars(application.getName())+"\"");
							buffer.append(" class=\""+Tools.escapeXMLChars(application.getClazz())+"\"");
							buffer.append(" version=\""+application.getVersion()+"\"");
							buffer.append(" dateInstalled=\""+mDateFormat.format(application.getDateInstalled())+"\"");
							if (application.getDateRemoved()!=null)
								buffer.append(" dateRemoved=\""+mDateFormat.format(application.getDateRemoved())+"\"");
							else
								buffer.append(" dateRemoved=\""+application.getDateRemoved()+"\"");
							buffer.append(" lastUsed=\""+mDateFormat.format(application.getLastUsed())+"\"");
							buffer.append(" total=\""+application.getTotal()+"\"");
							buffer.append(" shared=\""+(application.getShared().booleanValue()?1:0)+"\"");
							buffer.append(" />\n");
						}
						buffer.append("</applications>\n");
						buffer.append("</data>\n");
					}
					
					return update(dataConfiguration, buffer.toString(), "/galleon/updateApplications.php");
				}
			} catch (GalleonException ex) {
				log.error("Could not update application data", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return false;
	}	
	
	public static synchronized boolean updateInternet(DataConfiguration dataConfiguration, String payload) throws Exception {
		log.debug("updateInternet: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				return update(dataConfiguration, payload, "/galleon/updateInternet.php");
			} catch (GalleonException ex) {
				log.error("Could not update internet data", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return false;
	}
	
	public static synchronized boolean updateRss(DataConfiguration dataConfiguration, String payload) throws Exception {
		log.debug("updateRss: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				return update(dataConfiguration, payload, "/galleon/updateRss.php");
			} catch (GalleonException ex) {
				log.error("Could not update rss data", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return false;
	}
	
	private static synchronized String retrieve(DataConfiguration dataConfiguration, String payload, String url) throws Exception {
		log.debug("retrieve: ");
		if (dataConfiguration.isConfigured())
		{
			PostMethod post = new PostMethod("http://" + HOST + url);
			post.setFollowRedirects(false);
	
			try {
				if (login(dataConfiguration))
				{
					log.debug(payload);
					
					if (payload!=null)
					{
						NameValuePair data = new NameValuePair("data", payload);
						post.addParameter(data);
					}
					
					if (httpclient.executeMethod(post) == 200) {
						String strGetResponseBody = post.getResponseBodyAsString();
						log.debug(strGetResponseBody);
		
						SAXReader saxReader = new SAXReader();
						StringReader stringReader = new StringReader(strGetResponseBody);
						Document document = saxReader.read(stringReader);
		
						Element root = document.getRootElement();
						Element error = root.element("error");
						if (error != null) {
							String code = Tools.getAttribute(error, "code");
							String reason = Tools.getAttribute(error, "reason");
							throw new GalleonException(reason, Integer.parseInt(code));
						}
						
						return strGetResponseBody;
					}
				}
			} catch (GalleonException ex) {
				log.error("Could not retrieve", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
				post.releaseConnection();
				
				logout(dataConfiguration);
			}
		}
		return null;
	}
	
	public static synchronized String retrieveInternetTags(DataConfiguration dataConfiguration) throws Exception {
		log.debug("retrieveInternetTags: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				return retrieve(dataConfiguration, null, "/galleon/internetTags.php");
			} catch (GalleonException ex) {
				log.error("Could not retrieve internet tags", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return null;
	}
	
	public static synchronized String retrieveInternetFromTag(DataConfiguration dataConfiguration, String tag) throws Exception {
		log.debug("retrieveInternetFromTag: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				return retrieve(dataConfiguration, tag, "/galleon/internetFromTag.php");
			} catch (GalleonException ex) {
				log.error("Could not retrieve internet from tag", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return null;
	}	
	
	public static synchronized String retrieveRssTags(DataConfiguration dataConfiguration) throws Exception {
		log.debug("retrieveRssTags: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				return retrieve(dataConfiguration, null, "/galleon/rssTags.php");
			} catch (GalleonException ex) {
				log.error("Could not retrieve rss tags", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return null;
	}
	
	public static synchronized String retrieveRssFromTag(DataConfiguration dataConfiguration, String tag) throws Exception {
		log.debug("retrieveRssFromTag: ");
		if (dataConfiguration.isConfigured())
		{
			try {
				return retrieve(dataConfiguration, tag, "/galleon/rssFromTag.php");
			} catch (GalleonException ex) {
				log.error("Could not retrieve rss from tag", ex);
				throw ex;
			} catch (Exception ex) {
				log.error("Could not contact server", ex);
				throw new GalleonException("Server error");
			} finally {
			}
		}
		return null;
	}	
}