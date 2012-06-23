package com.brstf.wishlist.entries;

import android.database.Cursor;

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
		super.setFromURLText(url, text);

		addTag("Music");
	}
	
	protected String getTitlePattern(){ 
		return "class=\"doc-header-title\">(.*?)<";
	}
	
	protected String getIconPattern() {
		return "<img itemprop=\"image\"src=\"(.*?)\"";
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
	}
}
