package com.brstf.wishlist.ui;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.brstf.wishlist.R;

public class ChooseIntervalDialog extends SherlockDialogFragment {
	private RadioButton m15min = null;
	private RadioButton m30min = null;
	private RadioButton m1hour = null;
	private RadioButton mHalfday = null;
	private RadioButton mDay = null;
	private SharedPreferences mPrefs = null;

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
		final View view = inflater.inflate(R.layout.sync_interval, container);

		m15min = (RadioButton) view.findViewById(R.id.interval_15min);
		m30min = (RadioButton) view.findViewById(R.id.interval_halfhour);
		m1hour = (RadioButton) view.findViewById(R.id.interval_1hour);
		mHalfday = (RadioButton) view.findViewById(R.id.interval_halfday);
		mDay = (RadioButton) view.findViewById(R.id.interval_day);

		mPrefs = getActivity().getSharedPreferences(
				getString(R.string.PREFS_NAME), 0);
		;
		long interval = mPrefs.getLong(getString(R.string.prefs_sync_interval),
				0);
		if (interval == AlarmManager.INTERVAL_FIFTEEN_MINUTES) {
			m15min.setChecked(true);
		} else if (interval == AlarmManager.INTERVAL_HALF_HOUR) {
			m30min.setChecked(true);
		} else if (interval == AlarmManager.INTERVAL_HOUR) {
			m1hour.setChecked(true);
		} else if (interval == AlarmManager.INTERVAL_HALF_DAY) {
			mHalfday.setChecked(true);
		} else if (interval == AlarmManager.INTERVAL_DAY) {
			mDay.setChecked(true);
		}

		return view;
	}
}
