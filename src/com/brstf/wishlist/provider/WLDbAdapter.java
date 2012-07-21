package com.brstf.wishlist.provider;

import com.brstf.wishlist.entries.WLAlbumEntry;
import com.brstf.wishlist.entries.WLAppEntry;
import com.brstf.wishlist.entries.WLArtistEntry;
import com.brstf.wishlist.entries.WLBookEntry;
import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.entries.WLMovieEntry;
import com.brstf.wishlist.entries.WLPricedEntry;
import com.brstf.wishlist.provider.WLEntryContract.EntryColumns;
import com.brstf.wishlist.provider.WLEntryContract.TagColumns;
import com.brstf.wishlist.provider.WLEntryContract.TagQuery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * WLDbAdapter is a class to facilitate interactions with the SQLite database
 * that stores all information regarding the wish list.
 */
public class WLDbAdapter {
	// Member Variables, a key for each possible entry
	public static final String SEARCH_BODY = "body";

	interface Tables {
		String ENTRIES = "wlentries";
		String ENTRIES_SEARCH = "wlentries_search";
		String SEARCH_JOIN_ENTRIES = "wlentries_search "
				+ "LEFT OUTER JOIN wlentries ON wlentries_search.url=wlentries.url";
		String ENTRIES_TAGS = "wlentries_tags";
	}

	private interface Triggers {
		String ENTRIES_SEARCH_INSERT = "wlentries_search_insert";
		String ENTRIES_SEARCH_DELETE = "wlentries_search_delete";
		String ENTRIES_SEARCH_UPDATE = "wlentries_search_update";

		String ENTRIES_TAGS_INSERT = "wlentries_tags_insert";
		String ENTRIES_TAGS_DELETE = "wlentries_tags_delete";

		String ENTRIES_TAGS_UPDATE_APP = "wlentries_tags_update_app";
		String ENTRIES_TAGS_UPDATE_MUSIC = "wlentries_tags_update_music";
		String ENTRIES_TAGS_UPDATE_MOVIE = "wlentries_tags_update_movie";
		String ENTRIES_TAGS_UPDATE_BOOK = "wlentries_tags_update_book";
	}

	private interface Subquery {
		String ENTRIES_BODY = "(coalesce(new." + EntryColumns.KEY_NAME
				+ ", '')||'; '||coalesce(new." + EntryColumns.KEY_CREATOR
				+ ", '')||'; '||coalesce(new." + EntryColumns.KEY_TAGS
				+ ", ''))";
	}

	private interface Qualified {
		String ENTRIES_SEARCH = Tables.ENTRIES_SEARCH + "("
				+ EntryColumns.KEY_URL + "," + SEARCH_BODY + ")";
		String ENTRIES_SEARCH_ID = Tables.ENTRIES_SEARCH + "."
				+ EntryColumns.KEY_URL;
	}

	private interface References {
		String URL_ID = "REFERENCES " + Tables.ENTRIES + "("
				+ EntryColumns.KEY_URL + ")";
	}

	private static final String TAG = "WLDbAdapter";
	private DatabaseHelper mDbHelper;

	// Helper strings to assist in the operation of the SQLite database
	private static final String DATABASE_CREATE = "CREATE TABLE "
			+ Tables.ENTRIES
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, name TEXT, url "
			+ "TEXT NOT NULL, iconpath TEXT, iconurl TEXT, cprice FLOAT, "
			+ "rprice FLOAT, rating FLOAT, crating TEXT, movlength INTEGER, creator TEXT, "
			+ "alblength TEXT, numtracks INTEGER, date TEXT, pcount INTEGER, tags TEXT, UNIQUE "
			+ "(url) ON CONFLICT IGNORE)";

	private static final String DATABASE_NAME = "wldata";
	private static final int DATABASE_VERSION = 2;

	private SQLiteDatabase mDb = null;

