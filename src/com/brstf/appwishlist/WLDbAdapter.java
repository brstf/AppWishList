package com.brstf.appwishlist;

import com.brstf.appwishlist.entries.WLAppEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * WLDbAdapter is a class to facilitate interactions with the SQLite database
 * that stores all information regarding the wishlist.
 */
public class WLDbAdapter {
	// Member Variables
	public static final String KEY_NAME = "name";
	public static final String KEY_URL = "url";
	public static final String KEY_ICON = "icon";
	public static final String KEY_CPRICE = "cprice";
	public static final String KEY_OPRICE = "oprice";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "WLDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	// Helper strings to assist in the operation of the SQLite database
	private static final String DATABASE_CREATE = "create table wlapps (_id integer primary key autoincrement, "
			+ "name text not null, url text not null, icon text not null, "
			+ "cprice float, oprice float);";

	private static final String DATABASE_NAME = "wldata";
	private static final String DATABASE_TABLE = "wlapps";
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
			db.execSQL("DROP TABLE IF EXISTS wlapps");
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
	 * passed in WLAppEntry
	 * 
	 * @param ent
	 *            The WLAppEntry to add to the database
	 * @return The long id of the new entry in the database
	 */
	public long createEntry(WLAppEntry ent) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, ent.getTitle());
		values.put(KEY_URL, ent.getURL());
		values.put(KEY_ICON, ent.getIconPath());
		values.put(KEY_CPRICE, ent.getCurrentPrice());
		values.put(KEY_OPRICE, ent.getRegularPrice());

		return mDb.insert(DATABASE_TABLE, null, values);
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
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME,
				KEY_URL, KEY_ICON, KEY_CPRICE, KEY_OPRICE }, null, null, null,
				null, null);
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
				new String[] { KEY_ROWID, KEY_NAME, KEY_URL, KEY_ICON,
						KEY_CPRICE, KEY_OPRICE }, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Function to update a specific entry in the database with new values
	 * 
	 * @param rowId
	 *            The long id of the entry to update
	 * @param ent
	 *            A WLAppEntry holding all of the new values
	 * @return True if update was successful, false if update failed
	 */
	public boolean updateEntry(long rowId, WLAppEntry ent) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, ent.getTitle());
		values.put(KEY_URL, ent.getURL());
		values.put(KEY_ICON, ent.getIconPath());
		values.put(KEY_CPRICE, ent.getCurrentPrice());
		values.put(KEY_OPRICE, ent.getRegularPrice());

		return mDb
				.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
