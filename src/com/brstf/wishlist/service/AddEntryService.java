package com.brstf.wishlist.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;
import com.brstf.wishlist.util.ProviderUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * {@link IntentService} that can be started to add entries to the wishlist. The
 * service accepts intents with extras of the form key=AddEntryService.EXTRA_URL
 * value=<url of the entry to add>. After each entry is added to the list, the
 * list is reloaded.
 * 
 * @author brstf
 * 
 */
public class AddEntryService extends IntentService {
	private static final String TAG = "AddEntryService";
	public static final String EXTRA_URL = "ENTRYURL";
	private Handler mHandler = null;

	public AddEntryService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String url = (String) intent.getCharSequenceExtra(EXTRA_URL);

		// On slow connections, multiple intents for the same entry can get
		// added multiple times, so check to see if this entry is still pending
		WLDbAdapter dbhelper = new WLDbAdapter(getBaseContext());
		dbhelper.open();
		Cursor c = dbhelper.fetchEntry(dbhelper.fetchId(url));
		if (!WLEntryType.getTypeString(WLEntryType.PENDING).equals(
				c.getString(c.getColumnIndex(EntryColumns.KEY_TYPE)))) {
			// If it's no longer pending, exit this function
			dbhelper.close();
			return;
		}

		Log.d(TAG, "Adding: " + url);
		// TODO: should the addservice add this to pending?
		String result = downloadURL(url);

		final WLEntry ent = WLEntryType.getTypeEntry(
				WLEntryType.getTypeFromURL(url), -1);
		ent.setFromURLText(url, result);

		// Download the icon
		try {
			FileOutputStream fos = getBaseContext().openFileOutput(
					ent.getIconPath(), Context.MODE_PRIVATE);
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
					ent.getIconUrl()).getContent());
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
			Log.d(TAG, "Successfully downloaded icon for: " + ent.getIconPath());
		} catch (IOException e) {
			Log.d(TAG, "Failed to download icon for: " + ent.getIconPath());
		}

		// Add this entry to the database
		ProviderUtils.update(getContentResolver(), ent);

		dbhelper.addTag(url, WLEntryType.getTypeString(ent.getType())
				.toLowerCase());
		if (ent.getType() == WLEntryType.MUSIC_ALBUM
				|| ent.getType() == WLEntryType.MUSIC_ARTIST) {
			dbhelper.addTag(url, "music");
		}
		dbhelper.close();
		
		// After adding the tag, notify a change in the database
		getContentResolver().notifyChange(WLEntryContract.Tags.CONTENT_URI,
				null);
		
		// Show that the app was successfully added to the wishlist
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(AddEntryService.this,
						"Added " + ent.getTitle() + " to wishlist!",
						Toast.LENGTH_SHORT).show();
			}
		});
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
