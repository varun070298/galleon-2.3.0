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

import org.jawin.COMException;
import org.jawin.DispatchPtr;
import org.jawin.GUID;
import org.jawin.IUnknown;
import org.jawin.IdentityManager;

/**
 * Jawin Type Browser generated code
 */

public class TiVoBeacon extends DispatchPtr {
    public static final GUID CLSID = new GUID("{eeb93ced-8e9d-47c7-8D48-385ACD6B32BC}");

    public static GUID DIID = new GUID("{a97ce70e-3242-4f71-B0FA-6E97C72AE449}");

    static public final int iidToken;
    static {
        iidToken = IdentityManager.registerProxy(DIID, TiVoBeacon.class);
    }

    public TiVoBeacon() throws COMException {
        super();
    }

    public TiVoBeacon(String progid) throws COMException {
        super(progid);
    }

    public TiVoBeacon(IUnknown other) throws COMException {
        super(other);
    }

    public TiVoBeacon(GUID ClsID) throws COMException {
        super(ClsID);
    }

    /**
     *
     * @param port
     * @return void
     */
    public void PublishMediaServer(int port) throws COMException {
        invokeN("PublishMediaServer", new Object[] { new Integer(port) });
    }

    /**
     *
     * @param port
     * @return void
     */

    public void RevokeMediaServer(int port) throws COMException {
        invokeN("RevokeMediaServer", new Object[] { new Integer(port) });
    }

    /**
     *
     * @return int
     */
    public int Release() throws COMException {
        return ((Integer) invokeN("Release", new Object[] {})).intValue();
    }
}