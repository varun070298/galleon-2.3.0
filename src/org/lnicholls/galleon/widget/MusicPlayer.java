package org.lnicholls.galleon.widget;

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
import org.lnicholls.galleon.widget.DefaultApplication.Player;
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BView;

public class MusicPlayer extends DefaultPlayer {

	private static Logger log = Logger.getLogger(MusicPlayer.class.getName());

	public MusicPlayer(DefaultScreen parent, int x, int y, int width, int height, boolean visible,
			DefaultApplication application, Tracker tracker) {
		this(parent, x, y, width, height, visible, application, tracker, true);
	}

	public MusicPlayer(DefaultScreen parent, int x, int y, int width, int height, boolean visible,
			DefaultApplication application, Tracker tracker, boolean showWebImages) {
		super(parent, x, y, width, height, visible);

		mTracker = tracker;
		mApplication = application;

		MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();

		mMusicInfo = new MusicInfo(this, 0, 0, width, height, true, showWebImages
				&& musicPlayerConfiguration.isShowImages());

		mPlayBar = new PlayBar(this);
	}

	public void updatePlayer() {
		final Audio audio = currentAudio();
		if (audio != null) {
			mMusicInfo.setAudio(audio);
			if (!mPlaying)
				playPlayer();

			mPlaying = true;
		}
	}

	private Audio currentAudio() {
		if (mTracker != null) {
			try {
				Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
				if (nameFile != null) {
					if (nameFile.isFile())
						return getAudio(((File) nameFile.getValue()).getCanonicalPath());
					else
						return getAudio((String) nameFile.getValue());
				}
			} catch (Exception ex) {
				Tools.logException(MusicInfo.class, ex);
			}
		}
		return null;
	}

	private static Audio getAudio(String path) {
		Audio audio = null;
		try {
			List list = AudioManager.findByPath(path);
			if (list != null && list.size() > 0) {
				audio = (Audio) list.get(0);
			}
		} catch (Exception ex) {
			Tools.logException(MusicInfo.class, ex);
		}

		if (audio == null) {
			try {
				audio = (Audio) MediaManager.getMedia(path);
				AudioManager.createAudio(audio);
			} catch (Exception ex) {
				Tools.logException(MusicInfo.class, ex);
			}
		}
		return audio;
	}

	private void updateTitle() {
		try {
			Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
			Audio audio = null;
			if (nameFile.isFile())
				audio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
			else
				audio = getAudio((String) nameFile.getValue());

			if (audio.getTitle().equals(Mp3File.DEFAULT_ARTIST)) {
				audio.setTitle(nameFile.getName());
			}
			mMusicInfo.setAudio(audio);
		} catch (Exception ex) {
		}
	}

	private void updateTime(int seconds) {
		try {
			setPainting(false);
			// mPlayBar.setProgress(seconds);
			mPlayBar.setPosition(seconds);
		} finally {
			setPainting(true);
		}
		flush();
	}

	public void stopPlayer() {
		try {
			setPainting(false);
			mPlayBar.stop();
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

			/*
			 * if (mPlaying && audio != null) { mMusicInfo.setAudio(audio); }
			 */
		} catch (Exception ex) {
		}
	}

	public void playPlayer() {
		try {
			setPainting(false);
			mPlayBar.play();
		} finally {
			setPainting(true);
		}
	}

	public void rewindPlayer() {
		try {
			setPainting(false);
			mPlayBar.rewind();
		} finally {
			setPainting(true);
		}
	}

	public void pausePlayer() {
		if (mApplication.getPlayer().getState() != Player.STOP) {
			try {
				setPainting(false);
				if (mApplication.getPlayer().getState() == Player.PAUSE) {
					mPlayBar.play();
				} else {
					mPlayBar.pause();
				}
			} finally {
				setPainting(true);
			}
		}
	}

	public void nextPlayer() {
		try {
			setPainting(false);
			mPlayBar.play();
		} finally {
			setPainting(true);
		}
	}

	public boolean handleKeyPress(int code, long rawcode) {
		if (mMusicInfo.handleKeyPress(code, rawcode))
			return true;
		switch (code) {
		case KEY_PAUSE:
			pausePlayer();
			break;
		case KEY_PLAY:
		case KEY_FORWARD:
			playPlayer();
			break;
		case KEY_REVERSE:
			rewindPlayer();
			break;
		case KEY_CHANNELUP:
			break;
		case KEY_CHANNELDOWN:
			break;
		case KEY_SLOW:
			stopPlayer();
			break;
		case KEY_SELECT:
		case KEY_RIGHT:
			postEvent(new BEvent.Action(this, "pop"));
			remove();
			return true;
		case KEY_LEFT:
			postEvent(new BEvent.Action(this, "pop"));
			remove();
			return true;
		}
		return super.handleKeyPress(code, rawcode);
	}

	public boolean handleKeyRelease(int code, long rawcode) {
		switch (code) {
		case KEY_PAUSE:
			break;
		case KEY_PLAY:
		case KEY_FORWARD:
		case KEY_REVERSE:
			break;
		case KEY_CHANNELUP:
			break;
		case KEY_CHANNELDOWN:
			break;
		case KEY_SLOW:
			break;
		case KEY_ENTER:
			break;
		}
		return super.handleKeyRelease(code, rawcode);
	}

	public boolean handleAction(BView view, Object action) {
		Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
		if (action.equals("ready")) {
			/*
			 * if (mPlaying) { playPlayer(); }
			 */
			try {
				if (nameFile.isFile()) {
					mAudio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
				} else
					mAudio = getAudio((String) nameFile.getValue());
				if (mPlaying && mAudio != null) {
					mMusicInfo.setAudio(mAudio);
					mPlayBar.setDuration((int) mAudio.getDuration() / 1000);
				}
			} catch (Exception ex) {
				Tools.logException(MusicPlayer.class, ex);
			}
		} else if (action.equals("playing") || action.equals("seeking")) {
			if (mPlaying) {
				if (action.equals("playing"))
					playPlayer();
				if (mApplication.getPlayer().getTotal() != 0) {
					try {
						if (mAudio == null) {
							if (nameFile.isFile())
								mAudio = getAudio(((File) nameFile.getValue()).getCanonicalPath());
							else
								mAudio = getAudio((String) nameFile.getValue());
						}
						if (mAudio != null && mAudio.getDuration() != -1) {
							mPlayBar.setDuration((int) mAudio.getDuration() / 1000);
							updateTime(mApplication.getPlayer().getCurrentPosition() / 1000);
						}
					} catch (Exception ex) {
					}
				}
			}
			return true;
		} else if (action.equals("stopped")) {
			if (mPlaying) {
				stopPlayer();
				mMusicInfo.setTitle(" ");
				updateTime(0);
				mAudio = null;
			}
			return true;
		} else if (action.equals("update")) {
			if (mPlaying) {
				mMusicInfo.setTitle(mApplication.getPlayer().getTitle());
			} else
				mLastTitle = mApplication.getPlayer().getTitle();
			return true;
		}

		return super.handleAction(view, action);
	}

	public void remove() {
		mMusicInfo.flush();		mMusicInfo.clearResource();
		flush();		super.remove();
	}

	private DefaultApplication mApplication;

	private Audio mAudio;

	private boolean mPlaying;

	private String mLastTitle = "";

	private Tracker mTracker;

	private MusicInfo mMusicInfo;

	private PlayBar mPlayBar;
}