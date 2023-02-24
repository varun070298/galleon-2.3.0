package org.lnicholls.galleon.widget;

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

import java.awt.Color;
import java.awt.Image;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.media.Mp3File;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.Yahoo;
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public class MusicInfo extends BView {

    private static Logger log = Logger.getLogger(MusicInfo.class.getName());

    public MusicInfo(BView parent, int x, int y, int width, int height, boolean visible) {
        this(parent, x, y, width, height, visible, false);
    }

    public MusicInfo(BView parent, int x, int y, int width, int height, boolean visible, boolean webImages) {
        super(parent, x, y, width, height, visible);

        mWebImages = webImages;

        mTimeFormatShort = new SimpleDateFormat();
        mTimeFormatShort.applyPattern("mm:ss");

        mTimeFormatLong = new SimpleDateFormat();
        mTimeFormatLong.applyPattern("h:mm:ss");

        int start = 0;

        mCover = new BView(this, this.getWidth() - 210, 130, 200, 200, false);
        mCover.setResource(Color.BLACK);
        mCover.setTransparency(0.75f);

        mTitleText = new BText(this, 0, start, this.getWidth(), 70);
        mTitleText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
        mTitleText.setFont("system-24-bold.font"); //30
        //mTitleText.setColor(Color.CYAN);
        mTitleText.setColor(new Color(254, 178, 0));
        mTitleText.setShadow(true);

        start += 70;

        mSongText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mSongText.setFlags(RSRC_HALIGN_LEFT | RSRC_VALIGN_TOP);
        mSongText.setFont("default-18-bold.font");
        mSongText.setShadow(true);

        mTrackText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mTrackText.setFlags(RSRC_HALIGN_RIGHT | RSRC_VALIGN_TOP);
        mTrackText.setFont("default-18-bold.font");
        mTrackText.setShadow(true);

        start += 20;

        mAlbumText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mAlbumText.setFlags(RSRC_HALIGN_LEFT | RSRC_VALIGN_TOP);
        mAlbumText.setFont("default-18-bold.font");
        mAlbumText.setShadow(true);

        mYearText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mYearText.setFlags(RSRC_HALIGN_RIGHT | RSRC_VALIGN_TOP);
        mYearText.setFont("default-18-bold.font");
        mYearText.setShadow(true);

        start += 20;

        mArtistText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mArtistText.setFlags(RSRC_HALIGN_LEFT | RSRC_VALIGN_TOP);
        mArtistText.setFont("default-18-bold.font");
        mArtistText.setShadow(true);

        mGenreText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mGenreText.setFlags(RSRC_HALIGN_RIGHT | RSRC_VALIGN_TOP);
        mGenreText.setFont("default-18-bold.font");
        mGenreText.setShadow(true);

        start += 20;

        mDurationText = new LabelText(this, 0, start, this.getWidth(), 20, true);
        mDurationText.setFlags(RSRC_HALIGN_LEFT | RSRC_VALIGN_TOP);
        mDurationText.setFont("default-18-bold.font");
        mDurationText.setShadow(true);

        mStars = new BView[5];
        for (int i = 0; i < 5; i++) {
            mStars[i] = new BView(this, -34, 160, 34, 34, true);
            mStars[i].setResource(((DefaultApplication) getApp()).getStarIcon(), RSRC_IMAGE_BESTFIT);
            mStars[i].setTransparency(0.6f);
            mStars[i].setLocation(0 + (i * 40), 160, mAnim);
        }
    }

    public Audio getAudio() {
        return mAudio;
    }

    public void setAudio(final Audio audio) {
        setAudio(audio, audio.getTitle());
    }

    public synchronized void setAudio(final Audio audio, String title) {
        if (audio != null) {
            try {
                setPainting(false);
                mTitleText.setValue(Tools.trim(Tools.clean(title), 100));
                String song = audio.getTitle();
                if (song.equals(Mp3File.DEFAULT_ARTIST))
                    song = title;
                if (audio.getPath().startsWith("http")) {
                    //mSongText.setValue("Stream: " + Tools.trim(song, 80));
                    mSongText.setLabel("Stream:");
                    mSongText.setValue(Tools.trim(song, 80));
                    mTrackText.setVisible(false);
                    mDurationText.setVisible(false);
                    mAlbumText.setVisible(false);
                    mYearText.setVisible(false);
                    mArtistText.setVisible(false);
                    mGenreText.setVisible(false);

                    if (audio.getTitle().equals(Mp3File.DEFAULT_ARTIST)) {
                        try {
                            audio.setTitle(title);
                            AudioManager.updateAudio(audio);
                        } catch (Exception ex) {
                            Tools.logException(MusicInfo.class, ex);
                        }
                    }
                } else {
                    mSongText.setLabel("Title:");
                    mSongText.setValue(Tools.trim(song, 45));
                    if (audio.getOrigen()!=null && audio.getOrigen().equals("Podcast"))
                    	mTrackText.setVisible(false);
                    else
                    {
                    	mTrackText.setLabel("Track:");
                    	mTrackText.setValue(String.valueOf(audio.getTrack()));
                    	mTrackText.setVisible(true);
                    }
                    mDurationText.setLabel("Duration:");
                    if (audio.getDuration()>1000*60*60)
                    	mDurationText.setValue(mTimeFormatLong.format(new Date(audio.getDuration())));
                    else
                    	mDurationText.setValue(mTimeFormatShort.format(new Date(audio.getDuration())));
                    mAlbumText.setLabel("Album:");
                    mAlbumText.setValue(Tools.trim(audio.getAlbum(), 40));
                    mYearText.setLabel("Year:");
                    if (audio.getDate()==0)
                    	mYearText.setValue("None");
                    else
                    	mYearText.setValue(String.valueOf(audio.getDate()));
                    mArtistText.setLabel("Artist:");
                    mArtistText.setValue(Tools.trim(audio.getArtist(), 40));
                    mGenreText.setLabel("Genre:");
                    mGenreText.setValue(audio.getGenre());
                    mDurationText.setVisible(true);
                    mAlbumText.setVisible(true);
                    mYearText.setVisible(true);
                    mArtistText.setVisible(true);
                    mGenreText.setVisible(true);
                }

                setRating(audio);

                if (mAudio==null || !mAudio.getId().equals(audio.getId()))
                {
                	mAudio = audio;

                	if (mCoverThread==null)
                    {
                        mCoverThread = new Thread() {
                        	public void run() {
                                int x = mCover.getX();
                                int y = mCover.getY();

                                Audio audio = null;
                                while (getApp().getContext()!=null)
                                {
                                	if (mAudio!=null && !mAudio.getPath().startsWith("http")) {
                                		if (audio==null || !audio.getId().equals(mAudio.getId()))
                                		{
    	                            		try {
    	            		                    MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
    	            		                            .getMusicPlayerConfiguration();
    	            		                    java.awt.Image image = Mp3File.getCover(mAudio, musicPlayerConfiguration
    	            		                            .isUseAmazon(), musicPlayerConfiguration.isUseFile());
    	            		                    if (image != null) {
    	            		                        synchronized (this) {
    	            		                            mCover.setResource(createImage(image), RSRC_IMAGE_BESTFIT);
    	            		                            mCover.setVisible(true);
    	            		                            mCover.setTransparency(0.0f, mAnim);
    	            		                            getBApp().flush();
    	            		                        }
    	            		                    } else {
    	            		                        synchronized (this) {
    	            		                        	mCover.setVisible(false);    	            		                        	mCover.flush();    	            		                        	mCover.clearResource();
    	            		                            getBApp().flush();
    	            		                        }
    	            		                    }
    	            		                    audio = mAudio;
    	            		                } catch (Exception ex) {
    	            		                    Tools.logException(MusicInfo.class, ex, "Could not retrieve cover");
    	            		                }

    	            		                while (mWebImages & getApp().getContext() != null) {
    	            		                    try {
    	            		                        if (mCover.getResource() != null)
    	            		                        	synchronized(this)
    	            		                        	{
    	            		                        		wait(10000);
    	            		                        	}

    	            		                        if (!audio.getArtist().equals(mAudio.getArtist()))
    	            		                        {
    	            		                        	if (mResults != null) {
    	            		                                mResults.clear();
    	            		                                mResults = null;
    	            		                            }

    	            		                        	mCover.setVisible(false);
    	            		                            if (mCover.getResource() != null)    	            		                            {    	            		                            	mCover.getResource().flush();
    	            		                                mCover.getResource().remove();    	            		                            }

    	            		                            break;
    	            		                        }

    	            		                        if (mResults == null) {
    	            		                            if (mResults != null) {
    	            		                                mResults.clear();
    	            		                                mResults = null;
    	            		                            }

    	            		                            mResults = Yahoo.getImages("\"" + audio.getArtist() + "\" music");
    	            		                            if (mResults.size() == 0) {
    	            		                                mResults = null;
    	            		                                return;
    	            		                            }
    	            		                            mPos = 0;
    	            		                        }

    	            		                        NameValue nameValue = (NameValue) mResults.get(mPos);
    	            		                        Image image = Tools.getImage(new URL(nameValue.getValue()), -1, -1);

    	            		                        if (image != null) {
    	            		                                setPainting(false);
    	            		                                try {    	            		                                	if (getApp().getContext()!=null)    	            		                                    {
	    	            		                                    if (mCover.getResource() != null)	    	            		                                    {	    	            		                                    	mCover.getResource().flush();
	    	            		                                        mCover.getResource().remove();	    	            		                                    }
	    	            		                                    //mUrlText.setValue(nameValue.getName());
	    	            		                                    mCover.setLocation(x + mCover.getWidth(), y);
	    	            		                                    mCover.setVisible(true);
	    	            		                                    mCover.setTransparency(1f);	    	            		                                    mCover.setResource(createImage(image), RSRC_IMAGE_BESTFIT);
	    	            		                                    Resource resource = getResource("*1000");
	    	            		                                    mCover.setTransparency(0f, resource);
	    	            		                                    mCover.setLocation(x, y, resource);    	            		                                    }
    	            		                                    image.flush();
    	            		                                    image = null;
    	            		                                } finally {
    	            		                                    setPainting(true);
    	            		                                }
    	            		                        } else {
    	            		                            mResults.remove(mPos);
    	            		                        }

    	            		                        mPos = (mPos + 1) % mResults.size();

    	            		                    } catch (InterruptedException ex) {
    	            		                        return;
    	            		                    } catch (Exception ex) {
    	            		                        Tools.logException(MusicInfo.class, ex, "Could not retrieve web image");
    	            		                        try {
    	            		                            mResults.remove(mPos);
    	            		                        } catch (Throwable ex2) {
    	            		                        }
    	            		                    } finally {
    	            		                        getBApp().flush();
    	            		                    }
    	            		                }
                                		}
                                		else
                                    	{
                                			try
                    	                	{
                    		                	synchronized(this)
                                            	{
                                            		wait(10000);
                                            	}
                    	                	} catch (InterruptedException ex) {
                    	                        return;
                    	                	}
                                    	}
                	                }
                                	else
                                	{
    		                            try
                	                	{
                		                	synchronized(this)
                                        	{
                		                		mCover.setVisible(false);
            		                            if (mCover.getResource() != null)            		                            {            		                            	mCover.getResource().flush();
            		                                mCover.getResource().remove();            		                            }
            		                            getBApp().flush();

                                        		wait(10000);
                                        	}
                	                	} catch (InterruptedException ex) {
                	                        return;
                	                	}
                                	}
                                }
                            }
                        };
                        mCoverThread.start();
                    }

                    synchronized(mCoverThread)
	                {
	                	mCoverThread.notifyAll();
	                }
                }
            } catch (Throwable ex) {
                Tools.logException(MusicInfo.class, ex, "Could not retrieve web image");
            } finally {
                setPainting(true);
            }
        }
    }

    public void setTitle(String value) {
        try {
            setPainting(false);
            mTitleText.setValue(value);
        } finally {
            setPainting(true);
        }
    }

    public void clearResource() {
        clearCover();

        if (mResults != null) {
            mResults.clear();
            mResults = null;
        }

        super.clearResource();
    }

    private void clearCover() {
        setPainting(false);
        try {
                if (mCoverThread != null && mCoverThread.isAlive()) {
                    mCoverThread.interrupt();
                    mCoverThread.stop();
                    mCoverThread = null;

                    /*
                     * if (mResults != null) { mResults.clear(); mResults = null; }
                     */
    /*
                    mCover.setVisible(false);
                    if (mCover.getResource() != null)
                        mCover.getResource().remove();
*/
            }
        } finally {
            setPainting(true);
        }
    }

    private void setRating(Audio audio) {
        if (audio != null) {
            for (int i = 0; i < 5; i++) {
                if (i < audio.getRating())
                    mStars[i].setTransparency(0.0f);
                else
                    mStars[i].setTransparency(0.6f);
            }
        }
    }

    public boolean handleKeyPress(int code, long rawcode) {
        switch (code) {
        case KEY_THUMBSDOWN:
        	DefaultApplication application = (DefaultApplication)getApp();
			if (!application.isDemoMode())
			{
	        	if (mAudio != null && mAudio.getRating() > 0) {
	                getBApp().play("thumbsdown.snd");
	                getBApp().flush();
	                try {
	                    mAudio.setRating(Math.max(mAudio.getRating() - 1, 0));
	                    AudioManager.updateAudio(mAudio);
	                } catch (Exception ex) {
	                    Tools.logException(MusicInfo.class, ex);
	                }
	                setRating(mAudio);
	            } else {
	                getBApp().play("bonk.snd");
	                getBApp().flush();
	            }
			}
            return true;
        case KEY_THUMBSUP:
        	application = (DefaultApplication)getApp();
			if (!application.isDemoMode())
			{
	            if (mAudio != null && mAudio.getRating() < 5) {
	                getBApp().play("thumbsup.snd");
	                getBApp().flush();
	                try {
	                    mAudio.setRating(Math.min(mAudio.getRating() + 1, 5));
	                    AudioManager.updateAudio(mAudio);
	                } catch (Exception ex) {
	                    Tools.logException(MusicInfo.class, ex);
	                }
	                setRating(mAudio);
	            } else {
	                getBApp().play("bonk.snd");
	                getBApp().flush();
	            }
			}
            return true;
        }
        return super.handleKeyPress(code, rawcode);
    }

    private Audio mAudio;

    private SimpleDateFormat mTimeFormatShort;

    private SimpleDateFormat mTimeFormatLong;

    private Resource mAnim = getResource("*2000");

    private BView mCover;

    private BText mTitleText;

    private LabelText mSongText;

    private LabelText mTrackText;

    private LabelText mArtistText;

    private LabelText mAlbumText;

    private LabelText mDurationText;

    private LabelText mYearText;

    private LabelText mGenreText;

    private Tracker mTracker;

    private BView[] mStars;

    private Thread mCoverThread;

    private List mResults;

    private int mPos;

    private boolean mWebImages;
}