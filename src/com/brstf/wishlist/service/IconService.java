package com.brstf.wishlist.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * {@link IntentService} for downloading an entry's icon to the file system for
 * faster access (and less data usage).
 * 
 * @author brstf
 * 
 */
public class IconService extends IntentService {
	private static final String TAG = "IconService";
	public static final String EXTRA_FILENAME = "EXTRAFILENAME";
	public static final String EXTRA_URL= "EXTRAURL";

	public IconService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String fileName = intent.getStringExtra(EXTRA_FILENAME);
		String url = intent.getStringExtra(EXTRA_URL);

		// Try to download the icon of the passed in entry
		// TODO: Should this only download if necessary, or should the logic of
		// checking whether the icon exists or not remain separate?
		try {
			FileOutputStream fos = getBaseContext().openFileOutput(fileName,
					Context.MODE_PRIVATE);
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
					url).getContent());
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
			Log.d(TAG, "Successfully downloaded icon for: " + fileName);
		} catch (IOException e) {
			Log.d(TAG, "Failed to download icon for: " + fileName);
		}
	}

}
