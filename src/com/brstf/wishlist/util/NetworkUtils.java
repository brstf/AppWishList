package com.brstf.wishlist.util;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class NetworkUtils {
	/**
	 * Method to download a web page's text from a given URL. Used by the
	 * private class WLAddApp to add app info to the list
	 * 
	 * @param myurl
	 *            The URL of the web page to download
	 * @return The text of the webpage at the given URL
	 */
	public static String downloadURL(String myurl) {
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
}
