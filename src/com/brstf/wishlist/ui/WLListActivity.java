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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
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
		String filter = mFrag.getFilter();
		filter = filter == null ? "all" : filter;
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(mAdapter, this);
		getSupportActionBar().setSelectedNavigationItem(
				mAdapter.getPosition(WLEntries.getDisplayTag(filter)));
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
}
