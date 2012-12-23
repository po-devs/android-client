package com.pokebros.android.pokemononline;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RegistryConnectionService extends Service {
	
	public interface RegistryCommandListener {
	
		public abstract void ServerListEnd();
		public abstract void RegistryConnectionClosed();

		/*
		 * Called when Registry sends us a new Server
		 */
		public abstract void NewServer(String name, String desc,
				short players, String ip, short maxplayers, short port);
	}
	
	private final ConcurrentHashMap<Intent, LocalBinder> binders = new ConcurrentHashMap<Intent, RegistryConnectionService.LocalBinder>();
	
	static final String TAG = "Pokemon Online Registry";

	public class LocalBinder extends Binder {
		public LocalBinder() {
		}
		
		synchronized void connect(RegistryCommandListener listener) {
			mListener = listener;
			RegistryConnectionService.this.connect(this);
		}
		
		synchronized void disconnect() {
			mListener = null;
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
	}
	
	private void connect(final LocalBinder binder) {
		// XXX This should probably have a timeout
		new Thread(new Runnable() {
			public void run() {
				try {
					int count = 0;
					Log.v(TAG, "Starting a registry request...");
					PokeClientSocket socket = new PokeClientSocket("registry.pokemon-online.eu", 5090);
					while(true) {
						try {
							socket.recvMessagePoll();
						} catch (IOException e) {
							// disconnected
							break;
	        			} catch (ParseException e) {
	        				// Got message that overflowed length from server.
	        				// No way to recover.
	        				// TODO die completely
	        				break;
	        			}
						Baos tmp;
						while((tmp = socket.getMsg()) != null) {
							Log.v(TAG, "reading message " + (++count));
							handleMsg(socket, new Bais(tmp.toByteArray()), binder.listener());
						}
						
						/* If we got disconnected, no point in continuing */
						if (binder.listener() == null) {
							socket.close();
							break;
						}

						// don't use all CPU
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// no action
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
			short port = msg.readShort();

			if (listener != null) {
				listener.NewServer(name, desc, players, ip, maxplayers, port);
			}
			break;
		}
		case ServerListEnd:
			if (listener != null) {
				listener.ServerListEnd();
			}
			socket.close(); // server stops sending any meaningless data 
			break;
		default:
			Log.w(TAG, "Unknown message");
		}
	}
}
