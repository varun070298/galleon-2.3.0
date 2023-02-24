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

import java.util.Date;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Movie;

public final class MovieFile {
	private static final Logger log = Logger.getLogger(MovieFile.class
			.getName());

	private static final String DEFAULT_TITLE = "unknown";

	public static void defaultProperties(Movie movie) {
		movie.setExternalId("");
		movie.setTitle(DEFAULT_TITLE);
		movie.setPath("");
		movie.setUrl("");
		movie.setThumbUrl("");
		movie.setGenre("");
		movie.setDate(1900);
		movie.setDuration(0);
		movie.setDirector("");
		movie.setProducer("");
		movie.setPlotOutline("");
		movie.setPlot("");
		movie.setTagline("");
		movie.setVotes(0);
		movie.setTop250(-1);
		movie.setRating(0);
		movie.setActors("");
		movie.setCredits("");
		movie.setDateModified(new Date());
		movie.setMimeType("");
		movie.setOrigen("");
		movie.setRated("");
		movie.setRatedReason("");
	}
}