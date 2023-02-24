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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.FileSystemContainer.FileItem;
import org.lnicholls.galleon.util.FileSystemContainer.Item;

/*
 * .pls Playlists of audio files and URLs.
 */

public class PlsPlaylist extends Playlist {
    private static Logger log = Logger.getLogger(PlsPlaylist.class.getName());

    private static final String FILE = "File";

    private static final String TITLE = "Title";

    private static final String LENGTH = "Length";

    private static final String START = "[playlist]";

    public PlsPlaylist(String path) {
        super(path);
        loadMetaData();
    }

    // Delegate method to gather all of the items before applying all of the request criteria
    protected final void loadMetaData() {
        mItems.clear();

        List lines = new ArrayList();
        FileInputStream fileInputStream = null;
        String inputLine = null;
        File playlist = new File(mPath);

        try {
            fileInputStream = new FileInputStream(playlist);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            inputLine = reader.readLine();

            if (inputLine == null || !inputLine.startsWith(START)) {
                log.error("Invalid playlist: '" + inputLine + "' in " + getPath());
                return;
            }

            // FIX: Is whitespace allowed or do we need to be more strict?
            Pattern pattern = Pattern.compile("\\s*(File|Length|Title)([0-9]+)\\s*=\\s*(.*)");
            Matcher matcher = pattern.matcher("");

            // Parse the file
            while ((inputLine = reader.readLine()) != null) {
                matcher.reset(inputLine);
                if (!matcher.matches() || matcher.groupCount() != 3) {
                    // FIX: We should check the version and NumberOfEntries to
                    // see that they are correct.
                    if (!inputLine.startsWith("Version=") && !inputLine.startsWith("NumberOfEntries="))
                        log.error("Skipping line with unknown format: '" + inputLine + "' in " + getPath());

                    continue;
                }

                lines.add(new PlsLine(matcher.group(1), matcher.group(2), matcher.group(3)));
            }
        } catch (NumberFormatException e) {
            log.error("Skipping line with invalid number: '" + inputLine + "' in " + getPath());
        } catch (IOException e) {
            log.error("Invalid playlist: " + getPath());
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException ioe) {
                // Ignore
            }
        }

        Collections.sort(lines);
        Iterator i = lines.iterator();
        List songs = new ArrayList();
        PlsEntry plsEntry = null;

        while (i.hasNext()) {
            PlsLine plsLine = (PlsLine) i.next();

            if (plsEntry == null || plsEntry.n != plsLine.n) {
                plsEntry = new PlsEntry();
                plsEntry.n = plsLine.n;
                songs.add(plsEntry);
            }

            if (FILE.equals(plsLine.type))
                plsEntry.file = plsLine.data;
            else if (TITLE.equals(plsLine.type))
                plsEntry.title = plsLine.data;
            else if (LENGTH.equals(plsLine.type))
                try {
                    plsEntry.length = Integer.parseInt(plsLine.data);
                } catch (NumberFormatException e) {
                    // Ignore
                }
        }
        lines.clear();

        i = songs.iterator();
        while (i.hasNext()) {
            plsEntry = (PlsEntry) i.next();

            // Only allow format: http://205.188.234.66:8010
            Audio audio = null;
            if (plsEntry.file.startsWith("http")) {
                try {
                    List list = AudioManager.findByPath(plsEntry.file);
                    if (list!=null && list.size()>0)
                    {
                        audio = (Audio)list.get(0);
                    }
                } catch (Exception ex) {
                }
                
                if (audio==null)
                {
                    try {
                        audio = (Audio) MediaManager.getMedia(plsEntry.file);
                    } catch (Exception ex) {
                    }
                }
            } 
            /*
            else {
                File file = getFile(playlist, plsEntry.file);
                if (file != null) {
                    try {
                        audio = (Audio) MediaManager.getMedia(file.getCanonicalPath());
                    } catch (Exception ex) {
                    }
                }
            }
            */

            if (audio != null) {
                audio.setTrack(plsEntry.n);
                audio.setTitle(plsEntry.title);
                if (plsEntry.length != -1)
                    audio.setDuration(plsEntry.length);

                try {
                    if (audio.getId() != null)
                        AudioManager.updateAudio(audio);
                    else
                        AudioManager.createAudio(audio);
                } catch (Exception ex) {
                    Tools.logException(M3uPlaylist.class, ex);
                }

                //mItems.add(new FileItem(plsEntry.title, new File(audio.getPath())));
            }
            if (!plsEntry.file.startsWith("http"))
                mItems.add(new FileItem(plsEntry.title, getFile(playlist, plsEntry.file)));
            else
                mItems.add(new Item(plsEntry.title, plsEntry.file));
        }
    }

    private static final class PlsLine implements Comparable {
        private final String type;

        private final int n;

        private final String data;

        private PlsLine(String t, String num, String d) throws NumberFormatException {
            type = t;
            n = Integer.parseInt(num);
            data = d;
        }

        // Sort by number, then type (file, length, title)
        public int compareTo(Object o) {
            PlsLine that = (PlsLine) o;
            int ret = this.n - that.n;

            if (ret == 0)
                return this.type.compareTo(that.type);

            return ret;
        }
    }

    private static final class PlsEntry implements Comparable {
        private String file = null;

        private String title = "unknown";

        private int length = 0;

        private int n;

        public int compareTo(Object o) {
            PlsEntry that = (PlsEntry) o;
            return this.n - that.n;
        }
    }
}