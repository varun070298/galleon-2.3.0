package org.lnicholls.galleon.apps.weather;

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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Utility data structure for state FIP mapping needed for NOAA weather XML feed: http://www.nws.noaa.gov/alerts/
 */

public class StateData {
    private static Logger log = Logger.getLogger(StateData.class.getName());

    private static class Data {
        public Data(String symbol, String name, String fip) {
            mName = name;
            mSymbol = symbol;
            mFIP = fip;
        }

        String mName;

        String mSymbol;

        String mFIP;
    }

    private static LinkedList mStates;

    // Data obtained from http://quickfacts.census.gov/cgi-bin/qfd/lookup
    static {
        mStates = new LinkedList();
        mStates.add(new Data("AL", "Alabama", "01000"));
        mStates.add(new Data("AK", "Alaska", "02000"));
        mStates.add(new Data("AZ", "Arizona", "04000"));
        mStates.add(new Data("AR", "Arkansas", "05000"));
        mStates.add(new Data("CA", "California", "06000"));
        mStates.add(new Data("CO", "Colorado", "08000"));
        mStates.add(new Data("CT", "Connecticut", "09000"));
        mStates.add(new Data("DE", "Delaware", "10000"));
        mStates.add(new Data("DC", "Wash. D.C.", "11000"));
        mStates.add(new Data("FL", "Florida", "12000"));
        mStates.add(new Data("GA", "Georgia", "13000"));
        mStates.add(new Data("HI", "Hawaii", "15000"));
        mStates.add(new Data("ID", "Idaho", "16000"));
        mStates.add(new Data("IL", "Illinois", "17000"));
        mStates.add(new Data("IN", "Indiana", "18000"));
        mStates.add(new Data("IA", "Iowa", "19000"));
        mStates.add(new Data("KS", "Kansas", "20000"));
        mStates.add(new Data("KY", "Kentucky", "21000"));
        mStates.add(new Data("LA", "Louisiana", "22000"));
        mStates.add(new Data("ME", "Maine", "23000"));
        mStates.add(new Data("MD", "Maryland", "24000"));
        mStates.add(new Data("MA", "Massachusetts", "25000"));
        mStates.add(new Data("MI", "Michigan", "26000"));
        mStates.add(new Data("MN", "Minnesota", "27000"));
        mStates.add(new Data("MS", "Mississippi", "28000"));
        mStates.add(new Data("MO", "Missouri", "29000"));
        mStates.add(new Data("MT", "Montana", "30000"));
        mStates.add(new Data("NE", "Nebraska", "31000"));
        mStates.add(new Data("NV", "Nevada", "32000"));
        mStates.add(new Data("NH", "New Hampshire", "33000"));
        mStates.add(new Data("NJ", "New Jersey", "34000"));
        mStates.add(new Data("NM", "New Mexico", "35000"));
        mStates.add(new Data("NY", "New York", "36000"));
        mStates.add(new Data("NC", "North Carolina", "37000"));
        mStates.add(new Data("ND", "North Dakota", "38000"));
        mStates.add(new Data("OH", "Ohio", "39000"));
        mStates.add(new Data("OK", "Oklahoma", "40000"));
        mStates.add(new Data("OR", "Oregon", "41000"));
        mStates.add(new Data("PA", "Pennsylvania", "42000"));
        mStates.add(new Data("RI", "Rhode Island", "44000"));
        mStates.add(new Data("SC", "So. Carolina", "45000"));
        mStates.add(new Data("SD", "So. Dakota", "46000"));
        mStates.add(new Data("TN", "Tennessee", "47000"));
        mStates.add(new Data("TX", "Texas", "48000"));
        mStates.add(new Data("UT", "Utah", "49000"));
        mStates.add(new Data("VT", "Vermont", "50000"));
        mStates.add(new Data("VA", "Virginia", "51000"));
        mStates.add(new Data("WA", "Washington", "53000"));
        mStates.add(new Data("WV", "West Virginia", "54000"));
        mStates.add(new Data("WI", "Wisconsin", "55000"));
        mStates.add(new Data("WY", "Wyoming", "56000"));
    }

    public static String getFipFromSymbol(String symbol) {
        Iterator iterator = mStates.iterator();
        while (iterator.hasNext()) {
            Data data = (Data) iterator.next();
            if (data.mSymbol.equalsIgnoreCase(symbol))
                return data.mFIP;
        }
        return "00000";
    }

    public static String getFipFromName(String name) {
        Iterator iterator = mStates.iterator();
        while (iterator.hasNext()) {
            Data data = (Data) iterator.next();
            if (data.mName.equalsIgnoreCase(name))
                return data.mFIP;
        }
        return "00000";
    }
}