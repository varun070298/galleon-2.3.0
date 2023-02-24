package org.lnicholls.galleon.apps.togo;

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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.database.VideoManager;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.server.TiVo;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultMenuScreen;
import org.lnicholls.galleon.widget.DefaultOptionList;
import org.lnicholls.galleon.widget.DefaultOptionsScreen;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.LabelText;
import org.lnicholls.galleon.widget.OptionsButton;

import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.Resource;

public class ToGo extends DefaultApplication {

	private static Logger log = Logger.getLogger(ToGo.class.getName());

	public final static String TITLE = "ToGo";

	private Resource mMenuBackground;

	private Resource mInfoBackground;

	private Resource mYellowIcon;

	private Resource mYellowExclamationIcon;

	private Resource mWhiteIcon;

	private Resource mGreenIcon;

	private Resource mRedIcon;

	private Resource mBlueIcon;

	private Resource mEmptyIcon;

	private Resource mIcon;

	private Resource mLockIcon;

	public void init(IContext context) throws Exception {
		super.init(context);

		mMenuBackground = getSkinImage("menu", "background");
		mInfoBackground = getSkinImage("info", "background");
		mYellowIcon = getSkinImage("menu", "expiresSoon");
		mYellowExclamationIcon = getSkinImage("menu", "expired");
		mWhiteIcon = getSkinImage("menu", "info");
		mGreenIcon = getSkinImage("menu", "saveUntilDelete");
		mRedIcon = getSkinImage("menu", "recording");
		mBlueIcon = getSkinImage("menu", "suggestion");
		mEmptyIcon = getSkinImage("menu", "empty");
		mIcon = getSkinImage("menu", "icon");
		mLockIcon = getSkinImage("menu", "lock");

		push(new ToGoMenuScreen(this), TRANSITION_NONE);

		initialize();
	}

	public class OptionsScreen extends DefaultOptionsScreen {

		public OptionsScreen(DefaultApplication app) {
			super(app);

			getBelow().setResource(mInfoBackground);

			ToGoConfiguration togoConfiguration = (ToGoConfiguration) ((ToGoFactory) getFactory()).getAppContext()
					.getConfiguration();

			int start = TOP;
			int width = 280;
			int increment = 37;
			int height = 25;
			BText text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
			text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
			text.setFont("default-24-bold.font");
			text.setShadow(true);
			text.setValue("Show statistics");

			NameValue[] nameValues = new NameValue[] { new NameValue("Yes", "true"), new NameValue("No", "false") };
			mShowStatsButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height,
					true, nameValues, String.valueOf(togoConfiguration.isShowStats()));

			start = start + increment;

			text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
			text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
			text.setFont("default-24-bold.font");
			text.setShadow(true);
			text.setValue("Sorting");

			String sort = togoConfiguration.getSort();
			if (sort == null)
				sort = ToGoConfiguration.SORT_DATE_LATEST;
			nameValues = new NameValue[] { new NameValue("Alphabetic", ToGoConfiguration.SORT_ALPHA),
					new NameValue("Latest First", ToGoConfiguration.SORT_DATE_LATEST),
					new NameValue("Oldest First", ToGoConfiguration.SORT_DATE_OLDEST) };
			mSortButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height, true,
					nameValues, sort);

			setFocusDefault(mShowStatsButton);
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			getBelow().setResource(mInfoBackground);

