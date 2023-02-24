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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.FileSystemContainer.FileItem;
import org.lnicholls.galleon.util.FileSystemContainer.Item;

/*
 * M3U Playlists of audio files and URLs.
 */

public class M3uPlaylist extends Playlist {
    private static final String M3U_EXTENDED = "#EXTM3U";

    private static final String M3U_ENTRY = "#EXTINF";

    private static Logger log = Logger.getLogger(M3uPlaylist.class.getName());

    public M3uPlaylist(String path) {
        super(path);
        loadMetaData();
    }

    // Delegate method to gather all of the items before applying all of the request criteria
    protected final void loadMetaData() {
        mItems.clear();
        FileInputStream fileInputStream = null;
        try {
            File playlist = new File(mPath);
            fileInputStream = new FileInputStream(playlist);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

            // Check the first line to see if this is an extended m3u playlist
            boolean extended = false;
            String inputLine = reader.readLine();
            if (inputLine == null) {
                log.error("Invalid playlist: " + getPath());
                return;
            }

            if (inputLine.startsWith(M3U_EXTENDED)) {
                extended = true;
                inputLine = reader.readLine();
            }

            // Now read the rest. Extended playlist lines come in pairs...
            String name = "";
            String duration = "";
            do {
                inputLine = inputLine.trim();
                if (inputLine.length() == 0)
                    continue;

                if (extended && inputLine.startsWith(M3U_ENTRY)) {
                    // Format of the line is
                    // #EXTINF:[0-9]+,.+
                    // Where [0-9]+ is the song length in seconds and .+ is the
                    // name of the song.
                    String subLine = inputLine.substring(M3U_ENTRY.length() + 1);
                    int comma = subLine.indexOf(",");

                    if (comma != -1) {
                        duration = subLine.substring(0, comma);
                        name = subLine.substring(comma + 1);
                    } else {
                        // We're a forgiving parser... just skip invalid lines.
                        log.error("Invalid line '" + inputLine + "' in playlist: " + getPath());
                        duration = "";
                        name = "";
                    }
                }
                // All other lines are a uri to a song
                else {
                    // If name wasn't given, name is just the root of the filename
                    if (name.length() == 0) {
                        String normalized = inputLine.replace('\\', '/');
                        name = normalized.substring(normalized.lastIndexOf("/") + 1);
                        if (name.lastIndexOf(".") != -1)
                            name = name.substring(0, name.lastIndexOf("."));
                    }

                    if (log.isDebugEnabled())
                        log.debug("PlaylistItem: " + name + "=" + inputLine);

                    Audio audio = null;

                    // Handle urls
                    if (inputLine.startsWith("http")) {
                        try {
                            List list = AudioManager.findByPath(inputLine);
                            if (list!=null && list.size()>0)
                            {
                                audio = (Audio)list.get(0);
                            }
                        } catch (Exception ex) {
                        }
                        
                        if (audio==null)
                        {
                            try {
                                audio = (Audio) MediaManager.getMedia(inputLine);
                            } catch (Exception ex) {
                            }
                        }
                    }
                    // Handle files
                    /*
                    else {
                        File file = getFile(playlist, inputLine);
                        if (file != null) {
                            try {
                                audio = (Audio) MediaManager.getMedia(file.getCanonicalPath());
                            } catch (Exception ex) {
                            }
                        }
                    }
                    */

                    if (audio != null) {
                        audio.setTitle(name);

                        // Do we know the length of the song?
                        if (extended && duration.length() != 0) {
                            try {
                                int length = Integer.parseInt(duration);
                                if (length != -1)
                                    audio.setDuration(length);
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }

                        try {
                            if (audio.getId() != null)
                                AudioManager.updateAudio(audio);
                            else
                                AudioManager.createAudio(audio);
                        } catch (Exception ex) {
                            Tools.logException(M3uPlaylist.class, ex);
                        }

                        /*
                        if (audio.getPath().startsWith("http"))
                            mItems.add(new Item(name, audio.getPath()));
                        else
                            mItems.add(new FileItem(name, new File(audio.getPath())));
                            */
                    }
                    if (inputLine.startsWith("http"))
                        mItems.add(new Item(name, inputLine));
                    else
                        mItems.add(new FileItem(name, getFile(playlist, inputLine)));

                    name = "";
                    duration = "";
                }
            } while ((inputLine = reader.readLine()) != null);
        } catch (IOException e) {
            log.error("Invalid playlist: " + getPath());
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                    // Ignore
                }
        }
    }
}