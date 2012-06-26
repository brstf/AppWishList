package com.brstf.wishlist;

import java.util.ArrayList;

import com.brstf.wishlist.R;
import com.brstf.wishlist.WLEntries.WLChangedListener;
import com.brstf.wishlist.widgets.SquareButton;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Fragment for the Home screen: The logo, an all button, and buttons for each
 * category
 */
public class WLHomeFragment extends Fragment implements WLChangedListener {
	private OnTileSelectedListener mCallback;

	public interface OnTileSelectedListener {
		/**
		 * Called by WLHomeFragment when a tag tile is selected
		 * 
		 * @param tag
		 *            Tag on the tile selected
		 */
		public void onTagSelected(String tag);
	}

	private ArrayList<String> mElements = null;

	/**
	 * Constructor for the home fragment, initializes all member variables
	 */
	public WLHomeFragment() {
		super();

		mElements = new ArrayList<String>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View grid = inflater.inflate(R.layout.home, container, false);

		// Add a listener to the "all" button
		Button btnall = (Button) grid.findViewById(R.id.btnall);
		btnall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onTagSelected(null);
			}
		});

		LinearLayout col1 = (LinearLayout) grid.findViewById(R.id.column1);
		LinearLayout col2 = (LinearLayout) grid.findViewById(R.id.column2);

		for (int i = 0; i < mElements.size(); ++i) {
			View tile = getTile(i, inflater);
			if (i % 2 == 0) {
				col1.addView(tile);
			} else {
				col2.addView(tile);
			}
		}

		return grid;
	}

	@Override
	public void onStart() {
		super.onStart();
		mElements.clear();

		// Add one element per tag
		for (String tag : WLEntries.getInstance().getTags()) {
			mElements.add(tag);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Add one element per tag
		for (String tag : WLEntries.getInstance().getTags()) {
			mElements.add(tag);
		}

		// Attach the callback listener to the activity
		try {
			mCallback = (OnTileSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTileSelectedListener");
		}
	}

	/**
	 * Function to get the tile entry at the given position
	 * 
	 * @param position
	 *            The index of the tile entry to retrieve
	 * @param inflater
	 *            The LayoutInflater to use (passed infrom onCreateView)
	 * @return The View tile object to insert into the grid list on the home
	 *         screen
	 */
	private View getTile(int position, LayoutInflater inflater) {
		// Get the tile
		SquareButton tile = (SquareButton) inflater.inflate(R.layout.gridentry,
				null);
		tile.setText(mElements.get(position));

		// Add a click listener to each button, when pressed call the callback
		// function
		tile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback
						.onTagSelected(((SquareButton) v).getText().toString());
			}
		});

		return tile;
	}

	@Override
	public void onDataSetChanged() {
		// Do nothing for now, this view is too hard to change at the moment
	}
}
