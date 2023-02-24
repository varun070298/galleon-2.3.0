package org.lnicholls.galleon.database;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Auto-generated using Hibernate hbm2java tool.
 * Copyright (C) 2005, 2006 Leon Nicholls
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * See the file "COPYING" for more details.
 *     
*/
public class ShoutcastStation implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String genre;

    /** persistent field */
    private String url;

    /** persistent field */
    private int popularity;

    /** persistent field */
    private int status;

    /** full constructor */
    public ShoutcastStation(String genre, String url, int popularity, int status) {
        this.genre = genre;
        this.url = url;
        this.popularity = popularity;
        this.status = status;
    }

    /** default constructor */
    public ShoutcastStation() {
    }

    public Integer getId() {
        return this.id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPopularity() {
        return this.popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

// The following is extra code specified in the hbm.xml files

    public boolean equals(Object object)
    {
        ShoutcastStation shoutcastStation = (ShoutcastStation)object;
        if (url!=null && shoutcastStation.url!=null)
	        return url.equals(shoutcastStation.url);
		else
			return false;
    }

    public String getStatusString() {
        switch (status) {
        case 1:
            return "Pending";
        case 2:
            return "Downloaded";
        case 4:
            return "Refresh";
        case 8:
            return "Error";
        }
        return "Pending";
    }

    public static int STATUS_PENDING = 1;

    public static int STATUS_DOWNLOADED = 2;

    public static int STATUS_REFRESH = 4;

    public static int STATUS_ERROR = 8;

  
// end of extra code specified in the hbm.xml files
}
