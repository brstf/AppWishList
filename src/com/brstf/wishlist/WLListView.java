package com.brstf.wishlist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.brstf.wishlist.entries.WLAlbumEntry;
import com.brstf.wishlist.entries.WLAppEntry;
import com.brstf.wishlist.entries.WLArtistEntry;
import com.brstf.wishlist.entries.WLBookEntry;
import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.entries.WLMovieEntry;
import com.brstf.wishlist.widgets.SquareImageView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ListFragment;
import android.net.Uri;
import android.os.AsyncTask;
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
	// Hash map mapping icon name to already loaded icons
	private static HashMap<String, Bitmap> icons = null;

	private static class ViewHolder {
		public SquareImageView icon;
		public TextView title;
		public TextView creator;
		public TextView price;
		public int position;
	}

	private static class IconTask extends AsyncTask<Activity, Object, Bitmap> {
		private WLEntry mEnt;
		private int mPosition;
		private ViewHolder mHolder;

		public IconTask(WLEntry ent, int position, ViewHolder holder) {
			mEnt = ent;
			mPosition = position;
			mHolder = holder;
		}

		@Override
		protected Bitmap doInBackground(Activity... params) {
			// Try to read in the icon file
			FileInputStream fis = null;
			try {
				fis = params[0].openFileInput(mEnt.getIconPath());
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

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			icons.put(mEnt.getIconPath(), bitmap);
			if (bitmap == null)
				return;
			if (mHolder.position == mPosition) {
				mHolder.icon.setImageBitmap(bitmap);
			}
		}
	}

	public class WLListAdapter extends ArrayAdapter<WLEntry> {

		public WLListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View row = convertView;
			if (row == null) {
				row = getActivity().getLayoutInflater().inflate(R.layout.row,
						parent, false);

				// If the convert view was null, we need to construct a new view
				// holder for it
				holder = new ViewHolder();
				holder.icon = (SquareImageView) row.findViewById(R.id.icon);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.creator = (TextView) row.findViewById(R.id.creator);
				holder.price = (TextView) row.findViewById(R.id.price);

				row.setTag(holder);
			} else {
				// If it wasn't null, get the ViewHolder from the convertView
				holder = (ViewHolder) row.getTag();
			}

			holder.position = position;

			WLEntry ent = getItem(position);

			if (icons.containsKey(ent.getIconPath())) {
				holder.icon.setImageBitmap(icons.get(ent.getIconPath()));
			} else {
				new IconTask(ent, holder.position, holder).executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR, getActivity());
				switch (ent.getType()) {
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

			// Switch on the type of entry this is
			switch (ent.getType()) {
			case APP:
				return getAppRow((WLAppEntry) ent, row, holder);
			case MUSIC_ARTIST:
				return getArtistRow((WLArtistEntry) ent, row, holder);
			case MUSIC_ALBUM:
				return getAlbumRow((WLAlbumEntry) ent, row, holder);
			case BOOK:
				return getBookRow((WLBookEntry) ent, row, holder);
			case MOVIE:
				return getMovieRow((WLMovieEntry) ent, row, holder);
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
		private View getAppRow(WLAppEntry ent, View row, ViewHolder holder) {
			// Fill in the details
			holder.title.setText(ent.getTitle());
			holder.creator.setText(ent.getDeveloper());

			holder.price.setText(getPriceText(ent.getCurrentPrice()));

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
		private View getArtistRow(WLArtistEntry ent, View row, ViewHolder holder) {
			// Fill in the details
			holder.title.setText(ent.getTitle());
			holder.creator.setText(ent.getGenres());

			holder.price.setText("");

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
		private View getAlbumRow(WLAlbumEntry ent, View row, ViewHolder holder) {
			// Fill in the details
			holder.title.setText(ent.getTitle());
			holder.creator.setText(ent.getArtist());

			holder.price.setText(getPriceText(ent.getCurrentPrice()));

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
		private View getBookRow(WLBookEntry ent, View row, ViewHolder holder) {
			// Fill in the details
			holder.title.setText(ent.getTitle());
			holder.creator.setText(ent.getAuthor());

			holder.price.setText(getPriceText(ent.getCurrentPrice()));

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
		private View getMovieRow(WLMovieEntry ent, View row, ViewHolder holder) {
			// Fill in the details
			holder.title.setText(ent.getTitle());
			holder.creator.setText(ent.getDirector());

			holder.price.setText(getPriceText(ent.getCurrentPrice()));

			return row;
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
	public static String ARG_FILTERTAG = "filter_tag";
	private String filtertag = null;
	private Drawable musicPh = null;
	private Drawable appsPh = null;
	private Drawable moviesPh = null;
	private Drawable booksPh = null;

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
			filtertag = args.getString(ARG_FILTERTAG);
		}

		// Instantiate the adapter and use it
		mListAdapter = new WLListAdapter(this.getActivity(), R.layout.row);
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

		for (WLEntry ent : WLEntries.getInstance().getEntries(filtertag)) {
			mListAdapter.add(ent);
		}
	}
}
