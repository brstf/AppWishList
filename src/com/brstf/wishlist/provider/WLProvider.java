package com.brstf.wishlist.provider;

import java.util.HashMap;
import java.util.Map;

import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.provider.WLDbAdapter.Tables;
import com.brstf.wishlist.provider.WLEntryContract.Entries;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	private static final int GET_TAG = 1;
	private static final int SEARCH_ENTRIES = 2;
	private static final int GET_ENTRY = 3;
	private static final int GET_TAGS = 4;

	private static final UriMatcher sURIMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		// to get entries
		matcher.addURI(AUTHORITY, "entries", GET_ALL);
		matcher.addURI(AUTHORITY, "entries/search/*", SEARCH_ENTRIES);
		matcher.addURI(AUTHORITY, "entries/tag/*", GET_TAG);
		matcher.addURI(AUTHORITY, "entries/entry/*", GET_ENTRY);
		matcher.addURI(AUTHORITY, "tags", GET_TAGS);

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
			/*
			 * if (selectionArgs == null) { throw new IllegalArgumentException(
			 * "selectionArgs must be provided for the Uri: " + uri); }
			 */
			SQLiteQueryBuilder searchqb = new SQLiteQueryBuilder();
			searchqb.setProjectionMap(buildMap());
			searchqb.setTables(Tables.SEARCH_JOIN_ENTRIES);
			searchqb.appendWhere(WLDbAdapter.SEARCH_BODY + " MATCH '"
					+ WLEntryContract.Entries.getSearchQuery(uri) + "'");

			return searchqb.query(mDbHelper.getDatabase(), projection,
					selection, selectionArgs, null, null, sortOrder);
		case GET_TAG:
			String tag = uri.getLastPathSegment();
			SQLiteQueryBuilder tagqb = new SQLiteQueryBuilder();
			tagqb.setProjectionMap(buildMap());
			tagqb.setTables(Tables.ENTRIES);
			tagqb.appendWhere(WLDbAdapter.KEY_TAGS + " LIKE '%" + tag + "%'");

			return tagqb.query(mDbHelper.getDatabase(), projection,
					WLDbAdapter.KEY_TYPE + " <> ?", new String[] { WLEntryType
							.getTypeString(WLEntryType.PENDING) }, null, null,
					null);
		case GET_ALL:
			return getAll();
		case GET_TAGS:
			return getTags();
		case GET_ENTRY:

		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	private Cursor getTags() {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Tables.ENTRIES_TAGS);
		return qb.query(mDbHelper.getDatabase(),
				WLEntryContract.TagQuery.columns, null, null, null, null, null);
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
		return mDbHelper
				.query(true, WLEntryContract.EntriesQuery.columns,
						WLDbAdapter.KEY_TYPE + " <> ? ",
						new String[] { WLEntryType
								.getTypeString(WLEntryType.PENDING) }, null,
						null, null, null);
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case GET_ALL:
		case SEARCH_ENTRIES:
		case GET_TAG:
			return Entries.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = mDbHelper.getDatabase();
		db.insertOrThrow(WLDbAdapter.Tables.ENTRIES, null, values);
		getContext().getContentResolver().notifyChange(
				WLEntryContract.Entries.CONTENT_URI, null);

		return uri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (sURIMatcher.match(uri)) {
		case GET_ENTRY:
			final String url = uri.getLastPathSegment();
			final int id = mDbHelper.fetchId(url);

			// Update the entry
			final int rval = mDbHelper.updateEntry(id, values);

			// Notify that a change happened
			getContext().getContentResolver().notifyChange(uri, null);
			return rval;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sURIMatcher.match(uri)) {
		case GET_ENTRY:
			final String url = uri.getLastPathSegment();
			final int id = mDbHelper.fetchId(url);

			// Delete the entry
			mDbHelper.deleteEntry(id);
			getContext().getContentResolver().notifyChange(
					WLEntryContract.Entries.CONTENT_URI, null);
			return 1;
		default:
			throw new UnsupportedOperationException();
		}
	}
}
