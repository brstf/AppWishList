package com.brstf.appwishlist;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class WLGridList  {
	private Context mCtx = null;
	private ArrayList<String> mElements = null;
	
	/**
	 * Constructor for the Grid List
	 * 
	 * @param context
	 *            Context of the application using the list
	 */
	public WLGridList(Context ctx) {
		mCtx = ctx;
		mElements = new ArrayList<String>();
		mElements.add("Apps");
		mElements.add("Movies");
		mElements.add("Books");
		mElements.add("Music");
	}
	
	public int getCount() {
		return mElements.size();
	}

	public String getItem(int position) {
		return mElements.get(position);
	}
	
	public View getView(int position, View view) {
		LayoutInflater inflater = (LayoutInflater) mCtx.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//Now add two entires to the row
		SquareButton tile = new SquareButton(mCtx);
		tile = (SquareButton) inflater.inflate(R.layout.gridentry, null);
		tile.setText(mElements.get(position));
		/*if(tile.getText().equals("Apps")) {
			tile.setBackgroundResource(R.drawable.apps);
		} else if(tile.getText().equals("Music")) {
			tile.setBackgroundResource(R.drawable.music);
		} else if(tile.getText().equals("Books")) {
			tile.setBackgroundResource(R.drawable.books);
		} else if(tile.getText().equals("Movies")) {
			tile.setBackgroundResource(R.drawable.movies);
		} else {
			tile.setBackgroundResource(R.drawable.ic_launcher);
		}*/
		
		return tile;
	}

}
