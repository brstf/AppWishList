package com.brstf.wishlist.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class SquareButton extends Button {

	public SquareButton(Context context) {
	    super(context);
	}
	
	public SquareButton(Context context, AttributeSet attrs) {
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