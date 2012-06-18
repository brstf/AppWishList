package com.brstf.appwishlist.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView {

	public SquareImageView(Context context) {
	    super(context);
	}
	
	public SquareImageView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	 @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	
	    int width = MeasureSpec.getSize(widthMeasureSpec);
	
	    super.onMeasure(
	            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
	            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
	    );
	}
}