package com.brstf.wishlist.provider;

import java.util.List;

import com.brstf.wishlist.entries.EntryType;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class WLEntryContract {

	// Column names for entries
	public interface EntryColumns {
		public final String KEY_TYPE = "type";
		public final String KEY_NAME = "name";
		public final String KEY_URL = "url";
		public final String KEY_ICONPATH = "iconpath";
		public final String KEY_ICONURL = "iconurl";

		// Price Keys
		public final String KEY_CUR_PRICE_1 = "cur_price_1";
		public final String KEY_REG_PRICE_1 = "reg_price_1";
		public final String KEY_CUR_PRICE_2 = "cur_price_2";
		public final String KEY_REG_PRICE_2 = "reg_price_2";
		public final String KEY_CUR_PRICE_3 = "cur_price_3";
		public final String KEY_REG_PRICE_3 = "reg_price_3";
		public final String KEY_CUR_PRICE_4 = "cur_price_4";
		public final String KEY_REG_PRICE_4 = "reg_price_4";
		/*
		 * Prices:
		 * 
		 * 1 - Normal Price (Apps, Music, Books) Movie Price - Rental SD TV
		 * Price - Season Price SD Magazine Price - Issue Price
		 * 
		 * 2 - Movie Price - Rental HD TV Price - Season Price HD Magazine Price
		 * - Subscription Monthly
		 * 
		 * 3 - Movie Price - Buy SD Magazine Price - Subscription Annual
		 * 
		 * 4 - Movie Price - Buy HD
		 * 
		 * All of these are necessary, not sure of a better solution
		 */

		public final String KEY_RATING = "rating";
		public final String KEY_CRATING = "crating"; // Content rating
														// (ex: PG-13)
		public final String KEY_MOVLENGTH = "movlength";
		public final String KEY_CREATOR = "creator"; // App maker, movie,
														// director, artist,
														// author
		public final String KEY_ALBLENGTH = "alblength";
		public final String KEY_NUMTRACKS = "numtracks";
		public final String KEY_DATE = "date"; // Publish date, release
												// date
		public final String KEY_PCOUNT = "pcount";
		public final String KEY_TAGS = "tags";
	}

	public interface TagColumns {
		public static final String KEY_TAG = "tag";

		public static final String KEY_APP_COUNT = EntryType
				.getTypeString(EntryType.APP);
		public static final String KEY_MUSIC_COUNT = "music";
		public static final String KEY_MOVIE_COUNT = EntryType
				.getTypeString(EntryType.MOVIE);
		public static final String KEY_BOOK_COUNT = EntryType
				.getTypeString(EntryType.BOOK);
		public static final String KEY_MAGAZINE_COUNT = EntryType
				.getTypeString(EntryType.MAGAZINE);
	}

	public static final String AUTHORITY = "com.brstf.wishlist";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY);

	private static final String PATH_ENTRIES = "entries";
	private static final String PATH_SEARCH = "search";
	private static final String PATH_ENTRY = "entry";
	private static final String PATH_TAGS = "tags";

	public static class Entries implements EntryColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendEncodedPath(PATH_ENTRIES).build();

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.brstf.wishlist.entry";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.brstf.wishlist.entry";

		public static final String DEFAULT_SORT = EntryColumns.KEY_NAME
				+ " DESC";

		public static Uri buildEntryUri(String entry_url) {
			return CONTENT_URI.buildUpon().appendPath(PATH_ENTRY)
					.appendPath(entry_url).build();
		}

		public static Uri buildSearchUri(String query) {
			return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH)
					.appendPath(query).build();
		}

		public static boolean isSearchUri(Uri uri) {
			List<String> pathSegments = uri.getPathSegments();
			return pathSegments.size() >= 2
					&& PATH_SEARCH.equals(pathSegments.get(1));
		}

		public static String getEntryUrl(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getSearchQuery(Uri uri) {
			return uri.getPathSegments().get(2);
		}
	}

	public static class Tags implements BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendEncodedPath(PATH_TAGS).build();
	}

	public interface EntriesQuery {
		int _TOKEN = 0x1;
		final String[] columns = { BaseColumns._ID, EntryColumns.KEY_TYPE,
				EntryColumns.KEY_NAME, EntryColumns.KEY_CREATOR,
				EntryColumns.KEY_CUR_PRICE_1, EntryColumns.KEY_ICONPATH,
				EntryColumns.KEY_ICONURL, EntryColumns.KEY_URL };
	}

	public interface SearchQuery {
		int _TOKEN = 0x2;
		final String[] columns = { BaseColumns._ID, EntryColumns.KEY_TYPE,
				EntryColumns.KEY_NAME, EntryColumns.KEY_CREATOR,
				EntryColumns.KEY_CUR_PRICE_1, EntryColumns.KEY_ICONPATH,
				EntryColumns.KEY_ICONURL, EntryColumns.KEY_URL };
	}

	public interface PendingQuery {
		int _TOKEN = 0x3;
		final String[] columns = { BaseColumns._ID, EntryColumns.KEY_TYPE,
				EntryColumns.KEY_URL };
	}

	public interface TagQuery {
		int _TOKEN = 0x4;
		final String[] columns = { BaseColumns._ID, TagColumns.KEY_TAG,
				BaseColumns._COUNT, TagColumns.KEY_APP_COUNT,
				TagColumns.KEY_MUSIC_COUNT, TagColumns.KEY_MOVIE_COUNT,
				TagColumns.KEY_BOOK_COUNT, TagColumns.KEY_MAGAZINE_COUNT };
	}

	private WLEntryContract() {
	}
}
