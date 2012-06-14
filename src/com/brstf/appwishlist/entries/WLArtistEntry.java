package com.brstf.appwishlist.entries;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Class that defines the behavior of the Artist Entries in the wish list
 * artists don't have pricing, ratings, or anything so this class is pretty
 * bare-bones
 * 
 * @author brstf
 * 
 */
public class WLArtistEntry extends WLEntry {
	/**
	 * Constructor to construct the entry for artists
	 * 
	 * @param id
	 *            The id in the database corresponding to this entry
	 */
	public WLArtistEntry(int id) {
		super(id);
	}

	/**
	 * Return the type of MUSIC_ARTIST
	 */
	@Override
	public WLEntryType getType() {
		return WLEntryType.MUSIC_ARTIST;
	}

	@Override
	public void setFromURLText(String url, String text) {
		// Set the url
		setURL(url);
		
		// Set up the patterns and corresponding matchers
		Pattern p_title = Pattern.compile("class=\"doc-header-title\">(.*?)<");
		Pattern p_icon = Pattern.compile("<img itemprop=\"image\"src=\"(.*?)\"");
		
		Matcher m_title = p_title.matcher(text);
		Matcher m_icon = p_icon.matcher(text);
		
		// Find the patterns
		m_title.find();
		m_icon.find();
		
		// Set our variables with the retrieved information
		setTitle(m_title.group(1));
		setIconPath(m_icon.group(1));
	}
}