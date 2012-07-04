package com.brstf.wishlist.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brstf.wishlist.R;
import com.brstf.wishlist.WLEntries;
import com.brstf.wishlist.WLEntries.WLChangedListener;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.util.SimpleCursorLoader;
import com.brstf.wishlist.util.WLDbAdapter;
import com.brstf.wishlist.widgets.SquareImageView;

import android.app.Activity;
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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Class to display the the list of wished items
 * 
 * @author brstf
 */
public class WLListFragment extends SherlockListFragment implements
		WLChangedListener, LoaderCallbacks<Cursor> {
	// Hash map mapping icon name to already loaded icons
	private static HashMap<String, Bitmap> icons = null;
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
				fis = params[0].openFileInput(mIconPath);
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
			icons.put(mIconPath, bitmap);
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
			String[] columns = { WLDbAdapter.KEY_ROWID, WLDbAdapter.KEY_TYPE,
					WLDbAdapter.KEY_NAME, WLDbAdapter.KEY_CREATOR,
					WLDbAdapter.KEY_CPRICE, WLDbAdapter.KEY_ICONPATH,
					WLDbAdapter.KEY_ICONURL, WLDbAdapter.KEY_URL };
			String selection = WLDbAdapter.KEY_TYPE + " <> ?";
			String[] selectionArgs = { WLEntryType
					.getTypeString(WLEntryType.PENDING) };
			mDbHelper.open();
			return mDbHelper.query(true, columns, selection, selectionArgs,
					null, null, null, null);
		}
		
		public void closeDb() {
			mDbHelper.close();
		}
	}

	public class WLListAdapter extends CursorAdapter {
		private SparseBooleanArray mSelected;
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
		 * Sets the array of which entries are selected.
		 * 
		 * @param selected
		 *            SparseBooleanArray of all selected entries of the list
		 */
		public void setSelected(SparseBooleanArray selected) {
			mSelected = selected;
			notifyDataSetChanged();
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
			if (icons.containsKey(iconPath)) {
				holder.icon.setImageBitmap(icons.get(iconPath));
			} else {
				// Otherwise, set the place holder and spin off an asynctask to
				// load in the icon
				new IconTask(iconPath, holder).executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR, getSherlockActivity());

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
			if (mSelected != null && mSelected.get(cursor.getPosition())) {
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

	private WLListAdapter mListAdapter = null;
	private String filtertag = null;
	private Drawable musicPh = null;
	private Drawable appsPh = null;
	private Drawable moviesPh = null;
	private Drawable booksPh = null;
	private WLDbAdapter mDbHelper = null;

	private WLDbAdapter getHelper() {
		if (mDbHelper == null) {
			mDbHelper = new WLDbAdapter(this.getSherlockActivity());
		}
		return mDbHelper;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		musicPh = getResources().getDrawable(R.drawable.musicph);
		appsPh = getResources().getDrawable(R.drawable.appsph);
		moviesPh = getResources().getDrawable(R.drawable.moviesph);
		booksPh = getResources().getDrawable(R.drawable.booksph);

		// On create, make a new hashmap for the icons that will be filled in
		icons = new HashMap<String, Bitmap>();

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
				.initLoader(0, null, this);
	}

	@Override
	public void onStart() {
		super.onStart();
		WLEntries.getInstance().setWLChangedListener(this);

		this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(mStarredListener);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Clear out the icon list, and clear the callback
		icons.clear();
		WLEntries.getInstance().setWLChangedListener(null);
	}

	/**
	 * When an entry in the list is clicked, open up the Play listing for it
	 */
	@Override
	public void onListItemClick(android.widget.ListView l, View v,
			int position, long id) {
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
	}

	@Override
	public void onDataSetChanged() {
		mListAdapter.notifyDataSetChanged();
	}

	public void filter(String tag) {
		filtertag = tag;
		onDataSetChanged();
	}

	public String getFilter() {
		return filtertag;
	}

	private MultiChoiceModeListener mStarredListener = new MultiChoiceModeListener() {

		@Override
		public boolean onActionItemClicked(android.view.ActionMode mode,
				android.view.MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				SparseBooleanArray sba = WLListFragment.this.getListView()
						.getCheckedItemPositions();
				int checkedCount = WLListFragment.this.getListView()
						.getCheckedItemCount();
				int[] ids = new int[checkedCount];

				// Retrieve the database ids of all checked items
				int position = 0;
				Cursor cursor = mListAdapter.getCursor();
				for (int i = 0; i < checkedCount; ++i) {
					position = sba.keyAt(i);
					cursor.moveToPosition(position);
					ids[i] = cursor.getInt(cursor
							.getColumnIndex(WLDbAdapter.KEY_ROWID));
				}

				// Loop through each id, and remove it from the database
				mDbHelper.open();
				mDbHelper.beginTransaction();
				for (int id : ids) {
					mDbHelper.deleteEntry(id);
				}
				mDbHelper.setTransactionSuccessful();
				mDbHelper.endTransaction();
				mDbHelper.close();

				// Force reload the list
				// TODO: Better way of doing this?
				WLListFragment.this.getSherlockActivity()
						.getSupportLoaderManager().getLoader(0).forceLoad();
			}
			mode.finish();
			return true;
		}

		@Override
		public boolean onCreateActionMode(android.view.ActionMode mode,
				android.view.Menu menu) {
			WLListFragment.this.getActivity().getMenuInflater()
					.inflate(R.menu.actionmenu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(android.view.ActionMode mode) {
		}

		@Override
		public boolean onPrepareActionMode(android.view.ActionMode mode,
				android.view.Menu menu) {
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(android.view.ActionMode mode,
				int position, long id, boolean checked) {
			final int count = WLListFragment.this.getListView()
					.getCheckedItemCount();
			WLListFragment.this.mListAdapter.setSelected(WLListFragment.this
					.getListView().getCheckedItemPositions());
			mode.setTitle(Integer.toString(count));
		}
	};

	// //////////////////////////////
	// Loader callbacks
	//

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new WLCursorLoader(this.getSherlockActivity(), getHelper());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mListAdapter.swapCursor(data);
		((WLCursorLoader) loader).closeDb();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mListAdapter.swapCursor(null);
	}
}
