package com.podevs.android.poAndroid;

import android.os.Bundle;
import com.podevs.android.poAndroid.battle.ChallengeEnums.ChallengeDesc;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.utilities.Bais;

import java.util.Hashtable;
import java.util.Random;

public class IncomingChallenge {
	byte desc;
	public byte mode;
	public String srcTier;
	public String destTier;
	public int opponent;
	public int clauses;
	int team;
	Gen gen;
	String oppName = null;
	public static int note = new Random().nextInt();
	
	public IncomingChallenge(Bais msg) {
		desc = msg.readByte();
		opponent = msg.readInt();
		clauses = msg.readInt();
		mode = msg.readByte();
		team = msg.read();
		gen = new Gen(msg);
		srcTier = msg.readString();
		destTier = msg.readString();
	}
	
	public void setNick(PlayerInfo p) { if(p != null) oppName = p.nick(); }
	
	public boolean isValidChallenge(Hashtable<Integer, PlayerInfo> players) {
		setNick(players.get(opponent));
		return desc == ChallengeDesc.Sent.ordinal() && oppName != null;
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putByte("desc", desc);
		b.putByte("mode", mode);
		b.putInt("opponent", opponent);
		b.putInt("clauses", clauses);
		b.putString("oppName", oppName);
		b.putString("srcTier", srcTier);
		b.putByte("gen", gen.num);
		b.putByte("subgen", gen.subNum);
		b.putString("destTier", destTier);
		return b;
	}
}
