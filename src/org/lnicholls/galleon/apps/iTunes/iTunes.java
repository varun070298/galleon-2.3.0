package org.lnicholls.galleon.apps.iTunes;

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
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.HibernateUtil;
import org.lnicholls.galleon.database.PersistentValue;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.database.Playlists;
import org.lnicholls.galleon.database.PlaylistsManager;
import org.lnicholls.galleon.database.PlaylistsTracks;
import org.lnicholls.galleon.database.PlaylistsTracksManager;
import org.lnicholls.galleon.media.MediaManager;
import org.lnicholls.galleon.media.MediaRefreshThread;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.FileFilters;
import org.lnicholls.galleon.util.FileSystemContainer;
import org.lnicholls.galleon.util.Lyrics;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.ReloadCallback;
import org.lnicholls.galleon.util.ReloadTask;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.Yahoo;
import org.lnicholls.galleon.util.FileSystemContainer.FileItem;
import org.lnicholls.galleon.util.FileSystemContainer.FolderItem;
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
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;
import org.lnicholls.galleon.widget.DefaultApplication.VersionScreen;
import org.lnicholls.galleon.winamp.WinampPlayer;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.interfaces.IArgumentList;

public class iTunes extends DefaultApplication {

	private static Logger log = Logger.getLogger(iTunes.class.getName());

	public final static String TITLE = "iTunes";

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

		iTunesConfiguration musicConfiguration = (iTunesConfiguration) ((iTunesFactory) getFactory())
				.getAppContext().getConfiguration();

		List titles = null;
		try {
			titles = PlaylistsManager.listTitles();
		} catch (Exception ex) {
			Tools.logException(PlaylistParser.class, ex);
		}
		if (titles.size() == 1) {
			try {
				String title = (String) titles.get(0);

				List list = PlaylistsManager.findByTitle(title);
				if (list!=null && list.size()>0)
				{
					Playlists playlist = (Playlists) list.get(0);

					ArrayList tracks = new ArrayList();
					List playlists = PlaylistsTracksManager.findByPlaylists(playlist.getId());
					if (playlists!=null && playlists.size()>0)
					{
						Iterator iterator = playlists.iterator();
						while (iterator.hasNext()) {
							PlaylistsTracks track = (PlaylistsTracks) iterator.next();
							if (track.getTrack()!=null)
							{
								Audio audio = AudioManager.retrieveAudio(track.getTrack());
								tracks.add(new FileItem(audio.getTitle(), new File(audio.getPath())));
							}
						}
						playlists.clear();
					}
					Tracker tracker = new Tracker(tracks, 0);
					PathScreen pathScreen = new PathScreen(this, tracker, true);
					push(pathScreen, TRANSITION_LEFT);
					list.clear();
				}
			} catch (Exception ex) {
				Tools.logException(iTunes.class, ex);
			}
		} else
			push(new MusicMenuScreen(this), TRANSITION_NONE);

