package com.brstf.wishlist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.brstf.wishlist.entries.WLAlbumEntry;
import com.brstf.wishlist.entries.WLAppEntry;
import com.brstf.wishlist.entries.WLArtistEntry;
import com.brstf.wishlist.entries.WLBookEntry;
import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLMovieEntry;
import com.brstf.wishlist.widgets.SquareImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Class to display the the list of wished items
 * 
 * @author brstf
 */
public class WLListView extends ListFragment {
	public class WLListAdapter extends ArrayAdapter<WLEntry> {

		public WLListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			WLEntry ent = getItem(position);

			// Switch on the type of entry this is
			switch (ent.getType()) {
			case APP:
				return getAppRow((WLAppEntry) ent, parent);
			case MUSIC_ARTIST:
				return getArtistRow((WLArtistEntry) ent, parent);
			case MUSIC_ALBUM:
				return getAlbumRow((WLAlbumEntry) ent, parent);
			case BOOK:
				return getBookRow((WLBookEntry) ent, parent);
			case MOVIE:
				return getMovieRow((WLMovieEntry) ent, parent);
			}

			// Should never happen.
			return null;
		}

		/**
		 * Function to return a row layout for an App entry
		 * 
		 * @param ent
		 *            The App entry to get a row view for
		 * @param parent
		 *            The parent ViewGroup that this row should go in
		 * @return The corresponding View of the row
		 */
		private View getAppRow(WLAppEntry ent, ViewGroup parent) {
			// Get a View with the base layout
			final View row = getActivity().getLayoutInflater().inflate(
					R.layout.row_app, parent, false);

			// Fill in the details
			final SquareImageView icon = (SquareImageView) row
					.findViewById(R.id.icon);
			icon.setImageBitmap(getIconBm(ent));

			((TextView) row.findViewById(R.id.title)).setText(ent.getTitle());

			((TextView) row.findViewById(R.id.price)).setText(getPriceText(ent
					.getCurrentPrice()));

			return row;
		}

		/**
		 * Function to return a row layout for an Artist entry
		 * 
		 * @param ent
		 *            The Artist entry to get a row view for
		 * @param parent
		 *            The parent ViewGroup that this row should go in
		 * @return The corresponding View of the row
		 */
		private View getArtistRow(WLArtistEntry ent, ViewGroup parent) {
			// Get a View with the base layout
			final View row = getActivity().getLayoutInflater().inflate(
					R.layout.row_artist, parent, false);

			// Fill in the details
			final SquareImageView icon = (SquareImageView) row
					.findViewById(R.id.icon);
			icon.setImageBitmap(getIconBm(ent));

			((TextView) row.findViewById(R.id.title)).setText(ent.getTitle());

			return row;
		}

		/**
		 * Function to return a row layout for an Album entry
		 * 
		 * @param ent
		 *            The Album entry to get a row view for
		 * @param parent
		 *            The parent ViewGroup that this row should go in
		 * @return The corresponding View of the row
		 */
		private View getAlbumRow(WLAlbumEntry ent, ViewGroup parent) {
			// Get a View with the base layout
			final View row = getActivity().getLayoutInflater().inflate(
					R.layout.row_album, parent, false);

			// Fill in the details
			final SquareImageView icon = (SquareImageView) row
					.findViewById(R.id.icon);
			icon.setImageBitmap(getIconBm(ent));

			((TextView) row.findViewById(R.id.title)).setText(ent.getTitle());
			((TextView) row.findViewById(R.id.creator))
					.setText(ent.getArtist());

			((TextView) row.findViewById(R.id.price)).setText(getPriceText(ent
					.getCurrentPrice()));

			return row;
		}

		/**
		 * Function to return a row layout for a Book entry
		 * 
		 * @param ent
		 *            The Book entry to get a row view for
		 * @param parent
		 *            The parent ViewGroup that this row should go in
		 * @return The corresponding View of the row
		 */
		private View getBookRow(WLBookEntry ent, ViewGroup parent) {
			// Get a View with the base layout
			final View row = getActivity().getLayoutInflater().inflate(
					R.layout.row_album, parent, false);

			// Fill in the details
			final SquareImageView icon = (SquareImageView) row
					.findViewById(R.id.icon);
			icon.setImageBitmap(getIconBm(ent));

			((TextView) row.findViewById(R.id.title)).setText(ent.getTitle());
			((TextView) row.findViewById(R.id.creator))
					.setText(ent.getAuthor());

			((TextView) row.findViewById(R.id.price)).setText(getPriceText(ent
					.getCurrentPrice()));

			return row;
		}

		/**
		 * Function to return a row layout for a Movieentry
		 * 
		 * @param ent
		 *            The Movie entry to get a row view for
		 * @param parent
		 *            The parent ViewGroup that this row should go in
		 * @return The corresponding View of the row
		 */
		private View getMovieRow(WLMovieEntry ent, ViewGroup parent) {
			// Get a View with the base layout
			final View row = getActivity().getLayoutInflater().inflate(
					R.layout.row_album, parent, false);

			// Fill in the details
			final SquareImageView icon = (SquareImageView) row
					.findViewById(R.id.icon);
			icon.setImageBitmap(getIconBm(ent));

			((TextView) row.findViewById(R.id.title)).setText(ent.getTitle());
			((TextView) row.findViewById(R.id.creator)).setText(ent
					.getDirector());

			((TextView) row.findViewById(R.id.price)).setText(getPriceText(ent
					.getCurrentPrice()));

			return row;
		}

		/**
		 * Function to get the icon of a particular WLEntry as a Bitmap
		 * 
		 * @param ent
		 *            The entry whose icon to retrieve
		 * @return The icon of said entry as a Bitmap
		 */
		private Bitmap getIconBm(WLEntry ent) {
			// Try to read in the icon file
			FileInputStream fis = null;
			try {
				fis = getActivity().openFileInput(ent.getIconPath());
				// If successful, set the icon
				return BitmapFactory.decodeStream(fis);
			} catch (FileNotFoundException e) {
				// Catch any exceptions
				// TODO: Throw in a default setting here?
				e.printStackTrace();
			} finally {
				// Finally, close our input stream
				try {
					fis.close();
				} catch (IOException e) {
					// Catch any nasty IO Exceptions
					e.printStackTrace();
				}
			}

			// If we failed at reading in an icon, return a default icon
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate the adapter and use it
		mListAdapter = new WLListAdapter(this.getActivity(),
				R.layout.row_artist);
		setListAdapter(mListAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Fill in the data from the WLEntries list
		fillData();
	}

	/**
	 * When an entry in the list is clicked, open up the Play listing for it
	 */
	@Override
	public void onListItemClick(android.widget.ListView l, View v,
			int position, long id) {
		// Create the url to open
		Uri webpage = Uri.parse(mListAdapter.getItem(position).getURL());

		// Create the intent and start it
		// TODO: Should this only start the Play store?
		Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
		startActivity(webIntent);
	}

	/**
	 * Function to add all entries from the WLEntries list our list adapter
	 */
	private void fillData() {
		// Clear entries from the list
		mListAdapter.clear();
		
		for (WLEntry ent : WLEntries.getInstance().getEntries()) {
			mListAdapter.add(ent);
		}
	}
}
