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

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.lnicholls.galleon.util.Tools;

import com.tivo.hme.sdk.View;

/* Modified from JavaZoom code: http://www.javazoom.net
 */

public class ClassicSkin {

    private static final int SCALE_FACTOR = 2;

    private static final int[] ButtonsReleasedPanel = { 0, 0, 23, 18, 23, 0, 23, 18, 46, 0, 23, 18, 69, 0, 23, 18, 92,
            0, 22, 18, 114, 0, 22, 16 };

    private static final int[] ButtonsPressedPanel = { 0, 18, 23, 18, 23, 18, 23, 18, 46, 18, 23, 18, 69, 18, 23, 18,
            92, 18, 22, 18, 114, 16, 22, 16 };

    private static final int[] ButtonsPanelLocation = { 16, 88, 39, 88, 62, 88, 85, 88, 108, 88, 136, 89 };

    private static final int mFontWidth = 5;

    private static final int mFontHeight = 6;

    private static final String mFontIndex = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\"@a  0123456789  :()-'!_+ /[]^&%.=$#   ?*";

    private static final int[] mSampleRateLocation = { 156, 43 };

    private static final int[] mBitsRateLocation = { 110, 43 };

    private static final int[] mTitleLocation = { 111, 27 };

    private static final int[] mActiveModePanel = { 0, 0, 28, 12, 29, 0, 27, 12 };

    private static final int[] mPassiveModePanel = { 0, 12, 28, 12, 29, 12, 27, 12 };

    private static final int[] mMonoLocation = { 212, 41 };

    private static final int[] mStereoLocation = { 239, 41 };

    private static final int mNumberWidth = 9;

    private static final int mNumberHeight = 13;

    private static final String mNumberIndex = "0123456789 ";

    private static final int[] mMinute0Location = { 48, 26 };

    private static final int[] mMinuteLocation = { 60, 26 };

    private static final int[] mSecond0Location = { 78, 26 };

    private static final int[] mSecondLocation = { 90, 26 };

    private static final int[] mIconsPanel = { 0, 0, 9, 9, 9, 0, 9, 9, 18, 0, 9, 9, 36, 0, 3, 9, 27, 0, 2, 9 };

    private static final int[] mIconsLocation = { 26, 27, 24, 27 };

    private static final int[] mReleasedEPSRPanel = { 28, 0, 47, 14, 0, 0, 28, 14 };

    private static final int[] mPressedEPSRPanel = { 28, 30, 47, 14, 0, 30, 28, 14 };

    private static final int[] mPanelEPSRLocation = { 164, 89, 212, 89 };

    private static final int[] mPositionPanel = { 0, 0, 247, 10 };

    private static final int[] mReleasedPositionPanel = { 248, 0, 28, 10 };

    private static final int[] mPressedPositionPanel = { 278, 0, 28, 10 };

    private static final int mDeltaPositionBar = 231;

    private static final int[] mPositionBarLocation = { 16, 72 };

