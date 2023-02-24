package org.lnicholls.galleon.apps.jukebox;

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

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.PersistentValue;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.media.MediaManager;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.Lyrics;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.Yahoo;
import org.lnicholls.galleon.util.FileSystemContainer.FileItem;
import org.lnicholls.galleon.util.FileSystemContainer.Item;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultMenuScreen;
import org.lnicholls.galleon.widget.DefaultOptionList;
import org.lnicholls.galleon.widget.DefaultPlayer;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.MusicInfo;
import org.lnicholls.galleon.widget.MusicOptionsScreen;
import org.lnicholls.galleon.widget.MusicPlayer;
import org.lnicholls.galleon.widget.ScreenSaver;
import org.lnicholls.galleon.widget.ScrollText;
import org.lnicholls.galleon.widget.DefaultList;
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;
import org.lnicholls.galleon.winamp.WinampPlayer;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.sdk.Resource;

public class Jukebox extends DefaultApplication {

	private static Logger log = Logger.getLogger(Jukebox.class.getName());

	public final static String TITLE = "Jukebox";

	private Resource mMenuBackground;

	private Resource mInfoBackground;

	private Resource mPlayerBackground;

	private Resource mLyricsBackground;

	private Resource mImagesBackground;

	private Resource mFolderIcon;

	private Resource mCDIcon;

	private Resource mPlaylistIcon;

	public void init(IContext context) throws Exception {
		super.init(context);

		mMenuBackground = getSkinImage("menu", "background");
		mInfoBackground = getSkinImage("info", "background");
		mPlayerBackground = getSkinImage("player", "background");
		mLyricsBackground = getSkinImage("lyrics", "background");
		mImagesBackground = getSkinImage("images", "background");
		mFolderIcon = getSkinImage("menu", "folder");
		mCDIcon = getSkinImage("menu", "item");
		mPlaylistIcon = getSkinImage("menu", "playlist");

		JukeboxConfiguration jukeboxConfiguration = (JukeboxConfiguration) ((JukeboxFactory) getFactory())
				.getAppContext().getConfiguration();

		MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getServerConfiguration()
				.getMusicPlayerConfiguration();

		PersistentValue persistentValue = PersistentValueManager.loadPersistentValue(DefaultApplication.TRACKER);
		if (persistentValue != null) {

			List files = new ArrayList();
			StringTokenizer tokenizer = new StringTokenizer(persistentValue.getValue(), DefaultApplication.SEPARATOR);
			while (tokenizer.hasMoreTokens()) {
				String id = tokenizer.nextToken();
				Audio audio = AudioManager.retrieveAudio(new Integer(id));
				if (audio != null) {
					files.add(new FileItem(audio.getTitle(), new File(audio.getPath())));
				}
			}
			mTracker = new Tracker(files, 0);
			// TODO Support random play
			//mTracker.setRandom(musicPlayerConfiguration.isRandomPlayFolders());
		}
		else
			mTracker = new Tracker(new ArrayList(), 0);

		push(new JukeboxMenuScreen(this), TRANSITION_NONE);

		initialize();
	}

	public class JukeboxMenuScreen extends DefaultMenuScreen {
		public JukeboxMenuScreen(Jukebox app) {
			super(app, "Jukebox");

			setFooter("Press ENTER for options, 1 for delete, 9 for delete all");

			getBelow().setResource(mMenuBackground);

			/*
			BView warning = new BView(getAbove(), BORDER_LEFT+BODY_WIDTH/2, TOP, BODY_WIDTH/2, getHeight()/2);
			warning.setResource(Color.RED);
			BText text = new BText(warning, 10, 10, warning.getWidth(), warning.getHeight());
			text.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
			text.setFont("default-30-bold.font");
			text.setColor(Color.RED);
			text.setShadow(true);
			text.setValue("Are you sure you want to delete all of the Jukebox tracks ?");

			BButton button = new BButton(warning, (warning.getWidth()/2)-50, (warning.getHeight() - SAFE_TITLE_V) - 40, 100, 30);
			button.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, null, null, null, null, true);
			button.setResource(createText("default-24.font", Color.white, "OK"));
			//button.setFocus();
	        setFocusDefault(button);

			//warning.setFocusDefault(list);
			 */

			JukeboxConfiguration jukeboxConfiguration = (JukeboxConfiguration) ((JukeboxFactory) getFactory())
					.getAppContext().getConfiguration();

			if (mTracker != null) {
				Iterator iterator = mTracker.getList().iterator();
				while (iterator.hasNext()) {
					Item nameFile = (Item) iterator.next();
					if (nameFile.isFile()) {
						try {
							File file = (File) nameFile.getValue();
							Audio audio = getAudio(file.getCanonicalPath());
							if (audio != null)
								mMenuList.add(nameFile);
						} catch (Exception ex) {
							Tools.logException(Jukebox.class, ex);
						}
					}
				}
			}
		}

		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();

