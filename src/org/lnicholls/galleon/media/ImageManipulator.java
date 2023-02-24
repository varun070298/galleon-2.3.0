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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class ImageManipulator {
    
    private static Logger log = Logger.getLogger(ImageManipulator.class.getName());

    private static HashMap mRenderingHintsMap;

    private static RenderingHints mRenderingHints;

    static {
        mRenderingHintsMap = new HashMap(5);
        mRenderingHintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        mRenderingHintsMap.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        mRenderingHintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        mRenderingHintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        mRenderingHintsMap.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        mRenderingHints = new RenderingHints(mRenderingHintsMap);
        
        ImageIO.setUseCache(false);
    }

    public static BufferedImage getScaledImage(BufferedImage photo, int width, int height) {
    	if (photo.getWidth()==width && photo.getHeight()==height)
    		return photo;
    	
        double heightScale = (double) height / (double) photo.getHeight();
        double widthScale = (double) width / (double) photo.getWidth();
        double scaleFactor = 1.0;
        if (heightScale < widthScale)
            scaleFactor = heightScale;
        else
            scaleFactor = widthScale;

        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleFactor, scaleFactor),
                mRenderingHints);
        BufferedImage scaledImage = op.filter(photo, null);
        photo.flush();
        photo = null;
        op = null;

        return scaledImage;
    }
    
    public static BufferedImage rotate(BufferedImage photo, int width, int height, int degrees) {
        if (log.isDebugEnabled())
            log.debug("rotate is "+degrees);
        int imageWidth = photo.getWidth();
        int imageHeight = photo.getHeight();
        //Math.toDegrees(parameters.getRotate()) % 360;
        // Only rotate if neccessary
        if (degrees != 0) {
            // Handle the two special cases where the image is turned on its side
            double rotate = Math.toRadians(degrees);
            if (degrees == 90.0 || degrees == 270.0) {
                if (log.isDebugEnabled())
                    log.debug("Angle is 90 or 270");
                // Calculate the longest side of the image
                if (imageWidth > imageHeight) {
                    if (log.isDebugEnabled())
                        log.debug("Width>Height");
                    
                    //AffineTransform tx = AffineTransform.getScaleInstance(1.0D, -1D);
                    //AffineTransformOp op = new AffineTransformOp(tx, mRenderingHints);
                    //photo = op.filter(photo, null);
                    
                    // Create an image big enough so that if the original image is rotated it will fit inside
                    BufferedImage rotatedImage = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2D = rotatedImage.createGraphics();
                    graphics2D.setRenderingHints(mRenderingHintsMap);
                    // Place the original image inside the new image horizontally centered
                    graphics2D.drawImage(photo, 0, imageWidth / 2 - imageHeight / 2, imageWidth, imageHeight, null);
                    // Rotate around the center of the image
                    AffineTransformOp op = new AffineTransformOp(AffineTransform.getRotateInstance(rotate,
                            (float) imageWidth / 2.0, (float) imageWidth / 2.0), mRenderingHints);
                    BufferedImage oldImage = photo;
                    rotatedImage = op.filter(rotatedImage, null);
                    oldImage.flush();
                    oldImage = null;
                    graphics2D.dispose();
                    graphics2D = null;
                    
                    // Trim off the unneccessary edges
                    CropImageFilter cropFilter = new CropImageFilter(imageWidth / 2 - imageHeight / 2, 0,
                            imageHeight, imageWidth);
                    ImageProducer imageProducer = rotatedImage.getSource();
                    Image filteredImage = Toolkit.getDefaultToolkit().createImage(
                            new FilteredImageSource(imageProducer, cropFilter));
                    rotatedImage.flush();
                    rotatedImage = null;

                    photo = new BufferedImage(filteredImage.getWidth(null), filteredImage.getHeight(null),
                            BufferedImage.TYPE_INT_RGB);
                    graphics2D = photo.createGraphics();
                    graphics2D.setRenderingHints(mRenderingHintsMap);
                    graphics2D.drawImage(filteredImage, 0, 0, filteredImage.getWidth(null), filteredImage
                            .getHeight(null), null);
                    filteredImage.flush();
                    filteredImage = null;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Height>Width");
                    // Create an image big enough so that if the original image is rotated it will fit inside
                    BufferedImage rotatedImage = new BufferedImage(imageHeight, imageHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2D = rotatedImage.createGraphics();
                    graphics2D.setRenderingHints(mRenderingHintsMap);
                    // Place the original image inside the new image vertically centered
                    graphics2D.drawImage(photo, imageHeight / 2 - imageWidth / 2, 0, imageHeight, imageHeight, null);
                    // Rotate around the center of the image
                    AffineTransformOp op = new AffineTransformOp(AffineTransform.getRotateInstance(rotate,
                            (float) imageHeight / 2.0, (float) imageHeight / 2.0), null);
                    BufferedImage oldImage = photo;
                    BufferedImage rotated = op.filter(rotatedImage, null);
                    oldImage.flush();
                    oldImage = null;
                    graphics2D.dispose();
                    graphics2D = null;
                    rotatedImage.flush();
                    rotatedImage = null;

                    //Trim off the unneccessary edges
                    CropImageFilter cropFilter = new CropImageFilter(0, rotated.getHeight() / 2 - imageWidth / 2,
                            imageHeight, imageWidth);
                    ImageProducer imageProducer = rotated.getSource();
                    Image filteredImage = Toolkit.getDefaultToolkit().createImage(
                            new FilteredImageSource(imageProducer, cropFilter));
                    rotated.flush();
                    rotated = null;

                    photo = new BufferedImage(filteredImage.getWidth(null), filteredImage.getHeight(null),
                            BufferedImage.TYPE_INT_RGB);
                    graphics2D = photo.createGraphics();
                    graphics2D.setRenderingHints(mRenderingHintsMap);
                    graphics2D.drawImage(filteredImage, 0, 0, filteredImage.getWidth(null), filteredImage
                            .getHeight(null), null);
                    filteredImage.flush();
                    filteredImage = null;
                }
            } else {
                AffineTransformOp op = new AffineTransformOp(AffineTransform.getRotateInstance(rotate,
                        (float) imageWidth / 2.0, (float) imageHeight / 2.0), mRenderingHints);
                BufferedImage oldImage = photo;
                photo = op.filter(photo, null);
                oldImage.flush();
                oldImage = null;
            }
        }
        return photo;
    }    
}