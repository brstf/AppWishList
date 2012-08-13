package com.brstf.wishlist.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BootService extends Service {
	private static final String TAG = "BootService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Reschedule a new price check

		Log.d(TAG, "Scheduled service");

		Intent serviceIntent = new Intent(this, PriceCheckService.class);
		PendingIntent pintent = PendingIntent.getService(
				getApplicationContext(), 0, serviceIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		((AlarmManager) this.getSystemService(Context.ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.RTC,
						System.currentTimeMillis() + 1000,
						AlarmManager.INTERVAL_HALF_DAY, pintent);
	}

}
