package org.lnicholls.galleon.downloads;

/*
 * ThreadStatus.java
 *
 * Created on 22 de abril de 2003, 11:24 AM
 */

public class ThreadStatus {

	private static final String[] descs = { "Error", "Stopped", "Connecting", "", "", "Downloading", "", "Paused",
			"Resuming", "", "Completed" };

	public static final ThreadStatus ERROR = getStatus(0);

	public static final ThreadStatus STOPPED = getStatus(1);

	public static final ThreadStatus CONNECTING = getStatus(2);

	public static final ThreadStatus IN_PROGRESS = getStatus(5);

	public static final ThreadStatus PAUSED = getStatus(7);

	public static final ThreadStatus RESUMING = getStatus(8);

	public static final ThreadStatus COMPLETED = getStatus(10);

	int id = -1;

	String desc = "";

	public ThreadStatus(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	public static ThreadStatus getStatus(int id) {
		if (id > 10)
			id = 10;

		if (id < 0)
			id = 0;

		return new ThreadStatus(id, descs[id]);
	}

	public int getID() {
		return id;
	}

	public String getDescription() {
		return desc;
	}

	public String toString() {
		return desc;
	}
}