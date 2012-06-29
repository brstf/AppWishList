package com.brstf.wishlist.ui;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brstf.wishlist.util.ActivityHelper;

public class BaseActivity extends SherlockFragmentActivity {
	final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);

	@Override
	protected void onStart() {
		super.onStart();
		mActivityHelper.onStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return mActivityHelper.onCreateOptionsMenu(menu)
				|| super.onCreateOptionsMenu(menu);
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
