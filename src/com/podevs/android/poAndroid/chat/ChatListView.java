package com.podevs.android.poAndroid.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ChatListView extends ListView {
	public ChatListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ChatListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChatListView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if ((ev.getAction() & MotionEvent.ACTION_UP) != 0) {
			synchronized(this) {
				notifyAll();
			}
		}
		return super.onTouchEvent(ev);
	}
}
