package com.brstf.wishlist;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class ShareActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the intent that started this activity
		Intent intent = getIntent();
		String url = null;
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			Uri uri = intent.getData();
			url = uri.toString();
		} else {
			url = intent.getExtras().getString("android.intent.extra.TEXT");
		}

		// Before attempting to add this to the wishlist, see if it's already
		// there
		WLEntries entries = WLEntries.getInstance();
		entries.setContext(getApplicationContext());
		entries.reload();

		String rc = entries.addPending(url);
		if (rc != null) {
			if (rc == WLEntries.WL_PENDING) {
				Toast.makeText(getBaseContext(),
						"This entry is pending attition to your wishlist!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(),
						rc + " is already on your wishlist!",
						Toast.LENGTH_SHORT).show();
			}
		} else {

			// Show a notification to let the user know it's being added to the
			// wishlist
			Toast.makeText(getBaseContext(), "Adding to wishlist..",
					Toast.LENGTH_SHORT).show();
	
			// Start the Async Task to scrape the information, then finish this
			// activity
			new WLAddEntry().execute(url);
		}
		finish();
	}
}
