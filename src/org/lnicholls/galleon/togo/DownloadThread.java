package org.lnicholls.galleon.togo;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import net.sf.hibernate.HibernateException;

import org.apache.log4j.Logger;

import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.database.VideoManager;

public class DownloadThread extends Thread implements Constants {
    private static Logger log = Logger.getLogger(DownloadThread.class.getName());

    public DownloadThread(Server server) throws IOException {
        super("DownloadThread");
        mServer = server;
        setPriority(Thread.MIN_PRIORITY);

        mToGo = new ToGo();
    }

    public void run() {
        while (true) {
            try {
            	if (mSelectedVideo == null)
                    mSelectedVideo = mToGo.pickNextVideoForDownloading();
                
            	if (mSelectedVideo != null) {

                    if (log.isDebugEnabled())
                        log.debug("Picked: " + mSelectedVideo);

                    mDownloading = true;
                    synchronized (this) {
                        mSelectedVideo.setStatus(Video.STATUS_DOWNLOADING);
                        mSelectedVideo.setDownloadSize(0);
                        mSelectedVideo.setDownloadTime(0);
                        VideoManager.updateVideo(mSelectedVideo);
                    }
                    synchronized (this) {
                        notifyAll();
                    }
                    mCancelThread = new CancelThread(mSelectedVideo);
                    mCancelThread.start();
                    boolean success = mToGo.Download(mSelectedVideo, mCancelThread);
                    if (mCancelThread.isAlive()) {
                        mCancelThread.interrupt();
                    }
                    if (success) {
                        if (!mCancelThread.cancel()) {
                            synchronized (this) {
                                mSelectedVideo = VideoManager.retrieveVideo(mSelectedVideo.getId());
                                mSelectedVideo.setStatus(Video.STATUS_DOWNLOADED);
                                mSelectedVideo.setDateDownloaded(new Date());
                                VideoManager.updateVideo(mSelectedVideo);
                            }
                        }
                    }
                    else
                    {
                    	synchronized (this) {
                            mSelectedVideo = VideoManager.retrieveVideo(mSelectedVideo.getId());
                            mSelectedVideo.setStatus(Video.STATUS_INCOMPLETE);
                            VideoManager.updateVideo(mSelectedVideo);
                        }
                    	sleep(1000 * 30);
                    }
                }
                else
                	sleep(1000 * 30);
                mSelectedVideo = null;
            } catch (InterruptedException ex) {
            } // handle silently for waking up
            catch (Exception ex2) {
                Tools.logException(ToGoThread.class, ex2);
            } finally {
                if (mCancelThread != null && mCancelThread.isAlive()) {
                    mCancelThread.interrupt();
                }
                mCancelThread = null;
                mDownloading = false;
            }
        }
    }

    public void updateVideo(Video video) {
        if (video.getStatus() == Video.STATUS_USER_SELECTED) {
            if (!mDownloading) {
                mSelectedVideo = mToGo.pickNextVideoForDownloading();
                this.interrupt();
                synchronized (this) {
                    try {
                        wait();
                    } catch (Exception ex) {
                    }
                }
            }
        } else if (video.getStatus() == Video.STATUS_USER_CANCELLED) {
            if (mDownloading && mSelectedVideo.getId().equals(video.getId())) {
                if (mCancelThread != null && mCancelThread.isAlive()) {
                    mCancelThread.setCancel(true);
                    mCancelThread.interrupt();
                }
            }
        }
    }
    
    public void interrupt()
    {
        synchronized (this)
        {
            super.interrupt();
        }
    }

    class CancelThread extends Thread implements CancelDownload {
        public CancelThread(Video video) throws IOException {
            super("CancelThread");
            setPriority(Thread.MIN_PRIORITY);
            mVideo = video;
            mCancel = false;
        }

        public void run() {
            while (!mCancel) {
                try {
                    synchronized(this)
                    {
                        mVideo = VideoManager.retrieveVideo(mVideo.getId());
    
                        if (mVideo.getStatus() == Video.STATUS_USER_CANCELLED) {
                            log.info("Download cancelled by user: " + mVideo.getTitle());
                            mCancel = true;
                            break;
                        }
                    }
                    sleep(1000 * 30 * 1);
                } catch (InterruptedException ex) {
                    return;
                } // handle silently for waking up
                catch (Exception ex2) {
                    Tools.logException(CancelThread.class, ex2);
                }
            }
        }

        public boolean cancel() {
            return mCancel;
        }

        public void setCancel(boolean cancel) {
            mCancel = cancel;
        }
        
        public void interrupt()
        {
            synchronized (this)
            {
                super.interrupt();
            }
        }

        private Video mVideo;

        private boolean mCancel;
    }

    private Server mServer;

    private ToGo mToGo;

    private Video mSelectedVideo;

    private boolean mDownloading;

    private CancelThread mCancelThread;
}