package org.lnicholls.galleon.database;

import java.io.Serializable;
import java.util.Date;
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
public class Audio implements org.lnicholls.galleon.media.Media,Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** persistent field */
    private String artist;

    /** persistent field */
    private String album;

    /** persistent field */
    private String genre;

    /** persistent field */
    private long duration;

    /** persistent field */
    private long size;

    /** persistent field */
    private int date;

    /** persistent field */
    private int track;

    /** persistent field */
    private int bitRate;

    /** persistent field */
    private int sampleRate;

    /** persistent field */
    private int channels;

    /** nullable persistent field */
    private String comments;

    /** nullable persistent field */
    private String lyrics;

    /** nullable persistent field */
    private Boolean vbr;

    /** persistent field */
    private String mimeType;

    /** persistent field */
    private int type;

    /** nullable persistent field */
    private Date dateModified;

    /** nullable persistent field */
    private Date dateAdded;

    /** nullable persistent field */
    private Date datePlayed;

    /** persistent field */
    private String path;

    /** persistent field */
    private int playCount;

    /** persistent field */
    private int rating;

    /** nullable persistent field */
    private String tone;

    /** nullable persistent field */
    private Integer cover;

    /** nullable persistent field */
    private String origen;

    /** nullable persistent field */
    private String externalId;

    /** full constructor */
    public Audio(String title, String artist, String album, String genre, long duration, long size, int date, int track, int bitRate, int sampleRate, int channels, String comments, String lyrics, Boolean vbr, String mimeType, int type, Date dateModified, Date dateAdded, Date datePlayed, String path, int playCount, int rating, String tone, Integer cover, String origen, String externalId) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.size = size;
        this.date = date;
        this.track = track;
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.comments = comments;
        this.lyrics = lyrics;
        this.vbr = vbr;
        this.mimeType = mimeType;
        this.type = type;
        this.dateModified = dateModified;
        this.dateAdded = dateAdded;
        this.datePlayed = datePlayed;
        this.path = path;
        this.playCount = playCount;
        this.rating = rating;
        this.tone = tone;
        this.cover = cover;
        this.origen = origen;
        this.externalId = externalId;
    }

    /** default constructor */
    public Audio() {
    }

    /** minimal constructor */
    public Audio(String title, String artist, String album, String genre, long duration, long size, int date, int track, int bitRate, int sampleRate, int channels, String mimeType, int type, String path, int playCount, int rating) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.size = size;
        this.date = date;
        this.track = track;
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.mimeType = mimeType;
        this.type = type;
        this.path = path;
        this.playCount = playCount;
        this.rating = rating;
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

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /** 
     * Year
     */
    public int getDate() {
        return this.date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getTrack() {
        return this.track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getBitRate() {
        return this.bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannels() {
        return this.channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLyrics() {
        return this.lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public Boolean getVbr() {
        return this.vbr;
    }

    public void setVbr(Boolean vbr) {
        this.vbr = vbr;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /** 
     * One of: file=0, stream=1
     */
    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    /** 
     * The mood of the track
     */
    public String getTone() {
        return this.tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public Integer getCover() {
        return this.cover;
    }

    public void setCover(Integer cover) {
        this.cover = cover;
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
