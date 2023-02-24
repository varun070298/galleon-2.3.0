package org.lnicholls.galleon.util;

/*
 * Copyright (C) 2003  Mike Kelley, Leon Nicholls
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */ 
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.io.*;import org.lnicholls.galleon.media.*;

import org.apache.log4j.Logger;

/*
 * Factory for the various useful file sorting comparators
 */

public class FileSorters {
    private static Logger log = Logger.getLogger(FileSorters.class.getName());

    // Private constructor ensures this can not be instantiated
    private FileSorters() {
    }

    //	Do some pre-calculations to improve sorting speed
    public static final class SortCollator {
        private static final Collator collator = Collator.getInstance();

        public SortCollator(Media Item) {
            mItem = Item;
            mTitle = Item.getTitle();
            mTitleCollationKey = collator.getCollationKey(mTitle);
            mExtensionCollationKey = collator.getCollationKey("");
            int pos = Item.getPath().lastIndexOf('.');
            if (pos > 0)
            {
                int suffixLength = Item.getPath().length() - pos;
                if ((suffixLength == ".xxx".length()) || (suffixLength == ".xxxx".length())) {
                    mExtensionCollationKey = collator.getCollationKey(Item.getPath().substring(pos));
                }
                else
                    mExtensionCollationKey = collator.getCollationKey("");
            }

            for (int i = 0; i < mTitle.length(); i++)
                if (Character.isDigit(mTitle.charAt(i))) {
                    mHasDigits = true;
                    break;
                }
            mIsDirectory = (new File(mItem.getPath())).isDirectory();
            mDate = Item.getDateModified().getTime();
        }

        public final Media getItem() {
            return mItem;
        }

        // FileSorters will have direct access to these properties
        private Media mItem;

        private String mTitle;

        private CollationKey mTitleCollationKey;

        private CollationKey mExtensionCollationKey;

        private boolean mHasDigits;

        private boolean mIsDirectory;

        private long mDate;
    }

    // Extract an integer substring embedded at the head of a string
    private final static String getIntegerSubstring(String s) {
        int i = 0;
        while ((i < s.length()) && Character.isDigit(s.charAt(i))) {
            ++i;
        }
        return s.substring(0, i);
    }

    // Comparator that sorts by title
    static final class TitleComparator implements Comparator {
        public TitleComparator() {
        }

        public final int compare(Object o1, Object o2) {
            SortCollator f1 = (SortCollator) o1;
            SortCollator f2 = (SortCollator) o2;

            // TODO use file name as a proxy for title for now. We remove
            // the suffix so "Song.mp3" is sorted before "Song 2.mp3".

            if (f1.mHasDigits && f2.mHasDigits) {
                String name1 = f1.mTitle;
                String name2 = f2.mTitle;

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
                return f1.mTitleCollationKey.compareTo(f2.mTitleCollationKey);
        }
    }

    // Comparator that sorts by type
    static final class TypeComparator implements Comparator {
        public TypeComparator() {
        }

        public final int compare(Object o1, Object o2) {
            SortCollator f1 = (SortCollator) o1;
            SortCollator f2 = (SortCollator) o2;

            boolean isDirectory1 = f1.mIsDirectory;
            boolean isDirectory2 = f2.mIsDirectory;

            if (!isDirectory1 && !isDirectory2) {
                return f1.mExtensionCollationKey.compareTo(f2.mExtensionCollationKey);
            } else
            // This should list containers before items
            // TODO make this user-configurable
            if (!isDirectory1 && isDirectory2)
                return 1;
            else if (isDirectory1 && !isDirectory2)
                return -1;
            else
                return 0;
        }
    }

    // Comparator that sorts by date
    static final class DateComparator implements Comparator {
        public DateComparator() {
        }

        public final int compare(Object o1, Object o2) {
            SortCollator f1 = (SortCollator) o1;
            SortCollator f2 = (SortCollator) o2;

            long date1 = f1.mDate;
            long date2 = f2.mDate;
            if (date1 == date2)
                return 0;
            else if (date1 < date2)
                return -1;
            else
                return 1;
        }
    }

    // Static instances of the various comparators for general use.
    public static final Comparator titleComparator = new TitleComparator();

    public static final Comparator typeComparator = new TypeComparator();

    public static final Comparator dateComparator = new DateComparator();

    // The  sort param can contain multiple types.
    // This is a general chained comparator class that combines other comparators
    // and executes them in order until it finds a unqiue result.
    static final class ChainedComparator implements Comparator {
        private ArrayList comparators = new ArrayList();

        public final void addFilter(Comparator comparator) {
            comparators.add(comparator);
        }

        public final int compare(Object o1, Object o2) {
            for (Iterator i = comparators.iterator(); i.hasNext(); /* Nothing */) {
                Comparator comparator = (Comparator) i.next();
                int result = comparator.compare(o1, o2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        public final int getSize() {
            return comparators.size();
        }

        public final Comparator getFirstComparator() {
            return (Comparator) comparators.get(0);
        }
    }
/*
    // Return a ChainedComparator that matches a  sort parameter
    public static final Comparator getFileSorter(QueryContainerRequest QueryContainerRequest) {
        if (log.isDebugEnabled())
            log.debug("getFileSorter QueryContainerRequest=" + QueryContainerRequest);

        FileSorters.ChainedComparator chainedComparator = new FileSorters.ChainedComparator();
        if (QueryContainerRequest.getSortByCreationDate())
            chainedComparator.addFilter(FileSorters.dateComparator);

        if (QueryContainerRequest.getSortByLastChangeDate())
            chainedComparator.addFilter(FileSorters.dateComparator);

        if (QueryContainerRequest.getSortByType())
            chainedComparator.addFilter(FileSorters.typeComparator);

        if (QueryContainerRequest.getSortByTitle())
            chainedComparator.addFilter(FileSorters.titleComparator);

        // Handle simple case
        if (chainedComparator.getSize() == 1)
            return chainedComparator.getFirstComparator();

        return chainedComparator;
    }*/    

    public static void Sort(ArrayList arr, Comparator comparator) {
        QuickSort(arr, 0, arr.size() - 1, comparator);
    }

    private static void QuickSort(ArrayList arr, int lo, int hi, Comparator comparator) {
        if (lo >= hi)
            return;

        int mid = (lo + hi) / 2;
        Object tmp;
        Object middle = arr.get(mid);

        if (comparator.compare(arr.get(lo), middle) > 0) {
            arr.set(mid, arr.get(lo));
            arr.set(lo, middle);
            middle = arr.get(mid);
        }

        if (comparator.compare(middle, arr.get(hi)) > 0) {
            arr.set(mid, arr.get(hi));
            arr.set(hi, middle);
            middle = arr.get(mid);

            if (comparator.compare(arr.get(lo), middle) > 0) {
                arr.set(mid, arr.get(lo));
                arr.set(lo, middle);
                middle = arr.get(mid);
            }
        }

        int left = lo + 1;
        int right = hi - 1;

        if (left >= right)
            return;

        for (;;) {
            while (comparator.compare(arr.get(right), middle) > 0) {
                right--;
            }

            while (left < right && comparator.compare(arr.get(left), middle) <= 0) {
                left++;
            }

            if (left < right) {
                tmp = arr.get(left);
                arr.set(left, arr.get(right));
                arr.set(right, tmp);
                right--;
            } else {
                break;
            }
        }

        QuickSort(arr, lo, left, comparator);
        QuickSort(arr, left + 1, hi, comparator);
    }
}