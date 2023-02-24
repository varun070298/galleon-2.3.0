package org.lnicholls.galleon.util;

/*
 * Original source from: http://forum.java.sun.com/thread.jsp?forum=4&thread=245711
 */

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MacNetworkInfo extends NetInfo {
	private static final Logger log = Logger.getLogger(MacNetworkInfo.class.getName());
	
    public static final String IPCONFIG_COMMAND = "ifconfig";
    
    public MacNetworkInfo(String address)
    {
    	super(address);
    }

    public void parse() {
        String ipConfigResponse = null;
        try {
            ipConfigResponse = runConsoleCommand(IPCONFIG_COMMAND);
        } catch (IOException e) {
        }
        
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(ipConfigResponse, "\n");
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