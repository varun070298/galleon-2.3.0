package org.lnicholls.galleon.downloads;

/*
 * DataWritingEvent.java
 *
 * Created on 22 de abril de 2003, 12:07 PM
 */

public class DataWritingEvent implements java.io.Serializable {

	DownloadWork source = null;

	long pos = -1;

	int length = -1;

	public DataWritingEvent(DownloadWork source, long pos, int length) {
		this.source = source;
		this.pos = pos;
		this.length = length;
	}

	public DownloadWork getSource() {
		return source;
	}

	public long getPosition() {
		return pos;
	}

	public int getLength() {
		return length;
	}
}
