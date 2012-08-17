package com.brstf.wishlist.util;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;

import com.brstf.wishlist.R;
import com.brstf.wishlist.ui.SettingsActivity;
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
		mActivity.getSupportMenuInflater()
				.inflate(R.menu.home_menu_items, menu);
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
		case R.id.menu_settings:
			// Start Settings Activity
			Intent settingsIntent = new Intent(mActivity,
					SettingsActivity.class);
			mActivity.startActivity(settingsIntent);
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

		// Finishing this activity simulates the behavior of pressing the back
		// button. As of right now, this behavior is desired from every sub
		// activity, though this may be necessary to change later
		// TODO: Does any sub-activity need this changed??
		mActivity.finish();
	}

	/**
	 * Fills the given {@link SharedPreferences} with default values.
	 * 
	 * @param prefs
	 *            {@link SharedPreferences} to fill with default values
	 */
	public void fillDefaultPreferences(SharedPreferences prefs) {
		SharedPreferences.Editor editor = prefs.edit();

		// Mark the preferences as created
		editor.putBoolean("CREATED", true);

		// General Preferences:
		editor.putBoolean(mActivity.getString(R.string.prefs_sync_wifi), true);
		editor.putLong(mActivity.getString(R.string.prefs_sync_interval),
				AlarmManager.INTERVAL_HALF_DAY);
		editor.putBoolean(mActivity.getString(R.string.prefs_confirm_deletion),
				false);
		editor.putBoolean(
				mActivity.getString(R.string.prefs_add_upon_addition), false);

		// Display Preferences:
		editor.putInt(mActivity.getString(R.string.prefs_display_price_movie),
				0);
		editor.putInt(
				mActivity.getString(R.string.prefs_display_price_magazine), 0);

		// Finally, commit the changes
		editor.commit();
	}
}
