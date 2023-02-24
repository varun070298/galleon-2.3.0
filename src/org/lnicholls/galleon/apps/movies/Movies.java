package org.lnicholls.galleon.apps.movies;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.Movie;
import org.lnicholls.galleon.database.MovieManager;
import org.lnicholls.galleon.database.Theater;
import org.lnicholls.galleon.database.TheaterManager;
import org.lnicholls.galleon.database.TheaterShowtimes;
import org.lnicholls.galleon.media.MovieFile;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.Amazon;
import org.lnicholls.galleon.util.IMDB;
import org.lnicholls.galleon.util.Lyrics;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.ReloadCallback;
import org.lnicholls.galleon.util.ReloadTask;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.Yahoo;
import org.lnicholls.galleon.util.FileSystemContainer.Item;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultMenuScreen;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.MusicInfo;
import org.lnicholls.galleon.widget.ScrollText;
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;
import org.lnicholls.galleon.widget.DefaultApplication.VersionScreen;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.interfaces.IArgumentList;

public class Movies extends DefaultApplication {

	private static Logger log = Logger.getLogger(Movies.class.getName());

	public final static String TITLE = "Movies";

	private Resource mMenuBackground;

	private Resource mInfoBackground;

	private Resource mPlayerBackground;

	private Resource mLyricsBackground;

	private Resource mImagesBackground;

	private Resource mFolderIcon;

	private Resource mCDIcon;

	private Resource mPlaylistIcon;

	private Resource mFavoriteIcon;

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
		mFavoriteIcon = getSkinImage("menu", "favorite");

		MoviesConfiguration moviesConfiguration = (MoviesConfiguration) ((MoviesFactory) getFactory())
				.getAppContext().getConfiguration();

		push(new TheaterMenuScreen(this), TRANSITION_NONE);

