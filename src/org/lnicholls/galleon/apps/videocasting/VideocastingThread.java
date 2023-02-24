package org.lnicholls.galleon.apps.videocasting;

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
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.database.VideoManager;
import org.lnicholls.galleon.database.Videocast;
import org.lnicholls.galleon.database.VideocastManager;
import org.lnicholls.galleon.database.VideocastTrack;
import org.lnicholls.galleon.downloads.Download;
import org.lnicholls.galleon.downloads.StatusEvent;
import org.lnicholls.galleon.downloads.StatusListener;
import org.lnicholls.galleon.media.VideoFile;
import org.lnicholls.galleon.server.Constants;
import org.lnicholls.galleon.server.GoBackConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.lnicholls.galleon.util.ProgressListener;
import org.lnicholls.galleon.util.Tools;

public class VideocastingThread extends Thread implements Constants, ProgressListener, StatusListener {
	private static Logger log = Logger.getLogger(VideocastingThread.class.getName());

	public VideocastingThread(VideocastingConfiguration videocastingConfiguration) {
		super("VideocastingThread");

		mVideocastingConfiguration = videocastingConfiguration;

		setPriority(Thread.MIN_PRIORITY);
	}

	public void run() {
		while (true) {
			try {
				if (!mWaiting) {
					log.debug("videocastingthread run");
					List list = null;
					synchronized (this) {
						try {
							list = VideocastManager.listAll();
						} catch (Exception ex) {
							Tools.logException(VideocastingThread.class, ex);
						}
					}
					if (list != null && list.size() > 0) {
						for (Iterator i = list.iterator(); i.hasNext(); /* Nothing */) {
							mVideocast = (Videocast) i.next();

							Thread.sleep(100); // give the CPU some breathing
												// time

							if (mVideocast.getStatus() == mVideocast.STATUS_ERROR) {
								Date current = new Date();
								if (current.getTime() - mVideocast.getDateUpdated().getTime() > 1000 * 60 * 60) {
									mVideocast.setStatus(Videocast.STATUS_SUBSCRIBED);
									VideocastManager.updateVideocast(mVideocast);
								}
							} else if (mVideocast.getStatus() == mVideocast.STATUS_SUBSCRIBED) {
								synchronized (this) {
									try {
										mVideocast = VideocastManager.retrieveVideocast(mVideocast);
									} catch (Exception ex) {
										Tools.logException(VideocastingThread.class, ex, "Retrieve videocast failed");
									}
								}

								List videocastTracks = mVideocast.getTracks();
								if (videocastTracks != null) {
									VideocastTrack[] tracks = new VideocastTrack[videocastTracks.size()];
									int pos = 0;
									for (Iterator j = videocastTracks.iterator(); j.hasNext(); /* Nothing */) {
										VideocastTrack track = (VideocastTrack) j.next();
										tracks[pos++] = track;
									}

									// Update videocast
									if (Videocasting.getVideocast(mVideocast) != null) {
										synchronized (this) {
											try {
												VideocastManager.updateVideocast(mVideocast);
											} catch (Exception ex) {
												Tools.logException(Videocasting.class, ex);
											}
										}
										// Remove tracks that dont exist anymore
										for (int k = 0; k < tracks.length; k++) {
											boolean found = false;
											for (Iterator l = mVideocast.getTracks().iterator(); l.hasNext(); /* Nothing */) {
												VideocastTrack track = (VideocastTrack) l.next();
												if (tracks[k].getUrl() != null && track.getUrl() != null
														&& tracks[k].getUrl().equals(track.getUrl())) {
													found = true;
													break;
												}
											}

											if (!found) {
												deleteVideo(mVideocast, tracks[k]);
												synchronized (this) {
													try {
														mVideocast.getTracks().remove(tracks[k]);
														VideocastManager.updateVideocast(mVideocast);
													} catch (Exception ex) {
														Tools.logException(Videocasting.class, ex);
													}
												}
											}
										}

										videocastTracks = mVideocast.getTracks();
										tracks = new VideocastTrack[videocastTracks.size()];
										pos = 0;
										for (Iterator j = videocastTracks.iterator(); j.hasNext(); /* Nothing */) {
											VideocastTrack track = (VideocastTrack) j.next();
											tracks[pos++] = track;
										}
									} else {
										mVideocast.setStatus(Videocast.STATUS_ERROR);
										mVideocast.setDateUpdated(new Date());
										VideocastManager.updateVideocast(mVideocast);
										break;
									}

									// Sort the tracks by date
									Arrays.sort(tracks, new Comparator() {
										public int compare(Object o1, Object o2) {
											VideocastTrack track1 = (VideocastTrack) o1;
											VideocastTrack track2 = (VideocastTrack) o2;
											if (track1.getPublicationDate() != null
													&& track2.getPublicationDate() != null)
												return -(int) (track1.getPublicationDate().getTime() - track2
														.getPublicationDate().getTime());
											else
												return 0;
										}
									});

									mTrack = null;

									// Find a track selected by the user
									for (int j = 0; j < tracks.length; j++) {
										if (tracks[j].getStatus() == VideocastTrack.STATUS_QUEUED
												|| tracks[j].getStatus() == VideocastTrack.STATUS_DOWNLOADING) {
											mTrack = tracks[j];
											break;
										}
									}
									if (mTrack == null && mVideocast.getStatus() == Videocast.STATUS_SUBSCRIBED) {
										// Pick the latest that hasnt been
										// downloaded
										int count = 0;
										for (int j = tracks.length - 1; j >= 0; j--) {
											if (tracks[j].getStatus() != VideocastTrack.STATUS_DOWNLOAD_CANCELLED
													&& tracks[j].getStatus() != VideocastTrack.STATUS_DELETED
													&& tracks[j].getStatus() != VideocastTrack.STATUS_DOWNLOADED
													&& tracks[j].getStatus() != VideocastTrack.STATUS_DOWNLOAD_ERROR
													&& tracks[j].getStatus() != VideocastTrack.STATUS_PLAYED) {
												int errors = tracks[j].getErrors()==null ? 0 : tracks[j].getErrors().intValue();
												if (errors < 3)
													mTrack = tracks[j];
											} else if (tracks[j].getStatus() == VideocastTrack.STATUS_DOWNLOADED
													|| tracks[j].getStatus() == VideocastTrack.STATUS_PLAYED) {
												count++;
											}
										}
										if (mTrack != null) {
											int max = mVideocastingConfiguration.getDownload();
											if (max != -1) {
												if (count >= max) {
													VideocastTrack deleted = null;
													// Remove the oldest track
													// that
													// has already been played
													for (int j = tracks.length - 1; j >= 0; j--) {
														if (tracks[j].getStatus() == VideocastTrack.STATUS_PLAYED) {
															VideocastTrack played = tracks[j];
															try {
																played = mVideocast.getTrack(played.getUrl());
																Video video = played.getTrack();
																synchronized (this) {
																	if (video != null && video.getDatePlayed() != null) {
																		boolean delete = false;
																		if (mTrack.getPublicationDate() == null) {
																			if (new Date().getTime()
																					- video.getDatePlayed().getTime() > video
																					.getDuration()) {
																				delete = true;
																			}
																		} else if (new Date().getTime()
																				- video.getDatePlayed().getTime() > video
																				.getDuration()
																				&& mTrack.getPublicationDate().after(
																						played.getPublicationDate())) {
																			delete = true;
																		}

																		if (delete) {
																			deleteVideo(mVideocast, played);
																			deleted = played;
																			break;
																		}
																	}
																}
															} catch (Exception ex) {
																Tools.logException(VideocastingThread.class, ex,
																		"Track update failed");
															}
														}
													}
													if (deleted == null)
														continue;
												}
											}
										}
									}
									if (mTrack != null) {
										String path = System.getProperty("data") + File.separator + "videocasts"
												+ File.separator + clean(mVideocast.getTitle());
										File dir = new File(path);
										if (!dir.exists()) {
											dir.mkdirs();
										}

										String temp = System.getProperty("data") + File.separator + "temp"
												+ File.separator + clean(mVideocast.getTitle());
										File tempDir = new File(temp);
										if (!tempDir.exists()) {
											tempDir.mkdirs();
										}

										ServerConfiguration serverConfiguration = Server.getServer()
												.getServerConfiguration();
										GoBackConfiguration goBackConfiguration = serverConfiguration
												.getGoBackConfiguration();
										if (goBackConfiguration.isConvertVideo())
											path = tempDir.getCanonicalPath();
										else
											path = dir.getCanonicalPath();

										String extension = ".mpg";
										if (mTrack.getUrl().indexOf(".") != -1) {
											extension = mTrack.getUrl().substring(mTrack.getUrl().lastIndexOf("."));
										}
										String name = clean(mTrack.getTitle()) + extension;
										log.info("Downloading: " + mTrack.getTitle() + "(" + mTrack.getUrl() + ")");
										File file = new File(path + File.separator + name);

										try {
											mDownload = new Download(new URL(mTrack.getUrl()), file);

											Server.getServer().addDownload(mDownload, this);
											mDownloadNext = false;
											mWaiting = true;

										} catch (Exception ex) {
											Tools.logException(VideocastingThread.class, ex, mTrack.getUrl());
											mDownloadNext = true;

											synchronized (this) {
												try {
													mTrack.setDownloadSize(0);
													mTrack.setDownloadTime(0);
													mTrack.setTrack(null);
													mTrack.setStatus(VideocastTrack.STATUS_DOWNLOAD_ERROR);
													int errors = mTrack.getErrors()==null ? 0 : mTrack.getErrors().intValue(); 
													mTrack.setErrors(new Integer(errors+1));
													VideocastManager.updateVideocast(mVideocast);
												} catch (Exception ex2) {
													Tools.logException(VideocastingThread.class, ex2);
												}
											}
										}
										break;
									}
								}
							} else {
								VideocastManager.deleteVideocast(mVideocast);
							}
						}
					}
				}
				if (!mDownloadNext) {
					synchronized (this) {
						if (mTrack!=null && mTrack.getStatus()==VideocastTrack.STATUS_DOWNLOAD_CANCELLED)
						{
							if (mDownload!=null)
							{
								mDownload.interrupt();
							}
						}
						else
							wait(1000 * 60 * 60);
					}
				}
				mDownloadNext = false;
			} catch (InterruptedException ex) {
			} // handle silently for waking up
			catch (Exception ex2) {
				Tools.logException(VideocastingThread.class, ex2);
				try {
					sleep(1000 * 60);
				} catch (Exception ex) {
				}
			}
		}
	}

