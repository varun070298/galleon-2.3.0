package org.lnicholls.galleon.apps.iTunes;

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

import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.HibernateUtil;
import org.lnicholls.galleon.database.Playlists;
import org.lnicholls.galleon.database.PlaylistsManager;
import org.lnicholls.galleon.database.PlaylistsTracks;
import org.lnicholls.galleon.database.PlaylistsTracksManager;
import org.lnicholls.galleon.media.Mp3File;
import org.lnicholls.galleon.util.Tools;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PlaylistParser {
    private static Logger log = Logger.getLogger(PlaylistParser.class.getName());

    private static String DICT = "dict";

    private static String KEY = "key";

    public PlaylistParser(String path) {
        try {
        	//path = "D:/galleon/iTunes Music Library.xml";
        	ArrayList currentPlaylists = new ArrayList(); 
        	// Read all tracks
        	XMLReader trackReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        	TrackParser trackParser = new TrackParser();
        	trackReader.setContentHandler(trackParser);
        	trackReader.setErrorHandler(trackParser);
        	trackReader.setFeature("http://xml.org/sax/features/validation", false);
            File file = new File(path);
            if (file.exists()) {
                InputStream inputStream = Tools.getInputStream(file);
                trackReader.parse(new InputSource(inputStream));
                inputStream.close();
            }
            
            // Read all playlists
            XMLReader listReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        	ListParser listParser = new ListParser(currentPlaylists);
        	listReader.setContentHandler(listParser);
        	listReader.setErrorHandler(listParser);
        	listReader.setFeature("http://xml.org/sax/features/validation", false);
            file = new File(path);
            if (file.exists()) {
                InputStream inputStream = Tools.getInputStream(file);
                listReader.parse(new InputSource(inputStream));
                inputStream.close();
            }
	            
            // Remove old playlists
            List list = PlaylistsManager.listAll();
            if (list!=null && list.size()>0)
            {
	            Iterator playlistIterator = list.iterator();
	        	while (playlistIterator.hasNext())
	            {
	            	Playlists playlist = (Playlists)playlistIterator.next();
	            	boolean found = false;
	            	Iterator iterator = currentPlaylists.iterator();
	                while (iterator.hasNext())
	                {
	                	String externalId = (String)iterator.next();
	                	if (externalId.equals(playlist.getExternalId()))
	                	{
	                		found = true;
	                		break;
	                	}
	                }
	                
	                if (!found)
	                {
	                	PlaylistsManager.deletePlaylistsTracks(playlist);
	                	PlaylistsManager.deletePlaylists(playlist);
	                	log.debug("Removed playlist: "+playlist.getTitle());
	                }
	            }
	        	list.clear();
            }
            currentPlaylists.clear();
        } catch (IOException ex) {
            Tools.logException(PlaylistParser.class, ex);
        } catch (SAXException ex) {
            Tools.logException(PlaylistParser.class, ex);
        } catch (Exception ex) {
            Tools.logException(PlaylistParser.class, ex);
        }
    }
    
    class TrackParser extends DefaultHandler
    {
    	public TrackParser()
    	{
    		super();
    		mTracks = new HashMap();
    		mKey = new StringBuffer(100);
    		mValue = new StringBuffer(100);
    	}
    	
	    /**
	     * Method used by SAX at the start of the XML document parsing.
	     */
	    public void startDocument() {
	    }
	
	    /**
	     * SAX XML parser method. Start of an element.
	     * 
	     * @param namespaceURI
	     *            namespace URI this element is associated with, or an empty String
	     * @param localName
	     *            name of element (with no namespace prefix, if one is present)
	     * @param qName
	     *            XML 1.0 version of element name: [namespace prefix]:[localName]
	     * @param attributes
	     *            Attributes for this element
	     */
	
	    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
	            throws SAXException {
	        if (localName.equals(DICT)) {
	            mDictLevel = mDictLevel + 1;
	        } else if (localName.equals(KEY)) {
	            mInKey = true;
	            mKey.setLength(0);
	        }
	
	        if (mLastTag != null && mLastTag.equals(KEY) && !localName.equals(KEY) && !localName.equals(DICT)) {
	            mInValue = true;
	            mValue.setLength(0);
	            mType = localName;
	        } else if (mFoundTracks && mDictLevel == 3 && localName.equals(DICT)) {
	        	mTracks.clear();
	        }
	    }
	
	    public void characters(char[] ch, int start, int length) throws SAXException {
	    	if (mInKey) {
                mKey.append(new String(ch, start, length).trim());
	        } else if (mInValue) {
                mValue.append(new String(ch, start, length).trim());
	        }
	    }
	
	    /**
	     * SAX XML parser method. End of an element.
	     * 
	     * @param namespaceURI
	     *            namespace URI this element is associated with, or an empty String
	     * @param localName
	     *            name of element (with no namespace prefix, if one is present)
	     * @param qName
	     *            XML 1.0 version of element name: [namespace prefix]:[localName]
	     * @param attributes
	     *            Attributes for this element
	     */
	
	    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	    	mLastTag = localName;
	
	        if (mDictLevel == 1 && localName.equals(KEY) && same(mKey, "Tracks")) {
	            mFoundTracks = true;
	            mFoundPlaylists = false;
	        } else if (mDictLevel == 1 && localName.equals(KEY) && same(mKey, "Playlists")) {
	            mFoundTracks = false;
	            mFoundPlaylists = true;
	        } else if (mFoundTracks && mDictLevel == 3 && localName.equals(DICT)) {
                processTrack(mTracks);
                mTracks.clear();

                if (++mCounter%100==0)
         		   System.gc();
                try {
                    Thread.sleep(50); // give the CPU some breathing time
                } catch (Exception ex) {
                }
	        }
	
	        if (localName.equals(DICT)) {
	            mDictLevel = mDictLevel - 1;
	        } else if (localName.equals(KEY)) {
	            mInKey = false;
	        } else if (mType != null && localName.equals(mType)) {
	            if (mFoundTracks) {
	            	mTracks.put(mKey.toString(), mValue.toString());
	            }
	            mInValue = false;
	        }
	    }
	
	    /**
	     * Method used by SAX at the end of the XML document parsing.
	     */
	
	    public void endDocument() {
	    }
	
	    /**
	     * Receive notification of a parser warning.
	     * 
	     * <p>
	     * The default implementation does nothing. Application writers may override this method in a subclass to take
	     * specific actions for each warning, such as inserting the message in a log file or printing it to the console.
	     * </p>
	     * 
	     * @param e
	     *            The warning information encoded as an exception.
	     * @exception org.xml.sax.SAXException
	     *                Any SAX exception, possibly wrapping another exception.
	     * @see org.xml.sax.ErrorHandler#warning
	     * @see org.xml.sax.SAXParseException
	     */
	    public void warning(SAXParseException e) throws SAXException {
	        throw e;
	    }
	
	    /**
	     * Receive notification of a recoverable parser error.
	     * 
	     * <p>
	     * The default implementation does nothing. Application writers may override this method in a subclass to take
	     * specific actions for each error, such as inserting the message in a log file or printing it to the console.
	     * </p>
	     * 
	     * @param e
	     *            The warning information encoded as an exception.
	     * @exception org.xml.sax.SAXException
	     *                Any SAX exception, possibly wrapping another exception.
	     * @see org.xml.sax.ErrorHandler#warning
	     * @see org.xml.sax.SAXParseException
	     */
	    public void error(SAXParseException e) throws SAXException {
	        throw e;
	    }
	
	    /**
	     * Report a fatal XML parsing error.
	     * 
	     * <p>
	     * The default implementation throws a SAXParseException. Application writers may override this method in a subclass
	     * if they need to take specific actions for each fatal error (such as collecting all of the errors into a single
	     * report): in any case, the application must stop all regular processing when this method is invoked, since the
	     * document is no longer reliable, and the parser may no longer report parsing events.
	     * </p>
	     * 
	     * @param e
	     *            The error information encoded as an exception.
	     * @exception org.xml.sax.SAXException
	     *                Any SAX exception, possibly wrapping another exception.
	     * @see org.xml.sax.ErrorHandler#fatalError
	     * @see org.xml.sax.SAXParseException
	     */
	    public void fatalError(SAXParseException e) throws SAXException {
	        throw e;
	    }
	    
	    private void processTrack(HashMap track) {
	    	Audio audio = new Audio();
	    	Mp3File.defaultProperties(audio); // no size??

	        String location = null;
	        if (track.containsKey("Location")) {
	            location = decode((String) track.get("Location"));
	            if (location.endsWith("/"))
	                location = location.substring(0, location.length()-1);
	            // TODO ignore others?
	            if (location.startsWith("file://localhost/")) {
	                if (SystemUtils.IS_OS_MAC_OSX)
	                    location = location.substring("file://localhost".length(), location.length());
	                else
	                    location = location.substring("file://localhost/".length(), location.length());
	                try {
	                    File file = new File(location);
	                    if (file.exists() && file.getName().toLowerCase().endsWith(".mp3")) {
	                        location = file.getCanonicalPath();
	                        audio.setPath(location);
	                    } else
	                        return;
	                } catch (Exception ex) {
	                    Tools.logException(PlaylistParser.class, ex);
	                }
	            } else
	            	audio.setPath(location); // http
	        }
	        
	        if (location != null) {
	        	String externalId = null;
	        	boolean found = false;
	        	if (track.containsKey("Track ID")) {
	        		externalId = decode((String) track.get("Track ID"));
	        		/*
	        		try {
		                List list = AudioManager.findByExternalId(externalId);
		                if (list != null && list.size() > 0) {
		                	audio = (Audio) list.get(0);
		                    list.clear();
		                    found = true;
		                }
		            } catch (Exception ex) {
		                Tools.logException(PlaylistParser.class, ex);
		            }
		            */
	        	}
	        	
	        	if (!found)
	        	{
		        	try {
		                List list = AudioManager.findByPath(location);
		                if (list != null && list.size() > 0) {
		                	audio = (Audio) list.get(0);
		                    list.clear();
		                }
		            } catch (Exception ex) {
		                Tools.logException(PlaylistParser.class, ex);
		            }
	        	}
	            
	            try {
	                if (track.containsKey("Date Modified")) {
	                    Date modified = mDateFormat.parse((String) track.get("Date Modified"));
	                    if (!audio.getDateModified().equals(modified))
	                    	audio.setDateModified(modified);
	                    else {
	                        return;
	                    }
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }

	            if (track.containsKey("Track ID")) {
	            	audio.setExternalId(externalId);
	            }

	            if (track.containsKey("Name")) {
	            	audio.setTitle(decode((String) track.get("Name")));
	            }

	            if (track.containsKey("Artist")) {
	            	audio.setArtist(decode((String) track.get("Artist")));
	            }

	            if (track.containsKey("Album")) {
	            	audio.setAlbum(decode((String) track.get("Album")));
	            }

	            if (track.containsKey("Genre")) {
	            	audio.setGenre(decode((String) track.get("Genre")));
	            }
	            try {

	                if (track.containsKey("Size")) {
	                	audio.setSize(new Integer((String) track.get("Size")).intValue());
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Total Time")) {
	                	audio.setDuration((new Integer((String) track.get("Total Time")).intValue()));
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Rating")) {
	                	audio.setRating(new Integer((String) track.get("Rating")).intValue()/20);
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Track Number")) {
	                	audio.setTrack(new Integer((String) track.get("Track Number")).intValue());
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Year")) {
	                	audio.setDate(new Integer((String) track.get("Year")).intValue());
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Date Added")) {
	                	audio.setDateAdded(mDateFormat.parse((String) track.get("Date Added")));
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Bit Rate")) {
	                	audio.setBitRate(new Integer((String) track.get("Bit Rate")).intValue());
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Sample Rate")) {
	                	audio.setSampleRate(new Integer((String) track.get("Sample Rate")).intValue());
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }

	            if (track.containsKey("Comments")) {
	            	audio.setComments((String) track.get("Comments"));
	            }
	            try {

	                if (track.containsKey("Play Count")) {
	                	audio.setPlayCount(new Integer((String) track.get("Play Count")).intValue());
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	            try {

	                if (track.containsKey("Play Date UTC")) {
	                	audio.setDatePlayed(mDateFormat.parse((String) track.get("Play Date UTC")));
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }

	            audio.setOrigen("iTunes");
	            try {
	                if (audio.getId()==null) {
	                    AudioManager.createAudio(audio);
	                } 
	                else
	                {
	                    AudioManager.updateAudio(audio);
	                }
	            } catch (Exception ex) {
	                Tools.logException(PlaylistParser.class, ex);
	            }
	        }
	    }

	    private String decode(String value) {
	        try {
	            return Tools.unEscapeXMLChars(URLDecoder.decode(value, "UTF-8"));
	        } catch (Exception ex) {
	        }
	        return value;
	    }	    
	    
	    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	    private boolean mFoundTracks;

	    private boolean mFoundPlaylists;

	    private int mDictLevel;

	    private boolean mInKey;

	    private StringBuffer mKey;

	    private boolean mInValue;

	    private String mLastTag;

	    private String mType;

	    private StringBuffer mValue;

	    private HashMap mTracks;
	    
	    private int mCounter;
    }
    
    class ListParser extends DefaultHandler
    {
    	public ListParser(List playlists)
    	{
    		mCurrentPlaylists = playlists;
    		mKey = new StringBuffer(100);
    		mValue = new StringBuffer(100);
    	}
    	
	    /**
	     * Method used by SAX at the start of the XML document parsing.
	     */
	    public void startDocument() {
	    }
	
	    /**
	     * SAX XML parser method. Start of an element.
	     * 
	     * @param namespaceURI
	     *            namespace URI this element is associated with, or an empty String
	     * @param localName
	     *            name of element (with no namespace prefix, if one is present)
	     * @param qName
	     *            XML 1.0 version of element name: [namespace prefix]:[localName]
	     * @param attributes
	     *            Attributes for this element
	     */
	
	    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
	            throws SAXException {
	        if (localName.equals(DICT)) {
	            mDictLevel = mDictLevel + 1;
	        } else if (localName.equals(KEY)) {
	            mInKey = true;
	            mKey.setLength(0);
	        }
	
	        if (mLastTag != null && mLastTag.equals(KEY) && !localName.equals(KEY) && !localName.equals(DICT)) {
	            mInValue = true;
	            mValue.setLength(0);
	            mType = localName;
	        }
	    }
	
	    public void characters(char[] ch, int start, int length) throws SAXException {
	        if (mInKey) {
                mKey.append(new String(ch, start, length).trim());
	        } else if (mInValue) {
                mValue.append(new String(ch, start, length).trim());
	        }
	    }
	
	    /**
	     * SAX XML parser method. End of an element.
	     * 
	     * @param namespaceURI
	     *            namespace URI this element is associated with, or an empty String
	     * @param localName
	     *            name of element (with no namespace prefix, if one is present)
	     * @param qName
	     *            XML 1.0 version of element name: [namespace prefix]:[localName]
	     * @param attributes
	     *            Attributes for this element
	     */
	
	    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	    	mLastTag = localName;
	
	        if (mDictLevel == 1 && localName.equals(KEY) && same(mKey, "Tracks")) {
	            mFoundPlaylists = false;
	        } else if (mDictLevel == 1 && localName.equals(KEY) && same(mKey, "Playlists")) {
	            mFoundPlaylists = true;
	        }
	
	        if (localName.equals(DICT)) {
	            mDictLevel = mDictLevel - 1;
	        } else if (localName.equals(KEY)) {
	            mInKey = false;
	        } else if (mType != null && localName.equals(mType)) {
	            if (mFoundPlaylists) {
	                if (mDictLevel == 2 && same(mKey, "Name")) {
	                	if (mValue != null) {
	                        mPlaylist = mValue.toString();
	                    }
	                } 
	                else
                	if (mDictLevel == 2 && mKey != null && same(mKey, "Playlist ID")) {
                		String value = mValue.toString();
                		
                		mPlaylistId = value;
                		mCurrentPlaylists.add(value);
                		
                		try {
                    		boolean found = false;
                    		
                    		List list = PlaylistsManager.findByExternalId(mPlaylistId);
                    		if (list != null && list.size()>0)
                    		{
                    			Playlists playlists = (Playlists)list.get(0);
                    			PlaylistsManager.deletePlaylistsTracks(playlists);
                    			found = true;
                    			list.clear();
                    		}
                            
                            if (!found)
                            {
                            	try {
                                	Playlists playlist = new Playlists(mPlaylist, new Date(), new Date(), new Date(), 0, "iTunes",
                                			mPlaylistId);
	                                PlaylistsManager.createPlaylists(playlist);
            	                } catch (Exception ex) {
            	                    Tools.logException(PlaylistParser.class, ex);
            	                }
                            }
                    		
                            log.info("Processing Playlist: "+mPlaylist);
                        } catch (Exception ex) {
                            Tools.logException(PlaylistParser.class, ex);
                        }
	                } 
                	else 	                	
                	if (mDictLevel == 3 && mKey != null && same(mKey, "Track ID")) {
                		try {
                			List list = AudioManager.findByExternalId(mValue.toString());
                            if (list != null && list.size() > 0) {
                                Audio audio = (Audio) list.get(0);
                                if (audio != null && mPlaylist != null) {
                                	if (!audio.getPath().startsWith("http"))
                                    {
                                    	File file = new File(audio.getPath());
                                    	if (!file.exists())
                                    	{
                                    		return;
                                    	}
                                    }
                                	
                                	List plist = PlaylistsManager.findByExternalId(mPlaylistId);
                            		if (plist != null && plist.size()>0)
                            		{
                            			Playlists playlists = (Playlists)plist.get(0);
                            			PlaylistsTracksManager.createPlaylistsTracks(new PlaylistsTracks(playlists.getId(), audio.getId()));
                            			plist.clear();
                            		}
                                	
        	                        try {
        	                            Thread.sleep(50); // give the CPU some breathing time
        	                        } catch (Exception ex) {
        	                        }
                                }
                                list.clear();
                            }
                        } catch (Exception ex) {
                            Tools.logException(PlaylistParser.class, ex);
                        }
	                }
	            }
	            mInValue = false;
	        }
	    }
	
	    /**
	     * Method used by SAX at the end of the XML document parsing.
	     */
	
	    public void endDocument() {
	    }
	
	    /**
	     * Receive notification of a parser warning.
	     * 
	     * <p>
	     * The default implementation does nothing. Application writers may override this method in a subclass to take
	     * specific actions for each warning, such as inserting the message in a log file or printing it to the console.
	     * </p>
	     * 
	     * @param e
	     *            The warning information encoded as an exception.
	     * @exception org.xml.sax.SAXException
	     *                Any SAX exception, possibly wrapping another exception.
	     * @see org.xml.sax.ErrorHandler#warning
	     * @see org.xml.sax.SAXParseException
	     */
	    public void warning(SAXParseException e) throws SAXException {
	        throw e;
	    }
	
	    /**
	     * Receive notification of a recoverable parser error.
	     * 
	     * <p>
	     * The default implementation does nothing. Application writers may override this method in a subclass to take
	     * specific actions for each error, such as inserting the message in a log file or printing it to the console.
	     * </p>
	     * 
	     * @param e
	     *            The warning information encoded as an exception.
	     * @exception org.xml.sax.SAXException
	     *                Any SAX exception, possibly wrapping another exception.
	     * @see org.xml.sax.ErrorHandler#warning
	     * @see org.xml.sax.SAXParseException
	     */
	    public void error(SAXParseException e) throws SAXException {
	        throw e;
	    }
	
	    /**
	     * Report a fatal XML parsing error.
	     * 
	     * <p>
	     * The default implementation throws a SAXParseException. Application writers may override this method in a subclass
	     * if they need to take specific actions for each fatal error (such as collecting all of the errors into a single
	     * report): in any case, the application must stop all regular processing when this method is invoked, since the
	     * document is no longer reliable, and the parser may no longer report parsing events.
	     * </p>
	     * 
	     * @param e
	     *            The error information encoded as an exception.
	     * @exception org.xml.sax.SAXException
	     *                Any SAX exception, possibly wrapping another exception.
	     * @see org.xml.sax.ErrorHandler#fatalError
	     * @see org.xml.sax.SAXParseException
	     */
	    public void fatalError(SAXParseException e) throws SAXException {
	        throw e;
	    }
	    
	    private boolean mFoundPlaylists;

	    private int mDictLevel;

	    private boolean mInKey;

	    private StringBuffer mKey;

	    private boolean mInValue;

	    private String mLastTag;

	    private String mType;

	    private StringBuffer mValue;

   	    private String mPlaylist;
   	    
   	    private String mPlaylistId;
   	    
	    private List mCurrentPlaylists;
	    
	    private int mCounter;
    }
    
    private static boolean same(StringBuffer buffer, String value)
    {
    	if (buffer.length()==value.length())
    	{
	    	for (int i=0;i < buffer.length();i++)
	    	{
	    		if (value.charAt(i)!=buffer.charAt(i))
	    			return false;
	    	}
	    	return true;
    	}
    	return false;
    }
}