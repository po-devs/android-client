/*
 * Copyright (C) 2010 Marc Reichelt
 * 
 * Work derived from Workspace.java of the Launcher application
 *  see http://android.git.kernel.org/?p=platform/packages/apps/Launcher.git
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.marcreichelt.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * RealViewSwitcher allows users to switch between multiple screens (layouts) in the same way as the Android home screen (Launcher application).
 * <p>
 * You can add and remove views using the normal methods {@link ViewGroup#addView(View)}, {@link ViewGroup#removeView(View)} etc. You may want to listen for updates by calling {@link RealViewSwitcher#setOnScreenSwitchListener(OnScreenSwitchListener)}
 * in order to perform operations once a new screen has been selected.
 * 
 * @author Marc Reichelt, <a href="http://www.marcreichelt.de/">http://www.marcreichelt.de/</a>
 * @version 0.1.0
 */
public class RealViewSwitcher extends ViewGroup {

	// TODO: This class does the basic stuff right now, but it would be cool to have certain things implemented,
	// e.g. using an adapter for getting views instead of setting them directly, memory management and the
	// possibility of scrolling vertically instead of horizontally. If you have ideas or patches, please visit
	// my website and drop me a mail. :-)

	/**
	 * Listener for the event that the RealViewSwitcher switches to a new view.
	 */
	public static interface OnScreenSwitchListener {

		/**
		 * Notifies listeners about the new screen. Runs after the animation completed.
		 * 
		 * @param screen The new screen index.
		 */
		void onScreenSwitched(int screen);

	}

	protected static final int SNAP_VELOCITY = 1000;
	protected static final int INVALID_SCREEN = -1;

	protected Scroller mScroller;
	protected VelocityTracker mVelocityTracker;

	protected final static int TOUCH_STATE_REST = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;

	protected int mTouchState = TOUCH_STATE_REST;

	protected boolean mPressed = false;
	protected float mLastMotionX;
	protected float mLastMotionY;
	protected int mTouchSlop;
	protected int mMaximumVelocity;
	protected int mCurrentScreen;
	protected int mNextScreen = INVALID_SCREEN;

	protected boolean mFirstLayout = true;

	protected OnScreenSwitchListener mOnScreenSwitchListener;

	public RealViewSwitcher(Context context) {
		super(context);
		init();
	}

	public RealViewSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		mScroller = new Scroller(getContext());

		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			scrollTo(mCurrentScreen * width, 0);
			mFirstLayout = false;
		}
	}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();
        
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;
                if (xMoved || yMoved) {
                    // If xDiff > yDiff means the finger path pitch is smaller than 45deg so we assume the user want to scroll X axis
                    if (xDiff > 2*yDiff) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        mPressed = false;
                        //enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);

                   }
                    else
                    	mPressed = true;
                    // If yDiff > xDiff means the finger path pitch is bigger than 45deg so we assume the user want to either scroll Y or Y-axis gesture
                   /* else
                    {
                        // As x scrolling is left untouched (more or less untouched;)), every gesture should start by dragging in Y axis. In fact I only consider useful, swipe up and down.
                        // Guess if the first Pointer where the user click belongs to where a scrollable widget is.
                                mTouchedScrollableWidget = isWidgetAtLocationScrollable((int)mLastMotionX,(int)mLastMotionY);
                        if (!mTouchedScrollableWidget)
                        {
                                // Only y axis movement. So may be a Swipe down or up gesture
                                if ((y - mLastMotionY) > 0){
                                        if(Math.abs(y-mLastMotionY)>(touchSlop*2))mTouchState = TOUCH_SWIPE_DOWN_GESTURE;
                                }else{
                                        if(Math.abs(y-mLastMotionY)>(touchSlop*2))mTouchState = TOUCH_SWIPE_UP_GESTURE;
                                }
                        }
                    }*/
                    // Either way, cancel any pending longpress
                    /*if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been scheduled
                        // by a distant descendant, so use the mAllowLongPress flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }*/
                }
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                //mAllowLongPress = true;

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                /*if (mTouchState != TOUCH_STATE_SCROLLING)// && mTouchState != TOUCH_SWIPE_DOWN_GESTURE && mTouchState != TOUCH_SWIPE_UP_GESTURE) {
                    //final CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
                    if (!currentScreen.lastDownOnOccupiedCell()) {
                        getLocationOnScreen(mTempCell);
                        // Send a tap to the wallpaper if the last down was on empty space
                        if(lwpSupport)
                        mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                                "android.wallpaper.tap",
                                mTempCell[0] + (int) ev.getX(),
                                mTempCell[1] + (int) ev.getY(), 0, null);
                    }
                }*/
                // Release the drag
                //clearChildrenCache();
            	mPressed = false;
                mTouchState = TOUCH_STATE_REST;
                //mAllowLongPress = false;
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }
    
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}

		return (mTouchState != TOUCH_STATE_REST);
	}

	protected void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;

		snapToScreen(whichScreen);
	}

	public void snapToScreen(int whichScreen) {
		if (!mScroller.isFinished())
			return;

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

		mNextScreen = whichScreen;

		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));

			// notify observer about screen change
			if (mOnScreenSwitchListener != null)
				mOnScreenSwitchListener.onScreenSwitched(mCurrentScreen);

			mNextScreen = INVALID_SCREEN;
		}
	}

	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen The new screen.
	 */
	public void setCurrentScreen(int currentScreen) {
		mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
		scrollTo(mCurrentScreen * getWidth(), 0);
		invalidate();
	}

	/**
	 * Sets the {@link ViewSwitcher.OnScreenSwitchListener}.
	 * 
	 * @param onScreenSwitchListener The listener for switch events.
	 */
	public void setOnScreenSwitchListener(OnScreenSwitchListener onScreenSwitchListener) {
		mOnScreenSwitchListener = onScreenSwitchListener;
	}
	
	public boolean isPressed(){
		return mPressed;
	}

}
