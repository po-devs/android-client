package com.pokebros.android.pokemononline;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.pokebros.android.pokemononline.battle.Battle;
import com.pokebros.android.pokemononline.player.FullPlayerInfo;
import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.utilities.StringUtilities;

public class NetworkService extends Service {
	static final String TAG = "Network Service";
    final static String pkgName = "com.pokebros.android.pokemononline";

	private final IBinder binder = new LocalBinder();
	protected int NOTIFICATION = 4356;
	protected NotificationManager noteMan;
	//public Channel currentChannel = null;
	public LinkedList<Channel> joinedChannels = new LinkedList<Channel>();
	Thread sThread, rThread;
	PokeClientSocket socket = null;
	boolean findingBattle = false;
	public ChatActivity chatActivity = null;
	public BattleActivity battleActivity = null;
	public LinkedList<IncomingChallenge> challenges = new LinkedList<IncomingChallenge>();
	public boolean askedForPass = false;
	private String salt = null;
	public boolean failedConnect = false;
	public DataBaseHelper db;
	public String serverName = "Not Connected";
	public final ProtocolVersion version = new ProtocolVersion();
	public boolean serverSupportsZipCompression = false;
	@SuppressWarnings("unused")
	private byte []reconnectSecret; 
	
	public boolean hasBattle() {
		return battle != null;
	}
	
	private FullPlayerInfo meLoginPlayer;
	public PlayerInfo mePlayer;
	public Battle battle = null;// = new Battle();
	
	protected Hashtable<Integer, Channel> channels = new Hashtable<Integer, Channel>();
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	protected HashSet<Integer> pmedPlayers = new HashSet<Integer>();

	Tier superTier = new Tier();
	
	int bID = -1;
	public class LocalBinder extends Binder {
		NetworkService getService() {
			return NetworkService.this;
		}
	}
	
