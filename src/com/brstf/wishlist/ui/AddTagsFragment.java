package com.brstf.wishlist.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brstf.wishlist.R;
import com.brstf.wishlist.provider.WLEntryContract;

public class AddTagsFragment extends SherlockListFragment {
	private final String TAG = "AddTagsFragment";

	public static final String KEY_URLSID = "URLSID";
	private ArrayList<String> mTagsCur = null;
	private ArrayList<String> mTagsSom = null;
	private ArrayList<String> mTagsRem = null;
	private ArrayList<String> mTagsAll = null;

	private ArrayList<String> mTagsCurActive = null;
	private ArrayList<String> mTagsSomActive = null;
	private ArrayList<String> mTagsRemActive = null;

	private final String KEY_CURRENT = "\\,current,\\";
	private final String KEY_INSOME = "\\,insome,\\";
	private final String KEY_REMAIN = "\\,remain,\\";

	public class TagListAdapter extends ArrayAdapter<String> {
		LayoutInflater mInflater;

		public TagListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;
			String displayText = null;
			if (getItem(position).equals(KEY_CURRENT)
					|| getItem(position).equals(KEY_INSOME)
					|| getItem(position).equals(KEY_REMAIN)) {
				row = mInflater.inflate(R.layout.row_tag_title, parent, false);
				row.setClickable(false);
				row.setEnabled(false);
				row.setLongClickable(false);
				row.setFocusable(false);
				((TextView) row).setTextColor(0xFF33B5E5);
				if (getItem(position).equals(KEY_CURRENT)) {
					displayText = "SELECTED TAGS:";
				} else if (getItem(position).equals(KEY_INSOME)) {
					displayText = "TAGS IN SOME ENTRIES:";
				} else if (getItem(position).equals(KEY_REMAIN)) {
					displayText = "OTHER TAGS:";
				}
			} else {
				row = mInflater.inflate(android.R.layout.simple_list_item_1,
						parent, false);
				String selected = getItem(position);
				if (mTagsCurActive.contains(selected)
						&& !mTagsCur.contains(selected)) {
					((TextView) row).setTextColor(0xFF00FF00);
				} else if ((mTagsSomActive.contains(selected) && !mTagsSom
						.contains(selected))
						|| (mTagsRemActive.contains(selected) && !mTagsRem
								.contains(selected))) {
					((TextView) row).setTextColor(0xFFFF0000);
				}
				displayText = getItem(position);
			}
			((TextView) row).setText(displayText);
			return row;
		}

