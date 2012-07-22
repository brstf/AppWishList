package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;

import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;

public abstract class RatedEntry extends Entry {
	private float rating = 0.0f;

	public RatedEntry(int id) {
		super(id);
	}

	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the pattern and corresponding matcher
		Pattern p_rating = Pattern.compile(getRatingPattern());
		Matcher m_rating = p_rating.matcher(text);

		// Find the pattern
		m_rating.find();

		// Set our variable with the retrieved information
		if (m_rating.find()) {
			setRating(Float.parseFloat(m_rating.group(1)));
		} else {
			setRating(0.0f);
		}
	}
	
	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
		setRating(c.getFloat(c.getColumnIndex(EntryColumns.KEY_RATING)));
	}

	/**
	 * Class method to retrieve the regular expression pattern to find the
	 * rating of this entry
	 * 
	 * @return Regular expression pattern that finds the rating of this entry
	 */
	protected String getRatingPattern() {
		return "class=\"doc-details-ratings-price\".*?Rating: (.*?) stars";
	}
	
	/**
	 * Retrieves the rating of the entry
	 * 
	 * @return The current rating of the entry
	 */
	public float getRating() {
		return rating;
	}
	
	/**
	 * Sets the new rating of the entry
	 * 
	 * @param nRating
	 *            The new rating of the entry
	 */
	public void setRating(float nRating) {
		// Ensure a valid price
		if (nRating < 0.0f || nRating > 5.0f) {
			System.err.println("Invalid Rating: " + nRating);
			return;
		}

		rating = nRating;
	}
}
