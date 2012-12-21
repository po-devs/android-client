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

public class ChatRealViewSwitcher extends RealViewSwitcher {
	public ChatRealViewSwitcher(Context context) {
		super(context);
		init();
	}

	public ChatRealViewSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	@Override
	public void snapToScreen(int whichScreen) {
		if (!mScroller.isFinished())
			return;

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

		mNextScreen = whichScreen;

		final int newX = (whichScreen * getWidth() + getWidth())/2;
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		invalidate();
	}
}