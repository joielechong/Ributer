/**
 * @author Juan Rio Sipayung aka Joielechong
 * Medan, Sumatera Utara, Indonesia.
 *
 */
package com.joielechong.ributer;

import android.util.Log;

public class AliveCheckThread extends Thread {

	private final static String TAG = AliveCheckThread.class.getSimpleName();
	private final Process proc;
	private final CommandThread commandThread;

	public AliveCheckThread(Process proc, CommandThread commandThread) {
		this.proc = proc;
		this.commandThread = commandThread;
	}

	@Override
	public void run() {
		try {
			sleep(15000); // wait 15s, because Superuser also has 10s timeout
		} catch (InterruptedException e) {
			Log.i(TAG, "Interrupted.");
			return;
		}
		Log.w(TAG, "Still alive after 15 sec...");
		UIHelper.dumpProcessOutput(proc);
		proc.destroy();
		commandThread.interrupt();
		Log.w(TAG, "Interrupted and destroyed.");

		UIHelper.killApp(true);
	}
}
