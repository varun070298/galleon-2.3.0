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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.FileFilters;

/*
 * Playlists of audio files and URLs.
 */

public abstract class Playlist implements Media {
    private static Logger log = Logger.getLogger(Playlist.class.getName());

    public Playlist(String path) {
        mPath = path;
        mItems = new ArrayList();
    }

    public File getFile(File playlist, String path) {
        if (log.isDebugEnabled())
            log.debug("getFile(): playlist=" + playlist.getAbsolutePath() + " path=" + path);
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(playlist.getParent(), path);
        }
        if (file.exists()) {
            return file;
        }

        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            try {
                if (playlist.getAbsolutePath().toLowerCase().startsWith(roots[i].getAbsolutePath().toLowerCase())) {
                    file = new File(roots[i], path);
                    if (file.exists()) {
                        return file;
                    }
                    break;
                }
            } catch (Exception ex) {
                break;
            }
        }

        return null;
    }

    public static boolean isPlaylist(String filename) {
    	File file = new File(filename);
    	return file.exists() && FileFilters.playlistFilter.accept(file);
    }

    public String getTitle() {
        File file = new File(mPath);
        return file.getName();
    }

    public String getPath() {
        return mPath;
    }

    public Date getDateModified() {
        return new Date();
    }

    public String getMimeType() {
        return "playlist";
    }

    public List getList() {
        return mItems;
    }

    protected String mPath;

    protected ArrayList mItems;
}