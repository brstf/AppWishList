package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;

import android.database.Cursor;

public class MagazineEntry extends MultiPricedEntry {
	private String mCategory = null;

	public MagazineEntry(int id) {
		super(id);

		mCategory = "";
	}

	@Override
	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set the pattern and corresponding matcher
		Pattern p_category = Pattern
				.compile("<a href=\"/store/magazines/category/.*?\">(.*?)<");

		Matcher m_category = p_category.matcher(text);

		// Find the pattern
		if (m_category.find()) {
			setCategory(android.text.Html.fromHtml(m_category.group(1))
					.toString());
		} else {
			setCategory("No category");
		}
	}

	@Override
	protected String getPricePattern1() {
		// Issue Price
		return getBasePatternPrice() + "Current issue\".*?<div class=\"clear\"";
	}

	@Override
	protected String getPricePattern2() {
		// Monthly Subscription Price
		return getBasePatternPrice()
				+ "Monthly subscription with free trial\".*?<div class=\"clear\"";
	}

	@Override
	protected String getPricePattern3() {
		// Annual Subscription Price
		return getBasePatternPrice()
				+ "Yearly subscription with free trial\".*?<div class=\"clear\"";
	}

	@Override
	protected String getPricePattern4() {
		// For Magazines, we only need 3 prices
		return "$.";
	}

	private String getBasePatternPrice() {
		return "data-docPrice=\"(.{1,10})\" data-docPriceMicros=\".{1,14}\""
				+ " data-isFree=\".{0,10}\" data-isPurchased=\".{0,10}\" "
				+ "data-offerType=\".{0,10}\" data-rentalGrantPeriodDays=\"."
				+ "{0,10}\" data-rentalactivePeriodHours=\".{0,10}\" "
				+ "data-offerTitle=\"";
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
		setCategory(c.getString(c.getColumnIndex(EntryColumns.KEY_CREATOR)));
	}

	@Override
	public EntryType getType() {
		return EntryType.MAGAZINE;
	}

	@Override
	protected String getTitlePattern() {
		return "<h1.*?class=\"doc-header-title\".*?><a href=\".*?\">(.*?)<";
	}

	@Override
	protected String getIconPattern() {
		return "<div class=\"doc-banner-icon\"><img src=\"(.*?)\"";
	}

	/**
	 * Gets the category of this magazine entry.
	 * 
	 * @return String representation of the category of this magazine
	 */
	public String getCategory() {
		return mCategory;
	}

	/**
	 * Sets the category of this magazine entry to the given category.
	 * 
	 * @param category
	 *            New category for this magazine entry
	 */
	public void setCategory(String category) {
		mCategory = category;
	}
}
