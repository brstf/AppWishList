package com.brstf.wishlist.util;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.brstf.wishlist.R;
import com.brstf.wishlist.WLEntries;
import com.brstf.wishlist.ui.WLHomeActivity;
import com.brstf.wishlist.ui.WLListActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ActivityHelper implements OnQueryTextListener {
	protected SherlockFragmentActivity mActivity;
	private WLEntries mEntries;

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
		mEntries = WLEntries.getInstance();
	}

	public void onStart() {
		mEntries.setContext(mActivity.getApplicationContext());
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		mActivity.getSupportMenuInflater().inflate(R.menu.default_menu_items,
				menu);
		SearchView sview = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		sview.setQueryHint("Search Wishlist");
		//sview.setOnQueryTextListener(this);
		SearchManager sm = (SearchManager) mActivity.getSystemService(SherlockActivity.SEARCH_SERVICE);
		sview.setSearchableInfo(sm.getSearchableInfo(mActivity.getComponentName()));
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();
			return true;
		case R.id.menu_search:
			if( mActivity instanceof WLListActivity ) {
				mActivity.onSearchRequested();
				return true;
			}
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

	public WLEntries getEntries() {
		return mEntries;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Do nothing
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		System.out.println("Search " + mActivity.toString());
		return false;
	}
}