    public ClassicSkin(String filename) {
        mSkinLoader = new SkinLoader(filename);

        mButtonReleasedImages = new Image[6];
        mButtonPressedImages = new Image[6];
        mActiveModeImage = new Image[2];
        mPassiveModeImage = new Image[2];
        mIconsImage = new Image[5];
        mReleasedEPSRImage = new Image[2];
        mPressedEPSRImage = new Image[2];
        mPositionBar = new Image[1];
        mReleasedPositionImage = new Image[1];
        mPressedPositionImage = new Image[1];

        mMain = (Image) mSkinLoader.getResource("main.bmp");
        mMain = mMain.getScaledInstance(mMain.getWidth(null) * SCALE_FACTOR, mMain.getHeight(null) * SCALE_FACTOR,
                Image.SCALE_AREA_AVERAGING);

        mText = (Image) mSkinLoader.getResource("text.bmp");
        mText = mText.getScaledInstance(mText.getWidth(null) * SCALE_FACTOR, mText.getHeight(null) * SCALE_FACTOR,
                Image.SCALE_AREA_AVERAGING);
        mNumbers = (Image) mSkinLoader.getResource("numbers.bmp");
        if (mNumbers == null)
            mNumbers = (Image) mSkinLoader.getResource("nums_ex.bmp");
        mNumbers = mNumbers.getScaledInstance(mNumbers.getWidth(null) * SCALE_FACTOR, mNumbers.getHeight(null)
                * SCALE_FACTOR, Image.SCALE_AREA_AVERAGING);

        // Buttons
        Image image = (Image) mSkinLoader.getResource("cbuttons.bmp");
        image = image.getScaledInstance(image.getWidth(null) * SCALE_FACTOR, image.getHeight(null) * SCALE_FACTOR,
                Image.SCALE_AREA_AVERAGING);
        parsePanel(ButtonsReleasedPanel, image, mButtonReleasedImages);
        parsePanel(ButtonsPressedPanel, image, mButtonPressedImages);
        image.flush();
        image = null;

        // Mono/Stereo
        image = (Image) mSkinLoader.getResource("monoster.bmp");
        image = image.getScaledInstance(image.getWidth(null) * SCALE_FACTOR, image.getHeight(null) * 2,
                Image.SCALE_AREA_AVERAGING);
        parsePanel(mActiveModePanel, image, mActiveModeImage);
        parsePanel(mPassiveModePanel, image, mPassiveModeImage);
        image.flush();
        image = null;

        // Icons
        image = (Image) mSkinLoader.getResource("playpaus.bmp");
        image = image.getScaledInstance(image.getWidth(null) * SCALE_FACTOR, image.getHeight(null) * SCALE_FACTOR,
                Image.SCALE_AREA_AVERAGING);
        parsePanel(mIconsPanel, image, mIconsImage);
        image.flush();
        image = null;

        // Shuffle/Repeat
        image = (Image) mSkinLoader.getResource("shufrep.bmp");
        image = image.getScaledInstance(image.getWidth(null) * SCALE_FACTOR, image.getHeight(null) * SCALE_FACTOR,
                Image.SCALE_AREA_AVERAGING);
        parsePanel(mReleasedEPSRPanel, image, mReleasedEPSRImage);
        parsePanel(mPressedEPSRPanel, image, mPressedEPSRImage);
        image.flush();
        image = null;

        // Position
        image = (Image) mSkinLoader.getResource("posbar.bmp");
        mPositionPanel[3] = image.getHeight(null);
        mReleasedPositionPanel[3] = image.getHeight(null);
        mPressedPositionPanel[3] = image.getHeight(null);
        image = image.getScaledInstance(image.getWidth(null) * SCALE_FACTOR, image.getHeight(null) * SCALE_FACTOR,
                Image.SCALE_AREA_AVERAGING);
        parsePanel(mPositionPanel, image, mPositionBar);
        parsePanel(mReleasedPositionPanel, image, mReleasedPositionImage);
        parsePanel(mPressedPositionPanel, image, mPressedPositionImage);
        image.flush();
        image = null;

        // Numbers
        for (int h = 0; h < mNumberIndex.length(); h++) {
            mTimeImage[h] = Util.createBanner(mNumberIndex, mNumbers, mNumberWidth * SCALE_FACTOR, mNumberHeight
                    * SCALE_FACTOR, 0, "" + mNumberIndex.charAt(h));
        }
    }

    public View getMain(View root) {
        int imageWidth = mMain.getWidth(null);
        int imageHeigth = mMain.getHeight(null);
        View main = new View(root, (root.getWidth() - imageWidth) / 2, (root.getHeight() - imageHeigth) / 2, imageWidth,
                imageHeigth);
        main.setResource(main.createImage(mMain));
        return main;
    }

