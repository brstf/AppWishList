package com.brstf.wishlist.ui;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLProvider;
import com.brstf.wishlist.provider.WLEntryContract.TagColumns;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment for the Home screen: The logo, an all button, and buttons for each
 * category
 */
public class HomeFragment extends SherlockListFragment {
	private OnTileSelectedListener mCallback;

	public interface OnTileSelectedListener {
		/**
		 * Called by WLHomeFragment when a tag tile is selected
		 * 
		 * @param tag
		 *            Tag on the tile selected
		 */
		public void onTagSelected(String tag, int pos);
	}

	public class HomeAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;

		public HomeAdapter(Context context) {
			super(context, null, false);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String tag = cursor.getString(cursor
					.getColumnIndex(TagColumns.KEY_TAG));
			view.setTag(tag);
			((TextView) view).setText(tag
					+ " (T:"
					+ Integer.valueOf(cursor.getInt(cursor
							.getColumnIndex(BaseColumns._COUNT)))
					+ " A:"
					+ Integer.valueOf(cursor.getInt(cursor
							.getColumnIndex(TagColumns.KEY_APP_COUNT)))
					+ " Mu:"
					+ Integer.valueOf(cursor.getInt(cursor
							.getColumnIndex(TagColumns.KEY_MUSIC_COUNT)))
					+ " Mo:"
					+ Integer.valueOf(cursor.getInt(cursor
							.getColumnIndex(TagColumns.KEY_MOVIE_COUNT)))
					+ " B:"
					+ Integer.valueOf(cursor.getInt(cursor
							.getColumnIndex(TagColumns.KEY_BOOK_COUNT)))
					+ " Ma:"
					+ Integer.valueOf(cursor.getInt(cursor
							.getColumnIndex(TagColumns.KEY_MAGAZINE_COUNT)))
					+ ")");
		}

		@Override
		public View newView(Context c, Cursor cursor, ViewGroup parent) {
			View row = mInflater.inflate(android.R.layout.simple_list_item_1,
					parent, false);

			return row;
		}
	}

	private HomeAdapter mAdapter;
	private final int mLoader = 1;

	/**
	 * Constructor for the home fragment, initializes all member variables
	 */
	public HomeFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new HomeAdapter(this.getSherlockActivity()
				.getApplicationContext());
		setListAdapter(mAdapter);

		reloadTags();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAdapter.swapCursor(null);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		activity.getContentResolver().registerContentObserver(
				WLEntryContract.Tags.CONTENT_URI, true, mObserver);

		// Attach the callback listener to the activity
		try {
			mCallback = (OnTileSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTileSelectedListener");
		}
	}

	@Override
	public void onListItemClick(android.widget.ListView l, View v,
			int position, long id) {
		String tag = ((String) v.getTag()).toLowerCase();
		if (tag.equals("all")) {
			tag = null;
		}
		mCallback.onTagSelected(tag, position);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		getActivity().getContentResolver().unregisterContentObserver(mObserver);
	}

	private final ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (getSherlockActivity() == null) {
				return;
			}

			Loader<Cursor> loader = getLoaderManager().getLoader(mLoader);
			if (loader != null) {
				loader.forceLoad();
			}
		}
	};
	
	/**
	 * Reloads the tag list.
	 */
	public void reloadTags() {
		getLoaderManager().restartLoader(mLoader, null, mLoaderCallbacks);
	}

	private final LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(getSherlockActivity(),
					WLProvider.BASE_CONTENT_URI.buildUpon().appendPath("tags")
							.build(), WLEntryContract.TagQuery.columns, null,
					null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapter.swapCursor(arg1);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
		}
	};
}
