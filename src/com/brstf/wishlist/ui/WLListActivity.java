package com.brstf.wishlist.ui;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.brstf.wishlist.R;
import com.brstf.wishlist.WLEntries;

import android.os.Bundle;
import android.widget.ArrayAdapter;

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
			if (savedInstanceState != null) {
				return;
			}

			mFrag = new WLListFragment();
			mFrag.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, mFrag).commit();
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
		if (mFrag != null) {
			mFilter = mFrag.getFilter();
			mFilter = mFilter == null ? "all" : mFilter;
			getSupportActionBar().setSelectedNavigationItem(
					mAdapter.getPosition(WLEntries.getDisplayTag(mFilter)));

		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// On restore instance state, get the filter tag that we saved in
		// onSaveInstanceState
		mFilter = savedInstanceState.getString(KEY_FILTER);
		mFilter = mFilter == null ? "all" : mFilter;
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

		// If our activity was destroyed, mFrag will be null when we set our
		// ActionBar to a list navigation mode
		if (mFrag != null) {
			mFrag.filter(tag);
		}
		return true;
	}

	@Override
	public void onDestroy() {
		mFilter = mFrag.getFilter();

		getSupportFragmentManager().beginTransaction().remove(mFrag).commit();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// If the app is exiting and we have to save state, save the current
		// filter tag so we can restore it
		outState.putString(KEY_FILTER, mFilter);
	}
}
