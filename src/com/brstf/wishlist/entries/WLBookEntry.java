package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;

import com.brstf.wishlist.WLDbAdapter;

public class WLBookEntry extends WLPricedEntry {
	private int mPageCount;
	private String mAuthor = null;
	private String mPublishDate = null;

	public WLBookEntry(int id) {
		super(id);

		mPageCount = 0;
		mAuthor = "";
		mPublishDate = "";
	}

	@Override
	public WLEntryType getType() {
		return WLEntryType.BOOK;
	}

	@Override
	public void setFromURLText(String url, String text) {
		// Set the url
		setURL(url);
		
		// Set up the patterns and corresponding matchers
		Pattern p_title = Pattern.compile("data-docTitle=\"(.*?)\"");
		Pattern p_icon = Pattern.compile("data-docIconUrl=\"(.*?)\"");
		Pattern p_price = Pattern.compile("data-docPrice=\"(.*?)\"");
		Pattern p_pageCount = Pattern.compile("<dd itemprop=\"numberOfPages\">(.*?)<");
		Pattern p_author = Pattern.compile("<a href=\"/store/books/author?.*?\" itemprop=\"url\">(.*?)<");
		Pattern p_publish = Pattern.compile("<dt>Published:</dt><dd>(.*?)<");
		
		Matcher m_title = p_title.matcher(text);
		Matcher m_icon = p_icon.matcher(text);
		Matcher m_price = p_price.matcher(text);
		Matcher m_pageCount = p_pageCount.matcher(text);
		Matcher m_author = p_author.matcher(text);
		Matcher m_publish = p_publish.matcher(text);
		
		// Find the patterns
		m_title.find();
		m_icon.find();
		m_price.find();
		m_pageCount.find();
		m_author.find();
		m_publish.find();
		
		//Set our variables with the retrieved information
		setTitle(m_title.group(1));
		if (m_price.group(1).equals("Free")) {
			setRegularPrice(0.0f);
		} else {
			setRegularPrice(Float.valueOf(m_price.group(1).substring(1)));
		}
		setIconPath(m_icon.group(1));
		setPageCount(Integer.valueOf(m_pageCount.group(1)));
		setAuthor(m_author.group(1));
		setPublishDate(m_publish.group(1));
	}
	
	@Override
	public void setFromDb(Cursor c) {
		setPageCount(c.getInt(c.getColumnIndex(WLDbAdapter.KEY_PCOUNT)));
		setAuthor(c.getString(c.getColumnIndex(WLDbAdapter.KEY_CREATOR)));
		setPublishDate(c.getString(c.getColumnIndex(WLDbAdapter.KEY_DATE)));
	}
	
	/**
	 * Retrieves the number of pages in the book
	 * 
	 * @return The number of pages in the book
	 */
	public int getPageCount() {
		return mPageCount;
	}

	/**
	 * Retrieves the author name of this book as a string
	 * 
	 * @return The name of the author of the book
	 */
	public String getAuthor() {
		return mAuthor;
	}

	/**
	 * Retrieves the publication date of this book as a string
	 * 
	 * @return A string representing the publication date of this book
	 */
	public String getPublishDate() {
		return mPublishDate;
	}

	/**
	 * Sets the number of pages in this book to the passed in number
	 * 
	 * @param pageCount
	 *            The new number of pages in the book
	 */
	public void setPageCount(int pageCount) {
		mPageCount = pageCount;
	}

	/**
	 * Sets the author name of this book
	 * 
	 * @param author
	 *            The new author of the book
	 */
	public void setAuthor(String author) {
		mAuthor = author;
	}

	/**
	 * Sets the publication date of this book
	 * 
	 * @param publishDate
	 *            The new publication date of this book
	 */
	public void setPublishDate(String publishDate) {
		mPublishDate = publishDate;
	}
}
