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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pokebros.android.pokemononline.NetworkService;
import com.pokebros.android.pokemononline.R;
import com.pokebros.android.pokemononline.pms.PrivateMessageList.PrivateMessageListListener;

public class PrivateMessageActivity extends Activity implements PrivateMessageListListener {
	protected PrivateMessageList pms;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        
        setContentView(R.layout.pm_activity);        
    }
    
    private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			NetworkService netServ = ((NetworkService.LocalBinder)service).getService();
			pms = netServ.pms;
			pms.listener = PrivateMessageActivity.this;
		}
		
		public void onServiceDisconnected(ComponentName className) {
		}
	};
	
	private class MyAdapter extends PagerAdapter
	{
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

	public void onNewPM(PrivateMessage privateMessage) {
		//Todo: manage adapter & all
	}

}
