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
public final class WLEntries {
	private ArrayList<WLEntry> mEntries = null;
	private ArrayList<String> mTags = null;
	private HashMap<String, ArrayList<Integer>> mTagMap = null;
	private WLDbAdapter mDbHelper = null;

	public static WLEntries getInstance() {
		return mInstance;
	}

	/**
	 * This must be called before any operations are done, the database helper
	 * needs the application context
	 * 
	 * @param ctx
	 *            application context this WLEntries instance is running in
	 */
	public void setContext(Context ctx) {
		if (mDbHelper == null) {
			// Open up the SQLite database and fill data
			mDbHelper = new WLDbAdapter(ctx);
			fillEntries();
		}
	}

	private static final WLEntries mInstance = new WLEntries();

	/**
	 * Constructs the WLEntries instance, reads in all entries and constructs
	 * all useful member collections.
	 * 
	 * @param ctx
	 *            The application context of the initializing activity of the
	 *            WLEntries object
	 */
	private WLEntries() {
		mEntries = new ArrayList<WLEntry>();
		mTags = new ArrayList<String>();
		mTagMap = new HashMap<String, ArrayList<Integer>>();
	}

	/**
	 * Function to retrieve the ArrayList full of all entries
	 * 
	 * @return An arrayList of all entries in the database
	 */
	public ArrayList<WLEntry> getEntries() {
		return mEntries;
	}

	/**
	 * Function to return a subset of the Entries in the list filtered by which
	 * element has the given tag.
	 * 
	 * @param tag
	 *            String tag to filter the entries by
	 * @return An ArrayList of entries that have the given tag
	 */
	public ArrayList<WLEntry> getEntries(String tag) {
		if(tag == null) {
			return getEntries();
		}
		
		tag = tag.toLowerCase();
		ArrayList<WLEntry> filteredList = new ArrayList<WLEntry>();
		for (WLEntry ent : mEntries) {
			if (ent.isTagged(tag)) {
				filteredList.add(ent);
			}
		}
		return filteredList;
	}

	/**
	 * Function to return a subset of the passed in entries in the list filtered
	 * by the given tag
	 * 
	 * @param ents
	 *            List of entries to filter
	 * @param tag
	 *            Tag to filter the entries by
	 * @return An ArrayList of entries that were in ents and have the given tag
	 */
	public static ArrayList<WLEntry> getEntries(ArrayList<WLEntry> ents, String tag) {
		tag = tag.toLowerCase();
		final ArrayList<WLEntry> filteredList = new ArrayList<WLEntry>();
		for (WLEntry ent : ents) {
			if (ent.isTagged(tag)) {
				filteredList.add(ent);
			}
		}
		return filteredList;
	}

	/**
	 * Function to wipe and reload all data
	 */
	public void reload() {
		mEntries.clear();
		mTags.clear();
		mTagMap.clear();
		if (mDbHelper != null) {
			fillEntries();
		}
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

			// Continue moving through the entries
			c.moveToNext();
		}

		// Close up the database
		mDbHelper.close();
	}

}
