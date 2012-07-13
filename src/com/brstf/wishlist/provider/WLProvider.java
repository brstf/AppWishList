package com.brstf.wishlist.provider;

import java.util.HashMap;
import java.util.Map;

import com.brstf.wishlist.provider.WLDbAdapter.Tables;
import com.brstf.wishlist.provider.WLEntryContract.Entries;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class WLProvider extends ContentProvider {
	String TAG = "WLProvider";

	public static final String AUTHORITY = "com.brstf.wishlist";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY);

	private WLDbAdapter mDbHelper;

	// UriMatcher stuff
	private static final int GET_ALL = 0;
	private static final int GET_ENTRY = 1;
	private static final int SEARCH_ENTRIES = 2;

	private static final UriMatcher sURIMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		// to get entries
		matcher.addURI(AUTHORITY, "entries", GET_ALL);
		matcher.addURI(AUTHORITY, "entries/search/*", SEARCH_ENTRIES);
		matcher.addURI(AUTHORITY, "entries/*", GET_ENTRY);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		// On creation, initialize the database
		mDbHelper = new WLDbAdapter(getContext());
		mDbHelper.open();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Use the {@link UriMatcher} to see what kind of query we have and
		// format the
		// db query accordingly
		switch (sURIMatcher.match(uri)) {
		case SEARCH_ENTRIES:
			/*if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}*/
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setProjectionMap(buildMap());
			qb.setTables(Tables.SEARCH_JOIN_ENTRIES);
			qb.appendWhere(WLDbAdapter.SEARCH_BODY + " MATCH '"
					+ WLEntryContract.Entries.getSearchQuery(uri) + "'");

			return qb.query(mDbHelper.getDatabase(), projection, selection, selectionArgs, null,
					null, sortOrder);
		case GET_ENTRY:
			return getEntry(uri);
		case GET_ALL:
			return getAll();
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	private Map<String, String> buildMap() {
		HashMap<String, String> colmap = new HashMap<String, String>();
		colmap.put(BaseColumns._ID, Tables.ENTRIES + "." + BaseColumns._ID);
		colmap.put(Entries.KEY_NAME, Tables.ENTRIES + "." + Entries.KEY_NAME);
		colmap.put(Entries.KEY_URL, Tables.ENTRIES + "." + Entries.KEY_URL);
		colmap.put(Entries.KEY_ALBLENGTH, Tables.ENTRIES + "."
				+ Entries.KEY_ALBLENGTH);
		colmap.put(Entries.KEY_CPRICE, Tables.ENTRIES + "."
				+ Entries.KEY_CPRICE);
		colmap.put(Entries.KEY_CRATING, Tables.ENTRIES + "."
				+ Entries.KEY_CRATING);
		colmap.put(Entries.KEY_CREATOR, Tables.ENTRIES + "."
				+ Entries.KEY_CREATOR);
		colmap.put(Entries.KEY_DATE, Tables.ENTRIES + "." + Entries.KEY_DATE);
		colmap.put(Entries.KEY_ICONPATH, Tables.ENTRIES + "."
				+ Entries.KEY_ICONPATH);
		colmap.put(Entries.KEY_ICONURL, Tables.ENTRIES + "."
				+ Entries.KEY_ICONURL);
		colmap.put(Entries.KEY_MOVLENGTH, Tables.ENTRIES + "."
				+ Entries.KEY_MOVLENGTH);
		colmap.put(Entries.KEY_NUMTRACKS, Tables.ENTRIES + "."
				+ Entries.KEY_NUMTRACKS);
		colmap.put(Entries.KEY_PCOUNT, Tables.ENTRIES + "."
				+ Entries.KEY_PCOUNT);
		colmap.put(Entries.KEY_RATING, Tables.ENTRIES + "."
				+ Entries.KEY_RATING);
		colmap.put(Entries.KEY_RPRICE, Tables.ENTRIES + "."
				+ Entries.KEY_RPRICE);
		colmap.put(Entries.KEY_TAGS, Tables.ENTRIES + "." + Entries.KEY_TAGS);
		colmap.put(Entries.KEY_TYPE, Tables.ENTRIES + "." + Entries.KEY_TYPE);
		return colmap;
	}

	/**
	 * Retrieves a Cursor pointing to all entries of the database.
	 * 
	 * @return Cursor pointing to all entries of the database
	 */
	private Cursor getAll() {
		return mDbHelper.fetchAllEntries();
	}

	/**
	 * Gets a {@link Cursor} pointing to the wishlist entry specified by the
	 * given {@link Uri}.
	 * 
	 * @param uri
	 *            {@link Uri} indicating which entry to retrieve
	 * @return {@link Cursor} pointing to the specified wishlist entry
	 */
	private Cursor getEntry(Uri uri) {
		String rowId = uri.getLastPathSegment();
		return mDbHelper.fetchEntry(Integer.valueOf(rowId));
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case GET_ALL:
		case SEARCH_ENTRIES:
			return Entries.CONTENT_TYPE;
		case GET_ENTRY:
			return Entries.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		}
	}

	// TODO: Use these ?

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		throw new UnsupportedOperationException();
	}
}
