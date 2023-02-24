package org.lnicholls.galleon.apps.shoutcast;

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

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.ShoutcastStationManager;
import org.lnicholls.galleon.database.ShoutcastStation;
import org.lnicholls.galleon.media.MediaManager;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.ReloadCallback;
import org.lnicholls.galleon.util.ReloadTask;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.database.PersistentValue;

public class ShoutcastStations {
    private static Logger log = Logger.getLogger(ShoutcastStations.class.getName());

    public static String SHOUTCAST = "Shoutcast.com";

    private static int MAX_REQUESTS_PER_DAY = 20;

    public ShoutcastStations(ShoutcastConfiguration configuration) {
        mConfiguration = configuration;

        Server.getServer().scheduleShortTerm(new ReloadTask(new ReloadCallback() {
            public void reload() {
                try {
                	log.debug("Shoutcast");
                    getPlaylists();
                } catch (Throwable ex) {
                    log.error("Could not download stations", ex);
                }
            }
        }), 60 * 24);
    }

    private void getStations(String genre) {
    	try {
	        HttpClient httpclient = new HttpClient();
	        httpclient.getParams().setParameter("http.socket.timeout", new Integer(30000));
	        httpclient.getParams().setParameter("http.useragent", System.getProperty("http.agent"));

	        ArrayList stations = new ArrayList();
	        int total = 1;
	        for (int i=0;i<total;i++)
	        {
	            String page = "";
	            if (i>0)
	            	page = "&startat="+(i*20);
	        	GetMethod get = new GetMethod("http://www.shoutcast.com/directory/?sgenre="
	                    + URLEncoder.encode(URLDecoder.decode(genre, "UTF-8"), "UTF-8")+page);
	            get.setFollowRedirects(true);

	            try {
	                int iGetResultCode = httpclient.executeMethod(get);
	                final String strGetResponseBody = get.getResponseBodyAsString();
	                //log.debug(strGetResponseBody);

	                if (strGetResponseBody != null) {
	                	if (i==0)
	            	   	{
		            	   // Page 1 of 20
		            	   String REGEX = "Page ([0-9]*) of ([0-9]*)";
		                   Pattern p = Pattern.compile(REGEX);
		                   Matcher m = p.matcher(strGetResponseBody);
		                   if (m.find()) {
		                       total = Integer.parseInt(m.group(2));
		                   }

		                   // Servers - <font color="#FF0000">12,147</font>
		                   REGEX = "Servers - <font color=\"#FF0000\">([^<]*)</font>";
		                   p = Pattern.compile(REGEX);
		                   m = p.matcher(strGetResponseBody);
		                   if (m.find()) {
		                      PersistentValueManager.savePersistentValue(ShoutcastStations.this.getClass().getName() + "." + "servers", m.group(1));
		                   }
	            	   	}

	                    //"/sbin/shoutcast-playlist.pls?rn=5224&file=filename.pls"
	                    String REGEX = "=\"/sbin/shoutcast-playlist.pls([^<]*)\">";
	                    Pattern p = Pattern.compile(REGEX);
	                    Matcher m = p.matcher(strGetResponseBody);
	                    boolean found = false;
	                    while (m.find()) {
	                        if (log.isDebugEnabled())
	                            log.debug("Parameters: " + m.group(1));
	                        String link = "http://www.shoutcast.com/sbin/shoutcast-playlist.pls" + m.group(1);

	                        stations.add(link);
	                    }
	                }
	            } catch (Exception ex) {
	                log.error("Could not download stations: " + genre, ex);
	            } finally {
	                get.releaseConnection();
	            }
	        }
	        List current = ShoutcastStationManager.findByGenre(genre);
	        Iterator iterator = current.iterator();
	        while (iterator.hasNext())
	        {
	        	ShoutcastStation station = (ShoutcastStation)iterator.next();
	        	try
                {
        			// TODO delete audio
                   	ShoutcastStationManager.deleteShoutcastStation(station);
                } catch (Exception ex) {
        	        Tools.logException(ShoutcastStations.class, ex);
        	    }
	        }
	        int pos = 0;
	        iterator = stations.iterator();
	        while (iterator.hasNext())
	        {
	        	String link = (String)iterator.next();
	        	try
                {
                	List list = ShoutcastStationManager.findByUrl(link);
                	if (list==null || list.size()==0)
                	{
                    	ShoutcastStation station = new ShoutcastStation(genre, link, ++pos, ShoutcastStation.STATUS_PENDING);
                    	ShoutcastStationManager.createShoutcastStation(station);
                	}
                } catch (Exception ex) {
        	        Tools.logException(ShoutcastStations.class, ex);
        	    }
	        }
	    } catch (Exception ex) {
	        Tools.logException(ShoutcastStations.class, ex);
	    }
    }

