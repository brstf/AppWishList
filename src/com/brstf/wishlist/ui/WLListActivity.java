package com.brstf.wishlist.ui;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.brstf.wishlist.R;
import com.brstf.wishlist.provider.WLEntryContract;
import com.brstf.wishlist.provider.WLProvider;
import com.brstf.wishlist.provider.WLEntryContract.TagColumns;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WLListActivity extends BaseActivity implements
		OnNavigationListener {
	private TagAdapter mAdapter = null;
	private WLListFragment mFrag = null;
	public static final String KEY_TAGID = "TAGID";
	private int mTagId = 0;
	private final int mLoader = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mAdapter = new TagAdapter(getApplicationContext());
		getSupportLoaderManager()
				.restartLoader(mLoader, null, mLoaderCallbacks);

		if (findViewById(R.id.fragment_container) != null) {
			mTagId = getIntent().getIntExtra(KEY_TAGID, 0);

			// If we have a saved state, restore the previous filter tag
			if (savedInstanceState != null) {
				mTagId = savedInstanceState.getInt(KEY_TAGID);
			}

			mFrag = new WLListFragment();
			mFrag.setArguments(BaseActivity
					.intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, mFrag).commit();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(mAdapter, this);
		
		getSupportActionBar().setSelectedNavigationItem(mTagId);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		mAdapter.getCursor().moveToPosition(itemPosition);
		String tag = mAdapter.getCursor().getString(
				mAdapter.getCursor().getColumnIndex(TagColumns.KEY_TAG));
		tag = tag.toLowerCase();

		mTagId = itemPosition;

		onTagSelected(tag);

		return true;
	}

	public void onTagSelected(String tag) {
		// Construct a bundle with an entries/tag URI
		final Bundle args = new Bundle();
		if (tag.equals("all")) {
			args.putParcelable("_uri", WLEntryContract.Entries.CONTENT_URI);
		} else {
			args.putParcelable("_uri", WLEntryContract.Entries.CONTENT_URI
					.buildUpon().appendPath("tag").appendPath(tag).build());
		}
		mFrag.reloadFromArguments(args);
	}

	@Override
	public void onStop() {
		super.onStop();
		mFrag.onStop();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// If the app is exiting and we have to save state, save the current
		// filter tag so we can restore it
		outState.putInt(KEY_TAGID, mTagId);

		super.onSaveInstanceState(outState);
	}

	private class TagAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;

		public TagAdapter(Context context) {
			super(context, null, false);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String tag = cursor.getString(cursor
					.getColumnIndex(TagColumns.KEY_TAG));
			view.setTag(tag);
			((TextView) view).setText(tag);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(android.R.layout.simple_list_item_1,
					parent, false);
		}
	}

	private final LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(WLListActivity.this,
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
