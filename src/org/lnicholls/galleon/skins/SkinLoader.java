package org.lnicholls.galleon.skins;

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


import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;

import org.lnicholls.galleon.util.*;

public class SkinLoader {

    public SkinLoader(String filename) {
        mFilename = filename;
        mResources = new Hashtable();
        //loadResource(filename);
    }
    
    public ByteArrayOutputStream findResource(String key) {
        if (mLastObject!=null && mLastKey!=null)
        {
            if (mLastKey.toLowerCase().equals(key.toLowerCase()))
                return mLastObject;
        }
        
        ZipInputStream wsz = null;
        try {
            wsz = new ZipInputStream(new FileInputStream(mFilename));
            ZipEntry resource = wsz.getNextEntry();
            while (resource != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int success = wsz.read(data);
                while (success != -1) {
                    baos.write(data, 0, success);
                    success = wsz.read(data);
                }
                baos.close();
            
                String name = resource.getName().toLowerCase();
                if (!resource.isDirectory() && name.toLowerCase().equals(key.toLowerCase()))
                {
                    /*
                    if (name.endsWith("png") || name.endsWith("gif") || name.endsWith("jpg") || name.endsWith("jpeg"))
                    {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                        
                        mLastKey = key;
                        mLastObject = Tools.getImage(image);
                        return mLastObject;
                    }
                    else
                    if (name.endsWith("xml") || name.endsWith("js"))
                    {
                        mLastKey = key;
                        mLastObject = new String(baos.toByteArray());
                        return mLastObject;
                    }
                    else                
                    if (resource.getName().toLowerCase().endsWith("ttf"))
                    {
                        mLastKey = key;
                        mLastObject = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(baos.toByteArray()));
                        return mLastObject;
                    }
                    */
                    mLastKey = key;
                    mLastObject = baos;
                    return baos;
                }
                resource = wsz.getNextEntry();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (wsz != null)
                    wsz.close();
            } catch (IOException ex) {
            }
        }
        return null;
    }    

    private void loadResource(String filename) {
        ZipInputStream wsz = null;
        try {
            wsz = new ZipInputStream(new FileInputStream(filename));
            ZipEntry resource = wsz.getNextEntry();
            while (resource != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int success = wsz.read(data);
                while (success != -1) {
                    baos.write(data, 0, success);
                    success = wsz.read(data);
                }
                baos.close();
            
                String name = resource.getName().toLowerCase();
                if (!resource.isDirectory())
                {
                    if (name.endsWith("png") || name.endsWith("gif") || name.endsWith("jpg") || name.endsWith("jpeg"))
                    {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                        
                        mResources.put(resource.getName(), Tools.getImage(image));
                    }
                    else
                    if (name.endsWith("xml") || name.endsWith("js"))
                    {
                        mResources.put(resource.getName(), new String(baos.toByteArray()));
                    }
                    else                
                    if (resource.getName().toLowerCase().endsWith("ttf"))
                    {
                        mResources.put(resource.getName(), Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(baos.toByteArray())));
                    }
                }
                resource = wsz.getNextEntry();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (wsz != null)
                    wsz.close();
            } catch (IOException ex) {
            }
        }
    }
    
   public Object getResource(String name)
   {
       if (name!=null) 
           return mResources.get(name.toLowerCase());
       else
           return null;
   } 
   
   private String mFilename;
   private Hashtable mResources;
   private ByteArrayOutputStream mLastObject;
   private String mLastKey;
}