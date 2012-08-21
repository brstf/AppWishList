package com.brstf.wishlist.ui;

import java.util.HashMap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.brstf.wishlist.R;
import com.brstf.wishlist.entries.MovieDisplayPrice;
import com.brstf.wishlist.interfaces.OnDialogDismissListener;

public class ChooseMoviePriceDialog extends SherlockDialogFragment {
	private RadioButton mRSD = null;
	private RadioButton mRHD = null;
	private RadioButton mBSD = null;
	private RadioButton mBHD = null;
	private SharedPreferences mPrefs = null;
	private final HashMap<RadioButton, Integer> priceMap = new HashMap<RadioButton, Integer>();
	private OnDialogDismissListener mListener;

	ChooseMoviePriceDialog(OnDialogDismissListener list) {
		mListener = list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(SherlockDialogFragment.STYLE_NO_TITLE,
				android.R.style.Theme_Holo_Dialog);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_movie_price, container);

		mRSD = (RadioButton) view.findViewById(R.id.movprice_rsd);
		mRHD = (RadioButton) view.findViewById(R.id.movprice_rhd);
		mBSD = (RadioButton) view.findViewById(R.id.movprice_bsd);
		mBHD = (RadioButton) view.findViewById(R.id.movprice_bhd);

		priceMap.put(mRSD, MovieDisplayPrice.RENTAL_STANDARD_DEFINITION);
		priceMap.put(mRHD, MovieDisplayPrice.RENTAL_HIGH_DEFINITION);
		priceMap.put(mBSD, MovieDisplayPrice.BUY_STANDARD_DEFINITION);
		priceMap.put(mBHD, MovieDisplayPrice.BUY_HIGH_DEFINITION);

		mPrefs = getActivity().getSharedPreferences(
				getString(R.string.PREFS_NAME), 0);

		int displayPrice = mPrefs.getInt(
				getString(R.string.prefs_display_price_movie), 0);
		switch (displayPrice) {
		case MovieDisplayPrice.RENTAL_STANDARD_DEFINITION:
			mRSD.setChecked(true);
			break;
		case MovieDisplayPrice.RENTAL_HIGH_DEFINITION:
			mRHD.setChecked(true);
			break;
		case MovieDisplayPrice.BUY_STANDARD_DEFINITION:
			mBSD.setChecked(true);
			break;
		case MovieDisplayPrice.BUY_HIGH_DEFINITION:
			mBHD.setChecked(true);
			break;
		}

		return view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Add check listeners to all radio buttons
		OnCheckedChangeListener rButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!isChecked)
					return;

				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putInt(getString(R.string.prefs_display_price_movie),
						priceMap.get((RadioButton) buttonView));
				editor.commit();

				mListener.onDialogDismissal();
				dismiss();
			}
		};

		mRSD.setOnCheckedChangeListener(rButtonListener);
		mRHD.setOnCheckedChangeListener(rButtonListener);
		mBSD.setOnCheckedChangeListener(rButtonListener);
		mBHD.setOnCheckedChangeListener(rButtonListener);
	}
}
