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
public class Movie implements org.lnicholls.galleon.media.Media,Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String externalId;

    /** persistent field */
    private String IMDB;

    /** persistent field */
    private String title;

    /** persistent field */
    private String path;

    /** nullable persistent field */
    private String url;

    /** nullable persistent field */
    private String thumbUrl;

    /** nullable persistent field */
    private String genre;

    /** persistent field */
    private int date;

    /** persistent field */
    private int duration;

    /** nullable persistent field */
    private String director;

    /** nullable persistent field */
    private String producer;

    /** nullable persistent field */
    private String rated;

    /** nullable persistent field */
    private String ratedReason;

    /** nullable persistent field */
    private String plotOutline;

    /** nullable persistent field */
    private String plot;

    /** nullable persistent field */
    private String tagline;

    /** persistent field */
    private int votes;

    /** persistent field */
    private int rating;

    /** persistent field */
    private int top250;

    /** nullable persistent field */
    private String actors;

    /** nullable persistent field */
    private String credits;

    /** nullable persistent field */
    private Date dateModified;

    /** persistent field */
    private String mimeType;

    /** nullable persistent field */
    private String origen;

    /** full constructor */
    public Movie(String externalId, String IMDB, String title, String path, String url, String thumbUrl, String genre, int date, int duration, String director, String producer, String rated, String ratedReason, String plotOutline, String plot, String tagline, int votes, int rating, int top250, String actors, String credits, Date dateModified, String mimeType, String origen) {
        this.externalId = externalId;
        this.IMDB = IMDB;
        this.title = title;
        this.path = path;
        this.url = url;
        this.thumbUrl = thumbUrl;
        this.genre = genre;
        this.date = date;
        this.duration = duration;
        this.director = director;
        this.producer = producer;
        this.rated = rated;
        this.ratedReason = ratedReason;
        this.plotOutline = plotOutline;
        this.plot = plot;
        this.tagline = tagline;
        this.votes = votes;
        this.rating = rating;
        this.top250 = top250;
        this.actors = actors;
        this.credits = credits;
        this.dateModified = dateModified;
        this.mimeType = mimeType;
        this.origen = origen;
    }

    /** default constructor */
    public Movie() {
    }

    /** minimal constructor */
    public Movie(String externalId, String IMDB, String title, String path, int date, int duration, int votes, int rating, int top250, String mimeType) {
        this.externalId = externalId;
        this.IMDB = IMDB;
        this.title = title;
        this.path = path;
        this.date = date;
        this.duration = duration;
        this.votes = votes;
        this.rating = rating;
        this.top250 = top250;
        this.mimeType = mimeType;
    }

    public Integer getId() {
        return this.id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getIMDB() {
        return this.IMDB;
    }

    public void setIMDB(String IMDB) {
        this.IMDB = IMDB;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbUrl() {
        return this.thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDate() {
        return this.date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDirector() {
        return this.director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getProducer() {
        return this.producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getRated() {
        return this.rated;
    }

    public void setRated(String rated) {
        this.rated = rated;
    }

    public String getRatedReason() {
        return this.ratedReason;
    }

    public void setRatedReason(String ratedReason) {
        this.ratedReason = ratedReason;
    }

    public String getPlotOutline() {
        return this.plotOutline;
    }

    public void setPlotOutline(String plotOutline) {
        this.plotOutline = plotOutline;
    }

    public String getPlot() {
        return this.plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getTagline() {
        return this.tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public int getVotes() {
        return this.votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getTop250() {
        return this.top250;
    }

    public void setTop250(int top250) {
        this.top250 = top250;
    }

    public String getActors() {
        return this.actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getCredits() {
        return this.credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    /** 
     * When the movie was modified
     */
    public Date getDateModified() {
        return this.dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
