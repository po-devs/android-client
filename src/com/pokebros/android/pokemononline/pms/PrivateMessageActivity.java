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
		@Override
		public CharSequence getPageTitle(int position) {
			return getItemAt(position).other.nick();
		}

		@Override
		public int getCount() {
			return pms.privateMessages.size();
		}
		
		PrivateMessage getItemAt(int position) {
			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
			for (int i = 0; i < position; i++) {
				it.next();
			}
			return it.next();
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			PrivateMessage pm = getItemAt(position);
			
			ListView lv = new ListView(PrivateMessageActivity.this);
			container.addView(lv);
			
			lv.setAdapter(new PrivateMessageAdapter(PrivateMessageActivity.this, pm));
			lv.setTag(R.id.associated_pm, pm);
			lv.setTag(R.id.position, position);

			return lv;
		}
		
		@Override
		public int getItemPosition(Object object) {
			View v = (View) object;
			PrivateMessage pm = (PrivateMessage) v.getTag(R.id.associated_pm);
			int lastPos = (Integer) v.getTag(R.id.position);
			
			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
			for (int i = 0; i < pms.privateMessages.size(); i++) {
				if (it.next() == pm) {
					if (lastPos != i) {
						v.setTag(R.id.position, i);
						return i;
					}
					return POSITION_UNCHANGED;
				}
			}
			return POSITION_NONE;
		}

		@Override
		public boolean isViewFromObject(View v, Object o) {
			return v == o;
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
