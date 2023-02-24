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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.TimedCallable;

public class ReloadTask extends TimerTask implements Callable {

    private static Logger log = Logger.getLogger(ReloadTask.class.getName());

    public ReloadTask(ReloadCallback reloadCallback) {
        super();
        mReloadCallback = reloadCallback;
    }

    public void run() {
        if (log.isDebugEnabled())
            log.debug("ReloadTask run:");
        try {
            // Make user that each reload call doesnt take too long
            // TODO Make timeout configurable
            TimedCallable timedCallable = new TimedCallable(this, 5000 * 60);
            timedCallable.call();
        } catch (Exception ex) {
            Tools.logException(ReloadTask.class, ex);
        } catch (OutOfMemoryError ex) {
            if (log.isDebugEnabled())
                Tools.logMemory();
            System.gc();
            if (log.isDebugEnabled())
                Tools.logMemory();
        } catch (Throwable ex) {
            if (log.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                log.debug(writer.toString());
            }
        }
    }

    public synchronized java.lang.Object call() throws java.lang.Exception {
        mReloadCallback.reload();
        return null;
    }

    private ReloadCallback mReloadCallback;
}