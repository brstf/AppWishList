package com.brstf.appwishlist;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
		String url = intent.getExtras().getString("android.intent.extra.TEXT");

		// Open up the SQLite database
		mDbHelper = new WLDbAdapter(this.getApplicationContext());
		mDbHelper.open();

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
		private WLAppEntry ent = null;

		@Override
		protected String doInBackground(String... params) {
			// Create the new App Entry
			ent = new WLAppEntry(-1);
			// Here ID is set to -1. We're just writing this to the database so
			// we'll never see the id

			// Try to download the Play Store text and scrape all relevant
			// information
			try {
				// Grab the text from the url
				url = params[0];
				String result = downloadURL(url);
				ent.setURL(url);

				// Set up the patterns and corresponding matchers
				Pattern p_name = Pattern
						.compile("About This App</h4><dl class=\"doc-metadata-list\" itemscope itemtype=\"http://schema.org/MobileSoftwareApplication\"><meta itemprop=\"name\" content=\"(.*?)\"");
				Pattern p_icon = Pattern
						.compile("<meta itemprop=\"image\" content=\"(.*?)\"");
				Pattern p_price = Pattern.compile("data-docPrice=\"(.*?)\"");
				Matcher m_name = p_name.matcher(result);
				Matcher m_icon = p_icon.matcher(result);
				Matcher m_price = p_price.matcher(result);

				// Find the patterns
				m_name.find();
				m_icon.find();
				m_price.find();

				// Set our variables with the retrieved information
				ent.setName(m_name.group(1));
				if (m_price.group(1).equals("Free")) {
					ent.setOriginalPrice(0.0f);
				} else {
					ent.setOriginalPrice(Float.valueOf(m_price.group(1)
							.substring(1)));
				}

				// Create a FileOutputStream and write the image to file
				FileOutputStream fos = getApplicationContext().openFileOutput(
						ent.getName() + ".png", Context.MODE_PRIVATE);
				Bitmap bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(m_icon.group(1))
								.getContent());
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();

				// TODO: Make this a default icon on download failure
				ent.setIconPath(ent.getName() + ".png");

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
				// Otherwise, add the entry to the list
				mDbHelper.createEntry(ent);

				// Show that the app was successfully added to the wishlist
				Toast.makeText(getBaseContext(),
						"Added " + ent.getName() + " to wishlist!",
						Toast.LENGTH_SHORT).show();
			}

			// Finally, close the db helper
			mDbHelper.close();
		}
	}
}
