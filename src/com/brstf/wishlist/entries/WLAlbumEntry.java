package com.brstf.wishlist.entries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;

import com.brstf.wishlist.provider.WLDbAdapter;

public class WLAlbumEntry extends WLPricedEntry {
	private String mArtist = null;
	private String mLength = null;
	private int mTracks;
	private String mReleaseDate = null;

	public WLAlbumEntry(int id) {
		super(id);
	}

	@Override
	public WLEntryType getType() {
		return WLEntryType.MUSIC_ALBUM;
	}

	@Override
	public void setFromURLText(String url, String text) {
		super.setFromURLText(url, text);

		// Set up the patterns and corresponding matchers
		Pattern p_artist = Pattern
				.compile("href=\"/store/music/artist/.*?\">(.*?)<");
		Pattern p_length = Pattern
				.compile("Total Length<.*?class=\"meta-details-value\">(.*?)<");
		Pattern p_tracks = Pattern
				.compile("Tracks</td.*?class=\"meta-details-value\">(.*?)<");
		Pattern p_release = Pattern
				.compile("Released<.*?class=\"meta-details-value\">(.*?)<");

		Matcher m_artist = p_artist.matcher(text);
		Matcher m_length = p_length.matcher(text);
		Matcher m_tracks = p_tracks.matcher(text);
		Matcher m_release = p_release.matcher(text);

		// Find the patterns
		m_artist.find();
		m_tracks.find();
		m_release.find();

		// Set our variables with the retrieved information
		setArtist(android.text.Html.fromHtml(m_artist.group(1)).toString());
		
		if (m_length.find()) {
			setLength(android.text.Html.fromHtml(m_length.group(1)).toString());
		} else {
			setLength("");
		}
		
		setTrackCount(Integer.valueOf(m_tracks.group(1)));
		setReleaseDate(android.text.Html.fromHtml(m_release.group(1))
				.toString());

		addTag("Music");
	}
	
	@Override
	protected String getPricePattern() {
		return "<span itemprop=\"price\" content=\"(.*?)\"";
	}

	@Override
	protected String getTitlePattern() {
		return "class=\"doc-header-title\">(.*?)<";
	}

	@Override
	protected String getIconPattern() {
		return "<img itemprop=\"image\"src=\"(.*?)\"";
	}

	@Override
	public void setFromDb(Cursor c) {
		super.setFromDb(c);
		setArtist(c.getString(c.getColumnIndex(WLDbAdapter.KEY_CREATOR)));
		setLength(c.getString(c.getColumnIndex(WLDbAdapter.KEY_ALBLENGTH)));
		setTrackCount(c.getInt(c.getColumnIndex(WLDbAdapter.KEY_NUMTRACKS)));
		setReleaseDate(c.getString(c.getColumnIndex(WLDbAdapter.KEY_DATE)));
	}

	/**
	 * Retrieves the number of tracks in the album
	 * 
	 * @return The number of tracks in the album
	 */
	public int getTrackCount() {
		return mTracks;
	}

	/**
	 * Retrieves the artist of this album
	 * 
	 * @return The artist of this album
	 */
	public String getArtist() {
		return mArtist;
	}

	/**
	 * Retrieves the length of this album
	 * 
	 * @return The length of this album
	 */
	public String getLength() {
		return mLength;
	}

	/**
	 * Retrieves the release date of this album
	 * 
	 * @return The release date of this album
	 */
	public String getReleaseDate() {
		return mReleaseDate;
	}

	/**
	 * Sets the number of tracks in the album
	 * 
	 * @param trackCount
	 *            The new number of tracks in the album
	 */
	public void setTrackCount(int trackCount) {
		mTracks = trackCount;
	}

	/**
	 * Sets the artist of this album
	 * 
	 * @param artist
	 *            The new artist of this album
	 */
	public void setArtist(String artist) {
		mArtist = artist;
	}

	/**
	 * Sets the length of this album
	 * 
	 * @param length
	 *            The new length of this album
	 */
	public void setLength(String length) {
		mLength = length;
	}

	/**
	 * Sets the release date of this album
	 * 
	 * @param releaseDate
	 *            The new release date of this album
	 */
	public void setReleaseDate(String releaseDate) {
		mReleaseDate = releaseDate;
	}

}
