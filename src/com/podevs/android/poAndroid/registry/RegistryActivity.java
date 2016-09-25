package com.podevs.android.poAndroid.registry;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.chat.ChatActivity;
import com.podevs.android.poAndroid.player.FullPlayerInfo;
import com.podevs.android.poAndroid.pokeinfo.InfoConfig;
import com.podevs.android.poAndroid.registry.RegistryConnectionService.RegistryCommandListener;
import com.podevs.android.poAndroid.registry.ServerListAdapter.Server;
import com.podevs.android.poAndroid.settings.SetPreferenceActivity;
import com.podevs.android.poAndroid.teambuilder.TeambuilderActivity;
import com.podevs.android.utilities.Baos;

public class RegistryActivity extends FragmentActivity implements ServiceConnection, RegistryCommandListener {
	
	static final String TAG = "RegistryActivity";
	static final int TEAMBUILDER_CODE = 1;
	
	private ListView servers;
	private boolean viewToggle = false;
	private RelativeLayout registryRoot;
	private ServerListAdapter adapter;
	private EditText editAddr;
	private EditText editName;
	private boolean bound = false;
	private FullPlayerInfo meLoginPlayer = null;
	private SharedPreferences prefs;

	public static boolean localize_assets = false;
	public static Resources resources;

	/*
	enum RegistryDialog {
		SelectImportMethod,
		ImportTeamFromFile
	}
	*/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if (InfoConfig.context == null) {
    		InfoConfig.context = this;
    	}

		Log.e("HELLO", "HELLO");


        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        super.onCreate(savedInstanceState);

		localize_assets = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("localize", false);
		resources = getResources();

