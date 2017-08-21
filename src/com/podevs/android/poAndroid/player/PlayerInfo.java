package com.podevs.android.poAndroid.player;

import android.util.Log;
import com.podevs.android.utilities.*;

import java.util.ArrayList;

// Represents a player in the player list
public class PlayerInfo implements SerializeBytes {
	private final static short VERSION = 0;
	private final static String TAG = "PlayerInfo";
	
	public int id;
	public boolean isAway;
	public boolean hasLadderEnabled;
	public String nick;
	public QColor color;
	public short avatar;
	String info;
	public byte auth;
	public ArrayList<Integer> battles = new ArrayList<Integer>();

	public class TierStanding {
		public String tier;
		public short rating;
		
		TierStanding(String tier, short rating) {
			this.tier = tier;
			this.rating = rating;
		}
	}
	
	public ArrayList<TierStanding> tierStandings;

	public String nick() { return nick; }
	public String info() { return info; }
	public String toString() { return nick; }
	
	public PlayerInfo(FullPlayerInfo player) { nick = player.nick(); }
	
	public PlayerInfo() {}
	
	public PlayerInfo(Bais msg) {
		short len = msg.readShort();
		short version = (short)((short)msg.readByte() & 0xff);
		if (VERSION < version) {
			Log.d(TAG, "Skipped unknown version " + version);
			msg.skip(len - 1);
		}
		id = msg.readInt();
		/* Network flags, unused yet */
		msg.readFlags();

		Bais dataFlags = msg.readFlags();
		isAway = dataFlags.readBool();
		hasLadderEnabled = dataFlags.readBool();
		nick = msg.readString();
		color = new QColor(msg);
		avatar = msg.readShort();
		info = msg.readString();
		
		// No info provided, but "" is a little safer to use than null.
		if (info == null) {
			info = "";
		}
		auth = msg.readByte();
		
		short numTiers = (short)((short)msg.readByte() & 0xff);
		tierStandings = new ArrayList<TierStanding>(numTiers);
		for (int i = 0; i < numTiers; i++) {
			tierStandings.add(new TierStanding(msg.readString(), msg.readShort()));
		}

		if (!color.isValid()) {
			color = new QColor(id, 12);
		}
	}
	
	public void serializeBytes(Baos b) {
		b.putInt(id);
		b.putString(nick);
		b.putString(info);
		b.write(auth);
		b.putBaos(color);
	}
	
	static public class TierStandingGetter implements Getter<TierStanding> {
		public String get(TierStanding object, int index) {
			if (index == 0) {
				return object.tier;
			} else {
				if (object.rating == -1) {
					return "???";
				} else {
					return String.valueOf(object.rating);
				}
			}
		}
	}
	static public TierStandingGetter tierGetter = new TierStandingGetter();
	
	public boolean equals(PlayerInfo p) {
		return nick.equals(p);
	}
	
	/**
	 * Is the player battling
	 * @return true if the player is in one or more battles, false otherwise
	 */
	public boolean battling() {
		return battles.size() > 0;
	}
	
	public void addBattle(int battleid) {
		if (!battles.contains(battleid)) {
			battles.add(battleid);
		}
	}
	
	public void removeBattle(int battleID) {
		int index = battles.indexOf(battleID);
		if (index != -1) {
			battles.remove(index);
		}
	}
	
	/**
	 * Updates self to the other player info
	 * @param info the info to copy
	 */
	public void setTo(PlayerInfo info) {
		id = info.id;
		isAway = info.isAway;
		hasLadderEnabled = info.hasLadderEnabled;
		nick = info.nick;
		color = info.color;
		avatar = info.avatar;
		this.info = info.info;
		auth = info.auth;
		battles = info.battles;
		tierStandings = info.tierStandings;
	}
}