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
public class Version implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private int major;

    /** persistent field */
    private int release;

    /** persistent field */
    private int maintenance;

    /** persistent field */
    private int development;

    /** nullable persistent field */
    private Date dateAdded;

    /** full constructor */
    public Version(int major, int release, int maintenance, int development, Date dateAdded) {
        this.major = major;
        this.release = release;
        this.maintenance = maintenance;
        this.development = development;
        this.dateAdded = dateAdded;
    }

    /** default constructor */
    public Version() {
    }

    /** minimal constructor */
    public Version(int major, int release, int maintenance, int development) {
        this.major = major;
        this.release = release;
        this.maintenance = maintenance;
        this.development = development;
    }

    public Integer getId() {
        return this.id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    /** 
     *       Major version number.
     *   This changes only when there is significant, externally apparent enhancement frm the previous release.
     *        'n' represents the n'th version. Clients should carefully consider the implications of new versions as external interfaces 
     *        and behaviour may have changed.      
     *       
     */
    public int getMajor() {
        return this.major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    /** 
     *       Release Number. This changes when: 
     *       -  a new set of functionality is to be added, eg, implementation of a new W3C specification.  
     *       -  API or behaviour change. 
     *       -  its designated as a reference release.
     *       
     */
    public int getRelease() {
        return this.release;
    }

    public void setRelease(int release) {
        this.release = release;
    }

    /** 
     *       Maintenance Drop Number. Optional identifier used to designate maintenance drop applied to a specific release and contains 
     *       fixes for defects reported. It maintains compatibility with the release and contains no API changes. When missing, it designates 
     *       the final and complete development drop for a release.
     *       
     */
    public int getMaintenance() {
        return this.maintenance;
    }

    public void setMaintenance(int maintenance) {
        this.maintenance = maintenance;
    }

    /** 
     *       Development Drop Number.
     *       Optional identifier designates development drop of a specific release. D01 is the first development drop of a new release.
     *       Development drops are works in progress towards a compeleted, final release. A specific development drop may not completely implement 
     *       all aspects of a new feature, which may take several development drops to complete. At the point of the final drop for the release, 
     *       the D suffix will be omitted.
     *       Each 'D' drops can contain functional enhancements as well as defect fixes. 'D' drops may not be as stable as the final releases.
     *       
     */
    public int getDevelopment() {
        return this.development;
    }

    public void setDevelopment(int development) {
        this.development = development;
    }

    /** 
     * When the version was added
     */
    public Date getDateAdded() {
        return this.dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
