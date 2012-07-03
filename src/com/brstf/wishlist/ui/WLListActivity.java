package com.brstf.wishlist.ui;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brstf.wishlist.R;
import com.brstf.wishlist.WLEntries;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class WLListActivity extends BaseActivity implements
		OnNavigationListener {
	private ArrayAdapter<String> mAdapter = null;
	private WLListFragment mFrag = null;
	private static final String KEY_FILTER = "FILTER";
	private String mFilter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item);

		if (findViewById(R.id.fragment_container) != null) {
			mFilter = null;

			// If we have a saved state, restore the previous filter tag
			if (savedInstanceState != null) {
				mFilter = savedInstanceState.getString(KEY_FILTER);
				mFilter = mFilter == null ? "all" : mFilter;
			}

			mFrag = new WLListFragment();
			mFrag.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, mFrag).commit();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// Fill in adapter entries
		mAdapter.clear();
		mAdapter.add("All");
		for (String t : getActivityHelper().getEntries().getTags()) {
			mAdapter.add(WLEntries.getDisplayTag(t));
		}

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(mAdapter, this);

		// If we have a fragment initialized, grab the filter, and set it as the
		// filter tag
		if (mFilter == null) {
			mFilter = mFrag.getFilter();
			mFilter = mFilter == null ? "all" : mFilter;
		}
		getSupportActionBar().setSelectedNavigationItem(
				mAdapter.getPosition(WLEntries.getDisplayTag(mFilter)));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String tag = mAdapter.getItem(itemPosition);
		tag = tag.toLowerCase();
		if (tag.equals("all")) {
			tag = null;
		}

		mFrag.filter(tag);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// If the app is exiting and we have to save state, save the current
		// filter tag so we can restore it
		mFilter = mFrag.getFilter();
		outState.putString(KEY_FILTER, mFilter);

		super.onSaveInstanceState(outState);
	}
}
