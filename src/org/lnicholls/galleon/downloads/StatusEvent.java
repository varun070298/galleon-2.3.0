package org.lnicholls.galleon.downloads;

/*
 * Created on 21 de abril de 2003, 05:43 PM
 */

public class StatusEvent implements java.io.Serializable {

	public static final int ERROR = 0;

	public static final int STOPPED = 1;

	public static final int CONNECTING = 2;

	public static final int IN_PROGRESS = 5;

	public static final int PAUSED = 7;

	public static final int COMPLETED = 10;

	DownloadWork source = null;

	int oldStatus = STOPPED;

	int newStatus = STOPPED;

	public StatusEvent(DownloadWork source, int oldStatus, int newStatus) {
		this.source = source;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	public DownloadWork getSource() {
		return source;
	}

	public int getOldStatus() {
		return oldStatus;
	}

	public int getNewStatus() {
		return newStatus;
	}

	public String toString() {
		return ThreadStatus.getStatus(oldStatus).getDescription() + "->"
				+ ThreadStatus.getStatus(newStatus).getDescription();
	}
}
