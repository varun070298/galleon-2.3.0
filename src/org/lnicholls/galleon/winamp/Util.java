package org.lnicholls.galleon.winamp;

/*
 * 
 * JavaZOOM : jlgui@javazoom.net
 *            http://www.javazoom.net 
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

public class Util {
    public static Image createBanner(String characters, Image fonts, int fontWidth, int fontHeight, int Yspacing,
            String text) {
        int imageWidth = fonts.getWidth(null);
        int imageHeight = fonts.getHeight(null);

        int pixels[] = new int[text.length() * fontWidth * fontHeight];

        int spaceCharOffset = findCharOffset(characters, ' ');
        int charsPerLine = (int) Math.rint((imageWidth / fontWidth));

        for (int offset = 0; offset < text.length(); offset++) {
            int xPos = 0;
            int yPos = 0;
            int reste = 0;
            int entie = 0;
            int offsetA = findCharOffset(characters, text.charAt(offset));

            if (offsetA < characters.length()) {
                reste = offsetA % charsPerLine;
                entie = (offsetA - reste);
                xPos = reste * fontWidth;
                yPos = ((entie / charsPerLine) * fontHeight) + ((entie / charsPerLine) * Yspacing);
            } 
            else
            {
                reste = spaceCharOffset % charsPerLine;
                entie = (spaceCharOffset - reste);
                xPos = reste * fontWidth;
                yPos = ((entie / charsPerLine) * fontHeight) + ((entie / charsPerLine) * Yspacing);
            }

            PixelGrabber pg = new PixelGrabber(fonts, xPos, yPos, fontWidth, fontHeight, pixels, offset * fontWidth,
                    text.length() * fontWidth);
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
            }
        }

        return Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(text.length() * fontWidth, fontHeight, pixels, 0, text.length() * fontWidth));
    }

    private static int findCharOffset(String characters, char c) {
        for (int i = 0; i < characters.length(); i++) {
            if (characters.charAt(i) == c)
                return i;
        }
        return 0;
    }

    public static Image cropImage(Image image, int x, int y, int width, int height) {
        return Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(image.getSource(), new CropImageFilter(x, y, width, height)));
    }
}