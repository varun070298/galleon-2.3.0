package org.lnicholls.galleon.winamp;

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

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SkinLoader {

    public SkinLoader(String filename) {
        mResources = new Hashtable();
        loadResource(filename);
    }

    private void loadResource(String filename) {
        ZipInputStream input = null;
        try {
            input = new ZipInputStream(new FileInputStream(filename));
            ZipEntry resource = input.getNextEntry();
            while (resource != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int success = input.read(data);
                while (success != -1) {
                    baos.write(data, 0, success);
                    success = input.read(data);
                }
                baos.close();

                String name = resource.getName().toLowerCase();
                if (name.endsWith("bmp")) {
                    BMPLoader bmp = new BMPLoader();
                    mResources.put(name, bmp.getBMPImage(new ByteArrayInputStream(baos.toByteArray())));
                } else if (name.endsWith("txt")) {
                    mResources.put(name, new String(baos.toByteArray()));
                } else if (resource.getName().toLowerCase().endsWith("ttf")) {
                    mResources.put(resource.getName(), Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(
                            baos.toByteArray())));
                }
                resource = input.getNextEntry();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException ex) {
            }
        }
    }

    public Object getResource(String name) {
        return mResources.get(name.toLowerCase());
    }

    private Hashtable mResources;
}