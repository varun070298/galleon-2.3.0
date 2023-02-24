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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import javax.imageio.ImageIO;

import net.sf.hibernate.lob.BlobImpl;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Image;
import org.lnicholls.galleon.database.ImageManager;
import org.lnicholls.galleon.database.Thumbnail;
import org.lnicholls.galleon.database.ThumbnailManager;
import org.lnicholls.galleon.util.Tools;

import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import dk.jdai.model.EXIFInfo;

public class ImageFile {
	private static final Logger log = Logger.getLogger(ImageFile.class
			.getName());

	private static final String DEFAULT_TITLE = "unknown";

	public static Image getImage(String filename) {
		Image image = new Image();
		defaultProperties(image);
		return getImage(image, filename);
	}

	public static Image getImage(Image image, String filename) {
		image.setPath(filename);
		try {
			File file = new File(filename);
			image.setSize(file.length());
			Date date = new Date(file.lastModified());
			image.setDateModified(date);
			image.setDateCreated(date);
			image.setDateCaptured(date);
			image.setMimeType("image/"+filename.substring(filename.indexOf('.')));

			if (image.getTitle().equals(DEFAULT_TITLE)) {
				String value = Tools.extractName(file.getName());
				image.setTitle(value);
			}

		} catch (Exception ex) {
			Tools.logException(ImageFile.class, ex, filename);
		}
		return image;
	}

	public static void defaultProperties(Image image) {
		image.setTitle(DEFAULT_TITLE);
		image.setSize(-1);
		image.setComments("");
		image.setMimeType("image/jpeg");
		image.setDateCreated(new Date());
		image.setDateCaptured(new Date());
		image.setDateModified(new Date());
		image.setDateAdded(new Date());
		image.setDatePlayed(new Date());
		image.setPath("");
		image.setPlayCount(0);
		image.setRating(0);
		image.setTone("");
	}

	public static final BufferedImage getThumbnail(Image image) {
		try {
			BufferedImage thumbnailImage = null;
			if (image.getThumbnail() != null) {
				thumbnailImage = ThumbnailManager.findImageById(image
						.getThumbnail());
			}

			if (thumbnailImage == null) {
				// Extract the thumbnail data from the Exif header of the Jpeg
				// file
				EXIFInfo info = new EXIFInfo(new File(image.getPath()));

				// Does the Jpeg have a embedded thumbnail; true for most modern
				// digital camera images
				thumbnailImage = info.getThumbnail();
			}

			if (thumbnailImage == null) {
				mLock.writeLock().acquire();
				try {
					File file = new File(image.getPath());
					if (file.exists()) {
						BufferedImage photo = Tools.ImageIORead(file);

						if (photo != null) {
							photo = (BufferedImage) Tools.getImage(photo);
							thumbnailImage = ImageManipulator.getScaledImage(
									photo, 200, 200);

							try {
								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								JPEGImageEncoder encoder = JPEGCodec
										.createJPEGEncoder(byteArrayOutputStream);
								encoder.encode(thumbnailImage);
								byteArrayOutputStream.close();

								ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
										byteArrayOutputStream.toByteArray());

								BlobImpl blob = new BlobImpl(
										byteArrayInputStream,
										byteArrayOutputStream.size());

								Thumbnail thumbnail = new Thumbnail("JpgFile",
										"image/jpg", image.getPath());
								thumbnail.setImage(blob);
								thumbnail.setDateModified(new Date());
								ThumbnailManager.createThumbnail(thumbnail);

								image.setThumbnail(thumbnail.getId());
								ImageManager.updateImage(image);
							} catch (Exception ex) {
								Tools.logException(ImageFile.class, ex, image
										.getPath());
							}
							photo.flush();
						}
						photo = null;
					}
				} finally {
					mLock.writeLock().release();
				}
			}
			return thumbnailImage;
		} catch (Exception ex) {
			Tools.logException(ImageFile.class, ex, image.getPath());
		}

		return null;
	}

	private static FIFOReadWriteLock mLock = new FIFOReadWriteLock();
}