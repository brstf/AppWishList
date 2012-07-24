package com.brstf.wishlist.ui;

import android.os.Bundle;

import com.brstf.wishlist.R;

public class AddTags extends BaseActivity {
	private AddTagsFragment mFrag = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (findViewById(R.id.fragment_container) != null) {
			mFrag = new AddTagsFragment();
			mFrag.setArguments(BaseActivity
					.intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, mFrag).commit();
		}
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mFrag.onStop();
	}
}
