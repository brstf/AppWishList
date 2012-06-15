package com.brstf.appwishlist;

import com.brstf.appwishlist.entries.WLAlbumEntry;
import com.brstf.appwishlist.entries.WLAppEntry;
import com.brstf.appwishlist.entries.WLArtistEntry;
import com.brstf.appwishlist.entries.WLBookEntry;
import com.brstf.appwishlist.entries.WLEntry;
import com.brstf.appwishlist.entries.WLEntryType;
import com.brstf.appwishlist.entries.WLMovieEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
	public static final String KEY_ICON = "icon";
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
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "WLDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	// Helper strings to assist in the operation of the SQLite database
	private static final String DATABASE_CREATE = "create table wlentries (_id integer "
			+ "primary key autoincrement, type text not null, name text not null, url "
			+ "text not null, icon text not null, cprice float, rprice float, rating "
			+ "float, crating text, movlength int, creator text, "
			+ "alblength text, numtracks int, date text, pcount int);";

	private static final String DATABASE_NAME = "wldata";
	private static final String DATABASE_TABLE = "wlentries";
	private static final int DATABASE_VERSION = 2;

	// Application context of the activity using the database
	private final Context mCtx;

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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS wlentries");
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
		this.mCtx = context;
	}

	/**
	 * A function to open the SQLite database
	 * 
	 * @return This instance of WLDbAdapter
	 * @throws SQLException
	 *             If the database fails to open
	 */
	public WLDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Function to close the database. Close should always be called after
	 * open(), the database should not be left open.
	 */
	public void close() {
		mDbHelper.close();
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
		return mDb.insert(DATABASE_TABLE, null, createValues(ent));
	}

	/**
	 * Function to delete a specified entry from the database
	 * 
	 * @param rowId
	 *            The long id of the entry in the database to delete
	 * @return True on successful deletion, false if deletion failed
	 */
	public boolean deleteEntry(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Function to get all entries from the database
	 * 
	 * @return Cursor pointing at the first entry in the list of entries
	 */
	public Cursor fetchAllEntries() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TYPE,
				KEY_NAME, KEY_URL, KEY_ICON, KEY_CPRICE, KEY_RPRICE,
				KEY_RATING, KEY_CRATING, KEY_MOVLENGTH, KEY_CREATOR,
				KEY_ALBLENGTH, KEY_NUMTRACKS, KEY_DATE, KEY_PCOUNT }, null,
				null, null, null, null);
	}

	/**
	 * Function to get a specific entry from the database
	 * 
	 * @param rowId
	 *            The long id of the entry to retrieve
	 * @return A Cursor pointing to the desired entry
	 * @throws SQLException
	 *             If the query to the database fails
	 */
	public Cursor fetchEntry(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE,
				new String[] { KEY_ROWID, KEY_TYPE, KEY_NAME, KEY_URL,
						KEY_ICON, KEY_CPRICE, KEY_RPRICE, KEY_RATING,
						KEY_CRATING, KEY_MOVLENGTH, KEY_CREATOR, KEY_ALBLENGTH,
						KEY_NUMTRACKS, KEY_DATE, KEY_PCOUNT }, KEY_ROWID + "="
						+ rowId, null, null, null, null, null);
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
	 *            The rowid of the entry to update
	 * @param ent
	 *            The entry whose values to use for updating
	 * @return True if the entry was successfully updated, false otherwise
	 */
	public boolean updateEntry(long rowId, WLEntry ent) {
		return mDb.update(DATABASE_TABLE, createValues(ent), KEY_ROWID + "="
				+ rowId, null) > 0;
	}

	/**
	 * Creates a ContentValues object for the given entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @return The final ContentValues object
	 */
	private ContentValues createValues(WLEntry ent) {
		ContentValues values = new ContentValues();
		values.put(KEY_TYPE, WLEntryType.getTypeString(ent.getType()));
		values.put(KEY_NAME, ent.getTitle());
		values.put(KEY_URL, ent.getURL());
		values.put(KEY_ICON, ent.getIconPath());
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
	 * Creates a ContentValues object for the given APP entry
	 * 
	 * @param ent
	 *            The entry to create the ContentValues object for
	 * @param values
	 *            The base values context passed from createValues()
	 * @return The final ContentValues object
	 */
	private ContentValues createAppValues(WLAppEntry ent, ContentValues values) {
		values.put(KEY_CPRICE, ent.getCurrentPrice());
		values.put(KEY_RPRICE, ent.getRegularPrice());

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
	private ContentValues createBookValues(WLBookEntry ent, ContentValues values) {
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
	private ContentValues createMovieValues(WLMovieEntry ent,
			ContentValues values) {
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
	private ContentValues createArtistValues(WLArtistEntry ent,
			ContentValues values) {
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
	private ContentValues createAlbumValues(WLAlbumEntry ent,
			ContentValues values) {
		values.put(KEY_CREATOR, ent.getArtist());
		values.put(KEY_ALBLENGTH, ent.getLength());
		values.put(KEY_NUMTRACKS, ent.getTrackCount());
		values.put(KEY_DATE, ent.getReleaseDate());

		return values;
	}
}
