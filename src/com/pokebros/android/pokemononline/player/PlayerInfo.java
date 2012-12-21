package com.pokebros.android.pokemononline.player;

import java.util.ArrayList;
import java.util.Comparator;

import android.util.Log;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.UniqueID;

// Represents a player in the player list
public class PlayerInfo extends SerializeBytes {
	final static short VERSION = 0;
	final static String TAG = "PlayerInfo";
	
	public int id;
	Boolean isAway;
	Boolean hasLadderEnabled;
	String nick;
	public QColor color;
	short avatar;
	String info;
	public byte auth;

	class TierStanding {
		public String tier;
		public short rating;
		
		TierStanding(String tier, short rating) {
			this.tier = tier;
			this.rating = rating;
		}
	}
	
	ArrayList<TierStanding> tierStandings;

	public String nick() { return nick; }
	public String info() { return info; }
	public String toString() { return nick; }
	
	public PlayerInfo(FullPlayerInfo player) { nick = player.nick(); }
	
	public PlayerInfo() {}
	
	public PlayerInfo(Bais msg) {
		short len = msg.readShort();
		short version = (short)((short)msg.readByte() & 0xff);
		if (VERSION < version) {
			Log.d(TAG, "Skipped unkown version " + version);
			msg.skip(len - 1);
		}
		id = msg.readInt();
		Bais netFlags = msg.readFlags();
		Bais dataFlags = msg.readFlags();
		isAway = dataFlags.readBool();
		hasLadderEnabled = dataFlags.readBool();
		nick = msg.readString();
		color = new QColor(msg);
		avatar = msg.readShort();
		info = msg.readString();
		if (info == null)
			// No info provided, but "" is a little safer to use than null.
			info = "";
		auth = msg.readByte();
		
		short numTiers = (short)((short)msg.readByte() & 0xff);
		tierStandings = new ArrayList<TierStanding>(numTiers);
		for (int i = 0; i < numTiers; i++) {
			tierStandings.add(new TierStanding(msg.readString(), msg.readShort()));
		}
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putInt(id);
		b.putString(nick);
		b.putString(info);
		b.write(auth);
		b.putBaos(color);
		return b;
	}

	static public class ComparePlayerInfos implements Comparator<PlayerInfo> {
		public int compare(PlayerInfo p1, PlayerInfo p2) {
			return p1.nick.compareTo(p2.nick);
		}
	}
	
	public boolean equals(PlayerInfo p) {
		return nick.equals(p);
	}
	
	public void update(PlayerInfo p) {
		this.auth = p.auth;
		this.nick = p.nick;
		this.info = p.info;
		this.avatar = p.avatar;
	}
}