	/**
	 * DatabaseHelper is a class to facilitate WLDbAdapter's interactions with
	 * the base database.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * Constructor of the helper, takes in a context and calls it's parent
		 * constructor
		 * 
		 * @param context
		 *            The application context of the Activity using the database
		 */
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);

			// Create the search virtual table
			db.execSQL("CREATE VIRTUAL TABLE " + Tables.ENTRIES_SEARCH
					+ " USING fts3( " + BaseColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ EntryColumns.KEY_URL + " TEXT NOT NULL, " + SEARCH_BODY
					+ " TEXT NOT NULL, " + References.URL_ID + "," + "UNIQUE ("
					+ EntryColumns.KEY_URL
					+ ") ON CONFLICT REPLACE,tokenize=porter)");

			// Create triggers
			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_SEARCH_INSERT
					+ " AFTER INSERT ON " + Tables.ENTRIES
					+ " BEGIN INSERT INTO " + Qualified.ENTRIES_SEARCH
					+ " VALUES(new." + EntryColumns.KEY_URL + ", "
					+ Subquery.ENTRIES_BODY + ");END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_SEARCH_DELETE
					+ " AFTER DELETE ON " + Tables.ENTRIES
					+ " BEGIN DELETE FROM " + Tables.ENTRIES_SEARCH + " WHERE "
					+ Qualified.ENTRIES_SEARCH_ID + "=old."
					+ EntryColumns.KEY_URL + "; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_SEARCH_UPDATE
					+ " AFTER UPDATE ON " + Tables.ENTRIES + " BEGIN UPDATE "
					+ Tables.ENTRIES_SEARCH + " SET " + SEARCH_BODY + " = "
					+ Subquery.ENTRIES_BODY + " WHERE " + EntryColumns.KEY_URL
					+ " = old." + EntryColumns.KEY_URL + "; END;");

			// Create the table to store tag / tag count information
			db.execSQL("CREATE TABLE " + Tables.ENTRIES_TAGS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ TagColumns.KEY_TAG + " TEXT NOT NULL, "
					+ BaseColumns._COUNT + " INTEGER, "
					+ TagColumns.KEY_APP_COUNT + " INTEGER, "
					+ TagColumns.KEY_MUSIC_COUNT + " INTEGER, "
					+ TagColumns.KEY_MOVIE_COUNT + " INTEGER, "
					+ TagColumns.KEY_BOOK_COUNT + " INTEGER, UNIQUE ("
					+ TagColumns.KEY_TAG + ") ON CONFLICT IGNORE)");

			ContentValues cv = new ContentValues();
			cv.put(TagColumns.KEY_TAG, "all");
			cv.put(BaseColumns._COUNT, 0);
			cv.put(TagColumns.KEY_APP_COUNT, 0);
			cv.put(TagColumns.KEY_MUSIC_COUNT, 0);
			cv.put(TagColumns.KEY_MOVIE_COUNT, 0);
			cv.put(TagColumns.KEY_BOOK_COUNT, 0);
			db.insert(Tables.ENTRIES_TAGS, null, cv);

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_INSERT
					+ " AFTER INSERT ON " + Tables.ENTRIES + " BEGIN UPDATE "
					+ Tables.ENTRIES_TAGS + " SET " + BaseColumns._COUNT
					+ " = " + BaseColumns._COUNT + " + 1 WHERE "
					+ TagColumns.KEY_TAG + " == 'all'; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_DELETE
					+ " AFTER DELETE ON " + Tables.ENTRIES + " BEGIN UPDATE "
					+ Tables.ENTRIES_TAGS + " SET " + BaseColumns._COUNT
					+ " = " + BaseColumns._COUNT + " - 1 WHERE "
					+ TagColumns.KEY_TAG + " == 'all'; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_UPDATE_APP
					+ " AFTER UPDATE OF " + EntryColumns.KEY_TYPE + " ON "
					+ Tables.ENTRIES + " WHEN new." + EntryColumns.KEY_TYPE
					+ " == 'APP' BEGIN UPDATE " + Tables.ENTRIES_TAGS + " SET "
					+ TagColumns.KEY_APP_COUNT + " = "
					+ TagColumns.KEY_APP_COUNT + " + 1 WHERE "
					+ TagColumns.KEY_TAG + " == 'all'; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_UPDATE_MUSIC
					+ " AFTER UPDATE OF " + EntryColumns.KEY_TYPE + " ON "
					+ Tables.ENTRIES + " WHEN new." + EntryColumns.KEY_TYPE
					+ " == 'ARTIST' OR new." + EntryColumns.KEY_TYPE
					+ " == 'ALBUM' BEGIN UPDATE " + Tables.ENTRIES_TAGS
					+ " SET " + TagColumns.KEY_MUSIC_COUNT + " = "
					+ TagColumns.KEY_MUSIC_COUNT + " + 1 WHERE "
					+ TagColumns.KEY_TAG + " == 'all'; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_UPDATE_MOVIE
					+ " AFTER UPDATE OF " + EntryColumns.KEY_TYPE + " ON "
					+ Tables.ENTRIES + " WHEN new." + EntryColumns.KEY_TYPE
					+ " == 'MOVIE' BEGIN UPDATE " + Tables.ENTRIES_TAGS
					+ " SET " + TagColumns.KEY_MOVIE_COUNT + " = "
					+ TagColumns.KEY_MOVIE_COUNT + " + 1 WHERE "
					+ TagColumns.KEY_TAG + " == 'all'; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_UPDATE_BOOK
					+ " AFTER UPDATE OF " + EntryColumns.KEY_TYPE + " ON "
					+ Tables.ENTRIES + " WHEN new." + EntryColumns.KEY_TYPE
					+ " == 'BOOK' BEGIN UPDATE " + Tables.ENTRIES_TAGS
					+ " SET " + TagColumns.KEY_BOOK_COUNT + " = "
					+ TagColumns.KEY_BOOK_COUNT + " + 1 WHERE "
					+ TagColumns.KEY_TAG + " == 'all'; END;");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + Tables.ENTRIES);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.ENTRIES_SEARCH);

			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_SEARCH_DELETE);
			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_SEARCH_UPDATE);
			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_SEARCH_INSERT);

			db.execSQL("DROP TABLE IF EXISTS " + Tables.ENTRIES_TAGS);
			db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ENTRIES_TAGS_INSERT);
			db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ENTRIES_TAGS_DELETE);

			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_TAGS_UPDATE_APP);
			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_TAGS_UPDATE_MUSIC);
			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_TAGS_UPDATE_MOVIE);
			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.ENTRIES_TAGS_UPDATE_BOOK);

			onCreate(db);
		}
	}

	/**
	 * Constructor for the WLDbAdapter, simply takes in the application context
	 * of the Activity using it
	 * 
	 * @param context
	 *            The application context of the Activity using the database
	 */
	public WLDbAdapter(Context context) {
		this.mDbHelper = new DatabaseHelper(context);
	}

	/**
	 * Begins a transaction in exclusive mode on the database.
	 */
	public void beginTransaction() {
		mDb.beginTransaction();
	}

	/**
	 * Marks the current transaction on the database successful.
	 */
	public void setTransactionSuccessful() {
		mDb.setTransactionSuccessful();
	}

	public void open() {
		mDb = mDbHelper.getWritableDatabase();
	}

	public SQLiteDatabase getDatabase() {
		return mDb;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Ends the current transaction on the database.
	 */
	public void endTransaction() {
		mDb.endTransaction();
	}

	/**
	 * Adds a tag to the entry with the given URL.
	 * 
	 * @param url
	 * @param tag
	 */
	public void addTag(String url, String tag) {
		Cursor c = mDb.query(Tables.ENTRIES_TAGS,
				new String[] { TagColumns.KEY_TAG }, TagColumns.KEY_TAG
						+ " = ?", new String[] { tag }, null, null, null);

		// If the tag table didn't have this tag, add it
		if (!c.moveToFirst()) {
			ContentValues cv = new ContentValues();
			cv.put(TagColumns.KEY_TAG, tag);
			cv.put(BaseColumns._COUNT, 0);
			cv.put(TagColumns.KEY_APP_COUNT, 0);
			cv.put(TagColumns.KEY_MUSIC_COUNT, 0);
			cv.put(TagColumns.KEY_MOVIE_COUNT, 0);
			cv.put(TagColumns.KEY_BOOK_COUNT, 0);
			mDb.insert(Tables.ENTRIES_TAGS, null, cv);
		}

		// Now update the count and add the tag
		c = mDb.query(true, Tables.ENTRIES, new String[] {
				EntryColumns.KEY_TYPE, EntryColumns.KEY_TAGS },
				EntryColumns.KEY_URL + " = ?", new String[] { url }, null,
				null, null, null);
		c.moveToFirst();
		String tags = c.getString(c.getColumnIndex(EntryColumns.KEY_TAGS));
		tags = tags == null ? tag : tags + "," + tag;

		ContentValues values = new ContentValues();
		values.put(EntryColumns.KEY_TAGS, tags);
		mDb.update(Tables.ENTRIES, values, EntryColumns.KEY_URL + " = ?",
				new String[] { url });

		Cursor tagc = mDb.query(true, Tables.ENTRIES_TAGS, TagQuery.columns,
				TagColumns.KEY_TAG + " = ?", new String[] { tag }, null, null,
				null, null);
		tagc.moveToFirst();
		int count = tagc.getInt(tagc.getColumnIndex(BaseColumns._COUNT));
		values.clear();
		values.put(BaseColumns._COUNT, count + 1);

		// Increment the appropriate type count as well
		switch (WLEntryType.getTypeFromString(c.getString(c
				.getColumnIndex(EntryColumns.KEY_TYPE)))) {
		case APP:
			int appcount = tagc.getInt(tagc
					.getColumnIndex(TagColumns.KEY_APP_COUNT));
			values.put(TagColumns.KEY_APP_COUNT, appcount + 1);
			break;
		case MUSIC_ALBUM:
		case MUSIC_ARTIST:
			int musiccount = tagc.getInt(tagc
					.getColumnIndex(TagColumns.KEY_MUSIC_COUNT));
			values.put(TagColumns.KEY_MUSIC_COUNT, musiccount + 1);
			break;
		case MOVIE:
			int moviecount = tagc.getInt(tagc
					.getColumnIndex(TagColumns.KEY_MOVIE_COUNT));
			values.put(TagColumns.KEY_MOVIE_COUNT, moviecount + 1);
			break;
		case BOOK:
			int bookcount = tagc.getInt(tagc
					.getColumnIndex(TagColumns.KEY_BOOK_COUNT));
			values.put(TagColumns.KEY_BOOK_COUNT, bookcount + 1);
			break;
		}

		mDb.update(Tables.ENTRIES_TAGS, values, TagColumns.KEY_TAG + " = ?",
				new String[] { tag });
	}

	/**
	 * Checks if an entry with the given url exists in the database.
	 * 
	 * @param url
	 *            URL to check existence of
	 * @return Name of the entry if found, "PENDING" if entry does not yet have
	 *         a name, null if entry is not found
	 */
	public synchronized String containsUrl(String url) {
		String[] columns = { EntryColumns.KEY_URL, EntryColumns.KEY_NAME };
		String selection = EntryColumns.KEY_URL + " = ?";
		String[] selectionArgs = { url };
		Cursor c = mDb.query(true, Tables.ENTRIES, columns, selection,
				selectionArgs, null, null, null, null);

		// If no row was found, return null
		if (!c.moveToFirst()) {
			return null;
		}

		// Otherwise, check the name, if the name was null, it's pending
		String name = c.getString(c.getColumnIndex(EntryColumns.KEY_NAME));
		if (name == null) {
			return "PENDING";
		} else {
			return name;
		}
	}

	/**
	 * Fetches the _id column of the row with the given url.
	 * 
	 * @param url
	 *            URL to find row _id of
	 * @return Row _id of entry with given URL, -1 if not found
	 */
	public synchronized int fetchId(String url) {
		String[] columns = { BaseColumns._ID };
		String selection = EntryColumns.KEY_URL + " = ?";
		String[] selectionArgs = { url };
		Cursor c = mDb.query(true, Tables.ENTRIES, columns, selection,
				selectionArgs, null, null, null, null);

		// If no row was found, return -1
		if (!c.moveToFirst()) {
			return -1;
		}

		// Otherwise, return the id
		return c.getInt(c.getColumnIndex(BaseColumns._ID));
	}

	public Cursor query(boolean distinct, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		return mDb.query(distinct, Tables.ENTRIES, columns, selection,
				selectionArgs, groupBy, having, orderBy, limit);
	}

	/**
	 * Creates an entry in the database with the values specified from the
	 * passed in WLEntry
	 * 
	 * @param ent
	 *            The WLEntry to add to the database
	 * @return The long id of the new entry in the database
	 */
	public long createEntry(WLEntry ent) {
		return mDb.insert(Tables.ENTRIES, null, createValues(ent));
	}

	public long insert(ContentValues values) {
		return mDb.insert(Tables.ENTRIES, null, values);
	}

	/**
	 * Function to delete a specified entry from the database
	 * 
	 * @param rowId
	 *            The integer id of the entry in the database to delete
	 * @return True on successful deletion, false if deletion failed
	 */
	public boolean deleteEntry(int rowId) {
		Cursor c = fetchEntry(rowId);
		String tagString = c.getString(c.getColumnIndex(EntryColumns.KEY_TAGS));
		String[] tags = tagString.split(",");
		ContentValues cv = new ContentValues();
		for (String tag : tags) {
			c = mDb.query(true, Tables.ENTRIES_TAGS,
					new String[] { BaseColumns._COUNT }, TagColumns.KEY_TAG
							+ " = ?", new String[] { tag }, null, null, null,
					null);
			if (!c.moveToFirst())
				continue;
			int count = c.getInt(c.getColumnIndex(BaseColumns._COUNT));
			cv.clear();
			cv.put(BaseColumns._COUNT, count - 1);
			mDb.update(Tables.ENTRIES_TAGS, cv, TagColumns.KEY_TAG + " = ?",
					new String[] { tag });
		}

		return mDb.delete(Tables.ENTRIES, BaseColumns._ID + "=" + rowId, null) > 0;
	}

	/**
	 * Function to get all entries from the database
	 * 
	 * @return Cursor pointing at the first entry in the list of entries
	 */
	public Cursor fetchAllEntries() {
		return mDb.query(Tables.ENTRIES, new String[] { BaseColumns._ID,
				EntryColumns.KEY_TYPE, EntryColumns.KEY_NAME,
				EntryColumns.KEY_URL, EntryColumns.KEY_ICONPATH,
				EntryColumns.KEY_ICONURL, EntryColumns.KEY_CPRICE,
				EntryColumns.KEY_RPRICE, EntryColumns.KEY_RATING,
				EntryColumns.KEY_CRATING, EntryColumns.KEY_MOVLENGTH,
				EntryColumns.KEY_CREATOR, EntryColumns.KEY_ALBLENGTH,
				EntryColumns.KEY_NUMTRACKS, EntryColumns.KEY_DATE,
				EntryColumns.KEY_PCOUNT, EntryColumns.KEY_TAGS }, null, null,
				null, null, null);
	}

	/**
	 * Retrieves a {@link Cursor} pointing to all entries in the database that
	 * match the given {@link String} query.
	 * 
	 * @param query
	 *            {@link String} query to filter the database entries by
	 * @return {@link Cursor} pointing to all entries in the database that match
	 *         the given {@link String} query
	 */
	public Cursor getEntryMatches(String query) {
		String[] columns = new String[] { BaseColumns._ID,
				EntryColumns.KEY_TYPE, EntryColumns.KEY_NAME,
				"wlentries." + EntryColumns.KEY_URL, EntryColumns.KEY_ICONPATH,
				EntryColumns.KEY_ICONURL, EntryColumns.KEY_CPRICE,
				EntryColumns.KEY_RPRICE, EntryColumns.KEY_RATING,
				EntryColumns.KEY_CRATING, EntryColumns.KEY_MOVLENGTH,
				EntryColumns.KEY_CREATOR, EntryColumns.KEY_ALBLENGTH,
				EntryColumns.KEY_NUMTRACKS, EntryColumns.KEY_DATE,
				EntryColumns.KEY_PCOUNT, EntryColumns.KEY_TAGS };

		return mDb.query(Tables.SEARCH_JOIN_ENTRIES, columns,
				Tables.ENTRIES_SEARCH + " MATCH " + query, null, null, null,
				null);
	}

	/**
	 * Function to get a specific entry from the database
	 * 
	 * @param rowId
	 *            The integer id of the entry to retrieve
	 * @return A Cursor pointing to the desired entry
	 * @throws SQLException
	 *             If the query to the database fails
	 */
	public Cursor fetchEntry(int rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, Tables.ENTRIES, new String[] {
				BaseColumns._ID, EntryColumns.KEY_TYPE, EntryColumns.KEY_NAME,
				EntryColumns.KEY_URL, EntryColumns.KEY_ICONPATH,
				EntryColumns.KEY_ICONURL, EntryColumns.KEY_CPRICE,
				EntryColumns.KEY_RPRICE, EntryColumns.KEY_RATING,
				EntryColumns.KEY_CRATING, EntryColumns.KEY_MOVLENGTH,
				EntryColumns.KEY_CREATOR, EntryColumns.KEY_ALBLENGTH,
				EntryColumns.KEY_NUMTRACKS, EntryColumns.KEY_DATE,
				EntryColumns.KEY_PCOUNT, EntryColumns.KEY_TAGS },
				BaseColumns._ID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Updates the entry at the given rowId using the parameters of the given
	 * entry
	 * 
	 * @param rowId
	 *            The integer id of the entry to update
	 * @param ent
	 *            The entry whose values to use for updating
	 * @return True if the entry was successfully updated, false otherwise
	 */
	public boolean updateEntry(int rowId, WLEntry ent) {
		return mDb.update(Tables.ENTRIES, createValues(ent), BaseColumns._ID
				+ "=" + rowId, null) > 0;
	}

	public int updateEntry(int rowId, ContentValues values) {
		return mDb.update(Tables.ENTRIES, values,
				BaseColumns._ID + "=" + rowId, null);
	}

	/**
	 * Creates a ContentValues object for the given entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @return The final ContentValues object
	 */
	public static ContentValues createValues(WLEntry ent) {
		ContentValues values = new ContentValues();
		values.put(EntryColumns.KEY_TYPE,
				WLEntryType.getTypeString(ent.getType()));
		values.put(EntryColumns.KEY_NAME, ent.getTitle());
		values.put(EntryColumns.KEY_URL, ent.getURL());
		values.put(EntryColumns.KEY_ICONPATH, ent.getIconPath());
		values.put(EntryColumns.KEY_ICONURL, ent.getIconUrl());
		values.put(EntryColumns.KEY_TAGS, buildTags(ent.getTags()));
		switch (ent.getType()) {
		case APP:
			return createAppValues((WLAppEntry) ent, values);
		case BOOK:
			return createBookValues((WLBookEntry) ent, values);
		case MOVIE:
			return createMovieValues((WLMovieEntry) ent, values);
		case MUSIC_ARTIST:
			return createArtistValues((WLArtistEntry) ent, values);
		case MUSIC_ALBUM:
			return createAlbumValues((WLAlbumEntry) ent, values);
		}

		// This is bad. But should never happen *Crosses fingers*
		// That probably means, it'll happen
		return null;
	}

	/**
	 * Function to build a comma separated string of tags of an entry
	 * 
	 * @param tags
	 *            Array of String tags to use
	 * @return List of tags as a single, comma separated String
	 */
	private static String buildTags(String[] tags) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < tags.length; ++i) {
			sb.append(tags[i]);
			sb.append(",");
		}

		return sb.toString();
	}

	/**
	 * Creates a contentValues object for the given PricedEntry
	 * 
	 * @param ent
	 *            The priced entry to get the price values from
	 * @param values
	 *            The content values to add the values to
	 * @return The content values object with price/rating information added
	 */
	private static ContentValues createPricedValues(WLPricedEntry ent,
			ContentValues values) {
		values.put(EntryColumns.KEY_CPRICE, ent.getCurrentPrice());
		values.put(EntryColumns.KEY_RPRICE, ent.getRegularPrice());
		values.put(EntryColumns.KEY_RATING, ent.getRating());

		return values;
	}

	/**
	 * Creates a ContentValues object for the given APP entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @param values
	 *            The base values context passed from createValues()
	 * @return The final ContentValues object
	 */
	private static ContentValues createAppValues(WLAppEntry ent,
			ContentValues values) {
		values = createPricedValues(ent, values);
		values.put(EntryColumns.KEY_CREATOR, ent.getDeveloper());

		return values;
	}

	/**
	 * Creates a ContentValues object for the given BOOK entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @param values
	 *            The base values context passed from createValues()
	 * @return The final ContentValues object
	 */
	private static ContentValues createBookValues(WLBookEntry ent,
			ContentValues values) {
		values = createPricedValues(ent, values);
		values.put(EntryColumns.KEY_CREATOR, ent.getAuthor());
		values.put(EntryColumns.KEY_PCOUNT, ent.getPageCount());
		values.put(EntryColumns.KEY_DATE, ent.getPublishDate());

		return values;
	}

	/**
	 * Creates a ContentValues object for the given MOVIE entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @param values
	 *            The base values context passed from createValues()
	 * @return The final ContentValues object
	 */
	private static ContentValues createMovieValues(WLMovieEntry ent,
			ContentValues values) {
		values = createPricedValues(ent, values);
		values.put(EntryColumns.KEY_CRATING, ent.getContentRating());
		values.put(EntryColumns.KEY_CREATOR, ent.getDirector());
		values.put(EntryColumns.KEY_MOVLENGTH, ent.getMovieLength());

		return values;
	}

	/**
	 * Creates a ContentValues object for the given ARTIST entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @param values
	 *            The base values context passed from createValues()
	 * @return The final ContentValues object
	 */
	private static ContentValues createArtistValues(WLArtistEntry ent,
			ContentValues values) {
		// Sort of an oddity, creator here is set to genres
		values.put(EntryColumns.KEY_CREATOR, ent.getGenres());

		return values;
	}

	/**
	 * Creates a ContentValues object for the given ALBUM entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @param values
	 *            The base values context passed from createValues()
	 * @return The final ContentValues object
	 */
	private static ContentValues createAlbumValues(WLAlbumEntry ent,
			ContentValues values) {
		values = createPricedValues(ent, values);
		values.put(EntryColumns.KEY_CREATOR, ent.getArtist());
		values.put(EntryColumns.KEY_ALBLENGTH, ent.getLength());
		values.put(EntryColumns.KEY_NUMTRACKS, ent.getTrackCount());
		values.put(EntryColumns.KEY_DATE, ent.getReleaseDate());

		return values;
	}
}
