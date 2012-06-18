package com.brstf.wishlist;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;

/**
 * WLEntries object is an object that loads in and manages all entries of the
 * wish list. Supports useful functions like get all entries, get all tags,
 * filter by tags, etc.
 */
public class WLEntries {
	private ArrayList<WLEntry> mEntries = null;
	private ArrayList<String> mTags = null;
	private HashMap<String, ArrayList<Integer>> mTagMap = null;
	private WLDbAdapter mDbHelper = null;

	/**
	 * Constructs the WLEntries instance, reads in all entries and constructs
	 * all useful member collections
	 * 
	 * @param ctx
	 *            The application context of the initializing activity of the
	 *            WLEntries object
	 */
	public WLEntries(Context ctx) {
		mEntries = new ArrayList<WLEntry>();
		mTags = new ArrayList<String>();
		mTagMap = new HashMap<String, ArrayList<Integer>>();

		// Open up the SQLite database
		mDbHelper = new WLDbAdapter(ctx);
		fillEntries();
	}

	/**
	 * Method to initialize and fill in all member collections
	 */
	private void fillEntries() {
		// Open the database to obtain entries
		mDbHelper.open();

		// Fetch all entries from the database
		Cursor c = mDbHelper.fetchAllEntries();
		c.moveToFirst();

		// Loop through each entry
		while (!c.isAfterLast()) {
			// Get the type of the entry at c
			WLEntryType type = WLEntryType.getTypeFromString(c.getString(1));

			// Construct the entry of the given type and set its values
			WLEntry ent = WLEntryType.getTypeEntry(type, c.getInt(0));
			ent.setFromDb(c);

			// Add it to the list
			mEntries.add(ent);
			
			//Continue moving through the entries
			c.moveToNext();
		}

		// Close up the database
		mDbHelper.close();
	}
	
	
}
