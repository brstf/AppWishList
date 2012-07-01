package com.brstf.wishlist.ui;

import com.brstf.wishlist.R;

import android.content.Intent;
import android.os.Bundle;

public class WLHomeActivity extends BaseActivity implements
		WLHomeFragment.OnTileSelectedListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
			WLHomeFragment firstFragment = new WLHomeFragment();

			// In case this activity was started with special instructions from
			// an Intent, pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, firstFragment).commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupHomeActivity();
	}

	@Override
	public void onStart() {
		super.onStart();

		// Every time the activity starts, check for updates!
		//new WLPriceChecker().priceCheck();

		// Reload all entries
		getActivityHelper().getEntries().reload();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// When stopping write all entries from the entries list to the database
		getActivityHelper().getEntries().writeToDb();
	}

	@Override
	public void onTagSelected(String tag) {
		// When a tag button is selected, we'll want to start an activity for
		// the actual wish list
		final Intent intent = new Intent(this, WLListActivity.class);
		intent.putExtra(WLListFragment.EXTRA_TAG, tag);
		startActivity(intent);
	}
}