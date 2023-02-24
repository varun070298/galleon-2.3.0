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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;
import net.sf.hibernate.tool.hbm2ddl.SchemaUpdate;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.server.*;

public class HibernateUtil {

    private static Logger log = Logger.getLogger(HibernateUtil.class.getName());

    private static Configuration configuration;

    private static SessionFactory sessionFactory;

    private static final ThreadLocal session = new ThreadLocal();

    public static void initialize() throws Exception {
        // Initialize static variables
        log.info("Initializing Hibernate");
        try {
            configuration = new Configuration();
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(System.getProperty("conf") + "/hibernate.properties")));
            configuration.setProperties(properties);

            // Tell Hibernate about the classes we want mapped, taking advantage of
            // the way we've named their mapping documents (*.hbm.xml).
            configuration.addClass(Version.class);
            configuration.addClass(Audio.class);
            configuration.addClass(Image.class);
            configuration.addClass(Video.class);
            configuration.addClass(Thumbnail.class);
            configuration.addClass(PersistentValue.class);
            configuration.addClass(Podcast.class);
            configuration.addClass(Movie.class);
            configuration.addClass(Theater.class);
            configuration.addClass(Application.class);
            configuration.addClass(Videocast.class);
            configuration.addClass(Playlists.class);
            configuration.addClass(PlaylistsTracks.class);
            configuration.addClass(ShoutcastStation.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
        log.info("Initialized Hibernate");
    }

    public static Session openSession() throws HibernateException {
        Session s = (Session) session.get();
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    public static void closeSession() throws HibernateException {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
        {
            s.close();
            s = null;
        }
    }

    // Determine if the database schema is up to date with the current Galleon version
    private static boolean currentVersion() throws HibernateException {

        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query sqlQuery = session.createSQLQuery(
                    "select {version.*} from version {version} order by dateAdded desc", "version", Version.class);
            //sqlQuery.setMaxResults(1); // doesnt work for Derby
            List versions = sqlQuery.list();
            tx.commit();
            if (versions.size() > 0) {
                Version version = (Version) versions.get(0);
                return version.getMajor() == Constants.CURRENT_VERSION.getMajor()
                        && version.getRelease() == Constants.CURRENT_VERSION.getRelease()
                        && version.getMaintenance() == Constants.CURRENT_VERSION.getMaintenance()
                        && version.getDevelopment() == Constants.CURRENT_VERSION.getDevelopment();
            }
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        return false;
    }

    // Update the database schema version to the current Galleon version
    private static void updateVersion() throws HibernateException {
        log.info("Updating database schema version");
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Version current = new Version(Constants.CURRENT_VERSION.getMajor(), Constants.CURRENT_VERSION.getRelease(), Constants.CURRENT_VERSION.getMaintenance(),Constants.CURRENT_VERSION.getDevelopment(),new Date());
            session.save(current);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        log.info("Updated database schema version");
    }

    // Create a new Galleon schema in the database
    public static void createSchema() throws HibernateException {
        log.info("Creating database schema");
        try {
            SchemaExport schemaExport = new SchemaExport(configuration);
            schemaExport.create(true, true);
        } catch (Exception ex) {
            Tools.logException(HibernateUtil.class, ex);
        }
        log.info("Created database schema");
        updateVersion();
    }

    // Update the existing Galleon schema to the current schema version
    public static void updateSchema() throws HibernateException {
        if (!currentVersion()) {
            log.info("Updating database schema");
            try {
                SchemaUpdate schemaUpdate = new SchemaUpdate(configuration);
                schemaUpdate.execute(true, true);
            } catch (Exception ex) {
                Tools.logException(HibernateUtil.class, ex);
            }
            log.info("Updated database schema");
            updateVersion();
        }
    }
}