package org.lnicholls.galleon.util;

/*
 * Copyright (C) 2003  Mike Kelley
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/*
 * Factory for the various useful file filters.
 */

public class FileFilters {
	private static final Logger log = Logger.getLogger(FileFilters.class
			.getName());

	public static final String FILTER_IMAGE = "image/*";

	public static final String FILTER_AUDIO = "audio/*";

	public static final String FORMAT_IMAGE = "image";

	public static final String FORMAT_AUDIO = "audio";

	public static final String FILTER_FOLDER = "x-container/folder";

	public static final String FILTER_PLAYLIST = "x-container/playlist";

	// Private constructor ensures this can not be instantiated
	private FileFilters() {
	}

	// Filter that excludes directories and accepts only files that end with
	// one of the supplied suffixes.
	// E.g. new SuffixFilter({".mp3"}) will filter for mp3 files.
	public static final class SuffixFilter implements FileFilter {
		private String[] suffix;

		public SuffixFilter(String[] suffix) {
			// Copy the array of suffixes, ensuring that they are all lower
			// case.
			this.suffix = new String[suffix.length];
			for (int i = 0; i < suffix.length; ++i) {
				this.suffix[i] = suffix[i].toLowerCase();
			}
		}

		public final boolean accept(File file) {
			String fileNameLowerCase = file.getName().toLowerCase();

			// Is this a file with one of the designated suffixes?
			for (int i = 0; i < suffix.length; ++i) {
				if (fileNameLowerCase.endsWith(suffix[i])) {
					// File suffix matches, so unless this is a directory we
					// have a match.
					// If this is a directory no need to continue searching
					// either, just
					// return false.
					return !file.isDirectory() && !file.isHidden();
				}
			}
			// File does not match list of accepted suffixes
			return false;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			synchronized (buffer) {
				for (int i = 0; i < suffix.length; ++i) {
					buffer.append(suffix[i]);
					if (i < suffix.length - 1)
						buffer.append("\n");
				}
			}
			return buffer.toString();
		}
	}

	// Filter that excludes directories and accepts only files that start with
	// one of the supplied prefixes.
	// E.g. new PrefixFilter({"http"}) will filter for URL streams.
	public static final class PrefixFilter implements FileFilter {
		private String[] prefix;

		public PrefixFilter(String[] prefix) {
			// Copy the array of prefixes, ensuring that they are all lower
			// case.
			this.prefix = new String[prefix.length];
			for (int i = 0; i < prefix.length; ++i) {
				this.prefix[i] = prefix[i].toLowerCase();
			}
		}

		public final boolean accept(File file) {
			String fileNameLowerCase = file.getName().toLowerCase();

			// Is this a file with one of the designated suffixes?
			for (int i = 0; i < prefix.length; ++i) {
				if (fileNameLowerCase.startsWith(prefix[i])) {
					// File suffix matches, so unless this is a directory we
					// have a match.
					// If this is a directory no need to continue searching
					// either, just
					// return false.
					return !file.isDirectory() && !file.isHidden();
				}
			}
			// File does not match list of accepted prefixes
			return false;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			synchronized (buffer) {
				for (int i = 0; i < prefix.length; ++i) {
					buffer.append(prefix[i]);
					if (i < prefix.length - 1)
						buffer.append("\n");
				}
			}
			return buffer.toString();
		}
	}

	// Filter that accepts only directories or links to directories.
	public static final class DirectoryFilter implements FileFilter {
		public final boolean accept(File file) {
			return file.isDirectory() && !file.isHidden();
		}

		public String toString() {
			return "directoryFilter";
		}
	}

	// Filter that accepts all files that are not directories.
	public static final class AllFilesFilter implements FileFilter {
		public final boolean accept(File file) {
			return !file.isDirectory() && !file.isHidden();
		}

		public String toString() {
			return "allFilesFilter";
		}
	}

	// The filter param can contain multiple types, e.g.
	// x-container/folder,x-container/playlist,audio/*
	// This is a general positive filter class that combines other filters
	// (i.e. if any supplied filter added matches, the file matches).
	public static final class PositiveFilter implements FileFilter {
		private ArrayList filters = new ArrayList();

		public final void addFilter(FileFilter filter) {
			filters.add(filter);
		}

		public final boolean accept(File file) {
			for (Iterator i = filters.iterator(); i.hasNext(); /* Nothing */) {
				FileFilter filter = (FileFilter) i.next();
				if (filter.accept((file))) {
					return true;
				}
			}
			return false;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			synchronized (buffer) {
				for (Iterator i = filters.iterator(); i.hasNext(); /* Nothing */) {
					FileFilter filter = (FileFilter) i.next();
					buffer.append(filter);
					if (i.hasNext())
						buffer.append("\n");
				}
			}
			return buffer.toString();
		}
	}

