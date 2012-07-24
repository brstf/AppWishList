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
import com.brstf.wishlist.provider.WLEntryContract;

public class AddTagsFragment extends SherlockListFragment {
	public static final String KEY_URLSID = "URLSID";

	public class TagListAdapter extends ArrayAdapter<String> {
		LayoutInflater mInflater;

		public TagListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = mInflater.inflate(android.R.layout.simple_list_item_1,
					parent, false);
			((TextView) row).setText(getItem(position));
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

		fillTags(getArguments());
	}

	private void fillTags(Bundle arguments) {
		ArrayList<String> urls = new ArrayList<String>();
		urls = getArguments().getStringArrayList(KEY_URLSID);
		HashMap<String, Integer> tagmap = new HashMap<String, Integer>();

		for (String url : urls) {
			Cursor c = getSherlockActivity().getContentResolver().query(
					WLEntryContract.Entries.CONTENT_URI.buildUpon()
							.appendPath("entry").appendPath(url).build(),
					WLEntryContract.EntriesQuery.columns, null, null, null);
			c.moveToFirst();
			String stringtags = c.getString(
					c.getColumnIndex(WLEntryContract.EntryColumns.KEY_TAGS));
			String[] entTags = stringtags
					.split(",");
			for (String tag : entTags) {
				if (!tagmap.containsKey(tag)) {
					tagmap.put(tag, 0);
				}
				tagmap.put(tag, tagmap.get(tag) + 1);
			}
		}

		ArrayList<String> currenttags = new ArrayList<String>();
		ArrayList<String> sometags = new ArrayList<String>();
		ArrayList<String> othertags = new ArrayList<String>();
		currenttags.add("Current Tags:");
		sometags.add("Some Tags:");
		othertags.add("Other Tags:");

		// Now that we have all the tags and their counts, we construct the tag
		// list
		for (String key : tagmap.keySet()) {
			if (tagmap.get(key) == urls.size()) {
				currenttags.add(key);
			} else {
				sometags.add(key);
			}
		}

		Cursor c = getSherlockActivity().getContentResolver().query(
				WLEntryContract.Tags.CONTENT_URI, null, null, null, null);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			String tag = c.getString(c
					.getColumnIndex(WLEntryContract.TagColumns.KEY_TAG));
			if (!(currenttags.contains(tag) || sometags.contains(tag))) {
				othertags.add(tag);
			}
			c.moveToNext();
		}
		
		mAdapter.addAll(currenttags);
		mAdapter.addAll(sometags);
		mAdapter.addAll(othertags);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
}