    public ImageControl getPreviousControl(View player) {
        return new ImageControl(player, ButtonsPanelLocation[0] * SCALE_FACTOR, ButtonsPanelLocation[1] * SCALE_FACTOR,
                mButtonPressedImages[0].getWidth(null), mButtonPressedImages[0].getHeight(null),
                mButtonPressedImages[0], mButtonReleasedImages[0]);
    }

    public ImageControl getPlayControl(View player) {
        return new ImageControl(player, ButtonsPanelLocation[2] * SCALE_FACTOR, ButtonsPanelLocation[3] * SCALE_FACTOR,
                mButtonPressedImages[1].getWidth(null), mButtonPressedImages[1].getHeight(null),
                mButtonPressedImages[1], mButtonReleasedImages[1]);
    }

    public ImageControl getPauseControl(View player) {
        return new ImageControl(player, ButtonsPanelLocation[4] * SCALE_FACTOR, ButtonsPanelLocation[5] * SCALE_FACTOR,
                mButtonPressedImages[2].getWidth(null), mButtonPressedImages[2].getHeight(null),
                mButtonPressedImages[2], mButtonReleasedImages[2]);
    }

    public ImageControl getStopControl(View player) {
        return new ImageControl(player, ButtonsPanelLocation[6] * SCALE_FACTOR, ButtonsPanelLocation[7] * SCALE_FACTOR,
                mButtonPressedImages[3].getWidth(null), mButtonPressedImages[3].getHeight(null),
                mButtonPressedImages[3], mButtonReleasedImages[3]);
    }

    public ImageControl getNextControl(View player) {
        return new ImageControl(player, ButtonsPanelLocation[8] * SCALE_FACTOR, ButtonsPanelLocation[9] * SCALE_FACTOR,
                mButtonPressedImages[4].getWidth(null), mButtonPressedImages[4].getHeight(null),
                mButtonPressedImages[4], mButtonReleasedImages[4]);
    }

    public ImageControl getEjectControl(View player) {
        return new ImageControl(player, ButtonsPanelLocation[10] * SCALE_FACTOR, ButtonsPanelLocation[11]
                * SCALE_FACTOR, mButtonPressedImages[5].getWidth(null), mButtonPressedImages[5].getHeight(null),
                mButtonPressedImages[5], mButtonReleasedImages[5]);
    }

    public ScrollTextControl getTitle(View player) {
        ScrollTextControl text = new ScrollTextControl(player, mTitleLocation[0] * SCALE_FACTOR, mTitleLocation[1]
                * SCALE_FACTOR, 155 * SCALE_FACTOR, 6 * SCALE_FACTOR, mFontIndex, mText, mFontWidth * SCALE_FACTOR,
                mFontHeight * SCALE_FACTOR, 0);
        return text;
    }

    public View getStereoActive(View player) {
        View stereo = new View(player, mStereoLocation[0] * SCALE_FACTOR, mStereoLocation[1] * SCALE_FACTOR,
                mActiveModeImage[0].getWidth(null), mActiveModeImage[0].getHeight(null));
        stereo.setResource(stereo.createImage(mActiveModeImage[0]));
        return stereo;
    }

    public View getStereoPassive(View player) {
        View stereo = new View(player, mStereoLocation[0] * SCALE_FACTOR, mStereoLocation[1] * SCALE_FACTOR,
                mPassiveModeImage[0].getWidth(null), mPassiveModeImage[0].getHeight(null));
        stereo.setResource(stereo.createImage(mPassiveModeImage[0]));
        return stereo;
    }

    public View getMonoActive(View player) {
        View mono = new View(player, mMonoLocation[0] * SCALE_FACTOR, mMonoLocation[1] * SCALE_FACTOR,
                mActiveModeImage[1].getWidth(null), mActiveModeImage[1].getHeight(null));
        mono.setResource(mono.createImage(mActiveModeImage[1]));
        return mono;
    }

