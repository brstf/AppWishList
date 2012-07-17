package com.brstf.wishlist.ui;

import com.brstf.wishlist.R;
import com.brstf.wishlist.provider.WLProvider;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;

public class SearchActivity extends BaseActivity {
	private WLListFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = (WLListFragment) fm
				.findFragmentById(R.id.fragment_container);

		if (mFragment == null) {
			mFragment = new WLListFragment();
			fm.beginTransaction().add(R.id.fragment_container, mFragment)
					.commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		onNewIntent(getIntent());
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		String query = intent.getStringExtra(SearchManager.QUERY);

		setTitle(Html.fromHtml(query));
		Uri searchUri = buildSearchUri(query);
		
		Bundle args = new Bundle();
		args.putParcelable("_uri", searchUri);
		mFragment.reloadFromArguments(args);
	}

	private Uri buildSearchUri(String query) {
		return WLProvider.BASE_CONTENT_URI.buildUpon().appendPath("entries")
				.appendPath("search").appendPath(query).build();
	}
}