		initialize();
	}

	public class TheaterMenuScreen extends DefaultMenuScreen {
		public TheaterMenuScreen(Movies app) {
			super(app, "Theaters");

			getBelow().setResource(mMenuBackground);

			MoviesConfiguration moviesConfiguration = (MoviesConfiguration) ((MoviesFactory) getFactory())
					.getAppContext().getConfiguration();

			List list = null;
			try {
				list = TheaterManager.listAll();
			} catch (Exception ex) {
				Tools.logException(Movies.class, ex);
			}

			if (list != null && list.size() > 0) {
				for (Iterator i = list.iterator(); i.hasNext(); /* Nothing */) {
					Theater theater = (Theater) i.next();
					if (theater.getFavorite()!=null && theater.getFavorite().intValue()>0)
						mMenuList.add(theater);
				}
				for (Iterator i = list.iterator(); i.hasNext(); /* Nothing */) {
					Theater theater = (Theater) i.next();
					if (theater.getFavorite()==null || theater.getFavorite().intValue()==0)
						mMenuList.add(theater);
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
								List list = new ArrayList();
								for (int i = 0; i < mMenuList.size(); i++) {
									list.add(mMenuList.get(i));
								}

								Tracker tracker = new Tracker(list, mMenuList.getFocus());

								getBApp().push(new MovieMenuScreen((Movies) getBApp(), tracker), TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(Movies.class, ex);
							}
						}
					}.start();
					return true;
				}
			}
			return super.handleAction(view, action);
		}

		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 9, 2, 32, 32);
			Theater theater = (Theater) mMenuList.get(index);
			if (theater.getFavorite()!=null && theater.getFavorite().intValue()>0)
				icon.setResource(mFavoriteIcon);
			else
				icon.setResource(mFolderIcon);

			BText name = new BText(parent, 50, 4, parent.getWidth() - 40, parent.getHeight() - 4);
			name.setShadow(true);
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(theater.getName(), 45));
		}

	    public boolean handleKeyPress(int code, long rawcode) {
	        switch (code) {
	        case KEY_THUMBSDOWN:
                getBApp().play("thumbsdown.snd");
                getBApp().flush();

                BView row = mMenuList.getRow(mMenuList.getFocus());
                BView icon = (BView) row.getChild(0);
                icon.setResource(mFolderIcon);
                icon.flush();

                DefaultApplication application = (DefaultApplication)getApp();
				if (!application.isDemoMode())
				{
	                try
	                {
		                Theater theater = (Theater) mMenuList.get(mMenuList.getFocus());
		               	theater.setFavorite(new Integer(0));
		               	TheaterManager.updateTheater(theater);
					} catch (Exception ex) {
						log.error("Could not update theater", ex);
					}
				}

	            return true;
	        case KEY_THUMBSUP:
                getBApp().play("thumbsup.snd");
                getBApp().flush();

                row = mMenuList.getRow(mMenuList.getFocus());
                icon = (BView) row.getChild(0);
                icon.setResource(mFavoriteIcon);
                icon.flush();

                application = (DefaultApplication)getApp();
				if (!application.isDemoMode())
				{
	                try
	                {
	                	Theater theater = (Theater) mMenuList.get(mMenuList.getFocus());
	                	theater.setFavorite(new Integer(1));
	                	TheaterManager.updateTheater(theater);
	                } catch (Exception ex) {
						log.error("Could not update theater", ex);
					}
				}

	            return true;
	        }
	        return super.handleKeyPress(code, rawcode);
	    }
	}

	public class MovieMenuScreen extends DefaultMenuScreen {
		public MovieMenuScreen(Movies app, Tracker tracker) {
			super(app, "Movies");

			getBelow().setResource(mMenuBackground);

			mTracker = tracker;

			MoviesConfiguration moviesConfiguration = (MoviesConfiguration) ((MoviesFactory) getFactory())
					.getAppContext().getConfiguration();

			int start = TOP - 25;

			Theater theater = (Theater) mTracker.getList().get(mTracker.getPos());

			BText countText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20);
			countText.setFlags(IHmeProtocol.RSRC_HALIGN_CENTER);
			countText.setFont("default-18.font");
			countText.setColor(Color.GREEN);
			countText.setShadow(true);
			countText.setValue(theater.getName());

			mList = theater.getShowtimes();

			if (mList != null && mList.size() > 0) {
				for (Iterator i = mList.iterator(); i.hasNext(); /* Nothing */) {
					TheaterShowtimes theaterShowtimes = (TheaterShowtimes) i.next();
					mMenuList.add(theaterShowtimes);
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
								mMovieTracker = new Tracker(mList, mMenuList.getFocus());

								getBApp().push(new MovieScreen((Movies) getBApp(), mMovieTracker), TRANSITION_LEFT);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(Movies.class, ex);
							}
						}
					}.start();
					return true;
				}
			}
			return super.handleAction(view, action);
		}

		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 9, 2, 32, 32);
			TheaterShowtimes theaterShowtimes = (TheaterShowtimes) mMenuList.get(index);
			icon.setResource(mCDIcon);

			BText name = new BText(parent, 50, 4, parent.getWidth() - 40, parent.getHeight() - 4);
			name.setShadow(true);
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(theaterShowtimes.getMovie().getTitle(), 40));

			if (theaterShowtimes.getMovie().getRated().length() > 0) {
				BText ratedText = new BText(parent, parent.getWidth() - 60 - parent.getHeight(), 4, 60, parent
						.getHeight() - 4);
				ratedText.setShadow(true);
				ratedText.setFlags(RSRC_HALIGN_RIGHT);
				ratedText.setValue(theaterShowtimes.getMovie().getRated());
			}
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			if (mMovieTracker != null)
			{
				mFocus = mMovieTracker.getPos();
				mMovieTracker = (Tracker)mMovieTracker.clone();
			}
			return super.handleEnter(arg, isReturn);
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_LEFT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		private List mList;

		private Tracker mTracker;

		private Tracker mMovieTracker;
	}

	public class MovieScreen extends DefaultScreen {

		private BList list;

		public MovieScreen(Movies app, Tracker tracker) {
			super(app, "", true);

			mTracker = tracker;

			getBelow().setResource(mInfoBackground);

			int start = TOP;

			mRatingText = new BText(getNormal(), BORDER_LEFT, start - 25, BODY_WIDTH, 20);
			mRatingText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			mRatingText.setFont("default-18.font");
			mRatingText.setColor(Color.GREEN);
			mRatingText.setShadow(true);

			mImage = new BView(getNormal(), BORDER_LEFT + BODY_WIDTH - 200, 210, 200, 200, false);
			mImage.setResource(Color.BLACK);
			mImage.setTransparency(0.75f);

			mDateFormat = new SimpleDateFormat();
			mDateFormat.applyPattern("EEE M/d/yyyy");

			mTitleText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 85);
			mTitleText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
			mTitleText.setFont("system-30.font");
			mTitleText.setShadow(true);
			mTitleText.setColor(Color.GREEN);

			start += 90;

			mDescriptionText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH - 200, 130);
			mDescriptionText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
			mDescriptionText.setFont("default-18-bold.font");
			mDescriptionText.setShadow(true);

			start += 140;

			mDateText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
			mDateText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			mDateText.setFont("default-18.font");
			mDateText.setShadow(true);
			mDateText.setVisible(false);

			mActorsText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH - 200, 40);
			mActorsText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP);
			mActorsText.setFont("default-18.font");
			mActorsText.setShadow(true);

			start += 60;

			mGenreText = new BText(getNormal(), BORDER_LEFT, getHeight() - SAFE_TITLE_V - 20, BODY_WIDTH - 20, 20);
			mGenreText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			mGenreText.setFont("default-16.font");
			mGenreText.setShadow(true);

			/*
			 * list = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H + 10,
			 * (getHeight() - SAFE_TITLE_V) - 40, (int) Math.round((getWidth() -
			 * (SAFE_TITLE_H * 2)) / 2.5), 90, 35); list.add("Back to movies");
			 * setFocusDefault(list);
			 */

			BButton button = new BButton(getNormal(), SAFE_TITLE_H + 10, (getHeight() - SAFE_TITLE_V) - 40, (int) Math
					.round((getWidth() - (SAFE_TITLE_H * 2)) / 2.5), 35);
			button.setResource(createText("default-24.font", Color.white, "Return to movies"));
			button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null, null, true);
			setFocus(button);
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			getBelow().setResource(mInfoBackground);
			updateView();

			return super.handleEnter(arg, isReturn);
		}

		private void updateView() {
			TheaterShowtimes theaterShowtimes = (TheaterShowtimes) mTracker.getList().get(mTracker.getPos());
			final Movie movie = theaterShowtimes.getMovie();
			setSmallTitle(theaterShowtimes.getTimes());
			if (movie.getRated() != null) {
				mRatingText.setValue(movie.getRated());
			} else
				mRatingText.setValue("");
			// setTitle(movie.getTitle());
			mTitleText.setValue(movie.getTitle());
			if (movie.getPlotOutline().length() > 0)
				mDescriptionText.setValue(movie.getPlotOutline());
			else
				mDescriptionText.setValue(movie.getPlot());
			mDateText.setValue(mDateFormat.format(theaterShowtimes.getDay()));
			String actors = null;
			StringTokenizer tokenizer = new StringTokenizer(movie.getActors(), ",");
			int count = 0;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (count++ <= 1) {
					if (actors == null)
						actors = token;
					else
						actors = actors + ", " + token;
				} else
					break;
			}
			if (actors != null)
				mActorsText.setValue(actors);
			else
				mActorsText.setValue("");
			mGenreText.setValue(movie.getGenre());

			if (movie.getThumbUrl().length() > 0) {
				if (mImageThread != null && mImageThread.isAlive()) {
					mImageThread.interrupt();
					mImageThread = null;
				}

				mImageThread = new Thread() {
					public void run() {
						int x = mImage.getX();
						int y = mImage.getY();

						try {
							Image image = Tools.retrieveCachedImage(new URL(movie.getThumbUrl()));
							if (image == null) {
								image = Tools.getImage(new URL(movie.getThumbUrl()), -1, -1);
								if (image != null)
									Tools.cacheImage(image, image.getWidth(null), image.getHeight(null), movie
											.getThumbUrl());
							}
							if (image != null) {
								synchronized (this) {
									mImage.setResource(createImage(image), RSRC_IMAGE_BESTFIT);
									mImage.setVisible(true);
									mImage.setTransparency(0.0f, mAnim);
									getBApp().flush();
								}
							} else {
								synchronized (this) {
									mImage.setVisible(false);									mImage.flush();									mImage.clearResource();
									getBApp().flush();
								}
							}
						} catch (Exception ex) {
							Tools.logException(MusicInfo.class, ex, "Could not retrieve cover");
						}
					}
				};
				mImageThread.start();
			} else {
				try {
					setPainting(false);
					mImage.setVisible(false);
					if (mImage.getResource() != null)					{						mImage.getResource().flush();
						mImage.getResource().remove();					}
				} finally {
					setPainting(true);
				}
			}
		}

		private void clearImage() {
			try {
				setPainting(false);
				if (mImageThread != null && mImageThread.isAlive()) {
					mImageThread.interrupt();
					mImageThread = null;
				}
				mImage.setVisible(false);
				if (mImage.getResource() != null)				{
					mImage.getResource().flush();					mImage.getResource().remove();				}
				getBApp().flush();
			} finally {
				setPainting(true);
			}
		}

		public boolean handleExit() {
			clearImage();
			return super.handleExit();
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
			case KEY_RIGHT:
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
			}
			return super.handleKeyPress(code, rawcode);
		}

		public void getNextPos() {
			if (mTracker != null) {
				int pos = mTracker.getNextPos();
			}
		}

		public void getPrevPos() {
			if (mTracker != null) {
				int pos = mTracker.getPrevPos();
			}
		}

		private Tracker mTracker;

		private SimpleDateFormat mDateFormat;

		private BText mRatingText;

		private BText mTitleText;

		private BText mDescriptionText;

		private BText mDateText;

		private BText mActorsText;

		private BText mGenreText;

		private BView mImage;

		private Thread mImageThread;

		private Resource mAnim = getResource("*1000");
	}

	public class LyricsScreen extends DefaultScreen {
		private BList list;

		public LyricsScreen(Movies app, Tracker tracker) {
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
				Tools.logException(Movies.class, ex);
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
										Tools.logException(Movies.class, ex, "Could not update lyrics");
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
							Tools.logException(Movies.class, ex, "Could retrieve lyrics");
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

		public ImagesScreen(Movies app, Tracker tracker) {
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
				Tools.logException(Movies.class, ex);
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
						Tools.logException(Movies.class, ex, "Could not retrieve image");
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

	public static class MoviesFactory extends AppFactory {

		public void initialize() {
			final MoviesConfiguration moviesConfiguration = (MoviesConfiguration) getAppContext().getConfiguration();

			Server.getServer().scheduleLongTerm(new ReloadTask(new ReloadCallback() {
				public void reload() {
					try {
						log.debug("Movies");
						HashMap currentTheaters = new HashMap();
						HashMap currentMovies = new HashMap();

						try {
							Parser parser = new Parser("http://www.google.com/search?hl=en&oi=showtimes&near="
									+ moviesConfiguration.getZip() + "&q=movie:+theaters+today");

							NodeFilter filter = null;
					           NodeList list = new NodeList ();

					           Theater theater = null;

					           filter = new NodeClassFilter (TableTag.class);
					           list = parser.extractAllNodesThatMatch (filter);
					           if (list!=null && list.size()>0)
					           {
					        	   //System.out.println("tables="+list.size());
					        	   for (int i = 7; i < list.size (); i++)
					               {
					        		   TableTag table = (TableTag)list.elementAt (i);
					        		   filter = new NodeClassFilter (TableColumn.class);
					    	           NodeList tdList = new NodeList();
					    	           table.collectInto(tdList, filter);
					    	           //System.out.println("columns="+tdList.size());
					    	           for (int j = 0; j < tdList.size (); j++)
							           {
							        	   TableColumn td = (TableColumn)tdList.elementAt (j);
							        	   filter = new NodeClassFilter (LinkTag.class);
							        	   NodeList linkList = new NodeList();
						    	           td.collectInto(linkList, filter);
						    	           String title = null;
						    	           String duration = null;
						    	           String imdb = null;
						    	           String genre = null;
						    	           String rating = null;
						    	           String times = null;

						    	           if (td.getAttribute("colspan")!=null && td.getAttribute("colspan").equals("6"))
							        	   {
							    	           //System.out.println("links="+linkList.size());
									           for (int k = 0; k < linkList.size (); k++)
									           {
									               LinkTag linkTag = (LinkTag)linkList.elementAt (k);
									               if (linkTag.getLink().indexOf("http://www.google.com/movies")!=-1)
									               {
									            	   theater = new Theater();
													   theater.setName(clean(linkTag.getLinkText()));

									            	   int position = td.findPositionOf(linkTag);
									        		   for (int l=position+1;l<td.getChildCount();l++)
									        		   {
									        			   Node value = td.childAt(l);
									        			   if (!(value instanceof Tag))
									        			   {
								        					   String address = value.getText().replaceAll("&nbsp;"," ").replaceAll("&amp;","&").replaceAll("&#39;","'").trim();
								        					   String REGEX = "(.*)- \\((.*)\\)([^,]*) -$";
												               Pattern p = Pattern.compile(REGEX);
											                   Matcher m = p.matcher(address);
											                   if (m.find() && m.groupCount()==3) {
											                	    theater.setAddress(m.group(1).trim());
																	theater.setTelephone("(" + m.group(2).trim() + ") "
																			+ m.group(3).trim());
											                       break;
											                   }
									        			   }
									        		   }
									        		   theater = createTheater(theater);
													   if (!currentTheaters.containsKey(theater.getName()))
													   {
															currentTheaters.put(theater.getName(), theater);
													   }
									            	   break;
									               }
									           }
							        	   }
							        	   else
								           {
								        		   if (td.toPlainTextString().replaceAll("&nbsp;","").trim().length()>0 && td.getAttribute("width")!=null && td.getAttribute("width").equals("45%"))
								        		   {
								        			   for (int k = 0; k < linkList.size (); k++)
											           {
											               LinkTag linkTag = (LinkTag)linkList.elementAt (k);
									        			   if (k==0)
										        		   {
										        			   title = Tools.unEscapeXMLChars(linkTag.getLinkText());  // title

										        			   int position = td.findPositionOf(linkTag);
											        		   for (int l=position+1;l<td.getChildCount();l++)
											        		   {
											        			   Node value = td.childAt(l);
											        			   if (!(value instanceof Tag))
											        			   {
											        				   String text = value.getText().replaceAll("&nbsp;","").trim();  //2hr&nbsp;20min - Action/Adventure/SciFi/Fantasy -
											        				   String REGEX = "([^-]*) - Rated(.*) - (.*) -"; //1hr56min - RatedR - Comedy -
														               Pattern p = Pattern.compile(REGEX);
													                   Matcher m = p.matcher(text);
													                   if (m.find() && m.groupCount()==3) {
													                	   duration = m.group(1).trim();  // duration
													                	   rating = m.group(2).trim();  // rating
													                	   genre = m.group(3).trim();  // genre
													                	   break;
													                   }

											        				   break;
											        			   }
											        		   }
										        		   }
									        			   else
								        				   if (linkTag.getLinkText().toLowerCase().trim().indexOf("imdb")!=-1)
										        		   {
								        					   String REGEX = ".*/title/tt(.*)/";
												               Pattern p = Pattern.compile(REGEX);
											                   Matcher m = p.matcher(linkTag.getLink());
											                   if (m.find()) {
											                	   imdb = m.group(1).trim();  // imdb
											                   }

											                   int position = td.findPositionOf(linkTag);
											        		   for (int l=position+1;l<td.getChildCount();l++)
											        		   {
											        			   Node value = td.childAt(l);
											        			   if (value.toPlainTextString().trim().length()>0)
											        			   {
											        				   if (times == null)
											        					   times = value.toPlainTextString().replaceAll("&nbsp;",",").trim();   // times
											        				   else
											        					   times = times + value.toPlainTextString().replaceAll("&nbsp;",",").trim();   // times
											        			   }
											        		   }

											        		   //System.out.println("title="+title);
											        		   //System.out.println("duration="+duration);
											        		   //System.out.println("imdb="+imdb);
											        		   //System.out.println("genre="+genre);
											        		   //System.out.println("rating="+rating);

											        		   try {
																	Movie movie = null;
																	List movies = MovieManager.findByTitle(title);
																	if (movies.size() > 0) {
																		movie = (Movie) movies.get(0);
																	} else {
																		movie = new Movie();
																		MovieFile.defaultProperties(movie);
																		movie.setTitle(title);
																		movie.setGenre(genre);
																		movie.setRated(rating);
																		movie.setIMDB(imdb);																		IMDB.getMovie(movie);
																		String poster = null; //Amazon.getMoviePoster(movie.getTitle());																		if (poster != null) {
																			movie.setThumbUrl(poster);
																			Tools.cacheImage(new URL(poster), movie.getTitle());
																		}
																		MovieManager.createMovie(movie);
																	}

																	if (movie != null) {
																		List currentShowtimes = theater.getShowtimes();
																		if (currentShowtimes == null) {
																			currentShowtimes = new ArrayList();
																			theater.setShowtimes(currentShowtimes);
																		}
																		TheaterShowtimes theaterShowtimes = new TheaterShowtimes();
																	    theaterShowtimes.setTimes(times);
																		theaterShowtimes.setMovie(movie);
																		theaterShowtimes.setDay(new Date());
																		currentShowtimes.add(theaterShowtimes);
																		if (!currentMovies.containsKey(title))
																		{
																			currentMovies.put(movie.getTitle(), movie);
																		}
																		TheaterManager.updateTheater(theater);
																	}
																} catch (Exception ex) {
																	log.error("Could not create theater: " + theater.getName(), ex);
																}

											        		   break;
										        		   }
											           }
								        		   }
								           }
							           }
					               }

					           }
							parser = null;
						} catch (Exception ex) {
							Tools.logException(Movies.class, ex);
						}

						if (currentTheaters.size() == 0 || currentMovies.size() == 0) {
							try {
								NodeFilter filter = null;
								NodeList list = new NodeList();

								String theatre = null;

								boolean morePages = true;

								String page = "http://www.fandango.com/TheaterListings.aspx?pn=1&location="
										+ moviesConfiguration.getZip();

								while (morePages) {
									Parser parser = new Parser(page);

									filter = new NodeClassFilter(LinkTag.class);
									list = parser.extractAllNodesThatMatch(filter);
									if (list != null && list.size() > 0) {
										morePages = false;
										Theater theater = null;
										for (int i = 0; i < list.size(); i++) {
											LinkTag link = (LinkTag) list.elementAt(i);

											if (link.getAttribute("class") != null
													&& link.getAttribute("class").equals("titleLink")) {
												theater = new Theater();
												theater.setName(clean(link.getLinkText())); // theater
																							// name

												CompositeTag parent = (CompositeTag) link.getParent();
												int position = parent.findPositionOf(link);
												Node node = (Node) parent.getChild(++position);
												while (position < parent.getChildCount()) {
													if (node instanceof Tag) {
														Tag tag = (Tag) node;
														if (tag.getAttribute("class") != null
																&& tag.getAttribute("class").equals("address")) {
															NodeList children = tag.getChildren();
															theater.setAddress(children.elementAt(0)
																	.toPlainTextString()); // theater
																							// address
															break;
														}
													}
													node = (Node) parent.getChild(++position);
												}

												theater = createTheater(theater);
												if (!currentTheaters.containsKey(theater.getName()))
													currentTheaters.put(theater.getName(), theater);
											} else if (link.getAttribute("id") != null
													&& link.getAttribute("id").indexOf("MovieRepeater") != -1) {
												// <a
												// id="TheaterRepeater__ctl0_MovieRepeater__ctl0_movieControl_movieTitleLink1"
												// class="textLink"
												// href="http://www.fandango.com/MoviePage.aspx?date=&amp;mid=34505">Dark
												// Water</a>
												String title = clean(link.getLinkText()); // movie
																							// title
												String id = null;
												String rated = null;

												String REGEX = ".*mid=(.*)";
												Pattern p = Pattern.compile(REGEX);
												Matcher m = p.matcher(link.getLink());
												if (m.find()) {
													id = m.group(1); // id
												}

												CompositeTag parent = (CompositeTag) link.getParent();
												int position = parent.findPositionOf(link);
												Node node = (Node) parent.getChild(++position);
												while (position < parent.getChildCount()) {
													if (node instanceof Tag) {
														Tag tag = (Tag) node;
														if (tag.getAttribute("class") != null
																&& tag.getAttribute("class").equals("rating")) {
															NodeList children = tag.getChildren();
															if (children!=null && children.size() > 0) {
																REGEX = "\\((.*)\\)"; // Rated
																						// PG-13
																						// for
																						// intense
																p = Pattern.compile(REGEX);
																m = p
																		.matcher(children.elementAt(0)
																				.toPlainTextString());
																if (m.find()) {
																	rated = m.group(1); // rated
																}
															}
															break;
														}
													}
													node = (Node) parent.getChild(++position);
												}

												CompositeTag parentParent = (CompositeTag) parent.getParent(); // tr
												position = parentParent.findPositionOf(parent);
												node = (Node) parentParent.getChild(++position);
												while (position < parentParent.getChildCount()) {
													if (node instanceof Tag) {
														Tag tag = (Tag) node;
														if (tag.getAttribute("class") != null
																&& (tag.getAttribute("class").equals("tmOdd lCell") || tag
																		.getAttribute("class").equals("tmEven lCell"))) {
															String times = tag.toPlainTextString().trim().replaceAll(
																	" I", ","); // times

															try {
																Movie movie = null;
																List movies = MovieManager.findByTitle(title);
																if (movies.size() > 0) {
																	movie = (Movie) movies.get(0);
																} else {
																	movie = new Movie();
																	MovieFile.defaultProperties(movie);
																	movie.setTitle(title);
																	movie.setExternalId(id);
																	movie.setRated(rated);
																	IMDB.getMovie(movie);
																	String poster = null; //Amazon.getMoviePoster(movie.getTitle());
																	if (poster != null) {
																		movie.setThumbUrl(poster);
																		Tools.cacheImage(new URL(poster), movie
																				.getTitle());
																	}
																	MovieManager.createMovie(movie);
																}

																if (movie != null) {
																	List currentShowtimes = theater.getShowtimes();
																	if (currentShowtimes == null) {
																		currentShowtimes = new ArrayList();
																		theater.setShowtimes(currentShowtimes);
																	}

																	TheaterShowtimes theaterShowtimes = new TheaterShowtimes();
																	theaterShowtimes.setTimes(times);
																	theaterShowtimes.setMovie(movie);
																	theaterShowtimes.setDay(new Date());
																	currentShowtimes.add(theaterShowtimes);

																	if (!currentMovies.containsKey(movie.getTitle()))
																		currentMovies.put(movie.getTitle(), movie);

																	TheaterManager.updateTheater(theater);
																}
															} catch (Exception ex) {
																log.error("Could not create theater: "
																		+ theater.getName(), ex);
															}

															break;
														}
													}
													node = (Node) parentParent.getChild(++position);
												}
											} else if (link.getAttribute("id") != null
													&& link.getAttribute("id").equals("paginationControl_nextLink")) {
												if (link.getLink() != null && link.getLink().trim().length() > 0) {
													page = StringEscapeUtils.unescapeHtml(link.getLink());
													morePages = true;
												}
											}
										}
									}
									parser = null;
								}
							} catch (Exception ex) {
								Tools.logException(Movies.class, ex);
							}
						}

						if (currentTheaters.size() > 0 && currentMovies.size() > 0) {
							// remove theaters not referenced anymore
							try {
								List theaters = TheaterManager.listAll();

								if (theaters != null && theaters.size() > 0) {
									for (Iterator i = theaters.iterator(); i.hasNext(); /* Nothing */) {
										Theater theater = (Theater) i.next();

										if (!currentTheaters.containsKey(theater.getName()))
											TheaterManager.deleteTheater(theater);
									}
								}

								currentTheaters.clear();
							} catch (Exception ex) {
								Tools.logException(Movies.class, ex);
							}

							// remove movies not referenced anymore
							try {
								List movies = MovieManager.listAll();

								if (movies != null && movies.size() > 0) {
									for (Iterator i = movies.iterator(); i.hasNext(); /* Nothing */) {
										Movie movie = (Movie) i.next();

										if (!currentMovies.containsKey(movie.getTitle())) {
											Tools.deleteCachedImage(movie.getTitle());
											MovieManager.deleteMovie(movie);
										}
									}
								}

								currentMovies.clear();
							} catch (Exception ex) {
								Tools.logException(Movies.class, ex);
							}
						}

						// remove theaters with no movies
						try {
							List theaters = TheaterManager.listAll();

							if (theaters != null && theaters.size() > 0) {
								for (Iterator i = theaters.iterator(); i.hasNext(); /* Nothing */) {
									Theater theater = (Theater) i.next();

									List showTimes = theater.getShowtimes();
									if (showTimes == null || showTimes.size()==0)
									{
										TheaterManager.deleteTheater(theater);
									}
								}
							}
						} catch (Exception ex) {
							Tools.logException(Movies.class, ex);
						}
					} catch (Exception ex) {
						log.error("Could not download theaters", ex);
					}
				}
			}), 60 * 12);
		}

		private String clean(String value) {
			return StringEscapeUtils.unescapeHtml(value.replaceAll("&nbsp;", " ").trim());
		}

		private Theater createTheater(Theater theater) {
			try {
				List theatres = TheaterManager.findByName(theater.getName());

				if (theatres.size() > 0) {
					Theater existing = (Theater) theatres.get(0);
					Date date = new Date();
					if (date.getTime() > existing.getDateModified().getTime()) {
						existing.setAddress(theater.getAddress());
						existing.setTelephone(theater.getTelephone());
						existing.setDateModified(date);
						existing.setShowtimes(new ArrayList());
						TheaterManager.updateTheater(existing);
						return existing;
					}
				} else {
					if (log.isDebugEnabled())
						log.debug("New: " + theater.getName());
					theater.setDateModified(new Date());
					TheaterManager.createTheater(theater);
				}
			} catch (Exception ex) {
				log.error("Could not create theater", ex);
			}
			return theater;
		}
	}
}