		initialize();
	}

	public class MusicMenuScreen extends DefaultMenuScreen {
		public MusicMenuScreen(iTunes app) {
			super(app, "Music");

			setFooter("Press ENTER for options");

			getBelow().setResource(mMenuBackground);

			iTunesConfiguration musicConfiguration = (iTunesConfiguration) ((iTunesFactory) getFactory())
					.getAppContext().getConfiguration();

			mCountText = new BText(getNormal(), BORDER_LEFT, TOP - 30, BODY_WIDTH, 20);
			mCountText.setFlags(IHmeProtocol.RSRC_HALIGN_CENTER);
			mCountText.setFont("default-18.font");
			mCountText.setColor(Color.GREEN);
			mCountText.setShadow(true);

			List titles = null;
			try {
				titles = PlaylistsManager.listTitles();
			} catch (Exception ex) {
				Tools.logException(PlaylistParser.class, ex);
			}

			Iterator iterator = titles.iterator();
			while (iterator.hasNext()) {
				String title = (String) iterator.next();
				mMenuList.add(new FolderItem(title, title));
			}
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			int count = 0;
			try {
				count = AudioManager.countMP3sByOrigen("iTunes");
			} catch (Exception ex) {
				Tools.logException(iTunes.class, ex);
			}

			String status = "Total tracks: " + String.valueOf(count);
			PersistentValue persistentValue = PersistentValueManager.loadPersistentValue(iTunes.class.getName()
					+ ".refresh");
			if (persistentValue != null) {
				String refreshed = persistentValue.getValue();
				if (refreshed != null) {
					try
					{
						SimpleDateFormat dateFormat = new SimpleDateFormat();
						dateFormat.applyPattern("M/d/yy hh:mm a");
						status = status + "   Refreshed: " + dateFormat.format(new Date(refreshed));
					}
					catch (Exception ex)
					{
						log.error("Invalid date: " + persistentValue.getValue(), ex);
					}
				}
			}

			mCountText.setValue(status);

			return super.handleEnter(arg, isReturn);
		}

		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();

					new Thread() {
						public void run() {
							try {
								FileItem nameFile = (FileItem) (mMenuList.get(mMenuList.getFocus()));
								List list = PlaylistsManager.findByTitle((String) nameFile.getValue());
								if (list!=null && list.size()>0)
								{
									Playlists playlist = (Playlists) list.get(0);

									ArrayList tracks = new ArrayList();
									List playlists = PlaylistsTracksManager.findByPlaylists(playlist.getId());
									if (playlists!=null && playlists.size()>0)
									{
										Iterator iterator = playlists.iterator();
										while (iterator.hasNext()) {
											PlaylistsTracks track = (PlaylistsTracks) iterator.next();
											if (track.getTrack()!=null)
											{
												Audio audio = AudioManager.retrieveAudio(track.getTrack());
												tracks.add(new FileItem(audio.getTitle(), new File(audio.getPath())));
											}
										}
										playlists.clear();
									}
									Tracker tracker = new Tracker(tracks, 0);
									PathScreen pathScreen = new PathScreen((iTunes) getBApp(), tracker);
									push(pathScreen, TRANSITION_LEFT);
									list.clear();
								}
							} catch (Exception ex) {
								Tools.logException(iTunes.class, ex);
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
							FileItem nameFile = (FileItem) (mMenuList.get(mMenuList.getFocus()));
							List playlists = PlaylistsManager.findByTitle((String) nameFile.getValue());
							if (playlists != null && playlists.size() > 0) {
								Playlists playlist = (Playlists) playlists.get(0);

								List pList = PlaylistsTracksManager.findByPlaylists(playlist.getId());
								ArrayList tracks = new ArrayList();
								Iterator iterator = pList.iterator();
								while (iterator.hasNext()) {
									PlaylistsTracks track = (PlaylistsTracks) iterator.next();
									if (track.getTrack()!=null)
									{
										Audio audio = AudioManager.retrieveAudio(track.getTrack());
										tracks.add(new FileItem(audio.getTitle(), new File(audio.getPath())));
									}
								}
								Tracker tracker = new Tracker(tracks, 0);
								MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
										.getMusicPlayerConfiguration();
								tracker.setRandom(musicPlayerConfiguration.isRandomPlayFolders());
								PathScreen pathScreen = new PathScreen((iTunes) getBApp(), tracker);
								getBApp().push(new PlayerScreen((iTunes) getBApp(), tracker), TRANSITION_LEFT);
								getBApp().flush();
							}
						} catch (Exception ex) {
							Tools.logException(iTunes.class, ex);
						}
					}
				}.start();
				return true;
			}
			return super.handleAction(view, action);
		}

		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 9, 2, 32, 32);
			Item nameFile = (Item) mMenuList.get(index);
			if (nameFile.isFolder()) {
				icon.setResource(mFolderIcon);
			} else {
				icon.setResource(mCDIcon);
			}

			BText name = new BText(parent, 50, 4, parent.getWidth() - 40, parent.getHeight() - 4);
			name.setShadow(true);
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(Tools.clean(nameFile.getName()), 40));
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_PLAY:
				postEvent(new BEvent.Action(this, "play"));
				return true;
			case KEY_ENTER:
				getBApp().push(new MusicOptionsScreen((iTunes) getBApp(), mInfoBackground), TRANSITION_LEFT);
				return true;
			case KEY_REPLAY:
				if (((iTunes) getBApp()).getTracker()!=null)
				{
					new Thread() {
						public void run() {
							getBApp().push(new PlayerScreen((iTunes) getBApp(), ((iTunes) getBApp()).getTracker()), TRANSITION_LEFT);
							getBApp().flush();
						}
					}.start();
				}
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		BText mCountText;
	}

	public class PathScreen extends DefaultMenuScreen {

		public PathScreen(iTunes app, Tracker tracker) {
			this(app, tracker, false);
		}

		public PathScreen(iTunes app, Tracker tracker, boolean first) {
			super(app, "Music");

			setFooter("Press ENTER for options");

			getBelow().setResource(mMenuBackground);

			mTracker = tracker;
			mFirst = first;

			Iterator iterator = mTracker.getList().iterator();
			while (iterator.hasNext()) {
				Item item = (Item) iterator.next();
				mMenuList.add(item);
			}
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			mFocus = mTracker.getPos();
			mTracker = (Tracker)mTracker.clone();
			return super.handleEnter(arg, isReturn);
		}

		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				load();
				final Item nameFile = (Item) (mMenuList.get(mMenuList.getFocus()));
				if (nameFile.isFolder()) {
					new Thread() {
						public void run() {
							try {
								mTracker.setPos(mMenuList.getFocus());
								File file = (File) nameFile.getValue();
								FileSystemContainer fileSystemContainer = new FileSystemContainer(file
										.getCanonicalPath());
								((DefaultApplication) getBApp()).setCurrentTrackerContext(file.getCanonicalPath());
								Tracker tracker = new Tracker(fileSystemContainer
										.getItems(FileFilters.audioDirectoryFilter), 0);
								PathScreen pathScreen = new PathScreen((iTunes) getBApp(), tracker);
								getBApp().push(pathScreen, TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(iTunes.class, ex);
							}
						}
					}.start();
				} else {
					new Thread() {
						public void run() {
							try {
								mTracker.setPos(mMenuList.getFocus());
								MusicScreen musicScreen = new MusicScreen((iTunes) getBApp());
								musicScreen.setTracker(mTracker);

								getBApp().push(musicScreen, TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(iTunes.class, ex);
							}
						}
					}.start();
				}

				return true;
			} else if (action.equals("play")) {
				load();
				final Item nameFile = (Item) (mMenuList.get(mMenuList.getFocus()));
				if (nameFile.isFolder()) {
					new Thread() {
						public void run() {
							try {
								mTracker.setPos(mMenuList.getFocus());
								File file = (File) nameFile.getValue();
								FileSystemContainer fileSystemContainer = new FileSystemContainer(file
										.getCanonicalPath(), true);
								((DefaultApplication) getBApp()).setCurrentTrackerContext(file.getCanonicalPath());
								Tracker tracker = new Tracker(fileSystemContainer
										.getItems(FileFilters.audioDirectoryFilter), 0);

								MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
										.getMusicPlayerConfiguration();
								tracker.setRandom(musicPlayerConfiguration.isRandomPlayFolders());
								getBApp().push(new PlayerScreen((iTunes) getBApp(), tracker), TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(iTunes.class, ex);
							}
						}
					}.start();
				} else {
					new Thread() {
						public void run() {
							try {
								mTracker.setPos(mMenuList.getFocus());
								getBApp().push(new PlayerScreen((iTunes) getBApp(), mTracker), TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(iTunes.class, ex);
							}
						}
					}.start();
				}
			}
			return super.handleAction(view, action);
		}

		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 9, 2, 32, 32);
			Item nameFile = (Item) mMenuList.get(index);
			if (nameFile.isFolder()) {
				icon.setResource(mFolderIcon);
			} else {
				if (nameFile.isPlaylist())
					icon.setResource(mPlaylistIcon);
				else
					icon.setResource(mCDIcon);
			}

			BText name = new BText(parent, 50, 4, parent.getWidth() - 40, parent.getHeight() - 4);
			name.setShadow(true);
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(Tools.clean(nameFile.getName()), 40));
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_LEFT:
				if (!mFirst) {
					postEvent(new BEvent.Action(this, "pop"));
					return true;
				}
				break;
			case KEY_ENTER:
				getBApp().push(new MusicOptionsScreen((iTunes) getBApp(), mInfoBackground), TRANSITION_LEFT);
				return true;
			case KEY_REPLAY:
				if (((iTunes) getBApp()).getTracker()!=null)
				{
					new Thread() {
						public void run() {
							getBApp().push(new PlayerScreen((iTunes) getBApp(), ((iTunes) getBApp()).getTracker()), TRANSITION_LEFT);
							getBApp().flush();
						}
					}.start();
				}
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		private Tracker mTracker;

		private boolean mFirst;
	}

	public class MusicScreen extends DefaultScreen {

		private BList list;

		public MusicScreen(iTunes app) {
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
				getBApp().push(new MusicOptionsScreen((iTunes) getBApp(), mInfoBackground), TRANSITION_LEFT);
				return true;
			case KEY_REPLAY:
				if (((iTunes) getBApp()).getTracker()!=null)
				{
					new Thread() {
						public void run() {
							getBApp().push(new PlayerScreen((iTunes) getBApp(), ((iTunes) getBApp()).getTracker()), TRANSITION_LEFT);
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
						getBApp().push(new PlayerScreen((iTunes) getBApp(), mTracker), TRANSITION_LEFT);
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
					if (nameFile != null) {
						if (nameFile.isFile())
							return getAudio(((File) nameFile.getValue()).getCanonicalPath());
						else
							return getAudio((String) nameFile.getValue());
					}
				} catch (Exception ex) {
					Tools.logException(iTunes.class, ex);
				}
			}
			return null;
		}

		private MusicInfo mMusicInfo;

		private Tracker mTracker;
	}

	public class PlayerScreen extends DefaultScreen {

		public PlayerScreen(iTunes app, Tracker tracker) {
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

					MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
							.getMusicPlayerConfiguration();
					if (musicPlayerConfiguration.isScreensaver()) {
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
				LyricsScreen lyricsScreen = new LyricsScreen((iTunes) getBApp(), mTracker);
				getBApp().push(lyricsScreen, TRANSITION_LEFT);
				getBApp().flush();
				return true;
			/*
			 * case KEY_NUM0: MusicConfiguration musicConfiguration =
			 * (MusicConfiguration) ((MusicFactory) getContext().getFactory())
			 * .getAppContext().getConfiguration(); MusicPlayerConfiguration
			 * musicPlayerConfiguration =
			 * Server.getServer().getMusicPlayerConfiguration(); if
			 * (musicPlayerConfiguration.isShowImages()) {
			 * getBApp().play("select.snd"); getBApp().flush(); ImagesScreen
			 * imagesScreen = new ImagesScreen((Music) getBApp(), mTracker);
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

		public LyricsScreen(iTunes app, Tracker tracker) {
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
				Tools.logException(iTunes.class, ex);
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
										Tools.logException(iTunes.class, ex, "Could not update lyrics");
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
							Tools.logException(iTunes.class, ex, "Could retrieve lyrics");
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

		public ImagesScreen(iTunes app, Tracker tracker) {
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
				Tools.logException(iTunes.class, ex);
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
							mResults = Yahoo.getImages("\"" + lyricsAudio.getArtist() + "\" music");
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
						Tools.logException(iTunes.class, ex, "Could not retrieve image");
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

	public static class iTunesFactory extends AppFactory {

		public void initialize() {
			iTunesConfiguration iTunesConfiguration = (iTunesConfiguration) getAppContext().getConfiguration();

			MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();

			Server.getServer().scheduleLongTerm(new ReloadTask(new ReloadCallback() {
				public void reload() {
					try {
						log.debug("iTunes");
						reloadItunesLibrary(false);
					} catch (Exception ex) {
						log.error("Could not reload iTunes playlists", ex);
					}
				}
			}), 5);
		}

		public void updateAppContext(AppContext appContext) {
			super.updateAppContext(appContext);

			reloadItunesLibrary(true);
		}

		private void reloadItunesLibrary(boolean interrupt) {
			final iTunesConfiguration iTunesConfiguration = (iTunesConfiguration) getAppContext().getConfiguration();

			MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();

			if (mThread != null && mThread.isAlive()) {
				if (interrupt)
					mThread.interrupt();
				else
					return;
			}

			mThread = new Thread() {
				public void run() {
					try {
						boolean reload = false;
						Date fileDate = new Date();
						PersistentValue persistentValue = PersistentValueManager.loadPersistentValue(iTunes.class.getName()
								+ "." + "refresh3");
						if (persistentValue != null) {
							try
							{
								Date date = new Date(Long.parseLong(persistentValue.getValue()));
								File file = new File(iTunesConfiguration.getPlaylistPath());
								if (file.exists()) {
									fileDate = new Date(file.lastModified());
									if (fileDate.getTime() - date.getTime() > 1000)
										reload = true;
								}
							}
							catch (Exception ex)
							{
								log.error("Invalid date: " + persistentValue.getValue(), ex);
								reload = true;
							}
						} else
							reload = true;
						//PlaylistParser PlaylistParser = new PlaylistParser("D:/galleon/iTunes Music Library.xml");

						if (reload) {
							log.info("Reloading iTunes Library");
							String location = iTunesConfiguration.getPlaylistPath();
							File file = new File(iTunesConfiguration.getPlaylistPath());
							if (file.exists()) {
								String path = System.getProperty("data") + File.separator + "itunes";
								File dir = new File(path);
								if (!dir.exists()) {
									dir.mkdirs();
								}
								File copy = new File(dir.getCanonicalPath()  + File.separator + "playlist.xml");
								Tools.copy(file,copy);
								if (copy.exists())
									location = copy.getCanonicalPath();
							}

							PlaylistParser PlaylistParser = new PlaylistParser(location);

							synchronized (this) {
					            try {
					                AudioManager.clean();
					            } catch (Exception ex) {
					                Tools.logException(iTunes.class, ex);
					            }
					        }

							PersistentValueManager.savePersistentValue(iTunes.class.getName() + ".refresh3", String.valueOf(fileDate.getTime()));
							log.info("Reloaded iTunes Library");
						}
					} catch (Throwable ex) {
						Tools.logException(iTunes.class, ex);
					}
				}
			};
			mThread.setPriority(Thread.MIN_PRIORITY);
			mThread.start();
		}

		private String decode(String value) {
			try {
				return Tools.unEscapeXMLChars(URLDecoder.decode(value, "UTF-8"));
			} catch (Exception ex) {
			}
			return value;
		}
	}

	private static Audio getAudio(String path) {
		Audio audio = null;
		try {
			List list = AudioManager.findByPath(path);
			if (list != null && list.size() > 0) {
				audio = (Audio) list.get(0);
			}
		} catch (Exception ex) {
			Tools.logException(iTunes.class, ex);
		}

		if (audio == null) {
			try {
				audio = (Audio) MediaManager.getMedia(path);
				AudioManager.createAudio(audio);
			} catch (Exception ex) {
				Tools.logException(iTunes.class, ex);
			}
		}
		return audio;
	}

	// TODO Handle multiple iTunes playlists
	private static Thread mThread;
}