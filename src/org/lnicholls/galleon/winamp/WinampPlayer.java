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

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.media.MediaManager;
import org.lnicholls.galleon.media.Mp3File;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.FileSystemContainer.Item;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.DefaultPlayer;
import org.lnicholls.galleon.widget.DefaultApplication.Player;
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.View;

public class WinampPlayer extends DefaultPlayer {

    private static Logger log = Logger.getLogger(WinampPlayer.class.getName());

    public WinampPlayer(DefaultScreen parent, int x, int y, int width, int height, boolean visible,
            DefaultApplication application, Tracker tracker) {
        super(parent, x, y, width, height, visible);

        mTracker = tracker;
        mApplication = application;
        //mApplication.setTracker(mTracker);

        //mApplication.getPlayer().startTrack();
    }

    public void updatePlayer() {
        MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();
        String skin = null;
        if (musicPlayerConfiguration == null || musicPlayerConfiguration.getSkin() == null) {
            List skins = Server.getServer().getWinampSkins();
            skin = ((File) skins.get(0)).getAbsolutePath();
        } else
            skin = musicPlayerConfiguration.getSkin();
        ClassicSkin classicSkin = new ClassicSkin(skin);
        if (classicSkin != null) {
            //mBusy.setVisible(true);
            if (player == null) {
                player = classicSkin.getMain(WinampPlayer.this);
                previousControl = classicSkin.getPreviousControl(player);
                playControl = classicSkin.getPlayControl(player);
                pauseControl = classicSkin.getPauseControl(player);
                stopControl = classicSkin.getStopControl(player);
                nextControl = classicSkin.getNextControl(player);
                ejectControl = classicSkin.getEjectControl(player);
                title = classicSkin.getTitle(player);

                stereo = classicSkin.getStereoActive(player);
                mono = classicSkin.getMonoPassive(player);

                sampleRate = classicSkin.getSampleRate(player, "44");
                bitRate = classicSkin.getBitRate(player, " 96");

                stopIcon = classicSkin.getStopIcon(player);
                stopIcon.setVisible(false);
                playIcon = classicSkin.getPlayIcon(player);
                playIcon.setVisible(false);
                pauseIcon = classicSkin.getPauseIcon(player);
                pauseIcon.setVisible(false);

                repeat = classicSkin.getRepeatActive(player);
                shuffle = classicSkin.getShufflePassive(player);

                positionControl = classicSkin.getPosition(player, 0);

                seconds1 = classicSkin.getSeconds1(player);
                seconds2 = classicSkin.getSeconds2(player);
                minutes1 = classicSkin.getMinutes1(player);
                minutes2 = classicSkin.getMinutes2(player);
            }

            updateTitle();
            playPlayer();

            mPlaying = true;

            //mBusy.setVisible(false);
        }
    }
    
    private void updateTitle()
    {
        try {
            Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
            Audio audio = null;
            if (nameFile.isFile())
                audio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
            else
                audio = getAudio((String) nameFile.getValue());

            setTitleText(createTitle(audio));
        } catch (Exception ex) {
        }
    }

    private void updateTime(int seconds) {
        int secondD = 0, second = 0, minuteD = 0, minute = 0;
        int minutes = (int) Math.floor(seconds / 60);
        int hours = (int) Math.floor(minutes / 60);
        minutes = minutes - hours * 60;
        seconds = seconds - minutes * 60 - hours * 3600;
        if (seconds < 10) {
            secondD = 0;
            second = seconds;
        } else {
            secondD = ((int) seconds / 10);
            second = ((int) (seconds - (((int) seconds / 10)) * 10));
        }
        if (minutes < 10) {
            minuteD = 0;
            minute = minutes;
        } else {
            minuteD = ((int) minutes / 10);
            minute = ((int) (minutes - (((int) minutes / 10)) * 10));
        }

        setPainting(false);
        try {
            seconds1.setImage(second);
            seconds2.setImage(secondD);
            minutes1.setImage(minute);
            minutes2.setImage(minuteD);
        } finally {
            setPainting(true);
        }
    }