        if (!getIntent().hasExtra("sticky")) {
	        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            if ("com.podevs.android.poAndroid.NetworkService".equals(service.service.getClassName())) {
					startActivity(new Intent(RegistryActivity.this, ChatActivity.class));
	            	finish();
	            	return;
	            }
	        }
        }

        if (getIntent().hasExtra("failedConnect")) {
        	Toast.makeText(this, "Server connection failed", Toast.LENGTH_LONG).show();
        }

        this.stopService(new Intent(RegistryActivity.this, NetworkService.class));
        
        setContentView(R.layout.main);

        prefs = getPreferences(MODE_PRIVATE);
        
		editAddr = (EditText)RegistryActivity.this.findViewById(R.id.addredit);
		editAddr.setText(prefs.getString("lastAddr", ""));
		editName = (EditText)RegistryActivity.this.findViewById(R.id.nameedit);
		// Hide the soft-keyboard when the activity is created
		editName.setInputType(InputType.TYPE_NULL);
		editName.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				editName.setInputType(InputType.TYPE_CLASS_TEXT);
				editName.onTouchEvent(event);
				return true;
			}
		});

		meLoginPlayer = new FullPlayerInfo(RegistryActivity.this);
		editName.setText(meLoginPlayer.nick());
		
		//Capture out button from layout
        Button conbutton = (Button)findViewById(R.id.connectbutton);
        Button importbutton = (Button)findViewById(R.id.importteambutton);
		Button setbutton = (Button)findViewById(R.id.settings);

        //Register onClick listener
        conbutton.setOnClickListener(registryListener);
        importbutton.setOnClickListener(registryListener);
		setbutton.setOnClickListener(registryListener);

        registryRoot = (RelativeLayout) findViewById(R.id.registryroot);
        servers = (ListView)findViewById(R.id.serverlisting);
        adapter = new ServerListAdapter(this, R.id.serverlisting);
        servers.setAdapter(adapter);
        servers.setOnItemClickListener(new OnItemClickListener() {
        	/* Set the edit texts on list item click */
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Server server = (Server)parent.getItemAtPosition(position);
				editAddr.setText(server.ip + ":" + String.valueOf(server.port));
			}
		});
        
        
        servers.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Server server = (Server)parent.getItemAtPosition(position);
				Log.e(TAG, "Long click works: " + server.desc);
				// TODO: Display server description
		        /*Intent intent = new Intent(RegistryActivity.this, RichTextActivity.class);
		        intent.putExtra("richtext", server.desc);
		        RegistryActivity.this.startActivity(intent); */
				return true;
			}
        	
		});
        
        Intent intent = new Intent(RegistryActivity.this, RegistryConnectionService.class);
        bound = bindService(intent, this, BIND_AUTO_CREATE);

    }

    private OnClickListener registryListener = new OnClickListener() {
		public void onClick(View v) {
    		if (v == findViewById(R.id.connectbutton)){
    			String ipString = editAddr.getText().toString().split(":")[0];
    			String portString = "";
    			try {
    				portString = editAddr.getText().toString().split(":")[1];
    			} catch (ArrayIndexOutOfBoundsException e) {
					// No need to act
    			}
    			int portVal = -1;
				try {
					portVal = Integer.parseInt(portString);
				} catch(NumberFormatException e) {
					// No need to act
				}
				if (portVal < 1 || portVal > 65535) {
					Toast.makeText(RegistryActivity.this, "Invalid value for port", Toast.LENGTH_LONG).show();
        			return;
				}
				
				String nick = editName.getText().toString().trim();
				if (nick.length() == 0) {
					Toast.makeText(RegistryActivity.this, "Please enter a trainer name.", Toast.LENGTH_LONG).show();
					return;
				}
				
				meLoginPlayer.profile.nick = nick;
				
				Intent intent = new Intent(RegistryActivity.this, NetworkService.class);
				intent.putExtra("ip", ipString);
				intent.putExtra("port", portVal);
				intent.putExtra("loginPlayer", new Baos().putBaos(meLoginPlayer).toByteArray());
				prefs.edit().putString("lastAddr", editAddr.getText().toString()).commit();
				meLoginPlayer.profile.save(RegistryActivity.this);

				startService(intent);
				startActivity(new Intent(RegistryActivity.this, ChatActivity.class));
				RegistryActivity.this.finish();
    		}
			else if (v == findViewById(R.id.importteambutton)) {
    			startActivityForResult(new Intent(RegistryActivity.this, TeambuilderActivity.class), TEAMBUILDER_CODE);
    		} else if (v == findViewById(R.id.settings)) {
				startActivity(new Intent(RegistryActivity.this, SetPreferenceActivity.class));
    		}
    	}
    };
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == TEAMBUILDER_CODE) {
			/* If the teambuilder did something, reload the team */
			//if (resultCode == Activity.RESULT_OK) {
				meLoginPlayer = new FullPlayerInfo(RegistryActivity.this);
				editName.setText(meLoginPlayer.nick());
			//}
		}
	}
    	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainoptions, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int which, MenuItem item) {
    	// XXX Placeholder
    	return true;
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		registryRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				if(!viewToggle) {
					ServerListAdapter oldadapter = adapter;
					setContentView(R.layout.main);
			        servers = (ListView) findViewById(R.id.serverlisting);
			        servers.setAdapter(oldadapter);
			        viewToggle = true;
				}
				printToast("ONCONFIGURATION CHANGED", Toast.LENGTH_SHORT);
			}
		});
	}
    
	synchronized public void ServerListEnd() {
		if (!bound) {
			return;
		}
		Log.i(TAG, "Unbinding registry connection service");
		bound = false;
		
		try {
			unbindService(RegistryActivity.this);
		} catch (IllegalArgumentException ex) {
			/* Might happen if the registry acitivity was already stopped
			 * and the service was unbound from there.
			 */
			return;
		}

		runOnUiThread(new Runnable() {
			public void run() {
				adapter.sortByPlayers();
			}
		});
	}
	
	public void RegistryConnectionClosed() {
		ServerListEnd();
	}

	public void NewServer(final String name, final String desc, final short players,
			final String ip, final short maxplayers, final int port) {
		runOnUiThread(new Runnable() {
			public void run() {
            	adapter.addServer(name, desc, ip, port, players, maxplayers);		
			}
		});
	}

	synchronized public void onServiceConnected(ComponentName name, IBinder binder) {
		bound = true;
		((RegistryConnectionService.LocalBinder)binder).connect(this);
	}
	
	public void printToast(final String s, final int len) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(RegistryActivity.this, s, len).show();
			}
		});
	}

	synchronized public void onServiceDisconnected(ComponentName name) {
		bound = false;
	}
    
    @Override
    synchronized public void onDestroy() {
    	if (bound) {
    		unbindService(this);
    		bound = false;
    	}
    	super.onDestroy();
    }
}