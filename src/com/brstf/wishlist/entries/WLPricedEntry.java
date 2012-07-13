package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.provider.WLDbAdapter;

public abstract class WLPricedEntry extends WLEntry {
	private float cPrice = -1.0f;
	private float rPrice = -1.0f;
	private float rating = 0.0f;

	public WLPricedEntry(int id) {
		super(id);
	}

	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the patterns and corresponding matchers
		Pattern p_price = Pattern.compile(getPricePattern());
		Pattern p_rating = Pattern.compile(getRatingPattern());

		Matcher m_price = p_price.matcher(text);
		Matcher m_rating = p_rating.matcher(text);

		// Find the patterns
		m_price.find();
		m_rating.find();

		// Set our variables with the retrieved information
		if (m_price.group(1).equals("Free")) {
			setRegularPrice(0.0f);
		} else {
			setRegularPrice(Float.valueOf(m_price.group(1).substring(1)));
		}
		
		if (m_rating.find()) {
			setRating(Float.parseFloat(m_rating.group(1)));
		} else {
			setRating(0.0f);
		}
	}

	protected abstract String getPricePattern();

	/**
	 * Class method to retrieve the regular expression pattern to find the
	 * rating of this entry
	 * 
	 * @return Regular expression pattern that finds the rating of this entry
	 */
	protected String getRatingPattern() {
		return "class=\"doc-details-ratings-price\".*?Rating: (.*?) stars";
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
		setCurrentPrice(c.getFloat(c.getColumnIndex(WLDbAdapter.KEY_CPRICE)));
		setRegularPrice(c.getFloat(c.getColumnIndex(WLDbAdapter.KEY_RPRICE)));
		setRating(c.getFloat(c.getColumnIndex(WLDbAdapter.KEY_RATING)));
	}

	/**
	 * Retrieves the current price of the entry
	 * 
	 * @return The current price of the entry
	 */
	public float getCurrentPrice() {
		return cPrice;
	}

	/**
	 * Retrieves the regular price of the entry
	 * 
	 * @return The regular price of the entry
	 */
	public float getRegularPrice() {
		return rPrice;
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
	 * Returns whether or not this entry is on sale
	 * 
	 * @return True if the item is on sale, false otherwise
	 */
	public boolean isOnSale() {
		return getCurrentPrice() != getRegularPrice();
	}

	/**
	 * Sets the current price of this entry to the given price
	 * 
	 * @param price
	 *            The new current price of the entry
	 */
	public void setCurrentPrice(float price) {
		// Ensure a valid price
		if (price < 0.0f) {
			System.err.println("Negative Price: " + price);
			return;
		}

		// if this price is higher than our regular price, set a new regular
		// price
		if (price > rPrice) {
			setRegularPrice(price);
		}
		cPrice = price;
	}

	/**
	 * Sets the regular price of the entry to the given price
	 * 
	 * @param price
	 *            The new regular price of the entry
	 */
	public void setRegularPrice(float price) {
		// Ensure a valid price
		if (price < 0.0f) {
			System.err.println("Negative Price: " + price);
			return;
		}

		rPrice = price;

		if (cPrice == -1.0f) {
			setCurrentPrice(price);
		}
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
