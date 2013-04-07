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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pokebros.android.pokemononline.NetworkService;
import com.pokebros.android.pokemononline.R;
import com.pokebros.android.pokemononline.pms.PrivateMessageList.PrivateMessageListListener;
import com.viewpagerindicator.TitlePageIndicator;

public class PrivateMessageActivity extends Activity {
	protected PrivateMessageList pms;
	protected MyAdapter adapter = new MyAdapter();
	protected NetworkService netServ;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        
        setContentView(R.layout.pm_activity);
        final ViewPager vp = (ViewPager) findViewById(R.id.viewPager);
        vp.setAdapter(adapter);
        
        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
        titleIndicator.setViewPager(vp);
        
        final EditText send = (EditText) findViewById(R.id.pmInput);
        send.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
            	// and the socket is connected
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER) && netServ != null) {
                	PrivateMessage pm = adapter.getItemAt(vp.getCurrentItem()); 
                	int id = pm.other.id;
                	
                	if (netServ.players.get(id) == null) {
                		Toast.makeText(PrivateMessageActivity.this, "This player is offline", Toast.LENGTH_SHORT).show();
                		return true;
                	}
                	
                	// Perform action on key press
                	netServ.sendPM(id, send.getText().toString());
                	pm.addMessage(pm.me, send.getText().toString());
                	send.getText().clear();
                  return true;
                }
                return false;
            }
		});
    }
    
    private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			pms = netServ.pms;
			pms.listener = adapter;
			adapter.notifyDataSetChanged();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ = null;
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
			if (pms == null) {
				return 0;
			} else {
				return pms.privateMessages.size();
			}
		}
		
		PrivateMessage getItemAt(int position) {
			if (pms == null) {
				return null;
			}
			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
			for (int i = 0; i < position; i++) {
				it.next();
			}
			return it.next();
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			PrivateMessage pm = getItemAt(position);
			
			if (pm == null) {
				return null;
			}
			
			ListView lv = new ListView(PrivateMessageActivity.this);
			container.addView(lv);
			
			lv.setAdapter(new PrivateMessageAdapter(PrivateMessageActivity.this, pm));
			lv.setTag(R.id.associated_pm, pm);
			lv.setTag(R.id.position, position);

			return lv;
		}
		
		@Override
		public int getItemPosition(Object object) {
			if (pms == null) {
				return POSITION_UNCHANGED;
			}

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
			runOnUiThread(new Runnable() {
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	}
}
