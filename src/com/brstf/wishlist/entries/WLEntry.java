package com.brstf.wishlist.entries;

import java.util.HashSet;

import com.brstf.wishlist.WLDbAdapter;

import android.database.Cursor;

/**
 * Class for storing information about an entry into the wishlist Stores: - URL
 * to Google Play listing - The icon of the entry
 * 
 * @author brstf
 */

public abstract class WLEntry {
	private String mTitle; // Name of the app
	private String mURL; // URL link to Google Play Listing
	private String mIcon; // path to the icon file
	private int mDbId; // ID of the entry in the database
	private HashSet<String> mTags = null; // Set of tags "describing" this entry

	/**
	 * Default constructor, initiates all parameters to default meaningless
	 * values
	 */
	public WLEntry(int id) {
		// Initiate to default parameters
		mTitle = "";
		mURL = "";
		mIcon = "";
		mDbId = id;
		mTags = new HashSet<String>();
	}

	// Accessors

	/**
	 * Gets the name of the
	 * 
	 * @return The title of the stored in mName
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Gets the URL of the Google Play listing
	 * 
	 * @return The URL of the Google Play listing
	 */
	public String getURL() {
		return mURL;
	}

	/**
	 * Gets the path to the icon
	 * 
	 * @return The path to the icon
	 */
	public String getIconPath() {
		return mIcon;
	}

	/**
	 * Gets the database id of the entry
	 * 
	 * @return The id
	 */
	public int getId() {
		return mDbId;
	}

	/**
	 * Function that adds an additional tag to this entry
	 * 
	 * @param tag
	 *            Additional tag to add to the entry
	 */
	public void addTag(String tag) {
		mTags.add(tag.toLowerCase());
	}

	/**
	 * Gets an array of all tags describing the entry
	 * 
	 * @return String array containing all tags describing the entry
	 */
	public String[] getTags() {
		String[] tags = new String[mTags.size()];
		return mTags.toArray(tags);
	}

	/**
	 * Function to determine whether or not this entry is tagged with a
	 * particular tag
	 * 
	 * @param tag
	 *            String tag to test
	 * @return True if this entry is tagged with tag, false otherwise
	 */
	public boolean isTagged(String tag) {
		return mTags.contains(tag);
	}

	/**
	 * Function to remove a tag from this entry
	 * 
	 * @param tag
	 *            Tag to remove from the entry
	 * @return True if the tag was removed, false otherwise
	 */
	public boolean removeTag(String tag) {
		return mTags.remove(tag);
	}

	/**
	 * Gets the type of this entry (App, Book, Movie, etc.)
	 * 
	 * @return The WLEntryType enum corresponding to the type
	 */
	public abstract WLEntryType getType();

	/**
	 * Sets the member variables of this entry based on the web page text
	 * obtained from the given url
	 * 
	 * @param url
	 *            The url the text was obtained from
	 * @param text
	 *            The text obtained from the given url
	 */
	public abstract void setFromURLText(String url, String text);

	/**
	 * Function to set the member variables for a this particular entry from a
	 * pointer to a database entry
	 * 
	 * @param c
	 *            The cursor pointing to the database entry to set the data from
	 */
	public void setFromDb(Cursor c) {
		setTitle(c.getString(c.getColumnIndex(WLDbAdapter.KEY_NAME)));
		setURL(c.getString(c.getColumnIndex(WLDbAdapter.KEY_URL)));
		setIconPath(c.getString(c.getColumnIndex(WLDbAdapter.KEY_ICON)));

		// Add all tags
		String cseptags = c.getString(c.getColumnIndex(WLDbAdapter.KEY_TAGS));
		String[] tags = cseptags.split(",");
		for(String tag : tags) {
			addTag(tag);
		}
	}

	// Mutators

	/**
	 * Sets the title of this entry
	 * 
	 * @param title
	 *            The new title to assign to the entry
	 */
	public void setTitle(String title) {
		mTitle = title;
	}

	/**
	 * Sets the URL of this entry. Assignment can only be done once.
	 * 
	 * @param url
	 *            The URL to assign to this entry
	 * @return Whether or not the assignment was successful
	 */
	public boolean setURL(String url) {
		// Can only set URL once, after that point, all changes should come from
		// changes
		// in the Play store. This shouldn't come up, but just to be safe
		// prevent it
		if (mURL.equals("")) {
			mURL = url;
			return true;
		}
		return false;
	}

	/**
	 * Sets the icon path of this entry
	 * 
	 * @param icon
	 *            The new icon path of the entry
	 */
	public void setIconPath(String icon) {
		mIcon = icon;
	}
}
