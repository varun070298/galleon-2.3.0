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

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.lnicholls.galleon.util.Tools;

public class Skin {

    private static Logger log = Logger.getLogger(Skin.class.getName());

    public Skin(String path) {
        mPath = path;
        mSkinLoader = new SkinLoader(path);
        mSkinDescriptor = parse(new String(mSkinLoader.findResource("skin.xml").toByteArray()));
    }

    private SkinDescriptor parse(String value) {
        try {
            SAXReader saxReader = new SAXReader();
            StringReader stringReader = new StringReader(value);
            Document document = saxReader.read(stringReader);
            //Document document = saxReader.read(new File("d:/galleon/skin.xml"));

            SkinDescriptor skinDescriptor = new SkinDescriptor();

            Element root = document.getRootElement(); // check for errors
            skinDescriptor.setVersion(Tools.getAttribute(root, "version"));
            skinDescriptor.setTitle(Tools.getAttribute(root, "title"));
            skinDescriptor.setReleaseDate(Tools.getAttribute(root, "releaseDate"));
            skinDescriptor.setDescription(Tools.getAttribute(root, "description"));
            skinDescriptor.setAuthorName(Tools.getAttribute(root, "authorName"));
            skinDescriptor.setAuthorEmail(Tools.getAttribute(root, "authorEmail"));
            skinDescriptor.setAuthorHomepage(Tools.getAttribute(root, "authorHomepage"));

            for (Iterator imageIterator = root.elementIterator("image"); imageIterator.hasNext();) {
                Element element = (Element) imageIterator.next();
                Descriptor.Image image = new Descriptor.Image();
                image.setSource(Tools.getAttribute(element, "source"));
                image.setId(Tools.getAttribute(element, "id"));
                skinDescriptor.addImage(image);
            }

            for (Iterator i = root.elementIterator("app"); i.hasNext();) {
                Element appElement = (Element) i.next();
                SkinDescriptor.App app = new SkinDescriptor.App();
                app.setId(Tools.getAttribute(appElement, "id"));

                for (Iterator imageIterator = appElement.elementIterator("image"); imageIterator.hasNext();) {
                    Element element = (Element) imageIterator.next();
                    Descriptor.Image image = new Descriptor.Image();
                    image.setSource(Tools.getAttribute(element, "source"));
                    image.setId(Tools.getAttribute(element, "id"));
                    app.addImage(image);
                }

                for (Iterator screenIterator = appElement.elementIterator("screen"); screenIterator.hasNext();) {
                    Element screenElement = (Element) screenIterator.next();
                    SkinDescriptor.App.Screen screen = new SkinDescriptor.App.Screen();
                    screen.setId(Tools.getAttribute(screenElement, "id"));

                    for (Iterator imageIterator = screenElement.elementIterator("image"); imageIterator.hasNext();) {
                        Element element = (Element) imageIterator.next();
                        Descriptor.Image image = new Descriptor.Image();
                        image.setSource(Tools.getAttribute(element, "source"));
                        image.setId(Tools.getAttribute(element, "id"));
                        screen.addImage(image);
                    }

                    app.addScreen(screen);
                }
                skinDescriptor.addApp(app);
            }

            return skinDescriptor;
        } catch (Exception ex) {
            log.error("Could not parse skin definition", ex);
        }
        return null;
    }

    public ByteArrayOutputStream getImage(String appId, String screenId, String id) {
        String image = mSkinDescriptor.getImage(appId, screenId, id);
        if (image != null) {
            //return (BufferedImage)mSkinLoader.getResource(image);
            return mSkinLoader.findResource(image);
        }
        return null;
    }

    public String getPath() {
        return mPath;
    }

    private String mPath;

    private SkinLoader mSkinLoader;

    private SkinDescriptor mSkinDescriptor;
}