		@Override
		public boolean isEnabled(int position) {
			String selected = getItem(position);
			return !(selected.equals(KEY_CURRENT)
					|| selected.equals(KEY_INSOME) || selected
						.equals(KEY_REMAIN));
		}
	}

	private TagListAdapter mAdapter;

	public AddTagsFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new TagListAdapter(getSherlockActivity()
				.getApplicationContext());
		setListAdapter(mAdapter);

		mTagsCur = new ArrayList<String>();
		mTagsSom = new ArrayList<String>();
		mTagsRem = new ArrayList<String>();

		mTagsCurActive = new ArrayList<String>();
		mTagsSomActive = new ArrayList<String>();
		mTagsRemActive = new ArrayList<String>();

		if (mTagsAll == null) {
			fillMasterTagList();
		}

		fillTagsFromArguments(getArguments());
	}

	@Override
	public void onListItemClick(android.widget.ListView l, View v,
			int position, long id) {
		String selected = mAdapter.getItem(position);

		// Check which active tag list the clicked tag was in, and move it
		// appropriately
		if (mTagsCurActive.contains(selected)) {
			if (mTagsSom.contains(selected)) {
				mTagsSomActive.add(selected);
			} else if (mTagsAll.contains(selected)) {
				mTagsRemActive.add(selected);
			}
			mTagsCurActive.remove(selected);
		} else if (mTagsSomActive.contains(selected)) {
			mTagsCurActive.add(selected);
			mTagsSomActive.remove(selected);
		} else if (mTagsRemActive.contains(selected)) {
			mTagsCurActive.add(selected);
			mTagsRemActive.remove(selected);
		}
		
		sortActiveLists();

		// Notify the adapter that the dataset changed
		notifyAdapterChange();
	}

	/**
	 * Fills the master tag list - the list of all tags currently in the
	 * database.
	 */
	private void fillMasterTagList() {
		mTagsAll = new ArrayList<String>();

		// Query the content provider for all tags
		Cursor c = getSherlockActivity().getContentResolver().query(
				WLEntryContract.Tags.CONTENT_URI, null, null, null, null);
		c.moveToFirst();

		// Loop through all of them and add them to the list, this will
		// make checking for them later easier
		while (!c.isAfterLast()) {
			String tag = c.getString(c
					.getColumnIndex(WLEntryContract.TagColumns.KEY_TAG));
			mTagsAll.add(tag);
			c.moveToNext();
		}
	}

	/**
	 * Fills the adapter contents from passed in arguments. Builds list of
	 * currently used tags in all selected entries, some selected entries, and
	 * all other tags currently in use.
	 * 
	 * @param arguments
	 *            {@link Bundle} of arguments passed in to this fragment
	 *            containing all selected entries from the main list
	 */
	private void fillTagsFromArguments(Bundle arguments) {
		ArrayList<String> urls = new ArrayList<String>();
		urls = getArguments().getStringArrayList(KEY_URLSID);
		HashMap<String, Integer> tagmap = new HashMap<String, Integer>();

		// Loop through each selected url and
		for (String url : urls) {
			Cursor c = getSherlockActivity().getContentResolver().query(
					WLEntryContract.Entries.CONTENT_URI.buildUpon()
							.appendPath("entry").appendPath(url).build(),
					WLEntryContract.EntriesQuery.columns, null, null, null);
			c.moveToFirst();
			String stringtags = c.getString(c
					.getColumnIndex(WLEntryContract.EntryColumns.KEY_TAGS));
			String[] entTags = stringtags.split(",");
			for (String tag : entTags) {
				if (!tagmap.containsKey(tag)) {
					tagmap.put(tag, 0);
				}
				tagmap.put(tag, tagmap.get(tag) + 1);
			}
		}

		// Make sure our lists are clear
		mTagsCur.clear();
		mTagsSom.clear();
		mTagsRem.clear();
		mTagsCurActive.clear();
		mTagsSomActive.clear();
		mTagsRemActive.clear();

		// Now that we have all the tags and their counts, we construct the tag
		// list
		for (String key : tagmap.keySet()) {
			// If every entry had this tag, add it to current
			if (tagmap.get(key) == urls.size()) {
				mTagsCur.add(key);
			} else {
				// Otherwise, add it to some
				mTagsSom.add(key);
			}
		}

		for (String tag : mTagsAll) {
			if (!(mTagsCur.contains(tag) || mTagsSom.contains(tag))
					&& !tag.equals("all")) {
				mTagsRem.add(tag);
			}
		}

		mTagsCurActive.addAll(mTagsCur);
		mTagsSomActive.addAll(mTagsSom);
		mTagsRemActive.addAll(mTagsRem);
		
		sortActiveLists();

		notifyAdapterChange();
	}

	private void sortActiveLists() {
		Collections.sort(mTagsCurActive, new TagComparator());
		Collections.sort(mTagsSomActive, new TagComparator());
		Collections.sort(mTagsRemActive, new TagComparator());
	}

	/**
	 * Called when the adapter's dataset is changed, this function updates the
	 * adapter contents.
	 */
	private void notifyAdapterChange() {
		mAdapter.clear();

		if (mTagsCurActive.size() > 0) {
			mAdapter.add(KEY_CURRENT);
			mAdapter.addAll(mTagsCurActive);
		}

		if (mTagsSomActive.size() > 0) {
			mAdapter.add(KEY_INSOME);
			mAdapter.addAll(mTagsSomActive);
		}

		if (mTagsRemActive.size() > 0) {
			mAdapter.add(KEY_REMAIN);
			mAdapter.addAll(mTagsRemActive);
		}
	}

	private class TagComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			boolean lShifted = isShifted(lhs);
			boolean rShifted = isShifted(rhs);
			
			if( lShifted == rShifted ) {
				return lhs.compareTo(rhs);
			} else {
				if( lShifted ) {
					return -1;
				} else {
					return 1;
				}
			}
		}

		public boolean isShifted(String entry) {
			return (mTagsCurActive.contains(entry) && !mTagsCur.contains(entry))
					|| (mTagsSomActive.contains(entry) && !mTagsSom
							.contains(entry))
					|| (mTagsRemActive.contains(entry) && !mTagsRem
							.contains(entry));
		}
	}
}
