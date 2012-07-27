package com.brstf.wishlist.ui;

import java.util.ArrayList;
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
	public static final String KEY_URLSID = "URLSID";
	private ArrayList<String> mTagsCur = null;
	private ArrayList<String> mTagsSom = null;
	private ArrayList<String> mTagsRem = null;
	private ArrayList<String> mTagsAll = null;

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
				displayText = getItem(position);
			}
			((TextView) row).setText(displayText);
			return row;
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

		if (mTagsAll == null) {
			fillMasterTagList();
		}

		fillTagsFromArguments(getArguments());
	}

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

		notifyAdapterChange();
	}

	private void notifyAdapterChange() {
		mAdapter.clear();

		if (mTagsCur.size() > 0) {
			mAdapter.add(KEY_CURRENT);
			mAdapter.addAll(mTagsCur);
		}

		if (mTagsSom.size() > 0) {
			mAdapter.add(KEY_INSOME);
			mAdapter.addAll(mTagsSom);
		}

		if (mTagsRem.size() > 0) {
			mAdapter.add(KEY_REMAIN);
			mAdapter.addAll(mTagsRem);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}
}
