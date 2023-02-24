package org.lnicholls.galleon.media;

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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javazoom.spi.mpeg.sampled.file.tag.IcyInputStream;
import javazoom.spi.mpeg.sampled.file.tag.MP3Tag;
import javazoom.spi.mpeg.sampled.file.tag.TagParseEvent;
import javazoom.spi.mpeg.sampled.file.tag.TagParseListener;

import net.sf.hibernate.HibernateException;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.widget.DefaultApplication;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.TimedCallable;

public final class Mp3Url {
    private static final Logger log = Logger.getLogger(Mp3Url.class.getName());

    public static final Audio getAudio(String url) {
        Audio audio = new Audio();
        Mp3File.defaultProperties(audio);
        audio.setPath(url);
        audio.setDuration(-1);
        return audio;
    }

    public static InputStream getStream(String uri) throws IOException {
        return getStream(uri, null);
    }

    public static InputStream getStream(String uri, DefaultApplication application) throws IOException {
        if (uri.toLowerCase().endsWith(".http.mp3")) {
            log.debug("getStream: " + uri + ", " + application);

            boolean hasmore = false;
            int counter = 1;

            try
            {
	            String id = Tools.extractName(Tools.extractName(uri));
	            Audio audio = AudioManager.retrieveAudio(Integer.valueOf(id));
	            do
	            {
	            	log.debug("getStream: audio=" + audio.getPath());
	            	try {
		                class TimedThread implements Callable {
		                    private DefaultApplication mApplication = null;
		
		                    private String mPath = null;
		
		                    public TimedThread(String path, DefaultApplication application) {
		                        mPath = path;
		                        mApplication = application;
		                    }
		
		                    public synchronized java.lang.Object call() throws java.lang.Exception {
		                        //URL url = new URL("http://64.236.34.4:80/stream/1065");
		                        URL url = new URL(mPath);
		                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		                        conn.setRequestProperty("Icy-Metadata", "1");
		                        conn.setRequestProperty("User-Agent", "WinampMPEG/5.0");
		                        conn.setRequestProperty("Accept", "audio/mpeg");
		                        conn.setInstanceFollowRedirects(true);
		
		                        try
		                        {
			                        //IcyInputStream input = new IcyInputStream(new URLStream(conn.getInputStream(), conn.getContentLength(), mApplication));
		                        	IcyInputStream input = new IcyInputStream(conn.getInputStream());
			                        final IcyListener icyListener = new IcyListener(mApplication);
			                        input.addTagParseListener(icyListener);
			                        return input;
		                        }
		                        catch (Throwable ex)
		                        {
		                        	Tools.logException(Mp3Url.class, ex, mPath);
		                        }
		                        return conn.getInputStream();
		                    }
		                }
		
		            	TimedThread timedThread = new TimedThread(audio.getPath(), application);
		            	TimedCallable timedCallable = new TimedCallable(timedThread, 1000 * 10);
		            	InputStream mp3Stream = (InputStream) timedCallable.call();
		
		            	if (mp3Stream != null)
		                    return new BufferedInputStream(mp3Stream, 1024*100);
		            } catch (Exception ex) {
		                if (audio!=null && counter < 5)
		                {
			                try
			                {
			                	// Hack to support Shoutcast backup servers
			                	List list = AudioManager.findByTitleOrigenGenreExternalId(audio.getTitle(), audio.getOrigen(), audio.getGenre(), String.valueOf(++counter));
			                	if (list!=null && list.size()>0)
			                	{
			                		audio = (Audio)list.get(0);
			                		log.debug("Trying alternate: " + audio.getPath());
			                		hasmore = true;
			                	}
		                	} catch (Exception ex2) {
		    	                Tools.logException(Mp3Url.class, ex, uri);
		                	}
		                }
		                else
		                	Tools.logException(Mp3Url.class, ex, uri);
		            }
	            }
	            while (hasmore);
            } catch (Exception ex) {
                Tools.logException(Mp3Url.class, ex, uri);
        	}
        }
        return Mp3Url.class.getResourceAsStream("/couldnotconnect.mp3");
    }

    private static final class URLStream extends FilterInputStream {
        URLStream(InputStream in, long contentLength, DefaultApplication application) {
            super(in);
            mContentLength = contentLength;
            mApplication = application;
        }

        public int available() {
            return (int) mContentLength;
        }

        public int read() throws IOException {
            mContentLength -= 1;
            return in.read();
        }

        public int read(byte b[], int off, int length) throws IOException {
            // Ugly hack to detect tight loop because TiVo didnt close the stream properly
        	if (mApplication!=null && mApplication.getContext()==null) {
                    close();
            } else if (System.currentTimeMillis() - mTime == 0 ) {
                if (mCounter++ == 10)
                    close();
            } else
                mCounter = 0;
            mTime = System.currentTimeMillis();
            int n = super.read(b, off, length);
            if (n > 0) {
                mContentLength -= n;
            }
            return n;
        }

        public long skip(long n) throws IOException {
            n = super.skip(n);
            if (n > 0) {
                mContentLength -= n;
            }
            return n;
        }

        public void close() throws IOException {
            super.close();
        }

        private long mContentLength;

        private long mTime = System.currentTimeMillis();

        private int mCounter;
        private DefaultApplication mApplication;
    }

    public static class IcyListener implements TagParseListener {
        public IcyListener(DefaultApplication application) {
            mApplication = application;
        }

        public void tagParsed(TagParseEvent tpe) {
        	try
        	{
	            mLastTag = tpe.getTag();
	            String name = mLastTag.getName();
	            log.debug("tagParsed=" + name + "=" + mLastTag.getValue());
	            if ((name != null) && (name.equalsIgnoreCase("streamtitle"))) {
	                mLastTitle = (String) mLastTag.getValue();
	            } else if ((name != null) && (name.equalsIgnoreCase("streamurl"))) {
	                mLastUrl = (String) mLastTag.getValue();
	            }
	
	            mApplication.getPlayer().setTitle(mLastTitle);
        	}
        	catch (Throwable th) {
        		Tools.logException(Mp3Url.class, th);
        	}
        }

        public MP3Tag getLastTag() {
            return mLastTag;
        }

        public void setLastTag(MP3Tag tag) {
            mLastTag = tag;
        }

        public String getTitle() {
            return mLastTitle;
        }

        public String getUrl() {
            return mLastUrl;
        }

        public void setTitle(String string) {
            mLastTitle = string;
        }

        public void setUrl(String string) {
            mLastUrl = string;
        }

        private DefaultApplication mApplication;

        private MP3Tag mLastTag = null;

        private String mLastTitle = null;

        private String mLastUrl = null;
    }
}