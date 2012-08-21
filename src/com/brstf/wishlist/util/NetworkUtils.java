package com.brstf.wishlist.util;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.brstf.wishlist.service.PriceCheckService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

	/**
	 * Public function to determine whether or not an internet connection is
	 * available.
	 * 
	 * @param context
	 *            Application context to test network connectivity
	 * @return True if the internet is reachable, false otherwise
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	/**
	 * Public function to determine whether or not wifi is connected.
	 * 
	 * @param context
	 *            Application context to test network connectivity
	 * @return True if WiFi is reachable, false otherwise
	 */
	public static boolean isWifiAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return activeNetworkInfo.isConnected();
	}

	/**
	 * Schedules a new price check for the current time, repeating every
	 * {@link interval} milliseconds, and updates the {@link SharedPreferences}
	 * accordingly.
	 * 
	 * @param baseContext
	 *            {@link Context} that is calling this method
	 * @param interval
	 *            long interval (in milliseconds) between price checks
	 * @param prefs
	 *            {@link SharedPreferences} to update with scheduled time
	 * @param lastChecked
	 *            {@link String} of shared preference entry to update
	 */
	public static void schedulePriceCheck(Context baseContext, long interval,
			SharedPreferences prefs, String lastChecked) {
		schedulePriceCheck(baseContext, System.currentTimeMillis(), interval,
				prefs, lastChecked);
	}

	/**
	 * Schedules a new price check for the current time, repeating every
	 * {@link interval} milliseconds, and updates the {@link SharedPreferences}
	 * accordingly.
	 * 
	 * @param baseContext
	 *            {@link Context} that is calling this method
	 * @param time
	 *            Time (in milliseconds) to schedule the next price check for
	 * @param interval
	 *            long interval (in milliseconds) between price checks
	 * @param prefs
	 *            {@link SharedPreferences} to update with scheduled time
	 * @param lastChecked
	 *            {@link String} of shared preference entry to update
	 */
	public static void schedulePriceCheck(Context baseContext, long time,
			long interval, SharedPreferences prefs, String lastChecked) {
		// Schedule price checking updates
		PendingIntent pintent = PendingIntent.getService(baseContext, 0,
				new Intent(baseContext, PriceCheckService.class),
				PendingIntent.FLAG_UPDATE_CURRENT);
		((AlarmManager) baseContext.getSystemService(Context.ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.RTC, time, interval, pintent);

		// Update last checked time to current time
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(lastChecked, System.currentTimeMillis());
		editor.commit();
	}
}
