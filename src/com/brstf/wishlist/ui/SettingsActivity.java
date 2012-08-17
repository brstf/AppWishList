package com.brstf.wishlist.ui;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.brstf.wishlist.R;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsActivity extends BaseActivity {
	private ToggleButton mWifiSync;
	private LinearLayout mSyncInterval;
	private ToggleButton mConfirmDeletion;
	private ToggleButton mAddUponAddition;
	private LinearLayout mDisplayPriceMovie;
	private LinearLayout mDisplayPriceMagazine;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		View cView = getWindow().getDecorView().findViewById(
				android.R.id.content);

		// Get all the buttons from the settings view
		mWifiSync = (ToggleButton) cView.findViewById(R.id.btn_wifi_sync);
		mSyncInterval = (LinearLayout) cView
				.findViewById(R.id.btn_sync_inteval);
		mConfirmDeletion = (ToggleButton) cView
				.findViewById(R.id.btn_confirm_deletion);
		mAddUponAddition = (ToggleButton) cView
				.findViewById(R.id.btn_add_upon_addition);
		mDisplayPriceMovie = (LinearLayout) cView
				.findViewById(R.id.btn_display_price_movie);
		mDisplayPriceMagazine = (LinearLayout) cView
				.findViewById(R.id.btn_display_price_magazine);

		// Set the button states based on the Preferences
		mWifiSync.setChecked(mPrefs.getBoolean(
				getString(R.string.prefs_sync_wifi), false));
		setIntervalSubString();
		mConfirmDeletion.setChecked(mPrefs.getBoolean(
				getString(R.string.prefs_confirm_deletion), false));
		mAddUponAddition.setChecked(mPrefs.getBoolean(
				getString(R.string.prefs_add_upon_addition), false));
		setMoviePriceSubString();
		setMagazinePriceSubString();

		// Add listeners
		mWifiSync.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// When this button is toggled, edit shared preferences
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean(getString(R.string.prefs_sync_wifi),
						isChecked);
				editor.commit();
			}
		});

		mSyncInterval.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SherlockDialogFragment intervalFragment = new ChooseIntervalDialog();
				intervalFragment.show(getSupportFragmentManager(),
						"dialog_interval");
			}
		});

		mConfirmDeletion
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// When this button is toggled, edit shared preferences
						SharedPreferences.Editor editor = mPrefs.edit();
						editor.putBoolean(
								getString(R.string.prefs_confirm_deletion),
								isChecked);
						editor.commit();
					}
				});

		mAddUponAddition
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// When this button is toggled, edit shared preferences
						SharedPreferences.Editor editor = mPrefs.edit();
						editor.putBoolean(
								getString(R.string.prefs_add_upon_addition),
								isChecked);
						editor.commit();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * Sets the display string of the current sync interval {@link TextView}.
	 */
	private void setIntervalSubString() {
		long interval = mPrefs.getLong(getString(R.string.prefs_sync_interval),
				0);
		TextView textInterval = (TextView) mSyncInterval
				.findViewById(R.id.text_current_interval);
		if (interval == AlarmManager.INTERVAL_FIFTEEN_MINUTES) {
			textInterval.setText(getString(R.string.settings_interval_15min));
		} else if (interval == AlarmManager.INTERVAL_HALF_HOUR) {
			textInterval
					.setText(getString(R.string.settings_interval_halfhour));
		} else if (interval == AlarmManager.INTERVAL_HOUR) {
			textInterval.setText(getString(R.string.settings_interval_1hour));
		} else if (interval == AlarmManager.INTERVAL_HALF_DAY) {
			textInterval.setText(getString(R.string.settings_interval_halfday));
		} else if (interval == AlarmManager.INTERVAL_DAY) {
			textInterval.setText(getString(R.string.settings_interval_day));
		}
	}

	/**
	 * Sets the display string of the magazine price display.
	 */
	private void setMagazinePriceSubString() {
		int whichPrice = mPrefs.getInt(
				getString(R.string.prefs_display_price_magazine), -1);
		TextView textMagazine = (TextView) mDisplayPriceMagazine
				.findViewById(R.id.text_display_price_magazine);
		switch (whichPrice) {
		case 0:
			textMagazine.setText(getString(R.string.settings_magprice_issue));
			break;
		case 1:
			textMagazine.setText(getString(R.string.settings_magprice_monthly));
			break;
		case 2:
			textMagazine.setText(getString(R.string.settings_magprice_annual));
			break;
		}
	}

	/**
	 * Sets the display string of the movie price display.
	 */
	private void setMoviePriceSubString() {
		int whichPrice = mPrefs.getInt(
				getString(R.string.prefs_display_price_movie), -1);
		TextView textMovie = (TextView) mDisplayPriceMovie
				.findViewById(R.id.text_display_price_movie);
		switch (whichPrice) {
		case 0:
			textMovie.setText(getString(R.string.settings_movprice_rentsd));
			break;
		case 1:
			textMovie.setText(getString(R.string.settings_movprice_renthd));
			break;
		case 2:
			textMovie.setText(getString(R.string.settings_movprice_buysd));
			break;
		case 3:
			textMovie.setText(getString(R.string.settings_movprice_buyhd));
			break;
		}
	}
}
