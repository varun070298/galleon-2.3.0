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
public class PlaylistTrack implements Serializable {

    /** nullable persistent field */
    private org.lnicholls.galleon.database.Audio track;

    /** full constructor */
    public PlaylistTrack(org.lnicholls.galleon.database.Audio track) {
        this.track = track;
    }

    /** default constructor */
    public PlaylistTrack() {
    }

    public org.lnicholls.galleon.database.Audio getTrack() {
        return this.track;
    }

    public void setTrack(org.lnicholls.galleon.database.Audio track) {
        this.track = track;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .toString();
    }

}