	// Create single static class instances of the various filters so we don't
	// need to create them during traversal.
	private static final String[] audioSuffixes = { ".mp3" };

	private static final String[] urlPrefixes = { "http" };

	private static final String[] imageSuffixes = { ".jpg", ".jpeg", ".gif",
			".png", ".bmp", ".wbmp" };

	private static final String[] playlistSuffixes = { ".m3u", ".pls" };

	private static final String[] linkSuffixes = { ".lnk" };
	
	private static final String[] videoSuffixes = { ".tivo", ".mpg", "mpeg" };

	// Static instances of the various file filters for general use.
	public static final SuffixFilter audioFilter = new SuffixFilter(
			audioSuffixes);

	public static final PrefixFilter urlFilter = new PrefixFilter(urlPrefixes);

	public static final SuffixFilter playlistFilter = new SuffixFilter(
			playlistSuffixes);
	
	public static final SuffixFilter videoFilter = new SuffixFilter(
			videoSuffixes);

	public static final SuffixFilter imageFilter = new SuffixFilter(
			imageSuffixes);

	public static final SuffixFilter linkFilter = new SuffixFilter(linkSuffixes);

	public static final DirectoryFilter directoryFilter = new DirectoryFilter();

	public static final AllFilesFilter allFilesFilter = new AllFilesFilter();

	public static final PositiveFilter imageDirectoryFilter = new PositiveFilter();

	static {
		imageDirectoryFilter.addFilter(imageFilter);
		imageDirectoryFilter.addFilter(directoryFilter);
	}

	public static final PositiveFilter audioDirectoryFilter = new PositiveFilter();

	static {
		audioDirectoryFilter.addFilter(audioFilter);
		audioDirectoryFilter.addFilter(playlistFilter);
		audioDirectoryFilter.addFilter(directoryFilter);
	}
	
	public static final PositiveFilter videoDirectoryFilter = new PositiveFilter();

	static {
		videoDirectoryFilter.addFilter(videoFilter);
		videoDirectoryFilter.addFilter(directoryFilter);
	}

	public static final PositiveFilter audioFileDirectoryFilter = new PositiveFilter();

	static {
		audioFileDirectoryFilter.addFilter(audioFilter);
		audioFileDirectoryFilter.addFilter(directoryFilter);
	}

	// Helper function that matches a simple filter parameter (i.e. only one
	// filter type)
	private static final FileFilter matchFileFilter(String parameterFilter) {
		if (parameterFilter.equals(FILTER_IMAGE)
				|| parameterFilter.startsWith(FORMAT_IMAGE)) {
			return FileFilters.imageFilter;
		} else if (parameterFilter.equals(FILTER_AUDIO)
				|| parameterFilter.startsWith(FORMAT_AUDIO)) {
			FileFilters.PositiveFilter comboFilter = new FileFilters.PositiveFilter();
			comboFilter.addFilter(audioFilter);
			comboFilter.addFilter(urlFilter);
			return comboFilter;
		} else if (parameterFilter.equals(FILTER_FOLDER)) {
			return FileFilters.directoryFilter;
		} else if (parameterFilter.equals(FILTER_PLAYLIST)) {
			return FileFilters.playlistFilter;
		}
		if (log.isDebugEnabled())
			log
					.debug("FileFilters.matchFileFilter: couldn't find one for parameterFilter = "
							+ parameterFilter + "!");
		return null;
	}

	// Return a filter filter that matches a filter parameter
	public static final FileFilter getFileFilter(String filter) {
		if (filter == null) {
			return FileFilters.allFilesFilter; // The default
		}
		FileFilter simpleFilter = matchFileFilter(filter);
		if (simpleFilter != null) {
			return simpleFilter; // Simple filter match
		}

		// Failed to create a simple filter; try parsing for a string of filters
		// and
		// creating a combination positive filter.
		FileFilters.PositiveFilter comboFilter = new FileFilters.PositiveFilter();
		StringTokenizer tokenizer = new StringTokenizer(filter, ",");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (log.isDebugEnabled())
				log.debug("getFileFilter() token=" + token);
			simpleFilter = matchFileFilter(token);
			if (simpleFilter != null) {
				if (log.isDebugEnabled())
					log.debug("adding filter="
							+ simpleFilter.getClass().getName());
				comboFilter.addFilter(simpleFilter);
			} else {
				log.error("getFileFilter() param=" + filter + " no match for "
						+ token);
			}
		}
		return comboFilter;
	}
}