package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.provider.WLDbAdapter;

import android.database.Cursor;

/**
 * Class for storing information about an app entry in the wishlist
 */

public class WLAppEntry extends WLPricedEntry {
	private String mDeveloper = null;
	private static final String DEV_PATTERN = "class=\"doc-header-link\">(.*?)<";

	public WLAppEntry(int id) {
		super(id);

		mDeveloper = "";
	}

	@Override
	public WLEntryType getType() {
		return WLEntryType.APP;
	}

	@Override
	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the patterns and corresponding matchers
		Pattern p_dev = Pattern.compile(DEV_PATTERN);
		Matcher m_dev = p_dev.matcher(text);

		// Find the patterns
		m_dev.find();
		
		// Set the developer
		setDeveloper(android.text.Html.fromHtml(m_dev.group(1))
				.toString());
	}
	
	@Override
	protected String getPricePattern() {
		return "data-docPrice=\"(.*?)\"";
	}

	@Override
	protected String getTitlePattern() {
		return "class=\"doc-banner-title\">(.*?)<";
	}

	@Override
	protected String getIconPattern() {
		return "class=\"doc-banner-icon\"><img.*?src=\"(.*?)\"";
	}

	/**
	 * Retrieves the developer of this app
	 */
	public String getDeveloper() {
		return mDeveloper;
	}

	/**
	 * Sets the developer of this app to the given developer.
	 * 
	 * @param developer
	 *            Developer of this app
	 */
	public void setDeveloper(String developer) {
		mDeveloper = developer;
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);

		setDeveloper(c.getString(c.getColumnIndex(WLDbAdapter.KEY_CREATOR)));
	}
}
