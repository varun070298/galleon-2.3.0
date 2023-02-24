package org.lnicholls.galleon.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

abstract public class NetInfo {
	
	private static final String LOCALHOST = "localhost";

    public static final String NSLOOKUP_CMD = "nslookup";
    
    public NetInfo(String address)
    {
    	mAddress = address;
    }
    
    public String parseDomain() throws ParseException {
        return parseDomain(LOCALHOST);
    }
    
    protected String parseDomain(String hostname) throws ParseException {
        // get the address of the host we are looking for - verification
        java.net.InetAddress addy = null;
        try {
            addy = java.net.InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            throw new ParseException(e.getMessage(), 0);
        }

        // back out to the hostname - just validating
        hostname = addy.getCanonicalHostName();
        String nslookupCommand = NSLOOKUP_CMD + " " + hostname;

        // run the lookup command
        String nslookupResponse = null;
        try {
            nslookupResponse = runConsoleCommand(nslookupCommand);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new ParseException(e.getMessage(), 0);
        }

        StringTokenizer tokeit = new StringTokenizer(nslookupResponse, "\n", false);
        while (tokeit.hasMoreTokens()) {
            String line = tokeit.nextToken();
            if (line.startsWith("Name:")) {
                line = line.substring(line.indexOf(":") + 1);
                line = line.trim();
                if (isDomain(line, hostname)) {
                    line = line.substring(hostname.length() + 1);
                    return line;
                }
            }
        }

        return "n.a.";
    }

    private static boolean isDomain(String domainCandidate, String hostname) {
        Pattern domainPattern = Pattern.compile("[\\w-]+\\.[\\w-]+\\.[\\w-]+\\.[\\w-]+");
        Matcher m = domainPattern.matcher(domainCandidate);
        return m.matches() && domainCandidate.startsWith(hostname);
    }

    protected String runConsoleCommand(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
        StringBuffer buffer = new StringBuffer();
        synchronized (buffer) {
            for (;;) {
                int c = stdoutStream.read();
                if (c == -1)
                    break;
                buffer.append((char) c);
            }
            String outputText = buffer.toString();
            stdoutStream.close();
            return outputText;
        }
    }
    
    protected String getLocalHost() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
        }
        return "127.0.0.1";
    }
	
	abstract public void parse();

    public String getSubnetMask()
    {
    	if (mSubnetMask!=null)
    		return mSubnetMask;
    	else
    		return "255.255.255.0";
    }
    
    public String getPhysicalAddress()
    {
    	if (mPhysicalAddress!=null)
    		return mPhysicalAddress;
    	else
    		return getAddress();
    }
    
    public String getAddress()
    {
    	if (mAddress!=null)
    		return mAddress;
    	else
    		return getLocalHost();
    }
    
    private String mAddress;
    
    protected String mSubnetMask;
    
    protected String mPhysicalAddress;	
}