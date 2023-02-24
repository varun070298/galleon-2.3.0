package org.lnicholls.galleon.util;

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

import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

//import org.apache.log4j.Logger;

/*
 * Utility class loader for plugins deployed in jars
 *
 * @see java.security.SecureClassLoader
 */

public class JarClassLoader extends SecureClassLoader {
    //private static Logger log = Logger.getLogger(JarClassLoader.class.getName());

    public JarClassLoader(File file) throws IOException {
        mFile = file;
    }

    public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //if (log.isDebugEnabled())
        //    log.debug("loadClass: " + name);
        
        Class clas = null;

        // If the class has already been loaded, just return it.
        clas = findLoadedClass(name);
        if (clas != null) {
            if (resolve) {
                resolveClass(clas);
            }
            return clas;
        }

        String fileName = classToFile(name);

        byte[] data = loadResource(fileName);
        if (data != null && data.length > 0)
        {
            clas = defineClass(name, data, 0, data.length);
        }

        // We weren't able to get the class, so
        // use the default Classloader.
        if (clas == null) {
            //clas = Class.forName(name);
            //clas = super.loadClass(name,false);
            clas = getParent().loadClass(name);
        }

        // If we still can't find it, then it's a real
        // exception.
        if (clas == null) {
            throw new ClassNotFoundException(name);
        }

        // Resolve the class -- load all the classes
        // that this needs, and do any necessary linking.
        if (resolve) {
            resolveClass(clas);
        }

        // Return the class to the runtime system.
        return clas;
    }

    public static String fileToClass(String name) {
        char[] clsName = name.toCharArray();
        for (int i = clsName.length - 6; i >= 0; i--)
            if (clsName[i] == '/')
                clsName[i] = '.';
        return new String(clsName, 0, clsName.length - 6);
    }

    public static String classToFile(String name) {
        return name.replace('.', '/').concat(".class");
    }

    public InputStream getResourceAsStream(String name) {
        //if (log.isDebugEnabled())
        //    log.debug("getResourceAsStream: " + name);
        byte[] resourceBytes = loadResource(name);
        if (resourceBytes != null) {
            //if (log.isDebugEnabled())
            //    log.debug("getResourceAsStream: resourceBytes=" + resourceBytes.length);
            return new ByteArrayInputStream(resourceBytes);
        }
        return super.getSystemResourceAsStream(name);
    }

    public URL getResource(String name) {
        //if (log.isDebugEnabled())
        //    log.debug("getResource: " + name);
        JarFile jar = null;
        try {
            jar = new JarFile(mFile);
            ZipEntry entry = jar.getEntry(name);
            if (entry != null) {
                return makeURL(name);
            }
        } catch (IOException ex) {
            //log.debug("Failed to load resource " + name + " from " + mPluginDescriptor.getJar());
        } finally {
            try {
                if (jar != null)
                    jar.close();
            } catch (IOException e) {
            }
        }
        return super.getSystemResource(name);
    }

    /*
     * protected PermissionCollection getPermissions(CodeSource codesource) { Permissions permissions = new
     * Permissions(); AllPermission allPermission = new AllPermission(); permissions.add(allPermission);
     *
     * return permissions; }
     */

    private byte[] loadResource(String name) {
        //if (log.isDebugEnabled())
        //    log.debug("loadResource: " + name);
        JarFile jar = null;
        BufferedInputStream bis = null;
        try {
            jar = new JarFile(mFile);
            ZipEntry entry = jar.getEntry(name);
            if (entry != null) {
                bis = new BufferedInputStream(jar.getInputStream(entry));
                int len = (int) entry.getSize();
                byte[] data = new byte[len];
                int success = bis.read(data, 0, len);
                if (success == -1) {
                    throw new ClassNotFoundException(name);
                }
                return data;
            } else {
                throw new ClassNotFoundException(name);
            }
        } catch (Exception e) {
            //log.debug("Failed to load resource " + name + " from " + mPluginDescriptor.getJar());
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException e) {
            }
            try {
                if (jar != null)
                    jar.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    private URL makeURL(String name) throws MalformedURLException {
        // jar:file:///c:/javahmo/plugins/plugin/test.jar!/cross.gif'
        StringBuffer path = new StringBuffer("file:///").append(mFile.getAbsolutePath()).append("!/").append(name);
        return new URL("jar", "", path.toString().replace('\\', '/'));
    }

    private File mFile;
}