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
public class Theater implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private Date dateModified;

    /** nullable persistent field */
    private String address;

    /** nullable persistent field */
    private String telephone;

    /** nullable persistent field */
    private Integer favorite;

    /** persistent field */
    private List showtimes;

    /** full constructor */
    public Theater(String name, Date dateModified, String address, String telephone, Integer favorite, List showtimes) {
        this.name = name;
        this.dateModified = dateModified;
        this.address = address;
        this.telephone = telephone;
        this.favorite = favorite;
        this.showtimes = showtimes;
    }

    /** default constructor */
    public Theater() {
    }

    /** minimal constructor */
    public Theater(String name, List showtimes) {
        this.name = name;
        this.showtimes = showtimes;
    }

    public Integer getId() {
        return this.id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 
     * When the theater was updated
     */
    public Date getDateModified() {
        return this.dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Integer getFavorite() {
        return this.favorite;
    }

    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    public List getShowtimes() {
        return this.showtimes;
    }

    public void setShowtimes(List showtimes) {
        this.showtimes = showtimes;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
