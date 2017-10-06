/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.*;
import android.os.Debug;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import com.podevs.android.poAndroid.R;

import java.util.ArrayList;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */

public class DragLayer extends FrameLayout implements DragController {
    private static final int ANIMATION_SCALE_UP_DURATION = 110;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    // Number of pixels to add to the dragged item for scaling
    private static final float DRAG_SCALE = 24.0f;

    private boolean mDragging = false;
    private boolean mShouldDrop;
    private float mLastMotionX;
    private float mLastMotionY;

    /**
     * The bitmap that is currently being dragged
     */
    private Bitmap mDragBitmap = null;
    private View mOriginator;

    private int mBitmapOffsetX;
    private int mBitmapOffsetY;

    /**
     * X offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetX;

    /**
     * Y offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetY;

    /**
     * Utility rectangle
     */
    private Rect mDragRect = new Rect();

    /**
     * Where the drag originated
     */
    private DragSource mDragSource;

    /**
     * The data associated with the object being dragged
     */
    private Object mDragInfo;

    private final Rect mRect = new Rect();
    private final int[] mDropCoordinates = new int[2];

    // Faruq: utilize array list instead
    private ArrayList<DragListener> mListener = new ArrayList<>();

    private View mIgnoredDropTarget;

    private RectF mDragRegion;
    private boolean mEnteredRegion;
    private DropTarget mLastDropTarget;

    private final Paint mTrashPaint = new Paint();
    private Paint mDragPaint;

    private static final int ANIMATION_STATE_STARTING = 1;
    private static final int ANIMATION_STATE_RUNNING = 2;
    private static final int ANIMATION_STATE_DONE = 3;

    private static final int ANIMATION_TYPE_SCALE = 1;

    private float mAnimationFrom;
    private float mAnimationTo;
    private int mAnimationDuration;
    private long mAnimationStartTime;
    private int mAnimationType;
    private int mAnimationState = ANIMATION_STATE_DONE;

    private InputMethodManager mInputMethodManager;
    //ADW: Vars to use on fallback when the view bitmap cannot be generated
    private int mDrawWidth;
    private int mDrawHeight;
    private Paint mRectPaint;
    private static final int COLOR_NORMAL=0x66FF0000;
    private static final int COLOR_TRASH=0xAAFF0000;
    private boolean mDrawModeBitmap=true;
    ActivityManager activityManager;
    int[] pids;
    Debug.MemoryInfo[] memoryInfoArray;
    float debugTextSize;
    
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Make estimated paint area in gray
        int snagColor = context.getResources().getColor(R.color.snag_callout_color);
        Paint estimatedPaint = new Paint();
        estimatedPaint.setColor(snagColor);
        estimatedPaint.setStrokeWidth(3);
        estimatedPaint.setAntiAlias(true);
        mRectPaint=new Paint();
        mRectPaint.setColor(COLOR_NORMAL);
        activityManager =(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        pids = new int[]{};
        debugTextSize=DisplayMetrics.DENSITY_DEFAULT/10;
    }

    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

        // Faruq: ArrayList For Each
        for (DragListener l : mListener) {
        	l.onDragStart(v, source, dragInfo, dragAction);
        }
        Rect r = mDragRect;
        r.set(v.getScrollX(), v.getScrollY(), 0, 0);

