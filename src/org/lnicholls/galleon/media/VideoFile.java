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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mediaManager.video.FilePropertiesMovie;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.server.*;

public final class VideoFile {
	private static final Logger log = Logger.getLogger(VideoFile.class.getName());

	public static final String DEFAULT_MIME_TYPE = "audio/mpeg";

	private static final String DEFAULT_TITLE = "unknown";

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

	private static final int DEFAULT_TYPE = 0;

	private static final String DEFAULT_PATH = "";

	private static final int DEFAULT_PLAY_COUNT = 0;

	private static final int DEFAULT_RATING = 0;

	private static final String DEFAULT_TONE = "";

	public static final String PC = "PC";

	private static final String DEFAULT_VIDEO_RESOLUTION = "";

	private static final String DEFAULT_VIDEO_CODEC = "";

	private static final float DEFAULT_VIDEO_RATE = 0.0f;

	private static final int DEFAULT_VIDEO_BIT_RATE = 0;

	private static final String DEFAULT_AUDIO_CODEC = "";

	private static final float DEFAULT_AUDIO_RATE = 0.0f;

	private static final int DEFAULT_AUDIO_BIT_RATE = 0;

	private static final int DEFAULT_AUDIO_CHANNELS = 0;

	public static final Video getVideo(String filename) {
		Video video = new Video();
		defaultProperties(video);
		return getVideo(video, filename);
	}