	private void deleteVideo(Videocast videocast, VideocastTrack track) {
		if (videocast != null && track != null) {
			Video video = track.getTrack();
			if (video != null) {
				if (video.getPath() != null) {
					File file = new File(video.getPath());
					if (file.exists()) {
						file.delete();
						log.info("Removing videocast file: " + file.getAbsolutePath());
					}
					synchronized (this) {
						try {
							track.setDownloadSize(0);
							track.setDownloadTime(0);
							track.setTrack(null);
							track.setStatus(VideocastTrack.STATUS_DELETED);
							VideocastManager.updateVideocast(videocast);
							VideoManager.deleteVideo(video);
						} catch (Exception ex) {
							Tools.logException(VideocastingThread.class, ex, "Video delete failed: " + video.getPath());
						}
					}
				}
			}
		}
	}

	private String clean(String value) {
		value = Tools.clean(value);
		value = value.replaceAll(":", "_");
		value = value.replaceAll("\\\\", "_");
		value = value.replaceAll("/", "_");
		value = value.replaceAll("\"", "_");
		value = value.replaceAll("<", "_");
		value = value.replaceAll(">", "_");
		value = value.replaceAll("=", "_");
		value = value.replaceAll("\\*", "_");
		value = value.replaceAll("\\?", "_");
		value = value.replaceAll("'", "");
		value = value.replaceAll(",", "");
		value = value.replaceAll("\\(", "_");
		value = value.replaceAll("\\)", "_");
		value = value.replaceAll("-", "_");
		return value;
	}

