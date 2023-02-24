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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Iterator;

import net.sf.hibernate.Session;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.util.FileGatherer;
import org.lnicholls.galleon.util.Tools;

public class MediaRefreshThread extends Thread {
    private static Logger log = Logger.getLogger(MediaRefreshThread.class.getName());

    public MediaRefreshThread(String context) throws IOException {
        super("MediaRefreshThread");
        setPriority(Thread.MIN_PRIORITY);
        mContext = context;
        mPaths = new ArrayList();
    }

    public void run() {
        try {
            if (log.isDebugEnabled())
                log.debug("MediaRefreshThread start");

            for (int i = 0; i < mPaths.size(); i++)
                refresh((PathInfo) mPaths.get(i));

            if (log.isDebugEnabled())
                log.debug("MediaRefreshThread end");
            
            PersistentValueManager.savePersistentValue(MediaRefreshThread.class.getName() + "." + mContext, new Date().toGMTString());
        } // handle silently for waking up
        catch (Exception ex) {
            Tools.logException(MediaRefreshThread.class, ex);
        }
    }

    private void refresh(PathInfo pathInfo) {
        log.info("Refreshing media for: " + pathInfo.mPath);
        if (log.isDebugEnabled())
            Tools.logMemory("refresh1");
        long startTime = System.currentTimeMillis();
        // Update existing records and add new records
        FileGatherer.gatherDirectory(new File(pathInfo.mPath), pathInfo.mFilter, true,
                new FileGatherer.GathererCallback() {
        			private int mCounter;
                    public void visit(File file, File originalFile) {
                        synchronized (this) {
                            try {
                                if (!file.isDirectory())
                                {
                                	if (!file.getAbsolutePath().equals(file.getCanonicalPath()))
                                	{
	                                	List list = AudioManager.findByPath(file.getAbsolutePath());
	                                	if (list.size() > 0) {
	                                		Audio audio = (Audio) list.get(0);
	                                		AudioManager.deleteAudio(audio);
	                                	}
                                	}
                                	
                                	List list = AudioManager.findByPath(file.getCanonicalPath());
                                	Thread.yield();
                                    
                                    if (list.size() > 0) {
                                        Audio audio = (Audio) list.get(0);
                                        audio.setPath(file.getCanonicalPath());
                                        Date date = new Date(file.lastModified());
                                        if (date.getTime() > audio.getDateModified().getTime() || audio.getOrigen()==null) {
                                            if (log.isDebugEnabled())
                                                log.debug("Changed: " + file.getCanonicalPath());
                                            audio = (Audio) MediaManager.getMedia(audio, file.getCanonicalPath());
                                            if (audio!=null)
                                            {
                                            	audio.setOrigen("PC");
                                            	AudioManager.updateAudio(audio);
                                            }
                                        }
                                    } else {
                                        if (log.isDebugEnabled())
                                            log.debug("New: " + file.getCanonicalPath());
                                        Audio audio = (Audio) MediaManager.getMedia(file.getCanonicalPath());
                                        if (audio!=null)
                                        {
                                        	audio.setOrigen("PC");
                                        	AudioManager.createAudio(audio);
                                        }
                                    }
                                }
                                if (++mCounter%100==0)
             	         		   System.gc();
                                Thread.sleep(50); // give the CPU some breathing time
                            } catch (Exception ex) {
                                Tools.logException(MediaRefreshThread.class, ex, file.getAbsolutePath());
                            }
                        }
                    }
                });
        // Determine any records that need to be removed
        synchronized (this) {
            try {
                AudioManager.clean();
            } catch (Exception ex) {
                Tools.logException(MediaRefreshThread.class, ex);
            }
        }

        System.gc();

        long estimatedTime = System.currentTimeMillis() - startTime;
        log.info("Refreshing media took " + (estimatedTime / 1000) + " seconds");
        if (log.isDebugEnabled())
            Tools.logMemory("refresh2");
    }
    
    public static class PathInfo {
        public PathInfo(String path, FileFilter filter) {
            mPath = path;
            mFilter = filter;
        }

        private String mPath;

        private FileFilter mFilter;
    }

    public void addPath(PathInfo pathInfo) {
        mPaths.add(pathInfo);
    }

    public void removePath(PathInfo pathInfo) {
        mPaths.remove(pathInfo);
    }

    public void removePaths() {
        mPaths.clear();
    }

    public void interrupt() {
        synchronized (this) {
            super.interrupt();
        }
    }

    private String mContext;
    private ArrayList mPaths;
}