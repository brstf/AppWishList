package com.brstf.wishlist.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.entries.EntryType;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLEntryContract.EntriesQuery;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;
import com.brstf.wishlist.util.NetworkUtils;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PriceCheckService extends IntentService {
	private static final String TAG = "PriceCheckService";

	public PriceCheckService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Receive intent

		Log.d(TAG, "PriceCheckServiceStarted");
		// If intent contains a RESCHEDULE flag, reschedule the service

		// Query for all entries
		Cursor c = getContentResolver().query(
				WLEntryContract.Entries.CONTENT_URI,
				WLEntryContract.EntriesQuery.columns, null, null, null);
		c.moveToFirst();
		int numNotifications = 0;
		
		// Loop through each entry
		while (!c.isAfterLast()) {
			EntryType type = EntryType.getTypeFromString(c
					.getString(EntriesQuery.COLUMN_TYPE));
			// If this is a priced entry, do price checking
			if (EntryType.isSinglePricedEntry(type)) {
				String url = c.getString(EntriesQuery.COLUMN_URL);
				String text = NetworkUtils.downloadURL(url);

				// Retrieve current price of entry
				Pattern p_price = Pattern.compile(EntryType
						.getPricePattern(type));
				Matcher m_price = p_price.matcher(text);
				m_price.find();

				float curPrice = 0.0f;
				// Set current price of entry and update "Sale" status
				if (!m_price.group(1).equals("Free")) {
					curPrice = Float.valueOf(m_price.group(1).substring(1));
				}

				// Update the database
				ContentValues values = new ContentValues();
				values.put(EntryColumns.KEY_CUR_PRICE_1, curPrice);
				getContentResolver().update(
						WLEntryContract.Entries.buildEntryUri(url), values,
						null, null);

				// Notify the user of a price drop
				float oldPrice = c.getFloat(EntriesQuery.COLUMN_REG_PRICE_1);
				if (oldPrice != curPrice) {
					Log.d(TAG, "Sale! " + c.getString(EntriesQuery.COLUMN_NAME));
					NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

					// Construct the notification to send
					Notification notification = new Notification(
							android.R.drawable.alert_dark_frame,
							c.getString(EntriesQuery.COLUMN_NAME)
									+ " is on sale!",
							System.currentTimeMillis());
					CharSequence nTitle = c.getString(EntriesQuery.COLUMN_NAME)
							+ " is on sale!";
					CharSequence nText = c.getString(EntriesQuery.COLUMN_NAME)
							+ " is now $" + String.format("%.2f", curPrice)
							+ " (was $" + String.format("%.2f", oldPrice) + ")";

					// Setup the intent
					Intent nIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(url));
					PendingIntent contentIntent = PendingIntent.getActivity(
							this, 0, nIntent, 0);
					notification.setLatestEventInfo(getApplicationContext(),
							nTitle, nText, contentIntent);

					// Send the notification
					nm.notify(numNotifications++, notification);
				}
				Log.d(TAG, c.getString(EntriesQuery.COLUMN_NAME));
			} else if (EntryType.isMultiPricedEntry(type)) {

			}

			c.moveToNext();
		}
	}
}
