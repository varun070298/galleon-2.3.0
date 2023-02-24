package org.lnicholls.galleon.downloads;

/*
 * StatusListener.java
 *
 * Created on 21 de abril de 2003, 05:43 PM
 */

public interface StatusListener extends java.util.EventListener {

	public void statusChanged(StatusEvent se);
}
