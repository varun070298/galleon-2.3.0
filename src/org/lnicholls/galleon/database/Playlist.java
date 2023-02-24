package org.lnicholls.galleon.database;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
public class Playlist implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** nullable persistent field */
    private Date dateModified;

    /** nullable persistent field */
    private Date dateAdded;

    /** nullable persistent field */
    private Date datePlayed;

    /** persistent field */
    private int playCount;

    /** nullable persistent field */
    private String origen;

    /** nullable persistent field */
    private String externalId;

    /** persistent field */
    private List tracks;

    /** full constructor */
    public Playlist(String title, Date dateModified, Date dateAdded, Date datePlayed, int playCount, String origen, String externalId, List tracks) {
        this.title = title;
        this.dateModified = dateModified;
        this.dateAdded = dateAdded;
        this.datePlayed = datePlayed;
        this.playCount = playCount;
        this.origen = origen;
        this.externalId = externalId;
        this.tracks = tracks;
    }

    /** default constructor */
    public Playlist() {
    }

    /** minimal constructor */
    public Playlist(String title, int playCount, List tracks) {
        this.title = title;
        this.playCount = playCount;
        this.tracks = tracks;
    }

    public Integer getId() {
        return this.id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** 
     * When the track was created
     */
    public Date getDateModified() {
        return this.dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /** 
     * When the track was added
     */
    public Date getDateAdded() {
        return this.dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    /** 
     * When the track was last played
     */
    public Date getDatePlayed() {
        return this.datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public String getOrigen() {
        return this.origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List getTracks() {
        return this.tracks;
    }

    public void setTracks(List tracks) {
        this.tracks = tracks;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
