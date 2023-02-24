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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class FileSystemContainer {
    private static Logger log = Logger.getLogger(FileSystemContainer.class.getName());

    public FileSystemContainer(String path) {
         this(path, false);
    }
    
    public FileSystemContainer(String path, boolean recursive) {
        mPath = path;
        mRecursive = recursive;
    }
    
    public List getItemsSorted(FileFilter fileFilter) {
        List list = getItems(fileFilter);
        return sort(list);
    }

    public List getItems(FileFilter fileFilter) {
        if (log.isDebugEnabled())
            log.debug("getItems:");

        final ArrayList items = new ArrayList();

        try
        {
	        File directory = FileGatherer.resolveLink(new File(getPath())); // Handle shortcuts
	        if (directory.isDirectory()) {
	        	FileGatherer.gatherDirectory(directory, fileFilter, mRecursive, new FileGatherer.GathererCallback() {
	                public void visit(File file, File originalFile) {
	                	if (originalFile.equals(file))
	                    {
	                		if (file.isDirectory())
	                		{
	                			items.add(new FolderItem(file.getName(), file));
	                		}
	                        else
	                        {
	                            if (FileFilters.playlistFilter.accept(file))
	                                items.add(new PlaylistItem(Tools.extractName(file.getName()), file));
	                            else
	                                items.add(new FileItem(Tools.extractName(file.getName()), file));
	                        }
	                    }
	                    else
	                    {
	                        if (file.isDirectory())
	                        {
	                            items.add(new FolderItem(originalFile.getName(), file));
	                        }
	                        else
	                        {
	                            if (FileFilters.playlistFilter.accept(file))
	                                items.add(new PlaylistItem(Tools.extractName(originalFile.getName()), file));
	                            else
	                                items.add(new FileItem(Tools.extractName(originalFile.getName()), file));
	                        }
	                    }
	                }
	            });
	        }
        }
        catch (Exception ex)
        {
        	Tools.logException(FileSystemContainer.class, ex, "Could not get items");
        }
        return items;
    }

    public Date getLastModified() {
        File file = new File(getPath());
        if (file.exists())
            return new Date(file.lastModified());
        return new Date();
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public static class Item
    {
        public Item(String name, Object value)
        {
            mName = name;
            mValue = value;
        }
        
        public String getName()
        {
            return mName;
        }
        public Object getValue()
        {
            return mValue;
        }
        
        public boolean isFile()
        {
            return false;
        }
        public boolean isFolder()
        {
            return false;
        }
        public boolean isPlaylist()
        {
            return false;
        }
        
        public String toString()
        {
            return mName;
        }
        
        private String mName;
        private Object mValue;
    }
    
    public static class FileItem extends Item
    {
        public FileItem(String name, Object value)
        {
            super(name,value);
        }
        
        public boolean isFile()
        {
            return true;
        }
    }
    
    public static class FolderItem extends FileItem
    {
        public FolderItem(String name, Object value)
        {
            super(name,value);
        }
        
        public boolean isFolder()
        {
            return true;
        }
    }
    
    public static class PlaylistItem extends Item
    {
        public PlaylistItem(String name, Object value)
        {
            super(name,value);
        }
        
        public boolean isPlaylist()
        {
            return true;
        }
    }


    private final static boolean hasDigits(String value)
    {
        if (value!=null)
        {
            for (int i = 0; i < value.length(); i++)
            {
                if (Character.isDigit(value.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private final static String getIntegerSubstring(String s) {
        int i = 0;
        while ((i < s.length()) && Character.isDigit(s.charAt(i))) {
            ++i;
        }
        return s.substring(0, i);
    }    
    
    public final static List sort(List list)
    {
        Item[] items = (Item[]) list.toArray(new Item[0]);
        Arrays.sort(items, new Comparator() {
            
            public final int compare(Object o1, Object o2) {
                Item item1 = (Item) o1;
                Item item2 = (Item) o2;

                // TODO use file name as a proxy for title for now. We remove
                // the suffix so "Song.mp3" is sorted before "Song 2.mp3".

                if (hasDigits(item1.getName()) && hasDigits(item2.getName())) {
                    String name1 = item1.getName();
                    String name2 = item2.getName();

                    // Compare embedded numbers correctly, e.g. rank "Track 9" before "Track 10".
                    int i1 = 0, i2 = 0;
                    while (i1 < name1.length()) {
                        if (i2 >= name2.length()) {
                            // name 1 is longer than name2 and substring is identical.
                            // result: name1 > name2
                            return 1;
                        }
                        char c1 = name1.charAt(i1);
                        char c2 = name2.charAt(i2);
                        // Check to see if we have reached a number in both strings
                        if (Character.isDigit(c1) && Character.isDigit(c2)) {
                            // Start of a number in both strings. Extract the complete
                            // number from both for comparison.
                            String number1 = getIntegerSubstring(name1.substring(i1));
                            String number2 = getIntegerSubstring(name2.substring(i2));
                            double double1 = 0, double2 = 0;
                            try {
                                double1 = Double.parseDouble(number1);
                                double2 = Double.parseDouble(number2);
                            } catch (NumberFormatException e) {
                                log.error("TitleCompare exception " + e + " name1=" + name1 + " name2=" + name2);
                            }
                            if (double1 < double2) {
                                return -1;
                            } else if (double1 > double2) {
                                return 1;
                            }
                            i1 += number1.length();
                            i2 += number2.length();
                        } else {
                            // At least one string is non-digit at this point, so compare
                            // current character as string.
                            if (c1 < c2) {
                                return -1;
                            } else if (c1 > c2) {
                                return 1;
                            }
                            ++i1;
                            ++i2;
                        }
                    }
                    // i1 has reached end of string. If i2 has also reached end of string
                    // these strings are equal, but if i2 has characters left i2 is greater.
                    return (i2 >= name2.length()) ? 0 : -1;
                } else
                    return item1.toString().toLowerCase().compareTo(item2.toString().toLowerCase());
            }            
        });
        list.clear();
        for (int i = 0; i < items.length; i++)
            list.add(items[i]);
        return list;
    }    
    
    private String mPath;
    
    private boolean mRecursive;
}