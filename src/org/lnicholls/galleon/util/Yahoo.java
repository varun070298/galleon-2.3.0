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

import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import org.apache.log4j.Logger;

import com.yahoo.search.ImageSearchRequest;
import com.yahoo.search.ImageSearchResult;
import com.yahoo.search.ImageSearchResults;
import com.yahoo.search.SearchClient;

public class Yahoo {

    private static final Logger log = Logger.getLogger(Yahoo.class.getName());

    /*
     * The following is required by the Yahoo.com search developer network. DO NOT USE THESE WITH ANY OTHER PROJECT
     * SINCE THEY HAVE BEEN REGISTERED WITH YAHOO.COM FOR THE GALLEON PROJECT. Obtain your own key by registering at:
     * http://developer.yahoo.net/
     */

    private final static String APPLICATION_ID = "galleonhme";

    private final static int DAILY_SEARCH_LIMIT = 5000;

    public static List getImages(String key) {
        ArrayList results = new ArrayList();
        if ((System.currentTimeMillis() - mTime < 1000 * 60 * 60 * 24)) {
            if (mCounter <= 0) {
                // Not allowed to exceed daily limit
                log.error("Exceeded daily search limit for: " + key);
                return results;
            }
        } else {
            mTime = System.currentTimeMillis();
            mCounter = DAILY_SEARCH_LIMIT;
        }

        mCounter--;

        try {
            SearchClient searchClient = new SearchClient(APPLICATION_ID);
            ImageSearchRequest imageSearchRequest = new ImageSearchRequest(key);
            imageSearchRequest.setResults(50);
            imageSearchRequest.setAdultOk(false);
            ImageSearchResults imageSearchResults = searchClient.imageSearch(imageSearchRequest);

            ImageSearchResult[] list = imageSearchResults.listResults();
            for (int i = 0; i < list.length; i++) {
                ImageSearchResult imageSearchResult = list[i];
                if ((imageSearchResult.getFileFormat().equals("gif") || imageSearchResult.getFileFormat().equals("jpg")
                        || imageSearchResult.getFileFormat().equals("jpeg")
                        || imageSearchResult.getFileFormat().equals("png")) &&
                        (imageSearchResult.getWidth().intValue()>20 && imageSearchResult.getHeight().intValue()>20) && 
                        (imageSearchResult.getWidth().intValue()<=1024 && imageSearchResult.getHeight().intValue()<=768)) {
                    URL url = new URL(imageSearchResult.getUrl());
                    results.add(new NameValue(url.getHost(), imageSearchResult.getUrl()));
                }
            }
        } catch (Exception ex) {
            Tools.logException(Yahoo.class, ex, "Could not get images for: " + key);
        }

        return results;
    }

    private static long mTime = System.currentTimeMillis();

    private static int mCounter = DAILY_SEARCH_LIMIT;
}