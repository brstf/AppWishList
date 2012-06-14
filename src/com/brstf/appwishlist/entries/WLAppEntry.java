package com.brstf.appwishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for storing information about an app entry in the wishlist
 */

public class WLAppEntry extends WLPricedEntry {

	public WLAppEntry(int id) {
		super(id);
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
		Pattern p_title = Pattern
				.compile("About This App</h4><dl class=\"doc-metadata-list\" itemscope itemtype=\"http://schema.org/MobileSoftwareApplication\"><meta itemprop=\"name\" content=\"(.*?)\"");
		Pattern p_icon = Pattern
				.compile("<meta itemprop=\"image\" content=\"(.*?)\"");
		Pattern p_price = Pattern.compile("data-docPrice=\"(.*?)\"");
		Matcher m_title = p_title.matcher(text);
		Matcher m_icon = p_icon.matcher(text);
		Matcher m_price = p_price.matcher(text);

		// Find the patterns
		m_title.find();
		m_icon.find();
		m_price.find();

		// Set our variables with the retrieved information
		setTitle(m_title.group(1));
		if (m_price.group(1).equals("Free")) {
			setRegularPrice(0.0f);
		} else {
			setRegularPrice(Float.valueOf(m_price.group(1).substring(1)));
		}

		// Set the icon path
		setIconPath(m_icon.group(1));
	}
}
