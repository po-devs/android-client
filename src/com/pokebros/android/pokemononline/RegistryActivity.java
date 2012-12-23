package com.pokebros.android.pokemononline;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pokebros.android.pokemononline.RegistryConnectionService.RegistryCommandListener;
import com.pokebros.android.pokemononline.ServerListAdapter.Server;
import com.pokebros.android.pokemononline.player.FullPlayerInfo;

public class RegistryActivity extends Activity implements ServiceConnection, RegistryCommandListener {
	
	static final String TAG = "RegistryActivity";
	
	private ListView servers;
	private boolean viewToggle = false;
	private RelativeLayout registryRoot;
	private ServerListAdapter adapter;
	private EditText editAddr;
	private EditText editName;
	private boolean bound = false;
	private FullPlayerInfo meLoginPlayer;
	private SharedPreferences prefs;
	RegistryConnectionService service;

	enum RegistryDialog {
		SelectImportMethod,
		ImportTeamFromFile
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If we are already connected to a server show ChatActivity instead of RegistryActivity
        if (!getIntent().hasExtra("sticky")) {
	        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            if ("com.pokebros.android.pokemononline.NetworkService".equals(service.service.getClassName())) {
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

		meLoginPlayer = new FullPlayerInfo(RegistryActivity.this, prefs);
		editName.append(meLoginPlayer.nick());
		
		//Capture out button from layout
        Button conbutton = (Button)findViewById(R.id.connectbutton);
        Button importbutton = (Button)findViewById(R.id.importteambutton);
        //Register onClick listener
        conbutton.setOnClickListener(registryListener);
        importbutton.setOnClickListener(registryListener);
        
        registryRoot = (RelativeLayout) findViewById(R.id.registryroot);
        servers = (ListView)findViewById(R.id.serverlisting);
        adapter = new ServerListAdapter(this, R.id.serverlisting);
        servers.setAdapter(adapter);
        servers.setOnItemClickListener(new OnItemClickListener() {
        	/* Set the edit texts on list item click */
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Server server = (Server)parent.getItemAtPosition(position);
				editAddr.setText("");
				editAddr.append(server.ip + ":" + String.valueOf(server.port));
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
        
//        Intent intent = new Intent(RegistryActivity.this, RegistryConnectionService.class);
//        bindService(intent, this, 0);
//        startService(intent);
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
    			short portVal = -1;
				try {
					portVal = Short.parseShort(portString);
				} catch(NumberFormatException e) {
					// No need to act
				}
				if (portVal < 1 || portVal > 65535) {
					Toast.makeText(RegistryActivity.this, "Invalid value for port", Toast.LENGTH_LONG).show();
        			return;
				}
				
				String nick = editName.getText().toString();
				if (nick.length() > 0 && !nick.equals(meLoginPlayer.nick())) {
					// Save name changes
					try {
						// Open team for reading
						FileInputStream team = RegistryActivity.this.openFileInput("team.xml");
						
						// Read team into ByteArrayOutputStream
						Baos saveBuffer = new Baos();
						byte[] buffer = new byte[1024];
						int length;
						while ((length = team.read(buffer))>0)
							saveBuffer.write(buffer, 0, length);
						team.close();
						
						// Replace trainer name in Baos with user entered trainer name
						String stringBuffer = new String(saveBuffer.toByteArray());
						stringBuffer = stringBuffer.replaceFirst(">.*</Trainer>", ">" + nick + "</Trainer>");
						
						// Write Baos to file
						FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);
						saveTeam.write(stringBuffer.getBytes());
						saveTeam.flush();
						saveTeam.close();
					} catch (IOException e) {
						Log.v(TAG, "team.xml doesn't exist");
					} finally {
						meLoginPlayer.playerTeam.nick = nick;
					}
				} else if (nick.length() == 0) {
					Toast.makeText(RegistryActivity.this, "Please enter a trainer name.", Toast.LENGTH_LONG).show();
					return;
				}
				
				Intent intent = new Intent(RegistryActivity.this, NetworkService.class);
				intent.putExtra("ip", ipString);
				intent.putExtra("port", portVal);
				intent.putExtra("loginPlayer", new Baos().putBaos(meLoginPlayer).toByteArray());
				prefs.edit().putString("lastAddr", editAddr.getText().toString()).putString("lastName", editName.getText().toString()).commit();

				startService(intent);
				startActivity(new Intent(RegistryActivity.this, ChatActivity.class));
				RegistryActivity.this.finish();
    		}
    		else if (v == findViewById(R.id.importteambutton)) {
    			showDialog(RegistryDialog.SelectImportMethod.ordinal());
    		}
    	}
    };
    
