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

package org.lnicholls.galleon.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.*;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.*;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.server.*;

public class DataUpdateThread extends Thread {

    private static Logger log = Logger.getLogger(DataUpdateThread.class.getName());

    public DataUpdateThread() throws IOException {
        super("DataUpdateThread");
        setPriority(Thread.MIN_PRIORITY);
    }

    public void run() {
    	ServerConfiguration serverConfiguration = Server.getServer().getServerConfiguration();
    	DataConfiguration dataConfiguration = serverConfiguration.getDataConfiguration();
    	
    	int counter = 1;

    	while (true)
        {
    		try
    		{
				if (dataConfiguration.isConfigured())
				{
					System.out.println("login");
					boolean result = Users.login(dataConfiguration);
					if (result)
					{
						System.out.println("updateApplications");
						result = Users.updateApplications(dataConfiguration);
						if (result)
						{
							System.out.println("logout");
							result = Users.logout(dataConfiguration);
						}
					}
					
					if (result)
						counter = 1;
					else
						counter++;
				}
	        	
	            sleep(1000*60*60*counter);
	            
	            if (counter > 48)
	            	counter = 24;
	        }
    		catch (InterruptedException ex) {
    			
    		}
	        catch (Exception ex) {
	        	counter++;
	        	Tools.logException(DataUpdateThread.class, ex);
	            try
	            {
	            	sleep(1000*60*60*counter);	
	            }
	            catch (Exception ex2){}
	        }
        }
    }
}