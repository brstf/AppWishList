package com.brstf.wishlist;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brstf.wishlist.R;

import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.os.Bundle;

public class WishListActivity extends SherlockFragmentActivity implements
		WLHomeFragment.OnTileSelectedListener, OnNavigationListener {
	private WLEntries entries = null;
	private ArrayAdapter<String> mAdapter = null;
	private WLListView mList = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Create the WLEntries instance
		entries = WLEntries.getInstance();
		entries.setContext(getApplicationContext());

		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item);

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

		// Everytime the activity starts, check for updates!
		new WLPriceChecker().priceCheck();

		// Fill in adapter entries
		mAdapter.clear();
		mAdapter.add("All");
		for (String t : WLEntries.getInstance().getTags()) {
			mAdapter.add(t);
		}

		// Reload all entries
		entries.reload();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// When stopping write all entries from the entries list to the database
		entries.writeToDb();
	}

	@Override
	public void onTagSelected(String tag) {
		// When a tag button is selected, we'll want to replace this fragment
		// with the wishlist
		mList = new WLListView();

		// Pass in the tag to the wishlist view
		Bundle args = new Bundle();
		args.putString(WLListView.ARG_FILTERTAG, tag);
		mList.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace and add this to the back stack
		transaction.replace(R.id.fragment_container, mList);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
		
		// Initialize actionbar for the new fragment
		initListActionBar();
		if (tag == null) {
			tag = "All";
		}
		getSupportActionBar().setSelectedNavigationItem(
				mAdapter.getPosition(tag));
	}

	private void initListActionBar() {
		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(mAdapter, this);
		ab.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			getSupportFragmentManager().popBackStack();
			mList = null;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String tag = mAdapter.getItem(itemPosition);
		if (tag.equals("All")) {
			tag = null;
		}
		mList.filter(tag);
		return true;
	}
}