    private void setTitleText(String text) {
        text = Tools.extractName(text);

        if (!text.toUpperCase().equals(title.getText())) {
            setPainting(false);
            try {
                title.setText(text);
            } finally {
                setPainting(true);
            }
        }

        mLastTitle = text;
    }

    public void stopPlayer() {
        if (stopIcon != null) {
            setPainting(false);
            try {
                stopIcon.setVisible(true);
                playIcon.setVisible(false);
                pauseIcon.setVisible(false);
                positionControl.setPosition(0);
            } finally {
                setPainting(true);
            }
            try {
                Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
                Audio audio = null;
                if (nameFile.isFile())
                    audio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
                else
                    audio = getAudio((String) nameFile.getValue());

                if (mPlaying) {
                    setTitleText(createTitle(audio));
                } else
                    mLastTitle = createTitle(audio);
            } catch (Exception ex) {
            }
        }
    }

    public void playPlayer() {
        if (stopIcon != null) {
            setPainting(false);
            try {
                stopIcon.setVisible(false);
                playIcon.setVisible(true);
                pauseIcon.setVisible(false);
            } finally {
                setPainting(true);
            }
        }
    }

    public void pausePlayer() {
        if (stopIcon != null) {
            if (mApplication.getPlayer().getState() != Player.STOP) {
                setPainting(false);
                try {
                    stopIcon.setVisible(false);
                    playIcon.setVisible(false);
                    if (mApplication.getPlayer().getState() == Player.PAUSE) {
                        pauseIcon.setVisible(false);
                        playIcon.setVisible(true);
                    } else {
                        pauseIcon.setVisible(true);
                    }
                } finally {
                    setPainting(true);
                }
            }
        }
    }

    public void nextPlayer() {
        if (stopIcon != null) {
            setPainting(false);
            try {
                stopIcon.setVisible(false);
                playIcon.setVisible(true);
                pauseIcon.setVisible(false);
            } finally {
                setPainting(true);
            }
        }
    }

    public boolean handleKeyPress(int code, long rawcode) {
        if (getTransparency() != 0.0f)
            setTransparency(0.0f);
        switch (code) {
        case KEY_PAUSE:
            if (pauseControl != null)
                pauseControl.setSelected(true);
            pausePlayer();
            break;
        case KEY_PLAY:
            if (playControl != null)
                playControl.setSelected(true);
            playPlayer();
            break;
        case KEY_CHANNELUP:
            //getBApp().play("select.snd");
            //getBApp().flush();
            if (previousControl != null)
                previousControl.setSelected(true);
            break;
        case KEY_CHANNELDOWN:
            //getBApp().play("select.snd");
            //getBApp().flush();
            if (nextControl != null)
                nextControl.setSelected(true);
            break;
        case KEY_SLOW:
            if (stopControl != null)
                stopControl.setSelected(true);
            stopPlayer();
            break;
        case KEY_SELECT:
        case KEY_RIGHT:
            postEvent(new BEvent.Action(this, "pop"));
            return true;
        case KEY_LEFT:
            postEvent(new BEvent.Action(this, "pop"));
            return true;
        }
        return super.handleKeyPress(code, rawcode);
    }

    public boolean handleKeyRelease(int code, long rawcode) {
        switch (code) {
        case KEY_PAUSE:
            if (pauseControl != null)
                pauseControl.setSelected(false);
            break;
        case KEY_PLAY:
            if (playControl != null)
                playControl.setSelected(false);
            break;
        case KEY_CHANNELUP:
            if (previousControl != null)
                previousControl.setSelected(false);
            break;
        case KEY_CHANNELDOWN:
            if (nextControl != null)
                nextControl.setSelected(false);
            break;
        case KEY_SLOW:
            if (stopControl != null)
                stopControl.setSelected(false);
            break;
        case KEY_ENTER:
            if (ejectControl != null)
                ejectControl.setSelected(false);
            break;
        }
        return super.handleKeyRelease(code, rawcode);
    }

