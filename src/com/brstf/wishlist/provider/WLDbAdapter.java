package com.brstf.wishlist.provider;

import com.brstf.wishlist.entries.WLAlbumEntry;
import com.brstf.wishlist.entries.WLAppEntry;
import com.brstf.wishlist.entries.WLArtistEntry;
import com.brstf.wishlist.entries.WLBookEntry;
import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.entries.WLMovieEntry;
import com.brstf.wishlist.entries.WLPricedEntry;

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
	public static final String KEY_TYPE = "type";
	public static final String KEY_NAME = "name";
	public static final String KEY_URL = "url";
	public static final String KEY_ICONPATH = "iconpath";
	public static final String KEY_ICONURL = "iconurl";
	public static final String KEY_CPRICE = "cprice";
	public static final String KEY_RPRICE = "rprice";
	public static final String KEY_RATING = "rating";
	public static final String KEY_CRATING = "crating"; // Content rating (ex:
														// PG-13)
	public static final String KEY_MOVLENGTH = "movlength";
	public static final String KEY_CREATOR = "creator"; // App maker, movie
														// director, artist,
														// author
	public static final String KEY_ALBLENGTH = "alblength";
	public static final String KEY_NUMTRACKS = "numtracks";
	public static final String KEY_DATE = "date"; // Publish date, release date
	public static final String KEY_PCOUNT = "pcount";
	public static final String KEY_TAGS = "tags";

	public static final String SEARCH_BODY = "body";

	public static final String KEY_TAG = "tag";

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
	}

	private interface Subquery {
		String ENTRIES_BODY = "(coalesce(new." + KEY_NAME
				+ ", '')||'; '||coalesce(new." + KEY_CREATOR
				+ ", '')||'; '||coalesce(new." + KEY_TAGS + ", ''))";
	}

	private interface Qualified {
		String ENTRIES_SEARCH = Tables.ENTRIES_SEARCH + "(" + KEY_URL + ","
				+ SEARCH_BODY + ")";
		String ENTRIES_SEARCH_ID = Tables.ENTRIES_SEARCH + "." + KEY_URL;
	}

	private interface References {
		String URL_ID = "REFERENCES " + Tables.ENTRIES + "(" + KEY_URL + ")";
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
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_URL
					+ " TEXT NOT NULL, " + SEARCH_BODY + " TEXT NOT NULL, "
					+ References.URL_ID + "," + "UNIQUE (" + KEY_URL
					+ ") ON CONFLICT REPLACE,tokenize=porter)");

			// Create triggers
			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_SEARCH_INSERT
					+ " AFTER INSERT ON " + Tables.ENTRIES
					+ " BEGIN INSERT INTO " + Qualified.ENTRIES_SEARCH
					+ " VALUES(new." + KEY_URL + ", " + Subquery.ENTRIES_BODY
					+ ");END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_SEARCH_DELETE
					+ " AFTER DELETE ON " + Tables.ENTRIES
					+ " BEGIN DELETE FROM " + Tables.ENTRIES_SEARCH + " WHERE "
					+ Qualified.ENTRIES_SEARCH_ID + "=old." + KEY_URL
					+ "; END;");

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_SEARCH_UPDATE
					+ " AFTER UPDATE ON " + Tables.ENTRIES + " BEGIN UPDATE "
					+ Tables.ENTRIES_SEARCH + " SET " + SEARCH_BODY + " = "
					+ Subquery.ENTRIES_BODY + " WHERE " + KEY_URL + " = old."
					+ KEY_URL + "; END;");

			// Create the table to store tag / tag count information
			db.execSQL("CREATE TABLE " + Tables.ENTRIES_TAGS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ KEY_TAG + " TEXT NOT NULL, " + BaseColumns._COUNT
					+ " INTEGER, UNIQUE (" + KEY_TAG + ") ON CONFLICT IGNORE)");

			ContentValues cv = new ContentValues();
			cv.put(KEY_TAG, "all");
			cv.put(BaseColumns._COUNT, 0);
			db.insert(Tables.ENTRIES_TAGS, null, cv);

			db.execSQL("CREATE TRIGGER " + Triggers.ENTRIES_TAGS_INSERT
					+ " AFTER INSERT ON " + Tables.ENTRIES + " BEGIN UPDATE "
					+ Tables.ENTRIES_TAGS + " SET " + BaseColumns._COUNT
					+ " = " + BaseColumns._COUNT + " + 1 WHERE " + KEY_TAG
					+ " == 'all'; END;");
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
		Cursor c = mDb.query(Tables.ENTRIES_TAGS, new String[] { KEY_TAG },
				KEY_TAG + " = ?", new String[] { tag }, null, null, null);

		// If the tag table didn't have this tag, add it
		if (!c.moveToFirst()) {
			ContentValues cv = new ContentValues();
			cv.put(KEY_TAG, tag);
			cv.put(BaseColumns._COUNT, 0);
			mDb.insert(Tables.ENTRIES_TAGS, null, cv);
		}

		// Now update the count and add the tag
		c = mDb.query(true, Tables.ENTRIES, new String[] { KEY_TAGS }, KEY_URL
				+ " = ?", new String[] { url }, null, null, null, null);
		c.moveToFirst();
		String tags = c.getString(c.getColumnIndex(KEY_TAGS));
		if (tags == null) {
			tags = tag;
		} else {
			tags += "," + tag;
		}

		ContentValues values = new ContentValues();
		values.put(KEY_TAGS, tags);
		mDb.update(Tables.ENTRIES, values, KEY_URL + " = ?",
				new String[] { url });

		c = mDb.query(true, Tables.ENTRIES_TAGS,
				new String[] { BaseColumns._COUNT }, KEY_TAG + " = ?",
				new String[] { tag }, null, null, null, null);
		c.moveToFirst();
		int count = c.getInt(c.getColumnIndex(BaseColumns._COUNT));
		values.clear();
		values.put(BaseColumns._COUNT, count + 1);
		mDb.update(Tables.ENTRIES_TAGS, values, KEY_TAG + " = ?",
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
		String[] columns = { KEY_URL, KEY_NAME };
		String selection = KEY_URL + " = ?";
		String[] selectionArgs = { url };
		Cursor c = mDb.query(true, Tables.ENTRIES, columns, selection,
				selectionArgs, null, null, null, null);

		// If no row was found, return null
		if (!c.moveToFirst()) {
			return null;
		}

		// Otherwise, check the name, if the name was null, it's pending
		String name = c.getString(c.getColumnIndex(KEY_NAME));
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
		String selection = KEY_URL + " = ?";
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
		return mDb.delete(Tables.ENTRIES, BaseColumns._ID + "=" + rowId, null) > 0;
	}

	/**
	 * Function to get all entries from the database
	 * 
	 * @return Cursor pointing at the first entry in the list of entries
	 */
	public Cursor fetchAllEntries() {
		return mDb.query(Tables.ENTRIES, new String[] { BaseColumns._ID,
				KEY_TYPE, KEY_NAME, KEY_URL, KEY_ICONPATH, KEY_ICONURL,
				KEY_CPRICE, KEY_RPRICE, KEY_RATING, KEY_CRATING, KEY_MOVLENGTH,
				KEY_CREATOR, KEY_ALBLENGTH, KEY_NUMTRACKS, KEY_DATE,
				KEY_PCOUNT, KEY_TAGS }, null, null, null, null, null);
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
		String[] columns = new String[] { BaseColumns._ID, KEY_TYPE, KEY_NAME,
				"wlentries." + KEY_URL, KEY_ICONPATH, KEY_ICONURL, KEY_CPRICE,
				KEY_RPRICE, KEY_RATING, KEY_CRATING, KEY_MOVLENGTH,
				KEY_CREATOR, KEY_ALBLENGTH, KEY_NUMTRACKS, KEY_DATE,
				KEY_PCOUNT, KEY_TAGS };

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
				BaseColumns._ID, KEY_TYPE, KEY_NAME, KEY_URL, KEY_ICONPATH,
				KEY_ICONURL, KEY_CPRICE, KEY_RPRICE, KEY_RATING, KEY_CRATING,
				KEY_MOVLENGTH, KEY_CREATOR, KEY_ALBLENGTH, KEY_NUMTRACKS,
				KEY_DATE, KEY_PCOUNT, KEY_TAGS },
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
		values.put(KEY_TYPE, WLEntryType.getTypeString(ent.getType()));
		values.put(KEY_NAME, ent.getTitle());
		values.put(KEY_URL, ent.getURL());
		values.put(KEY_ICONPATH, ent.getIconPath());
		values.put(KEY_ICONURL, ent.getIconUrl());
		values.put(KEY_TAGS, buildTags(ent.getTags()));
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
		values.put(KEY_CPRICE, ent.getCurrentPrice());
		values.put(KEY_RPRICE, ent.getRegularPrice());
		values.put(KEY_RATING, ent.getRating());

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
		values.put(KEY_CREATOR, ent.getDeveloper());

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
		values.put(KEY_CREATOR, ent.getAuthor());
		values.put(KEY_PCOUNT, ent.getPageCount());
		values.put(KEY_DATE, ent.getPublishDate());

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
		values.put(KEY_CRATING, ent.getContentRating());
		values.put(KEY_CREATOR, ent.getDirector());
		values.put(KEY_MOVLENGTH, ent.getMovieLength());

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
		values.put(KEY_CREATOR, ent.getGenres());

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
		values.put(KEY_CREATOR, ent.getArtist());
		values.put(KEY_ALBLENGTH, ent.getLength());
		values.put(KEY_NUMTRACKS, ent.getTrackCount());
		values.put(KEY_DATE, ent.getReleaseDate());

		return values;
	}
}
