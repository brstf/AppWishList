package com.brstf.wishlist.entries;

public enum EntryType {

	NONE, APP, BOOK, MOVIE, MUSIC_ARTIST, MUSIC_ALBUM, MAGAZINE, PENDING;

	/**
	 * Constructs a new WLEntry based on the given type
	 * 
	 * @param type
	 *            The type of entry to construct
	 * @param id
	 *            The database id of the entry to create (that will be passed to
	 *            the constructor)
	 * @return A new entry of the given type
	 */
	public static Entry getTypeEntry(EntryType type, int id) {
		switch (type) {
		case APP:
			return new AppEntry(id);
		case BOOK:
			return new BookEntry(id);
		case MOVIE:
			return new MovieEntry(id);
		case MUSIC_ARTIST:
			return new ArtistEntry(id);
		case MUSIC_ALBUM:
			return new AlbumEntry(id);
		case MAGAZINE:
			return new MagazineEntry(id);
		default:
			return null;
		}
	}

	/**
	 * Function to return a string representation of the given WLEntryType
	 * 
	 * @param type
	 *            The type whose string representation to retrieve
	 * @return The string representation for the given type
	 */
	public static String getTypeString(EntryType type) {
		switch (type) {
		case APP:
			return "APP";
		case BOOK:
			return "BOOK";
		case MOVIE:
			return "MOVIE";
		case MUSIC_ARTIST:
			return "ARTIST";
		case MUSIC_ALBUM:
			return "ALBUM";
		case MAGAZINE:
			return "MAGAZINE";
		case PENDING:
			return "PENDING";
		default:
			return null;
		}
	}

	/**
	 * Function to reverse getTypeString(). Returns the WLEntryType
	 * corresponding to the passed in string
	 * 
	 * @param type
	 *            The string representation of a type
	 * @return The type corresponding to the given string
	 */
	public static EntryType getTypeFromString(String type) {
		if (type.equals("APP")) {
			return APP;
		} else if (type.equals("BOOK")) {
			return BOOK;
		} else if (type.equals("MOVIE")) {
			return MOVIE;
		} else if (type.equals("ARTIST")) {
			return MUSIC_ARTIST;
		} else if (type.equals("ALBUM")) {
			return MUSIC_ALBUM;
		} else if (type.equals("MAGAZINE")) {
			return MAGAZINE;
		} else if (type.equals("PENDING")) {
			return PENDING;
		} else {
			return NONE;
		}
	}

	/**
	 * Function to retrieve the type of entry based on a give Google Play store
	 * URL
	 * 
	 * @param url
	 *            The Google Play URL to retrieve type from
	 * @return The entry type of the given Google Play URL
	 */
	public static EntryType getTypeFromURL(String url) {
		if (url.contains("/store/apps/")) {
			return APP;
		} else if (url.contains("/store/movies/")) {
			return MOVIE;
		} else if (url.contains("/store/books/")) {
			return BOOK;
		} else if (url.contains("/store/music/artist")) {
			return MUSIC_ARTIST;
		} else if (url.contains("/store/music/album")) {
			return MUSIC_ALBUM;
		} else if (url.contains("/store/magazines/")) {
			return MAGAZINE;
		} else {
			return NONE;
		}
	}

	public static boolean isSinglePricedEntryString(String type) {
		return isSinglePricedEntry(getTypeFromString(type));
	}

	public static boolean isSinglePricedEntry(EntryType type) {
		switch (type) {
		case MOVIE:
		case MAGAZINE:
		case MUSIC_ARTIST:
		case NONE:
			return false;
		default:
			return true;
		}
	}
	
	public static boolean isMultiPricedEntryString(String type) {
		return isSinglePricedEntry(getTypeFromString(type));
	}

	public static boolean isMultiPricedEntry(EntryType type) {
		switch (type) {
		case MOVIE:
		case MAGAZINE:
			return true;
		default:
			return false;
		}
	}
}
