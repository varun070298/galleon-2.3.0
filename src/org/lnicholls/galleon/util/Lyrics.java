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

import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.lnicholls.galleon.database.Version;
import org.lnicholls.galleon.server.Constants;

public class Lyrics {

    private static final Logger log = Logger.getLogger(Lyrics.class.getName());

    /*
     * http://lyrictracker.com/soap.php?cln=lyrictracker&clv=3.1.1&id=NjA1MzU=&act=detail
     * http://lyrc.com.ar/xsearch.php?songname=Faith&artist=Celine%20Dion&act=1
     */

    private static String RESULT = "result";

    private static String NAME = "name";

    private static String GROUP = "group";

    private static String LYRIC = "lyric";

    private static String COUNT = "count";

    private static String ID = "id";
    
    private static String ARTIST = "artist";
    
    private static String TITLE = "title";

    public static String getLyrics(String song, String artist) {
        if (System.currentTimeMillis() - mTime < 1000) {
            // Dont overload the server
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {
            }
        }
        mTime = System.currentTimeMillis();

        String lyrics = getLyrics1(song, artist);
        if (lyrics == null) {
            lyrics = getLyrics2(song, artist);
        }
        return lyrics;
    }

    public static String getLyrics1(String song, String artist) {
        try {
            // http://lyrictracker.com/soap.php?cln=lyrictracker&clv=3.1.1&id=NjA1MzU=&act=detail
            Version version = Constants.CURRENT_VERSION;
            URL url = new URL("http://lyrictracker.com/soap.php?cln=galleon&clv=" + version.getMajor() + "."
                    + version.getRelease() + "." + version.getMaintenance() + "&ti=" + URLEncoder.encode(song) + "&ar="
                    + URLEncoder.encode(artist) + "&act=query&and=1");
            String page = Tools.getPage(url);
            if (page != null && page.length()>0)
            {
	            log.debug("getLyrics1: "+page);
	            
	            song = clean(song).toLowerCase();
	            log.debug("song="+song);
	            artist = clean(artist).toLowerCase();
	            log.debug("artist="+artist);
	
	            if (page != null) {
	                SAXReader saxReader = new SAXReader();
	                StringReader stringReader = new StringReader(page);
	                Document document = saxReader.read(stringReader);
	                // <results>
	                Element root = document.getRootElement();
	                int count = Integer.parseInt(root.attribute(COUNT).getText());
	                if (count > 0) {
	                    for (Iterator i = root.elementIterator(); i.hasNext();) {
	                        Element element = (Element) i.next();
	                        boolean found = false;
	                        if (element.getName().equals(RESULT)) {
	                            String returnedArtist = element.attribute(ARTIST).getText();
	                            if (returnedArtist!=null && artist.indexOf(clean(returnedArtist).toLowerCase())==-1)
	                                break;
	                            String returnedTitle = element.attribute(TITLE).getText();
	                            if (returnedTitle!=null && song.indexOf(clean(returnedTitle).toLowerCase())==-1)
	                                break;
	                            
	                            String id = element.attribute(ID).getText();
	                            if (id != null) {
	                                url = new URL("http://lyrictracker.com/soap.php?cln=galleon&clv=" + version.getMajor()
	                                        + "." + version.getRelease() + "." + version.getMaintenance() + "&id=" + id
	                                        + "&act=detail");
	                                page = Tools.getPage(url);
	                                log.debug("getLyrics1: "+page);
	                                return page.replaceAll("\\'","'");
	                            }
	                        }
	                    }
	                }
	            }
            }
        } catch (Exception ex) {
            Tools.logException(Lyrics.class, ex, "Could not get lyrics for: " + song);
        }
        return null;
    }

    public static String getLyrics2(String song, String artist) {
        try {
            // http://lyrc.com.ar/xsearch.php?songname=October&artist=U2&act=1
            URL url = new URL("http://lyrc.com.ar/xsearch.php?songname=" + URLEncoder.encode(song) + "&artist="
                    + URLEncoder.encode(artist) + "&act=1");
            String page = Tools.getPage(url);
            log.debug("getLyrics2: "+page);
            
            song = clean(song).toLowerCase();
            log.debug("song="+song);
            artist = clean(artist).toLowerCase();
            log.debug("artist="+artist);

            if (page != null) {
                page = page.replaceAll("&","&amp;");
                
                SAXReader saxReader = new SAXReader();
                //Document document = saxReader.read(new File("d:/galleon/lyrics.xml"));
                StringReader stringReader = new StringReader(page);
                Document document = saxReader.read(stringReader);

                // <lyrc>
                Element root = document.getRootElement();

                for (Iterator i = root.elementIterator(); i.hasNext();) {
                    Element element = (Element) i.next();
                    boolean found = false;
                    if (element.getName().equals(RESULT)) {
                        for (Iterator detailsIterator = element.elementIterator(); detailsIterator.hasNext();) {
                            Element detailsNode = (Element) detailsIterator.next();
                            if (detailsNode.getName().equals(NAME))
                            {
                                log.debug("compare: "+clean(detailsNode.getText()).toLowerCase()+" with "+song);
                                if (song.indexOf(clean(detailsNode.getText()).toLowerCase())==-1)
                                    break;
                            }
                            
                            if (detailsNode.getName().equals(GROUP))
                            {
                                log.debug("compare: "+clean(detailsNode.getText()).toLowerCase()+ " with "+artist);
                                if (artist.indexOf(clean(detailsNode.getText()).toLowerCase())==-1)
                                    break;
                            }
                                
                            if (detailsNode.getName().equals(LYRIC))
                                return detailsNode.getText();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Tools.logException(Lyrics.class, ex, "Could not get lyrics for: " + song);
        }
        return null;
    }
    
    private static String clean(String value) {
        StringBuffer buffer = new StringBuffer(value.length());
        synchronized (buffer) {
            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);
                if (character < 128 && character!='\'' && character!='`' && character!='`' && character!='?' && character!='!' && character!='"')
                    buffer.append(value.charAt(i));
            }
        }
        return buffer.toString();
    }

    private static long mTime = System.currentTimeMillis();
}