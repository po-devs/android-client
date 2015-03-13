package com.podevs.android.htmlTableBeta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;

/**
 * Created by JonathanJM on 3/7/2015.
 */

public class ViewParser {
    View mView;
    Context mContext;

    public ViewParser(View v) {
        mView = v;
    }

    public ViewParser(Context context) {
        mContext = context;
    }

    public BitmapDrawable betaTest() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.table, null);
        TextView text = (TextView) view.findViewById(R.id.textView1);
        text.setText("HIIIIIIIII");
        text.setTextColor(Color.RED);
        return new BitmapDrawable(mContext.getResources(), getBitmap(view));
    }

    public Drawable getDrawable() {
        return new Drawable() {
            @Override
            public void draw(Canvas canvas) {

            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter cf) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
    }

    public Bitmap getBitmap2(View mView) {
        Bitmap returnedBitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = mView.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        mView.draw(canvas);
        return returnedBitmap;
    }

    public Bitmap getBitmap(View mView) {
        mView.clearFocus();
        mView.setPressed(false);
        mView.setWillNotCacheDrawing(false);
        int color = mView.getDrawingCacheBackgroundColor();
        mView.setDrawingCacheBackgroundColor(color);

        if (color != 0) {
            mView.destroyDrawingCache();
        }

        mView.buildDrawingCache();

        Bitmap cache = mView.getDrawingCache();

        if (cache == null) {
            if (mView.getMeasuredHeight() <= 0) {
                mView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Bitmap bitmap = Bitmap.createBitmap(mView.getMeasuredWidth(), mView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
                mView.draw(canvas);
                return bitmap;
            } else {
                ViewGroup.LayoutParams params = mView.getLayoutParams();
                Bitmap bitmap = Bitmap.createBitmap(params.width, params.height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                mView.layout(mView.getLeft(), mView.getTop(), mView.getRight(), mView.getBottom());
                mView.draw(canvas);
                return bitmap;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(cache);
        return bitmap;
    }
}