	/**
	 * Is the player in any of the same channels as us?
	 * @param pid the id of the player we are interested in
	 * @return true if the player shares a channel with us, false otherwise
	 */
	public boolean isOnAnyChannel(int pid) {
		for (Channel c: channels.values()) {
			if (c.players.contains(pid)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Called by a channel when a player leaves. If the player is not on any channel
	 * and there's no special circumstances (as in PM), the player will get removed
	 * @param pid The id of the player that left
	 */
	public void onPlayerLeaveChannel(int pid) {
		if (!isOnAnyChannel(pid) && !pmedPlayers.contains(pid)) {
			removePlayer(pid);
		}
	}
	
	/**
	 * Removes a player from memory
	 * @param pid The id of the player to remove
	 */
	public void removePlayer(int pid) {
		players.remove(pid);
		if (pmedPlayers.contains(pid)) {
			//TODO: close the PM?
			pmedPlayers.remove(pid);
		}
	}
	
	@Override
	// This is *NOT* called every time someone binds to us, I don't really know why
	// but onServiceConnected is correctly called in the activity sooo....
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	// This is called once
	public void onCreate() {
		db = new DataBaseHelper(NetworkService.this);
		showNotification(ChatActivity.class, "Chat");
		noteMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		// XXX TODO be more graceful
		Log.d(TAG, "NETWORK SERVICE DESTROYED; EXPECT BAD THINGS TO HAPPEN");
	}
	
	public void connect(final String ip, final int port) {
		// XXX This should probably have a timeout
		new Thread(new Runnable() {
			public void run() {
				try {
					socket = new PokeClientSocket(ip, port);
				} catch (IOException e) {
					failedConnect = true;
					if(chatActivity != null) {
						chatActivity.notifyFailedConnection();
					}
					return;
				}
				//socket.sendMessage(meLoginPlayer.serializeBytes(), Command.Login);
				Baos loginCmd = new Baos();
				loginCmd.putBaos(version); //Protocol version
				/* Network Flags: hasClientType, hasVersionNumber, hasReconnect, hasDefaultChannel, hasAdditionalChannels, hasColor, hasTrainerInfo, hasNewTeam, hasEventSpecification, hasPluginList. */
				loginCmd.putFlags(new boolean []{true,true}); //Network flags
				loginCmd.putString("android");
				short versionCode;
				try {
					versionCode = (short)getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				} catch (NameNotFoundException e1) {
					versionCode = 0;
				}
				loginCmd.putShort(versionCode);
				loginCmd.putString(meLoginPlayer.nick());
				/* Data Flags: supportsZipCompression, isLadderEnabled, wantsIdsWithMessages, isIdle */
				loginCmd.putFlags(new boolean []{false,true,true});
				socket.sendMessage(loginCmd, Command.Login);

        		while(socket.isConnected()) {
        			try {
        				// Get some data from the wire
        				socket.recvMessagePoll();
        			} catch (IOException e) {
        				// Disconnected
        				break;
        			} catch (ParseException e) {
        				// Got message that overflowed length from server.
        				// No way to recover.
        				// TODO die completely
        				break;
        			}
        			Baos tmp;
        			// Handle any messages that completed
        			while ((tmp = socket.getMsg()) != null) {
	        			Bais msg = new Bais(tmp.toByteArray());
	        			handleMsg(msg);
        			}
        			
        			/* Do not use too much CPU */
        			try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// Do nothing
					}
        		}
			}
		}).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Bundle bundle = null;
		if (intent != null) // Intent can be null if service restarts after being killed
							// XXX We probably don't handle such restarts very gracefully 
			bundle = intent.getExtras();
		if (bundle != null && bundle.containsKey("loginPlayer")) {
			meLoginPlayer = new FullPlayerInfo(new Bais(bundle.getByteArray("loginPlayer")));
			mePlayer = new PlayerInfo (meLoginPlayer);
		}
		if (bundle != null && bundle.containsKey("ip"))
			connect(bundle.getString("ip"), bundle.getShort("port"));
		return START_STICKY;
	}

    protected void showNotification(Class<?> toStart, String text, String note) {
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    	builder.setSmallIcon(R.drawable.icon)
    		.setTicker(note)
    		.setContentText(text)
    		.setWhen(System.currentTimeMillis())
    		.setContentTitle("Pokemon Online");

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, toStart), Intent.FLAG_ACTIVITY_NEW_TASK);
        
        builder.setContentIntent(notificationIntent);

        NotificationManager mNotificationManager =
        	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        	// mId allows you to update the notification later on.
    	mNotificationManager.notify(toStart.hashCode(), builder.build());
    }

    protected void showNotification(Class<?> toStart, String text) {
    	showNotification(toStart, text, text);
    }
	
