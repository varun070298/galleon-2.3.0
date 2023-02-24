package org.lnicholls.galleon.server;

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

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/*
 * Utility class required by the service wrapper. Delegate to the Server class.
 */

public class ServiceWrapper implements WrapperListener {

	private ServiceWrapper() {
	}

	/**
	 * The start method is called when the WrapperManager is signaled by the
	 * native wrapper code that it can start its application. This method call
	 * is expected to return, so a new thread should be launched if necessary.
	 *
	 * @param args
	 *            List of arguments used to initialize the application.
	 *
	 * @return Any error code if the application should exit on completion of
	 *         the start method. If there were no problems then this method
	 *         should return null.
	 */
	public Integer start(String[] args) {
		try {
			if (mServer == null) {
				mServer = new Server();
				mServer.start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// return new Integer(1);
		}
		return null;
	}

	/**
	 * Called when the application is shutting down. The Wrapper assumes that
	 * this method will return fairly quickly. If the shutdown code code could
	 * potentially take a long time, then WrapperManager.stopping() should be
	 * called to extend the timeout period. If for some reason, the stop method
	 * can not return, then it must call WrapperManager.stopped() to avoid
	 * warning messages from the Wrapper.
	 *
	 * @param exitCode
	 *            The suggested exit code that will be returned to the OS when
	 *            the JVM exits.
	 *
	 * @return The exit code to actually return to the OS. In most cases, this
	 *         should just be the value of exitCode, however the user code has
	 *         the option of changing the exit code if there are any problems
	 *         during shutdown.
	 */
	public int stop(int exitCode) {
		try {
			mServer.stop();
			mServer = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return exitCode;
	}

	/**
	 * Called whenever the native wrapper code traps a system control signal
	 * against the Java process. It is up to the callback to take any actions
	 * necessary. Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT,
	 * WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or
	 * WRAPPER_CTRL_SHUTDOWN_EVENT
	 *
	 * @param event
	 *            The system control signal.
	 */
	public void controlEvent(int event) {
		if (WrapperManager.isControlledByNativeWrapper()) {
			// The Wrapper will take care of this event
		} else {
			// We are not being controlled by the Wrapper, so
			// handle the event ourselves.
			if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
					|| (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
					|| (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT)) {
				WrapperManager.stop(0);
			}
		}
	}

	/*---------------------------------------------------------------
	 * Main Method
	 *-------------------------------------------------------------*/
	public static void main(String[] args) {
		try {
			if (System.getProperty("wrapper.native_library") == null)
				System.setProperty("wrapper.native_library", "wrapper");
			if (System.getProperty("wrapper.java.library.path.1") == null)
				System.setProperty("wrapper.java.library.path.1", "../lib");

			if (args.length > 0) {
				System.setProperty("root", args[0]);
				System.setProperty("java.library.path", args[0] + "/bin;"
						+ args[0] + "/lib;" + args[0] + "/conf;"
						+ System.getProperty("java.library.path"));
				System.setProperty("java.class.path", args[0] + "/bin;"
						+ args[0] + "/lib;" + args[0] + "/conf;"
						+ System.getProperty("java.class.path"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Start the application. If the JVM was launched from the native
		// Wrapper then the application will wait for the native Wrapper to
		// call the application's start method. Otherwise the start method
		// will be called immediately.

		WrapperManager.start(new ServiceWrapper(), args);
	}

	private Server mServer;
}