    public View getMonoPassive(View player) {
        View mono = new View(player, mMonoLocation[0] * SCALE_FACTOR, mMonoLocation[1] * SCALE_FACTOR,
                mPassiveModeImage[1].getWidth(null), mPassiveModeImage[1].getHeight(null));
        mono.setResource(mono.createImage(mPassiveModeImage[1]));
        return mono;
    }

    public TextControl getSampleRate(View player, String rate) {
        TextControl text = new TextControl(player, mSampleRateLocation[0] * SCALE_FACTOR, mSampleRateLocation[1]
                * SCALE_FACTOR, mFontWidth * rate.length() * SCALE_FACTOR, 6 * SCALE_FACTOR, mFontIndex, mText,
                mFontWidth * SCALE_FACTOR, mFontHeight * SCALE_FACTOR, 0);
        text.setText(rate);
        return text;
    }

    public TextControl getBitRate(View player, String rate) {
        TextControl text = new TextControl(player, mBitsRateLocation[0] * SCALE_FACTOR, mBitsRateLocation[1]
                * SCALE_FACTOR, mFontWidth * rate.length() * SCALE_FACTOR, 6 * SCALE_FACTOR, mFontIndex, mText,
                mFontWidth * SCALE_FACTOR, mFontHeight * SCALE_FACTOR, 0);
        text.setText(rate);
        return text;
    }

    public View getStopIcon(View player) {
        View icon = new View(player, mIconsLocation[2] * SCALE_FACTOR, mIconsLocation[3] * SCALE_FACTOR, mIconsImage[2]
                .getWidth(null), mIconsImage[2].getHeight(null));
        icon.setResource(icon.createImage(mIconsImage[2]));
        return icon;
    }

    public View getPlayIcon(View player) {
        View icon = new View(player, mIconsLocation[2] * SCALE_FACTOR, mIconsLocation[3] * SCALE_FACTOR, mIconsImage[0]
                .getWidth(null), mIconsImage[0].getHeight(null));
        icon.setResource(icon.createImage(mIconsImage[0]));
        return icon;
    }

    public View getPauseIcon(View player) {
        View icon = new View(player, mIconsLocation[2] * SCALE_FACTOR, mIconsLocation[3] * SCALE_FACTOR, mIconsImage[1]
                .getWidth(null), mIconsImage[1].getHeight(null));
        icon.setResource(icon.createImage(mIconsImage[1]));
        return icon;
    }

    public View getShuffleActive(View player) {
        View icon = new View(player, mPanelEPSRLocation[0] * SCALE_FACTOR, mPanelEPSRLocation[1] * SCALE_FACTOR,
                mPressedEPSRImage[0].getWidth(null), mPressedEPSRImage[0].getHeight(null));
        icon.setResource(icon.createImage(mPressedEPSRImage[0]));
        return icon;
    }

    public View getShufflePassive(View player) {
        View icon = new View(player, mPanelEPSRLocation[0] * SCALE_FACTOR, mPanelEPSRLocation[1] * SCALE_FACTOR,
                mReleasedEPSRImage[0].getWidth(null), mReleasedEPSRImage[0].getHeight(null));
        icon.setResource(icon.createImage(mReleasedEPSRImage[0]));
        return icon;
    }

    public View getRepeatActive(View player) {
        View icon = new View(player, mPanelEPSRLocation[2] * SCALE_FACTOR, mPanelEPSRLocation[3] * SCALE_FACTOR,
                mPressedEPSRImage[1].getWidth(null), mPressedEPSRImage[1].getHeight(null));
        icon.setResource(icon.createImage(mPressedEPSRImage[1]));
        return icon;
    }

    public View getRepeatPassive(View player) {
        View icon = new View(player, mPanelEPSRLocation[2] * SCALE_FACTOR, mPanelEPSRLocation[3] * SCALE_FACTOR,
                mReleasedEPSRImage[1].getWidth(null), mReleasedEPSRImage[1].getHeight(null));
        icon.setResource(icon.createImage(mReleasedEPSRImage[1]));
        return icon;
    }