        offsetDescendantRectToMyCoords(v, r);
        mTouchOffsetX = mLastMotionX - r.left;
        mTouchOffsetY = mLastMotionY - r.top;

        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap viewBitmap = v.getDrawingCache();
        if(viewBitmap!=null){
        	mDrawModeBitmap=true;
	        int width = viewBitmap.getWidth();
	        int height = viewBitmap.getHeight();
	
	        Matrix scale = new Matrix();
	        float scaleFactor = v.getWidth();
	        scaleFactor = (scaleFactor + DRAG_SCALE) /scaleFactor;
	        scale.setScale(scaleFactor, scaleFactor);
	
	        mAnimationTo = 1.0f;
	        mAnimationFrom = 1.0f / scaleFactor;
	        mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
	        mAnimationState = ANIMATION_STATE_STARTING;
	        mAnimationType = ANIMATION_TYPE_SCALE;
            try {
    	        mDragBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, width, height, scale, true);
                final Bitmap dragBitmap = mDragBitmap;
                mBitmapOffsetX = (dragBitmap.getWidth() - width) / 2;
                mBitmapOffsetY = (dragBitmap.getHeight() - height) / 2;
            } catch (OutOfMemoryError e) {
                mDrawModeBitmap=false;
                width = v.getWidth();
                height = v.getHeight();
                scaleFactor = v.getWidth();
                scaleFactor = (scaleFactor + DRAG_SCALE) /scaleFactor;
                mDrawWidth=(int) (v.getWidth()*scaleFactor);
                mDrawHeight=(int) (v.getHeight()*scaleFactor);
                mAnimationTo = 1.0f;
                mAnimationFrom = 1.0f / scaleFactor;
                mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
                mAnimationState = ANIMATION_STATE_STARTING;
                mAnimationType = ANIMATION_TYPE_SCALE;
                mBitmapOffsetX = (mDrawWidth-width) / 2;
                mBitmapOffsetY = (mDrawHeight-height) / 2;
            }
	        v.destroyDrawingCache();
	        v.setWillNotCacheDrawing(willNotCache);
	        v.setDrawingCacheBackgroundColor(color);
        }else{
        	mDrawModeBitmap=false;
            int width = v.getWidth();
            int height = v.getHeight();
            float scaleFactor = v.getWidth();
            scaleFactor = (scaleFactor + DRAG_SCALE) /scaleFactor;
            mDrawWidth=(int) (v.getWidth()*scaleFactor);
            mDrawHeight=(int) (v.getHeight()*scaleFactor);
            mAnimationTo = 1.0f;
            mAnimationFrom = 1.0f / scaleFactor;
            mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
            mAnimationState = ANIMATION_STATE_STARTING;
            mAnimationType = ANIMATION_TYPE_SCALE;
            mBitmapOffsetX = (mDrawWidth-width) / 2;
            mBitmapOffsetY = (mDrawHeight-height) / 2;
        }
        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(INVISIBLE);
        }

        mDragPaint = null;
        mDragging = true;
        mShouldDrop = true;
        mOriginator = v;
        mDragSource = source;
        mDragInfo = dragInfo;

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

        mEnteredRegion = false;

        invalidate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging || super.dispatchKeyEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mDragging) {
            if (mAnimationState == ANIMATION_STATE_STARTING) {
                mAnimationStartTime = SystemClock.uptimeMillis();
                mAnimationState = ANIMATION_STATE_RUNNING;
            }

            if (mAnimationState == ANIMATION_STATE_RUNNING) {
                float normalized = (float) (SystemClock.uptimeMillis() - mAnimationStartTime) /
                        mAnimationDuration;
                if (normalized >= 1.0f) {
                    mAnimationState = ANIMATION_STATE_DONE;
                }
                normalized = Math.min(normalized, 1.0f);
                final float value = mAnimationFrom  + (mAnimationTo - mAnimationFrom) * normalized;

                switch (mAnimationType) {
                    case ANIMATION_TYPE_SCALE:
                        if(mDrawModeBitmap && mDragBitmap!=null){
	                    	final Bitmap dragBitmap = mDragBitmap;
	                        canvas.save();
	                        canvas.translate(getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
	                                getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);
	                        canvas.translate((dragBitmap.getWidth() * (1.0f - value)) / 2,
	                                (dragBitmap.getHeight() * (1.0f - value)) / 2);
	                        canvas.scale(value, value);
	                        canvas.drawBitmap(dragBitmap, 0.0f, 0.0f, mDragPaint);
	                        canvas.restore();
                        }else{
                            canvas.save();
                            canvas.translate(getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                                    getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);
                            canvas.translate((mDrawWidth * (1.0f - value)) / 2,
                                    (mDrawHeight * (1.0f - value)) / 2);
                            canvas.drawRoundRect(new RectF(0, 0,mDrawWidth , mDrawHeight), 8.0f, 8.0f, mRectPaint);
                            canvas.restore();
                        }
                        break;
                }
            } else {
                // Draw actual icon being dragged
                if(mDrawModeBitmap && mDragBitmap!=null){
                	canvas.drawBitmap(mDragBitmap,
                        getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                        getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY, mDragPaint);
                }else{
                    canvas.save();
                	canvas.translate(getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                            getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);
                	canvas.drawRoundRect(new RectF(0, 0,mDrawWidth , mDrawHeight), 8.0f, 8.0f, mRectPaint);
                	canvas.restore();
                }
            }
        }
        if (pids.length > 0) {
            mRectPaint.setTextSize(debugTextSize);
            mRectPaint.setAntiAlias(true);
            mRectPaint.setColor(0xff000000);
            if (pids.length > 0)
                canvas.drawRect(0, 0, getWidth(), 70, mRectPaint);
            mRectPaint.setColor(0xffffffff);
            memoryInfoArray= activityManager.getProcessMemoryInfo(pids);
            for(Debug.MemoryInfo pidMemoryInfo: memoryInfoArray)
            {
                canvas.drawText("getTotalPrivateDirty: " + pidMemoryInfo.getTotalPrivateDirty(), 0, debugTextSize, mRectPaint);
                canvas.drawText("getTotalPss: " + pidMemoryInfo.getTotalPss(), 0, debugTextSize*2, mRectPaint);
                canvas.drawText("getTotalSharedDirty: " + pidMemoryInfo.getTotalSharedDirty(), 0, debugTextSize*3, mRectPaint);
            }
        }
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
            }
            if (mOriginator != null) {
                mOriginator.setVisibility(VISIBLE);
            }
            // Faruq: ArrayList For Each
            for (DragListener l : mListener) {
            	l.onDragEnd();
            }  
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mLastDropTarget = null;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mShouldDrop && drop(x, y)) {
                    mShouldDrop = false;
                }
                endDrag();
                break;
        }

        return mDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:

            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;
            
            break;
        case MotionEvent.ACTION_MOVE:

            final float touchX = mTouchOffsetX;
            final float touchY = mTouchOffsetY;

            final int offsetX = mBitmapOffsetX;
            final int offsetY = mBitmapOffsetY;

            int left = (int) (mLastMotionX - touchX - offsetX);
            int top = (int) (mLastMotionY - touchY - offsetY);

            int width;
            int height;
            if(mDrawModeBitmap && mDragBitmap!=null){
	            final Bitmap dragBitmap = mDragBitmap;
	            width = dragBitmap.getWidth();
	            height = dragBitmap.getHeight();
            }else{
                width = mDrawWidth;
                height = mDrawHeight;            	
            }
            final Rect rect = mRect;
            rect.set(left - 1, top - 1, left + width + 1, top + height + 1);

            mLastMotionX = x;
            mLastMotionY = y;

            left = (int) (x - touchX - offsetX);
            top = (int) (y - touchY - offsetY);

            // Invalidate current icon position
            rect.union(left - 1, top - 1, left + width + 1, top + height + 1);

            final int[] coordinates = mDropCoordinates;
            DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
                    dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                    }
                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            } else {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            }

            invalidate(rect);

            mLastDropTarget = dropTarget;

            if (mDragRegion != null) {
                final RectF region = mDragRegion;
                final boolean inRegion = region.contains(ev.getRawX(), ev.getRawY());
                if (!mEnteredRegion && inRegion) {
                    mDragPaint = mTrashPaint;
                    mRectPaint.setColor(COLOR_TRASH);
                    mEnteredRegion = true;
                } else if (mEnteredRegion && !inRegion) {
                    mDragPaint = null;
                    mRectPaint.setColor(COLOR_NORMAL);
                    mEnteredRegion = false;
                }
            
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mShouldDrop) {
                drop(x, y);
                mShouldDrop = false;
            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            endDrag();
        }

        return true;
    }

    private boolean drop(float x, float y) {
        invalidate();

        final int[] coordinates = mDropCoordinates;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        if (dropTarget != null) {
        	dropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
        			(int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
        	if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1],
        			(int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo)) {
        		dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1],
        				(int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
        		mDragSource.onDropCompleted((View) dropTarget, true);
        		return true;
        	} else {
        		mDragSource.onDropCompleted((View) dropTarget, false);
        		return true;
        	}
        }
        return false;
    }

    DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        return findDropTarget(this, x, y, dropCoordinates);
    }

    private DropTarget findDropTarget(ViewGroup container, int x, int y, int[] dropCoordinates) {
        final Rect r = mDragRect;
        final int count = container.getChildCount();
        final int scrolledX = x + container.getScrollX();
        final int scrolledY = y + container.getScrollY();
        final View ignoredDropTarget = mIgnoredDropTarget;

        for (int i = count - 1; i >= 0; i--) {
            final View child = container.getChildAt(i);
            if (child.getVisibility() == VISIBLE && child != ignoredDropTarget) {
                child.getHitRect(r);
                if (r.contains(scrolledX, scrolledY)) {
                    DropTarget target = null;
                    if (child instanceof ViewGroup) {
                        x = scrolledX - child.getLeft();
                        y = scrolledY - child.getTop();
                        target = findDropTarget((ViewGroup) child, x, y, dropCoordinates);
                    }
                    if (target == null) {
                        if (child instanceof DropTarget) {
                            // Only consider this child if they will accept
                            DropTarget childTarget = (DropTarget) child;
                            if (childTarget.acceptDrop(mDragSource, x, y, 0, 0, mDragInfo)) {
                                dropCoordinates[0] = x;
                                dropCoordinates[1] = y;
                                return (DropTarget) child;
                            } else {
                                return null;
                            }
                        }
                    } else {
                        return target;
                    }
                }
            }
        }

        return null;
    }

    // Faruq: utilize array list instead
    public void addDragListener(DragListener l) {
    	mListener.add(l);
    }

    public void removeDragListener(DragListener l) {
    	mListener.remove(l);
    } 
    
    /**
     * Specifies the view that must be ignored when looking for a drop target.
     *
     * @param view The view that will not be taken into account while looking
     *        for a drop target.
     */
    void setIgnoredDropTarget(View view) {
        mIgnoredDropTarget = view;
    }

    /**
     * Specifies the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    void setDeleteRegion(RectF region) {
        mDragRegion = region;
    }

}
