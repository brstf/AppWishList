package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;

import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;

public abstract class SinglePricedEntry extends RatedEntry {
	private float cPrice = -1.0f;
	private float rPrice = -1.0f;

	public SinglePricedEntry(int id) {
		super(id);
	}

	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the pattern and corresponding matcher
		Pattern p_price = Pattern.compile(getPricePattern());
		Matcher m_price = p_price.matcher(text);

		// Find the pattern
		m_price.find();

		// Set our variables with the retrieved information
		if (m_price.group(1).equals("Free")) {
			setRegularPrice(0.0f);
		} else {
			setRegularPrice(Float.valueOf(m_price.group(1).substring(1)));
		}
	}

	public abstract String getPricePattern();

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
		setCurrentPrice(c.getFloat(c.getColumnIndex(EntryColumns.KEY_CUR_PRICE_1)));
		setRegularPrice(c.getFloat(c.getColumnIndex(EntryColumns.KEY_REG_PRICE_1)));
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
}
