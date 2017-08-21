package com.podevs.android.poAndroid.registry;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.PokeClientSocket;
import com.podevs.android.poAndroid.pokeinfo.InfoConfig;
import com.podevs.android.utilities.Bais;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryConnectionService extends Service {
	public interface RegistryCommandListener {
	
		void ServerListEnd();
		void RegistryConnectionClosed();

		/*
		 * Called when Registry sends us a new Server
		 */
		void NewServer(String name, String desc, short players, String ip, short maxplayers, int port);
	}
	
	private final ConcurrentHashMap<Intent, LocalBinder> binders = new ConcurrentHashMap<Intent, RegistryConnectionService.LocalBinder>();
	
	static final String TAG = "Pokemon Online Registry";

	public class LocalBinder extends Binder {
		/* Since the socket is blocking, we need to be able to interrupt it
		 * when no longer needing the registry
		 */
		PokeClientSocket socket = null;
		
		public LocalBinder() {
		}
		
		synchronized void connect(RegistryCommandListener listener) {
			mListener = listener;
			RegistryConnectionService.this.connect(this);
		}
		
		synchronized void disconnect() {
			mListener = null;
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}
		
		synchronized RegistryCommandListener listener() {
			return mListener;
		}
		
		private RegistryCommandListener mListener;
	}
	
	@Override
	// This is called every time someone binds to us
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "binding service");
		binders.put(intent, new LocalBinder());
		return binders.get(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "unbinding service");
		LocalBinder binder = binders.get(intent);
		if (binder != null) {
			binders.remove(intent);
			binder.disconnect();
		}
		return false;
	}
	
	@Override
	// This is called once
	public void onCreate() {
		super.onCreate();
		
		InfoConfig.setContext(this);
	}
	
	private void connect(final LocalBinder binder) {
		// XXX This should probably have a timeout
		new Thread(new Runnable() {
			public void run() {
				try {
					Log.v(TAG, "Starting a registry request...");
					PokeClientSocket socket = new PokeClientSocket("registry.pokemon-online.eu", 5090);
					binder.socket = socket;
					while(true) {
						try {
						    handleMsg(socket, socket.getMsg(), binder.listener());
						} catch (IOException e) {
							// disconnected
							break;
	        			} catch (ParseException e) {
	        				// Got message that overflowed length from server.
	        				// No way to recover.
	        				// TODO die completely
	        				break;
	        			}
					}
				}
				catch (IOException e) {
					Log.e(TAG, "Registry connection failed");
				} catch (UnresolvedAddressException e) {
					Log.e(TAG, "Unable to resolve address for registry");
				}
				RegistryCommandListener listener = binder.listener();
				if (listener != null) {
					listener.RegistryConnectionClosed();
				}
				Log.v(TAG, "Registry connection finished");
			}}).start();
	}

	public void handleMsg(PokeClientSocket socket, Bais msg, RegistryCommandListener listener) {
		/* Completely obvious way to "convert"
		 * a byte into a value in an enum.
		 */
		int i = msg.read();
		Command c = Command.values()[i];	
		
		switch(c) {
		case PlayersList: {
			String name = msg.readString();
			String desc =  msg.readString();
			short players =  msg.readShort();
			String ip =  msg.readString();
			short maxplayers = msg.readShort();
			int port = (msg.readShort() + 65536) % 65536;

			if (listener != null) {
				listener.NewServer(name, desc, players, ip, maxplayers, port);
			}
			break;
		}
		case ServerListEnd: {
			if (listener != null) {
				listener.ServerListEnd();
			}
			socket.close(); // server stops sending any meaningless data 
			break;
		} case Announcement: {
			// TODO deal with announcement
			break;
		} default:
			Log.w(TAG, "Unknown message");
		}
	}
}
