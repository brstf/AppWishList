package com.brstf.wishlist.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.entries.Entry;
import com.brstf.wishlist.entries.EntryType;
import com.brstf.wishlist.entries.SinglePricedEntry;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLProvider;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;
import com.brstf.wishlist.util.NetworkUtils;
import com.brstf.wishlist.util.ProviderUtils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;
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
			// Get the entry from the cursor
			Entry ent = EntryType.getTypeEntry(EntryType.getTypeFromString(c
					.getString(c.getColumnIndex(EntryColumns.KEY_TYPE))), c
					.getInt(c.getColumnIndex(BaseColumns._ID)));
			ent.setFromDb(c);

			// If this is a priced entry, do price checking
			if (EntryType.isSinglePricedEntry(ent.getType())) {
				SinglePricedEntry spent = (SinglePricedEntry) ent;

				String text = NetworkUtils.downloadURL(spent.getURL());

				// Retrieve current price of entry
				Pattern p_price = Pattern.compile(spent.getPricePattern());
				Matcher m_price = p_price.matcher(text);
				m_price.find();

				// Set current price of entry and update "Sale" status
				if (m_price.group(1).equals("Free")) {
					spent.setCurrentPrice(0.0f);
				} else {
					spent.setCurrentPrice(Float.valueOf(m_price.group(1)
							.substring(1)));
				}
				if (spent.isOnSale()) {
					spent.addTag("sale");
				} else {
					spent.removeTag("sale");
				}

				ProviderUtils.update(getContentResolver(), spent);
				Log.d(TAG, spent.getTitle());
			} else if (EntryType.isMultiPricedEntry(ent.getType())) {

			}
			
			c.moveToNext();
		}
	}

}
