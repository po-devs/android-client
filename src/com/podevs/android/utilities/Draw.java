package com.podevs.android.utilities;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

@Deprecated
public class Draw extends BitmapDrawable {
    protected Drawable drawable;
    // Needs to use non-deprecated bitmapDrawable method.

    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }
}
