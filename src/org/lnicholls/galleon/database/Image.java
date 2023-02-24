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
public class Image implements org.lnicholls.galleon.media.Media,Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** nullable persistent field */
    private Date dateAdded;

    /** nullable persistent field */
    private Date dateModified;

    /** nullable persistent field */
    private Date dateCreated;

    /** nullable persistent field */
    private Date dateCaptured;

    /** nullable persistent field */
    private Date datePlayed;

    /** persistent field */
    private String path;

    /** persistent field */
    private long size;

    /** nullable persistent field */
    private String comments;

    /** persistent field */
    private String mimeType;

    /** persistent field */
    private int playCount;

    /** persistent field */
    private int rating;

    /** nullable persistent field */
    private String tone;

    /** nullable persistent field */
    private Integer rotation;

    /** nullable persistent field */
    private Integer thumbnail;

    /** nullable persistent field */
    private String origen;

    /** full constructor */
    public Image(String title, Date dateAdded, Date dateModified, Date dateCreated, Date dateCaptured, Date datePlayed, String path, long size, String comments, String mimeType, int playCount, int rating, String tone, Integer rotation, Integer thumbnail, String origen) {
        this.title = title;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.dateCreated = dateCreated;
        this.dateCaptured = dateCaptured;
        this.datePlayed = datePlayed;
        this.path = path;
        this.size = size;
        this.comments = comments;
        this.mimeType = mimeType;
        this.playCount = playCount;
        this.rating = rating;
        this.tone = tone;
        this.rotation = rotation;
        this.thumbnail = thumbnail;
        this.origen = origen;
    }

    /** default constructor */
    public Image() {
    }

    /** minimal constructor */
    public Image(String title, String path, long size, String mimeType, int playCount, int rating) {
        this.title = title;
        this.path = path;
        this.size = size;
        this.mimeType = mimeType;
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

    /** 
     * When the image was added
     */
    public Date getDateAdded() {
        return this.dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    /** 
     * When the image was created
     */
    public Date getDateModified() {
        return this.dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /** 
     * When the image was added
     */
    public Date getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /** 
     * When the image was added
     */
    public Date getDateCaptured() {
        return this.dateCaptured;
    }

    public void setDateCaptured(Date dateCaptured) {
        this.dateCaptured = dateCaptured;
    }

    /** 
     * When the image was last played
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

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
     * The mood of the image
     */
    public String getTone() {
        return this.tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public Integer getRotation() {
        return this.rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public Integer getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(Integer thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getOrigen() {
        return this.origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
