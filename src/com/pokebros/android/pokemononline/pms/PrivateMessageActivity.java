package com.pokebros.android.pokemononline.pms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

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

	public void onNewPM(PrivateMessage privateMessage) {
		//Todo: manage adapter & all
	}

}
