package com.brstf.wishlist.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brstf.wishlist.R;
import com.brstf.wishlist.service.PendingService;
import com.brstf.wishlist.util.ActivityHelper;
import com.brstf.wishlist.util.NetworkUtils;

public class BaseActivity extends SherlockFragmentActivity {
	final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
	protected static SharedPreferences mPrefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
		if (!mPrefs.contains("CREATED")) {
			// Create the initial preferences
			mActivityHelper.fillDefaultPreferences(mPrefs);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Startup the pending intent service when the app is opened
		final Intent pendingIntent = new Intent(this, PendingService.class);
		startService(pendingIntent);

		// Get the preferred interval, then schedule price checking updates if
		// it's been too long
		long interval = mPrefs.getLong(getString(R.string.prefs_sync_interval),
				0);
		if (mPrefs.getLong(getString(R.string.prefs_last_check_time), 0)
				+ interval < System.currentTimeMillis()) {
			NetworkUtils.schedulePriceCheck(getBaseContext(), interval, mPrefs,
					getString(R.string.prefs_last_check_time));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mActivityHelper.onCreateOptionsMenu(menu)) {
			return true;
		} else {
			return super.onCreateOptionsMenu(menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mActivityHelper.onOptionsItemSelected(item)
				|| super.onOptionsItemSelected(item);
	}

	protected ActivityHelper getActivityHelper() {
		return mActivityHelper;
	}

	public static Bundle intentToFragmentArguments(Intent intent) {
		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}

		final Uri uri = intent.getData();
		if (uri != null) {
			arguments.putParcelable("_uri", uri);
		}

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(extras);
		}

		return arguments;
	}

	public static Intent fragmentArgumentsToIntent(Bundle arguments) {
		final Intent intent = new Intent();

		if (arguments == null) {
			return intent;
		}

		final Uri uri = arguments.getParcelable("_uri");
		if (uri != null) {
			intent.setData(uri);
		}

		intent.putExtras(arguments);
		intent.removeExtra("_uri");
		return intent;
	}
}
