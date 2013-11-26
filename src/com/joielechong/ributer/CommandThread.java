/**
 * @author Juan Rio Sipayung aka Joielechong
 * Medan, Sumatera Utara, Indonesia.
 *
 */
package com.joielechong.ributer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import android.util.Log;

public class CommandThread extends Thread {
	private static final String TAG = CommandThread.class.getSimpleName();

	private final OnErrorListener errorListener;
	private final String command;

	public CommandThread(OnErrorListener errorListener, String strCommand) {
		if (errorListener == null) {
			throw new IllegalArgumentException("errorListener cannot be null.");
		}
		this.errorListener = errorListener;
		this.command = strCommand;
	}

	@Override
	public void run() {
		Log.d(TAG, "Executing '" + command + "' command...");
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		OutputStreamWriter osw = null;
		try {
			try {
				proc = runtime.exec("su");
				osw = new OutputStreamWriter(proc.getOutputStream());
				osw.write(command);
				osw.flush();
				osw.close();
			} catch (IOException e) {
				logFailure(e);
				if (e.getMessage()
						.contains("Error running exec(). Command: su")) {
					errorListener.onNotRoot();
				} else {
					errorListener.onError(e);
				}
			} catch (Exception e) {
				logFailure(e);
				errorListener.onError(e);
			}
			try {
				if (proc != null) {
					Thread aliveCheck = new AliveCheckThread(proc, this);
					aliveCheck.start();
					Log.i(TAG, "Waiting for command...");
					int exitCode = proc.waitFor();
					Log.i(TAG, "Process exited with code " + exitCode + ".");
					aliveCheck.interrupt();
				}
			} catch (InterruptedException e) {
				logFailure(e);
				Log.e(TAG, "Interrupted while waiting for process to finish.");
			}
			if (proc != null) {
				String stdErr = UIHelper.dumpProcessOutput(proc);
				if (proc.exitValue() != 0 && stdErr.length() > 0) {
					logFailure();
					if (stdErr.contains("not allowed to su.")) {
						errorListener.onNotRoot();
					} else {
						errorListener.onError(stdErr);
					}
				}
			}
		} finally {
			// Clean up
			if (proc != null) {
				proc.destroy();
			}
		}
	}

	private void logFailure() {
		logFailure(null);
	}

	private void logFailure(Exception e) {
		Log.e(TAG, "Failed to execute '" + command + "' command.", e);
	}
}