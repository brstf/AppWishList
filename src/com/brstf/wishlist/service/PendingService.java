package com.brstf.wishlist.service;

import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

public class PendingService extends IntentService {
	private static final String TAG = "PendingService";
	private WLDbAdapter mDbHelper = null;

	public PendingService() {
		super(TAG);
	}

	public void onCreate() {
		super.onCreate();

		// Create the database adapter that this service will use
		mDbHelper = new WLDbAdapter(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String[] columns = WLEntryContract.PendingQuery.columns;
		String selection = EntryColumns.KEY_TYPE + " == ?";
		String[] selectionArgs = { WLEntryType
				.getTypeString(WLEntryType.PENDING) };

		// Query the database for all pending entries
		mDbHelper.open();

		try {
			Cursor c = mDbHelper.query(true, columns, selection, selectionArgs,
					null, null, null, null);

			// Loop through each element returned by the cursor and attempt to
			// add it to the wishlist
			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					// Get the url of the entry
					String url = c.getString(c
							.getColumnIndex(EntryColumns.KEY_URL));

					final Intent addIntent = new Intent(this,
							AddEntryService.class);
					addIntent.putExtra(AddEntryService.EXTRA_URL, url);
					startService(addIntent);

					c.moveToNext();
				}
			}
		} catch (Exception e) {
		} finally {
			mDbHelper.close();
		}
	}

}
