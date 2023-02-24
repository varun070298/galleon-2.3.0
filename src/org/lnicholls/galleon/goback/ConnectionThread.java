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
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.goback.*;
import org.lnicholls.galleon.util.*;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * Extracted from: TiVo Connect Automatic Machine Discovery Protocol Specification. Revision: 1.5.1, Last updated:
 * 3/5/2003 Copyright © 2003, TiVo Inc. All rights reserved
 *
 * Normally, TCP beacons are exchanged between two specific TCMs, one TCM initiating the connection on port 2190. With
 * the IP address (typically supplied by a user) associated with some "target" TCM, the initiating TCM establishes a TCP
 * connection with the target TCM and then transmits a single packet. The target TCM responds with its own packet. From
 * this point on, the connection is maintained (normally "quiet", with no additional packets sent) until either TCM
 * drops the connection. Although TCP beacons need only be exchanged once upon initial connection, each TCM should be
 * prepared to accept and process redundant packets from the other, just as with broadcast-based discovery. 3.5 Update
 * Procedure When any information about a TCM changes, the affected TCM should switch back to highfrequency mode (again
 * for 30 seconds, just as upon startup) broadcasting several packets containing the new information. This will allow
 * the changes to propagate quickly to any other TCMs participating in broadcast-based discovery. In order to also
 * propagate the changes to those TCMs participating in connection-based discovery, a single packet containing the new
 * information should also be sent to each currently connected TCM (regardless of whether the affected TCM initiated the
 * connection or not).
 */

public class ConnectionThread extends Thread implements Constants {
    private static Logger log = Logger.getLogger(ConnectionThread.class.getName());

    public ConnectionThread(Server server) throws IOException {
        super("ConnectionThread");
        mServer = server;

        mPool = new PooledExecutor(3);
        mPool = new PooledExecutor(new LinkedQueue());
        mPool.setKeepAliveTime(-1);
        mPool.createThreads(2);
    }

    public void run() {
        BufferedReader input;
        BufferedWriter output;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(mServer.getBeaconPort());
            if (log.isDebugEnabled())
                log.debug("Server waiting for client on port " + serverSocket.getLocalPort());

            // server infinite loop
            while (true) {
                Socket socket = serverSocket.accept();
                //socket.setKeepAlive(true);
                //socket.setSoTimeout(0);
                log.info("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                try {
                    ConnectionHandler request = new ConnectionHandler(socket, this);
                    mPool.execute(request);
                } catch (Exception ex) {
                    Tools.logException(ConnectionThread.class, ex);
                }
            }
        } catch (IOException ex) {
            System.runFinalization();
            Tools.logException(ConnectionThread.class, ex);
        } finally {
            try {
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException ex) {
                Tools.logException(ConnectionThread.class, ex);
            }
        }

    }

    public Server getServer() {
        return mServer;
    }

    private PooledExecutor mPool;

    private Server mServer;
}