	public void getPlaylists() {
		List list = mConfiguration.getLimitedGenres();
	    if (list!=null && list.size()>0)
	    {
			int limit = MAX_REQUESTS_PER_DAY / list.size();
		    Iterator iterator = list.iterator();
		    while (iterator.hasNext())
		    {
		    	ShoutcastConfiguration.LimitedGenre limitedGenre = (ShoutcastConfiguration.LimitedGenre)iterator.next();
		    	PersistentValue persistentValue = PersistentValueManager.loadPersistentValue(ShoutcastStations.this.getClass().getName() + "." + limitedGenre.getGenre());
			    int start = 0;
			    if (persistentValue != null) {
			        try {
			            start = Integer.parseInt(persistentValue.getValue());
			        } catch (Exception ex) {
			        }
			    }

			    try
	            {
			    	List stations = ShoutcastStationManager.findByGenre(limitedGenre.getGenre());
	            	if (stations==null || stations.size()==0)
	            	{
	            		getStations(limitedGenre.getGenre());
	            		stations = ShoutcastStationManager.findByGenre(limitedGenre.getGenre());
	            	}
	            	if (stations!=null && stations.size()>0)
	            	{
	            		if (start >= stations.size())
	            		{
	            			start = 0;
	            			// reset every station to pending to get latest info again
	            			for (int i=0; i < stations.size(); i++)
	            	    	{
	            	    		ShoutcastStation station = (ShoutcastStation)stations.get(i);
	            	    		try
	            	    		{
	            	    			station.setStatus(ShoutcastStation.STATUS_PENDING);
	            	    			ShoutcastStationManager.updateShoutcastStation(station);
	            	    		}
	            	    		catch (Exception ex)
	            	    		{
	            	    			Tools.logException(ShoutcastStations.class, ex);
	            	    		}
	            	    	}
	            			getStations(limitedGenre.getGenre());
	            			stations = ShoutcastStationManager.findByGenre(limitedGenre.getGenre());
	            		}

	            		int max = start + limit;
	        		    if (limitedGenre.getLimit()!=-1 && max > limitedGenre.getLimit())
	        		    	max = limitedGenre.getLimit();
	        		    if (max > stations.size())
	        		    	max = stations.size();

	        	    	for (int i=start; i < max; i++)
	        	    	{
	        	    		ShoutcastStation station = (ShoutcastStation)stations.get(i);
	        	    		if (station.getStatus()==ShoutcastStation.STATUS_PENDING)
	        	    		{
	        	    			URL url = new URL(station.getUrl());
	                            String page = Tools.getPage(url);
	                            if (page != null && page.length()>0) {
	                                int total = 0;
	                            	String inputLine = "";
	                                BufferedReader reader = new BufferedReader(new StringReader(page));
	                                while ((inputLine = reader.readLine()) != null) {
	                                	if (inputLine.startsWith("File")) {
	                                        String u = inputLine.substring(inputLine.indexOf("=") + 1);
	                                        inputLine = reader.readLine();
	                                        String t = inputLine.substring(inputLine.indexOf("=") + 1).trim();
	                                        if (t.startsWith("(")) {
	                                            t = t.substring(t.indexOf(")") + 1);
	                                        }
	                                        if (log.isDebugEnabled())
	                                            log.debug("PlaylistItem: " + t + "=" + u);

	                                        try {
	                                            // Remove duplicates
	                                            List all = AudioManager.findByOrigenGenre(SHOUTCAST, limitedGenre.getGenre());
	                                            for (int j = 0; j < all.size(); j++) {
	                                                Audio audio = (Audio) all.get(j);
	                                                for (int k = j; k < all.size(); k++) {
	                                                    Audio other = (Audio) all.get(k);
	                                                    if (!audio.getId().equals(other.getId())) {
	                                                        if (audio.getPath()!=null && other.getPath()!=null && audio.getPath().equals(other.getPath())) {
	                                                            AudioManager.deleteAudio(other);
	                                                        }
	                                                    }
	                                                }
	                                            }

	                                            Audio current = null;
	                                            List same = AudioManager.findByPath(u);
	                                            if (same.size() > 0)
	                                                current = (Audio) same.get(0);

	                                            if (current != null) {
	                                            	current.setTitle(t);
	                                            	current.setGenre(limitedGenre.getGenre());
	                                            	current.setOrigen(SHOUTCAST);
	                                            	current.setExternalId(String.valueOf(++total));
	                                            	AudioManager.updateAudio(current);
	                                            } else {
	                                                Audio audio = (Audio) MediaManager.getMedia(u);
	                                                audio.setTitle(t);
	                                                audio.setGenre(limitedGenre.getGenre());
	                                                audio.setOrigen(SHOUTCAST);
	                                                audio.setExternalId(String.valueOf(++total));
	                                                AudioManager.createAudio(audio);
	                                            }
	                                        } catch (Exception ex) {
	                                        }
	                                    }
	                                }
	                                reader.close();
	                            }
	                            try
	            	    		{
	            	    			station.setStatus(ShoutcastStation.STATUS_DOWNLOADED);
	            	    			ShoutcastStationManager.updateShoutcastStation(station);
	            	    		}
	            	    		catch (Exception ex)
	            	    		{
	            	    			Tools.logException(ShoutcastStations.class, ex);
	            	    		}
	        	    		}
	        	    	}
	        	    	PersistentValueManager.savePersistentValue(ShoutcastStations.this.getClass().getName() + "." + limitedGenre.getGenre(), String.valueOf(max));
	            	}
	            } catch (Exception ex) {
	    	        Tools.logException(ShoutcastStations.class, ex);
	    	    }
		    }
	    }
	}

