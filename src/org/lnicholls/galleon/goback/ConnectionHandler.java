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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.goback.*;
import org.lnicholls.galleon.util.*;
/**
 * Extracted from: TiVo Connect Automatic Machine Discovery Protocol Specification. Revision: 1.5.1, Last updated:
 * 3/5/2003 Copyright © 2003, TiVo Inc. All rights reserved The packet data format is common to all other aspects of the
 * protocol, while the broadcast-based and connection-based discovery mechanisms, although similar, each have their own
 * associated details. Every TCM must be prepared to participate in broadcast- and connection-based discovery
 * simultaneously. Regardless of the method used to transmit beacon packets, each participating TCM maintains an
 * internal list of all other TCMs from which it has heard. Records in this list are updated whenever new information
 * arrives. Records for TCMs that have not been heard from recently (or whose departures have been explicitly detected)
 * are eventually cycled off this list. In this way, whenever further communication is needed, the set of networked
 * machines able to "TiVo Connect", as well as their available services, can be known at any given moment with no need
 * to query the network.
 */

public class ConnectionHandler implements Runnable, Constants {
    private static Logger log = Logger.getLogger(ConnectionHandler.class.getName());

    public ConnectionHandler(Socket socket, ConnectionThread connectionThread) throws Exception {
        mSocket = socket;
        mInputStream = socket.getInputStream();
        mOutputStream = socket.getOutputStream();
        mConnectionThread = connectionThread;
    }

    public void run() {
        byte[] buf = new byte[300];
        int n;
        Beacon beacon = null;
        try {
            boolean responded = false;
            while ((n = mInputStream.read(buf)) > 0) {
                if (log.isDebugEnabled())
                    log.debug("Data=" + (new String(buf)));
                beacon = new Beacon(buf);

                if (beacon.isValid()) {
                    TCM tcm = mConnectionThread.getServer().getTCM(beacon);
                    if (tcm == null) {
                        if (log.isDebugEnabled())
                            log.debug("Got beacon: " + beacon);
                        tcm = new TCM(mSocket.getInetAddress(), beacon, true);
                        mConnectionThread.getServer().addTCM(tcm);
                    } else {
                        mConnectionThread.getServer().removeTCM(tcm);
                        tcm = new TCM(mSocket.getInetAddress(), beacon, true);
                        mConnectionThread.getServer().addTCM(tcm);
                    }
                    mConnectionThread.getServer().getBroadcastThread().sendPacket(tcm);
                    responded = true;
                }
            }

            if (log.isDebugEnabled())
                log.debug("No more data:" + n);
            if (!responded) {
                TCM tcm = mConnectionThread.getServer().getTCM(mSocket.getInetAddress());
                if (tcm != null) {
                    mConnectionThread.getServer().removeTCM(tcm);
                    mConnectionThread.getServer().addTCM(new TCM(mSocket.getInetAddress(), beacon, true));
                    mConnectionThread.getServer().getBroadcastThread().sendPacket(tcm);
                }
            }
        } catch (Exception ex) {
            Tools.logException(ConnectionHandler.class, ex);
        } finally {
            try {
                if (mOutputStream != null)
                    mOutputStream.close();
                if (mInputStream != null)
                    mInputStream.close();
                if (mSocket != null)
                    mSocket.close();
            } catch (IOException ex) {
                Tools.logException(ConnectionHandler.class, ex);
            }
        }

    }

    private ConnectionThread mConnectionThread;

    private Socket mSocket;

    private InputStream mInputStream;

    private OutputStream mOutputStream;

    private BufferedReader mBufferedReader;
}