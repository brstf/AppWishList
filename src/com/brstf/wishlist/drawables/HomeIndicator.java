package com.brstf.wishlist.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class HomeIndicator extends Drawable {
	private int ac, msc, mvc, bkc, mgc;

	public HomeIndicator(int _ac, int _msc, int _mvc, int _bkc, int _mgc) {
		ac = _ac;
		msc = _msc;
		mvc = _mvc;
		bkc = _bkc;
		mgc = _mgc;
	}

	@Override
	public void draw(Canvas canvas) {
		float cw = (float) canvas.getWidth();
		float total = (float) (ac + msc + mvc + bkc + mgc);

		float left = 0.0f;

		float aw = ((float) ac / total * cw);
		float msw = ((float) msc / total * cw);
		float mvw = ((float) mvc / total * cw);
		float bkw = ((float) bkc / total * cw);
		float mgw = ((float) mgc / total * cw);

		// APP
		Paint paint = new Paint();
		paint.setColor(0xFFCDF93E);
		canvas.drawRect(left, 0, left + aw, canvas.getHeight(), paint);

		// MUSIC
		left += aw;
		paint.setColor(0xFFFF9200);
		canvas.drawRect(left, 0, left + msw, canvas.getHeight(), paint);

		// MOVIE
		left += msw;
		paint.setColor(0xFFBF3A30);
		canvas.drawRect(left, 0, left + mvw, canvas.getHeight(), paint);

		// BOOK
		left += mvw;
		paint.setColor(0xFF689CD2);
		canvas.drawRect(left, 0, left + bkw, canvas.getHeight(), paint);

		// MAGAZINE
		left += bkw;
		paint.setColor(0xFF912470);
		canvas.drawRect(left, 0, left + mgw, canvas.getHeight(), paint);

	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub

	}

}
