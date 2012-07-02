package com.brstf.wishlist.service;

import android.app.IntentService;
import android.content.Intent;

public class PriceCheckService extends IntentService {
	private static final String TAG = "PriceCheckService";
	
	public PriceCheckService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
	}

}
