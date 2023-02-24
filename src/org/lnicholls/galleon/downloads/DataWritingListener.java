package org.lnicholls.galleon.downloads;

/*
 * DataWritingListener.java
 *
 * Created on 22 de abril de 2003, 12:12 PM
 */

public interface DataWritingListener extends java.util.EventListener {

	public void dataWritten(DataWritingEvent dwe);
}
