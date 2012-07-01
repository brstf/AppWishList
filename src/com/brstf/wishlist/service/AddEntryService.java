package com.brstf.wishlist.service;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brstf.wishlist.WLDbAdapter;
import com.brstf.wishlist.WLEntries;
import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * {@link IntentService} that can be started to add entries to the wishlist. The
 * service accepts intents with extras of the form key=AddEntryService.EXTRA_URL
 * value=<url of the entry to add>. After each entry is added to the list, it
 * informs the {@link WLEntries} instance that it has been updated, and reloads
 * the list.
 * 
 * @author brstf
 * 
 */
public class AddEntryService extends IntentService {
	private static final String TAG = "AddEntryService";
	public static final String EXTRA_URL = "ENTRYURL";
	private Handler mHandler = null;

	private WLEntries mEntries = WLEntries.getInstance();

	public AddEntryService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String url = (String) intent.getCharSequenceExtra(EXTRA_URL);
		Log.d(TAG, "Adding: " + url);
		// TODO: should the addservice add this to pending?
		String result = downloadURL(url);

		final WLEntry ent = WLEntryType.getTypeEntry(
				WLEntryType.getTypeFromURL(url), -1);
		ent.setFromURLText(url, result);

		// Start up the IconService to download the icon
		final Intent iconIntent = new Intent(this, IconService.class);
		iconIntent.putExtra(IconService.EXTRA_FILENAME, ent.getIconPath());
		iconIntent.putExtra(IconService.EXTRA_URL, ent.getIconUrl());
		startService(iconIntent);

		// Add this entry to the database
		WLDbAdapter mDbHelper = new WLDbAdapter(this.getBaseContext());
		mDbHelper.open();
		mDbHelper.createEntry(ent);

		// Show that the app was successfully added to the wishlist
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(AddEntryService.this,
						"Added " + ent.getTitle() + " to wishlist!",
						Toast.LENGTH_SHORT).show();
			}
		});

		// Finally, close the db helper
		mDbHelper.close();

		// Remove this entry from the pending list
		mEntries.removePendingEntry(url);
		mEntries.reload();
	}

	/**
	 * Convenience function to download a web page's text from a given URL.
	 * 
	 * @param url
	 *            URL of the web page to download
	 * @return Text of the webpage at the given URL
	 */
	private String downloadURL(String url) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			// Get the response
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response_str = client.execute(request, responseHandler);
			return response_str;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} catch (IOException e) {
			Log.d("WL", "Error");
			return "Error";
		}
	}
}
