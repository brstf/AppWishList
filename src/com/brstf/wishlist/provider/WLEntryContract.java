package com.brstf.wishlist.provider;

import java.util.List;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class WLEntryContract {

	// Column names for entries
	public interface EntryColumns {
		public final String KEY_TYPE = "entry_type";
		public final String KEY_NAME = "entry_name";
		public final String KEY_URL = "entry_url";
		public final String KEY_ICONPATH = "entry_iconpath";
		public final String KEY_ICONURL = "entry_iconurl";
		public final String KEY_CPRICE = "entry_cprice";
		public final String KEY_RPRICE = "entry_rprice";
		public final String KEY_RATING = "entry_rating";
		public final String KEY_CRATING = "entry_crating"; // Content rating
															// (ex: PG-13)
		public final String KEY_MOVLENGTH = "entry_movlength";
		public final String KEY_CREATOR = "entry_creator"; // App maker, movie,
															// director, artist,
															// author
		public final String KEY_ALBLENGTH = "entry_alblength";
		public final String KEY_NUMTRACKS = "entry_numtracks";
		public final String KEY_DATE = "entry_date"; // Publish date, release
														// date
		public final String KEY_PCOUNT = "entry_pcount";
		public final String KEY_TAGS = "entry_tags";
	}

	public static final String AUTHORITY = "com.brstf.wishlist";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY);

	private static final String PATH_ENTRIES = "entries";
	private static final String PATH_SEARCH = "search";

	public static class Entries implements EntryColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendEncodedPath(PATH_ENTRIES).build();

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.brstf.wishlist.entry";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE 
				+ "/vnd.brstf.wishlist.entry";
		
		public static final String DEFAULT_SORT = EntryColumns.KEY_NAME + " DESC";
		
		public static Uri buildEntryUri(String entry_url) {
			return CONTENT_URI.buildUpon().appendPath(entry_url).build();
		}
		
		public static Uri buildSearchUri(String query) {
			return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
		}
		
		public static boolean isSearchUri(Uri uri) {
			List<String> pathSegments = uri.getPathSegments();
			return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
		}
		
		public static String getEntryUrl(Uri uri) {
			return uri.getPathSegments().get(1);
		}
		
		public static String getSearchQuery(Uri uri) {
			return uri.getPathSegments().get(2);
		}
	}
	
	private WLEntryContract(){}
}