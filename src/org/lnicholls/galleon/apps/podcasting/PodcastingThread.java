package org.lnicholls.galleon.apps.podcasting;

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
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.Podcast;
import org.lnicholls.galleon.database.PodcastManager;
import org.lnicholls.galleon.database.PodcastTrack;
import org.lnicholls.galleon.database.VideocastTrack;
import org.lnicholls.galleon.downloads.Download;
import org.lnicholls.galleon.downloads.StatusEvent;
import org.lnicholls.galleon.downloads.StatusListener;
import org.lnicholls.galleon.media.Mp3File;
import org.lnicholls.galleon.server.Constants;
import org.lnicholls.galleon.server.GoBackConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.lnicholls.galleon.util.ProgressListener;
import org.lnicholls.galleon.util.Tools;

public class PodcastingThread extends Thread implements Constants, ProgressListener, StatusListener {
	private static Logger log = Logger.getLogger(PodcastingThread.class.getName());

	public PodcastingThread(PodcastingConfiguration podcastingConfiguration) {
		super("PodcastingThread");

		mPodcastingConfiguration = podcastingConfiguration;

		setPriority(Thread.MIN_PRIORITY);
	}

	public void run() {
		while (true) {
			try {
				if (!mWaiting) {
					List list = null;
					synchronized (this) {
						try {
							list = PodcastManager.listAll();
						} catch (Exception ex) {
							Tools.logException(PodcastingThread.class, ex);
						}
					}
					boolean downloadNext = false;
					if (list != null && list.size() > 0) {
						for (Iterator i = list.iterator(); i.hasNext(); /* Nothing */) {
							mPodcast = (Podcast) i.next();

							Thread.sleep(100); // give the CPU some breathing
												// time

							if (mPodcast.getStatus() == mPodcast.STATUS_ERROR) {
								Date current = new Date();
								if (current.getTime() - mPodcast.getDateUpdated().getTime() > 1000 * 60 * 60) {
									mPodcast.setStatus(Podcast.STATUS_SUBSCRIBED);
									PodcastManager.updatePodcast(mPodcast);
								}
							} else if (mPodcast.getStatus() == mPodcast.STATUS_SUBSCRIBED) {
								synchronized (this) {
									try {
										mPodcast = PodcastManager.retrievePodcast(mPodcast);
									} catch (Exception ex) {
										Tools.logException(PodcastingThread.class, ex, "Retrieve podcast failed");
									}
								}

								List podcastTracks = mPodcast.getTracks();
								if (podcastTracks != null) {
									PodcastTrack[] tracks = new PodcastTrack[podcastTracks.size()];
									int pos = 0;
									for (Iterator j = podcastTracks.iterator(); j.hasNext(); /* Nothing */) {
										PodcastTrack track = (PodcastTrack) j.next();
										tracks[pos++] = track;
									}

									// Update podcast
									if (Podcasting.getPodcast(mPodcast) != null) {
										synchronized (this) {
											try {
												PodcastManager.updatePodcast(mPodcast);
											} catch (Exception ex) {
												Tools.logException(Podcasting.class, ex);
											}
										}
										// Remove tracks that dont exist anymore
										for (int k = 0; k < tracks.length; k++) {
											boolean found = false;
											for (Iterator l = mPodcast.getTracks().iterator(); l.hasNext(); /* Nothing */) {
												PodcastTrack track = (PodcastTrack) l.next();
												if (tracks[k].getUrl() != null && track.getUrl() != null
														&& tracks[k].getUrl().equals(track.getUrl())) {
													found = true;
													break;
												}
											}

											if (!found) {
												deleteAudio(mPodcast, tracks[k]);
												synchronized (this) {
													try {
														mPodcast.getTracks().remove(tracks[k]);
														PodcastManager.updatePodcast(mPodcast);
													} catch (Exception ex) {
														Tools.logException(Podcasting.class, ex);
													}
												}
											}
										}

										podcastTracks = mPodcast.getTracks();
										tracks = new PodcastTrack[podcastTracks.size()];
										pos = 0;
										for (Iterator j = podcastTracks.iterator(); j.hasNext(); /* Nothing */) {
											PodcastTrack track = (PodcastTrack) j.next();
											tracks[pos++] = track;
										}
									} else {
										mPodcast.setStatus(Podcast.STATUS_ERROR);
										mPodcast.setDateUpdated(new Date());
										PodcastManager.updatePodcast(mPodcast);
										break;
									}

									// Sort the tracks by date
									Arrays.sort(tracks, new Comparator() {
										public int compare(Object o1, Object o2) {
											PodcastTrack track1 = (PodcastTrack) o1;
											PodcastTrack track2 = (PodcastTrack) o2;
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
										if (tracks[j].getStatus() == PodcastTrack.STATUS_QUEUED
												|| tracks[j].getStatus() == PodcastTrack.STATUS_DOWNLOADING) {
											mTrack = tracks[j];
											break;
										}
									}
									if (mTrack == null && mPodcast.getStatus() == Podcast.STATUS_SUBSCRIBED) {
										// Pick the latest that hasnt been
										// downloaded
										int count = 0;
										for (int j = tracks.length - 1; j >= 0; j--) {
											if (tracks[j].getStatus() != PodcastTrack.STATUS_DOWNLOAD_CANCELLED
													&& tracks[j].getStatus() != PodcastTrack.STATUS_DELETED
													&& tracks[j].getStatus() != PodcastTrack.STATUS_DOWNLOADED
													&& tracks[j].getStatus() != PodcastTrack.STATUS_DOWNLOAD_ERROR
													&& tracks[j].getStatus() != PodcastTrack.STATUS_PLAYED) {
												int errors = tracks[j].getErrors()==null ? 0 : tracks[j].getErrors().intValue();
												if (errors < 3 )
													mTrack = tracks[j];
											} else if (tracks[j].getStatus() == PodcastTrack.STATUS_DOWNLOADED
													|| tracks[j].getStatus() == PodcastTrack.STATUS_PLAYED) {
												count++;
											}
										}
										if (mTrack != null) {
											int max = mPodcastingConfiguration.getDownload();
											if (max != -1) {
												if (count >= max) {
													PodcastTrack deleted = null;
													// Remove the oldest track
													// that
													// has already been played
													for (int j = tracks.length - 1; j >= 0; j--) {
														if (tracks[j].getStatus() == PodcastTrack.STATUS_PLAYED) {
															PodcastTrack played = tracks[j];
															try {
																played = mPodcast.getTrack(played.getUrl());
																Audio audio = played.getTrack();
																synchronized (this) {
																	if (audio != null && audio.getDatePlayed() != null) {
																		boolean delete = false;
																		if (mTrack.getPublicationDate() == null) {
																			if (new Date().getTime()
																					- audio.getDatePlayed().getTime() > audio
																					.getDuration()) {
																				delete = true;
																			}
																		} else if (new Date().getTime()
																				- audio.getDatePlayed().getTime() > audio
																				.getDuration()
																				&& mTrack.getPublicationDate().after(
																						played.getPublicationDate())) {
																			delete = true;
																		}

																		if (delete) {
																			deleteAudio(mPodcast, played);
																			deleted = played;
																			break;
																		}
																	}
																}
															} catch (Exception ex) {
																Tools.logException(PodcastingThread.class, ex,
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
										String path = System.getProperty("data") + File.separator + "podcasts"
												+ File.separator + clean(mPodcast.getTitle());
										File dir = new File(path);
										if (!dir.exists()) {
											dir.mkdirs();
										}

										ServerConfiguration serverConfiguration = Server.getServer()
												.getServerConfiguration();

										String extension = ".mp3";
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
											Tools.logException(PodcastingThread.class, ex, mTrack.getUrl());
											mDownloadNext = true;

											synchronized (this) {
												try {
													mTrack.setDownloadSize(0);
													mTrack.setDownloadTime(0);
													mTrack.setTrack(null);
													mTrack.setStatus(PodcastTrack.STATUS_DOWNLOAD_ERROR);
													int errors = mTrack.getErrors()==null ? 0 : mTrack.getErrors().intValue(); 
													mTrack.setErrors(new Integer(errors+1));
													PodcastManager.updatePodcast(mPodcast);
												} catch (Exception ex2) {
													Tools.logException(PodcastingThread.class, ex2);
												}
											}
										}
										break;
									}
								}
							} else {
								PodcastManager.deletePodcast(mPodcast);
							}
						}
					}
				}
				if (!mDownloadNext) {
					synchronized (this) {
						if (mTrack!=null && mTrack.getStatus()==PodcastTrack.STATUS_DOWNLOAD_CANCELLED)
						{
							if (mDownload!=null)
							{
								mDownload.interrupt();
							}
						}
						else
						{
							wait(1000 * 60 * 60);
						}
					}
				}
				mDownloadNext = false;
			} catch (InterruptedException ex) {
			} // handle silently for waking up
			catch (Exception ex2) {
				Tools.logException(PodcastingThread.class, ex2);
				try {
					sleep(1000 * 60);
				} catch (Exception ex) {
				}
			}
		}
	}

	private void deleteAudio(Podcast podcast, PodcastTrack track) {
		if (podcast != null && track != null) {
			Audio audio = track.getTrack();
			if (audio != null) {
				if (audio.getPath() != null) {
					File file = new File(audio.getPath());
					if (file.exists()) {
						file.delete();
						log.info("Removing podcast file: " + file.getAbsolutePath());
					}
					synchronized (this) {
						try {
							track.setDownloadSize(0);
							track.setDownloadTime(0);
							track.setTrack(null);
							track.setStatus(PodcastTrack.STATUS_DELETED);
							PodcastManager.updatePodcast(podcast);
							AudioManager.deleteAudio(audio);
						} catch (Exception ex) {
							Tools.logException(PodcastingThread.class, ex, "Audio delete failed: " + audio.getPath());
						}
					}
				}
			}
		}
	}

	public void statusChanged(StatusEvent se) {
		if (se.getNewStatus() == StatusEvent.STOPPED) {
			synchronized (this) {
				try {
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
				}
			}

			synchronized (this) {
				try {
					mTrack.setStatus(PodcastTrack.STATUS_DOWNLOAD_CANCELLED);
					PodcastManager.updatePodcast(mPodcast);
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (HibernateException ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
				}
			}
			
			mDownloadNext = true;
			mWaiting = false;
			interrupt();
		}
		else		
		if (se.getNewStatus() == StatusEvent.ERROR) {
			synchronized (this) {
				try {
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
				}
			}

			synchronized (this) {
				try {
					mTrack.setStatus(PodcastTrack.STATUS_DOWNLOAD_ERROR);
					int errors = mTrack.getErrors()==null ? 0 : mTrack.getErrors().intValue(); 
					mTrack.setErrors(new Integer(errors+1));
					PodcastManager.updatePodcast(mPodcast);
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (HibernateException ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
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
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
				}
			}

			synchronized (this) {
				try {
					mTrack.setStatus(PodcastTrack.STATUS_DOWNLOADING);
					PodcastManager.updatePodcast(mPodcast);
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (HibernateException ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
				}
			}

			mStart = System.currentTimeMillis();
		} else if (se.getNewStatus() == StatusEvent.COMPLETED) {
			synchronized (this) {
				try {
					mTrack = mPodcast.getTrack(mTrack.getUrl());
				} catch (Exception ex) {
					Tools.logException(PodcastingThread.class, ex, "Track update failed");
				}
			}

			try {
				if (mDownload.getElapsedTime()>0)
					log.info("Download rate=" + mDownload.getSize() / mDownload.getElapsedTime() + " KBps");

				synchronized (this) {
					try {
						Audio audio = null;
						List audios = AudioManager.findByPath(mDownload.getLocalFile().getCanonicalPath());
						if (audios != null && audios.size() > 0) {
							audio = (Audio) audios.get(0);
							if (audio != null) {
								audio.setOrigen("Podcast");
								// if (mTrack.getDuration()!=null)
								// podcast.setDuration(new
								// Integer(mTrack.getDuration().longValue()/1000));
								AudioManager.updateAudio(audio);
							}
						} else {
							try {
								audio = Mp3File.getAudio(mDownload.getLocalFile().getCanonicalPath());
							} catch (Exception ex) {
								Tools.logException(PodcastingThread.class, ex, "Track tags failed");
							}
							if (audio != null) {
								audio.setOrigen("Podcastcast");
								// if (mTrack.getDuration()!=null)
								// podcast.setDuration((int)mTrack.getDuration().longValue()/1000);
								AudioManager.createAudio(audio);
							}
						}
						if (audio != null) {
							mTrack.setSize(mDownload.getLocalFile().length());
							mTrack.setTrack(audio);
							mTrack.setStatus(PodcastTrack.STATUS_DOWNLOADED);
							PodcastManager.updatePodcast(mPodcast);

							ServerConfiguration serverConfiguration = Server.getServer().getServerConfiguration();
							GoBackConfiguration goBackConfiguration = serverConfiguration.getGoBackConfiguration();

							// Conversion
							String path = System.getProperty("data") + File.separator + "podcasts" + File.separator
									+ clean(mPodcast.getTitle());
							File dir = new File(path);
							String name = clean(mTrack.getTitle()) + ".mp3";
							File output = new File(dir.getCanonicalPath() + File.separator + name);
							mDownload.getLocalFile().renameTo(output);
							audio.setPath(output.getCanonicalPath());
							AudioManager.updateAudio(audio);
						}
					} catch (Exception ex) {
						Tools.logException(PodcastingThread.class, ex, "Track update failed");
					}
				}
				mDownloadNext = true;
			} catch (Exception ex) {
				Tools.logException(PodcastingThread.class, ex, mTrack.getUrl());
				mDownloadNext = true;
			} finally {
			}

			mWaiting = false;
			interrupt();
		}
	}

	private String clean(String value) {
		value = value.replaceAll(":", "_");
		value = value.replaceAll("\\\\", "_");
		value = value.replaceAll("/", "_");
		value = value.replaceAll("\"", "_");
		value = value.replaceAll("<", "_");
		value = value.replaceAll(">", "_");
		value = value.replaceAll("=", "_");
		value = value.replaceAll("\\*", "_");
		value = value.replaceAll("\\?", "_");
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

	private PodcastingConfiguration mPodcastingConfiguration;

	private boolean mWaiting;

	private Podcast mPodcast = null;

	private PodcastTrack mTrack = null;

	private boolean mDownloadNext = false;

	private long mStart = 0;

	private long mLast = 0;

	private Download mDownload;
}
