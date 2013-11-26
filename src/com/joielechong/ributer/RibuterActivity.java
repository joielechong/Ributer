/**
 * @author Juan Rio Sipayung aka Joielechong
 * Medan, Sumatera Utara, Indonesia.
 *
 */
package com.joielechong.ributer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class RibuterActivity extends Activity implements OnErrorListener,
DialogInterface.OnKeyListener, View.OnClickListener {

	static final String TAG = RibuterActivity.class.getSimpleName();
	Button btnForceShutdown;
	Button btnForceReboot;
	Button btnHotBoot;
	Button btnForceRebootRecovery;
	Button btnVisitWeb;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_ributer);
		btnForceShutdown = (Button) findViewById(R.id.force_shutdown);
		btnForceReboot = (Button) findViewById(R.id.force_reboot);
		btnHotBoot = (Button) findViewById(R.id.hot_boot);
		btnForceRebootRecovery = (Button) findViewById(R.id.force_reboot_recovery);
		btnVisitWeb = (Button) findViewById(R.id.visit_web);
		btnForceShutdown.setOnClickListener(this);
		btnForceReboot.setOnClickListener(this);
		btnHotBoot.setOnClickListener(this);
		btnForceRebootRecovery.setOnClickListener(this);
		btnVisitWeb.setOnClickListener(this);
	}

	private void command(String strCommand) {
		new CommandThread(this, strCommand).start();
	}

	public void openWebURL() {
		Intent browse = new Intent(Intent.ACTION_VIEW,
				Uri.parse(getString(R.string.website_first)));
		startActivity(browse);
	}

	public void onClick(View view) {
		if (view.getId() == R.id.visit_web) {
			openWebURL();
		} else {
			String strCommand = null;
			CharSequence strMessage = null;

			switch (view.getId()) {
			case R.id.force_shutdown:
				final String strShutdownAndroid = "/system/bin/reboot -p";
				strCommand = strShutdownAndroid;
				strMessage = getString(R.string.really_shutdown);
				break;
			case R.id.force_reboot:
				final String strRebootAndroid = "/system/bin/reboot";
				strCommand = strRebootAndroid;
				strMessage = getString(R.string.really_reboot);
				break;
			case R.id.hot_boot:
				final String strHotBootAndroid = "busybox killall system_server";
				strCommand = strHotBootAndroid;
				strMessage = getString(R.string.really_hot_boot);
				break;
			case R.id.force_reboot_recovery:
				final String strRebootRecoveryAndroid = "/system/bin/reboot recovery";
				strCommand = strRebootRecoveryAndroid;
				strMessage = getString(R.string.really_force_reboot_recovery);
				break;
			default:
				RibuterActivity.this.forceExit();
			}

			final String f_strCommand = strCommand;

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(strMessage)
			.setOnKeyListener(this)
			.setCancelable(true)
			.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int id) {
					RibuterActivity.this.command(f_strCommand);
				}
			})
			.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int id) {
					// If user choose No, do nothing
					//RibuterActivity.this.forceExit();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		/*
		 * Check if the app was just launched. If the app was just launched then
		 * assume that the HOME key will be pressed next unless a navigation
		 * event by the user or the app occurs. Otherwise the user or the app
		 * navigated to this activity so the HOME key was not pressed.
		 */
		UIHelper.checkJustLaunced();
	}

	@Override
	public void finish() {
		/*
		 * This can only invoked by the user or the app finishing the activity
		 * by navigating from the activity so the HOME key was not pressed.
		 */
		UIHelper.homeKeyPressed = false;
		super.finish();
	}

	@Override
	public void onStop() {
		super.onStop();

		/*
		 * Check if the HOME key was pressed. If the HOME key was pressed then
		 * the app will be killed. Otherwise the user or the app is navigating
		 * away from this activity so assume that the HOME key will be pressed
		 * next unless a navigation event by the user or the app occurs.
		 */
		UIHelper.checkHomeKeyPressed(true);
	}

// We don't need menu on this application
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.ributer, menu);
//
//		/*
//		 * Assume that the HOME key will be pressed next unless a navigation
//		 * event by the user or the app occurs.
//		 */
//		UIHelper.homeKeyPressed = true;
//
//		return true;
//	}

	@Override
	public boolean onSearchRequested() {
		/*
		 * Disable the SEARCH key.
		 */
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UIHelper.killApp(true);
	}

	@Override
	public void onNotRoot() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showNotRootedDialog();
			}
		});
	}

	@Override
	public void onError(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showErrorDialog(msg);
			}
		});
	}

	@Override
	public void onError(final Exception exc) {
		final String msg = exc.getClass().getSimpleName() + ": "
				+ exc.getMessage();
		onError(msg);
	}

	private void showErrorDialog(String msg) {
		AlertDialog.Builder builder = buildErrorDialog(msg);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showNotRootedDialog() {
		final Uri uri = Uri.parse(getString(R.string.rooting_url));
		AlertDialog.Builder builder = buildErrorDialog(getString(R.string.not_rooted));
		builder.setNegativeButton(R.string.what,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				RibuterActivity.this.forceExit();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private AlertDialog.Builder buildErrorDialog(String msg) {
		return new AlertDialog.Builder(this)
		.setMessage(msg)
		.setOnKeyListener(this)
		.setCancelable(true)
		.setNeutralButton(R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				RibuterActivity.this.forceExit();
			}
		});
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			dialog.dismiss();
			forceExit();
			return true;
		}
		return false;
	}

	private void forceExit() {
		finish();
		UIHelper.killApp(true);
	}
}