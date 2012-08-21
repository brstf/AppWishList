package com.brstf.wishlist.ui;

import java.util.HashMap;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.brstf.wishlist.R;
import com.brstf.wishlist.interfaces.OnDialogDismissListener;
import com.brstf.wishlist.util.NetworkUtils;

public class ChooseIntervalDialog extends SherlockDialogFragment {
	private RadioButton m15min = null;
	private RadioButton m30min = null;
	private RadioButton m1hour = null;
	private RadioButton mHalfday = null;
	private RadioButton mDay = null;
	private SharedPreferences mPrefs = null;
	private final HashMap<RadioButton, Long> intervalMap = new HashMap<RadioButton, Long>();
	private OnDialogDismissListener mListener;

	ChooseIntervalDialog(OnDialogDismissListener list) {
		mListener = list;
	}

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

		intervalMap.put(m15min, AlarmManager.INTERVAL_FIFTEEN_MINUTES);
		intervalMap.put(m30min, AlarmManager.INTERVAL_HALF_HOUR);
		intervalMap.put(m1hour, AlarmManager.INTERVAL_HOUR);
		intervalMap.put(mHalfday, AlarmManager.INTERVAL_HALF_DAY);
		intervalMap.put(mDay, AlarmManager.INTERVAL_DAY);

		mPrefs = getActivity().getSharedPreferences(
				getString(R.string.PREFS_NAME), 0);

		long interval = mPrefs.getLong(getString(R.string.prefs_sync_interval),
				0);
		if (interval == intervalMap.get(m15min)) {
			m15min.setChecked(true);
		} else if (interval == intervalMap.get(m30min)) {
			m30min.setChecked(true);
		} else if (interval == intervalMap.get(m1hour)) {
			m1hour.setChecked(true);
		} else if (interval == intervalMap.get(mHalfday)) {
			mHalfday.setChecked(true);
		} else if (interval == intervalMap.get(mDay)) {
			mDay.setChecked(true);
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

				long interval = intervalMap.get((RadioButton) buttonView);
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putLong(getString(R.string.prefs_sync_interval),
						interval);
				editor.commit();

				String lastTimeString = getString(R.string.prefs_last_check_time);
				long lastTime = mPrefs.getLong(lastTimeString, 0);
				NetworkUtils.schedulePriceCheck(getActivity().getBaseContext(),
						lastTime + interval, interval, mPrefs, lastTimeString);

				mListener.onDialogDismissal();
				dismiss();
			}
		};

		m15min.setOnCheckedChangeListener(rButtonListener);
		m30min.setOnCheckedChangeListener(rButtonListener);
		m1hour.setOnCheckedChangeListener(rButtonListener);
		mHalfday.setOnCheckedChangeListener(rButtonListener);
		mDay.setOnCheckedChangeListener(rButtonListener);
	}
}
