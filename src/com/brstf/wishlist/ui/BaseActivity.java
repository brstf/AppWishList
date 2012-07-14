package com.brstf.wishlist.ui;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brstf.wishlist.service.PendingService;
import com.brstf.wishlist.util.ActivityHelper;

public class BaseActivity extends SherlockFragmentActivity {
	final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);

	@Override
	protected void onStart() {
		super.onStart();

		// Startup the pending intent service when the app is opened
		final Intent pendingIntent = new Intent(this, PendingService.class);
		startService(pendingIntent);
		mActivityHelper.onStart();
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
}
