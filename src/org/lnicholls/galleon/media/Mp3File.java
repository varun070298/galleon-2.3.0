package org.lnicholls.galleon.media;

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

import helliker.id3.MP3File;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.lob.BlobImpl;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.database.Thumbnail;
import org.lnicholls.galleon.database.ThumbnailManager;
import org.lnicholls.galleon.util.Amazon;
import org.lnicholls.galleon.util.Tools;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.TimedCallable;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public final class Mp3File {
	private static final Logger log = Logger.getLogger(Mp3File.class.getName());

	private static final String[] genres = { "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
			"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno",
			"Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
			"Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
			"Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
			"Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
			"Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap",
			"Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes",
			"Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll",
			"Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival",
			"Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock",
			"Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
			"Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Brass", "Primus", "Porn Groove",
			"Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhytmic Soul",
			"Freestyle", "Duet", "Punk Rock", "Drum Solo", "Acapella", "Euro-House", "Dance Hall", "Goa",
			"Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat",
			"Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary C", "Christian Rock",
			"Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop" };

	// ID3v2 Frames
	private static final String ACCOMPANIMENT = "TPE2";

	private static final String ALBUM = "TALB";

	private static final String ALBUM_SORT_ORDER = "TSOA";

	private static final String BPM = "TBPM";

	private static final String COMMERCIAL_INFO_URL = "WCOM";

	private static final String COMPOSER = "TCOM";

	private static final String CONDUCTOR = "TPE3";

	private static final String CONTENT_GROUP = "TIT1";

	private static final String CONTENT_TYPE = "TCON";

	private static final String COPYRIGHT_INFO_URL = "WCOP";

	private static final String COPYRIGHT_MESSAGE = "TCOP";

	private static final String ENCODED_BY = "TENC";

	private static final String ENCODING_TIME = "TDEN";

	private static final String FILE_OWNER = "TOWN";

	private static final String FILE_TYPE = "TFLT";

	private static final String INITIAL_KEY = "TKEY";

	private static final String INTERNET_RADIO_STATION_NAME = "TRSN";

	private static final String INTERNET_RADIO_STATION_OWNER = "TRSO";

	private static final String INVOLVED_PEOPLE = "TIPL";

	private static final String ISRC = "TSRC";

	private static final String LANGUAGE = "TLAN";

	private static final String LEAD_PERFORMERS = "TPE1"; // author

	private static final String LENGTH = "TLEN";

	private static final String LYRICIST = "TEXT";

	private static final String MEDIA_TYPE = "TMED";

	private static final String MOOD = "TMOO";

	private static final String MUSICIAN_CREDITS = "TMCL";

	private static final String ORIGINAL_ALBUM = "TOAL";

	private static final String ORIGINAL_ARTIST = "TOPE";

	private static final String ORIGINAL_FILENAME = "TOFN";

	private static final String ORIGINAL_LYRICIST = "TOLY";

	private static final String ORIGINAL_RELEASE_TIME = "TDOR";

	private static final String PART_OF_SET = "TPOS";

	private static final String PERFORMER_SORT_ORDER = "TSOP";

	private static final String PLAYLIST_DELAY = "TDLY";

	private static final String PRODUCED_NOTICE = "TPRO";

	private static final String PUBLISHER = "TPUB";

	private static final String RECORDING_TIME = "TDRC";

	private static final String RELEASE_TIME = "TDRL";

	private static final String REMIXED_BY = "TPE4";

	private static final String SET_SUBTITLE = "TSST";

	private static final String SOFTWARE_HARDWARE_SETTINGS = "TSSE";

	private static final String SUBTITLE = "TIT3";

	private static final String TAGGING_TIME = "TDTG";

	private static final String TITLE = "TIT2";

	private static final String TITLE_SORT_ORDER = "TSOT";

	private static final String TRACK_NUMBER = "TRCK";

	private static final String USER_DEFINED_TEXT_INFO = "TXXX";

	private static final String YEAR = "TYER";

	private static final String OFFICIAL_FILE_WEBPAGE_URL = "WOAF";

	private static final String OFFICIAL_ARTIST_WEBPAGE_URL = "WOAR";

	private static final String OFFICIAL_SOURCE_WEBPAGE_URL = "WOAS";

	private static final String OFFICIAL_INTERNET_RADIO_WEBPAGE_URL = "WOAS";

	private static final String PAYMENT_URL = "WPAY";

	private static final String OFFICIAL_PUBLISHER_WEBPAGE_URL = "WPUB";

	private static final String USER_DEFINED_URL = "WXXX";

	private static final String AUDIO_ENCRYPTION = "AENC";

	private static final String ATTACHED_PICTURE = "APIC";

	private static final String AUDIO_SEEK_POINT_INDEX = "ASPI";

	private static final String COMMENTS = "COMM";

	private static final String COMMERCIAL_FRAME = "COMR";

	private static final String ENCRYPTION_METHOD_REGISTRATION = "ENCR";

	private static final String EQUALISATION = "EQU2";

	private static final String EVENT_TIMING_CODES = "ETCO";

	private static final String GENERAL_ENCAPSULATED_OBJECT = "GEOB";

	private static final String GROUP_IDENTIFICATION_REGISTRATION = "GRID";

	private static final String LINKED_INFORMATION = "LINK";

	private static final String MUSIC_CD_IDENTIFIER = "MCDI";

	private static final String MPEG_LOCATION_LOOKUP_TABLE = "MLLT";

	private static final String OWNERSHIP_FRAME = "OWNE";

	private static final String PRIVATE_FRAME = "PRIV";

	private static final String PLAY_COUNTER = "PCNT";

	private static final String POPULARIMETER = "POPM";

	private static final String POSITION_SYNCHRONISATION_FRAME = "POSS";

	private static final String RECOMMENDED_BUFFER_SIZE = "RBUF";

	private static final String RELATIVE_VOLUME_ADJUSTMENT = "RVA2";

	private static final String REVERB = "RVRB";

	private static final String SEEK_FRAME = "SEEK";

	private static final String SIGNATURE_FRAME = "SIGN";

	private static final String SYNCHRONISED_LYRIC = "SYLT";

	private static final String SYNCHRONISED_TEMPO_CODES = "SYTC";

	private static final String UNIQUE_FILE_IDENTIFIER = "UFID";

	private static final String TERMS_OF_USE = "USER";

	private static final String UNSYNCHRONISED_LYRIC_TRANSCRIPTION = "USLT";

	private static final String DEFAULT_TITLE = "unknown";

	public static final String DEFAULT_ARTIST = "unknown";

	public static final String DEFAULT_ALBUM = "unknown";

	private static final String DEFAULT_GENRE = "Other";

	private static final long DEFAULT_SIZE = 0;

	private static final long DEFAULT_DURATION = 0;

	private static final int DEFAULT_DATE = 0;

	private static final int DEFAULT_TRACK = 0;

	private static final int DEFAULT_BIT_RATE = 128;

	private static final int DEFAULT_SAMPLE_RATE = 44100;

	private static final String DEFAULT_COMMENTS = "";

	private static final String DEFAULT_LYRICS = "";

	private static final int DEFAULT_CHANNELS = 1;

	private static final Boolean DEFAULT_VBR = Boolean.FALSE;

	public static final String DEFAULT_MIME_TYPE = "audio/mpeg";

	private static final int DEFAULT_TYPE = 0;

	private static final String DEFAULT_PATH = "";

	private static final int DEFAULT_PLAY_COUNT = 0;

	private static final int DEFAULT_RATING = 0;

	private static final String DEFAULT_TONE = "";

	public static final String PC = "PC";

	public static final String getGenre(int number) {
		if ((number < 0) || (number >= genres.length))
			return DEFAULT_GENRE;
		else
			return genres[number];
	}

	public static final String getGenre(String genre) {
		if (genre == null)
			return DEFAULT_GENRE;
		for (int i = 0; i < genres.length; i++) {
			if (genres[i].equals(genre))
				return genre;
		}
		return DEFAULT_GENRE;
	}

	public static final Audio getAudio(String filename) {
		Audio audio = new Audio();
		defaultProperties(audio);
		return getAudio(audio, filename);
	}

	public static final Audio getAudio(Audio audio, String filename) {
		File file = new File(filename);
		try {
			audio.setPath(file.getCanonicalPath());
		} catch (Exception ex) {
			Tools.logException(Mp3File.class, ex, filename);
		}

		audio.setSize(file.length());
		audio.setDateModified(new Date(file.lastModified()));
		try {
			firstPass(file, audio);
		} catch (Exception ex) {
			Tools.logException(Mp3File.class, ex, filename);
		}

		try {
			secondPass(file, audio);
		} catch (Throwable ex) {
			Tools.logException(Mp3File.class, ex, filename);
		}

		try {
			thirdPass(file, audio);
		} catch (Throwable ex) {
			Tools.logException(Mp3File.class, ex, filename);
		}

		if (audio.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			Pattern pattern = Pattern.compile("(.*) - (.*)");
			Matcher matcher = pattern.matcher(value);
			if (matcher != null && matcher.matches()) {
				audio.setArtist(matcher.group(1));
				audio.setTitle(matcher.group(2));
			} else
				audio.setTitle(value);
		}

		return audio;
	}

	// Utility class used to extract meta data within a specified time limit.
	private static final class TimedThread1 implements Callable {
		private File file;

		public TimedThread1(File file) {
			this.file = file;
		}

		public synchronized Object call() throws Exception {
			pgbennett.id3.MP3File mp3File = new pgbennett.id3.MP3File();
			mp3File.init(file, pgbennett.id3.MP3File.EXISTING_TAGS_ONLY);
			return mp3File;
		}
	}

	private static final void firstPass(File file, Audio audio) throws Exception {
		TimedCallable timedCallable = new TimedCallable(new TimedThread1(file), 1000 * 5);
		pgbennett.id3.MP3File mp3File = (pgbennett.id3.MP3File) timedCallable.call();
		timedCallable = null;
		if (mp3File!=null && mp3File.isMP3()) {
			/*
			 * if (properties.containsKey("mp3.id3tag.v2")) { try { InputStream
			 * frames = (InputStream) properties.get("mp3.id3tag.v2");
			 * parseID3v2Frames(frames, audio); } catch (Exception e1) { } }
			 */

			audio.setSampleRate(mp3File.getSampleRate());
			audio.setBitRate(mp3File.getBitRate());
			if (mp3File.getTitle() != null && mp3File.getTitle().trim().length() != 0
					&& !mp3File.getTitle().equals(DEFAULT_TITLE))
				audio.setTitle(mp3File.getTitle());
			if (mp3File.getArtist() != null && mp3File.getArtist().trim().length() != 0
					&& !mp3File.getArtist().equals(DEFAULT_ARTIST))
				audio.setArtist(mp3File.getArtist());
			if (mp3File.getAlbum() != null && mp3File.getAlbum().trim().length() != 0
					&& !mp3File.getAlbum().equals(DEFAULT_ALBUM))
				audio.setAlbum(mp3File.getAlbum());
			try {
				int date = Integer.parseInt(mp3File.getYear());
				if (date != DEFAULT_DATE)
					audio.setDate(date);
			} catch (NumberFormatException e1) {
			}
			if (mp3File.getPlayingTime() != DEFAULT_DURATION)
				audio.setDuration(mp3File.getPlayingTime());
			if (mp3File.getGenre() != null && mp3File.getGenre().trim().length() != 0
					&& !mp3File.getGenre().equals(DEFAULT_GENRE)) {
				String value = mp3File.getGenre();
				Pattern pattern = Pattern.compile("\\(([0-9]*)\\).*");
				Matcher matcher = pattern.matcher(value);
				if (matcher.matches()) {
					try {
						value = getGenre(Integer.parseInt(matcher.group(1)));
						if (!value.equals(DEFAULT_GENRE))
							audio.setGenre(value);
					} catch (NumberFormatException e1) {
					}
				} else if (!value.equals(DEFAULT_GENRE))
					audio.setGenre(value);
			}
			if (mp3File.getTrack() != DEFAULT_TRACK)
				audio.setTrack(mp3File.getTrack());
			try {
				if (mp3File.getComment() != null && mp3File.getComment().trim().length() != 0
						&& !mp3File.getComment().equals(DEFAULT_COMMENTS))
					audio.setComments(mp3File.getComment());
			} catch (Exception e1) {
			}
			String channel = mp3File.getMPEGChannelMode();
			if (channel.equals("stereo"))
				if (1 != DEFAULT_CHANNELS)
					audio.setChannels(1);
			try {
				Boolean vbr = new Boolean(mp3File.isVBR());
				if (vbr != DEFAULT_VBR)
					audio.setVbr(vbr);
			} catch (Exception e1) {
			}

			pgbennett.id3.ID3v2Tag tag = mp3File.getID3v2Tag(); // jampal
			if (tag != null) {
				pgbennett.id3.ID3v2Frames frames = tag.getFrames();
				String id = pgbennett.id3.ID3v2Frames.ATTACHED_PICTURE;
				pgbennett.id3.ID3v2Frame frame = (pgbennett.id3.ID3v2Frame) frames
						.get(pgbennett.id3.ID3v2Frames.ATTACHED_PICTURE);
				if (frame == null) {
					Iterator iterator = frames.keySet().iterator();
					while (iterator.hasNext()) {
						String key = (String) iterator.next();
						if (key.startsWith(pgbennett.id3.ID3v2Frames.ATTACHED_PICTURE)) {
							id = key;
							break;
						}
					}
					frame = (pgbennett.id3.ID3v2Frame) frames.get(id);
				}
				if (frame != null) {
					pgbennett.id3.ID3v2Picture pic = frame.getPicture();

					ByteArrayInputStream input = new ByteArrayInputStream(pic.pictureData);
					createCover(input, audio, pic.mimeType);
				}
			}
		}
	}

	// Utility class used to extract meta data within a specified time limit.
	private static final class TimedThread2 implements Callable {
		private File file;

		public TimedThread2(File file) {
			this.file = file;
		}

		public synchronized Object call() throws Exception {
			return AudioSystem.getAudioFileFormat(file);
		}
	}

	private static final void secondPass(File file, Audio audio) throws Exception {
		TimedCallable timedCallable = new TimedCallable(new TimedThread2(file), 1000 * 5);
		AudioFileFormat audioFormat = (AudioFileFormat) timedCallable.call();
		timedCallable = null;
		if (audioFormat!=null && audioFormat.getType().toString().equalsIgnoreCase("mp3") && (audioFormat instanceof TAudioFileFormat)) {
			Map properties = ((TAudioFileFormat) audioFormat).properties();
			/*
			 * Set keys = properties.keySet(); Iterator iterator =
			 * keys.iterator(); while (iterator.hasNext()) { String key =
			 * (String)iterator.next();
			 * System.out.println(key+"="+properties.get(key)); }
			 */

			if (properties.containsKey("mp3.id3tag.v2")) {
				try {
					InputStream frames = (InputStream) properties.get("mp3.id3tag.v2");
					parseID3v2Frames(frames, audio);
				} catch (Exception ex) {
					Tools.logException(Mp3File.class, ex, "Cannot parse ID3v2: " + audio.getPath());
				}
			}

			if (properties.containsKey("mp3.frequency.hz")) {
				int sampleRate = ((Integer) properties.get("mp3.frequency.hz")).intValue();
				if (sampleRate != DEFAULT_SAMPLE_RATE)
					audio.setSampleRate(sampleRate);
			}
			if (properties.containsKey("mp3.bitrate.nominal.bps")) {
				int bitRate = (int) Math
						.round(((Integer) properties.get("mp3.bitrate.nominal.bps")).intValue() / 1000.0);
				if (bitRate != DEFAULT_BIT_RATE)
					audio.setBitRate(bitRate);
			}
			if (properties.containsKey("title")) {
				String title = (String) properties.get("title");
				if (title != null && title.trim().length() != 0 && !title.equals(DEFAULT_TITLE))
					audio.setTitle(title);
			}
			if (properties.containsKey("author")) {
				String artist = (String) properties.get("author");
				if (artist != null && artist.trim().length() != 0 && !artist.equals(DEFAULT_ARTIST))
					audio.setArtist(artist);
			}
			if (properties.containsKey("album")) {
				String album = (String) properties.get("album");
				if (album != null && album.trim().length() != 0 && !album.equals(DEFAULT_ALBUM))
					audio.setAlbum(album);
			}
			if (properties.containsKey("date")) {
				try {
					int date = Integer.parseInt((String) properties.get("date"));
					if (date != DEFAULT_DATE)
						audio.setDate(date);
				} catch (NumberFormatException e1) {
				}
			}
			if (properties.containsKey("duration")) {
				long duration = (long) Math.round((((Long) properties.get("duration")).longValue()) / 1000000.0);
				if (duration != DEFAULT_DURATION)
					audio.setDuration(duration);
			}
			if (properties.containsKey("mp3.id3tag.genre")) {
				String genre = (String) properties.get("mp3.id3tag.genre");
				if (genre != null && genre.trim().length() != 0) {
					Pattern pattern = Pattern.compile("\\(([0-9]*)\\).*");
					Matcher matcher = pattern.matcher(genre);
					if (matcher.matches()) {
						try {
							genre = getGenre(Integer.parseInt(matcher.group(1)));
							if (!genre.equals(DEFAULT_GENRE))
								audio.setGenre(genre);
						} catch (NumberFormatException e1) {
						}
					} else if (!genre.equals(DEFAULT_GENRE))
						audio.setGenre(genre);
				}
			}
			if (properties.containsKey("mp3.id3tag.track")) {
				try {
					int track = Integer.parseInt((String) properties.get("mp3.id3tag.track"));
					if (track != DEFAULT_TRACK)
						audio.setTrack(track);
				} catch (NumberFormatException e1) {
				}
			}
			if (properties.containsKey("comment")) {
				String comment = (String) properties.get("comment");
				if (comment != null && comment.trim().length() != 0 && !comment.equals(DEFAULT_COMMENTS))
					audio.setComments(comment);
			}
			if (properties.containsKey("mp3.channels")) {
				try {
					int channels = ((Integer) properties.get("mp3.channels")).intValue();
					if (channels != DEFAULT_CHANNELS)
						audio.setChannels(channels);
				} catch (NumberFormatException e1) {
				}
			}
			if (properties.containsKey("mp3.vbr")) {
				try {
					Boolean vbr = new Boolean((String) properties.get("mp3.vbr"));
					if (!vbr.equals(DEFAULT_VBR))
						audio.setVbr(vbr);
				} catch (Exception e1) {
				}
			}
		}
	}

	// Utility class used to extract meta data within a specified time limit.
	private static final class TimedThread3 implements Callable {
		private String path;

		public TimedThread3(String path) {
			this.path = path;
		}

		public synchronized Object call() throws Exception {
			return new MP3File(path, MP3File.EXISTING_TAGS_ONLY);
		}
	}

	private static final void thirdPass(File file, Audio audio) throws Exception {
		// MP3 library sometimes takes too long to read tags...
		TimedCallable timedCallable = new TimedCallable(new TimedThread3(file.getAbsolutePath()), 1000 * 5);
		MP3File mp3File = (MP3File) timedCallable.call();
		timedCallable = null;

		if (mp3File!=null && mp3File.isMP3()) {
			int bitrate = mp3File.getBitRate();
			if (bitrate == 0)
				audio.setBitRate(128);
			else
				audio.setBitRate(bitrate);

			String value = null;
			try {
				value = Tools.clean(mp3File.getArtist());
				if (value != null && value.trim().trim().length() != 0 && !value.equals(DEFAULT_ARTIST))
					audio.setArtist(value);
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}
			try {
				value = Tools.clean(mp3File.getTitle());
				if (value != null && value.trim().trim().length() != 0 && !value.equals(DEFAULT_TITLE))
					audio.setTitle(value);
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}
			try {
				value = Tools.clean(mp3File.getAlbum());
				if (value != null && value.trim().trim().length() != 0 && !value.equals(DEFAULT_ALBUM))
					audio.setAlbum(value);
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}
			try {
				value = mp3File.getGenre();
				if (value != null && value.trim().trim().length() != 0 && !value.equals(DEFAULT_GENRE)) {
					// The MP3 library is returning the genre in the (x) format
					// for
					// some files instead of mapping it onto the genre name.
					// This code will do the mapping manually.
					Pattern pattern = Pattern.compile("\\(([0-9]*)\\).*");
					Matcher matcher = pattern.matcher(value);
					if (matcher.matches()) {
						try {
							value = getGenre(Integer.parseInt(matcher.group(1)));
							if (!value.equals(DEFAULT_GENRE))
								audio.setGenre(value);
						} catch (NumberFormatException e1) {
						}
					} else if (!value.equals(DEFAULT_GENRE))
						audio.setGenre(value);
				}
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}
			try {
				value = mp3File.getYear();
				if (value != null && value.trim().trim().length() != 0) {
					try {
						int date = Integer.parseInt(value);
						if (date != DEFAULT_DATE)
							audio.setDate(date);
					} catch (Exception ex) {
						// Ignore
					}
				}
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}

			int track = DEFAULT_TRACK;
			try {
				String trk = mp3File.getTrackString();
				try {
					track = Integer.parseInt(trk);
				} catch (NumberFormatException e) {
					// iPod numbers tracks as n/m (n out of m) eg
					int div = trk.indexOf("/");
					if (div != -1) {
						try {
							track = Integer.parseInt(trk.substring(0, div));
						} catch (NumberFormatException e2) {
						}
					}
				}
			} catch (Exception ex) {
				track = 0;
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}
			if (track != DEFAULT_TRACK)
				audio.setTrack(track);

			if (track == DEFAULT_TRACK) {
				// V1 tags sometimes use the comments to list the track number
				try {
					value = Tools.clean(mp3File.getComment());
					if (value != null && value.trim().trim().length() != 0 && !value.equals(DEFAULT_ARTIST)) {
						Pattern pattern = Pattern.compile("^Track\\s([0-9]*)$");
						Matcher matcher = pattern.matcher(value);
						if (matcher != null && matcher.find()) {
							try {
								track = Integer.parseInt(matcher.group(1));
								if (track != DEFAULT_TRACK)
									audio.setTrack(track);
							} catch (NumberFormatException e) {
								audio.setTrack(0);
							}
						}
					}
				} catch (Exception ex) {
					Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
				}
			}

			try {
				long duration = DEFAULT_DURATION;
				try {
					duration = (long) mp3File.getPlayingTime() * 1000;
				} catch (NumberFormatException e) {
					duration = 0;
				}
				if (duration != DEFAULT_DURATION)
					audio.setDuration(duration);
				if (duration == DEFAULT_DURATION) {
					// Guess the length
					long length = 0;
					if (bitrate == 0)
						length = (long) ((double) (mp3File.getFileSize() * 8) / (double) (128 * 1024));
					else
						length = (long) ((double) (mp3File.getFileSize() * 8) / (double) (bitrate * 1024));
					if (duration != DEFAULT_DURATION)
						audio.setDuration(length * 1000);
				}
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, file.getAbsolutePath());
			}
		}

		mp3File = null;
	}

	public static final void defaultProperties(Audio audio) {
		audio.setTitle(DEFAULT_TITLE);
		audio.setArtist(DEFAULT_ARTIST);
		audio.setAlbum(DEFAULT_ALBUM);
		audio.setGenre(DEFAULT_GENRE);
		audio.setSize(DEFAULT_SIZE);
		audio.setDuration(DEFAULT_DURATION);
		audio.setDate(DEFAULT_DATE);
		audio.setTrack(DEFAULT_TRACK);
		audio.setBitRate(DEFAULT_BIT_RATE);
		audio.setSampleRate(DEFAULT_SAMPLE_RATE);
		audio.setComments(DEFAULT_COMMENTS);
		audio.setLyrics(DEFAULT_LYRICS);
		audio.setChannels(DEFAULT_CHANNELS);
		audio.setVbr(DEFAULT_VBR);
		audio.setMimeType(DEFAULT_MIME_TYPE);
		audio.setType(DEFAULT_TYPE);
		audio.setDateModified(new Date());
		audio.setDateAdded(new Date());
		// audio.setDatePlayed(new Date());
		audio.setPath(DEFAULT_PATH);
		audio.setPlayCount(DEFAULT_PLAY_COUNT);
		audio.setRating(DEFAULT_RATING);
		audio.setTone(DEFAULT_TONE);
		audio.setOrigen(PC);
	}

	/*
	 * protected void loadShoutastInfo(AudioFileFormat aff) throws IOException,
	 * UnsupportedAudioFileException { String type = aff.getType().toString();
	 * if (!type.equalsIgnoreCase("mp3")) throw new
	 * UnsupportedAudioFileException("Not MP3 audio format"); if (aff instanceof
	 * TAudioFileFormat) { Map props = ((TAudioFileFormat) aff).properties(); //
	 * Try shoutcast meta data (if any). Iterator it =
	 * props.keySet().iterator(); comments = new Vector(); while (it.hasNext()) {
	 * String key = (String) it.next(); if
	 * (key.startsWith("mp3.shoutcast.metadata.")) { String value = (String)
	 * props.get(key); key = key.substring(23,key.trim().length()); if
	 * (key.equalsIgnoreCase("icy-name")) { title = value; } else if
	 * (key.equalsIgnoreCase("icy-genre")) { genre = value; } else {
	 * comments.add(key+"="+value); } } } } }
	 */

	public static final void stream(OutputStream outputStream) {
	}

	protected static void parseID3v2Frames(InputStream frames, Audio audio) {
		byte[] bframes = null;
		int size = -1;
		try {
			size = frames.available();
			bframes = new byte[size];
			frames.mark(size);
			frames.read(bframes);
			frames.reset();
		} catch (Throwable ex) {
			Tools.logException(Mp3File.class, ex, "Cannot parse ID3v2");
			return;
		}

		try {
			String value = null;
			for (int i = 0; i < bframes.length - 4; i++) {
				String code = new String(bframes, i, 4);
				if (code.equals(UNSYNCHRONISED_LYRIC_TRANSCRIPTION) || code.equals(SYNCHRONISED_LYRIC)) {
					i = i + 10;
					size = (int) (bframes[i - 6] << 24) + (bframes[i - 5] << 16) + (bframes[i - 4] << 8)
							+ (bframes[i - 3]);
					if (code.equals(COMMENTS))
						value = parseText(bframes, i, size, 5);
					else
						value = parseText(bframes, i, size, 1);
					if ((value != null) && (value.trim().length() > 0)) {
						if (code.equals(UNSYNCHRONISED_LYRIC_TRANSCRIPTION)) {
							if (value.trim().length() > 4) {
								// eng
								value = value.substring(4);
							}
							audio.setLyrics(value);
						}
					}
					i = i + size - 1;
				} else if ((code.equals(ATTACHED_PICTURE))) {
					// System.out.println("Album cover art found");
				}
			}
		} catch (Throwable ex) {
			Tools.logException(Mp3File.class, ex, "Cannot parse ID3v2: " + audio.getPath());
		}
	}

	private static String parseText(byte[] bframes, int offset, int size, int skip) throws Exception {
		String value = null;
		String[] ENC_TYPES = { "ISO-8859-1", "UTF16", "UTF-16BE", "UTF-8" };
		return new String(bframes, offset + skip, size - skip, ENC_TYPES[bframes[offset]]);
	}

	private static void createCover(InputStream input, Audio audio, String mimeType) {
		try {
			Image image = ImageIO.read(input);
			createCover(image, audio, mimeType);
		} catch (Exception ex) {
			Tools.logException(Mp3File.class, ex, "Cannot create cover");
		}
	}

	private static void createCover(Image image, Audio audio, String mimeType) {
		if (image != null) {
			try {
				// TODO What if not bufferedimage??
				if (image instanceof BufferedImage) {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(byteArrayOutputStream);
					encoder.encode((BufferedImage) image);
					byteArrayOutputStream.close();

					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream
							.toByteArray());

					BlobImpl blob = new BlobImpl(byteArrayInputStream, byteArrayOutputStream.size());

					Thumbnail thumbnail = null;
					try {
						List list = ThumbnailManager.findByKey(getKey(audio));
						if (list != null && list.size() > 0)
							thumbnail = (Thumbnail) list.get(0);
					} catch (HibernateException ex) {
						log.error("Cover create failed", ex);
					}

					try {
						if (thumbnail == null) {
							thumbnail = new Thumbnail(audio.getTitle(), mimeType, getKey(audio));
							thumbnail.setImage(blob);
							thumbnail.setDateModified(new Date());
							ThumbnailManager.createThumbnail(thumbnail);
						} else {
							thumbnail.setImage(blob);
							thumbnail.setDateModified(new Date());
							ThumbnailManager.updateThumbnail(thumbnail);
						}
					} catch (HibernateException ex) {
						log.error("Cover create failed", ex);
					}
					if (thumbnail != null) {
						audio.setCover(thumbnail.getId());
					}
				}

				image.flush();
				image = null;
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, "Cannot create cover");
			}
		}
	}

	public static String getKey(Audio audio) {
		return audio.getAlbum() + "," + audio.getArtist();
	}

	public static Image getCover(Audio audio) {
		return getCover(audio, true, true);
	}

	public static Image getCover(Audio audio, boolean useAmazon, boolean useFile) {
		// Ways to get album cover:
		// 1. Embedded APIC tag.
		// 2. File system image file.
		// 3. Amazon image lookup.

		if (audio.getCover() != null && !audio.getAlbum().equals(DEFAULT_ALBUM)
				&& !audio.getArtist().equals(DEFAULT_ARTIST)) {
			try {
				java.awt.Image image = ThumbnailManager.findImageById(audio.getCover());
				if (image != null)
					return image;
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, "Cannot create cover from id: " + audio.getPath());
			}
		}

		if (!audio.getAlbum().equals(DEFAULT_ALBUM) && !audio.getArtist().equals(DEFAULT_ARTIST)) {
			try {
				java.awt.Image image = ThumbnailManager.findImageByKey(Mp3File.getKey(audio));
				if (image != null)
					return image;
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, "Cannot create cover from key: " + audio.getPath());
			}
		}
		
		if (useFile) {
			File file = new File(audio.getPath());
			File directory = new File(file.getParent());
			if (directory.exists()) {
				File[] files = directory.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.getName().toLowerCase().equals("folder.jpg") || pathname.getName().toLowerCase().equals("album.jpg");
					}
				});
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						try {
							BufferedImage image = Tools.ImageIORead(files[i]);
							if (image != null) {
								createCover(new FileInputStream(files[i]), audio, "image/jpg");

								try {
									AudioManager.updateAudio(audio);
								} catch (HibernateException ex) {
									log.error("Cover create failed", ex);
								}
								return image;
							}
						} catch (Exception ex) {
							Tools.logException(Mp3File.class, ex, "Cannot create cover from: "
									+ files[i].getAbsolutePath());
						}
						break;
					}
				}
			}
		}

		if (useAmazon) {
			if (!audio.getAlbum().equals(DEFAULT_ALBUM) && !audio.getArtist().equals(DEFAULT_ARTIST)) {
				Image image = Amazon.getAlbumImage(getKey(audio), audio.getArtist(), audio.getAlbum());
				if (image != null) {
					try {
						createCover(image, audio, "image/jpg");

						try {
							AudioManager.updateAudio(audio);
						} catch (HibernateException ex) {
							log.error("Cover create failed", ex);
						}

					} catch (Exception ex) {
						Tools
								.logException(Mp3File.class, ex, "Cannot create cover from Amazon.com: "
										+ audio.getPath());
					}
					return image;
				}
			}
		}

		return null;
	}
	
	public static InputStream getStream(String uri) throws IOException {
		if (uri.toLowerCase().endsWith(".mp3")) {
			log.debug("getStream: " + uri);

			try {
				String id = Tools.extractName(uri);
				final Audio audio = AudioManager.retrieveAudio(Integer.valueOf(id));
				if (audio!=null)
				{
					File file = new File(audio.getPath());
					if (file.exists()) {
						try {
							/*
							TimedCallable timedCallable = new TimedCallable(new TimedThread2(file), 1000 * 2);
							AudioFileFormat aff = (AudioFileFormat) timedCallable.call();
							timedCallable = null;
							
							if (aff!=null)
							{
								log.debug("Audio Type : " + aff.getType());
								AudioInputStream in = AudioSystem.getAudioInputStream(file);
								AudioFormat baseFormat = in.getFormat();
								log.debug("Source Format : " + baseFormat.toString());
								return in;
							}
							*/
							return new BufferedInputStream(new FileInputStream(file));
						} catch (Exception ex) {
							return Tools.getInputStream(file);
						}
					}
				}
			} catch (Exception ex) {
				Tools.logException(Mp3File.class, ex, uri);
			}
		}
		return Mp3File.class.getResourceAsStream("/couldnotconnect.mp3");
	}

	/*
	 * InputStream tmp = getStream(baseUri); try { Mp3Helper mp3Helper = new
	 * Mp3Helper(tmp, tmp.available());
	 * http.addHeader(IHmeConstants.TIVO_DURATION, "" +
	 * mp3Helper.getMp3Duration()); } finally { tmp.close(); }
	 */
}