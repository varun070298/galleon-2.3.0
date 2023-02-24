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
public class VideocastTrack implements Serializable {

    /** persistent field */
    private String title;

    /** nullable persistent field */
    private String link;

    /** nullable persistent field */
    private String guid;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String summary;

    /** nullable persistent field */
    private String subtitle;

    /** nullable persistent field */
    private String category;

    /** nullable persistent field */
    private String keywords;

    /** nullable persistent field */
    private Boolean explicit;

    /** nullable persistent field */
    private Boolean block;

    /** nullable persistent field */
    private String author;

    /** nullable persistent field */
    private Date publicationDate;

    /** persistent field */
    private String url;

    /** persistent field */
    private String mimeType;

    /** persistent field */
    private long size;

    /** persistent field */
    private int status;

    /** nullable persistent field */
    private Long duration;

    /** nullable persistent field */
    private Integer rating;

    /** persistent field */
    private int downloadTime;

    /** persistent field */
    private long downloadSize;

    /** nullable persistent field */
    private Integer videocast;

    /** nullable persistent field */
    private Integer errors;

    /** nullable persistent field */
    private org.lnicholls.galleon.database.Video track;

    /** full constructor */
    public VideocastTrack(String title, String link, String guid, String description, String summary, String subtitle, String category, String keywords, Boolean explicit, Boolean block, String author, Date publicationDate, String url, String mimeType, long size, int status, Long duration, Integer rating, int downloadTime, long downloadSize, Integer videocast, Integer errors, org.lnicholls.galleon.database.Video track) {
        this.title = title;
        this.link = link;
        this.guid = guid;
        this.description = description;
        this.summary = summary;
        this.subtitle = subtitle;
        this.category = category;
        this.keywords = keywords;
        this.explicit = explicit;
        this.block = block;
        this.author = author;
        this.publicationDate = publicationDate;
        this.url = url;
        this.mimeType = mimeType;
        this.size = size;
        this.status = status;
        this.duration = duration;
        this.rating = rating;
        this.downloadTime = downloadTime;
        this.downloadSize = downloadSize;
        this.videocast = videocast;
        this.errors = errors;
        this.track = track;
    }

    /** default constructor */
    public VideocastTrack() {
    }

    /** minimal constructor */
    public VideocastTrack(String title, String url, String mimeType, long size, int status, int downloadTime, long downloadSize) {
        this.title = title;
        this.url = url;
        this.mimeType = mimeType;
        this.size = size;
        this.status = status;
        this.downloadTime = downloadTime;
        this.downloadSize = downloadSize;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Boolean getExplicit() {
        return this.explicit;
    }

    public void setExplicit(Boolean explicit) {
        this.explicit = explicit;
    }

    public Boolean getBlock() {
        return this.block;
    }

    public void setBlock(Boolean block) {
        this.block = block;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getPublicationDate() {
        return this.publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getDuration() {
        return this.duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getRating() {
        return this.rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    /** 
     * Download time in seconds
     */
    public int getDownloadTime() {
        return this.downloadTime;
    }

    public void setDownloadTime(int downloadTime) {
        this.downloadTime = downloadTime;
    }

    /** 
     * Download size in bytes
     */
    public long getDownloadSize() {
        return this.downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public Integer getVideocast() {
        return this.videocast;
    }

    public void setVideocast(Integer videocast) {
        this.videocast = videocast;
    }

    public Integer getErrors() {
        return this.errors;
    }

    public void setErrors(Integer errors) {
        this.errors = errors;
    }

    public org.lnicholls.galleon.database.Video getTrack() {
        return this.track;
    }

    public void setTrack(org.lnicholls.galleon.database.Video track) {
        this.track = track;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .toString();
    }

// The following is extra code specified in the hbm.xml files


    public boolean equals(Object object)

    {

        VideocastTrack videocastTrack = (VideocastTrack)object;

        return url.equals(videocastTrack.url);

    }



    public String getStatusString() {

        switch (status) {

        case 1:

            return "Downloading";

        case 2:

            return "Downloaded";

        case 4:

            return "Queued";

        case 8:

            return "Download Cancelled";

        case 16:

            return "Download Error";

        case 32:

            return "Deleted";

        case 64:

            return "Played";

        }

        return "Not downloaded";

    }



    public static int STATUS_DOWNLOADING = 1;



    public static int STATUS_DOWNLOADED = 2;



    public static int STATUS_QUEUED = 4;



    public static int STATUS_DOWNLOAD_CANCELLED = 8;



    public static int STATUS_DOWNLOAD_ERROR = 16;



    public static int STATUS_DELETED = 32;



    public static int STATUS_PLAYED = 64;



  
// end of extra code specified in the hbm.xml files
}
