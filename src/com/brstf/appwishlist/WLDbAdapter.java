package com.brstf.appwishlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WLDbAdapter  {
	public static final String KEY_NAME = "name";
	public static final String KEY_URL = "url";
	public static final String KEY_ICON = "icon";
	public static final String KEY_CPRICE = "cprice";
	public static final String KEY_OPRICE = "oprice";
	public static final String KEY_ROWID = "_id";
	
	private static final String TAG = "WLDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static final String DATABASE_CREATE = 
			"create table wlapps (_id integer primary key autoincrement, " +
			"name text not null, url text not null, icon text not null, " +
			"cprice float, oprice float);";
	
	private static final String DATABASE_NAME = "wldata";
	private static final String DATABASE_TABLE = "wlapps";
	private static final int DATABASE_VERSION = 2;
	
	private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " +
					newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS wlapps");
			onCreate(db);
		}
	}
	
	public WLDbAdapter(Context context) {
		this.mCtx = context;
	}
	
	public WLDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	public long createEntry(WLAppEntry ent) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, ent.getName());
		values.put(KEY_URL, ent.getURL());
		values.put(KEY_ICON, ent.getIconPath());
		values.put(KEY_CPRICE, ent.getCurrentPrice());
		values.put(KEY_OPRICE, ent.getOriginalPrice());
		
		return mDb.insert(DATABASE_TABLE, null, values);
	}
	
	public boolean deleteEntry(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public Cursor fetchAllEntries() {
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME, 
				KEY_URL, KEY_ICON, KEY_CPRICE, KEY_OPRICE}, null, null, null,
				null, null);
	}
	
	public Cursor fetchEntry(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] 
				{KEY_ROWID, KEY_NAME, KEY_URL, KEY_ICON, KEY_CPRICE, 
				KEY_OPRICE} , KEY_ROWID + "=" + rowId, null, null, 
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public boolean updateEntry(long rowId, WLAppEntry ent) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, ent.getName());
		values.put(KEY_URL, ent.getURL());
		values.put(KEY_ICON, ent.getIconPath());
		values.put(KEY_CPRICE, ent.getCurrentPrice());
		values.put(KEY_OPRICE, ent.getOriginalPrice());
		
		return mDb.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
