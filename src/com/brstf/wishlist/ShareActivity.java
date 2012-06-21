package com.brstf.wishlist;

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ShareActivity extends Activity {

	private WLDbAdapter mDbHelper = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the intent that started this activity
		Intent intent = getIntent();
		String url = null;
		if(intent.getAction().equals(Intent.ACTION_VIEW)) {
			Uri uri = intent.getData();
			url = uri.toString();
		} else {
			url = intent.getExtras().getString("android.intent.extra.TEXT");
		}
		
		// Before attempting to add this to the wishlist, see if it's already there
		WLEntries entries = WLEntries.getInstance();
		entries.setContext(getApplicationContext());
		entries.reload();
		
		for(WLEntry ent : entries.getEntries()) {
			if(ent.getURL().equals(url)) {
				Toast.makeText(getBaseContext(), ent.getTitle() + " is already on your wishlist!",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		// Open up the SQLite database
		mDbHelper = new WLDbAdapter(this.getApplicationContext());

		// Show a notification to let the user know it's being added to the
		// wishlist
		Toast.makeText(getBaseContext(), "Adding to wishlist..",
				Toast.LENGTH_SHORT).show();
		

		// Start the Async Task to scrape the information, then finish this
		// activity
		new WLAddApp().execute(url);
		finish();
	}

	/**
	 * Method to download a web page's text from a given URL. Used by the
	 * private class WLAddApp to add app info to the list
	 * 
	 * @param myurl
	 *            The URL of the web page to download
	 * @return The text of the webpage at the given URL
	 */
	private String downloadURL(String myurl) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(myurl);
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

	/**
	 * AsyncTask to create a WLAppEntry and add it to the App List
	 * 
	 * @author brstf
	 */
	private class WLAddApp extends AsyncTask<String, String, String> {
		// The URL and app entry
		private String url;
		private WLEntry ent = null;

		@Override
		protected String doInBackground(String... params) {
			// Try to download the Play Store text and scrape all relevant
			// information
			try {
				// Grab the text from the url
				url = params[0];
				String result = downloadURL(url);

				ent = WLEntryType.getTypeEntry(WLEntryType.getTypeFromURL(url),
						-1);

				// TODO: LOADSA MONEY!
				ent.setFromURLText(url, result);

				// Create a FileOutputStream and write the image to file
				FileOutputStream fos = getApplicationContext().openFileOutput(
						ent.getTitle() + ".png", Context.MODE_PRIVATE);
				Bitmap bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(ent.getIconPath())
								.getContent());
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();

				ent.setIconPath(ent.getTitle() + ".png");

				// Successful exit
				return "Success";
			} catch (IOException e) {
				// On an exception return null, this will be handled in
				// onPostExecute
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// If the string returned is null, we encountered an error
			if (result == null) {
				// So show a notification indicating failure:
				Toast.makeText(getBaseContext(),
						"Failed to add app to wishlist.", Toast.LENGTH_SHORT)
						.show();
			} else {
				mDbHelper.open();
				// Otherwise, add the entry to the list
				mDbHelper.createEntry(ent);

				// Show that the app was successfully added to the wishlist
				Toast.makeText(getBaseContext(),
						"Added " + ent.getTitle() + " to wishlist!",
						Toast.LENGTH_SHORT).show();
				
				// Finally, close the db helper
				mDbHelper.close();
				
				WLEntries entries = WLEntries.getInstance();
				entries.setContext(getApplicationContext());
				entries.reload();
			}
		}
	}
}