			return super.handleEnter(arg, isReturn);
		}

		public boolean handleExit() {

			try {
				DefaultApplication application = (DefaultApplication) getApp();
				if (!application.isDemoMode()) {
					ToGoConfiguration togoConfiguration = (ToGoConfiguration) ((ToGoFactory) getFactory())
							.getAppContext().getConfiguration();
					togoConfiguration.setShowStats(Boolean.valueOf(mShowStatsButton.getValue()).booleanValue());
					togoConfiguration.setSort(mSortButton.getValue());

					Server.getServer().updateApp(((ToGoFactory) getFactory()).getAppContext());
				}
			} catch (Exception ex) {
				Tools.logException(ToGo.class, ex, "Could not configure togo app");
			}
			return super.handleExit();
		}

		private OptionsButton mShowStatsButton;

		private OptionsButton mSortButton;
	}

	public class ToGoMenuScreen extends DefaultMenuScreen {
		public ToGoMenuScreen(ToGo app) {
			super(app, "Now Playing List");

			setFooter("Press ENTER for options");

			getBelow().setResource(mMenuBackground);

			mDateFormat = new SimpleDateFormat();
			mDateFormat.applyPattern("EEE M/dd");
			mCalendar = new GregorianCalendar();

			int start = TOP - 25;

			final ToGoConfiguration togoConfiguration = (ToGoConfiguration) ((ToGoFactory) getFactory())
					.getAppContext().getConfiguration();
			mCountText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20);
			mCountText.setFlags(IHmeProtocol.RSRC_HALIGN_CENTER);
			mCountText.setFont("default-18.font");
			mCountText.setColor(Color.GREEN);
			mCountText.setShadow(true);

			// TODO Update list
			createMenu();
		}

		private void createMenu() {
			ToGoConfiguration togoConfiguration = (ToGoConfiguration) ((ToGoFactory) getFactory()).getAppContext()
					.getConfiguration();
			mMenuList.clear();
			try {
				mSort = togoConfiguration.getSort();
				if (mSort == null)
					mSort = ToGoConfiguration.SORT_DATE_LATEST;
				List recordings = VideoManager.listAll();
				Video[] videoArray = (Video[]) recordings.toArray(new Video[0]);
				Arrays.sort(videoArray, new Comparator() {
					public int compare(Object o1, Object o2) {
						Video video1 = (Video) o1;
						Video video2 = (Video) o2;

						if (video1.getTitle() != null && video2.getTitle() != null
								&& mSort.equals(ToGoConfiguration.SORT_ALPHA))
							return video1.getTitle().compareTo(video2.getTitle());
						else if (video1.getDateRecorded() != null && video2.getDateRecorded() != null
								&& mSort.equals(ToGoConfiguration.SORT_DATE_LATEST))
							return -video1.getDateRecorded().compareTo(video2.getDateRecorded());
						else if (video1.getDateRecorded() != null && video2.getDateRecorded() != null
								&& mSort.equals(ToGoConfiguration.SORT_DATE_OLDEST))
							return video1.getDateRecorded().compareTo(video2.getDateRecorded());
						else
							return 0;
					}
				});

				mTotalCount = 0;				mTotalTime = 0;				mTotalSize = 0;				mTotalCapacity = 0;				List tivos = Server.getServer().getTiVos();
				for (int i = 0; i < videoArray.length; i++) {
					Video video = (Video) videoArray[i];
					if (video.getStatus() != Video.STATUS_RECORDING) {
						// Only display recordings that are still on the
						// recorder
						if (video.getStatus() == Video.STATUS_DOWNLOADED || video.getStatus() == Video.STATUS_DELETED) {
							if (video.getAvailability() == null)
								continue;
							else if (video.getAvailability().intValue() == Video.RECORDING_DELETED)
								continue;
						}

						boolean match = false;
						if (video.getSource() != null) {
							for (int j = 0; j < tivos.size(); j++) {
								TiVo tivo = (TiVo) tivos.get(j);
								if (tivo.getAddress().equals(video.getSource())) {
									match = true;
									break;
								}
							}
						}
						if (match) {
							mMenuList.add(mMenuList.size(), video);
							mTotalCount = mTotalCount + 1;
							mTotalTime = mTotalTime + video.getDuration();
							mTotalSize = mTotalSize + video.getSize();
						}
					}
				}
			} catch (HibernateException ex) {
				log.error("Getting recordings failed", ex);
			}

			List tivos = Server.getServer().getTiVos();
			Iterator iterator = tivos.iterator();
			while (iterator.hasNext()) {
				TiVo tivo = (TiVo) iterator.next();
				mTotalCapacity = mTotalCapacity + tivo.getCapacity();
			}
			// setFooter("Press 0 for Sort by Date, 1 Group All, 2 Group
			// Suggestions"); // 0 for Sort by Title, 1 List
			// All, 2 List Suggestions
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			mFocus = mMenuList.getFocus();			if (isReturn) {
				createMenu();				mMenuList.setFocus(mFocus, false);
			}
			ToGoConfiguration togoConfiguration = (ToGoConfiguration) ((ToGoFactory) getFactory()).getAppContext()
					.getConfiguration();
			if (togoConfiguration.isShowStats()) {
				long available = (mTotalCapacity * 1024) - (mTotalSize / (1024 * 1024));
				if (available < 0)
					available = 0;
				String value = "";
				value = "Total: " + String.valueOf(mTotalCount);
				SimpleDateFormat timeFormat = new SimpleDateFormat();
				timeFormat.applyPattern("HH:mm");
				int duration = (int) Math.rint(mTotalTime / 1000 / 60.0);
				mCalendar.set(GregorianCalendar.HOUR_OF_DAY, (duration / 60));
				mCalendar.set(GregorianCalendar.MINUTE, duration % 60);
				mCalendar.set(GregorianCalendar.SECOND, 0);
				value = value + "   " + "Length: " + timeFormat.format(mCalendar.getTime());
				DecimalFormat numberFormat = new DecimalFormat("###,###");
				// sizeText.setValue("Size: " + numberFormat.format(totalSize /
				// (1024 * 1024)) + " MB");
				value = value + "   " + "Size: " + numberFormat.format(mTotalSize / (1024 * 1024)) + " MB";
				value = value + "   " + "Available: " + numberFormat.format(available) + " MB";

				mCountText.setValue(value);
				mCountText.setVisible(true);
			} else
				mCountText.setVisible(false);
			return super.handleEnter(arg, isReturn);
		}

		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();
					ToGoScreen togoScreen = new ToGoScreen((ToGo) getBApp());
					togoScreen.setList(mMenuList);
					getBApp().push(togoScreen, TRANSITION_LEFT);
					return true;
				}
			}
			return super.handleAction(view, action);
		}

		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 10, 3, 30, 30);
			Video video = (Video) mMenuList.get(index);
			if (video.getStatus() == Video.STATUS_DOWNLOADED) {
				icon.setResource(mIcon);
			} else if (video.getIcon() != null) {
				if (video.getIcon().equals("in-progress-recording"))
					icon.setResource(mRedIcon);
				else if (video.getIcon().equals("expires-soon-recording"))
					icon.setResource(mYellowIcon);
				else if (video.getIcon().equals("expired-recording"))
					icon.setResource(mYellowExclamationIcon);
				else if (video.getIcon().equals("save-until-i-delete-recording"))
					icon.setResource(mGreenIcon);
				else
					icon.setResource(mEmptyIcon);
			}

			BText name = new BText(parent, 50, 4, parent.getWidth() - 40, parent.getHeight() - 4);
			name.setShadow(true);
			if (video.getStatus() == Video.STATUS_DOWNLOADING || video.getStatus() == Video.STATUS_DOWNLOADED)
				name.setFont("default-24-italic.font");
			else
				name.setFont("default-24.font");
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(video.getTitle(), 29));

			mCalendar.setTime(video.getDateRecorded());
			mCalendar.set(GregorianCalendar.MINUTE, (mCalendar.get(GregorianCalendar.MINUTE) * 60
					+ mCalendar.get(GregorianCalendar.SECOND) + 30) / 60);
			mCalendar.set(GregorianCalendar.SECOND, 0);

			BText date = new BText(parent, parent.getWidth() - 120 - parent.getHeight(), 4, 120, parent.getHeight() - 4);
			date.setShadow(true);
			date.setFlags(RSRC_HALIGN_RIGHT);
			date.setValue(mDateFormat.format(mCalendar.getTime()));
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_ENTER:
				getBApp().push(new OptionsScreen((ToGo) getBApp()), TRANSITION_LEFT);
			}

			return super.handleKeyPress(code, rawcode);
		}

		protected SimpleDateFormat mDateFormat;

		protected GregorianCalendar mCalendar;

		private BText mCountText;

		private int mTotalCount = 0;

		private int mTotalTime = 0;

		private long mTotalSize = 0;

		private int mTotalCapacity = 0;

		private String mSort;
	}

	public class ToGoScreen extends DefaultScreen {
		protected SimpleDateFormat mDateFormat;

		protected SimpleDateFormat mTimeFormat;

		protected GregorianCalendar mCalendar;

		protected DecimalFormat mNumberFormat;

		private BList list;

		public ToGoScreen(ToGo app) {
			super(app, "Program", true);

			getBelow().setResource(mInfoBackground);

			mDateFormat = new SimpleDateFormat();
			mDateFormat.applyPattern("EEE M/d hh:mm a");
			mTimeFormat = new SimpleDateFormat();
			mTimeFormat.applyPattern("H:mm");
			mCalendar = new GregorianCalendar();
			mNumberFormat = new DecimalFormat("###,###");

			int start = TOP;

			int location = 40;
			icon = new BView(getNormal(), BORDER_LEFT, start + 8, 30, 30);

			titleText = new BText(getNormal(), BORDER_LEFT + location, start, BODY_WIDTH - 40, 40);
			titleText.setFlags(RSRC_HALIGN_LEFT | RSRC_VALIGN_TOP);
			// titleText.setFont("default-36.font");
			titleText.setFont("system-30.font");
			titleText.setShadow(true);

			start += 45;

			descriptionText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 90);
			descriptionText.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
			descriptionText.setFont("default-18-bold.font");
			descriptionText.setShadow(true);

			start += 85;

			dateText = new LabelText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20, true);
			dateText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
			dateText.setFont("default-18-bold.font");
			dateText.setShadow(true);

			durationText = new LabelText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20, true);
			durationText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			durationText.setFont("default-18-bold.font");
			durationText.setShadow(true);

			start += 20;

			ratingText = new LabelText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20, true);
			ratingText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
			ratingText.setFont("default-18-bold.font");
			ratingText.setShadow(true);

			videoText = new LabelText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20, true);
			videoText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			videoText.setFont("default-18-bold.font");
			videoText.setShadow(true);

			start += 20;

			genreText = new LabelText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20, true);
			genreText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
			genreText.setFont("default-18-bold.font");
			genreText.setShadow(true);

			sizeText = new LabelText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 20, true);
			sizeText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			sizeText.setFont("default-18-bold.font");
			sizeText.setShadow(true);

			start += 30;

			statusText = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
			statusText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
			statusText.setFont("default-24-bolditalic.font");
			// statusText.setColor(new Color(150, 100, 100));
			statusText.setColor(Color.ORANGE);
			statusText.setShadow(true);

			start += 35;

			statusBarBg = new BView(getNormal(), getWidth() - SAFE_TITLE_H - BODY_WIDTH / 3, start, BODY_WIDTH / 3, 30);
			// statusBarBg.setResource(Color.WHITE);
			statusBarBg.setResource(Color.BLACK);
			statusBarBg.setTransparency(.5f);
			statusBarBg.setVisible(false);
			statusBar = new BView(getNormal(), getWidth() - SAFE_TITLE_H - BODY_WIDTH / 3 + 2, start + 2,
					BODY_WIDTH / 3 - 4, 30 - 4);
			statusBar.setResource(Color.GREEN);
			statusBar.setVisible(false);
			/*
			 * speedText = new BText(getNormal(), getWidth() - SAFE_TITLE_H -
			 * text_getWidth()/3, start , text_width/3, 30);
			 * speedText.setFlags(IHmeProtocol.RSRC_HALIGN_CENTER |
			 * IHmeProtocol.RSRC_VALIGN_CENTER);
			 * speedText.setFont("default-18-bold.font");
			 * speedText.setColor(Color.BLACK);
			 * speedText.setShadow(Color.DARK_GRAY,1); speedText.setValue(" ");
			 * speedText.setVisible(false);
			 */

			mLock = new BView(this, SAFE_TITLE_H, SAFE_TITLE_V, 32, 32);
			mLock.setResource(mLockIcon);
			mLock.setVisible(false);

			list = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H, (getHeight() - SAFE_TITLE_V) - 80,
					(getWidth() - (SAFE_TITLE_H * 2)) / 2, 90, 35);
			list.add("Save to computer");
			list.add("Don't do anything");

			setFocusDefault(list);
		}

		private void updateText() {
			try {
				setPainting(false);
				int location = 40;
				Video video = getVideo();
				if (icon.getResource() != null)
					icon.setResource(mEmptyIcon);
				if (video.getIcon() != null) {
					if (video.getIcon().equals("in-progress-recording"))
						icon.setResource(mRedIcon);
					else if (video.getIcon().equals("expires-soon-recording"))
						icon.setResource(mYellowIcon);
					else if (video.getIcon().equals("expired-recording"))
						icon.setResource(mYellowExclamationIcon);
					else if (video.getIcon().equals("save-until-i-delete-recording"))
						icon.setResource(mGreenIcon);
					else {
						icon.setResource(mEmptyIcon);
						location = 0;
					}
				} else
					location = 0;
				titleText.setLocation(BORDER_LEFT + location, TOP);

				titleText.setValue(video.getTitle() == null ? "" : Tools.trim(video.getTitle(), 28));

				mCalendar.setTime(video.getOriginalAirDate() == null ? new Date() : video.getOriginalAirDate());
				mCalendar.set(GregorianCalendar.MINUTE, (mCalendar.get(GregorianCalendar.MINUTE) * 60
						+ mCalendar.get(GregorianCalendar.SECOND) + 30) / 60);
				mCalendar.set(GregorianCalendar.SECOND, 0);

				String description = null;
				if (video.getEpisodeTitle() != null && video.getEpisodeTitle().length() != 0) {
					description = video.getEpisodeTitle() + " (" + mCalendar.get(Calendar.YEAR) + ")";
					if (video.getDescription() != null)
						description = description + " " + video.getDescription();
				} else {
					if (video.getDescription() == null)
						description = "(" + mCalendar.get(Calendar.YEAR) + ")";
					else
						description = video.getDescription() + " (" + mCalendar.get(Calendar.YEAR) + ") ";
				}

				descriptionText.setValue(description);

				mCalendar.setTime(video.getDateRecorded() == null ? new Date() : video.getDateRecorded());
				mCalendar.set(GregorianCalendar.MINUTE, (mCalendar.get(GregorianCalendar.MINUTE) * 60
						+ mCalendar.get(GregorianCalendar.SECOND) + 30) / 60);
				mCalendar.set(GregorianCalendar.SECOND, 0);

				dateText.setLabel("Date:");
				String channel = String.valueOf(video.getChannelMajorNumber());
				if (channel.equals("0") && video.getChannel() != null)
					channel = video.getChannel();
				String value = mDateFormat.format(mCalendar.getTime());
				if (!channel.equals("0"))
					value = value + " - " + channel;
				if (video.getStation() != null)
					value = value + " " + video.getStation();
				dateText.setValue(value);

				int duration = (int) Math.rint(video.getDuration() / 1000 / 60.0);
				mCalendar.set(GregorianCalendar.HOUR_OF_DAY, (duration / 60));
				mCalendar.set(GregorianCalendar.MINUTE, duration % 60);
				mCalendar.set(GregorianCalendar.SECOND, 0);

				durationText.setLabel("Duration:");
				durationText.setValue(mTimeFormat.format(mCalendar.getTime()));

				ratingText.setLabel("Rated:");
				ratingText.setValue((video.getRating() == null ? "N/A" : video.getRating()));

				videoText.setLabel("Video:");
				String txt = video.getRecordingQuality();
				if (txt == null || txt.length() == 0 || txt.equalsIgnoreCase("good"))
					txt = "Basic";
				videoText.setValue(txt);

				genreText.setLabel("Genre:");
				if (video.getProgramGenre() != null && video.getProgramGenre().length() > 0)
					value = Tools.trim(video.getProgramGenre(), 40);
				else
					value = "Unknown";
				genreText.setValue(value);

				sizeText.setLabel("Size:");
				sizeText.setValue(mNumberFormat.format(video.getSize() / (1024 * 1024)) + " MB");

				if (video.getParentalControls() != null && video.getParentalControls().booleanValue())
					mLock.setVisible(true);
				else
					mLock.setVisible(false);

				statusText.setValue(video.getStatusString());

				if (video.getStatus() == Video.STATUS_DOWNLOADING || video.getStatus() == Video.STATUS_DOWNLOADED) {
					statusBarBg.setVisible(true);
					statusBar.setVisible(true);
					statusBar.setSize(1, statusBar.getHeight());
					// speedText.setVisible(true);

					if (video.getDownloadTime() > 0) {
						long rate = (video.getDownloadSize() / 1024) / video.getDownloadTime();
						statusText.setValue(video.getStatusString() + ": " + rate + " KB/Sec");
						// speedText.setValue(rate+" KB/Sec");
						if (video.getStatus() == Video.STATUS_DOWNLOADED) {
							statusBar.setSize(statusBarBg.getWidth() - 4, statusBar.getHeight());
						} else {
							float barFraction = video.getDownloadSize() / (float) video.getSize();
							if ((statusBarBg.getWidth() - 4) * barFraction < 1)
								statusBar.setSize(1, statusBar.getHeight());
							else {
								int size = (int) (barFraction * (statusBarBg.getWidth() - 4));
								if (size < 1)
									size = 1;
								statusBar.setSize(size, statusBar.getHeight());
							}
						}
					} else {
						String progress = "";
						for (int i = 0; i < mCounter; i++)
							progress = progress + ".";
						statusText.setValue("Connecting" + progress);
						// speedText.setValue("0 KB/Sec");
						statusBar.setVisible(false);
						mCounter++;
					}
				} else {
					statusBarBg.setVisible(false);
					statusBar.setVisible(false);
					// speedText.setVisible(false);
				}

				Boolean status = new Boolean(video.getStatus() == Video.STATUS_RULE_MATCHED
						|| video.getStatus() == Video.STATUS_USER_SELECTED
						|| video.getStatus() == Video.STATUS_DOWNLOADING);
				if (status.booleanValue())
					list.set(0, "Don't save to computer");
				else
					list.set(0, "Save to computer");
			} finally {
				setPainting(true);
			}

			if (ToGoScreen.this.getApp().getContext() != null)
				flush();
		}

		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			try {
				setPainting(false);
				if (mUpdateThread != null && mUpdateThread.isAlive())
					mUpdateThread.interrupt();
			} finally {
				setPainting(true);
			}

			mUpdateThread = new Thread() {
				public void run() {
					int counter = 0;
					while (true) {
						try {
							synchronized (this) {
								updateText();
							}

							if (counter++ < 10)
								sleep(1000 * 5);
							else
								sleep(1000 * 10);
						} catch (InterruptedException ex) {
							return;
						} // handle silently for waking up
						catch (Exception ex2) {
							Tools.logException(ToGo.class, ex2);
							return;
						}
					}
				}

				public void interrupt() {
					synchronized (this) {
						super.interrupt();
					}
				}
			};
			mUpdateThread.start();
			return super.handleEnter(arg, isReturn);
		}

		public boolean handleExit() {
			try {
				setPainting(false);
				if (mUpdateThread != null && mUpdateThread.isAlive())
					mUpdateThread.interrupt();
			} finally {
				setPainting(true);
			}
			mUpdateThread = null;
			return super.handleExit();
		}

		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
			case KEY_RIGHT:
				if (list.getFocus() == 0) {
					postEvent(new BEvent.Action(this, "record"));
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
				updateText();
				return true;
			case KEY_CHANNELDOWN:
				getBApp().play("pagedown.snd");
				getBApp().flush();
				getNextPos();
				updateText();
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}

		public void getNextPos() {
			if (mList != null) {
				int pos = mList.getFocus() + 1;
				if (mList.getFocus() == mList.size() - 1)
					pos = 0;
				mList.setFocus(pos, false);
			}
		}

		public void getPrevPos() {
			if (mList != null) {
				int pos = mList.getFocus() - 1;
				if (mList.getFocus() == 0)
					pos = mList.size() - 1;
				mList.setFocus(pos, false);
			}
		}

		public boolean handleAction(BView view, Object action) {
			if (action.equals("record")) {
				Video video = getVideo();
				Boolean status = new Boolean(video.getStatus() == Video.STATUS_RULE_MATCHED
						|| video.getStatus() == Video.STATUS_USER_SELECTED
						|| video.getStatus() == Video.STATUS_DOWNLOADING);
				if (status.booleanValue()) {
					video.setStatus(Video.STATUS_USER_CANCELLED);
					getBApp().play("thumbsdown.snd");
				} else {
					video.setStatus(Video.STATUS_USER_SELECTED);
					getBApp().play("thumbsup.snd");
				}

				try {
					log.debug("video: " + video.toString());
					Server.getServer().updateVideo(video);
					video = VideoManager.retrieveVideo(video.getId());
				} catch (Exception ex) {
					log.error("Video update failed", ex);
				}

				mCounter = 0;

				updateText();

				return true;
			}

			return super.handleAction(view, action);
		}

		public void setList(BList list) {
			mList = list;
		}

		private synchronized Video getVideo() {
			Video video = (Video) mList.get(mList.getFocus());
			try {
				return VideoManager.retrieveVideo(video.getId());
			} catch (Exception ex) {
				log.error("Video retrieve failed", ex);
			}
			return video;
		}

		private BList mList;

		private BText statusText;

		int location = 40;

		private BView icon;

		private BText titleText;

		private BText descriptionText;

		private LabelText dateText;

		private LabelText durationText;

		private LabelText ratingText;

		private LabelText videoText;

		private LabelText genreText;

		private LabelText sizeText;

		private BView statusBarBg;

		private BView statusBar;

		private BView mLock;

		// private BText speedText;
		private Thread mUpdateThread;

		private int mCounter;
	}

	public static class ToGoFactory extends AppFactory {

		public void initialize() {
		}
	}
}