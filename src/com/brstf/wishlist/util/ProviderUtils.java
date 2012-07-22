package com.brstf.wishlist.util;

import com.brstf.wishlist.entries.Entry;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;

import android.content.ContentResolver;
import android.net.Uri;

public class ProviderUtils {

	public static Uri insert(ContentResolver resolver, Entry ent) {
		return resolver.insert(WLEntryContract.Entries.CONTENT_URI,
				WLDbAdapter.createValues(ent));
	}

	public static int update(ContentResolver resolver, Entry ent) {
		return resolver.update(
				WLEntryContract.Entries.buildEntryUri(ent.getURL()),
				WLDbAdapter.createValues(ent), null, null);
	}

	public static int delete(ContentResolver resolver, Entry ent) {
		return resolver
				.delete(WLEntryContract.Entries.buildEntryUri(ent.getURL()),
						null, null);
	}

	private ProviderUtils() {
	}
}