	public static final Video getVideo(Video video, String filename) {
		File file = new File(filename);
		try {
			video.setPath(file.getCanonicalPath());
		} catch (Exception ex) {
			Tools.logException(VideoFile.class, ex, filename);
		}

		video.setSize(file.length());
		video.setDateModified(new Date(file.lastModified()));
		video.setDateRecorded(new Date(file.lastModified()));
		video.setOriginalAirDate(new Date(file.lastModified()));
		try {
			pass(file, video);
		} catch (Throwable ex) {
			Tools.logException(VideoFile.class, ex, filename);
		}

		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			// MythTV:
			// {Lost}{2005-11-16}{The Other 48 Days}{10.00 PM Wed Nov 16, 2005}{KGO}{3600}{The story of the other survivors over the prior 48 days.}1060_20060111230000_20060111233000.nuv.mpg  
			// {SeriesTitle}{OriginalAirDate}{EpisodeTitle}{DateRecorded}{CallSign}{Duration}{Description}Ignored-Data.mpg
			Pattern pattern = Pattern.compile("^\\{([^\\}]*)\\}\\{([^\\}]*)\\}\\{([^\\}]*)\\}\\{([^\\}]*)\\}\\{([^\\}]*)\\}(?:\\{([^\\}]*)\\}\\{([^\\}]*)\\})?");
			Matcher matcher = pattern.matcher(value);
			if (matcher.find()) {
				video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("yyyy-MM-dd"); // 2005-11-16
					Date date = timeDateFormat.parse(matcher.group(2));
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setEpisodeTitle(matcher.group(3));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("HH.mm a EEE MMM dd, yyyy"); // 10.00 PM Wed Nov 16, 2005
					Date date = timeDateFormat.parse(matcher.group(4));
					video.setDateRecorded(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(5));
				try
				{
					// stored in miliseconds, not seconds
					video.setDuration(Integer.parseInt(matcher.group(6))*1000);
				}
				catch (Exception ex) {}
				video.setDescription(matcher.group(7));				
			}
		}
		
		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			// Lost - ''Collision'' (Recorded Nov 23, 2005, KGO).TiVo;
			// SeriesTitle [- ''EpisodeTitle''] (DateRecorded, CallSign)
			Pattern pattern = Pattern.compile("^(.*) - ''(.*)'' \\(Recorded (.*), ([^,]*)\\)$");
			Matcher matcher = pattern.matcher(value);
			if (matcher.find()) {
				video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				video.setEpisodeTitle(matcher.group(2));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("MMM dd, yyyy"); // Nov 23, 2005
					Date date = timeDateFormat.parse(matcher.group(3));
					video.setDateModified(date);
					video.setDateRecorded(date);
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(4));
			}
		}
		
		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			// Battlestar Galactica - Resurrection Ship (Recorded Fri Jan 13 2006 10 00PM SCIFI).TiVo
		    Pattern pattern = Pattern.compile("^(.*) - (.*) \\(Recorded ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*)\\)$");
		    Matcher matcher = pattern.matcher(value);
		    if (matcher.find()) {
		    	video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				video.setEpisodeTitle(matcher.group(2));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("EEE MMM d yyyy hh mma");  //Sat Nov 12 2005 08 00PM
					Date date = timeDateFormat.parse(matcher.group(3)+" "+matcher.group(4)+" "+matcher.group(5)+" "+matcher.group(6)+" "+matcher.group(7)+" "+matcher.group(8));
					video.setDateModified(date);
					video.setDateRecorded(date);
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(9));
		    }
		}

		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			// Perfect Proposal - Ryan & Carrie (Recorded Sun Oct 30 2005 04 30PM, TLC).TiVo
		    Pattern pattern = Pattern.compile("^(.*) - (.*) \\(Recorded (.*), ([^,]*)\\)$");
		    Matcher matcher = pattern.matcher(value);
		    if (matcher.find()) {
		    	video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				video.setEpisodeTitle(matcher.group(2));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("EEE MMM d yyyy hh mma");  //Sat Nov 12 2005 08 00PM
					Date date = timeDateFormat.parse(matcher.group(3));
					video.setDateModified(date);
					video.setDateRecorded(date);
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(4));
		    }
		}		
		
		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			Pattern pattern = Pattern.compile("^(.*) \\(Recorded (.*), ([^,]*)\\)$");
		    Matcher matcher = pattern.matcher(value);
		    if (matcher.find()) {
		    	video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("MMM dd, yyyy"); // Nov 23, 2005
					Date date = timeDateFormat.parse(matcher.group(2));
					video.setDateModified(date);
					video.setDateRecorded(date);
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(3));
		    }
		}

		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			// Battlestar Galactica - Resurrection Ship (Recorded Fri Jan 13 2006 10 00PM SCIFI).TiVo
		    Pattern pattern = Pattern.compile("^(.*) \\(Recorded ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*) ([\\S]*)\\)$");
		    Matcher matcher = pattern.matcher(value);
		    if (matcher.find()) {
		    	video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("EEE MMM d yyyy hh mma");  //Sat Nov 12 2005 08 00PM
					Date date = timeDateFormat.parse(matcher.group(2)+" "+matcher.group(3)+" "+matcher.group(4)+" "+matcher.group(5)+" "+matcher.group(6)+" "+matcher.group(7));
					video.setDateModified(date);
					video.setDateRecorded(date);
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(8));
		    }
		}		
		
		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
		    Pattern pattern = Pattern.compile("^(.*) \\(Recorded (.*), ([^,]*)\\)$");
		    Matcher matcher = pattern.matcher(value);
		    if (matcher.find()) {
		    	video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				try
				{
					SimpleDateFormat timeDateFormat = new SimpleDateFormat();
					timeDateFormat.applyPattern("EEE MMM d yyyy hh mma");  //Sat Nov 12 2005 08 00PM
					Date date = timeDateFormat.parse(matcher.group(2));
					video.setDateModified(date);
					video.setDateRecorded(date);
					video.setOriginalAirDate(date);
				}
				catch (Exception ex) {}
				video.setCallsign(matcher.group(3));
		    }
		}
		
		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			// The 4400 1x01 - ''The Return'' Pilot.mpg
			Pattern pattern = Pattern.compile("^(.*) (.*)x(.*) - ''(.*)''(.*)$");
			Matcher matcher = pattern.matcher(value);
			if (matcher.find()) {
				video.setTitle(matcher.group(1));
				video.setSeriesTitle(matcher.group(1));
				video.setEpisodeTitle(matcher.group(4));
				try
				{
					video.setEpisodeNumber(Integer.parseInt(matcher.group(3)));
				}
				catch (Exception ex) {}
			}
		}		
		
		if (video.getTitle().equals(DEFAULT_TITLE)) {
			String value = Tools.trimSuffix(file.getName());
			Pattern pattern = Pattern.compile("(.*) - (.*)");
			Matcher matcher = pattern.matcher(value);
			if (matcher != null && matcher.matches()) {
				video.setTitle(matcher.group(2));
			} else
				video.setTitle(value);
		}

		return video;
	}

	public static final void defaultProperties(Video video) {
		video.setTitle(DEFAULT_TITLE);
		video.setMimeType(DEFAULT_MIME_TYPE);
		video.setDateModified(new Date());
		video.setDateRecorded(new Date());
		video.setOriginalAirDate(new Date());
		video.setPath(DEFAULT_PATH);
		video.setPlayCount(new Integer(DEFAULT_PLAY_COUNT));
		// video.setRating(DEFAULT_RATING);
		video.setTone(DEFAULT_TONE);
		video.setOrigen(PC);
		// TODO
		video.setVideoResolution(DEFAULT_VIDEO_RESOLUTION);
		video.setVideoCodec(DEFAULT_VIDEO_CODEC);
		video.setVideoRate(new Float(DEFAULT_VIDEO_RATE));
		video.setVideoBitRate(new Integer(DEFAULT_VIDEO_BIT_RATE));
		video.setAudioCodec(DEFAULT_AUDIO_CODEC);
		video.setAudioRate(new Float(DEFAULT_AUDIO_RATE));
		video.setAudioBitRate(new Integer(DEFAULT_AUDIO_BIT_RATE));
		video.setAudioChannels(new Integer(DEFAULT_AUDIO_CHANNELS));
	}

	private static final void pass(File file, Video video) throws Exception {
		FilePropertiesMovie filePropertiesMovie = new FilePropertiesMovie(file.getAbsolutePath());

		video.setSize(filePropertiesMovie.getFileSize());
		video.setVideoResolution(filePropertiesMovie.getVideoResolution());
		video.setVideoCodec(filePropertiesMovie.getVideoCodec());
		try {
			video.setVideoRate(new Float(filePropertiesMovie.getVideoRate()));
		} catch (Exception ex) {
		}

		try {
			video.setVideoBitRate(new Integer(filePropertiesMovie.getVideoBitRate()));
		} catch (Exception ex) {
		}

		if (filePropertiesMovie.getDuration()>0)
			video.setDuration(filePropertiesMovie.getDuration());
		video.setAudioCodec(filePropertiesMovie.getAudioCodec());

		try {
			video.setAudioBitRate(new Integer(filePropertiesMovie.getAudioBitRate()));
		} catch (Exception ex) {
		}

		try {
			Pattern pattern = Pattern.compile("(.*)KHz");
			Matcher matcher = pattern.matcher(filePropertiesMovie.getAudioRate());
			if (matcher != null && matcher.matches()) {
				video.setAudioRate(new Float(matcher.group(1)));
			} else
				video.setAudioRate(new Float(filePropertiesMovie.getAudioRate()));
		} catch (Exception ex) {
		}

		try {
			video.setAudioChannels(new Integer(filePropertiesMovie.getAudioChannels()));
		} catch (Exception ex) {
		}

		 //System.out.println(filePropertiesMovie);
		 //System.out.println(video.getVideoResolution());
		 //System.out.println(video.getVideoCodec());
		 //System.out.println(video.getVideoRate());
		 //System.out.println(video.getVideoBitRate());
		 //System.out.println(video.getDuration());
		 //System.out.println(video.getAudioCodec());
		 //System.out.println(video.getAudioRate());
		 //System.out.println(video.getAudioBitRate());
		 //System.out.println(video.getAudioChannels());
	}

	static class StreamHandler extends Thread {
		StreamHandler(InputStream is) {
			mInputStream = is;
			setPriority(Thread.MIN_PRIORITY);
		}

		public void run() {
			try {
				Pattern pattern = Pattern.compile("frame=(.*) q=(.*) size=(.*) time=(.*) bitrate=(.*)"); // frame=
																											// 7499
																											// q=0.0
																											// size=
																											// 110266kB
																											// time=250.1
																											// bitrate=3611.9kbits/s
				InputStreamReader inputStreamReader = new InputStreamReader(mInputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						System.out.println(matcher.group(1));
					}
					Thread.sleep(200);
				}
			}
			catch (InterruptedException ex)
			{
			} catch (Exception ex) {
				Tools.logException(VideoFile.class, ex);
			}
		}

		InputStream mInputStream;
	}
	
	public static boolean convert(Video video, String path) {
		try {
			GoBackConfiguration goBackConfiguration = Server.getServer().getGoBackConfiguration();
			if (goBackConfiguration.getConversionTool()!=null)
			{
				String exec = goBackConfiguration.getConversionTool().replaceAll("\\{\\%1\\}",video.getPath().replaceAll("\\\\","\\\\\\\\")).replaceAll("\\{\\%2\\}",path.replaceAll("\\\\","\\\\\\\\"));
				
				Runtime rt = Runtime.getRuntime();
				//String exec = "\"c:/program files/videolan/vlc/vlc\""
				//	//+ " -vvv "
				//	+ " \"" + video.getPath() + "\""
				//	+ " -I telnet --sout #transcode{vcodec=mp2v,vb=4096,width=720,height=480,fps=29.97,scale=1,acodec=mpga,ab=128,channels=2}:duplicate{dst=std{access=file,mux=ps,url="  //dummy
				//	+ "\""+path+"\"}} vlc:quit";
				log.debug(exec);
				Process proc = rt.exec(exec);
	
				StreamHandler errorHandler = new StreamHandler(proc.getErrorStream());
				StreamHandler outputHandler = new StreamHandler(proc.getInputStream());
	
				errorHandler.start();
				outputHandler.start();
	
				int exitVal = proc.waitFor();
				if (exitVal!=0)
	           {
	        	   if (errorHandler.isAlive())
	        		   errorHandler.interrupt();
	        	   if (outputHandler.isAlive())
	        		   outputHandler.interrupt();
	        	   log.error("Could not convert: "+video.getPath() + " ("+exitVal+")");
	        	   return false;
	           }
				return true;
			}
		} catch (Throwable t) {
			Tools.logException(VideoFile.class, t, "Could not convert: "+video.getPath());
		}
		return false;
	}

	public static boolean convertffmpeg(Video video, String path) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt
					.exec("\""
							+ System.getProperty("bin")
							+ File.separatorChar
							+ "ffmpeg\" -i \""
							+ video.getPath()
							+ "\" -y -hq -target ntsc-dvd -r ntsc -aspect 4:3 -s 720x480 -acodec mp2 -ab 128 "
							+ "\""+path+"\"");

			StreamHandler errorHandler = new StreamHandler(proc.getErrorStream());
			StreamHandler outputHandler = new StreamHandler(proc.getInputStream());

			errorHandler.start();
			outputHandler.start();

			int exitVal = proc.waitFor();
			if (exitVal!=0)
           {
        	   if (errorHandler.isAlive())
        		   errorHandler.interrupt();
        	   if (outputHandler.isAlive())
        		   outputHandler.interrupt();
        	   log.debug("Could not convert: "+video.getPath() + " ("+exitVal+")");
        	   return false;
           }
			return true;
		} catch (Throwable t) {
			Tools.logException(VideoFile.class, t, "Could not convert: "+video.getPath());
		}
		return false;
	}
	
	public static boolean isTiVoFormat(Video video)
	{
		try
		{
			return ( video.getVideoResolution().equals("720x480") || video.getVideoResolution().equals("704x480") || video.getVideoResolution().equals("544x480") || video.getVideoResolution().equals("480x480") || video.getVideoResolution().equals("352x480")) &&
				(video.getVideoCodec().equals("MPEG") || (video.getVideoCodec().equals("MPEG2"))) &&
				video.getVideoRate().equals("29.97") &&
				video.getAudioCodec().equals("MPEG");
		}
		catch (Exception ex)
		{
		}
		return false;
	}
}