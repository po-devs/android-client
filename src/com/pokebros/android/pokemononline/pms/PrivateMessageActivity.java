package com.pokebros.android.pokemononline.pms;

import java.util.Iterator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pokebros.android.pokemononline.NetworkService;
import com.pokebros.android.pokemononline.R;
import com.pokebros.android.pokemononline.pms.PrivateMessageList.PrivateMessageListListener;

public class PrivateMessageActivity extends Activity {
	protected PrivateMessageList pms;
	protected MyAdapter adapter = new MyAdapter();

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        
        setContentView(R.layout.pm_activity);
        ViewPager vp = (ViewPager) findViewById(R.id.viewPager);
        vp.setAdapter(adapter);
    }
    
    private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			NetworkService netServ = ((NetworkService.LocalBinder)service).getService();
			pms = netServ.pms;
			pms.listener = adapter;
			adapter.notifyDataSetChanged();
		}
		
		public void onServiceDisconnected(ComponentName className) {
		}
	};
	
	private class MyAdapter extends PagerAdapter implements PrivateMessageListListener
	{
		MyAdapter() {
			pms.listener = this;
		}
		
		@Override
		public int getCount() {
			return pms.privateMessages.size();
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
			for (int i = 0; i < position; i++) {
				it.next();
			}
			PrivateMessage pm = it.next();
			
			//TODO: recycle views?
			ListView lv = new ListView(PrivateMessageActivity.this);
			container.addView(lv);
			
			lv.setAdapter(new PrivateMessageAdapter(PrivateMessageActivity.this, pm));

			return lv;
		}

		@Override
		public boolean isViewFromObject(View v, Object o) {
			return (Object)v == o;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		public void onNewPM(PrivateMessage privateMessage) {
			notifyDataSetChanged();
		}
	}
}
