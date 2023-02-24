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

package org.lnicholls.galleon.util;

import sun.security.util.SecurityConstants;

import java.lang.reflect.Member;

public class CustomSecurityManager extends SecurityManager {

	public CustomSecurityManager()
	{
		
	}
	
	public void checkMemberAccess(Class clazz, int which) {
		if (clazz == null) {
		    throw new NullPointerException("class can't be null");
		}
		System.out.println(clazz.getName()+"="+which);
		if (which != Member.PUBLIC) {
		    Class stack[] = getClassContext();
		    /*
		     * stack depth of 4 should be the caller of one of the
		     * methods in java.lang.Class that invoke checkMember
		     * access. The stack should look like:
		     * 
		     * someCaller                         [3]
		     * java.lang.Class.someReflectionAPI  [2]
		     * java.lang.Class.checkMemeberAccess [1]
		     * SecurityManager.checkMemeberAccess [0]
		     *
		     */
		    if ((stack.length<4) || 
			(stack[3].getClassLoader() != clazz.getClassLoader())) {
			checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
		    }
		}
    }
}