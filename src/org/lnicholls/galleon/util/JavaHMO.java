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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.lnicholls.galleon.util.Tools;

public class JavaHMO {

    private static Logger log = Logger.getLogger(JavaHMO.class.getName());

    public JavaHMO() {
        try {
            File configureDir = new File(System.getProperty("conf"));
            File file = new File(configureDir.getAbsolutePath() + "/javahmo.xml");
            if (file.exists()) {
                String parsed = parse(file);
                if (parsed != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(configureDir.getAbsoluteFile()
                            + "/configurej.xml");
                    PrintWriter printWriter = new PrintWriter(fileOutputStream);
                    printWriter.print(parsed);
                    printWriter.close();
                    //  TODO
                    //file.delete();
                }
            }
        } catch (Exception ex) {
            Tools.logException(JavaHMO.class, ex);
        }
    }

    private String parse(File file) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(file);

            StringBuffer buffer = new StringBuffer();
            synchronized (buffer) {
                Element root = document.getRootElement();
                buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                buffer.append("<configuration");
                buffer.append(createAttribute("version", Tools.getVersion()));
                buffer.append(">\n");

                for (Iterator serverIterator = root.elementIterator("server"); serverIterator.hasNext();) {
                    Element element = (Element) serverIterator.next();
                    buffer.append("<server");
                    buffer.append(createAttribute("title", "Galleon"));
                    buffer.append(createAttribute("reload", Tools.getAttribute(element, "reload")));
                    buffer.append(createAttribute("port", Tools.getAttribute(element, "port")));
                    buffer.append(createAttribute("ipaddress", Tools.getAttribute(element, "ipaddress")));
                    buffer.append(createAttribute("shuffleItems", Tools.getAttribute(element, "shuffleItems")));
                    buffer.append(createAttribute("generateThumbnails", Tools.getAttribute(element,
                            "generateThumbnails")));
                    buffer.append("/>\n");
                }

                for (Iterator pluginIterator = root.elementIterator("plugin"); pluginIterator.hasNext();) {
                    Element element = (Element) pluginIterator.next();
                    buffer.append("<app");
                    buffer.append(createAttribute("name", Tools.getAttribute(element, "title")));
                    String theClass = Tools.getAttribute(element, "class");
                    if (theClass.equals("org.lnicholls.javahmo.plugins.images.ImagesContainer")) {
                        buffer
                                .append(createAttribute("class",
                                        "org.lnicholls.galleon.apps.photos.PhotosConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("path", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.internet.InternetContainer")) {
                        buffer.append(createAttribute("class",
                                "org.lnicholls.galleon.apps.internet.InternetConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("url", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.playlists.PlaylistsContainer")) {
                        buffer.append(createAttribute("class",
                                "org.lnicholls.galleon.apps.playlists.PlaylistsConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("playlist", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.organizer.OrganizerContainer")) {
                        buffer.append(createAttribute("class",
                                "org.lnicholls.galleon.apps.musicOrganizer.MusicOrganizerConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("path", element));
                        buffer.append(parseNameValues("group", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.rss.RssContainer")) {
                        buffer.append(createAttribute("class", "org.lnicholls.galleon.apps.rss.RSSConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("rssFeed", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.web.WebContainer")) {
                        buffer.append(createAttribute("class", "org.lnicholls.galleon.apps.web.WebConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("url", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.music.MusicContainer")) {
                        buffer.append(createAttribute("class", "org.lnicholls.galleon.apps.music.MusicConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("path", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.nntp.NntpContainer")) {
                        buffer.append(createAttribute("class", "org.lnicholls.galleon.apps.nntp.NNTPConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("newsGroup", element));
                    } else if (theClass.equals("org.lnicholls.javahmo.plugins.imageOrganizer.OrganizerContainer")) {
                        buffer.append(createAttribute("class",
                                "org.lnicholls.galleon.apps.imageOrganizer.ImageOrganizerConfiguration"));
                        buffer.append(">\n");

                        buffer.append(parseNameValues("path", element));
                        buffer.append(parseNameValues("group", element));
                    } else {
                        buffer.append(">\n");
                    }

                    buffer.append("</app>\n");
                }

                buffer.append("</configuration>\n");
            }

            return buffer.toString();
        } catch (Exception ex) {
            log.error("Could not parse JavaHMO configuration", ex);
        }
        return null;
    }

    private String createAttribute(String name, String value) {
        return " " + name + "=\"" + value + "\"";
    }

    private String parseNameValues(String tag, Element element) {
        StringBuffer buffer = new StringBuffer();
        synchronized (buffer) {
            for (Iterator tagsIterator = element.elementIterator(tag + "s"); tagsIterator.hasNext();) {
                buffer.append("<" + tag + "s>\n");
                Element tagsElement = (Element) tagsIterator.next();
                for (Iterator tagIterator = tagsElement.elementIterator(tag); tagIterator.hasNext();) {
                    Element tagElement = (Element) tagIterator.next();
                    buffer.append("<" + tag);
                    buffer.append(createAttribute("class", "org.lnicholls.galleon.util.NameValue"));
                    buffer.append(createAttribute("name", Tools.getAttribute(tagElement, "name")));
                    buffer.append(createAttribute("value", Tools.getAttribute(tagElement, "value")));
                    buffer.append("/>\n");
                }
                buffer.append("</" + tag + "s>\n");
            }
        }
        return buffer.toString();
    }
}