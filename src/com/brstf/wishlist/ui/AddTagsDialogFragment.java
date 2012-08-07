package com.brstf.wishlist.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.brstf.wishlist.R;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class AddTagsDialogFragment extends SherlockDialogFragment {
	public static final String KEY_URLSID = "URLSID";

	private final String KEY_CURRENT = "\\,current,\\";
	private final String KEY_REMAIN = "\\,remain,\\";

	private ArrayList<String> urls = null;

	private ArrayList<String> mTagsAll = null;
	private ArrayList<String> mTagsCur = null;
	private ArrayList<String> mTagsSom = null;
	private ArrayList<String> mTagsRem = null;

	private ArrayList<String> mTagsToAdd = null;
	private ArrayList<String> mTagsSomActive = null;
	private ArrayList<String> mTagsRemActive = null;

	private ListView mList;
	private ArrayAdapter<String> mAdapter;

	public AddTagsDialogFragment() {
		// Empty Constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize all lists
		mTagsCur = new ArrayList<String>();
		mTagsSom = new ArrayList<String>();
		mTagsRem = new ArrayList<String>();
		mTagsToAdd = new ArrayList<String>();
		mTagsSomActive = new ArrayList<String>();
		mTagsRemActive = new ArrayList<String>();

		// If we did not construct all tags yet, do so
		if (mTagsAll == null) {
			fillMasterTagList();
		}

		// Initialize the appropriate adapter
		mAdapter = initAddAdapter();

		fillTagsFromArguments(getArguments());

		// Set the style appropriately
		setStyle(SherlockDialogFragment.STYLE_NO_TITLE,
				android.R.style.Theme_Holo_Dialog);
	}

	/**
	 * Fills the master tag list - the list of all tags currently in the
	 * database.
	 */
	private void fillMasterTagList() {
		mTagsAll = new ArrayList<String>();

		// Query the content provider for all tags
		Cursor c = getActivity().getContentResolver().query(
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
		urls = getArguments().getStringArrayList(KEY_URLSID);
		HashMap<String, Integer> tagmap = new HashMap<String, Integer>();

		// Loop through each selected url and
		for (String url : urls) {
			Cursor c = getActivity().getContentResolver().query(
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
		mTagsToAdd.clear();
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

		mTagsSomActive.addAll(mTagsSom);
		mTagsRemActive.addAll(mTagsRem);

		sortActiveLists();

		notifyAdapterChange();
	}

	/**
	 * Sorts all active lists by the TagComparator.
	 */
	private void sortActiveLists() {
		TagComparator tc = new TagComparator();
		Collections.sort(mTagsCur, tc);
		Collections.sort(mTagsToAdd, tc);
		Collections.sort(mTagsSomActive, tc);
		Collections.sort(mTagsRemActive, tc);
	}

	/**
	 * Comparator class to sort in alphabetical order.
	 */
	private class TagComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			return lhs.compareTo(rhs);
		}
	}

	/**
	 * Called when the adapter's dataset is changed, this function updates the
	 * adapter contents.
	 */
	private void notifyAdapterChange() {
		mAdapter.clear();

		mAdapter.add(KEY_CURRENT);
		for (String s : mTagsCur) {
			mAdapter.add(s);
		}

		for (String s : mTagsToAdd) {
			mAdapter.add(s);
		}

		if (mTagsSomActive.size() > 0 || mTagsRemActive.size() > 0) {
			mAdapter.add(KEY_REMAIN);
		}

		for (String s : mTagsSomActive) {
			mAdapter.add(s);
		}

		for (String s : mTagsRemActive) {
			mAdapter.add(s);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_add_tag, container);
		mList = (ListView) view.findViewById(R.id.dialog_list);

		mList.setAdapter(mAdapter);

		mList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				String selected = mAdapter.getItem(position);
				if (mTagsToAdd.contains(selected)) {
					if (mTagsSom.contains(selected)) {
						mTagsSomActive.add(selected);
					} else if (mTagsRem.contains(selected)) {
						mTagsRemActive.add(selected);
					}
					mTagsToAdd.remove(selected);
				} else if (mTagsSomActive.contains(selected)) {
					mTagsToAdd.add(selected);
					mTagsSomActive.remove(selected);
				} else if (mTagsRemActive.contains(selected)) {
					mTagsToAdd.add(selected);
					mTagsRemActive.remove(selected);
				}

				sortActiveLists();

				// Notify the adapter that the data set has changed
				notifyAdapterChange();
			}
		});

		// Add functionality to the UI elements:

		// Get the revert button and add its functionality
		ImageButton revertButton = (ImageButton) view
				.findViewById(R.id.btn_cancel_transaction);
		revertButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AddTagsDialogFragment.this.dismiss();
			}
		});

		// Get the save button and add its functionality
		ImageButton saveButton = (ImageButton) view
				.findViewById(R.id.btn_save_changes);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WLDbAdapter dbhelper = new WLDbAdapter(
						AddTagsDialogFragment.this.getActivity()
								.getBaseContext());
				dbhelper.open();

				// Loop through each entry and add each tag
				for (String url : urls) {
					for (String tag : mTagsToAdd) {
						dbhelper.addTag(url, tag);
					}
				}

				dbhelper.close();

				// Dismiss the dialog
				AddTagsDialogFragment.this.dismiss();
			}
		});

		// Get the submit button and add its functionality
		ImageButton submitButton = (ImageButton) view
				.findViewById(R.id.btn_submit);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Obtain the text from the dialog box
				EditText tagTextView = (EditText) view
						.findViewById(R.id.dialog_edit_text);
				String tagText = tagTextView.getText().toString();
				Log.d("AddDialog", tagText);

				// Split for multiple tags
				String[] tags = tagText.split(",");
				
				// Loop through each tag:
				for( String tag : tags ) {
					// trim the tag
					tag = tag.trim().toLowerCase();
					
					// Check to make sure tag names are valid (non-empty)
					if( tag.length() == 0 ) {
						continue;
					}

					// Add this tag to the new tags list
					mTagsToAdd.add(tag);
				}
				// Clear the text
				tagTextView.getText().clear();
				notifyAdapterChange();
			}
		});

		return view;
	}

	/**
	 * Initialize an {@link AddListAdapter} for this
	 * {@link AddTagsDialogFragment}.
	 * 
	 * @return {@link AddListAdapter} for the {@link ListView}
	 */
	private AddListAdapter initAddAdapter() {
		return new AddListAdapter(this.getActivity().getApplicationContext());
	}

	/**
	 * ArrayAdapter for the Add style of the {@link AddTagsDialogFragment}.
	 */
	public class AddListAdapter extends ArrayAdapter<String> {
		LayoutInflater mInflater;

		public AddListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mInflater = LayoutInflater.from(context);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;
			String displayText = null;

			// If this is a separator key string
			if (getItem(position).equals(KEY_CURRENT)
					|| getItem(position).equals(KEY_REMAIN)) {
				// Inflate the separator layout style
				row = mInflater.inflate(R.layout.row_tag_title, parent, false);

				// Disable clicking of any sort
				row.setClickable(false);
				row.setLongClickable(false);
				row.setFocusable(false);

				// Set appropriate display text
				if (getItem(position).equals(KEY_CURRENT)) {
					displayText = "SELECTED TAGS:";
				} else if (getItem(position).equals(KEY_REMAIN)) {
					displayText = "OTHER TAGS:";
				}
			} else {
				// Otherwise, inflate the normal list layout style
				row = mInflater.inflate(android.R.layout.simple_list_item_1,
						parent, false);
				String selected = getItem(position);

				// If this is tag is currently applied to all entries, don't
				// allow clicking (we cannot remove tags in the add tags dialog)
				if (mTagsCur.contains(selected)) {
					row.setClickable(false);
					row.setLongClickable(false);
					row.setFocusable(false);
				}

				// If a new tag has been added, set its text color to green
				if (mTagsToAdd.contains(selected)) {
					((TextView) row).setTextColor(0xFF00FF00);
				}
				displayText = selected;
			}

			// Finally, set the text of the text view and return
			((TextView) row).setText(displayText);
			return row;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEnabled(int position) {
			String selected = getItem(position);
			if (selected.equals(KEY_CURRENT) || selected.equals(KEY_REMAIN)) {
				return false;
			} else if (mTagsCur.contains(selected)) {
				return false;
			} else {
				return true;
			}
		}
	}
}
