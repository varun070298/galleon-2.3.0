package org.lnicholls.galleon.downloads;

/*
 * DownloadThread.java
 *
 * Created on 13 de abril de 2003, 01:25 AM
 */

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;

public class DownloadThread extends Thread implements DownloadWork, Serializable {

	private static Logger log = Logger.getLogger(DownloadThread.class.getName());

	Downloader d = null;

	URLConnection uc = null;

	RandomAccessFile raf = null;

	int id = -1;

	long from = 0;

	long to = 0;

	int c = 0;

	int status = StatusEvent.STOPPED;

	int lastStatus = ThreadStatus.STOPPED.getID();

	HashMap sl = new HashMap();

	HashMap dwl = new HashMap();

	long size = 0, completed = 0;

	boolean run = true;

	public DownloadThread(Downloader d, File f) throws Exception {
		this(d, f, -1, -1);
	}

	public DownloadThread(Downloader d, File f, Range r) throws Exception {
		this(d, f, r.getStart(), r.getEnd());
	}

	public DownloadThread(Downloader d, File f, long from, long to) throws Exception {
		this.d = d;
		setPriority(d.getPriority());
		uc = d.getURL().openConnection();

		this.from = from;
		this.to = to;

		if (from >= 0 && to >= 0) {
			if (from > to) {
				long x = from;

				from = to;
				to = x;
			}

			uc.setRequestProperty("Range", "bytes=" + from + "-" + to);
			size = to - from + 1;
		}

		raf = new FileManager().getFileFor(f);
	}

	public void addStatusListener(StatusListener sl) {
		this.sl.put(sl.toString(), sl);
	}

	public StatusListener[] getStatusListeners() {
		return (StatusListener[]) sl.values().toArray(new StatusListener[0]);
	}

	public void removeStatusListener(StatusListener sl) {
		this.sl.remove(sl.toString());
	}

	public void addDataWritingListener(DataWritingListener dwl) {
		this.dwl.put(dwl.toString(), dwl);
	}

	public DataWritingListener[] getDataWritingListeners() {
		return (DataWritingListener[]) dwl.values().toArray(new DataWritingListener[0]);
	}

	public void removeDataWritingListener(DataWritingListener dwl) {
		this.dwl.remove(dwl.toString());
	}

	public int getID() {
		return id;
	}

	public Downloader getParentDownload() {
		return d;
	}

	public long getSize() {
		return size;
	}

	public long getBytesCompleted() {
		return completed;
	}

	public int getStatus() {
		return status;
	}

	public void setID(int id) {
		this.id = id;

		setName("Thread " + id);
	}

	public void run() {

		InputStream in = null;

		try {
			run = true;

			changeStatusTo(ThreadStatus.CONNECTING);

			log.debug("[" + getName() + "] Connecting...");

			uc.connect();

			if (status == ThreadStatus.STOPPED.getID())
				return;

			if (from > -1)
				raf.seek(from);

			size = uc.getContentLength();

			int read = 1, readsf = 0;

			changeStatusTo(ThreadStatus.IN_PROGRESS);

			log.debug("[" + getName() + "] Downloading...");

			in = uc.getInputStream();

			int i = 0, x = 0;

			byte[] b = new byte[1024];

			long s = System.currentTimeMillis();

			while ((i = in.read(b)) > -1 && run) {
				raf.write(b, 0, i);

				completed += i;

				if (dwl != null)
					notifyDataWriting(new DataWritingEvent(this, raf.getFilePointer(), i));
			}

			if (!run)
				changeStatusTo(ThreadStatus.STOPPED);
			else {
				if (completed < size)
					throw new Exception("[" + getName() + "] Connection lost. Incomplete download");

				changeStatusTo(ThreadStatus.COMPLETED);
			}
		} catch (Throwable ex) {
			Tools.logException(DownloadThread.class, ex);
			changeStatusTo(ThreadStatus.ERROR);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
			}

			try {
				if (raf != null)
					raf.close();
			} catch (Exception e) {
			}
		}
	}

	public void stopDownload() {
		run = false;
	}

	public int getPercentageComplete() {
		return (int) ((long) (c * 100) / (long) (to - from + 1));
	}

	private void changeStatusTo(ThreadStatus ts) {

		lastStatus = getStatus();
		status = ts.getID();

		notifyStatusChange(new StatusEvent(this, lastStatus, status));
	}

	private void notifyStatusChange(StatusEvent se) {
		StatusListener[] sla = (StatusListener[]) sl.values().toArray(new StatusListener[0]);

		for (int w = 0; w < sla.length; ++w)
			sla[w].statusChanged(se);
	}

	private void notifyDataWriting(DataWritingEvent dwe) {
		DataWritingListener[] dwla = (DataWritingListener[]) dwl.values().toArray(new DataWritingListener[0]);

		for (int w = 0; w < dwla.length; ++w)
			dwla[w].dataWritten(dwe);
	}
}