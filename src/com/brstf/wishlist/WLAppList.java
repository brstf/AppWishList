package com.brstf.wishlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.brstf.appwishlist.R;
import com.brstf.wishlist.entries.WLAlbumEntry;
import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLEntryType;
import com.brstf.wishlist.entries.WLPricedEntry;
import com.brstf.wishlist.widgets.SquareImageView;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class WLAppList extends ListFragment {

	public class WLAdapter extends ArrayAdapter<WLEntry> {
		// The position id of selected menu item (for context menu)
		private int mSelId;

		public WLAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);// , wlAppList);

			// Initialize member variables
			mSelId = -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;

			// If this is not the selected index, the view is as normal
			if (position != mSelId) {
				row = getNormalRow(position, parent);
			} else {
				// Otherwise the view is the context menu
				row = getActivity().getLayoutInflater().inflate(
						R.layout.selrow, parent, false);

				// Get the revert button
				((ImageButton) row.findViewById(R.id.btnrev))
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// When the revert button is clicked, simply
								// select no row
								setSelectedPosition(-1);
								notifyDataSetChanged();
							}
						});
				((ImageButton) row.findViewById(R.id.btndel))
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// When the delete button is clicked, remove
								// this item
								mDbHelper.deleteEntry(getItem(mSelId).getId());

								// Try to delete the icon
								File f = new File(getItem(mSelId).getIconPath());
								f.delete();
								f = null;

								// Remove from the list, remove selection and
								// update
								remove(getItem(mSelId));
								setSelectedPosition(-1);
								notifyDataSetChanged();
							}
						});
			}

			// Get the ImageView associated with the row
			SquareImageView icon = (SquareImageView) row
					.findViewById(R.id.icon);

			// First ensure that the item at this position has an associated
			// icon
			if (!getItem(position).getIconPath().equals("")) {
				// If so, try to read it in
				FileInputStream fis = null;
				try {
					fis = getActivity().openFileInput(
							getItem(position).getIconPath());
					// If successful, set the icon
					icon.setImageBitmap(BitmapFactory.decodeStream(fis));
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
			}

			// Finally, return the row
			return row;
		}

		/**
		 * Gets the View of a row that is not selected
		 * 
		 * @param position
		 *            The position of the row in the list
		 * @param parent
		 *            The parent ViewGroup of the listFragment
		 * @return The row view
		 */
		private View getNormalRow(int position, ViewGroup parent) {
			// Get the view associated with this row
			View row = null;
			WLEntry ent = getItem(position);

			// Default to the normal row layout
			int rowLayout = getRowLayout(ent);

			// Inflate the row View
			row = getActivity().getLayoutInflater().inflate(rowLayout, parent,
					false);

			// Set the text of the appname to be the name of the app
			((TextView) row.findViewById(R.id.title)).setText(ent.getTitle());

			return getRow(rowLayout, ent, row);
		}

		private View getRow(int rowLayout, WLEntry ent, View row) {
			if (rowLayout == R.layout.row_album) {
				((TextView) row.findViewById(R.id.creator))
						.setText(((WLAlbumEntry) ent).getArtist());
			}

			// Display the price information based on presence of a sale
			if( rowLayout == R.layout.row_sale){
				// Set the text of the original price textview
				TextView priceView = ((TextView) row.findViewById(R.id.price));
				priceView.setText(getPriceText(((WLPricedEntry) ent).getRegularPrice()));

				// Set the paintflags to allow for strikethru for the sale
				priceView.setPaintFlags(priceView.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);

				// Finally, set the text for the currentprice
				((TextView) row.findViewById(R.id.sale_price))
						.setText(getPriceText(((WLPricedEntry) ent).getCurrentPrice()));
			} else {
				((TextView) row.findViewById(R.id.price))
						.setText(getPriceText(((WLPricedEntry) ent).getCurrentPrice()));
			}
			return row;
		}

		private int getRowLayout(WLEntry ent) {
			if (ent.getType() == WLEntryType.MUSIC_ALBUM) {
				return R.layout.row_album;
			} 
			if (ent.getType() != WLEntryType.MUSIC_ARTIST && ((WLPricedEntry) ent).isOnSale()) {
				// If there's a sale, use the row_sale layout
				return R.layout.row_sale;
			}
			
			return R.layout.row;
		}

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

		public void setSelectedPosition(int sel) {
			// If within bounds, set it
			mSelId = sel;
		}
	}

	// The list adapter for this ListFragment
	private WLAdapter mListAdapter = null;
	private WLDbAdapter mDbHelper = null;

	/**
	 * The onCreate method for the activity, instantiates the ListAdapter, sets
	 * it, and for debugging purposes adds a few apps to the list
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate the adapter and set it as this ListFragment's ListAdapter
		mListAdapter = new WLAdapter(this.getActivity(), R.layout.row);
		setListAdapter(mListAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Open up the SQLite database
		mDbHelper = new WLDbAdapter(this.getActivity().getApplicationContext());
		mDbHelper.open();
		fillData();

		// Set the longclick listener
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				mListAdapter.setSelectedPosition(position);
				mListAdapter.notifyDataSetChanged();
				return true;
			}
		});

		// Refresh all potential info for an app listing
		// new WLPriceChecker(mListAdapter).priceCheck();
	}

	/**
	 * When an app entry in the list is clicked, open up the Play listing for it
	 */
	@Override
	public void onListItemClick(android.widget.ListView l, View v,
			int position, long id) {
		// Deselect any selected item
		mListAdapter.setSelectedPosition(-1);

		// Create the url to open
		Uri webpage = Uri.parse(mListAdapter.getItem(position).getURL());

		// Create the intent and start it
		// TODO: Should this only start the Play store?
		Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
		startActivity(webIntent);
	}

	@Override
	public void onStop() {
		super.onStop();

		mDbHelper.close();
	}

	/**
	 * Function to add all entries from the database to the list
	 */
	private void fillData() {
		// Get all entries from the database and create the item list
		Cursor c = mDbHelper.fetchAllEntries();
		mListAdapter.clear();

		c.moveToFirst();
		int typeCol = c.getColumnIndex(WLDbAdapter.KEY_TYPE);
		int idCol = c.getColumnIndex(WLDbAdapter.KEY_ROWID);
		while (!c.isAfterLast()) {
			Log.d("DBG", String.valueOf(c.getColumnCount()));
			WLEntry ent = WLEntryType.getTypeEntry(
					WLEntryType.getTypeFromString(c.getString(typeCol)),
					c.getInt(idCol));

			mListAdapter.add(ent);
			c.moveToNext();
		}
	}

	/**
	 * Returns the state of the device being connected to a network
	 * 
	 * @return True if connected to the internet, false if not
	 */
	public boolean isOnline() {
		// Get the connectivity manager and network info
		ConnectivityManager connMgr = (ConnectivityManager) this.getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Return (network info exists && we're connected)
		return (networkInfo != null && networkInfo.isConnected());
	}
}
