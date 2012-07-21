package com.brstf.wishlist.util;

import android.content.Intent;

import com.brstf.wishlist.R;
import com.brstf.wishlist.ui.WLHomeActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ActivityHelper {
	protected SherlockFragmentActivity mActivity;

	/**
	 * Factory method for creating {@link ActivityHelper} objects for a given
	 * activity.
	 * 
	 * @param activity
	 *            {@link SherlockActivity} that this {@link ActivityHelper} is
	 *            helping
	 * @return An instance of {@link ActivityHelper} to help the
	 *         {@link SherlockActivity}
	 */
	public static ActivityHelper createInstance(
			SherlockFragmentActivity activity) {
		return new ActivityHelper(activity);
	}

	protected ActivityHelper(SherlockFragmentActivity activity) {
		mActivity = activity;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		mActivity.getSupportMenuInflater().inflate(R.menu.default_menu_items,
				menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();
			return true;
		case R.id.menu_search:
			mActivity.onSearchRequested();
			return true;
		}
		return false;
	}

	public void setupHomeActivity() {
		final ActionBar ab = mActivity.getSupportActionBar();
		ab.setDisplayShowTitleEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setHomeButtonEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	public void setupSubActivity() {
		final ActionBar ab = mActivity.getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setHomeButtonEnabled(true);
	}

	public void goHome() {
		if (mActivity instanceof WLHomeActivity) {
			return;
		}
		final Intent intent = new Intent(mActivity, WLHomeActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mActivity.startActivity(intent);
	}
}
