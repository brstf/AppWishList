package com.brstf.wishlist.util;

import com.brstf.wishlist.entries.WLEntry;
import com.brstf.wishlist.provider.WLDbAdapter;
import com.brstf.wishlist.provider.WLEntryContract;

import android.content.ContentResolver;
import android.net.Uri;

public class ProviderUtils {

	public static Uri insert(ContentResolver resolver, WLEntry ent) {
		return resolver.insert(WLEntryContract.Entries.CONTENT_URI,
				WLDbAdapter.createValues(ent));
	}

	public static int update(ContentResolver resolver, WLEntry ent) {
		return resolver.update(
				WLEntryContract.Entries.buildEntryUri(ent.getURL()),
				WLDbAdapter.createValues(ent), null, null);
	}

	public static int delete(ContentResolver resolver, WLEntry ent) {
		return resolver
				.delete(WLEntryContract.Entries.buildEntryUri(ent.getURL()),
						null, null);
	}

	private ProviderUtils() {
	}
}
