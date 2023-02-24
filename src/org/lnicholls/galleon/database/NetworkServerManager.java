package org.lnicholls.galleon.database;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;

public class NetworkServerManager {

    private static Logger log = Logger.getLogger(NetworkServerManager.class.getName());

    private static final int DEFAULT_PORT = 1527;

    private static final int MAX_PING_COUNT = 10;

    public static void initialize() throws Exception {
        mHibernateProperties = new Properties();
        File file = new File(System.getProperty("conf") + "/" + "hibernate.properties");
        if (!file.exists()) {
            InstantiationException exception = new InstantiationException("hibernate.properties not found");
            log.error("Creating Derby Network Server failed", exception);
            throw exception;
        }
        mHibernateProperties.load(new FileInputStream(file));
        
        if (mHibernateProperties.getProperty("hibernate.dialect").equals("net.sf.hibernate.dialect.DerbyDialect"))
        {
            Properties derbyProperties = new Properties();
            file = new File(System.getProperty("conf") + "/" + "derby.properties");
            if (!file.exists()) {
                InstantiationException exception = new InstantiationException("derby.properties not found");
                log.error("Creating Derby Network Server failed", exception);
                throw exception;
            }
            derbyProperties.load(new FileInputStream(file));            
        
            mPort = DEFAULT_PORT;
            if (derbyProperties.getProperty("derby.drda.portNumber")!=null)
            {
                try
                {
                    mPort = Integer.parseInt(derbyProperties.getProperty("derby.drda.portNumber"));
                }
                catch (NumberFormatException ex)
                {
                    log.error("Invalid derby.drda.portNumber: "+ derbyProperties.getProperty("derby.drda.portNumber"), ex);    
                }
            }
            
            int port = Tools.findAvailablePort(mPort);
            if (port!=mPort)
            {
                mPort = port;
                log.info("Changed port to "+mPort);
            }
    
            try {
                // Only use the network server for Derby database
                log.info("Creating Database Network Server");
                if (derbyProperties.getProperty("derby.system.home")==null)
                    System.setProperty("derby.system.home", System.getProperty("data"));
                if (derbyProperties.getProperty("derby.stream.error.file")==null)
                    System.setProperty("derby.stream.error.file", System.getProperty("logs")+"/"+"derby.log");
                
                mNetworkServerControl = new NetworkServerControl(InetAddress.getByName("127.0.0.1"), mPort);
                mNetworkServerControl.start(null);
    
                if (SystemUtils.IS_OS_MAC_OSX)
                    System.setProperty("derby.storage.fileSyncTransactionLog", "true");  // OSX bug
                
                testDatabase();
    
                log.info("Created Database Network Server");
                    
            } catch (Exception ex) {
                log.error("Derby Network server could not be created", ex);
                throw ex;
            }
        }
        else
            log.info("Alternate Hibernate database: "+mHibernateProperties.getProperty("hibernate.dialect"));
    }

    public static void trace(boolean status) {
        try {
            if (mNetworkServerControl!=null)
                mNetworkServerControl.trace(status);
        } catch (Exception ex) {
            log.error("Tracing failed", ex);
        }
    }

    // Check to see if the database is alive
    public static boolean ping() {
        try {
            if (mNetworkServerControl!=null)
                mNetworkServerControl.ping();
            return true;
        } catch (Exception ex) {
            log.error("Ping failed", ex);
        }
        return false;
    }

    public static void shutdown() {
        try {
            if (mNetworkServerControl!=null)
                mNetworkServerControl.shutdown();
        } catch (Exception ex) {
            log.error("Shutdown failed", ex);
        }
    }

    public static void start() {
        try {
            if (mNetworkServerControl!=null)
                mNetworkServerControl.start(new PrintWriter(System.out, true));
        } catch (Exception ex) {
            log.error("Start failed", ex);
        }
    }

    // Wait for the database to start
    private static void waitForStart() throws Exception {
        for (int i = 1; i <= MAX_PING_COUNT; i++) {
            try {
                if (mNetworkServerControl!=null)
                    mNetworkServerControl.ping();
                return;
            } catch (Exception ex) {
                log.debug("Derby Network Server Ping: " + i);
                if (i == MAX_PING_COUNT) {
                    throw ex;
                }
                Thread.sleep(5000);
            }
        }
    }

    // Test to see if we can connect to the database
    private static void testDatabase() throws Exception {
        if (log.isDebugEnabled())
            log.debug("Testing database");

        waitForStart();
        
        //Connection conn =
        //DriverManager.getConnection("jdbc:derby:galleon;user=galleon;password=galleon"+";create=true");
        Connection conn = DriverManager.getConnection(mHibernateProperties.getProperty("hibernate.connection.url")
                + ";create=true");
        
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select count(*) from sys.systables");
            if (resultSet.next()) {
                log.debug("number of rows in sys.systables = " + resultSet.getInt(1));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
        }
        if (log.isDebugEnabled())
            log.debug("Tested database");
    }

    // Determine if the Galleon schema already exists
    public static boolean findSchema() throws Exception {
        if (log.isDebugEnabled())
            log.debug("Finding schema");
        Connection conn = DriverManager.getConnection(mHibernateProperties.getProperty("hibernate.connection.url")
                + ";create=true");

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select count(*) from sys.sysschemas where schemaname='GALLEON'");
            if (resultSet.next()) {
                log.debug("number of rows in sys.sysschemas = " + resultSet.getInt(1));
                return resultSet.getInt(1) == 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
        }
        return false;
    }

    private static int mPort;

    private static NetworkServerControl mNetworkServerControl;

    private static Properties mHibernateProperties;
}