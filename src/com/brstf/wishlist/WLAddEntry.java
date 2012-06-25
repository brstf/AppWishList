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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;

/**
 * AsyncTask to create a WLEntry and add it to the wish list
 * 
 * @author brstf
 */
public class WLAddEntry extends AsyncTask<String, String, String> {
	// The URL and app entry
	private String url;
	private WLEntry ent = null;

	/**
	 * Method to download a web page's text from a given URL. Used by the
	 * private class WLAddEntry to add app info to the list
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

	@Override
	protected String doInBackground(String... params) {
		WLEntries entries = WLEntries.getInstance();
		// Try to download the Play Store text and scrape all relevant
		// information
		try {
			// Grab the text from the url
			url = params[0];
			String result = downloadURL(url);

			ent = WLEntryType.getTypeEntry(WLEntryType.getTypeFromURL(url), -1);

			ent.setFromURLText(url, result);

			// Create a FileOutputStream and write the image to file
			String fileName = ent.getTitle() + ".png";
			fileName = fileName.replaceAll("[/\\\\?\\.<>$]", "");
			FileOutputStream fos = entries.getContext().openFileOutput(
					fileName, Context.MODE_PRIVATE);
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
					ent.getIconUrl()).getContent());
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();

			ent.setIconPath(fileName);

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
		WLEntries entries = WLEntries.getInstance();
		// If the string returned is null, we encountered an error
		if (result == null) {
			// So show a notification indicating failure:
			Toast.makeText(entries.getContext(),
					"Failed to add app to wishlist.", Toast.LENGTH_SHORT)
					.show();
		} else {
			WLDbAdapter mDbHelper = new WLDbAdapter(entries.getContext());
			mDbHelper.open();
			// Otherwise, add the entry to the list
			mDbHelper.createEntry(ent);

			// Show that the app was successfully added to the wishlist
			Toast.makeText(entries.getContext(),
					"Added " + ent.getTitle() + " to wishlist!",
					Toast.LENGTH_SHORT).show();

			// Finally, close the db helper
			mDbHelper.close();

			entries.removePendingEntry(url);
			entries.reload();
		}
	}
}