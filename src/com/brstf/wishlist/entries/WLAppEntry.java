package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.WLDbAdapter;

import android.database.Cursor;

/**
 * Class for storing information about an app entry in the wishlist
 */

public class WLAppEntry extends WLPricedEntry {
	private String mDeveloper = null;
	private static final String TITLE_DEV_ICON_PATTERN = "class=\"doc-banner-title\">(.*?)<.*?class=\"doc-header-link\">(.*?)<.*?class=\"doc-banner-icon\"><img.*?src=\"(.*?)\"";

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
		// Set the url
		setURL(url);

		// Set up the patterns and corresponding matchers
		Pattern p_titledevicon = Pattern.compile(TITLE_DEV_ICON_PATTERN);
		Pattern p_price = Pattern.compile("data-docPrice=\"(.*?)\"");
		Pattern p_rating = Pattern.compile(RATING_PATTERN);
		Matcher m_titledevicon = p_titledevicon.matcher(text);
		Matcher m_price = p_price.matcher(text);
		Matcher m_rating = p_rating.matcher(text);

		// Find the patterns
		m_titledevicon.find();
		m_price.find();
		
		// Set our variables with the retrieved information
		setTitle(android.text.Html.fromHtml(m_titledevicon.group(1)).toString());
		if (m_price.group(1).equals("Free")) {
			setRegularPrice(0.0f);
		} else {
			setRegularPrice(Float.valueOf(m_price.group(1).substring(1)));
		}

		// Set the developer
		setDeveloper(android.text.Html.fromHtml(m_titledevicon.group(2))
				.toString());

		// Set the icon path
		setIconUrl(android.text.Html.fromHtml(m_titledevicon.group(3))
				.toString());
		
		if(m_rating.find()) {
			setRating(Float.parseFloat(m_rating.group(1)));
		} else {
			setRating(0.0f);
		}

		addTag(WLEntryType.getTypeString(getType()));
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
