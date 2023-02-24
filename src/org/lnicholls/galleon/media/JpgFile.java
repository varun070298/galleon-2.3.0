package org.lnicholls.galleon.media;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Image;
import org.lnicholls.galleon.util.Tools;

import dk.jdai.model.EXIFInfo;

public final class JpgFile extends ImageFile {
	private static final Logger log = Logger.getLogger(JpgFile.class.getName());

	private static final String DEFAULT_TITLE = "unknown";

	public static final Image getImage(Image image, String filename) {
		image.setPath(filename);
		try {
			File file = new File(filename);
			image.setSize(file.length());
			image.setDateModified(new Date(file.lastModified()));
			// Extract the Exif header from the Jpeg file to retrieve the dates
			// info
			EXIFInfo info = new EXIFInfo(new File(filename));

			if (info != null) {
				Map properties = info.getEXIFMetaData();
				DateFormat dateformat = new SimpleDateFormat(
						"yyyy:MM:dd HH:mm:ss");
				String result = (String) properties.get("DateTimeOriginal");
				if (result != null && (result.indexOf("0000:00:00") < 0)) {
					image.setDateCreated(dateformat.parse(result));
				}

				result = (String) properties.get("DateTime");
				if (result != null && (result.indexOf("0000:00:00") < 0)) {
					image.setDateModified(dateformat.parse(result));
				}

				result = (String) properties.get("DateTimeDigitized");
				if (result != null && (result.indexOf("0000:00:00") < 0)) {
					image.setDateCaptured(dateformat.parse(result));
				}
			}

			// TODO Get rotation

			if (image.getTitle().equals(DEFAULT_TITLE)) {
				String value = Tools.extractName(file.getName());
				image.setTitle(value);
			}

		} catch (Exception ex) {
			Tools.logException(JpgFile.class, ex, filename);
		}
		return image;
	}
}