	@Override
	protected Dialog onCreateDialog(final int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (RegistryDialog.values()[id]) {
		case SelectImportMethod:
			final boolean[] options = new boolean[2];
			builder.setTitle("Import team...")
			.setSingleChoiceItems(new CharSequence[]{"From file", "From QR code"}, -1, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					options[which] = true;
					options[1 - which] = false;
				}
			})
			.setPositiveButton("Import", new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (options[0]) // From File
						showDialog(RegistryDialog.ImportTeamFromFile.ordinal());
					else if (options[1]) { // From QR Code
						AlertDialog result = IntentIntegrator.initiateScan(RegistryActivity.this);
						if (result != null)
							result.show();
					}
				}
			})
			.setNegativeButton("Cancel", null);
			break;
		case ImportTeamFromFile:
			// Set an EditText view to get user input
			final EditText input = new EditText(RegistryActivity.this);
			input.append(Environment.getExternalStorageDirectory().getPath());

			builder.setTitle("Team Import")
			.setMessage("Please type the path to your team.")
			.setView(input)
			.setPositiveButton("Import", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int whichButton) {
					String path = input.getText().toString();

					if (path != null) {
						try {
							// Copy imported file to default team location
							FileInputStream team = new FileInputStream(path);
							FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);

							byte[] buffer = new byte[1024];
							int length;
							while ((length = team.read(buffer))>0)
								saveTeam.write(buffer, 0, length);
							saveTeam.flush();
							saveTeam.close();
							team.close();
							meLoginPlayer = new FullPlayerInfo(RegistryActivity.this, prefs);
							editName.setText("");
							editName.append(meLoginPlayer.nick());
							if (!meLoginPlayer.isDefault)
								Toast.makeText(RegistryActivity.this, "Team successfully imported from " + path, Toast.LENGTH_SHORT).show();
							else {
								Toast.makeText(RegistryActivity.this, "Team from " + path + " could not be parsed successfully. Is the file a valid team?", Toast.LENGTH_LONG).show();
								deleteFile("team.xml");
							}
						} catch (IOException e) {
							System.out.println("Team not found");
							Toast.makeText(RegistryActivity.this, path + " could not be opened. Does the file exist?", Toast.LENGTH_LONG).show();
						}
					}
				}})
			.setNegativeButton("Cancel", null);
			break;
		}
		return builder.create();
	}
    
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null && "QR_CODE".equals(scanResult.getFormatName())) {
			try {
				byte[] qrRead = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
				if (qrRead == null)
					Toast.makeText(RegistryActivity.this, "Team from QR code could not be parsed successfully.", Toast.LENGTH_LONG).show();
				// Discard the first 4 bits. These set the mode of the qr data (always the same for us)
				for(int i = 0; i < qrRead.length - 1; i++)
					// The new byte is your lower 4 bits and the upper 4 bits of the next guy
					qrRead[i] = (byte) (((qrRead[i] & 0xf) << 4) | ((qrRead[i+1] & 0xf0) >>> 4));
				// Read in the length (two bytes)
				int qrLen = ((int)(qrRead[0]) << 8) | ((int)qrRead[1] & 0xff);
				InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(qrRead, 2, qrLen));
				FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = iis.read(buffer))>0)
					saveTeam.write(buffer, 0, length);
				saveTeam.flush();
				saveTeam.close();
				meLoginPlayer = new FullPlayerInfo(RegistryActivity.this, prefs);
				editName.setText("");
				editName.append(meLoginPlayer.nick());
				if (!meLoginPlayer.isDefault)
					Toast.makeText(RegistryActivity.this, "Team successfully imported from QR code", Toast.LENGTH_SHORT).show();
				else {
					Toast.makeText(RegistryActivity.this, "Team from QR code could not be parsed successfully. Is the QR code a valid team?", Toast.LENGTH_LONG).show();
					deleteFile("team.xml");
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(RegistryActivity.this, "Team from QR code could not be parsed successfully. Is the QR code a valid team?", Toast.LENGTH_LONG).show();
			}
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
    
	public void ServerListEnd() {
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.sortByPlayers();
				service.stopSelf();
			}
		});
	}

	public void NewServer(final String name, final String desc, final short players,
			final String ip, final short maxplayers, final short port) {
		runOnUiThread(new Runnable() {
			public void run() {
            	adapter.addServer(name, desc, ip, port, players, maxplayers);		
			}
		});
	}

	public void onServiceConnected(ComponentName name, IBinder binder) {
		bound = true;
		service = ((RegistryConnectionService.LocalBinder)binder).getService();
		service.setListener(this);
	}
	
	public void printToast(final String s, final int len) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(RegistryActivity.this, s, len).show();
			}
		});
	}

	public void onServiceDisconnected(ComponentName name) {
		bound = false;
		unbindService(this);
		if (service != null)
	    	service.setListener(null);
	}
    
    @Override
    public void onDestroy() {
    	if (bound)
    		unbindService(this);
    	super.onDestroy();
    }
}