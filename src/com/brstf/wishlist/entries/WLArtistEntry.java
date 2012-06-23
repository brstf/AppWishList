package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.brstf.wishlist.WLDbAdapter;

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
	private String mGenres = null;

	/**
	 * Constructor to construct the entry for artists
	 * 
	 * @param id
	 *            The id in the database corresponding to this entry
	 */
	public WLArtistEntry(int id) {
		super(id);

		mGenres = "";
	}

	/**
	 * Return the type of MUSIC_ARTIST
	 */
	@Override
	public WLEntryType getType() {
		return WLEntryType.MUSIC_ARTIST;
	}

	/**
	 * Retrieves the list of genres this artist is tagged with in the Play store
	 * 
	 * @return Comma separated list of genres that this artist is tagged with
	 */
	public String getGenres() {
		return mGenres;
	}

	/**
	 * Function to add a new genre to the list of genres
	 * 
	 * @param genre
	 *            New genre to add to this artist
	 */
	private void addGenre(String genre) {
		if (!mGenres.equals("")) {
			mGenres = mGenres + ", ";
		}

		mGenres = mGenres + android.text.Html.fromHtml(genre);
	}

	@Override
	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the patterns and corresponding matchers
		Pattern p_genrelist = Pattern
				.compile("<h3>Genres</h3><ul class=\"category-list\">(.*?)</ul>");
		Pattern p_genre = Pattern
				.compile("<li class=\"category-item\"><a href=\".*?\">(.*?)<.*?</li>");
		Matcher m_genrelist = p_genrelist.matcher(text);

		// Find the genre list:
		m_genrelist.find();
		String genrelist = m_genrelist.group(1);

		// Now search for genres within the genre list
		Matcher m_genre = p_genre.matcher(genrelist);
		while (m_genre.find()) {
			addGenre(m_genre.group(1));
		}

		addTag("Music");
	}

	protected String getTitlePattern() {
		return "class=\"doc-header-title\">(.*?)<";
	}

	protected String getIconPattern() {
		return "<img itemprop=\"image\"src=\"(.*?)\"";
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);

		// Again, an oddity genres are stored in KEY_CREATOR
		setGenre(c.getString(c.getColumnIndex(WLDbAdapter.KEY_CREATOR)));
	}

	/**
	 * Sets the genre list text of this artist to the given genre list
	 * 
	 * @param genres
	 *            New list of genres for this artist
	 */
	public void setGenre(String genres) {
		mGenres = genres;
	}
}
