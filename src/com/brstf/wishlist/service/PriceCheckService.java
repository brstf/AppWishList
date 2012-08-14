package com.brstf.wishlist.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.entries.EntryType;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLEntryContract.EntriesQuery;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;
import com.brstf.wishlist.util.NetworkUtils;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
				Log.d(TAG, c.getString(EntriesQuery.COLUMN_NAME));
			} else if (EntryType.isMultiPricedEntry(type)) {

			}

			c.moveToNext();
		}
	}
}
