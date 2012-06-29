package com.brstf.wishlist;

import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Manages the creation and modification of the ActionBar
 */
public class ActionBarManager {
	private ActionBar ab = null;
	private static final ActionBarManager abm = new ActionBarManager();

	/**
	 * Retrieves the single instance of the singleton class ActionBarManager
	 * 
	 * @return Instance of this ActionBarManager
	 */
	public static ActionBarManager getInstance() {
		return abm;
	}

	private ActionBarManager() {
	}

	/**
	 * Set the context of the ActionBarManger so it can create an ActionBar
	 * 
	 * @param mCtx
	 *            Context of the activity to create the actionBar with
	 */
	public void setActivity(SherlockFragmentActivity act) {
		ab = act.getSupportActionBar();
	}

	/**
	 * Initializes action bar before moving to a WLListView fragment
	 */
	public void initListActionBar(SpinnerAdapter adapter,
			OnNavigationListener listener) {
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(adapter, listener);
		ab.show();
	}

	/**
	 * Private function to initialize the action bar
	 */
	public void initHomeActionBar() {
		// ActionBar!
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setHomeButtonEnabled(false);
		ab.setDisplayShowTitleEnabled(true);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		ab.show();
	}

	/**
	 * Gets the actionbar
	 * 
	 * @return ActionBar, what else?
	 */
	public ActionBar getActionBar() {
		return ab;
	}
}
