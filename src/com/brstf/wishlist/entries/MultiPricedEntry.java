package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;

import android.database.Cursor;

public abstract class MultiPricedEntry extends RatedEntry {
	private float cPrice1 = -1.0f;
	private float rPrice1 = -1.0f;
	private float cPrice2 = -1.0f;
	private float rPrice2 = -1.0f;
	private float cPrice3 = -1.0f;
	private float rPrice3 = -1.0f;
	private float cPrice4 = -1.0f;
	private float rPrice4 = -1.0f;

	public MultiPricedEntry(int id) {
		super(id);
	}

	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the patterns and corresponding matchers
		Pattern p_price_1 = Pattern.compile(getPricePattern1());
		Pattern p_price_2 = Pattern.compile(getPricePattern2());
		Pattern p_price_3 = Pattern.compile(getPricePattern3());
		Pattern p_price_4 = Pattern.compile(getPricePattern4());

		Matcher m_price_1 = p_price_1.matcher(text);
		Matcher m_price_2 = p_price_2.matcher(text);
		Matcher m_price_3 = p_price_3.matcher(text);
		Matcher m_price_4 = p_price_4.matcher(text);

		// Find each pattern, and set each variable if found

		// PRICE_1
		if (m_price_1.find()) {
			if (m_price_1.group(1).equals("Free")) {
				setRegularPrice1(0.0f);
			} else {
				setRegularPrice1(Float.valueOf(m_price_1.group(1).substring(1)));
			}
		}

		// PRICE_2
		if (m_price_2.find()) {
			if (m_price_2.group(1).equals("Free")) {
				setRegularPrice2(0.0f);
			} else {
				setRegularPrice2(Float.valueOf(m_price_2.group(1).substring(1)));
			}
		}

		// PRICE_3
		if (m_price_3.find()) {
			if (m_price_3.group(1).equals("Free")) {
				setRegularPrice3(0.0f);
			} else {
				setRegularPrice3(Float.valueOf(m_price_3.group(1).substring(1)));
			}
		}

		// PRICE_4
		if (m_price_4.find()) {
			if (m_price_4.group(1).equals("Free")) {
				setRegularPrice4(0.0f);
			} else {
				setRegularPrice4(Float.valueOf(m_price_4.group(1).substring(1)));
			}
		}
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
		setCurrentPrice1(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_CUR_PRICE_1)));
		setCurrentPrice2(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_CUR_PRICE_2)));
		setCurrentPrice3(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_CUR_PRICE_3)));
		setCurrentPrice4(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_CUR_PRICE_4)));
		setRegularPrice1(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_REG_PRICE_1)));
		setRegularPrice2(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_REG_PRICE_2)));
		setRegularPrice3(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_REG_PRICE_3)));
		setRegularPrice4(c.getFloat(c
				.getColumnIndex(EntryColumns.KEY_REG_PRICE_4)));
	}

	protected abstract String getPricePattern1();

	protected abstract String getPricePattern2();

	protected abstract String getPricePattern3();

	protected abstract String getPricePattern4();

	/**
	 * Retrieves the current price_1 from the entry (Movie Rental SD price, TV
	 * Season Price SD, Magazine Issue Price).
	 * 
	 * @return Current price_1 of the entry
	 */
	public float getCurrentPrice1() {
		return cPrice1;
	}

	/**
	 * Retrieves the current price_2 from the entry (Movie Rental HD price, TV
	 * Season Price HD, Magazine Monthly Subscription Price).
	 * 
	 * @return Current price_2 of the entry
	 */
	public float getCurrentPrice2() {
		return cPrice2;
	}

	/**
	 * Retrieves the current price_3 from the entry (Movie Purchase SD price,
	 * Magazine Annual Subscription Price).
	 * 
	 * @return Current price_3 of the entry
	 */
	public float getCurrentPrice3() {
		return cPrice3;
	}

	/**
	 * Retrieves the current price_4 from the entry (Movie Purchase HD Price).
	 * 
	 * @return Current price_4 of the entry
	 */
	public float getCurrentPrice4() {
		return cPrice4;
	}

	/**
	 * Retrieves the regular price_1 from the entry (Movie Rental SD price, TV
	 * Season Price SD, Magazine Issue Price).
	 * 
	 * @return Regular price_1 of the entry
	 */
	public float getRegularPrice1() {
		return rPrice1;
	}

	/**
	 * Retrieves the regular price_2 from the entry (Movie Rental HD price, TV
	 * Season Price HD, Magazine Monthly Subscription Price).
	 * 
	 * @return Regular price_2 of the entry
	 */
	public float getRegularPrice2() {
		return rPrice2;
	}

	/**
	 * Retrieves the regular price_3 from the entry (Movie Purchase SD price,
	 * Magazine Annual Subscription Price).
	 * 
	 * @return Regular price_3 of the entry
	 */
	public float getRegularPrice3() {
		return rPrice3;
	}

	/**
	 * Retrieves the regular price_4 from the entry (Movie Purchase HD Price).
	 * 
	 * @return Regular price_4 of the entry
	 */
	public float getRegularPrice4() {
		return rPrice4;
	}

	/**
	 * Sets the current price_1 of this entry to the given price.
	 * 
	 * @param price
	 *            New current price_1 of the entry
	 */
	public void setCurrentPrice1(float price) {
		if (price > rPrice1) {
			setRegularPrice1(price);
		}
		cPrice1 = price;
	}

	/**
	 * Sets the current price_2 of this entry to the given price.
	 * 
	 * @param price
	 *            New current price_12of the entry
	 */
	public void setCurrentPrice2(float price) {
		if (price > rPrice2) {
			setRegularPrice2(price);
		}
		cPrice2 = price;
	}

	/**
	 * Sets the current price_3 of this entry to the given price.
	 * 
	 * @param price
	 *            New current price_3 of the entry
	 */
	public void setCurrentPrice3(float price) {
		if (price > rPrice3) {
			setRegularPrice3(price);
		}
		cPrice3 = price;
	}

	/**
	 * Sets the current price_4 of this entry to the given price.
	 * 
	 * @param price
	 *            New current price_4 of the entry
	 */
	public void setCurrentPrice4(float price) {
		if (price > rPrice4) {
			setRegularPrice4(price);
		}
		cPrice4 = price;
	}

	/**
	 * Sets the regular price_1 of this entry to the given price.
	 * 
	 * @param price
	 *            New regular price_1 of the entry
	 */
	public void setRegularPrice1(float price) {
		rPrice1 = price;

		if (cPrice1 < 0.0f) {
			setCurrentPrice1(price);
		}
	}

	/**
	 * Sets the regular price_2 of this entry to the given price.
	 * 
	 * @param price
	 *            New regular price_2 of the entry
	 */
	public void setRegularPrice2(float price) {
		rPrice2 = price;

		if (cPrice2 < 0.0f) {
			setCurrentPrice2(price);
		}
	}

	/**
	 * Sets the regular price_3 of this entry to the given price.
	 * 
	 * @param price
	 *            New regular price_3 of the entry
	 */
	public void setRegularPrice3(float price) {
		rPrice3 = price;

		if (cPrice3 < 0.0f) {
			setCurrentPrice3(price);
		}
	}

	/**
	 * Sets the regular price_4 of this entry to the given price.
	 * 
	 * @param price
	 *            New regular price_4 of the entry
	 */
	public void setRegularPrice4(float price) {
		rPrice4 = price;

		if (cPrice4 < 0.0f) {
			setCurrentPrice4(price);
		}
	}
}
