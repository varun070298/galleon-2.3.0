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
public class Podcast implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** nullable persistent field */
    private String link;

    /** nullable persistent field */
    private String author;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String subtitle;

    /** nullable persistent field */
    private String summary;

    /** nullable persistent field */
    private String category;

    /** nullable persistent field */
    private String image;

    /** nullable persistent field */
    private Boolean explicit;

    /** nullable persistent field */
    private Boolean block;

    /** nullable persistent field */
    private String keywords;

    /** persistent field */
    private int status;

    /** nullable persistent field */
    private Date dateUpdated;

    /** persistent field */
    private String path;

    /** persistent field */
    private int rating;

    /** nullable persistent field */
    private String origen;

    /** nullable persistent field */
    private String externalId;

    /** nullable persistent field */
    private Integer ttl;

    /** nullable persistent field */
    private Date datePlayed;

    /** nullable persistent field */
    private Integer playCount;

    /** persistent field */
    private List tracks;

    /** full constructor */
    public Podcast(String title, String link, String author, String description, String subtitle, String summary, String category, String image, Boolean explicit, Boolean block, String keywords, int status, Date dateUpdated, String path, int rating, String origen, String externalId, Integer ttl, Date datePlayed, Integer playCount, List tracks) {
        this.title = title;
        this.link = link;
        this.author = author;
        this.description = description;
        this.subtitle = subtitle;
        this.summary = summary;
        this.category = category;
        this.image = image;
        this.explicit = explicit;
        this.block = block;
        this.keywords = keywords;
        this.status = status;
        this.dateUpdated = dateUpdated;
        this.path = path;
        this.rating = rating;
        this.origen = origen;
        this.externalId = externalId;
        this.ttl = ttl;
        this.datePlayed = datePlayed;
        this.playCount = playCount;
        this.tracks = tracks;
    }

    /** default constructor */
    public Podcast() {
    }

    /** minimal constructor */
    public Podcast(String title, int status, String path, int rating, List tracks) {
        this.title = title;
        this.status = status;
        this.path = path;
        this.rating = rating;
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

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getDateUpdated() {
        return this.dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
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

    public Integer getTtl() {
        return this.ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public Date getDatePlayed() {
        return this.datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }

    public Integer getPlayCount() {
        return this.playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
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

// The following is extra code specified in the hbm.xml files


    public String getStatusString() {

        switch (status) {

        case 1:

            return "Subscribed";

        case 2:

            return "Deleted";

        case 3:

            return "Error";

        }

        return "Not subscribed";

    }



    public PodcastTrack getTrack(String url)

    {

      if (getTracks()!=null)

      {

        for (java.util.Iterator j = getTracks().iterator(); j.hasNext(); /* Nothing */) {

            PodcastTrack track = (PodcastTrack) j.next();

            if (track.getUrl().equals(url))

                return track;

        }

      }

      return null;

    }





    public static int STATUS_SUBSCRIBED = 1;



    public static int STATUS_DELETED = 2;



    public static int STATUS_ERROR = 3;



  
// end of extra code specified in the hbm.xml files
}