					new Thread() {
						public void run() {
							try {
								MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getServerConfiguration()
								.getMusicPlayerConfiguration();
								mPlayerTracker = (Tracker) mTracker.clone();
								mPlayerTracker.setPos(mMenuList.getFocus());
								JukeboxScreen jukeboxScreen = new JukeboxScreen((Jukebox) getBApp());
								jukeboxScreen.setTracker(mPlayerTracker);
								getBApp().push(jukeboxScreen, TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(Jukebox.class, ex);
							}
						}
					}.start();
					return true;
				}
			} else if (action.equals("play")) {
				load();
				new Thread() {
					public void run() {
						try {
							MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getServerConfiguration()
							.getMusicPlayerConfiguration();
							mPlayerTracker = (Tracker) mTracker.clone();
							mPlayerTracker.setPos(mMenuList.getFocus());
							mPlayerTracker.setRandom(musicPlayerConfiguration.isRandomPlayFolders());
							if (musicPlayerConfiguration.isRandomPlayFolders())
								mPlayerTracker.setPos(mPlayerTracker.getNextPos());
							PlayerScreen playerScreen = new PlayerScreen((Jukebox) getBApp(), mPlayerTracker);
							getBApp().push(playerScreen, TRANSITION_LEFT);
							getBApp().flush();
						} catch (Exception ex) {
							Tools.logException(Jukebox.class, ex);
						}
					}
				}.start();
				return true;
			}
			return super.handleAction(view, action);
		}

		protected void createRow(BView parent, int index) {
			try {
				BView icon = new BView(parent, 9, 2, 32, 32);
				Item nameFile = (Item) mMenuList.get(index);
				File file = (File) nameFile.getValue();
				Audio audio = getAudio(file.getCanonicalPath());
				icon.setResource(mCDIcon);

				BText name = new BText(parent, 50, 4, parent.getWidth() - 40, parent.getHeight() - 4);
				name.setShadow(true);
				name.setFlags(RSRC_HALIGN_LEFT);
				String title = Tools.clean(audio.getTitle()) + " (" + Tools.clean(audio.getArtist()) + ")";
				name.setValue(Tools.trim(title, 40));
			} catch (Exception ex) {
				Tools.logException(Jukebox.class, ex);
			}
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_NUM1:
			case KEY_THUMBSDOWN:
				getBApp().play("select.snd");
				getBApp().flush();

				DefaultApplication application = (DefaultApplication)getApp();
				if (!application.isDemoMode())
				{
					if (mMenuList.size()>0)
					{
						int focus = mMenuList.getFocus();
						mTracker.getList().remove(mMenuList.getFocus());
						mMenuList.remove(mMenuList.getFocus());
						mMenuList.setFocus(focus, false);

						String trackerString = getTrackerString(mTracker);
						PersistentValueManager.savePersistentValue(TRACKER, trackerString);
					}
				}
				return true;
			case KEY_NUM9:
				ConfirmationScreen confirmationScreen = new ConfirmationScreen((Jukebox) getBApp(), mTracker);
				getBApp().push(confirmationScreen, TRANSITION_LEFT);
				getBApp().flush();
				return true;
			case KEY_PLAY:
				postEvent(new BEvent.Action(this, "play"));
				return true;
			case KEY_ENTER:
				getBApp().push(new MusicOptionsScreen((Jukebox) getBApp(), mInfoBackground), TRANSITION_LEFT);
				return true;
			case KEY_REPLAY:
				if (((Jukebox) getBApp()).getTracker() != null) {
					new Thread() {
						public void run() {
							getBApp().push(new PlayerScreen((Jukebox) getBApp(), ((Jukebox) getBApp()).getTracker()),
									TRANSITION_LEFT);
							getBApp().flush();
						}
					}.start();
				}
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			if (mTracker != null)
			{
				if (mTracker.getList().size()==0)
				{
					boolean result = super.handleEnter(arg, isReturn);
					mMenuList.clear();
					mMenuList.setVisible(false);
					setFocusDefault(getAbove());
					getBApp().flush();
					return result;
				}
				else
				{
					mFocus = mTracker.getPos();
					mPlayerTracker = (Tracker)mTracker.clone();
					return super.handleEnter(arg, isReturn);
				}
			}
			return super.handleEnter(arg, isReturn);
		}

		private Tracker mPlayerTracker;
	}

	public class JukeboxScreen extends DefaultScreen {

		private BList list;

		public JukeboxScreen(Jukebox app) {
			super(app, "Song", true);

			setFooter("Press ENTER for options");

			getBelow().setResource(mInfoBackground);

			mMusicInfo = new MusicInfo(this.getNormal(), BORDER_LEFT, TOP, BODY_WIDTH, BODY_HEIGHT, true);

			list = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H + 10, (getHeight() - SAFE_TITLE_V) - 80,
					(int) Math.round((getWidth() - (SAFE_TITLE_H * 2)) / 2.5), 90, 35);
			list.add("Play");
			list.add("Don't do anything");

			setFocusDefault(list);
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			getBelow().setResource(mInfoBackground);
			updateView();

			return super.handleEnter(arg, isReturn);
		}

		private void updateView() {
			Audio audio = currentAudio();
			if (audio != null) {
				Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
				mMusicInfo.setAudio(audio, nameFile.getName());
			}
		}

		public boolean handleExit() {
			mMusicInfo.flush();			mMusicInfo.clearResource();
			return super.handleExit();
		}

		public boolean handleKeyPress(int code, long rawcode) {
			if (mMusicInfo.handleKeyPress(code, rawcode))
				return true;
			Audio audio = currentAudio();
			switch (code) {
			case KEY_SELECT:
			case KEY_RIGHT:
			case KEY_PLAY:
				if (list.getFocus() == 0) {
					postEvent(new BEvent.Action(this, "play"));
					return true;
				} else {
					postEvent(new BEvent.Action(this, "pop"));
					return true;
				}
			case KEY_LEFT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			case KEY_CHANNELUP:
				getBApp().play("pageup.snd");
				getBApp().flush();
				getPrevPos();
				updateView();
				return true;
			case KEY_CHANNELDOWN:
				getBApp().play("pagedown.snd");
				getBApp().flush();
				getNextPos();
				updateView();
				return true;
			case KEY_ENTER:
				getBApp().push(new MusicOptionsScreen((Jukebox) getBApp(), mInfoBackground), TRANSITION_LEFT);
				return true;
			case KEY_REPLAY:
				if (((Jukebox) getBApp()).getTracker() != null) {
					new Thread() {
						public void run() {
							getBApp().push(new PlayerScreen((Jukebox) getBApp(), ((Jukebox) getBApp()).getTracker()),
									TRANSITION_LEFT);
							getBApp().flush();
						}
					}.start();
				}
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		public void getNextPos() {
			if (mTracker != null) {
				int pos = mTracker.getNextPos();
				Item nameFile = (Item) mTracker.getList().get(pos);
				while (nameFile.isFolder() || nameFile.isPlaylist()) {
					pos = mTracker.getNextPos();
					nameFile = (Item) mTracker.getList().get(pos);
				}
			}
		}

		public void getPrevPos() {
			if (mTracker != null) {
				int pos = mTracker.getPrevPos();
				Item nameFile = (Item) mTracker.getList().get(pos);
				while (nameFile.isFolder() || nameFile.isPlaylist()) {
					pos = mTracker.getPrevPos();
					nameFile = (Item) mTracker.getList().get(pos);
				}
			}
		}

		public boolean handleAction(BView view, Object action) {
			if (action.equals("play") || action.equals("push")) {

				getBApp().play("select.snd");
				getBApp().flush();

				new Thread() {
					public void run() {
						getBApp().push(new PlayerScreen((Jukebox) getBApp(), mTracker), TRANSITION_LEFT);
						getBApp().flush();
					}
				}.start();
				return true;
			}

			return super.handleAction(view, action);
		}

		public void setTracker(Tracker value) {
			mTracker = value;
		}

		private Audio currentAudio() {
			if (mTracker != null) {
				try {
					Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
					if (nameFile != null && nameFile.getValue() != null) {
						if (nameFile.isFile())
							return getAudio(((File) nameFile.getValue()).getCanonicalPath());
						else
							return getAudio((String) nameFile.getValue());
					}
				} catch (Exception ex) {
					Tools.logException(Jukebox.class, ex);
				}
			}
			return null;
		}

		private MusicInfo mMusicInfo;

		private Tracker mTracker;
	}

	public class PlayerScreen extends DefaultScreen {

		public PlayerScreen(Jukebox app, Tracker tracker) {
			super(app, true);

			getBelow().setResource(mPlayerBackground);

			boolean sameTrack = false;
			DefaultApplication defaultApplication = (DefaultApplication) getApp();
			Audio currentAudio = defaultApplication.getCurrentAudio();
			Tracker currentTracker = defaultApplication.getTracker();
			if (currentTracker != null && currentAudio != null) {
				Item newItem = (Item) tracker.getList().get(tracker.getPos());
				if (currentAudio.getPath().equals(newItem.getValue().toString())) {
					mTracker = currentTracker;
					sameTrack = true;
				} else {
					mTracker = tracker;
					app.setTracker(mTracker);
				}
			} else {
				mTracker = tracker;
				app.setTracker(mTracker);
			}

			setTitle(" ");

			setFooter("Press INFO for lyrics, REPLAY to return to this screen");

			if (!sameTrack || getPlayer().getState() == Player.STOP)
				getPlayer().startTrack();
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			new Thread() {
				public void run() {
					mBusy.setVisible(true);
					mBusy.flush();

					synchronized (this) {
						try {
							setPainting(false);
							MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
									.getMusicPlayerConfiguration();
							if (musicPlayerConfiguration.getPlayer().equals(MusicPlayerConfiguration.CLASSIC))
								player = new MusicPlayer(PlayerScreen.this, BORDER_LEFT, SAFE_TITLE_H, BODY_WIDTH,
										BODY_HEIGHT, false, (DefaultApplication) getApp(), mTracker);
							else
								player = new WinampPlayer(PlayerScreen.this, 0, 0, PlayerScreen.this.getWidth(),
										PlayerScreen.this.getHeight(), false, (DefaultApplication) getApp(), mTracker);
							player.updatePlayer();
							player.setVisible(true);
						} finally {
							setPainting(true);
						}
					}
					setFocusDefault(player);
					setFocus(player);
					mBusy.setVisible(false);

					MusicPlayerConfiguration jukeboxPlayerConfiguration = Server.getServer()
							.getMusicPlayerConfiguration();
					if (jukeboxPlayerConfiguration.isScreensaver()) {
						mScreenSaver = new ScreenSaver(PlayerScreen.this);
						mScreenSaver.start();
					}
					getBApp().flush();
				}

				public void interrupt() {
					synchronized (this) {
						super.interrupt();
					}
				}
			}.start();

			return super.handleEnter(arg, isReturn);
		}

		public boolean handleExit() {
			try {
				setPainting(false);
				if (mScreenSaver != null && mScreenSaver.isAlive()) {
					mScreenSaver.interrupt();
					mScreenSaver = null;
				}
				if (player != null) {
					player.stopPlayer();
					player.setVisible(false);
					player.flush();					player.remove();
					player = null;
				}
			} finally {
				setPainting(true);
			}
			return super.handleExit();
		}

		public boolean handleKeyPress(int code, long rawcode) {
			if (mScreenSaver != null)
				mScreenSaver.handleKeyPress(code, rawcode);
			switch (code) {
			case KEY_INFO:
			case KEY_NUM0:
				getBApp().play("select.snd");
				getBApp().flush();
				LyricsScreen lyricsScreen = new LyricsScreen((Jukebox) getBApp(), mTracker);
				getBApp().push(lyricsScreen, TRANSITION_LEFT);
				getBApp().flush();
				return true;
			/*
			 * case KEY_NUM0: JukeboxConfiguration jukeboxConfiguration =
			 * (JukeboxConfiguration) ((JukeboxFactory)
			 * getContext().getFactory()) .getAppContext().getConfiguration();
			 * MusicPlayerConfiguration jukeboxPlayerConfiguration =
			 * Server.getServer().getMusicPlayerConfiguration(); if
			 * (jukeboxPlayerConfiguration.isShowImages()) {
			 * getBApp().play("select.snd"); getBApp().flush(); ImagesScreen
			 * imagesScreen = new ImagesScreen((Jukebox) getBApp(), mTracker);
			 * getBApp().push(imagesScreen, TRANSITION_LEFT); getBApp().flush();
			 * return true; } else return false;
			 */
			}

			return super.handleKeyPress(code, rawcode);
		}

		// private WinampPlayer player;

		private DefaultPlayer player;

		private Tracker mTracker;

		private ScreenSaver mScreenSaver;
	}

	public class LyricsScreen extends DefaultScreen {
		private BList list;

		public LyricsScreen(Jukebox app, Tracker tracker) {
			super(app, "Lyrics", false);

			getBelow().setResource(mLyricsBackground);

			mTracker = tracker;

			scrollText = new ScrollText(getNormal(), BORDER_LEFT, TOP, BODY_WIDTH - 10, getHeight() - SAFE_TITLE_V
					- TOP - 70, "");
			scrollText.setVisible(false);

			// setFocusDefault(scrollText);

			// setFooter("lyrc.com.ar");
			setFooter("lyrictracker.com");

			mBusy.setVisible(true);

			/*
			 * list = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H + 10,
			 * (getHeight() - SAFE_TITLE_V) - 60, (int) Math .round((getWidth() -
			 * (SAFE_TITLE_H * 2)) / 2), 90, 35);
			 * //list.setBarAndArrows(BAR_HANG, BAR_DEFAULT, H_LEFT, null);
			 * list.add("Back to player"); setFocusDefault(list);
			 */

			BButton button = new BButton(getNormal(), SAFE_TITLE_H + 10, (getHeight() - SAFE_TITLE_V) - 40, (int) Math
					.round((getWidth() - (SAFE_TITLE_H * 2)) / 2), 35);
			button.setResource(createText("default-24.font", Color.white, "Return to player"));
			button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null, null, true);
			setFocus(button);
		}

		public void updateLyrics() {
			try {
				setPainting(false);
				if (mLyricsThread != null && mLyricsThread.isAlive())
					mLyricsThread.interrupt();
			} finally {
				setPainting(true);
			}
			Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
			Audio audio = null;
			try {
				List list = null;
				if (nameFile.isFile())
					list = AudioManager.findByPath(((File) nameFile.getValue()).getCanonicalPath());
				else
					list = AudioManager.findByPath((String) nameFile.getValue());
				if (list != null && list.size() > 0) {
					audio = (Audio) list.get(0);
				}
			} catch (Exception ex) {
				Tools.logException(Jukebox.class, ex);
			}
			if (audio.getLyrics() != null && audio.getLyrics().length() > 0) {
				try {
					setPainting(false);
					mBusy.setVisible(false);
					getBApp().flush();
					scrollText.setVisible(true);
					scrollText.setText(audio.getLyrics());
					getBApp().flush();
				} finally {
					setPainting(true);
				}
			} else {
				final Audio lyricsAudio = audio;

				mLyricsThread = new Thread() {
					public void run() {
						try {
							String lyrics = Lyrics.getLyrics(lyricsAudio.getTitle(), lyricsAudio.getArtist());
							if (lyrics == null || lyrics.trim().length() == 0) {
								lyrics = "Lyrics not found";
							} else {
								synchronized (this) {
									try {
										lyricsAudio.setLyrics(lyrics);
										AudioManager.updateAudio(lyricsAudio);
									} catch (Exception ex) {
										Tools.logException(Jukebox.class, ex, "Could not update lyrics");
									}
								}
							}
							synchronized (this) {
								try {
									setPainting(false);
									mBusy.setVisible(false);
									getBApp().flush();
									scrollText.setVisible(true);
									scrollText.setText(lyrics);
									getBApp().flush();
								} finally {
									setPainting(true);
								}
							}
						} catch (Exception ex) {
							Tools.logException(Jukebox.class, ex, "Could retrieve lyrics");
						}
					}

					public void interrupt() {
						synchronized (this) {
							super.interrupt();
						}
					}
				};
				mLyricsThread.start();
			}
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			updateLyrics();

			return super.handleEnter(arg, isReturn);
		}

		public boolean handleExit() {
			try {
				setPainting(false);
				if (mLyricsThread != null && mLyricsThread.isAlive()) {
					mLyricsThread.interrupt();
					mLyricsThread = null;
				}
			} finally {
				setPainting(true);
			}
			return super.handleExit();
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			case KEY_UP:
			case KEY_DOWN:
			case KEY_CHANNELUP:
			case KEY_CHANNELDOWN:
				scrollText.handleKeyPress(code, rawcode);
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		private ScrollText scrollText;

		private Thread mLyricsThread;

		private Tracker mTracker;
	}

	public class ImagesScreen extends DefaultScreen {
		private BList list;

		public ImagesScreen(Jukebox app, Tracker tracker) {
			super(app, "Images", true);

			getBelow().setResource(mImagesBackground);

			mTracker = tracker;

			mImageView = new BView(this.getNormal(), BORDER_LEFT, TOP, BODY_WIDTH, getHeight() - SAFE_TITLE_V - TOP
					- 75);
			mImageView.setVisible(false);

			mPosText = new BText(getNormal(), BORDER_LEFT, getHeight() - SAFE_TITLE_V - 60, BODY_WIDTH, 30);
			mPosText.setFlags(RSRC_HALIGN_RIGHT | RSRC_VALIGN_TOP);
			mPosText.setFont("default-18-bold.font");
			mPosText.setColor(Color.CYAN);
			mPosText.setShadow(true);

			mUrlText = new BText(getNormal(), SAFE_TITLE_H, getHeight() - SAFE_TITLE_V - 78, BODY_WIDTH, 15);
			mUrlText.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_BOTTOM);
			mUrlText.setFont("default-12-bold.font");
			mUrlText.setColor(Color.WHITE);
			mUrlText.setShadow(true);

			setFooter("search.yahoo.com");

			mBusy.setVisible(true);

			/*
			 * list = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H + 10,
			 * (getHeight() - SAFE_TITLE_V) - 60, (int) Math .round((getWidth() -
			 * (SAFE_TITLE_H * 2)) / 2), 90, 35);
			 * //list.setBarAndArrows(BAR_HANG, BAR_DEFAULT, H_LEFT, null);
			 * list.add("Back to player"); setFocusDefault(list);
			 */

			BButton button = new BButton(getNormal(), SAFE_TITLE_H + 10, (getHeight() - SAFE_TITLE_V) - 55, (int) Math
					.round((getWidth() - (SAFE_TITLE_H * 2)) / 2), 35);
			button.setResource(createText("default-24.font", Color.white, "Return to player"));
			button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null, null, true);
			setFocus(button);
		}

		public void updateImage() {
			Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
			Audio audio = null;
			try {
				List list = null;
				if (nameFile.isFile())
					list = AudioManager.findByPath(((File) nameFile.getValue()).getCanonicalPath());
				else
					list = AudioManager.findByPath((String) nameFile.getValue());
				if (list != null && list.size() > 0) {
					audio = (Audio) list.get(0);
				}
			} catch (Exception ex) {
				Tools.logException(Jukebox.class, ex);
			}
			final Audio lyricsAudio = audio;

			mImageThread = new Thread() {
				public void run() {
					try {
						synchronized (this) {
							mBusy.setVisible(true);
							getBApp().flush();
						}

						if (mResults == null || mResults.size() == 0) {
							mResults = Yahoo.getImages("\"" + lyricsAudio.getArtist() + "\" jukebox");
							mPos = 0;
						}
						if (mResults.size() == 0) {
							synchronized (this) {
								try {
									setPainting(false);
									mBusy.setVisible(false);
									getBApp().flush();
								} finally {
									setPainting(true);
								}
							}
							return;
						}

						NameValue nameValue = (NameValue) mResults.get(mPos);
						Image image = Tools.getImage(new URL(nameValue.getValue()), -1, -1);

						if (image != null) {
							synchronized (this) {
								try {
									setPainting(false);
									if (mImageView.getResource() != null)									{
										mImageView.getResource().flush();										mImageView.getResource().remove();									}
									mUrlText.setValue(nameValue.getName());
									mImageView.setVisible(true);
									mImageView.setTransparency(1f);
									mImageView.setResource(createImage(image), RSRC_IMAGE_BESTFIT);
									mImageView.setTransparency(0f, getResource("*500"));
									image.flush();
									image = null;
								} finally {
									setPainting(true);
								}
							}
						} else {
							mResults.remove(mPos);
						}

					} catch (Exception ex) {
						Tools.logException(Jukebox.class, ex, "Could not retrieve image");
						mResults.remove(mPos);
					} finally {
						synchronized (this) {
							try {
								setPainting(false);
								if (mResults != null && mResults.size() > 0)
									mPosText.setValue(String.valueOf(mPos + 1) + " of "
											+ String.valueOf(mResults.size()));
								else
									mPosText.setValue("No images found");
								mBusy.setVisible(false);
							} finally {
								setPainting(true);
							}
							getBApp().flush();
						}
					}
				}

				public void interrupt() {
					synchronized (this) {
						super.interrupt();
					}
				}
			};
			mImageThread.start();
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			updateImage();

			return super.handleEnter(arg, isReturn);
		}

		public boolean handleExit() {
			try {
				setPainting(false);
				if (mImageThread != null && mImageThread.isAlive()) {
					mImageThread.interrupt();
					mImageThread = null;
					mResults.clear();
					mResults = null;
				}
			} finally {
				setPainting(true);
			}
			return super.handleExit();
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			case KEY_UP:
			case KEY_DOWN:
			case KEY_CHANNELUP:
				if (mResults != null && mResults.size() > 0) {
					getBApp().play("pageup.snd");
					getBApp().flush();
					mPos = mPos - 1;
					if (mPos == -1)
						mPos = mResults.size() - 1;
				}
				updateImage();
				return true;
			case KEY_CHANNELDOWN:
				if (mResults != null && mResults.size() > 0) {
					getBApp().play("pagedown.snd");
					getBApp().flush();
					mPos = mPos + 1;
					if (mPos == mResults.size())
						mPos = 0;
				}
				updateImage();
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		private BView mImageView;

		private Thread mImageThread;

		private Tracker mTracker;

		private List mResults;

		private int mPos;

		private BText mPosText;

		private BText mUrlText;
	}

	public class ConfirmationScreen extends DefaultScreen {
		private BList list;

		public ConfirmationScreen(Jukebox app, Tracker tracker) {
			super(app, "Confirmation", true);

			mTracker = tracker;

			getBelow().setResource(mImagesBackground);

			mPosText = new BText(getNormal(), BORDER_LEFT, TOP, BODY_WIDTH, 100);
			mPosText.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
			mPosText.setFont("default-30-bold.font");
			mPosText.setColor(Color.RED);
			mPosText.setShadow(true);
			mPosText.setValue("Are you sure you want to delete all of the Jukebox tracks ?");

			list = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H, (getHeight() - SAFE_TITLE_V) - 80,
					(getWidth() - (SAFE_TITLE_H * 2)) / 2, 90, 35);
			list.add("Delete all tracks");
			list.add("Don't do anything");
			list.setFocus(1, false);

			setFocusDefault(list);
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_RIGHT:
			case KEY_LEFT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			case KEY_SELECT:
				DefaultApplication application = (DefaultApplication)getApp();
				if (!application.isDemoMode())
				{
					if (list.getFocus() == 0) {
						getBApp().play("select.snd");
						getBApp().flush();

						mTracker.getList().clear();
						PersistentValueManager.savePersistentValue(TRACKER, "");
					}
				}
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			return super.handleEnter(arg, isReturn);
		}

		private BText mPosText;
	}

	private static Audio getAudio(String path) {
		Audio audio = null;
		try {
			List list = AudioManager.findByPath(path);
			if (list != null && list.size() > 0) {
				audio = (Audio) list.get(0);
			}
		} catch (Exception ex) {
			Tools.logException(Jukebox.class, ex);
		}

		if (audio == null) {
			try {
				audio = (Audio) MediaManager.getMedia(path);
				AudioManager.createAudio(audio);
			} catch (Exception ex) {
				Tools.logException(Jukebox.class, ex);
			}
		}
		return audio;
	}

	public static class JukeboxFactory extends AppFactory {

		public void initialize() {
			JukeboxConfiguration jukeboxConfiguration = (JukeboxConfiguration) getAppContext().getConfiguration();
		}
	}

	private Tracker mTracker;
}