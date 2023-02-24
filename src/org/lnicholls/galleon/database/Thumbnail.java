package org.lnicholls.galleon.database;

import java.io.Serializable;
import java.sql.Blob;
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
public class Thumbnail implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** persistent field */
    private String mimeType;

    /** nullable persistent field */
    private Date dateModified;

    /** persistent field */
    private String keywords;

    /** nullable persistent field */
    private Blob image;

    /** full constructor */
    public Thumbnail(String title, String mimeType, Date dateModified, String keywords, Blob image) {
        this.title = title;
        this.mimeType = mimeType;
        this.dateModified = dateModified;
        this.keywords = keywords;
        this.image = image;
    }

    /** default constructor */
    public Thumbnail() {
    }

    /** minimal constructor */
    public Thumbnail(String title, String mimeType, String keywords) {
        this.title = title;
        this.mimeType = mimeType;
        this.keywords = keywords;
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

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /** 
     * Image binary
     */
    public Blob getImage() {
        return this.image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
