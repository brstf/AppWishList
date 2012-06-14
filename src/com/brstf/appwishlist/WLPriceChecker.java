package com.brstf.appwishlist;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brstf.appwishlist.WLAppList.WLAdapter;

import android.os.AsyncTask;
import android.util.Log;

public class WLPriceChecker {
	private WLAdapter mAdapter = null;
	private WLDbAdapter mDbHelper = null;

	public WLPriceChecker(WLAdapter adapter) {
		mAdapter = adapter;
	}

	/**
	 * Scans all the entries in the adapter and updates prices if the price
	 * information has changed
	 */
	public void priceCheck() {
		// Open up the SQLite database
		mDbHelper = new WLDbAdapter(mAdapter.getContext()
				.getApplicationContext());
		mDbHelper.open();

		new WLPriceCheck().execute("");
	}

	// For lack of a better method, stolen right from ShareActivity
	// TODO: Find a better method.
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

	private class WLPriceCheck extends AsyncTask<String, Integer, String> {
		private boolean items_changed = false;

		@Override
		protected String doInBackground(String... params) {
			// Loop through each entry in the list, scrape the data and update
			// the price if necessary
			for (int i = 0; i < mAdapter.getCount(); ++i) {
				// Grab the text from the url
				String url = mAdapter.getItem(i).getURL();
				String result = downloadURL(url);

				// Set up the patterns and corresponding matchers
				Pattern p_price = Pattern.compile("data-docPrice=\"(.*?)\"");
				Matcher m_price = p_price.matcher(result);
				m_price.find();

				// Set our variables with the retrieved information
				float price = 0.0f;
				if (!m_price.group(1).equals("Free")) {
					price = Float.valueOf(m_price.group(1).substring(1));
				}

				// Check if these values are different than the existing values
				if (price != mAdapter.getItem(i).getCurrentPrice()) {
					mAdapter.getItem(i).setCurrentPrice(price);
					if (price > mAdapter.getItem(i).getRegularPrice()) {
						mAdapter.getItem(i).setRegularPrice(price);
					}
					onProgressUpdate(i);
					items_changed = true;
				}
			}

			// Successful exit
			return "Success";
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// Update the updated item (progress update is only called if an
			// entry's information is changed)
			mDbHelper.updateEntry(mAdapter.getItem(values[0]).getId(),
					mAdapter.getItem(values[0]));
		}

		@Override
		protected void onPostExecute(String result) {// If the string returned
														// is null, we
														// encountered an error
			if (items_changed) {
				mAdapter.notifyDataSetChanged();
			}
			// Be sure to close the dbhelper
			mDbHelper.close();
		}
	}
}
