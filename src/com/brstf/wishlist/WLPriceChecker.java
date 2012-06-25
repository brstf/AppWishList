package com.brstf.wishlist;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;

import android.os.AsyncTask;
import android.util.Log;

public class WLPriceChecker {
	ArrayList<WLEntry> mEntries = null;

	public WLPriceChecker() {
		mEntries = WLEntries.getInstance().getEntries();
	}

	/**
	 * Scans all the entries in the adapter and updates prices if the price
	 * information has changed
	 */
	public void priceCheck() {
		new WLPriceCheck().execute("");
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

	private class WLPriceCheck extends AsyncTask<String, Object, String> {
		@Override
		protected String doInBackground(String... params) {
			// Loop through each entry in the list, scrape the data and update
			// the price if necessary
			for (int i = 0; i < mEntries.size(); ++i) {
				WLEntry ent = mEntries.get(i);

				// Grab the text from the url
				String url = ent.getURL();
				String result = downloadURL(url);

				// The up-to-date entry
				WLEntry uEnt = (WLEntry) WLEntryType.getTypeEntry(
						ent.getType(), -1);
				uEnt.setFromURLText(url, result);

				onProgressUpdate(new Object[] { i, uEnt });
			}

			// Successful exit
			return "Success";
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// Update the updated item (progress update is only called if an
			// entry's information is changed)
			WLEntries.getInstance().updateEntry((Integer) values[0],
					(WLEntry) values[1]);
		}
	}
}