	public void progress(String value) {
		if (log.isDebugEnabled())
			log.debug(value);
	}

	public void interrupt() {
		synchronized (this) {
			super.interrupt();
		}
	}

	public void update() {
		synchronized (this) {
			notifyAll();
		}
	}

	public void statusChanged(StatusEvent se) {
		log.debug("statusChanged=" + se);
		if (se.getNewStatus() == StatusEvent.ERROR) {
			synchronized (this) {
				try {
					mTrack = mVideocast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(VideocastingThread.class, ex, "Track update failed");
				}
			}

			synchronized (this) {
				try {
					mTrack.setStatus(VideocastTrack.STATUS_DOWNLOAD_ERROR);
					int errors = mTrack.getErrors()==null ? 0 : mTrack.getErrors().intValue(); 
					mTrack.setErrors(new Integer(errors+1));
					VideocastManager.updateVideocast(mVideocast);
					mTrack = mVideocast.getTrack(mTrack.getUrl());
				} catch (HibernateException ex) {
					Tools.logException(VideocastingThread.class, ex, "Track update failed");
				}
			}
			
			mDownloadNext = true;
			mWaiting = false;
			interrupt();
		}
		else
		if (se.getNewStatus() == StatusEvent.IN_PROGRESS) {
			synchronized (this) {
				try {
					mTrack = mVideocast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(VideocastingThread.class, ex, "Track update failed");
				}
			}

			synchronized (this) {
				try {
					mTrack.setStatus(VideocastTrack.STATUS_DOWNLOADING);
					VideocastManager.updateVideocast(mVideocast);
					mTrack = mVideocast.getTrack(mTrack.getUrl());
				} catch (HibernateException ex) {
					Tools.logException(VideocastingThread.class, ex, "Track update failed");
				}
			}

			mStart = System.currentTimeMillis();
		} else if (se.getNewStatus() == StatusEvent.COMPLETED) {
			synchronized (this) {
				try {
					mTrack = mVideocast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(VideocastingThread.class, ex, "Track update failed");
				}
			}

			try {
				if (mDownload.getElapsedTime()>0)
					log.info("Download rate=" + mDownload.getSize() / mDownload.getElapsedTime() + " KBps");

				synchronized (this) {
					try {
						Video video = null;
						List videos = VideoManager.findByPath(mDownload.getLocalFile().getCanonicalPath());
						if (videos != null && videos.size() > 0) {
							video = (Video) videos.get(0);
							if (video != null) {
								video.setOrigen("Videocast");
								if (mTrack.getDuration() != null)
									video.setDuration((int) mTrack.getDuration().longValue() / 1000);
								VideoManager.updateVideo(video);
							}
						} else {
							try {
								video = VideoFile.getVideo(mDownload.getLocalFile().getCanonicalPath());
							} catch (Exception ex) {
								Tools.logException(VideocastingThread.class, ex, "Track tags failed");
							}
							if (video != null) {
								video.setOrigen("Videocast");
								if (mTrack.getDuration() != null)
									video.setDuration((int) mTrack.getDuration().longValue() / 1000);
								VideoManager.createVideo(video);
							}
						}
						if (video != null) {
							mTrack.setSize(mDownload.getLocalFile().length());
							mTrack.setTrack(video);
							mTrack.setStatus(VideocastTrack.STATUS_DOWNLOADED);
							VideocastManager.updateVideocast(mVideocast);

							boolean correctFormat = VideoFile.isTiVoFormat(video);

							ServerConfiguration serverConfiguration = Server.getServer().getServerConfiguration();
							GoBackConfiguration goBackConfiguration = serverConfiguration.getGoBackConfiguration();

							// Conversion
							String path = System.getProperty("data") + File.separator + "videocasts" + File.separator
									+ clean(mVideocast.getTitle());
							File dir = new File(path);
							if (correctFormat) {
								String name = clean(mTrack.getTitle()) + ".mpg";
								File output = new File(dir.getCanonicalPath() + File.separator + name);
								mDownload.getLocalFile().renameTo(output);
								video.setPath(output.getCanonicalPath());
								VideoManager.updateVideo(video);
							} else if (goBackConfiguration.isConvertVideo()) {
								String name = clean(mTrack.getTitle()) + ".mpg";
								File output = new File(dir.getCanonicalPath() + File.separator + name);
								if (VideoFile.convert(video, output.getCanonicalPath())) {
									Video converted = VideoFile.getVideo(output.getCanonicalPath());
									if (converted != null && output.length() > 0) {
										converted.setOrigen("Videocast");
										VideoManager.createVideo(converted);
										mTrack.setTrack(converted);
										VideocastManager.updateVideocast(mVideocast);

										VideoManager.deleteVideo(video);
										File file = new File(video.getPath());
										if (file.exists())
											file.delete();
										
										File data = new File(System.getProperty("data") + File.separator + "temp");
										if (data.exists() && data.isDirectory()) {
											File[] files = data.listFiles();
											for (int i=0;i<files.length;i++)
												files[i].delete();
										}
									}
								}
							}
						}
					} catch (Exception ex) {
						Tools.logException(VideocastingThread.class, ex, "Track update failed");
					}
				}
				mDownloadNext = true;
			} catch (Exception ex) {
				Tools.logException(VideocastingThread.class, ex, mTrack.getUrl());
				mDownloadNext = true;
			} finally {
			}

			mWaiting = false;
			interrupt();
		}
	}

	private VideocastingConfiguration mVideocastingConfiguration;

	private boolean mWaiting;

	private Videocast mVideocast = null;

	private VideocastTrack mTrack = null;

	private boolean mDownloadNext = false;

	private long mStart = 0;

	private long mLast = 0;

	private Download mDownload;
}