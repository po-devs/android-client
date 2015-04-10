package com.podevs.android.utilities;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * ViewPager
 */

public class SmartViewPager extends ViewPager {

	public SmartViewPager(Context context) {
		super(context);
	}

	public SmartViewPager(Context context, AttributeSet set) {
		super(context, set);
	}
	
	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
	   if(v != this && v instanceof ViewPager) {
	      return true;
	   }
	   return super.canScroll(v, checkV, dx, x, y);
	}
}
