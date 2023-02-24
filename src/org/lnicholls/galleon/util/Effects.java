package org.lnicholls.galleon.util;

/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

import java.util.Collection;
import java.util.Set;
import java.util.HashMap;



public class Effects {

    private static final HashMap effects = new HashMap();

    public static final String RANDOM = "Random";

    public static final String SEQUENTIAL = "Sequential";

    static {
        effects.put("Cross Fade", new CrossFade());
        effects.put("Wipe Left", new Wipe(Wipe.LEFT));
        effects.put("Wipe Left Fade", new Wipe(Wipe.LEFT, true, true));
        effects.put("Wipe Left Fade Out", new Wipe(Wipe.LEFT, true, false));
        effects.put("Wipe Left Fade In", new Wipe(Wipe.LEFT, false, true));
        effects.put("Wipe Right", new Wipe(Wipe.RIGHT));
        effects.put("Wipe Right Fade", new Wipe(Wipe.RIGHT, true, true));
        effects.put("Wipe Right Fade Out", new Wipe(Wipe.RIGHT, true, false));
        effects.put("Wipe Right Fade In", new Wipe(Wipe.RIGHT, false, true));
        effects.put("Wipe Top", new Wipe(Wipe.TOP));
        effects.put("Wipe Top Fade", new Wipe(Wipe.TOP, true, true));
        effects.put("Wipe Top Fade Out", new Wipe(Wipe.TOP, true, false));
        effects.put("Wipe Top Fade In", new Wipe(Wipe.TOP, false, true));
        effects.put("Wipe Bottom", new Wipe(Wipe.BOTTOM));
        effects.put("Wipe Bottom Fade", new Wipe(Wipe.BOTTOM, true, true));
        effects.put("Wipe Bottom Fade Out", new Wipe(Wipe.BOTTOM, true, false));
        effects.put("Wipe Bottom Fade In", new Wipe(Wipe.BOTTOM, false, true));
        effects.put("Wipe NW", new Wipe(Wipe.NW));
        effects.put("Wipe NW Fade", new Wipe(Wipe.NW, true, true));
        effects.put("Wipe NW Fade Out", new Wipe(Wipe.NW, true, false));
        effects.put("Wipe NW Fade In", new Wipe(Wipe.NW, false, true));
        effects.put("Wipe NE", new Wipe(Wipe.NE));
        effects.put("Wipe NE Fade", new Wipe(Wipe.NE, true, true));
        effects.put("Wipe NE Fade Out", new Wipe(Wipe.NE, true, false));
        effects.put("Wipe NE Fade In", new Wipe(Wipe.NE, false, true));
        effects.put("Wipe SE", new Wipe(Wipe.SE));
        effects.put("Wipe SE Fade", new Wipe(Wipe.SE, true, true));
        effects.put("Wipe SE Fade Out", new Wipe(Wipe.SE, true, false));
        effects.put("Wipe SE Fade In", new Wipe(Wipe.SE, false, true));
        effects.put("Wipe SW", new Wipe(Wipe.SW));
        effects.put("Wipe SW Fade", new Wipe(Wipe.SW, true, true));
        effects.put("Wipe SW Fade Out", new Wipe(Wipe.SW, true, false));
        effects.put("Wipe SW Fade In", new Wipe(Wipe.SW, false, true));
    }

    public static Collection getEffects() {
        return effects.values();
    }
    
    public static Set getEffectNames() {
        return effects.keySet();
    }
    
    public static Effect getEffect(String name)
    {
        return (Effect)effects.get(name);
    }
}