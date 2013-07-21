package com.podevs.android.pokemononline.teambuilder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.pokeinfo.InfoConfig;

public class TeambuilderActivity extends Activity {
	protected ViewPager viewPager;

	View mainLayout, teamLayout;
	private class MyAdapter extends PagerAdapter
	{
		@Override
		public int getCount() {
			return 2;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			switch (position) {
			case 0: container.addView(mainLayout);return mainLayout;
			case 1: container.addView(teamLayout);return teamLayout;
			}
			return null;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (Object)arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	InfoConfig.context = this;

        super.onCreate(savedInstanceState);

        viewPager = (ViewPager) new ViewPager(this);
		mainLayout = getLayoutInflater().inflate(R.layout.battle_mainscreen, null);
		teamLayout = getLayoutInflater().inflate(R.layout.battle_teamscreen, null);
		
		viewPager.setAdapter(new MyAdapter());
		setContentView(viewPager);
    }
}
