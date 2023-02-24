package org.lnicholls.galleon.downloads;

/*
 * Luis Javier Beltran
 * luisjavier2@users.sourceforge.net

 * http://sourceforge.net/projects/gdownloader
 * 
 * DownloadThreadManager.java
 *
 * Created on 15 de abril de 2003, 09:42 PM
 */

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;

public class Downloader extends Thread implements DownloadWork, Serializable {

	private static Logger log = Logger.getLogger(Downloader.class.getName());

	private class SL implements StatusListener, Serializable {
		public void statusChanged(StatusEvent se) {
			DownloadThread dt = (DownloadThread) se.getSource();

			if (se.getNewStatus() == se.COMPLETED)
				--remaining;

			if (remaining == 0) {
				notifyStatusChange(new StatusEvent(d, getStatus(), StatusEvent.COMPLETED));

				download.setStatus(StatusEvent.COMPLETED);
			}
			else
			if (se.getNewStatus() == se.ERROR)
			{
				notifyStatusChange(new StatusEvent(d, getStatus(), StatusEvent.ERROR));
			}
		}
	}

	private class DWL implements DataWritingListener, Serializable {
		public void dataWritten(DataWritingEvent dwe) {
			download.setBytesCompleted(download.getBytesCompleted() + dwe.getLength());

			if (dwl != null)
				notifyDataWriting(new DataWritingEvent(d, dwe.getPosition(), dwe.getLength()));
		}
	}

	int remaining = 0;

	DownloadThread[] dt = new DownloadThread[0];

	Downloader d = this;

	Range[] range = new Range[0];

	SL tsl = new SL();

	DWL tdwl = new DWL();

	HashMap dwl = new HashMap();

	Download download = null;

	public Downloader(Download download) {
		this.download = download;
	}

	public int getID() {
		return download.getID();
	}

	public int getDownloadPriority() {
		return download.getDownloadPriority();
	}

	public int getElapsedTime() {
		return download.getElapsedTime();
	}

	public URL getURL() {
		return download.getURL();
	}

	public File getLocalFile() {
		return download.getLocalFile();
	}

	public long getSize() {
		return download.getSize();
	}

	public long getBytesCompleted() {
		return download.getBytesCompleted();
	}

	public int getThreadNumber() {
		return download.getThreadNumber();
	}

	public DownloadThread[] getThreads() {
		return dt;
	}

	public void setID(int id) {
		download.setID(id);
	}

	public void setDownloadPriority(int p) {
		download.setDownloadPriority(p);
	}

	public void setThreadNumber(int threads) {
		download.setDownloadPriority(threads);
		prepareThreads();
	}

	public int getStatus() {
		return download.getStatus();
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

	private void notifyDataWriting(DataWritingEvent dwe) {
		DataWritingListener[] dwla = (DataWritingListener[]) dwl.values().toArray(new DataWritingListener[0]);

		for (int w = 0; w < dwla.length; ++w)
			dwla[w].dataWritten(dwe);
	}

	public void run() {

		try {
			download();
		} catch (Exception ex) {
			Tools.logException(Downloader.class, ex);
		}
	}

	private void prepareThreads() {

		long fl = getSize() / getThreadNumber();

		range = new Range[getThreadNumber()];
		dt = new DownloadThread[getThreadNumber()];

		try {
			for (int x = 0; x < getThreadNumber(); ++x) {
				if (x == getThreadNumber() - 1)
					range[x] = new Range(x * fl, getSize());
				else
					range[x] = new Range(x * fl, x * fl + fl - 1);

				dt[x] = new DownloadThread(this, getLocalFile(), range[x]);
				dt[x].setID(x);
			}
		} catch (Exception ex) {
			Tools.logException(Downloader.class, ex);
		}
	}

	public void restartThread(int id) throws Exception {

		if (id >= getThreadNumber() || id < 0)
			return;

		download.setBytesCompleted(download.getBytesCompleted() - dt[id].getBytesCompleted());
		StatusListener[] sla = dt[id].getStatusListeners();
		DataWritingListener[] dwla = dt[id].getDataWritingListeners();

		dt[id] = null;

		dt[id] = new DownloadThread(this, getLocalFile(), range[id]);
		dt[id].setID(id);

		for (int z = 0; z < sla.length; ++z)
			dt[id].addStatusListener(sla[z]);
		for (int z = 0; z < sla.length; ++z)
			dt[id].addDataWritingListener(dwla[z]);
		dt[id].start();
	}

	public void resumeThread(int id) throws Exception {
		if (id >= getThreadNumber() || id < 0)
			return;

		long c = dt[id].getBytesCompleted();
		StatusListener[] sla = dt[id].getStatusListeners();
		DataWritingListener[] dwla = dt[id].getDataWritingListeners();

		// Bytes to rollback
		int rollback = 10;

		// Check if completed bytes is less than the number of bytes to rollback
		if (c < rollback)
			rollback = (int) c;

		download.setBytesCompleted(download.getBytesCompleted() - rollback);
		range[id] = range[id].getSubRange(range[id].getStart() + c - rollback);
		// Destroy thread
		dt[id] = null;

		dt[id] = new DownloadThread(this, getLocalFile(), range[id]);
		dt[id].setID(id);

		for (int z = 0; z < sla.length; ++z)
			dt[id].addStatusListener(sla[z]);
		for (int z = 0; z < sla.length; ++z)
			dt[id].addDataWritingListener(dwla[z]);
		dt[id].start();
	}

	public void download() throws Exception {

		download.setStartTime(System.currentTimeMillis());

		download.setStatus(StatusEvent.CONNECTING);

		notifyStatusChange(new StatusEvent(this, StatusEvent.STOPPED, StatusEvent.CONNECTING));

		if (download.getSize() > -1) {
			download.setStatus(StatusEvent.IN_PROGRESS);

			notifyStatusChange(new StatusEvent(this, StatusEvent.CONNECTING, StatusEvent.IN_PROGRESS));

			for (int x = 0; x < getThreadNumber(); ++x) {
				dt[x].addStatusListener(tsl);
				dt[x].addDataWritingListener(tdwl);
				dt[x].start();
				++remaining;
			}
		}
	}

	public void pauseDownload() {
		for (int x = 0; x < getThreadNumber(); ++x)
			dt[x].stopDownload();

		notifyStatusChange(new StatusEvent(d, getStatus(), ThreadStatus.PAUSED.getID()));

		download.setStatus(ThreadStatus.PAUSED.getID());
	}

	public void resumeDownload() throws Exception {
		for (int x = 0; x < getThreadNumber(); ++x)
			if (dt[x].getStatus() != ThreadStatus.COMPLETED.getID())
				resumeThread(x);

		notifyStatusChange(new StatusEvent(d, getStatus(), ThreadStatus.RESUMING.getID()));

		download.setStatus(ThreadStatus.RESUMING.getID());
	}
	
	public void stopDownload() {
		for (int x = 0; x < getThreadNumber(); ++x)
			dt[x].stopDownload();

		notifyStatusChange(new StatusEvent(d, getStatus(), ThreadStatus.STOPPED.getID()));

		download.setStatus(ThreadStatus.STOPPED.getID());
	}	

	public Download getDownload() {
		return download;
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

	public void notifyStatusChange(StatusEvent se) {
		StatusListener[] sla = (StatusListener[]) sl.values().toArray(new StatusListener[0]);

		for (int w = 0; w < sla.length; ++w)
			sla[w].statusChanged(se);
	}

	HashMap sl = new HashMap();
}