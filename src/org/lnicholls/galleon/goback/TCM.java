package org.lnicholls.galleon.goback;

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

import java.net.InetAddress;

import org.lnicholls.galleon.goback.Beacon;

/*
 *
 * TiVo Connection Machine class to represent TiVo nodes on the network
 */
public class TCM {
    public TCM(InetAddress address, Beacon beacon, boolean manual) {
        mAddress = address;
        mBeacon = beacon;
        mLastUpdate = System.currentTimeMillis();
        mManual = manual;
    }

    public InetAddress getAddress() {
        return mAddress;
    }

    public Beacon getBeacon() {
        return mBeacon;
    }

    public long getLastUpdate() {
        return mLastUpdate;
    }

    // Was this TCM discovered by a manual connection?
    public boolean getManual() {
        return mManual;
    }

    private long mLastUpdate;

    private Beacon mBeacon;

    private InetAddress mAddress;

    private boolean mManual;
}