package com.brstf.wishlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	private ArrayList<String> mPending = null;
	private ArrayList<WLEntry> mEntries = null;
	private List<String> mTags = null;
	private HashMap<String, ArrayList<Integer>> mTagMap = null;
	private WLDbAdapter mDbHelper = null;
	private Context mCtx = null;
	private WLChangedListener mCallback = null;
	public static final String WL_PENDING = "PENDING";

	public interface WLChangedListener {
		/**
		 * This callback is triggered when the WLEntries data set is changed
		 */
		public void onDataSetChanged();
	}

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

	/**
	 * Sets the DataSetChangedListener of this WLEntries instance
	 * 
	 * @param dscl
	 *            New DataSetChangedListener for the WLEntries instance
	 */
	public void setWLChangedListener(WLChangedListener dscl) {
		mCallback = dscl;
	}

	/**
	 * Retrieves the context of the WLEntries instance
	 * 
	 * @return Context of the WLEntries instance
	 */
	public Context getContext() {
		return mCtx;
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

		fillPendingEntres();

		if (mCallback != null) {
			// Notify about the changed dataset
			mCallback.onDataSetChanged();
		}
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
		String fileName = uEnt.getTitle() + ".png";
		fileName = fileName.replaceAll("[/\\\\?\\.<>$]", "");
		if (mEntries.get(index).getIconPath().equals("")
				|| !(new File(fileName).exists())) {
			// Download the icon if necessary
			try {
				// Create a FileOutputStream and write the image to file
				FileOutputStream fos = mCtx.openFileOutput(fileName,
						Context.MODE_PRIVATE);
				Bitmap bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(uEnt.getIconUrl())
								.getContent());
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();

				mEntries.get(index).setIconPath(fileName);
			} catch (IOException e) {
				Toast.makeText(mCtx,
						"Failed to download icon for " + uEnt.getTitle(),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Function to add a "Pending" entry to the list. This is added to the
	 * pending list of entries, so that while an entry is constructed, and
	 * before it is added to the list of entries, a duplicate entry cannot be
	 * added. Additionally, while the device is offline, pending entries can be
	 * stored, and details will be filled in when the device comes back online.
	 * 
	 * @param url
	 *            Url of the entry to add to the pending list of entries
	 */
	private synchronized void addPendingEntry(String url) {
		mPending.add(url);
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

		// Write the pending list out to a file
		String pending = getPendingString();
		try {
			FileOutputStream fos = mCtx.openFileOutput("PENDING",
					Context.MODE_PRIVATE);
			fos.write(pending.getBytes());
			fos.close();
		} catch (IOException e) {
			Toast.makeText(mCtx, "Failed to save Pending entries",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Private function to fill pending entries from the PENDING file (if it
	 * exists).
	 */
	private void fillPendingEntres() {
		// If the pending list hasn't been initialized, create it
		if (mPending == null) {
			mPending = new ArrayList<String>();
		}

		File pFile = new File("PENDING");
		if (pFile.exists()) {
			try {
				FileInputStream fis = mCtx.openFileInput("PENDING");
				byte[] data = new byte[fis.available()];
				fis.read(data);
				String pending = new String(data);

				for (String s : pending.split("\n")) {
					addPendingEntry(s);
				}
			} catch (IOException e) {
				Toast.makeText(mCtx, "Failed to read in Pending entries",
						Toast.LENGTH_SHORT).show();
			}
		}

		clearPending();
	}

	/**
	 * Function to attempt to clear all pending entries from the pending list
	 */
	public synchronized void clearPending() {
		// If there are still pending entries, continue trying to clear them
		if (getNumPendingEntries() > 0 && isNetworkAvailable()) {
			new WLAddEntry().execute(mPending.get(0));
		}
	}

	/**
	 * Gets a comma separated list of all pending urls
	 * 
	 * @return
	 */
	public String getPendingString() {
		StringBuilder pending = new StringBuilder("");
		for (String p : mPending) {
			pending.append(p + "\n");
		}
		return pending.toString();
	}

	/**
	 * Retrieves the list of pending entries
	 * 
	 * @return ArrayList of pending entry urls
	 */
	public List<String> getPendingEntries() {
		return mPending;
	}

	/**
	 * Retrieves the number of pending entries that haven't yet been added to
	 * the list
	 * 
	 * @return Number of pending entries
	 */
	public int getNumPendingEntries() {
		return mPending.size();
	}

	/**
	 * Removes a pending entry with the given url
	 * 
	 * @param url
	 *            Url of the pending entry to remove
	 */
	public void removePendingEntry(String url) {
		mPending.remove(url);
	}

	/**
	 * Function to check whether a url is contained in this list of entries or
	 * not. If it is not contained, it is added to the pending list.
	 * 
	 * @param url
	 *            Url to check presence of
	 * @return Name of the entry in the list if it's found, WL_PENDING if this
	 *         url is pending addition, and null if the url was not found
	 */
	public synchronized String addPending(String url) {
		// First check entries
		for (WLEntry ent : mEntries) {
			if (ent.getURL().equals(url)) {
				return ent.getTitle();
			}
		}

		// Next check pending entries
		for (String p : mPending) {
			if (p.equals(url)) {
				return WL_PENDING;
			}
		}

		addPendingEntry(url);

		// If we did not encounter it, return null
		return null;
	}

	/**
	 * Private function to determine whether or not an internet connection is
	 * available
	 * 
	 * @return True if the internet is reachable, false otherwise
	 */
	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mCtx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
	
	/**
	 * Capitalizes the first letter of the tag and returns it
	 * @param tag String tag to convert
	 * @return Tag with the first letter capitalized (e.g. 'apps' -> 'Apps')
	 */
	public static String getDisplayTag(String tag) {
		if(tag == null) {
			throw new IllegalArgumentException("tag cannot be null");
		}
		return tag.substring(0, 1).toUpperCase() + tag.substring(1);
	}
}
