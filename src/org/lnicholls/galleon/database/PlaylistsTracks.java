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
public class PlaylistsTracks implements Serializable {

    /** identifier field */
    private Integer id;

    /** nullable persistent field */
    private Integer playlists;

    /** nullable persistent field */
    private Integer track;

    /** full constructor */
    public PlaylistsTracks(Integer playlists, Integer track) {
        this.playlists = playlists;
        this.track = track;
    }

    /** default constructor */
    public PlaylistsTracks() {
    }

    public Integer getId() {
        return this.id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlaylists() {
        return this.playlists;
    }

    public void setPlaylists(Integer playlists) {
        this.playlists = playlists;
    }

    public Integer getTrack() {
        return this.track;
    }

    public void setTrack(Integer track) {
        this.track = track;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