    public void remove()
    {
    	try {
    		List list = AudioManager.listGenres(ShoutcastStations.SHOUTCAST);
			for (Iterator i = list.iterator(); i.hasNext(); /* Nothing */) {
				String genre = (String) i.next();
	    		remove(genre);
			}

    	} catch (Exception ex) {
    		Tools.logException(ShoutcastStations.class, ex);
        }
    }

    public void remove(String genre)
    {
    	try {
    		List current = ShoutcastStationManager.findByGenre(genre);
	        Iterator iterator = current.iterator();
	        while (iterator.hasNext())
	        {
	        	ShoutcastStation station = (ShoutcastStation)iterator.next();
	        	ShoutcastStationManager.deleteShoutcastStation(station);
	        }
    		List all = AudioManager.findByOrigenGenre(SHOUTCAST, genre);
            for (int j = 0; j < all.size(); j++) {
                Audio audio = (Audio) all.get(j);
                AudioManager.deleteAudio(audio);
            }
    	} catch (Exception ex) {
    		Tools.logException(ShoutcastStations.class, ex);
        }
    }

    public String getServers()
    {
    	PersistentValue persistentValue = PersistentValueManager.loadPersistentValue(ShoutcastStations.this.getClass().getName() + "." + "servers");
    	if (persistentValue!=null)
    	{
            return persistentValue.getValue();
    	}
    	return null;
    }

    public void setShoutcastConfiguration(ShoutcastConfiguration shoutcastConfiguration)
    {
    	mConfiguration = shoutcastConfiguration;
    }

    private ShoutcastConfiguration mConfiguration;

    private static long mTime = System.currentTimeMillis();

    private static int mCounter = MAX_REQUESTS_PER_DAY;
}