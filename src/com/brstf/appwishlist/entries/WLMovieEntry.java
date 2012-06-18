package com.brstf.appwishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;

import com.brstf.appwishlist.WLDbAdapter;

/**
 * Class for storing information about a movie entry in the wishlist
 */

public class WLMovieEntry extends WLPricedEntry {
	private String mContentRating = null;
	private String mDirector = null;
	private int mLength;

	public WLMovieEntry(int id) {
		super(id);

		mContentRating = "";
		mDirector = "";
		mLength = 0;
	}

	@Override
	public WLEntryType getType() {
		return WLEntryType.MOVIE;
	}

	@Override
	public void setFromURLText(String url, String text) {
		// Set the url
		setURL(url);

		// Set the patterns and corresponding matchers
		Pattern p_title = Pattern
				.compile("<h1 itemprop=\"name\"class=\"doc-banner-title\">(.*?)<");
		Pattern p_icon = Pattern
				.compile("<img itemprop=\"image\"src=\"(.*?)\"");
		Pattern p_price = Pattern.compile("data-docPrice=\"(.*?)\"");
		Pattern p_cr = Pattern.compile("itemprop=\"contentRating\">(.*?)<");
		Pattern p_dir = Pattern.compile("itemprop=\"director\".*?itemprop=\"name\">(.*?)<");
		Pattern p_length = Pattern.compile("<dt>Movie Length:</dt><dd>(.*?) minutes");
		
		Matcher m_title = p_title.matcher(text);
		Matcher m_icon = p_icon.matcher(text);
		Matcher m_price = p_price.matcher(text);
		Matcher m_cr = p_cr.matcher(text);
		Matcher m_dir = p_dir.matcher(text);
		Matcher m_length = p_length.matcher(text);
		
		// Find the patterns
		m_title.find();
		m_icon.find();
		m_price.find();
		m_cr.find();
		m_dir.find();
		m_length.find();
		
		// Set our variables with the retrieved information
		setTitle(m_title.group(1));
		if (m_price.group(1).equals("Free")) {
			setRegularPrice(0.0f);
		} else {
			setRegularPrice(Float.valueOf(m_price.group(1).substring(1)));
		}
		setIconPath(m_icon.group(1));
		setContentRating(m_cr.group(1));
		setDirector(m_dir.group(1));
		setMovieLength(Integer.valueOf(m_length.group(1)));
	}
	
	@Override
	public void setFromDb(Cursor c) {
		setContentRating(c.getString(c.getColumnIndex(WLDbAdapter.KEY_CRATING)));
		setDirector(c.getString(c.getColumnIndex(WLDbAdapter.KEY_CREATOR)));
		setMovieLength(c.getInt(c.getColumnIndex(WLDbAdapter.KEY_MOVLENGTH)));
	}

	/**
	 * Retrieves the content rating of the movie (G, PG, PG-13, R)
	 * 
	 * @return The content rating of the movie as a String
	 */
	public String getContentRating() {
		return mContentRating;
	}

	/**
	 * Retrieves the director of the movie
	 * 
	 * @return The director of the movie as a string
	 */
	public String getDirector() {
		return mDirector;
	}

	/**
	 * Retrieves the length of the movie (in minutes)
	 * 
	 * @return The length of the movie (in minutes) as an int
	 */
	public int getMovieLength() {
		return mLength;
	}

	/**
	 * Retrieves the content rating of the movie (G, PG, PG-13, R)
	 * 
	 * @param cRating
	 *            The new content rating of the movie as a String
	 */
	public void setContentRating(String cRating) {
		mContentRating = cRating;
	}

	/**
	 * Retrieves the director of the movie
	 * 
	 * @param director
	 *            The new director of the movie as a string
	 */
	public void setDirector(String director) {
		mDirector = director;
	}

	/**
	 * Retrieves the length of the movie (in minutes)
	 * 
	 * @param length
	 *            The new length of the movie (in minutes) as an int
	 */
	public void setMovieLength(int length) {
		mLength = length;
	}

}
