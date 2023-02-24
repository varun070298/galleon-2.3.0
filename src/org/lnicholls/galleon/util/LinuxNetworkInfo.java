package org.lnicholls.galleon.util;

/*
 * Original source from: http://forum.java.sun.com/thread.jsp?forum=4&thread=245711
 */

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class LinuxNetworkInfo extends NetInfo {
	private static final Logger log = Logger.getLogger(LinuxNetworkInfo.class.getName());
	
	public static final String IPCONFIG_COMMAND = "ifconfig";
    
    public LinuxNetworkInfo(String address)
    {
    	super(address);
    }

    public void parse() {
        String ipConfigResponse = null;
        try {
            ipConfigResponse = runConsoleCommand(IPCONFIG_COMMAND);
        } catch (IOException e) {
        }
        
        String response = "Link encap 10Mbps Ethernet  HWaddr 00:00:C0:90:B3:42\n"
        	+ "inet addr 192.168.0.3 Bcast 172.16.1.255 Mask 255.255.255.1\n"
        	+ "UP BROADCAST RUNNING  MTU 1500  Metric 0\n"
        	+ "RX packets 3136 errors 217 dropped 7 overrun 26\n"
        	+ "TX packets 1752 errors 25 dropped 0 overrun 0";

        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(ipConfigResponse, "\n");
        //java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(response, "\n");
        String address = null;
        
		Pattern macPattern = Pattern.compile("HWaddr (.*)");
		Pattern subnetPattern = Pattern.compile("inet addr (.*) Bcast (.*) Mask (.*)");
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().trim();
            
            Matcher m = macPattern.matcher(line);
    		if (m.find() && m.groupCount() == 1) {
    			mPhysicalAddress = m.group(1).trim();
    		}            
    		else
    		{
	    		m = subnetPattern.matcher(line);
	    		if (m.find() && m.groupCount() == 3) {
	    			if (m.group(1).trim().equals(getAddress()))
	    			{
	    				address = m.group(1).trim();
	    				mSubnetMask = m.group(3).trim();
	    			}
	    		}
    		}
            
            if (address!=null && mPhysicalAddress!=null && mSubnetMask!=null)
            {
            	if (address.equals(getAddress()))
                {
                	break;
                }
            }
        }        
    }
}