	public void handleMsg(Bais msg) {
		byte i = msg.readByte();
		Command c = Command.values()[i];
		System.out.println("Received: " + c);
		switch (c) {
		case ChannelPlayers:
		case JoinChannel: 
		case LeaveChannel:
		case BattleList: {
			Channel ch = channels.get(msg.readInt());
			if(ch != null)
				ch.handleChannelMsg(c, msg);
			else
				Log.e(TAG, "Received message for nonexistent channel");
			break;
		} case VersionControl: {
			ProtocolVersion serverVersion = new ProtocolVersion(msg);

			if (serverVersion.compareTo(version) > 0) {
				Log.d(TAG, "Server has newer protocol version than we expect");
			} else if (serverVersion.compareTo(version) < 0) {
				Log.d(TAG, "PO Android uses newer protocol than Server");
			}
			
			serverSupportsZipCompression = msg.readBool();
			
			ProtocolVersion lastVersionWithoutFeatures = new ProtocolVersion(msg);
			ProtocolVersion lastVersionWithoutCompatBreak = new ProtocolVersion(msg);
			ProtocolVersion lastVersionWithoutMajorCompatBreak = new ProtocolVersion(msg);

			if (serverVersion.compareTo(version) > 0) {
				if (lastVersionWithoutFeatures.compareTo(version) > 0) {
					Toast.makeText(this, R.string.new_server_features_warning, Toast.LENGTH_SHORT).show();
				} else if (lastVersionWithoutCompatBreak.compareTo(version) > 0) {
					Toast.makeText(this, R.string.minor_compat_break_warning, Toast.LENGTH_SHORT).show();
				} else if (lastVersionWithoutMajorCompatBreak.compareTo(version) > 0) {
					Toast.makeText(this, R.string.major_compat_break_warning, Toast.LENGTH_LONG).show();
				}
			}
			String serverName = msg.readString();
			Log.d(TAG, "Server name is " + serverName);
			break;
		} case Register: {
			// Username not registered
			break;
		} case Login: {
			Bais flags = msg.readFlags();
			Boolean hasReconnPass = flags.readBool();
			if (hasReconnPass) {
				// Read byte array
				reconnectSecret = msg.readQByteArray();
			}
			mePlayer = new PlayerInfo(msg);
			int numTiers = msg.readInt();
			for (int j = 0; j < numTiers; j++) {
				// Tiers for each of our teams
				// TODO Do something with this info?
				msg.readString();
			}
			players.put(mePlayer.id, mePlayer);
			break;
		} case TierSelection: {
			msg.readInt(); // Number of tiers
			Tier prevTier = new Tier(msg.readByte(), msg.readString());
			prevTier.parentTier = superTier;
			superTier.subTiers.add(prevTier);
			while(msg.available() != 0) { // While there's another tier available
				Tier t = new Tier(msg.readByte(), msg.readString());
				if(t.level == prevTier.level) { // Sibling case
					prevTier.parentTier.addSubTier(t);
					t.parentTier = prevTier.parentTier;
				}
				else if(t.level < prevTier.level) { // Uncle case
					while(t.level < prevTier.level)
						prevTier = prevTier.parentTier;
					prevTier.parentTier.addSubTier(t);
					t.parentTier = prevTier.parentTier;
				}
				else if(t.level > prevTier.level) { // Child case
					prevTier.addSubTier(t);
					t.parentTier = prevTier;
				}
				prevTier = t;
			}
			break;
		} case ChannelsList: {
			int numChannels = msg.readInt();
			for(int j = 0; j < numChannels; j++) {
				int chanId = msg.readInt();
				Channel ch = new Channel(chanId, msg.readString(), this);
				channels.put(chanId, ch);
				//addChannel(msg.readQString(),chanId);
			}
			Log.d(TAG, channels.toString());
			break;
		} case PlayersList: {
			while (msg.available() != 0) { // While there's playerInfo's available
				PlayerInfo p = new PlayerInfo(msg);
				players.put(p.id, p);
			}
			break;
		} case SendMessage: {
			Bais netFlags = msg.readFlags();
			boolean hasChannel = netFlags.readBool();
			boolean hasId = netFlags.readBool();
			Bais dataFlags = msg.readFlags();
			boolean isHtml = dataFlags.readBool();
			Channel chan = hasChannel ? channels.get(msg.readInt()) : null;
			PlayerInfo player = hasId ? players.get(msg.readInt()) : null;
			CharSequence message = msg.readString();
			if (hasId) {
				CharSequence color = (player == null ? "orange" : player.color.toHexString());
				CharSequence name = player.nick();
				
				if (isHtml) {
					message = Html.fromHtml("<font color='" + color + "'><b>" + name + 
							": </b></font>" + message);
				} else {
					message = Html.fromHtml("<font color='" + color + "'><b>" + name +
							": </b></font>" + StringUtilities.escapeHtml((String)message));
				}
			} else {
				message = isHtml ? Html.fromHtml((String)message) : message;
			}
			if (!hasChannel) {
				// Broadcast message
				if (chatActivity != null && message.toString().contains("Wrong password for this name.")) // XXX Is this still the message sent?
					chatActivity.makeToast(message.toString(), "long");
				else {
					Iterator<Channel> it = joinedChannels.iterator();
					while (it.hasNext()) {
						it.next().writeToHist(message);
					}
				}
			} else {
				if (chan == null) {
					Log.e(TAG, "Received message for nonexistent channel");
				} else {
					chan.writeToHist(message);
				}
			}
			break;
		}
/*		case BattleList:
		case JoinChannel:
		case LeaveChannel:
		case ChannelBattle: {
//		case ChannelMessage:
//		case HtmlChannel: {
			Channel ch = channels.get(msg.readInt());
			if(ch != null)
				ch.handleChannelMsg(c, msg);
			else
				System.out.println("Received message for nonexistant channel");
			break;
//		} case ServerName: {
//			serverName = msg.readQString();
//			if (chatActivity != null)
//				chatActivity.updateTitle();
//			break;
		} case TierSelection: {
			msg.readInt(); // Number of tiers
			Tier prevTier = new Tier((byte)msg.read(), msg.readQString());
			prevTier.parentTier = superTier;
			superTier.subTiers.add(prevTier);
			while(msg.available() != 0) { // While there's another tier available
				Tier t = new Tier((byte)msg.read(), msg.readQString());
				if(t.level == prevTier.level) { // Sibling case
					prevTier.parentTier.addSubTier(t);
					t.parentTier = prevTier.parentTier;
				}
				else if(t.level < prevTier.level) { // Uncle case
					while(t.level < prevTier.level)
						prevTier = prevTier.parentTier;
					prevTier.parentTier.addSubTier(t);
					t.parentTier = prevTier.parentTier;
				}
				else if(t.level > prevTier.level) { // Child case
					prevTier.addSubTier(t);
					t.parentTier = prevTier;
				}
				prevTier = t;
			}
			break;
		} case ChallengeStuff: {
			IncomingChallenge challenge = new IncomingChallenge(msg);
			challenge.setNick(players.get(challenge.opponent));
			System.out.println("CHALLENGE STUFF: " + ChallengeEnums.ChallengeDesc.values()[challenge.desc]);
			switch(ChallengeEnums.ChallengeDesc.values()[challenge.desc]) {
			case Sent:
				if (challenge.isValidChallenge(players)) {
					challenges.addFirst(challenge);
					if (chatActivity != null && chatActivity.hasWindowFocus()) {
						chatActivity.notifyChallenge();
					} else {
						Notification note = new Notification(R.drawable.icon, "You've been challenged by " + challenge.oppName + "!", System.currentTimeMillis());
						note.setLatestEventInfo(this, "POAndroid", "You've been challenged!", PendingIntent.getActivity(this, 0,
								new Intent(NetworkService.this, ChatActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK));
						noteMan.cancel(IncomingChallenge.note);
						noteMan.notify(IncomingChallenge.note, note);
					}
				}
				break;
			case Refused:
				if(challenge.oppName != null && chatActivity != null) {
					chatActivity.makeToast(challenge.oppName + " refused your challenge", "short");
				}
				break;
			case Busy:
				if(challenge.oppName != null && chatActivity != null) {
					chatActivity.makeToast(challenge.oppName + " is busy", "short");
				}
				break;
			case InvalidTeam:
				if (chatActivity != null)
					chatActivity.makeToast("Challenge failed due to invalid team", "long");
				break;
			case InvalidGen:
				if (chatActivity != null)
					chatActivity.makeToast("Challenge failed due to invalid gen", "long");
				break;
			}
			break;
		} case ChannelsList: {
			int numChannels = msg.readInt();
			for(int k = 0; k < numChannels; k++) {
				int chanId = msg.readInt();
				Channel ch = new Channel(chanId, msg.readQString(), this);
				channels.put(chanId, ch);
				//addChannel(msg.readQString(),chanId);
			}
			System.out.println(channels.toString());
			break;
		} case ChannelPlayers: {
			Channel ch = channels.get(msg.readInt());
			int numPlayers = msg.readInt();
			if(ch != null) {
				for(int k = 0; k < numPlayers; k++) {
					int id = msg.readInt();
					ch.addPlayer(players.get(id));
				}
			}
			else
				System.out.println("Received message for nonexistant channel");
			break;
//		} case HtmlMessage: {
//			String htmlMessage = msg.readQString();
//			System.out.println("Html Message: " + htmlMessage);
//			break;
		}*/ case Logout: {
			// Only sent when player is in a PM with you and logs out
			int playerID = msg.readInt();
			removePlayer(playerID);
			//System.out.println("Player " + playerID + " logged out.");
			break;
		} /*case BattleFinished: {
			int battleID = msg.readInt();
			byte battleDesc = msg.readByte();
			int id1 = msg.readInt();
			int id2 = msg.readInt();
			System.out.println("bID " + battleID + " battleDesc " + battleDesc + " id1 " + id1 + " id2 " + id2);
			String[] outcome = new String[]{" won by forfeit against ", " won against ", " tied with "};
			if (battle != null && battle.bID == battleID) {
				if (mePlayer.id == id1 && battleDesc < 2) {
					showNotification(ChatActivity.class, "Chat", "You won!");
				} else if (mePlayer.id == id2 && battleDesc < 2) {
					showNotification(ChatActivity.class, "Chat", "You lost!");
				} else if (battleDesc == 2) {
					showNotification(ChatActivity.class, "Chat", "You tied!");
				}
				
				if (players.get(id1) != null && players.get(id2) != null && battleDesc < 2)
					joinedChannels.peek().writeToHist(Html.fromHtml("<b><i>" + escapeHtml(players.get(id1).nick()) + outcome[battleDesc] + escapeHtml(players.get(id2).nick()) + ".</b></i>"));
				
				if (battleDesc == 0 || battleDesc == 3) {
					battle = null;
					if (battleActivity != null)
						battleActivity.end();
				}
			}
			break;
		}*/ case SendPM: {
			int playerID = msg.readInt();
			pmedPlayers.add(playerID);
			// Ignore the message
			String pm = new String("This user is running the Pokemon Online Android client and cannot respond to private messages.");
			Baos bb = new Baos();
			bb.putInt(playerID);
			bb.putString(pm);
			socket.sendMessage(bb, Command.SendPM);
			break;
		}/* case SendTeam: {
			PlayerInfo p = new PlayerInfo(msg);
			if (players.containsKey(p.id)) {
				PlayerInfo player = players.get(p.id);
				player.update(p);
				Enumeration<Channel> e = channels.elements();
				while (e.hasMoreElements()) {
					Channel ch = e.nextElement();
					if (ch.players.containsKey(player.id)) {
						ch.updatePlayer(player);
					}
				}
			}
			break;

		} case BattleMessage: {
			msg.readInt(); // currently support only one battle, unneeded
			msg.readInt(); // discard the size, unneeded
			if (battle != null)
				battle.receiveCommand(msg);
			break;
		} case EngageBattle: {
			bID = msg.readInt();
			int pID1 = msg.readInt();
			int pID2 = msg.readInt();
			if(pID1 == 0) { // This is us!
				BattleConf conf = new BattleConf(msg);
				// Start the battle
				battle = new Battle(conf, msg, players.get(conf.id(0)),
					players.get(conf.id(1)), mePlayer.id, bID, this);
				joinedChannels.peek().writeToHist("Battle between " + mePlayer.nick() + 
					" and " + players.get(pID2).nick() + " started!");
				Intent in;
				in = new Intent(this, BattleActivity.class);
				in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(in);
				findingBattle = false;
			}
			break;
		} case Login: {
			mePlayer = new PlayerInfo(msg);
			players.put(mePlayer.id, mePlayer);
			break;
		} case AskForPass: {
			salt = msg.readQString();
			// XXX not sure what the second half is supposed to check
			// from analyze.cpp : 265 of PO's code
			if (salt.length() < 6) { //  || strlen((" " + salt).toUtf8().data()) < 7)
				System.out.println("Protocol Error: The server requires insecure authentication");
				break;
			}
			askedForPass = true;
			if (chatActivity != null && (chatActivity.hasWindowFocus() || chatActivity.progressDialog.isShowing())) {
				chatActivity.notifyAskForPass();
			}
			break;
		} case AddChannel: {
			addChannel(msg.readQString(),msg.readInt());
			break;
		} case RemoveChannel: {
			int chanId = msg.readInt();
			if (chatActivity != null)
				chatActivity.removeChannel(channels.get(chanId));
			channels.remove(chanId);
			break;
		} case ChanNameChange: {
			int chanId = msg.readInt();
			if (chatActivity != null)
				chatActivity.removeChannel(channels.get(chanId));
			channels.remove(chanId);
			channels.put(chanId, new Channel(chanId, msg.readQString(), this));
			break;
		} case SendMessage: {
			String message = msg.readQString();
			System.out.println(message);
			if (chatActivity != null && message.contains("Wrong password for this name."))
				chatActivity.makeToast(message, "long");
			else if (chatActivity != null && joinedChannels.peek() != null)
				joinedChannels.peek().writeToHist(message);
			break;
		} */default: {
			System.out.println("Unimplented message");
		}
		}
		if (battle != null && battleActivity != null && battle.histDelta.length() != 0)
			battleActivity.updateBattleInfo(false);
		if (chatActivity != null && joinedChannels.peek() != null)
			chatActivity.updateChat();
	}

