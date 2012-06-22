package com.brstf.wishlist;

import com.brstf.wishlist.R;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class WishListActivity extends FragmentActivity implements
		WLHomeFragment.OnTileSelectedListener {
	private WLEntries entries = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Create the WLEntries instance
		entries = WLEntries.getInstance();
		entries.setContext(getApplicationContext());

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
	public void onStart() {
		super.onStart();

		// Reload all entries
		entries.reload();
	}

	@Override
	public void onTagSelected(String tag) {
		// When a tag button is selected, we'll want to replace this fragment
		// with the wishlist
		WLListView newFragment = new WLListView();
		
		// Pass in the tag to the wishlist view
		Bundle args = new Bundle();
		args.putString(WLListView.ARG_FILTERTAG, tag);
		newFragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace and add this to the back stack
		transaction.replace(R.id.fragment_container, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

}