    public boolean handleAction(BView view, Object action) {
        Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
        if (action.equals("ready")) {
            if (mPlaying) {
                playPlayer();
            }
            String title = nameFile.getName();
            try {
                if (nameFile.isFile()) {
                    Audio audio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
                    title = createTitle(audio);
                } else
                    title = nameFile.getName();
            } catch (Exception ex) {
                Tools.logException(WinampPlayer.class, ex);
            }

            if (mPlaying) {
                setTitleText(title);
            } else
                mLastTitle = title;
        } else if (action.equals("playing") || action.equals("seeking")) {
            if (mPlaying) {
                if (mApplication.getPlayer().getTotal() != 0) {
                    int value = (int) Math.round(mApplication.getPlayer().getCurrentPosition()
                            / (float) mApplication.getPlayer().getTotal() * 100);
                    try {
                        Audio audio = null;
                        if (nameFile.isFile())
                            audio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
                        else
                            audio = getAudio((String) nameFile.getValue());
                        if (audio != null && audio.getDuration() != -1)
                            positionControl.setPosition(value);
                    } catch (Exception ex) {
                    }
                    updateTime(mApplication.getPlayer().getCurrentPosition() / 1000);
                }

                int value = mApplication.getPlayer().getBitrate();
                String newValue = Integer.toString(value);
                if (value < 100)
                    newValue = " " + newValue;
                bitRate.setText(newValue);
            }

            return true;
        } else if (action.equals("stopped")) {
            if (mPlaying) {
                stopPlayer();
                setTitleText(" ");
                updateTime(0);
            }
            return true;
        } else if (action.equals("update")) {
            if (mPlaying) {
                setTitleText(mApplication.getPlayer().getTitle());
            } else
                mLastTitle = mApplication.getPlayer().getTitle();
            return true;
        }

        return super.handleAction(view, action);
    }

    public boolean handleEvent(HmeEvent event) {
        switch (event.getOpCode()) {
        case EVT_KEY: {
            if (title != null && title.handleEvent(event))
                return true;
            break;
        }
        }
        return super.handleEvent(event);
    }

    private String createTitle(Audio audio) {
        String details = "";
        if (!audio.getTitle().equals(Mp3File.DEFAULT_ARTIST)) {
            details = audio.getTitle();
        }
        if (!audio.getArtist().equals(Mp3File.DEFAULT_ARTIST)) {
            if (details.length() == 0)
                details = audio.getArtist();
            else
                details = details + " - " + audio.getArtist();
        }
        if (!audio.getAlbum().equals(Mp3File.DEFAULT_ARTIST)) {
            if (details.length() == 0)
                details = audio.getAlbum();
            else
                details = details + " - " + audio.getAlbum();
        }
        if (audio.getDate() != 0) {
            if (details.length() == 0)
                details = String.valueOf(audio.getDate());
            else
                details = details + " - " + String.valueOf(audio.getDate());
        }
        if (details.length() > 90)
            details = Tools.trim(details, 90);
        return details;
    }

    private static Audio getAudio(String path) {
        Audio audio = null;
        try {
            List list = AudioManager.findByPath(path);
            if (list != null && list.size() > 0) {
                audio = (Audio) list.get(0);
            }
        } catch (Exception ex) {
            Tools.logException(WinampPlayer.class, ex);
        }

        if (audio == null) {
            try {
                audio = (Audio) MediaManager.getMedia(path);
                AudioManager.createAudio(audio);
            } catch (Exception ex) {
                Tools.logException(WinampPlayer.class, ex);
            }
        }
        return audio;
    }

    private DefaultApplication mApplication;

    private Audio mAudio;

    // when did the last key press occur
    private long lastKeyPress;

    private View player;

    private ImageControl previousControl;

    private ImageControl playControl;

    private ImageControl pauseControl;

    private ImageControl stopControl;

    private ImageControl nextControl;

    private ImageControl ejectControl;

    private ScrollTextControl title;

    private View stereo;

    private View mono;

    private TextControl bitRate;

    private TextControl sampleRate;

    private View stopIcon;

    private View playIcon;

    private View pauseIcon;

    private View repeat;

    private View shuffle;

    private PositionControl positionControl;

    private ImageView seconds1;

    private ImageView seconds2;

    private ImageView minutes1;

    private ImageView minutes2;

    private Tracker mTracker;

    private boolean mPlaying;

    private String mLastTitle = "";

}