    public PositionControl getPosition(View player, int pos) {
        int midpoint = (mPositionBarLocation[1] + (mPositionPanel[3] / 2)) * SCALE_FACTOR;
        int position = midpoint - (int) Math.round(mPositionBar[0].getHeight(null) / 2.0);
        PositionControl positionControl = new PositionControl(player, mPositionBarLocation[0] * SCALE_FACTOR, position,
                mPositionBar[0].getWidth(null), mPositionBar[0].getHeight(null), mPositionBar[0],
                mPressedPositionImage[0], mReleasedPositionImage[0]);
        positionControl.setPosition(pos);
        return positionControl;
    }

    public ImageView getSeconds1(View player) {
        ImageView view = new ImageView(player, mSecondLocation[0] * SCALE_FACTOR, mSecondLocation[1] * SCALE_FACTOR,
                mTimeImage);
        view.setImage(0);
        return view;
    }

    public ImageView getSeconds2(View player) {
        ImageView view = new ImageView(player, mSecond0Location[0] * SCALE_FACTOR, mSecond0Location[1] * SCALE_FACTOR,
                mTimeImage);
        view.setImage(0);
        return view;
    }

    public ImageView getMinutes1(View player) {
        ImageView view = new ImageView(player, mMinuteLocation[0] * SCALE_FACTOR, mMinuteLocation[1] * SCALE_FACTOR,
                mTimeImage);
        view.setImage(0);
        return view;
    }

    public ImageView getMinutes2(View player) {
        ImageView view = new ImageView(player, mMinute0Location[0] * SCALE_FACTOR, mMinute0Location[1] * SCALE_FACTOR,
                mTimeImage);
        view.setImage(0);
        return view;
    }

    private void parsePanel(int[] panel, Image panelImage, Image[] images) {
        int x, y, width, height;
        int j = 0;
        for (int i = 0; i < images.length; i++) {
            images[i] = Tools.createBufferedImage(panel[j + 2] * SCALE_FACTOR, panel[j + 3] * SCALE_FACTOR,
                    BufferedImage.TYPE_INT_RGB);
            x = panel[j] * SCALE_FACTOR;
            y = panel[j + 1] * SCALE_FACTOR;
            width = panel[j] * SCALE_FACTOR + panel[j + 2] * SCALE_FACTOR;
            height = panel[j + 1] * SCALE_FACTOR + panel[j + 3] * SCALE_FACTOR;
            if (width > panelImage.getWidth(null)) {
                (images[i].getGraphics()).drawImage(panelImage, 0, 0, panel[0 + 2] * SCALE_FACTOR, panel[0 + 3]
                        * SCALE_FACTOR, panel[0] * SCALE_FACTOR, panel[0 + 1] * SCALE_FACTOR, panel[0] * SCALE_FACTOR
                        + panel[0 + 2] * SCALE_FACTOR, panel[0 + 1] * SCALE_FACTOR + panel[0 + 3] * SCALE_FACTOR, null);
            } else
                (images[i].getGraphics()).drawImage(panelImage, 0, 0, panel[j + 2] * SCALE_FACTOR, panel[j + 3]
                        * SCALE_FACTOR, x, y, width, height, null);
            j = j + 4;
        }
    }

    private SkinLoader mSkinLoader;

    private Image mMain;

    private Image mText;

    private Image mNumbers;

    private Image[] mButtonReleasedImages;

    private Image[] mButtonPressedImages;

    private Image[] mPositionBar;

    private Image[] mReleasedPositionImage;

    private Image[] mPressedPositionImage;

    private Image[] mActiveModeImage;

    private Image[] mPassiveModeImage;

    private Image[] mTimeImage = { null, null, null, null, null, null, null, null, null, null, null };

    private Image[] mIconsImage;

    private Image[] mReleasedEPSRImage;

    private Image[] mPressedEPSRImage;
}