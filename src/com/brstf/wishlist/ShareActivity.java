package com.brstf.wishlist;

import com.actionbarsherlock.app.SherlockActivity;
import com.brstf.wishlist.entries.EntryType;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;
import com.brstf.wishlist.service.AddEntryService;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ShareActivity extends SherlockActivity {
	private static final String TAG = "ShareActivity";
	private static final String URL_PATTERN = "https://play.google.com/store/(apps|movies|magazines|books|music/artist|music/album)/.*$";

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

		// Before attempting to add this to the wishlist, verify the url
		if (!url.matches(URL_PATTERN)) {
			Log.d(TAG, "Bad URL: " + url);
			Toast.makeText(getBaseContext(),
					"Not a valid Google Play Store url", Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		// If the url is valid, check to make sure it's not already in the
		// wishlist
		String rc = addPending(url);
		if (rc != null) {
			if (rc == EntryType.getTypeString(EntryType.PENDING)) {
				Toast.makeText(getBaseContext(),
						"This entry is pending attition to your wishlist!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(),
						rc + " is already on your wishlist!",
						Toast.LENGTH_SHORT).show();
			}
		} else if (isNetworkAvailable()) {
			// Show a notification to let the user know it's being added to the
			// wishlist
			Toast.makeText(getBaseContext(), "Adding to wishlist..",
					Toast.LENGTH_SHORT).show();

			// Start AddEntryService to scrape the information, then finish this
			// activity
			final Intent addIntent = new Intent(this, AddEntryService.class);
			addIntent.putExtra(AddEntryService.EXTRA_URL, url);
			startService(addIntent);
		} else {
			Toast.makeText(
					getBaseContext(),
					"No network connection (Entry will be added when connection restored)",
					Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	/**
	 * Function to check if this url exists in the database.
	 * 
	 * @param url
	 */
	private synchronized String addPending(String url) {
		WLDbAdapter dbhelper = new WLDbAdapter(this);
		dbhelper.open();
		String rc = dbhelper.containsUrl(url);

		// If this wasn't pending or in the list, add a pending entry to the
		// database
		if (rc == null) {
			ContentValues values = new ContentValues();
			values.put(EntryColumns.KEY_URL, url);
			values.put(EntryColumns.KEY_TYPE,
					EntryType.getTypeString(EntryType.PENDING));
			getContentResolver().insert(
					WLEntryContract.Entries.buildEntryUri(url), values);
		}
		dbhelper.close();

		return rc;
	}

	/**
	 * Private function to determine whether or not an internet connection is
	 * available
	 * 
	 * @return True if the internet is reachable, false otherwise
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
}
