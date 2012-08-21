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
import com.brstf.wishlist.entries.MagazineDisplayPrice;
import com.brstf.wishlist.interfaces.OnDialogDismissListener;

public class ChooseMagazinePriceDialog extends SherlockDialogFragment {
	private RadioButton mIssue = null;
	private RadioButton mSubMonthly = null;
	private RadioButton mSubAnnual = null;
	private SharedPreferences mPrefs = null;
	private final HashMap<RadioButton, Integer> priceMap = new HashMap<RadioButton, Integer>();
	private OnDialogDismissListener mListener;

	ChooseMagazinePriceDialog(OnDialogDismissListener list) {
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
		final View view = inflater.inflate(R.layout.dialog_magazine_price,
				container);

		mIssue = (RadioButton) view.findViewById(R.id.magprice_issue);
		mSubMonthly = (RadioButton) view.findViewById(R.id.magprice_monthly);
		mSubAnnual = (RadioButton) view.findViewById(R.id.magprice_annual);

		priceMap.put(mIssue, MagazineDisplayPrice.CURRENT_ISSUE);
		priceMap.put(mSubMonthly, MagazineDisplayPrice.SUBSCRIPTION_MONTHLY);
		priceMap.put(mSubAnnual, MagazineDisplayPrice.SUBSCRIPTION_ANNUAL);

		mPrefs = getActivity().getSharedPreferences(
				getString(R.string.PREFS_NAME), 0);

		int displayPrice = mPrefs.getInt(
				getString(R.string.prefs_display_price_magazine), 0);
		switch (displayPrice) {
		case MagazineDisplayPrice.CURRENT_ISSUE:
			mIssue.setChecked(true);
			break;
		case MagazineDisplayPrice.SUBSCRIPTION_MONTHLY:
			mSubMonthly.setChecked(true);
			break;
		case MagazineDisplayPrice.SUBSCRIPTION_ANNUAL:
			mSubAnnual.setChecked(true);
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
				editor.putInt(getString(R.string.prefs_display_price_magazine),
						priceMap.get((RadioButton) buttonView));
				editor.commit();

				mListener.onDialogDismissal();
				dismiss();
			}
		};

		mIssue.setOnCheckedChangeListener(rButtonListener);
		mSubMonthly.setOnCheckedChangeListener(rButtonListener);
		mSubAnnual.setOnCheckedChangeListener(rButtonListener);
	}
}
