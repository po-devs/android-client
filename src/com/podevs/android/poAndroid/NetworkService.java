package com.podevs.android.poAndroid;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;
import com.podevs.android.poAndroid.battle.*;
import com.podevs.android.poAndroid.chat.Channel;
import com.podevs.android.poAndroid.chat.ChatActivity;
import com.podevs.android.poAndroid.chat.ControlPanelGroup;
import com.podevs.android.poAndroid.player.FullPlayerInfo;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.poAndroid.player.UserInfo;
import com.podevs.android.poAndroid.pms.PrivateMessageActivity;
import com.podevs.android.poAndroid.pms.PrivateMessageList;
import com.podevs.android.poAndroid.poke.PokeParser;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.Team;
import com.podevs.android.poAndroid.pokeinfo.InfoConfig;
import com.podevs.android.utilities.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkService extends Service {
	static final String TAG = "Network Service";
	final static String pkgName = "com.podevs.android.poAndroid";
	final static String defaultKey = "default chan for ";

	private final IBinder binder = new LocalBinder();
	//public Channel currentChannel = null;
	public LinkedList<Channel> joinedChannels = new LinkedList<Channel>();
	// Thread sThread, rThread;
	volatile public PokeClientSocket socket = null;
	public boolean findingBattle = false;
	public boolean registered = false;
	public ChatActivity chatActivity = null;
	public LinkedList<IncomingChallenge> challenges = new LinkedList<IncomingChallenge>();
	public boolean askedForPass = false;
	public boolean askedForServerPass = false;
	private String salt = null;
	private byte[] salty = null;
	public boolean failedConnect = false;
	private boolean reconnectDenied = false;
	public String serverName = "Not Connected";
	private volatile boolean halted = false;
	public final ProtocolVersion version = new ProtocolVersion();
	public ProtocolVersion serverVersion = version;
	public boolean serverSupportsZipCompression = false;
	private byte reconnectSecret[] = null;
	public ArrayList<Integer> ignoreList= new ArrayList<Integer>();
	private ImageParser imageParser;
	private BattleInlineHandler tagHandler;
	private static Pattern hashTagPattern;
	public static final Pattern urlPattern = Pattern.compile("(https?:\\/\\/[-\\w\\.]+)+(:\\d+)?(\\/([\\S\\/_\\.]*(\\?\\S+)?)?)?");
	private Matcher hashTagMatcher;

	public static class chatPrefs {
		// Chat
		boolean flashing = true;
		public boolean timeStamp = false;
		String color = "#FFFF00";
		boolean notificationsFlash = false;
		// PM
		boolean timeStampPM = true;
		public boolean notificationsPM = true;
		boolean cry = false;
		int pokeNumber = 648;
		long lastCall = 0;
		int soundVolume = 10;
	}

	private static chatPrefs chatSettings = new chatPrefs();

	/**
	 * Are we engaged in a battle?
	 * @return True if we are at least in one battle
	 */
	public boolean isBattling() {
		return !activeBattles.isEmpty();
	}
	
	/**
	 * Are we engaged in a battle with that particular battle ID?
	 * @param battleId the battle ID
	 * @return true if we are a player of the battle with the battle ID
	 */
	public boolean isBattling(int battleId) {
		return activeBattles.containsKey(battleId);
	}
	
	private Battle activeBattle(int battleId) {
		return activeBattles.get(battleId);
	}

	public FullPlayerInfo meLoginPlayer;
	public Hashtable<Integer, Battle> activeBattles = new Hashtable<Integer, Battle>();
	public Hashtable<Integer, SpectatingBattle> spectatedBattles = new Hashtable<Integer, SpectatingBattle>();

	public Tier superTier = new Tier();
	public int myid = -1;
	public PlayerInfo me = new PlayerInfo();
	
	public Hashtable<Integer, Channel> channels = new Hashtable<Integer, Channel>();
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	public Hashtable<Integer, BattleDesc> battles = new Hashtable<Integer, BattleDesc>();
	static public HashSet<Integer> pmedPlayers = new HashSet<Integer>();
	public PrivateMessageList pms = new PrivateMessageList(me);

	public class LocalBinder extends Binder {
		public NetworkService getService() {
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
			if (c.players.containsKey(pid)) {
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

	public void addBattle(int battleid, BattleDesc desc, boolean show) {
		battles.put(battleid, desc);
// battle event
		// String n1 = "";
		if (players.containsKey(desc.p1)) {
			players.get(desc.p1).addBattle(battleid);
		//	n1 = players.get(desc.p1).nick();
		}
		// String n2 = "";
		if (players.containsKey(desc.p2)) {
			players.get(desc.p2).addBattle(battleid);
			// n2 = players.get(desc.p2).nick();
		}
		/*
		if (show) {
			for (Channel chan : channels.values()) {
				if (chan.joined && chan.channelEvents) {
					if (chan.players.contains(players.get(desc.p1)) || chan.players.contains(players.get(desc.p2))) {
						// needs better method.
						// Maybe create an array of channels to check?
						CharSequence message = Html.fromHtml("<i><font color=\"#A0A0A0\">Battle started between " + n1 + " and " + n2 + ".</font></i>");
						chan.writeToHistSmall(message);
					}
				}
			}
		}
		*/
	}

	/**
	 * Removes a battle from memory
	 * @param battleID the battle id of the battle to remove
	 */
	private void removeBattle(int battleID) {
		if (!battles.containsKey(battleID)) {
			return;
		}
		BattleDesc battle = battles.get(battleID);
		if (hasPlayer(battle.p1)) {
			players.get(battle.p1).removeBattle(battleID);
		}
		if (hasPlayer(battle.p2)) {
			players.get(battle.p2).removeBattle(battleID);
		}
	}
	
	/**
	 * Returns a list of all the battles fought or spectated
	 * @return the battles fought/spectated
	 */
	public Collection<SpectatingBattle> getBattles() {
		LinkedList<SpectatingBattle> ret = new LinkedList<SpectatingBattle>();
		ret.addAll(activeBattles.values());
		ret.addAll(spectatedBattles.values());
		
		return ret;
	}
	
	/**
	 * Checks all battles spectated or fought and removes/destroys the ones
	 * that are finished
	 */
	public void checkBattlesToEnd() {
		 for (SpectatingBattle battle: getBattles()) {
			 if (battle.gotEnd) {
				 closeBattle(battle.bID);
			 }
		 }
	}

	/**
	 * Removes a battle spectated/fought from memory and destroys it
	 * @param bID The id of the battle to remove
	 */
	public void closeBattle(int bID) {
		if (isBattling(bID)) {
			activeBattles.remove(bID).destroy();
		}
		if (spectatedBattles.containsKey(bID)) {
			spectatedBattles.remove(bID).destroy();
		}
		/* Remove the battle notification */
		NotificationManager mNotificationManager = getNotificationManager();
    	mNotificationManager.cancel("battle", bID);
	}

	/**
	 * Does the player exist in memory
	 * @param pid the id of the player we're interested in
	 * @return true if the player is in memory, or false
	 */
	public boolean hasPlayer(int pid) {
		return players.containsKey(pid);
	}
	
	public boolean hasChannel(int cid) {
		return channels.containsKey(cid);
	}

	/**
	 * Checks if the players of the battle are online, and remove the battle from memory if not
	 * @param battleid the id of the battle to check
	 */
	private void testRemoveBattle(Integer battleid) {
		BattleDesc battle = battles.get(battleid);

		if (battle != null) {
			if (!players.containsKey(battle.p1) && !players.containsKey(battle.p2)) {
				battles.remove(battle);
			}
		}
	}

	/**
	 * Gets the name of a player or "???" if the player couldn't be found
	 * @param playerId id of the player we're interested in
	 * @return name of the player or "???" if not found
	 */
	public String playerName(int playerId) {
		PlayerInfo player = players.get(playerId);

		if (player == null) {
			return "???";
		} else {
			return player.nick();
		}
	}
	
	public int playerAuth(int playerId) {
		PlayerInfo player = players.get(playerId);

		if (player == null) {
			return 0;
		} else {
			return player.auth;
		}
	}

	/**
	 * Removes a player from memory
	 * @param pid The id of the player to remove
	 */
	public void removePlayer(int pid) {
		PlayerInfo player = players.remove(pid);
		if (pmedPlayers.contains(pid)) {
			//TODO: close the PM?
			pmedPlayers.remove(pid);
		}

		if (player != null) {
			for(Integer battleid: player.battles) {
				testRemoveBattle(battleid);
			}
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
		super.onCreate();
		
		/* The context needed for the database */
		InfoConfig.setContext(this);
		
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Chat - Pokemon Online")
                .setContentText("Pokemon Online is running")
                .setOngoing(true);
        
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ChatActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_NEW_TASK);

        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        
		startForeground(R.id.networkService, mBuilder.build());

		loadPOPreferences(getBaseContext());
		loadSettings();
		hashTagPattern = Pattern.compile("(#\\S*\\s??)");
		imageParser = new ImageParser(this);
		tagHandler = new BattleInlineHandler(this);
	}

	public static SharedPreferences POPreferences = null;

	public static void loadSettings() {
		chatSettings.color = POPreferences.getString("flashColor", "#FFFF00");
		chatSettings.flashing = POPreferences.getBoolean("flashing", true);
		chatSettings.timeStamp = POPreferences.getBoolean("timeStamp", false);
		chatSettings.timeStampPM = POPreferences.getBoolean("timeStampPM", true);
		chatSettings.notificationsPM = POPreferences.getBoolean("notificationsPM", true);
		chatSettings.notificationsFlash = POPreferences.getBoolean("notificationsFlash", false);
		chatSettings.cry = POPreferences.getBoolean("crySound", false);
		chatSettings.pokeNumber = Integer.parseInt(POPreferences.getString("pokemonNumber", "648"));
		chatSettings.soundVolume = Integer.parseInt(POPreferences.getString("soundVolume", "10"));
		MessageListAdapter.copyandpaste = POPreferences.getBoolean("copyandpaste",false);
	}

	private static void loadPOPreferences(Context context) {
		POPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		MessageListAdapter.copyandpaste = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("copyandpaste", false);
	}

	public static boolean getPMSettings() {
		return chatSettings.timeStampPM;
	}

	@Override
	public void onDestroy() {
		// XXX TODO be more graceful
		Log.d(TAG, "NETWORK SERVICE DESTROYED; EXPECT BAD THINGS TO HAPPEN");
		
		for(SpectatingBattle battle : getBattles()) {
			closeBattle(battle.bID);
		}
		
		stopForeground(true);
		halted = true;
	}
	
	private String ip;
	private int port;

	public void connect(String ip, int port) {
		this.ip = ip;
		this.port = port;

        final String cookie = FileContent.getFileContent(this, "cookie-" + Security.md5(ip));
		// XXX This should probably have a timeout
		new Thread(new Runnable() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			public void run() {
				try {
					socket = new PokeClientSocket(NetworkService.this.ip, NetworkService.this.port);
				} catch (IOException e) {
					failedConnect = true;
					if(chatActivity != null) {
						chatActivity.notifyFailedConnection();
					}
					return;
				}

				Baos loginCmd = new Baos();
				loginCmd.putBaos(version); //Protocol version
				
				String defaultChannel = null;
				Set<String> autoJoinChannels = null;

				SharedPreferences prefs = getSharedPreferences("autoJoinChannels", MODE_PRIVATE);
				String key = NetworkService.this.ip + ":" + NetworkService.this.port;
				
				if (prefs.contains(key)) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						autoJoinChannels = prefs.getStringSet(key, null);
					}
				}
				defaultChannel = prefs.getString(defaultKey+key, null);
				
				/* Network Flags: hasClientType, hasVersionNumber, hasReconnect, hasDefaultChannel, hasAdditionalChannels, 
				 * hasColor, hasTrainerInfo, hasNewTeam, hasEventSpecification, hasPluginList. */                //hasCookie hasID
				loginCmd.putFlags(new boolean []{true, true, true, defaultChannel != null, autoJoinChannels != null,
						meLoginPlayer.color().isValid(), true,	meLoginPlayer.team.isValid(), false, false, cookie.length() > 0, true}); //Network flags
				loginCmd.putString("android");
				short versionCode;
				try {
					versionCode = (short)getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				} catch (NameNotFoundException e1) {
					versionCode = 404;
				}
				loginCmd.putShort(versionCode);
				loginCmd.putString(meLoginPlayer.nick());
				/* Data Flags: supportsZipCompression, isLadderEnabled, wantsIdsWithMessages, isIdle */
				loginCmd.putFlags(new boolean []{false, true, true, getSharedPreferences("clientOptions", MODE_PRIVATE).getBoolean("idle", false)});
				
				/* Reconnect even if all the bits are different */
				loginCmd.write(0);
				
				if (defaultChannel != null) {
					loginCmd.putString(defaultChannel);
				}
				if (autoJoinChannels != null) {
					Object channels [] = autoJoinChannels.toArray();
					loginCmd.putInt(channels.length);
					for (Object chan : channels) {
						loginCmd.putString(chan.toString());
					}
				}
				
				if (meLoginPlayer.color().isValid()) {
					loginCmd.putBaos(meLoginPlayer.color());
				}
				
				loginCmd.putBaos(meLoginPlayer.profile.trainerInfo);
				
				if (meLoginPlayer.team.isValid()) {
					loginCmd.write(1); // number of teams
					loginCmd.putBaos(meLoginPlayer.team);
				}

                if (cookie.length() > 0) {
                    loginCmd.putString(cookie);
                }

				loginCmd.putBool(getUniqueIDFlag());

				loginCmd.putString(getUniqueID());

				socket.sendMessage(loginCmd, Command.Login);
				
				do {
					readSocketMessages(socket);
					
					if (halted) {
						return;
					}
					
					writeMessage("(" + StringUtilities.timeStamp() + ") Disconnected from server");
										
					reconnect();
				} while (!reconnectDenied && !halted);
			}
		}).start();
	}
	public void changeConnect(String newNick) {
		registered = false;
		Baos b = new Baos();
		b.putFlags(new boolean[] {true});
		b.putString(newNick);
		socket.sendMessage(b, Command.SendTeam);
	}

	public void changeTeam(String path) {
		Team team = new PokeParser(NetworkService.this, path, true).getTeam();
		Baos b = new Baos();
		b.putFlags(new boolean[] {true, meLoginPlayer.color().isValid(), true, true});
		b.putString(meLoginPlayer.nick());
		if (meLoginPlayer.color().isValid()) {
			b.putBaos(meLoginPlayer.color());
		}

		b.putBaos(meLoginPlayer.profile.trainerInfo);

		b.write(0);
		b.write(1);
		b.putBaos(team);

		socket.sendMessage(b, Command.SendTeam);
	}

	private void reconnect() {
		/* Impossible to reconnect with -1 id */
		if (myid == -1 || reconnectSecret == null) {
			reconnectDenied = true;
			return;
		}
		while (!halted) {
			try {
				socket = new PokeClientSocket(NetworkService.this.ip, NetworkService.this.port);
			} catch (IOException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				continue;
			}
			
			Baos msgToSend = new Baos();
			msgToSend.putInt(me.id);
			msgToSend.putBytes(reconnectSecret);
			
			socket.sendMessage(msgToSend, Command.Reconnect);
			
			return;
		}
	}
	
	private void readSocketMessages(PokeClientSocket socket) {
		while(!halted && socket.isConnected()) {
			try {
				handleMsg(socket.getMsg());
			} catch (IOException e) {
				// Disconnected
				break;
			} catch (ParseException e) {
				// Got message that overflowed length from server.
				// No way to recover.
				// TODO die completely
				break;
			}
		}
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
			me.setTo(new PlayerInfo (meLoginPlayer));
		}
		if (bundle != null && bundle.containsKey("ip") && bundle.containsKey("port"))
			connect(bundle.getString("ip"), bundle.getInt("port"));
				
		return START_STICKY;
	}

	public void handleMsg(Bais msg) {
		byte i = msg.readByte();
		
		if (i < 0 || i >= Command.values().length) {
			Log.w(TAG, "Command out of bounds: "  +i);
		}
		
		Command c = Command.values()[i];
		//Log.d(TAG, "Received: " + c);
		switch (c) {
		case ChannelPlayers:
		case JoinChannel: 
		case LeaveChannel:{
			Channel ch = channels.get(msg.readInt());
			if(ch != null) {
				ch.handleChannelMsg(c, msg);
			} else {
				Log.e(TAG, "Received message for nonexistent channel");
			}
			break;
		} case VersionControl: {
			serverVersion = new ProtocolVersion(msg);

			/* Toast messages there trigger an exception:
			 * 
			 * java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
				at android.os.Handler.<init>(Handler.java:121)
				at android.widget.Toast.<init>(Toast.java:73)
				at android.widget.Toast.makeText(Toast.java:249)
				at com.podevs.android.poAndroid.NetworkService.handleMsg(NetworkService.java:523)
				at com.podevs.android.poAndroid.NetworkService.readSocketMessages(NetworkService.java:468)
				at com.podevs.android.poAndroid.NetworkService.access$2(NetworkService.java:450)
				at com.podevs.android.poAndroid.NetworkService$1.run(NetworkService.java:408)
				at java.lang.Thread.run(Thread.java:1019)
			 */
			if (serverVersion.compareTo(version) > 0) {
				Log.d(TAG, "Server has newer protocol version than we expect");
			} else if (serverVersion.compareTo(version) < 0) {
				//Toast.makeText(this, "PO Android uses newer network protocol than Server", Toast.LENGTH_LONG);
			}

			serverSupportsZipCompression = msg.readBool();

			ProtocolVersion lastVersionWithoutFeatures = new ProtocolVersion(msg);
			ProtocolVersion lastVersionWithoutCompatBreak = new ProtocolVersion(msg);
			ProtocolVersion lastVersionWithoutMajorCompatBreak = new ProtocolVersion(msg);

			if (serverVersion.compareTo(version) > 0) {
				if (lastVersionWithoutFeatures.compareTo(version) > 0) {
					//Toast.makeText(this, R.string.new_server_feaantures_warning, Toast.LENGTH_SHORT).show();
				} else if (lastVersionWithoutCompatBreak.compareTo(version) > 0) {
					//Toast.makeText(this, R.string.minor_compat_break_warning, Toast.LENGTH_SHORT).show();
				} else if (lastVersionWithoutMajorCompatBreak.compareTo(version) > 0) {
					//Toast.makeText(this, R.string.major_compat_break_warning, Toast.LENGTH_LONG).show();
				}
			}
			serverName = msg.readString();
			if (chatActivity != null) {
				chatActivity.updateTitle();
			}
			break;
		} case Cookie: {
            Bais network = msg.readFlags();
            if (network.readBool()) {
                String content = msg.readString();
                if (content.length() > 4000) {
                    Log.w(TAG, "Cookie too long, not saving");
                } else {
                    FileContent.setFileContent(this, "cookie-" + Security.md5(ip), content);
                }
            } else {
                FileContent.removeFile(this, "cookie-" + Security.md5(ip));
            }
        } case KeepAlive: {
			socket.sendMessage(null, Command.KeepAlive);
			break;
		} case Register: {
			// Username not registered
			break;
		} case Login: {
			reconnectDenied = false;

			Bais flags = msg.readFlags();
			Boolean hasReconnPass = flags.readBool();
			if (hasReconnPass) {
				reconnectSecret = msg.readQByteArray();
			} else {
				reconnectSecret = null;
			}
			
			me.setTo(new PlayerInfo(msg));
			myid = me.id;
			int numTiers = msg.readInt();
			for (int j = 0; j < numTiers; j++) {
				// Tiers for each of our teams
				// TODO Do something with this info?
				msg.readString();
			}
			players.put(myid, me);
			break;
		} case TierSelection: {
			superTier.reset();

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
			
			superTier.save(this);
			break;
		} case ChannelsList: {
			int numChannels = msg.readInt();

			for(int j = 0; j < numChannels; j++) {
				int chanId = msg.readInt();
				
				if (hasChannel(chanId)) {
					Channel ch = channels.get(chanId);
					ch.name = msg.readString();
				} else {
					Channel ch = new Channel(chanId, msg.readString(), this);
					channels.put(chanId, ch);
				}

				//addChannel(msg.readQString(),chanId);
			}
			Log.d(TAG, channels.toString());
			break;
		} case PlayersList: {
			while (msg.available() != 0) { // While there's playerInfo's available
				PlayerInfo p = new PlayerInfo(msg);
				PlayerInfo oldPlayer = players.get(p.id);
				players.put(p.id, p);

				if (oldPlayer != null) {
					if (!p.nick().equals(oldPlayer.nick())) {
						for (Channel chan : channels.values()) {
							if (chan.joined && chan.channelEvents) {
								if (chan.players.contains(p) || chan.players.contains(oldPlayer)) {
									CharSequence message = Html.fromHtml("<i><font color=\"#A0A0A0\">" + oldPlayer.nick() + " changed names to " + p.nick() + "</font></i>");
									chan.writeToHistSmall(message);
								}
							}
						}
					}
					p.battles = oldPlayer.battles;
					// name change event
					if (chatActivity != null) {

						/* Updates the player in the adapter memory */
						chatActivity.updatePlayer(p, oldPlayer);
					}
				}
				/* Updates self player */
				if (p.id == myid) {
					me.setTo(p);
				}
			}
			break;
		} case OptionsChanged: {
			int id = msg.readInt();
			Bais dataFlags = msg.readFlags();
			
			PlayerInfo p = players.get(id);
			if (p != null) {
                PlayerInfo p2 = p;
				p2.hasLadderEnabled = dataFlags.readBool();
				p2.isAway = dataFlags.readBool();

				if (p2.id == myid) {
					me.setTo(p2);
				}
                chatActivity.updatePlayer(p, p2);
			}
			break;
		}	case GetUserInfo: {
				UserInfo info = new UserInfo(msg);
				chatActivity.updateControlPanel(info);
				break;
			}
			case GetUserAlias: {
				chatActivity.updateControlPanel(msg.readString());
				break;
			}
			case ShowRankings: {
				boolean starting = msg.readBool();
				if (starting) {
					int startingPage = msg.readInt();
					int staringRank = msg.readInt();
					int total = msg.readInt();
					// Add Ranking window
				} else {
					String name = msg.readString();
					int points = msg.readInt();
					// Add ranking
				}
				break;
			}
			case SendMessage: {
			Bais netFlags = msg.readFlags();
			boolean hasChannel = netFlags.readBool();
			boolean hasId = netFlags.readBool();
			Bais dataFlags = msg.readFlags();
			boolean isHtml = dataFlags.readBool();
			
			Channel chan = null;
			PlayerInfo player = null;
			int pId = 0;
			if (hasChannel) {
				chan = channels.get(msg.readInt());
				tagHandler.currentChannel = chan;
			}
			if (hasId) {
				player = players.get(pId = msg.readInt());
			}
			CharSequence message = msg.readString();
			if (isHtml) {
				while (message.toString().contains("<a href='po:watch/")) {
					// THIS IS TEMPORARY //
					String stringified = message.toString();
					int openingtag = stringified.indexOf("<a href='po:watch/");
					int closingtag = stringified.indexOf("</a>", openingtag);
					String part1 = stringified.substring(0, openingtag);
					String part2 = "<watch id='";
					String part3 = stringified.substring(openingtag + "<a href='po:watch/".length(), closingtag);
					String part4 = "</watch>";
					String part5 = stringified.substring(closingtag + "</a>".length());
					stringified = part1 + part2 + part3 + part4 + part5;
					message = stringified;
					// THIS IS TEMPORARY //
				}
			}
			if (hasId) {
				if (ignoreList.contains(pId)) {
					break;
				}
				CharSequence color = (player == null ? "orange" : player.color.toHexString());
				CharSequence name = playerName(pId);
				String test = "";
				if (chatSettings.timeStamp) {
					test = "(" + StringUtilities.timeStamp() + ") ";
				} else {
					test = "";
				}
				String beg = "<font color='" + color + "'><b>" + test;
				if (playerAuth(pId) > 0 && playerAuth(pId) < 4) {
					beg += "+<i>" + name + ": </i></b></font>";
				} else {
					beg += name + ": </b></font>";
				}
				if (isHtml) {
					message = Html.fromHtml(beg + message);
				} else {
					if (chatSettings.flashing) {
						if (message.toString().toLowerCase().contains(this.me.nick.toLowerCase())) {
							// Be certain!
							Pattern myName = Pattern.compile("(?<!\\S)(" + this.me.nick.toLowerCase() + ")(?!\\w)");
							Matcher myMatcher = myName.matcher(message.toString().toLowerCase());
							if (myMatcher.find()) {
								message = Html.fromHtml(beg + StringUtilities.escapeHtml((String) message));
								int left = (String.valueOf(message)).toLowerCase().indexOf(this.me.nick.toLowerCase());
								int right = this.me.nick.length() + left;
								if (!hasChannel) {
									// Broadcast message
									if (chatActivity != null && message.toString().contains("Wrong password for this name.")) // XXX Is this still the message sent?
										chatActivity.makeToast(message.toString(), "long");
									else {
										Iterator<Channel> it = joinedChannels.iterator();
										while (it.hasNext()) {
											Channel next = it.next();
											tagHandler.currentChannel = next;
											next.writeToHist(message, left, right, chatSettings.color, false, null);
										}
									}
								} else {
									if (chan == null) {
										Log.e(TAG, "Received message for nonexistent channel");
									} else {
										boolean click = false;
										String command = null;
										hashTagMatcher = hashTagPattern.matcher(message);
										if (hashTagMatcher.find()) {
											command = hashTagMatcher.group(0);
											if (channelNameTagger(command.toLowerCase().replace("#", ""))) {
												click = true;
											} else {
												command = null;
											}
										}
										chan.writeToHist(message, left, right, chatSettings.color, click, command);
										if (chan != joinedChannels.getFirst()) {
											chan.flashed = true;
											chatActivity.notifyChannelList();
											if (chatSettings.notificationsFlash) {
												chatActivity.makeToast(playerName(pId) + " flashed you in " + chan.name() + ".", "long");
											}
										}
									}
								}
								break;
							}
						}
					}
					message = Html.fromHtml(beg + StringUtilities.escapeHtml((String) message));
				}
			} else {
				if (isHtml) {
					message = Html.fromHtml((String)message, imageParser, tagHandler);
				} else {
					String str = StringUtilities.escapeHtml((String)message);
					int index = str.indexOf(':');
					
					if (str.startsWith("*** ")) {
						message = Html.fromHtml("<font color='#FF00FF'>" + str + "</font>");
					} else if (index != -1) {
						String firstPart = str.substring(0, index);
						String secondPart;
						
						try {
							secondPart = str.substring(index+2);
						} catch (IndexOutOfBoundsException ex) {
							secondPart = "";
						}
						
						CharSequence color = "#318739";
						if (firstPart.equals("Welcome Message")) {
							color = "blue";
						} else if (firstPart.equals("~~Server~~")) {
							color = "orange";
						}
						
						message = Html.fromHtml("<font color='" + color + "'><b>" + firstPart +
								": </b></font>" + secondPart);
					}
				}
			}

			if (!hasChannel) {
				// Broadcast message
				if (chatActivity != null && message.toString().contains("Wrong password for this name.")) // XXX Is this still the message sent?
					chatActivity.makeToast(message.toString(), "long");
				else {
					Iterator<Channel> it = joinedChannels.iterator();
					while (it.hasNext()) {
						Channel next = it.next();
						tagHandler.currentChannel = next;
						next.writeToHist(message, false, null);
					}
				}
			} else {
				if (chan == null) {
					Log.e(TAG, "Received message for nonexistent channel");
				} else {
					boolean click = false;
					String command = null;
					hashTagMatcher = hashTagPattern.matcher(message);
					if (hashTagMatcher.find()) {
						command = hashTagMatcher.group(0);
						if (channelNameTagger(command.toLowerCase().replace("#", ""))) {
							click = true;
						} else {
							command = null;
						}
					}
					chan.writeToHist(message, click, command);
					if (chan != joinedChannels.getFirst()) {
						chan.newmessage = true;
					}
				}
			}
			break;
		}
		case BattleList: {
			msg.readInt(); //channel, but irrelevant
			int numBattles = msg.readInt();
			for (; numBattles > 0; numBattles--) {
				int battleId = msg.readInt();
				//byte mode = msg.readByte(); /* protocol is messed up */
				int player1 = msg.readInt();
				int player2 = msg.readInt();

				addBattle(battleId, new BattleDesc(player1, player2), false);
			}
			break;
		}
		case ChannelBattle: {
			msg.readInt(); //channel, but irrelevant
			int battleId = msg.readInt();
			//byte mode = msg.readByte();
			int player1 = msg.readInt();
			int player2 = msg.readInt();

			addBattle(battleId, new BattleDesc(player1, player2), false);
			break;
		} case ChallengeStuff: {
			IncomingChallenge challenge = new IncomingChallenge(msg);
			challenge.setNick(players.get(challenge.opponent));

			if (challenge.desc < 0 || challenge.desc >= ChallengeEnums.ChallengeDesc.values().length) {
				Log.w(TAG, "Challenge description out of bounds: " + challenge.desc);
				break;
			}
			switch(ChallengeEnums.ChallengeDesc.values()[challenge.desc]) {
			case Sent:
				if (challenge.isValidChallenge(players)) {
					if (ignoreList.contains(challenge.opponent)) {
						break;
					}
					challenges.addFirst(challenge);
					if (chatActivity != null && chatActivity.hasWindowFocus()) {
						chatActivity.notifyChallenge();
					} else {
						NotificationCompat.Builder mBuilder =
								new NotificationCompat.Builder(this)
										.setSmallIcon(R.drawable.icon)
										.setContentTitle("Pokemon Online")
										.setContentText("You've been challenged by " + challenge.oppName + "!");

						// Because clicking the notification opens a new ("special") activity, there's
						// no need to create an artificial back stack.
						PendingIntent resultPendingIntent =
								PendingIntent.getActivity(
									this,
									0,
									new Intent(NetworkService.this, ChatActivity.class),
									PendingIntent.FLAG_UPDATE_CURRENT
								);

						mBuilder.setContentIntent(resultPendingIntent);

						// Sets an ID for the notification
						int mNotificationId = IncomingChallenge.note;
						// Gets an instance of the NotificationManager service
						NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						// Builds the notification and issues it.
						mNotifyMgr.notify(mNotificationId, mBuilder.build());;
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
			case InvalidTier:
				if (chatActivity != null)
					chatActivity.makeToast("Challenge failed due to invalid tier", "long");
				break;
			}
			break;
		} case Reconnect: {
			boolean success = msg.readBool();
			
			if (success) {
				reconnectDenied = false;
				
				writeMessage("(" + StringUtilities.timeStamp() + ") Reconnected to server");
				
				for (Channel ch: joinedChannels) {
					ch.clearData();
				}
				joinedChannels.clear();
			} else {
				reconnectDenied = true;
			}
			break;
		} case Logout: {
			// Only sent when player is in a PM with you and logs out
			int playerID = msg.readInt();
			removePlayer(playerID);
				// logout event
			//System.out.println("Player " + playerID + " logged out.");
			break;
		} case BattleFinished: {
			int battleID = msg.readInt();
			byte battleDesc = msg.readByte();
			msg.readByte(); // battle mode
			int id1 = msg.readInt();
			int id2 = msg.readInt();
			Log.i(TAG, "bID " + battleID + " battleDesc " + battleDesc + " id1 " + id1 + " id2 " + id2);
			String[] outcome = new String[]{" won by forfeit against ", " won against ", " tied with "};
			if (isBattling(battleID) || spectatedBattles.containsKey(battleID)) {
				if (isBattling(battleID)) {
					//TODO: notification on win/lose
//					if (mePlayer.id == id1 && battleDesc < 2) {
//						showNotification(ChatActivity.class, "Chat", "You won!");
//					} else if (mePlayer.id == id2 && battleDesc < 2) {
//						showNotification(ChatActivity.class, "Chat", "You lost!");
//					} else if (battleDesc == 2) {
//						showNotification(ChatActivity.class, "Chat", "You tied!");
//					}
				}

				if (battleDesc < 2) {
					joinedChannels.peek().writeToHist(Html.fromHtml("<b><i>" + 
							StringUtilities.escapeHtml(playerName(id1)) + outcome[battleDesc] + 
							StringUtilities.escapeHtml(playerName(id2)) + ".</b></i>"), false, null);
				}

				if (battleDesc == 0 || battleDesc == 3) {
					closeBattle(battleID);
				}
			}

			removeBattle(battleID);
			break;
		} case SendPM: {
			int playerId = msg.readInt();
			if (ignoreList.contains(playerId)) {
				break;
			}
			String message = msg.readString();
			
			dealWithPM(playerId, message);
			break;
		} case SendTeam: {
				Bais flags = msg.readFlags();
				if (flags.readBool()) {
					String name = msg.readString();
				}
				if (flags.readBool()) {
					ArrayList<String> tiers = msg.readQStringList();
					chatActivity.makeToast("Loaded " + tiers.get(0), "short");
				}
			break;

		} case BattleMessage: {
			int battleId = msg.readInt(); // currently support only one battle, unneeded
			msg.readInt(); // discard the size, unneeded
			if (isBattling(battleId)) {
				activeBattle(battleId).receiveCommand(msg);
			}
			break;
		} case EngageBattle: {
			int battleId = msg.readInt();
			Bais flags = msg.readFlags();
			byte mode = msg.readByte();
			int p1 = msg.readInt();
			int p2 = msg.readInt();

			addBattle(battleId, new BattleDesc(p1, p2, mode), true);

			if(flags.readBool()) { // This is us!
				BattleConf conf = new BattleConf(msg, serverVersion.compareTo(new ProtocolVersion(1,0)) < 0);
				// Start the battle
				Battle battle = new Battle(conf, msg, getNonNullPlayer(conf.id(0)),
						getNonNullPlayer(conf.id(1)), myid, battleId, this);
				activeBattles.put(battleId, battle);

				joinedChannels.peek().writeToHist("Battle between " + playerName(p1) + 
						" and " + playerName(p2) + " started!", false, null);
				Intent intent = new Intent(this, BattleActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("battleId", battleId);
				startActivity(intent);
				findingBattle = false;
				
				showBattleNotification("Battle", battleId, conf);
			}
			
			if (chatActivity != null) {
				chatActivity.updatePlayer(players.get(p1), players.get(p1));
				chatActivity.updatePlayer(players.get(p2), players.get(p2));
			}
			break;
		} 
		case SpectateBattle: {
			Bais flags = msg.readFlags();
			int battleId = msg.readInt();
			
			if (flags.readBool()) {
				if (spectatedBattles.contains(battleId)) {
					Log.e(TAG, "Already watching battle " + battleId);
					socket.sendMessage(new Baos().putInt(battleId).putBool(false), Command.SpectateBattle);
					return;
				}
	            BattleConf conf = new BattleConf(msg, serverVersion.compareTo(new ProtocolVersion(1,0)) < 0);
				if (conf.mode != 0) {
					Log.e(TAG, "Can't watch doubles/triples/rotation: " + battleId);

					return;
				}
	            PlayerInfo p1 = getNonNullPlayer(conf.id(0));
	            PlayerInfo p2 = getNonNullPlayer(conf.id(1));
	            SpectatingBattle battle = new SpectatingBattle(conf, p1, p2, battleId, this);
	            spectatedBattles.put(battleId, battle);
	            
	            Intent intent = new Intent(this, BattleActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            intent.putExtra("battleId", battleId);
	            startActivity(intent);
	            
	            showBattleNotification("Spectated Battle", battleId, conf);
	        } else {
	        	closeBattle(battleId);
	        }
			break;
		}
		case SpectateBattleMessage: {
			int battleId = msg.readInt(); 
			msg.readInt(); // discard the size, unneeded
			if (spectatedBattles.containsKey(battleId)) {
				spectatedBattles.get(battleId).receiveCommand(msg);
			}
			break;
		}
		case AskForPass: {
			salt = msg.readString();
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
			addChannel(msg.readString(),msg.readInt());
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
			channels.put(chanId, new Channel(chanId, msg.readString(), this));
			break;
		} case ServerPassword: {
				salty = msg.readQByteArray();
				askedForServerPass = true;
				if (chatActivity != null && (chatActivity.hasWindowFocus() || chatActivity.progressDialog.isShowing())) {
					chatActivity.notifyAskForServerPass();
				}
			break;
		}/*  case AndroidID: {
+			new Thread(new Runnable() {
+				@TargetApi(Build.VERSION_CODES.HONEYCOMB)
+				public void run() {
+					try {
+						socket = new PokeClientSocket(NetworkService.this.ip, NetworkService.this.port);
+					} catch (IOException e) {
+						return;
+					}
+					socket.sendMessage(getUniqueID(), Command.AndroidID);
+				}
+			}).start();
				break;
		}*/ default: {
			System.out.println("Unimplented message");
		}
		}
		for(SpectatingBattle battle : getBattles()) {
			if (battle.activity != null && battle.histDelta.length() != 0) {
				battle.activity.updateBattleInfo(false);
			}
		}
		if (chatActivity != null && chatActivity.currentChannel() != null)
			chatActivity.updateChat();
	}

	private void writeMessage(String s) {
		if (chatActivity != null && chatActivity.currentChannel() != null) {
			chatActivity.currentChannel().writeToHist("\n"+s, false, null);
			chatActivity.updateChat();
		} else if (joinedChannels.size() > 0) {
			for (Channel c: joinedChannels) {
				c.writeToHist("\n"+s, false, null);
			}
		}
	}
	
	private PlayerInfo getNonNullPlayer(int id) {
		PlayerInfo p = players.get(id);
		
		if (p == null) {
			p = new PlayerInfo();
			p.nick = "???";
			p.id = id;
		}

		return p;
	}

	public chatPrefs getSettings() {
		return chatSettings;
	}

	public void tryFlashChannel(Channel chan) {
		if (chan != joinedChannels.getFirst()) {
			chan.flashed = true;
			chatActivity.notifyChannelList();
			if (chatSettings.notificationsFlash) {
				chatActivity.makeToast("You were flashed in " + chan.name() + ".", "short");
			}
		}
	}

	public void joinChannel(String channelName) {
		Baos join = new Baos();
		join.putString(channelName);
		if (socket.isConnected())
			socket.sendMessage(join, Command.JoinChannel);
	}

	public boolean channelNameTagger(String channelName) {
		for (Channel c : channels.values()) {
			if (c.name().toLowerCase().equals(channelName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a PM window with the other guy
	 * @param playerId the other guy's id
	 */
	public void createPM(int playerId) {
		pms.createPM(players.get(playerId));
	}
	
	private void dealWithPM(int playerId, String message) {
		pmedPlayers.add(playerId);
		createPM(playerId);
		pms.newMessage(players.get(playerId), message);

		if (chatSettings.notificationsPM && !PrivateMessageActivity.onTop()) {
			showPMNotification(playerId);
			if (chatSettings.cry) {
				long time = System.currentTimeMillis();
				if (time - 10000 > chatSettings.lastCall) {
					playPMCry(chatSettings.pokeNumber);
					chatSettings.lastCall = time;
				}
			}
		}
	}

	private void showPMNotification(int playerId) {
		PlayerInfo p = players.get(playerId);
		if (p == null) {
			p = new PlayerInfo();
		}
		
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_mail)
                .setContentTitle("PM - Pokemon Online")
                .setContentText("New message from " + p.nick())
                .setOngoing(true);
        
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, PrivateMessageActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("playerId", playerId);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ChatActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );

        //PendingIntent resultPendingIntent = PendingIntent.getActivity(this, battleId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = getNotificationManager();
        // mId allows you to update the notification later on.
        mNotificationManager.notify("pm", 0, mBuilder.build());
	}

	private void showBattleNotification(String title, int battleId, BattleConf conf) {
		PlayerInfo p1 = getNonNullPlayer(conf.id(0));
		PlayerInfo p2 = getNonNullPlayer(conf.id(1));
		
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(p1.nick() + " vs " + p2.nick())
                .setOngoing(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, BattleActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("battleId", battleId);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ChatActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            battleId,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );

        //PendingIntent resultPendingIntent = PendingIntent.getActivity(this, battleId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = getNotificationManager();
        // mId allows you to update the notification later on.
        mNotificationManager.notify("battle", battleId, mBuilder.build());
	}

	NotificationManager getNotificationManager() {
		return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void sendPass(String s, boolean isUserPass) {
		MessageDigest md5;
		if (isUserPass) {
			getSharedPreferences("passwords", MODE_PRIVATE).edit().putString(salt, s).commit();
			askedForPass = false;
			try {
				md5 = MessageDigest.getInstance("MD5");
				Baos hashPass = new Baos();
				hashPass.putBytes(md5.digest(mashBytes(toHex(md5.digest(s.getBytes("ISO-8859-1"))).getBytes("ISO-8859-1"), salt.getBytes("ISO-8859-1"))));
				socket.sendMessage(hashPass, Command.AskForPass);
			} catch (NoSuchAlgorithmException nsae) {
			} catch (UnsupportedEncodingException uee) {
			}
		} else {
			try {
				askedForServerPass = false;
				md5 = MessageDigest.getInstance("MD5");
				Baos hashPass = new Baos();
				hashPass.putBytes(md5.digest(mashBytes(md5.digest(s.getBytes("ISO-8859-1")), salty)));
				socket.sendMessage(hashPass, Command.ServerPassword);
				salty = null;
			} catch (NoSuchAlgorithmException nsae) {
			} catch (UnsupportedEncodingException uee) {
			}
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

	protected void addChannel(String chanName, int chanId) {
		Channel c = new Channel(chanId, chanName, this);
		channels.put(chanId, c);
		if(chatActivity != null)
			chatActivity.addChannel(c);
	}

	public void playCry(final SpectatingBattle battle, ShallowBattlePoke poke) {
		final int ringMode = ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
		
		/* Don't ring if in silent mode */
		if (ringMode == AudioManager.RINGER_MODE_NORMAL) {
			new Thread(new CryPlayer(poke, battle)).start();							
		} else if (ringMode == AudioManager.RINGER_MODE_VIBRATE) {
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			// Vibrate for 700 milliseconds
			v.vibrate(700);
		}

		/* In other cases, the cry's end will call notify on its own */
		if (ringMode != AudioManager.RINGER_MODE_NORMAL) {
			/* Can't notify right away, would be before the wait */
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(ringMode == AudioManager.RINGER_MODE_VIBRATE ? 1000 : 100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (battle) {
						battle.notify();
					}
				}
			}); 
		}
	}

	public void playPMCry(Integer pokemon) {
		final int ringMode = ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode();

		/* Don't ring if in silent mode */
		if (ringMode == AudioManager.RINGER_MODE_NORMAL) {
			new Thread(new CryPlayer(pokemon)).start();
		} else if (ringMode == AudioManager.RINGER_MODE_VIBRATE) {
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			// Vibrate for 700 milliseconds
			v.vibrate(700);
		}
	}

	class CryPlayer implements Runnable {
		ShallowBattlePoke poke;
		SpectatingBattle battle;

		public CryPlayer(ShallowBattlePoke poke, SpectatingBattle battle) {
			this.poke = poke;
			this.battle = battle;
		}

		public CryPlayer (int num) {
			poke = new ShallowBattlePoke();
			poke.uID.pokeNum = (short) num;
			battle = null;
		}

		public void run() {
			int resID = getResources().getIdentifier("p" + poke.uID.pokeNum,
					"raw", pkgName);
			if (resID != 0) {
				MediaPlayer cryPlayer = MediaPlayer.create(NetworkService.this, resID);
				float logarithmicVolume = (float) (Math.log(100-chatSettings.soundVolume)/10);
				cryPlayer.setVolume(logarithmicVolume, logarithmicVolume);
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
						cryPlayer.wait(5000);
					} catch (InterruptedException e) {}
				}
				cryPlayer.release();
				if (battle != null) {
					synchronized (battle) {
						battle.notify();
					}
				}
				cryPlayer = null;
			}
		}
	}

	public void disconnect() {
		if (socket != null && socket.isConnected()) {
			halted = true;
			socket.close();
		}
		this.stopForeground(true);
		this.stopSelf();
	}

    /*
	public PlayerInfo getPlayerByName(String playerName) {
		Enumeration<Integer> e = players.keys();
		while(e.hasMoreElements()) {
			PlayerInfo info = players.get(e.nextElement());
			if (info.nick().equals(playerName))
				return info;
		}
		return null;
	}
	*/

	public BattleDesc battle(Integer battleid) {
		return battles.get(battleid);
	}

	/**
	 * Tells the server we're not spectating a battle anymore, and close the appropriate
	 * spectating window
	 * @param bID the battle we're not watching anymore
	 */
	public void stopWatching(int bID) {
		socket.sendMessage(new Baos().putInt(bID).putBool(false), Command.SpectateBattle);
		closeBattle(bID);
	}

	public void startWatching(int bID) {
		if (bID != 0) {
			Baos watch = new Baos();
			watch.putInt(bID);
			watch.putBool(true); // watch, not leaving
			socket.sendMessage(watch, Command.SpectateBattle);
		}
	}

	/**
	 * Sends a private message to a user
	 * @param id Id of the user dest
	 * @param message message to send 
	 */
	public void sendPM(int id, String message) {
		Baos bb = new Baos();
		bb.putInt(id);
		bb.putString(message);
		socket.sendMessage(bb, Command.SendPM);
		
		pmedPlayers.add(id);
	}


	public void requestUserInfo(String name) {
		Baos b = new Baos();
		b.putString(name);
		socket.sendMessage(b, Command.GetUserInfo);
	}

	public void requestRanking(String tier, String name) {
		Baos b = new Baos();
		b.putString(tier);
		b.putBool(false);
		b.putString(name);
		socket.sendMessage(b, Command.ShowRankings);
	}

	public void requestRanking(String tier, int page) {
		Baos b = new Baos();
		b.putString(tier);
		b.putBool(false);
		b.putInt(page);
		socket.sendMessage(b, Command.ShowRankings);
	}

    public void loadJoinedChannelSettings() {
        SharedPreferences prefs = getSharedPreferences("autoJoinChannelsSettings", MODE_PRIVATE);

        String key = this.ip + ":" + this.port;

        Set<String> hasSettings = prefs.getStringSet(key, null);

        if (hasSettings != null) {
            for (Channel chan : joinedChannels) {
                if (hasSettings.contains(chan.name())) {
                    joinedChannels.get(joinedChannels.indexOf(chan)).channelEvents = true;
                }
            }
        }
    }

    public void updateJoinedChannelSettings() {
        SharedPreferences prefs = getSharedPreferences("autoJoinChannelsSettings", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        String key = this.ip + ":" + this.port;

        edit.remove(key);

        HashSet<String> settings = new HashSet<String>();

        for (Channel chan : joinedChannels) {
            if (chan.channelEvents) {
                settings.add(chan.name());
            }
        }

        edit.putStringSet(key, settings).apply();
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void updateJoinedChannels() {
		if (chatActivity != null) {
			chatActivity.populateUI(true);
            if (chatActivity.progressDialog != null) {
                chatActivity.networkDismissDialog();
            }
		}
		
		SharedPreferences prefs = getSharedPreferences("autoJoinChannels", MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		
		String key = this.ip + ":" + this.port;
		
		if (joinedChannels.size() == 1 && joinedChannels.getFirst().id == 0) {
			/* Only joined default channel! */
			edit.remove(key).remove(defaultKey+key);
		} else {
			boolean hasDefault = false;
			HashSet<String> autoJoin = new HashSet<String>();
			
			for (Channel chan : joinedChannels) {
				if (chan.id == 0) {
					hasDefault = true;
				} else {
					autoJoin.add(chan.name);
				}
			}
			
			if (hasDefault) {
				edit.remove(defaultKey+key);
			} else {
				String firstChan = joinedChannels.getFirst().name;
				autoJoin.remove(firstChan);
				
				edit.putString(defaultKey+key, firstChan);
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				edit.putStringSet(key, autoJoin);
			}
		}
		edit.commit();
	}

	public String getUniqueID() {
		String msg = "";
		try {
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				String ANDROID_ID = "";
				ANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
				if (ANDROID_ID != null) {
					if (ANDROID_ID.length() > 2) {
						String serial;
						try {
							serial = Build.class.getField("SERIAL").get(null).toString();
						} catch (NoSuchFieldException e) {
							serial = "PokemonOnline";
						} catch (IllegalAccessException e) {
							serial = "PokemonOnline";
						}
						ANDROID_ID += this.ip;
						serial += this.ip;
						UUID ANDROID = new UUID(ANDROID_ID.hashCode(), serial.hashCode());
						msg = ANDROID.toString();
					} else {
						msg = getPseudoUniqueID();
					}
				} else {
					msg = getPseudoUniqueID();
				}
			} else {
				msg = getPseudoUniqueID();
			}
			if (msg == null || msg.length() < 2) {
				return "SomethingBadHappened";
			}
		} catch (Exception e) {
			return "SomethingBadHappened";
		}
		return msg;
	}

	public Boolean getUniqueIDFlag() {
		Boolean flag;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			String ANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
			if (ANDROID_ID != null) {
				flag = true;
			} else {
				flag = false;
			}
		} else {
			flag = false;
		}
		return flag;
	}


	private String getPseudoUniqueID() {
		try {
			String serial = "";
			try {
				serial = Build.class.getField("SERIAL").get(null).toString();
			} catch (NoSuchFieldException e) {
				serial = "PokemonOnline";
			} catch (IllegalAccessException e) {
				serial = "PokemonOnline";
			}
			String PSEUDO_ID = "13" + (Build.BOARD.length() % 5) + (Build.BRAND.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
			PSEUDO_ID += this.ip;
			serial += this.ip;
			UUID PSEUDO = new UUID(PSEUDO_ID.hashCode(), serial.hashCode());
			String PSEUDO_String = PSEUDO.toString();
			if (PSEUDO_String == null || PSEUDO_String.length() < 2) {
				return "SomethingBadHappened";
			}
			return PSEUDO_String;
		} catch (Exception e) {
			return "SomethingBadHappened";
		}
	}

	public String getDefaultPass() {
		return getSharedPreferences("passwords", MODE_PRIVATE).getString(salt, "");
	}
}