	public void sendPass(String s) {
		askedForPass = false;
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			Baos hashPass = new Baos();
			hashPass.putString(toHex(md5.digest(mashBytes(toHex(md5.digest(s.getBytes("ISO-8859-1"))).getBytes("ISO-8859-1"), salt.getBytes("ISO-8859-1")))));
			socket.sendMessage(hashPass, Command.AskForPass);
		} catch (NoSuchAlgorithmException nsae) {
			System.out.println("Attempting authentication threw an exception: " + nsae);
		} catch (UnsupportedEncodingException uee) {
			System.out.println("Attempting authentication threw an exception: " + uee);
		}
	}
	
	private byte[] mashBytes(final byte[] a, final byte[] b) {
		byte[] ret = new byte[a.length + b.length];
		System.arraycopy(a, 0, ret, 0, a.length);
		System.arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}
	
	private String toHex(byte[] b) {
		String ret = new BigInteger(1, b).toString(16);
		while (ret.length() < 32)
			ret = "0" + ret;
		return ret;
	}
	
	protected void herp() {
		System.out.println("HERP");
	}
	
	protected void addChannel(String chanName, int chanId) {
		Channel c = new Channel(chanId, chanName, this);
		channels.put(chanId, c);
		if(chatActivity != null)
			chatActivity.addChannel(c);
	}
	
	public void playCry(ShallowBattlePoke poke) {
		new Thread(new CryPlayer(poke)).start();
	}
	
	class CryPlayer implements Runnable {
		ShallowBattlePoke poke;
		
		public CryPlayer(ShallowBattlePoke poke) {
			this.poke = poke;
		}
		
		public void run() {
			int resID = getResources().getIdentifier("p" + poke.uID.pokeNum,
					"raw", pkgName);
			if (resID != 0) {
				MediaPlayer cryPlayer = MediaPlayer.create(NetworkService.this, resID);
				cryPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						synchronized (mp) {
							mp.notify();
						}
					}
				});
				synchronized (cryPlayer) {
					cryPlayer.start();
					try {
						cryPlayer.wait(10000);
					} catch (InterruptedException e) {}
				}
				cryPlayer.release();
				synchronized (battle) {
					battle.notify();
				}
				cryPlayer = null;
			}
		}
	}
	
    public void disconnect() {
    	if (socket != null && socket.isConnected()) {
    		socket.close();
    	}
    	this.stopForeground(true);
    	this.stopSelf();
    }
    
    public PlayerInfo getPlayerByName(String playerName) {
    	Enumeration<Integer> e = players.keys();
    	while(e.hasMoreElements()) {
    		PlayerInfo info = players.get(e.nextElement());
    		if (info.nick().equals(playerName))
    			return info;
    	}
    	return null;
    }
}
