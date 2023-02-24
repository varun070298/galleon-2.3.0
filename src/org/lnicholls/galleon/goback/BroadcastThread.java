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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.goback.*;
import org.lnicholls.galleon.util.*;

import com.MHSoftware.net.IP.IpAddress;
import com.MHSoftware.net.IP.NetworkInterface;

/**
 * Extracted from: TiVo Connect Automatic Machine Discovery Protocol Specification. Revision: 1.5.1, Last updated:
 * 3/5/2003 Copyright © 2003, TiVo Inc. All rights reserved
 *
 * Normally, UDP beacons are sent periodically to the broadcast address of the local network, on port 2190 (registered
 * to TiVo). Upon startup (meaning TiVo DVR boot up or activation of TiVo Connect software running on some other
 * hardware) a TCM broadcasts, for a short period of time (say, 30 seconds) a number of redundant packets in "high
 * frequency mode" (perhaps every 5 seconds). After this initial period, the TCM drops into "low frequency mode"
 * broadcasting at a reduced rate (maybe only once every minute). The initial high-frequency mode allows listeners
 * several chances to quickly detect TCMs that have just arrived, while the eventual low-frequency broadcasts should be
 * infrequent enough to not overly burden the network – even in the presence of many TCMs. With broadcast-based
 * discovery, every TCM must be prepared to accept redundant packets, and should always assume they contain the latest
 * information about the originating TCM. Again, keep in mind that "connection-less" UDP beacons are sent blindly onto
 * the network – with no guarantee that they'll ever be received. The periodic and redundant nature of the
 * broadcastbased discovery mechanism is necessary for it to work effectively.
 */

public class BroadcastThread extends Thread implements Constants {
    private static Logger log = Logger.getLogger(BroadcastThread.class.getName());

    public BroadcastThread(Server server, int port) throws IOException {
        super("BroadcastThread");
        mServer = server;
        mPort = port;
        mSocket = new DatagramSocket(mPort);
        mSocket.setBroadcast(true);
        enableHighFrequency();
    }

    public void run() {
        while (true) {
            try {
                if (mHighFrequency) {
                    sleep(HIGH_FREQUENCY_DELAY);
                    if (System.currentTimeMillis() - mStartedHighFrequency > HIGH_FREQUENCY_PERIOD)
                        mHighFrequency = false;
                } else
                    sleep(LOW_FREQUENCY_DELAY);

                sendPackets(false);
            } catch (InterruptedException ex) {
            } // handle silently for waking up
            catch (Exception ex2) {
                Tools.logException(BroadcastThread.class, ex2);
            }
        }
    }

    public String getBroadCastAddress() {
        // Try conf/configure.xml server network info
        if (mServer.getIPAddress() != null && mServer.getIPAddress().length() > 0) {
            try {
                IpAddress ip = new IpAddress(mServer.getIPAddress());
                ip = ip.getBroadcastAddress(NetworkInfo.getSubnetMask(mServer.getIPAddress()));
                return ip.toString();
            } catch (Exception ex) {
                //log.error("IP Address=" + mServer.getIPAddress() + " Subnet Mask=" + mServer.getNetMask() + " invalid");
            }
        }

        // Try Linux /sbin/ifconfig
        if (System.getProperty("os.name").equals("Linux")) {
            NetworkInterface[] aList = NetworkInterface.getInterfaces();
            NetworkInterface oCurrent;

            for (int i = 0; i < aList.length; i++) {
                oCurrent = aList[i];
                if (mServer.getIPAddress()!=null)
                {
                	if (oCurrent.getAddress().equals(mServer.getIPAddress()))
                		return oCurrent.getBroadcastAddress();
                }
                else
                	return oCurrent.getBroadcastAddress();
            }
        }

        // Try standard 255.255.255.255
        return BROADCAST_ADDRESS;
    }

    // Clear will indicate that no services are provided; used for shutdown.
    public void sendPackets(boolean clear) {
        String broadCastAddress = getBroadCastAddress();

        try {
            // Send a unicast beacon to each known manually connected TCM
            Beacon beacon = new Beacon(true, mServer.getBeaconPort(), mServer.getHMOPort(), clear);
            byte[] buf = beacon.toString().getBytes();
            Iterator iterator = mServer.getTCMIterator();
            while (iterator.hasNext()) {
                TCM tcm = (TCM) iterator.next();
                if (tcm.getManual()) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, tcm.getAddress(), mPort);
                    mSocket.send(packet);
                    if (log.isDebugEnabled())
                        log.debug("Sent: " + beacon + " to " + tcm.getAddress().getHostAddress());
                    packet = null;
                }
            }

            // Send a broadcast beacon
            InetAddress address = InetAddress.getByName(broadCastAddress);
            beacon = new Beacon(false, mServer.getBeaconPort(), mServer.getHMOPort(), clear);
            buf = beacon.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, mPort);
            mSocket.send(packet);
            if (log.isDebugEnabled())
                log.debug("Sent broadcast: " + beacon + " to " + address.getHostAddress());
            packet = null;
            buf = null;
            beacon = null;
        } catch (Exception ex) {
            Tools.logException(BroadcastThread.class, ex);
        }
    }

    public void sendPacket(TCM tcm) {
        try {
            Beacon beacon = new Beacon(true, mServer.getBeaconPort(), mServer.getHMOPort());
            byte[] buf = beacon.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, tcm.getAddress(), mPort);
            mSocket.send(packet);
            if (log.isDebugEnabled())
                log.debug("Sent packet: " + beacon + " to " + tcm.getAddress().getHostAddress());
            packet = null;
            buf = null;
            beacon = null;
        } catch (Exception ex) {
            Tools.logException(BroadcastThread.class, ex);
        }
    }

    public DatagramSocket getSocket() {
        return mSocket;
    }

    public boolean getHighFrequency() {
        return mHighFrequency;
    }

    public void enableHighFrequency() {
        mHighFrequency = true;
        mStartedHighFrequency = System.currentTimeMillis();
        log.info("High frequency enabled");
        interrupt();
    }

    protected void finalize() throws Throwable {
        try {
            mSocket.close();
        } finally {
            super.finalize();
        }
    }

    private Server mServer;

    private int mPort;

    private DatagramSocket mSocket;

    private boolean mHighFrequency;

    private long mStartedHighFrequency;
}