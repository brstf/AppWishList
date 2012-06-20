package com.brstf.wishlist;

import com.brstf.wishlist.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class WishListActivity extends FragmentActivity {
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
			WLListView firstFragment = new WLListView();

			// In case this activity was started with special instructions from
			// an Intent, pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, firstFragment).commit();
		}
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		// Reload all entries
		entries.reload();
	}
	
}