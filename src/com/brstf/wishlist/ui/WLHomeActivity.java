package com.brstf.wishlist.ui;

import com.brstf.wishlist.R;
import com.brstf.wishlist.provider.WLEntryContract;

import android.content.Intent;
import android.os.Bundle;

public class WLHomeActivity extends BaseActivity implements
		HomeFragment.OnTileSelectedListener {
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
			HomeFragment firstFragment = new HomeFragment();

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
		// new WLPriceChecker().priceCheck();
	}

	@Override
	public void onTagSelected(String tag, int pos) {
		// When a tag button is selected, we'll want to start an activity for
		// the actual wish list
		final Intent intent = new Intent(this, WLListActivity.class);
		if (tag == null) {
			intent.setData(WLEntryContract.Entries.CONTENT_URI);
		} else {
			intent.setData(WLEntryContract.Entries.CONTENT_URI.buildUpon()
					.appendPath("tag").appendPath(tag).build());
		}
		
		intent.putExtra(WLListActivity.KEY_TAGID, pos);
		
		startActivity(intent);
	}
}