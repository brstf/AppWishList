package com.brstf.appwishlist;

/**
 * Class for storing information about an entry into the wishlist
 * Stores:
 * 	- URL to Google Play listing
 * 	- The name of the App
 * 	- The icon of the App
 * 	- The current price of the App
 * 	- The normal price of the App
 *  - The rating of the App (?)
 * @author brstf
 */

public class WLAppEntry {
	private String mName;	// Name of the app
	private String mURL;   	// URL link to Google Play Listing
	private String mIcon;   // path to the icon file
	private float mCPrice; 	// current price
	private float mOPrice; 	// original price (or highest price)
	private int mDbId;		// ID of the entry in the database
	
	/**
	 * Default constructor, initiates all parameters to default 
	 * meaningless values
	 */
	public WLAppEntry(int id) {
		//Initiate to default parameters
		mName 	= "";
		mURL 	= "";
		mIcon	= "";
		mCPrice	= -1.0f;
		mOPrice = -1.0f;
		mDbId = id;
	}
	
	//Accessors
	
	/**
	 * Gets the name of the app
	 * @return The name of the app stored in mName
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Gets the URL of the Google Play listing
	 * @return The URL of the Google Play listing
	 */
	public String getURL() {
		return mURL;
	}
	
	/**
	 * Gets the path to the app icon
	 * @return The path to the app icon
	 */
	public String getIconPath() {
		return mIcon;
	}
	
	/**
	 * Gets the current price of the App
	 * @return The current price of the App
	 */
	public float getCurrentPrice() {
		return mCPrice;
	}
	
	/**
	 * Gets the original price of the App
	 * @return The original price of the App
	 */
	public float getOriginalPrice() {
		return mOPrice;
	}
	
	/**
	 * Gets the database id of the entry
	 * @return The id
	 */
	public int getId() {
		return mDbId;
	}
	
	//Mutators
	
	/**
	 * Sets the name of this app entry
	 * @param name The new name to assign to the app entry
	 */
	public void setName( String name ) {
		mName = name;
	}
	
	/**
	 * Sets the URL of this app entry.  Assignment can only be done once.
	 * @param url The URL to assign to this entry
	 * @return Whether or not the assignment was successful
	 */
	public boolean setURL( String url ) {
		//Can only set URL once, after that point, all changes should come from changes
		// in the Play store.  This shouldn't come up, but just to be safe prevent it
		if( mURL.equals("") ) {
			mURL = url;
			return true;
		} 
		return false;
	}
	
	/**
	 * Sets the icon path of this app entry
	 * @param icon The new icon path of the entry
	 */
	public void setIconPath( String icon ) {
		mIcon = icon;
	}
	
	/**
	 * Sets the current price of this app
	 * @param price The new price of the app
	 */
	public void setCurrentPrice( float price ) {
		mCPrice = price;
	}

	/**
	 * Sets the original price of the app (similar to URL can only be set once)
	 * @param price The original price of the app
	 */
	public void setOriginalPrice( float price ) {
		mOPrice = price;
		if( mCPrice == -1.0f ) {
			setCurrentPrice( price );
		}
	}
}
