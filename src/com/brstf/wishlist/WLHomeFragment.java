package com.brstf.wishlist;

import java.util.ArrayList;

import com.brstf.appwishlist.R;
import com.brstf.wishlist.widgets.SquareButton;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Fragment for the Home screen: The logo, an all button, and buttons for each
 * category
 */
public class WLHomeFragment extends Fragment {
	private ArrayList<String> mElements = null;

	/**
	 * Constructor for the home fragment, initializes all member variables
	 */
	public WLHomeFragment() {
		super();

		mElements = new ArrayList<String>();
		mElements.add("Apps");
		mElements.add("Movies");
		mElements.add("Books");
		mElements.add("Music");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View grid = inflater.inflate(R.layout.home, container, false);

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

		return tile;
	}
}
