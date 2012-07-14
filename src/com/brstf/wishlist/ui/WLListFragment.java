package com.brstf.wishlist.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.brstf.wishlist.R;
import com.brstf.wishlist.WLEntries;
import com.brstf.wishlist.WLEntries.WLChangedListener;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.util.SimpleCursorLoader;
import com.brstf.wishlist.widgets.SquareImageView;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Class to display the the list of wished items
 * 
 * @author brstf
 */
public class WLListFragment extends SherlockListFragment implements
		WLChangedListener {
	// Hash map mapping icon name to already loaded icons
	private static ThumbnailCache mIconCache = null;
	public static final String EXTRA_TAG = "filter_tag";

	private static class ViewHolder {
		public LinearLayout background;
		public SquareImageView icon;
		public TextView title;
		public TextView creator;
		public TextView price;
	}

	private static class IconTask extends AsyncTask<Activity, Void, Bitmap> {
		private String mIconPath;
		private SquareImageView mImageView;

		public IconTask(String iconPath, ViewHolder holder) {
			mIconPath = iconPath;
			mImageView = holder.icon;
			mImageView.setTag(this);
		}

		@Override
		protected Bitmap doInBackground(Activity... params) {
			// Try to read in the icon file
			FileInputStream fis = null;
			try {
				fis = params[0].getBaseContext().openFileInput(mIconPath);
				// If successful, set the icon
				final Bitmap bm = BitmapFactory.decodeStream(fis);
				fis.close();
				return bm;
			} catch (IOException e) {
				// Catch any exceptions
				e.printStackTrace();
			} finally {
				// Finally, close our input stream
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
					// Catch any nasty IO Exceptions
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			mIconCache.put(mIconPath, bitmap);
			if (bitmap == null)
				return;
			if (mImageView.getTag() == this) {
				mImageView.setImageBitmap(bitmap);
			}
		}
	}

	public static final class WLCursorLoader extends SimpleCursorLoader {
		private WLDbAdapter mDbHelper;

		public WLCursorLoader(Context context, WLDbAdapter dbhelper) {
			super(context);
			mDbHelper = dbhelper;
		}

		@Override
		public Cursor loadInBackground() {
			String[] columns = WLEntryContract.EntryQuery.columns;
			String selection = WLDbAdapter.KEY_TYPE + " <> ?";
			String[] selectionArgs = { WLEntryType
					.getTypeString(WLEntryType.PENDING) };
			return mDbHelper.query(true, columns, selection, selectionArgs,
					null, null, null, null);
		}
	}

	public class WLListAdapter extends CursorAdapter {
		private SparseBooleanArray mSelected = new SparseBooleanArray();
		private final LayoutInflater mInflater;

		public WLListAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			mInflater = LayoutInflater.from(context);
		}

		public WLListAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mInflater = LayoutInflater.from(context);
		}

		/**
		 * Toggles the selected state of the entry with the passed in id.
		 * 
		 * @param id
		 *            Integer id of the entry to toggle
		 */
		public void toggleSelected(int id) {
			if (!mSelected.get(id, false)) {
				mSelected.put(id, true);
			} else {
				mSelected.delete(id);
			}
			notifyDataSetChanged();
		}

		/**
		 * Clears the list of selected items.
		 */
		public void clearSelected() {
			mSelected.clear();
		}

		/**
		 * Gets the number of selected items in the list.
		 * 
		 * @return Number of selected items in the list
		 */
		public int getSelectedCount() {
			return mSelected.size();
		}

		/**
		 * Gets the SparseBooleanArray holding information about which items in
		 * the list are selected.
		 * 
		 * @return SparseBooleanArray with positions in the list mapped to true
		 *         if the entry at that index is selected
		 */
		public SparseBooleanArray getSelected() {
			return mSelected;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();

			// Fill in the view holder
			fillViewHolder(holder, cursor);
		}

		@Override
		public View newView(Context c, Cursor cursor, ViewGroup parent) {
			View row = mInflater.inflate(R.layout.row, parent, false);

			// If the convert view was null, we need to construct a new view
			// holder for it
			ViewHolder holder = new ViewHolder();
			holder.background = (LinearLayout) row
					.findViewById(R.id.rowbackground);
			holder.icon = (SquareImageView) row.findViewById(R.id.icon);
			holder.title = (TextView) row.findViewById(R.id.title);
			holder.creator = (TextView) row.findViewById(R.id.creator);
			holder.price = (TextView) row.findViewById(R.id.price);

			row.setTag(holder);
			return row;
		}

		private void fillViewHolder(ViewHolder holder, Cursor cursor) {
			// If the icon is cached, simply retrieve the cached icon
			int pathindex = cursor.getColumnIndex(WLDbAdapter.KEY_ICONPATH);
			String iconPath = cursor.getString(pathindex);
			if (mIconCache.get(iconPath) != null) {
				holder.icon.setImageBitmap(mIconCache.get(iconPath));
			} else {
				// Otherwise, set the place holder and spin off an asynctask to
				// load in the icon
				new IconTask(iconPath, holder).execute(getActivity());

				// TODO: enable this on compatible software
				// .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, getActivity());

				int typeindex = cursor.getColumnIndex(WLDbAdapter.KEY_TYPE);
				WLEntryType type = WLEntryType.getTypeFromString(cursor
						.getString(typeindex));
				switch (type) {
				case APP:
					holder.icon.setImageDrawable(appsPh);
					break;
				case MUSIC_ARTIST:
				case MUSIC_ALBUM:
					holder.icon.setImageDrawable(musicPh);
					break;
				case BOOK:
					holder.icon.setImageDrawable(booksPh);
					break;
				case MOVIE:
					holder.icon.setImageDrawable(moviesPh);
					break;
				}
			}

			// Set the title and creator
			holder.title.setText(cursor.getString(cursor
					.getColumnIndex(WLDbAdapter.KEY_NAME)));
			holder.creator.setText(cursor.getString(cursor
					.getColumnIndex(WLDbAdapter.KEY_CREATOR)));

			// Set the price if it exists
			if (cursor.getString(cursor.getColumnIndex(WLDbAdapter.KEY_CPRICE)) != null) {
				holder.price.setText(getPriceText(cursor.getFloat(cursor
						.getColumnIndex(WLDbAdapter.KEY_CPRICE))));
			} else {
				holder.price.setText(null);
			}

			// Set the selected background
			if (mSelected.get(cursor.getPosition())) {
				holder.background.setBackgroundColor(0x7820A1A4);
			} else {
				holder.background.setBackgroundColor(Color.TRANSPARENT);
			}
		}

		/**
		 * Function to get the String representation of a price. For example,
		 * 5.98f == "$5.98", and 0f == "Free"
		 * 
		 * @param price
		 *            The price to convert to a string
		 * @return String representation of that value for prices
		 */
		private String getPriceText(float price) {
			// If the price passed in was an actual cost (i.e. not free)
			if (price > 0.0f) {
				// Format the string to be a dollar amount (i.e. "$X.XX")
				return String.format("$%.2f", price);
			} else {
				// if free, return "Free"
				return "Free";
			}
		}
	}

	private static final String TAG = "WLListFragment";
	private static final int LOADER_CURSOR = 1;
	private WLListAdapter mListAdapter = null;
	private String filtertag = null;
	private Drawable musicPh = null;
	private Drawable appsPh = null;
	private Drawable moviesPh = null;
	private Drawable booksPh = null;
	private WLDbAdapter mDbHelper = null;
	private ActionMode lamode = null;

	private WLDbAdapter getHelper() {
		if (mDbHelper == null) {
			mDbHelper = new WLDbAdapter(this.getSherlockActivity());
		}
		return mDbHelper;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new WLDbAdapter(this.getSherlockActivity());
		mDbHelper.open();

		musicPh = getResources().getDrawable(R.drawable.musicph);
		appsPh = getResources().getDrawable(R.drawable.appsph);
		moviesPh = getResources().getDrawable(R.drawable.moviesph);
		booksPh = getResources().getDrawable(R.drawable.booksph);

		// On create, make a new hashmap for the icons that will be filled in
		final ActivityManager am = (ActivityManager) getActivity()
				.getSystemService(Context.ACTIVITY_SERVICE);
		final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
		mIconCache = new ThumbnailCache(memoryClassBytes / 2);

		// Get the arguments passed in
		Bundle args = this.getArguments();
		if (args != null) {
			// If there were arguments, grab the filter tag to filter the list
			// by
			filtertag = args.getString(EXTRA_TAG);
		}

		// Instantiate the adapter and use it
		mListAdapter = new WLListAdapter(this.getSherlockActivity(), null, true);
		setListAdapter(mListAdapter);

		this.getSherlockActivity().getSupportLoaderManager()
				.initLoader(LOADER_CURSOR, null, mLoaderCallbacks);
	}

	@Override
	public void onStart() {
		super.onStart();

		WLEntries.getInstance().setWLChangedListener(this);
		if (mListAdapter.getCursor() != null) {
			getLoaderManager().restartLoader(LOADER_CURSOR, null,
					mLoaderCallbacks);
		}

		this.getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		this.getListView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						Log.d(TAG, "Long click");
						lamode = WLListFragment.this.getSherlockActivity()
								.startActionMode(new ListActionMode());
						return false;
					}
				});
	}

	@Override
	public void onStop() {
		super.onStop();

		// Clear out the icon list, and clear the callback
		mIconCache.evictAll();
		WLEntries.getInstance().setWLChangedListener(null);
	}

	/**
	 * When an entry in the list is clicked, open up the Play listing for it
	 */
	@Override
	public void onListItemClick(android.widget.ListView l, View v,
			int position, long id) {
		if (lamode == null) {
			// Create the url to open
			Cursor cursor = mListAdapter.getCursor();
			cursor.moveToPosition(position);
			int urlindex = cursor.getColumnIndex(WLDbAdapter.KEY_URL);

			Uri webpage = Uri.parse(cursor.getString(urlindex));

			// Create the intent and start it
			// TODO: Should this only start the Play store?
			// TODO: This shouldn't be able to Add to Wishlist
			Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
			startActivity(webIntent);
		} else {
			// We're in our ActionMode, so check off the clicked item
			WLListFragment.this.mListAdapter.toggleSelected(position);
			final int count = WLListFragment.this.mListAdapter
					.getSelectedCount();
			lamode.setTitle(Integer.toString(count));
			if (count == 0) {
				lamode.finish();
			}
		}
	}

	@Override
	public void onDataSetChanged() {
		// Reload the list
		getLoaderManager().restartLoader(LOADER_CURSOR, null, mLoaderCallbacks);
	}

	public void filter(String tag) {
		filtertag = tag;
		onDataSetChanged();
	}

	public String getFilter() {
		return filtertag;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
		mListAdapter.swapCursor(null);
	}

	/**
	 * Class to instantiate and create the custom action mode for the list.
	 * 
	 * @author brstf
	 */
	private final class ListActionMode implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(
				com.actionbarsherlock.view.ActionMode mode,
				com.actionbarsherlock.view.MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				final SparseBooleanArray sba = WLListFragment.this.mListAdapter
						.getSelected();
				final int checkedCount = sba.size();
				int[] ids = new int[checkedCount];
				String[] paths = new String[checkedCount];

				// Retrieve the database ids of all checked items
				int position = 0;
				Cursor cursor = mListAdapter.getCursor();
				for (int i = 0; i < checkedCount; ++i) {
					position = sba.keyAt(i);
					cursor.moveToPosition(position);
					ids[i] = cursor.getInt(cursor
							.getColumnIndex(BaseColumns._ID));

					int pathindex = cursor
							.getColumnIndex(WLDbAdapter.KEY_ICONPATH);
					paths[i] = cursor.getString(pathindex);
				}

				// Loop through each id, and remove it from the database
				mDbHelper.beginTransaction();
				for (int i = 0; i < checkedCount; ++i) {
					// Remove from the database and delete icon
					mDbHelper.deleteEntry(ids[i]);
					File f = new File(WLListFragment.this.getSherlockActivity()
							.getBaseContext().getFilesDir().getAbsolutePath()
							+ "/" + paths[i]);
					if (f.exists()) {
						f.delete();
					}
				}
				mDbHelper.setTransactionSuccessful();
				mDbHelper.endTransaction();

				// Remove references
				ids = null;
				paths = null;

				// Reload the list
				getLoaderManager().restartLoader(LOADER_CURSOR, null,
						mLoaderCallbacks);
			}
			mode.finish();
			return true;
		}

		@Override
		public boolean onCreateActionMode(
				com.actionbarsherlock.view.ActionMode mode,
				com.actionbarsherlock.view.Menu menu) {
			WLListFragment.this.getSherlockActivity().getSupportMenuInflater()
					.inflate(R.menu.actionmenu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(
				com.actionbarsherlock.view.ActionMode mode) {
			WLListFragment.this.mListAdapter.clearSelected();
			WLListFragment.this.lamode = null;
			WLListFragment.this.mListAdapter.notifyDataSetChanged();
		}

		@Override
		public boolean onPrepareActionMode(
				com.actionbarsherlock.view.ActionMode mode,
				com.actionbarsherlock.view.Menu menu) {
			return false;
		}
	};

	/**
	 * Sets the items loaded in this adapter to search results from the given
	 * query {@link Uri}.
	 * 
	 * @param searchUri
	 *            {@link Uri} containing the search query
	 */
	public void loadFromSearch(Uri searchUri) {
		Bundle args = new Bundle();
		args.putParcelable("_uri", searchUri);
		getSherlockActivity().getSupportLoaderManager().restartLoader(
				LOADER_CURSOR, args, mLoaderCallbacks);
	}

	// //////////////////////////////
	// Loader callbacks
	//

	private final LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			if (args != null) {
				Uri searchUri = (Uri) args.getParcelable("_uri");
				return new CursorLoader(getSherlockActivity(), searchUri,
						WLEntryContract.EntryQuery.columns, null, null, null);
			}
			return new WLCursorLoader(
					WLListFragment.this.getSherlockActivity(), getHelper());
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mListAdapter.swapCursor(data);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mListAdapter.swapCursor(null);
		}
	};

	private static class ThumbnailCache extends LruCache<String, Bitmap> {
		public ThumbnailCache(int maxSizeBytes) {
			super(maxSizeBytes);
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
		}
	}
}
