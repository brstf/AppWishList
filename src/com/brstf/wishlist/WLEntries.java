package com.brstf.wishlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.entries.WLPricedEntry;

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
	private Context mCtx = null;

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
			mCtx = ctx;
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
		if (tag == null) {
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
	 * Function to return all of the tags describing entries in the list
	 * 
	 * @return ArrayList of String tags of all tags
	 */
	public Set<String> getTags() {
		return mTagMap.keySet();
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
	public static ArrayList<WLEntry> getEntries(ArrayList<WLEntry> ents,
			String tag) {
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

		// Index of entry
		int i = 0;

		// Loop through each entry
		while (!c.isAfterLast()) {
			// Get the type of the entry at c
			WLEntryType type = WLEntryType.getTypeFromString(c.getString(1));

			// Construct the entry of the given type and set its values
			WLEntry ent = WLEntryType.getTypeEntry(type, c.getInt(0));
			ent.setFromDb(c);

			// Add it to the list
			mEntries.add(ent);

			// Add all tags to the tag list
			for (String tag : ent.getTags()) {
				mTags.add(tag);

				if (!mTagMap.containsKey(tag)) {
					mTagMap.put(tag, new ArrayList<Integer>());
				}
				mTagMap.get(tag).add(i);
			}

			// Continue moving through the entries
			c.moveToNext();
		}

		// Close up the database
		mDbHelper.close();
	}

	public void updateEntry(int index, WLEntry uEnt) {
		// If this entry is a priced entry of some sort, update price and rating
		if (uEnt.getType() != WLEntryType.MUSIC_ARTIST) {
			((WLPricedEntry) mEntries.get(index))
					.setCurrentPrice(((WLPricedEntry) uEnt).getCurrentPrice());
			((WLPricedEntry) mEntries.get(index))
					.setRating(((WLPricedEntry) uEnt).getRating());
		}

		// If the icon path is empty or the icon got deleted somehow, download
		// it
		if (mEntries.get(index).getIconPath().equals("")
				|| !(new File(mEntries.get(index).getIconPath()).exists())) {
			// Download the icon if necessary
			try {
				// Create a FileOutputStream and write the image to file
				FileOutputStream fos = mCtx.openFileOutput(uEnt.getTitle()
						+ ".png", Context.MODE_PRIVATE);
				Bitmap bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(uEnt.getIconUrl())
								.getContent());
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();

				mEntries.get(index).setIconPath(uEnt.getTitle() + ".png");
			} catch (IOException e) {
				Toast.makeText(mCtx,
						"Failed to download icon for " + uEnt.getTitle(),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Function to update all entries in the database and notify any adapters
	 * that entries have changed
	 */
	public void writeToDb() {
		mDbHelper.open();
		for (WLEntry ent : mEntries) {
			mDbHelper.updateEntry(ent.getId(), ent);
		}
		mDbHelper.close();
	}
}
