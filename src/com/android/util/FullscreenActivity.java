package com.android.util;

import com.android.util.SystemUiHider;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class FullscreenActivity extends Activity {
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private SystemUiHider mSystemUiHider;
    
    protected void onCreate(Bundle savedInstanceState, View contentView) {
        super.onCreate(savedInstanceState);
        
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView,
        										   SystemUiHider.FLAG_HIDE_NAVIGATION);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(
    		new SystemUiHider.OnVisibilityChangeListener() {
				@Override
				public void onVisibilityChange(boolean visible) {
				    if (visible) {
				        delayedHide(AUTO_HIDE_DELAY_MILLIS);
				    }
				}
    		}
        );

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSystemUiHider.toggle();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(1000);
    }

    @Override
    protected void onPostResume() {
    	super.onPostResume();
    	delayedHide(1000);
        mSystemUiHider.show();
    }

    private Handler mHideHandler = new Handler();
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    // Schedules a call to hide() in delay milliseconds, canceling any
    // previously scheduled calls.
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
