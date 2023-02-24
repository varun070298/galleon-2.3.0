package org.lnicholls.galleon.util;

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

import java.awt.Image;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.lnicholls.galleon.database.Movie;
import org.lnicholls.galleon.server.Server;

public class Amazon {

    private static final Logger log = Logger.getLogger(Amazon.class.getName());

    /*
     * The following is required by the Amazon.com web services. DO NOT USE THESE WITH ANY OTHER PROJECT SINCE THEY HAVE
     * BEEN REGISTERED WITH AMAZON.COM FOR THE GALLEON PROJECT. Obtain your own key by registering at:
     * http://www.amazon.com/gp/browse.html/ref=smm_sn_aws/102-9770766-0077768?%5Fencoding=UTF8&node=3435361
     */

    private final static String SUBSCRIPTION_ID = "1S15VY5XR4PV42R2YRG2";

    public static Image getAlbumImage(String key, String artist, String title) {
        // http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&SubscriptionId=1S15VY5XR4PV42R2YRG2&Operation=ItemSearch&SearchIndex=Music&ResponseGroup=Images&Artist="
        // + artist + "&Title=" + title

        try {
            URL url = new URL("http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&SubscriptionId="
                    + SUBSCRIPTION_ID + "&Operation=ItemSearch&SearchIndex=Music&ResponseGroup=Images&Artist="
                    + URLEncoder.encode(artist) + "&Title=" + URLEncoder.encode(title));
            return getImage(key, url);
        } catch (MalformedURLException ex) {
            Tools.logException(Amazon.class, ex, "Could not create AWS url: "+artist+","+title);
        }
        return null;
    }

    public static Image getMusicImage(String key, String keywords) {
        try {
            URL url = new URL("http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&SubscriptionId="
                    + SUBSCRIPTION_ID + "&Operation=ItemSearch&SearchIndex=Music&ResponseGroup=Images&Keywords="
                    + URLEncoder.encode(keywords));
            return getImage(key, url);
        } catch (MalformedURLException ex) {
            Tools.logException(Amazon.class, ex, "Could not create AWS url: "+keywords);
        }
        return null;
    }

    public static synchronized Image getImage(String key, URL url) {
        Image image = null;

        if (System.currentTimeMillis() - mTime < 1000) {
            // Not allowed to call AWS more than once a second
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {
            }
        }

        try {
            SAXReader saxReader = new SAXReader();
            // http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&SubscriptionId=1S15VY5XR4PV42R2YRG2&Operation=ItemSearch&SearchIndex=Music&ResponseGroup=Images&Artist="
            // + artist + "&Title=" + title
            String page = Tools.getPage(url);
            if (page != null && page.length()>0)
            {
	            log.debug("Amazon images: " + page);
	
	            StringReader stringReader = new StringReader(page);
	            Document document = saxReader.read(stringReader);
	
	            //Document document = saxReader.read(new File("d:/galleon/amazon.xml"));
	            Element root = document.getRootElement();
	
	            Element items = root.element("Items");
	            if (items != null) {
	                for (Iterator i = items.elementIterator("Item"); i.hasNext();) {
	                    Element item = (Element) i.next();
	
	                    Element someImage = item.element("LargeImage");
	                    if (someImage == null)
	                        someImage = item.element("MediumImage");
	                    if (someImage == null)
	                        someImage = item.element("SmallImage");
	                    if (someImage != null) {
	                        Element address = someImage.element("URL");
	                        if (address != null) {
	                            log.debug(address.getTextTrim());
	
	                            Element height = someImage.element("Height");
	                            Element width = someImage.element("Width");
	                            try {
	                                image = Tools.getImage(new URL(address.getTextTrim()),
	                                        Integer.parseInt(height.getTextTrim()), Integer.parseInt(width.getTextTrim()));
	                                break;
	                            } catch (Exception ex) {
	                                log.error("Could not download Amazon image: " + address.getTextTrim(), ex);
	                            }
	                        }
	                    }
	                }
	            }
            }
        } catch (Exception ex) {
            Tools.logException(Amazon.class, ex, "Could parse AWS url: "+url.toExternalForm());
        }

        mTime = System.currentTimeMillis();
        return image;
    }
    
	public static String getMoviePoster(String name) {
		if (System.currentTimeMillis() - mTime < 1000) {
            // Not allowed to call AWS more than once a second
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {
            }
        }

		String url = null;
		try {
			Parser parser = new Parser("http://www.amazon.com/exec/obidos/search-handle-url?index=theatrical&field-keywords="+URLEncoder.encode(name));  //www.amazon.com/exec/obidos/search-handle-url?index=theatrical&field-keywords=Batman

			NodeList linkList = parser.extractAllNodesThatMatch(new NodeClassFilter(LinkTag.class));
			for (int j = 0; j < linkList.size(); j++) {
				LinkTag linkTag = (LinkTag) linkList.elementAt(j);
				if (linkTag.getLink().indexOf("ZZZZZ")!=-1)
				{
					url = linkTag.extractLink();
					break;
				}
			}
		} catch (Exception ex) {
			Tools.logException(Amazon.class, ex, "Could not get movie poster: " + name);
		}
		
		mTime = System.currentTimeMillis();
		return url;
	}
    
    private static long mTime = System.currentTimeMillis();
}