package com.podevs.android.poAndroid.battle;

import com.podevs.android.utilities.ArrayUtilities;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class BattleDynamicInfo implements SerializeBytes {
	public static final byte SPIKES = 1;
	public static final byte SPIKESL2 = 2;
	public static final byte SPIKESL3 = 4;
	public static final byte STEALTHROCK = 8;
	public static final byte TOXICSPIKES = 16;
	public static final byte TOXICSPIKESL2 = 32;
	public static final byte STICKYWEB = 64;
	
	byte[] boosts = new byte[7];
	byte flags;
	
	public BattleDynamicInfo(Bais b) {
		for(int i = 0; i < 7; i++) boosts[i] = b.readByte();
		flags = b.readByte();
	}

	public void serializeBytes(Baos b) {
		for(int i = 0; i < 7; i++) b.write(boosts[i]);
		b.write(flags);
	}
	
	public String statsAndHazards() {
		String s = "HP:";
		s += "\nAttack:";
		s += "\nDefense:";
		s += "\nSp. Att:";
		s += "\nSp. Def:";
		s += "\nSpeed:";
		if (boosts[5] != 0) s += "\nAccuracy:";
		if (boosts[6] != 0) s += "\nEvasion:";
		if(flags != 0) s += "\n";
		if((flags & SPIKES) != 0) s += "\nSpikes";
		if((flags & SPIKESL2) != 0) s += "\nSpikes Lvl. 2";
		if((flags & SPIKESL3) != 0) s += "\nSpikes Lvl. 3";
		if((flags & STEALTHROCK) != 0) s += "\nStealth Rock";
		if((flags & TOXICSPIKES) != 0) s += "\nToxic Spikes";
		if((flags & TOXICSPIKESL2) != 0) s += "\nToxic Spikes Lvl. 2";
		if((flags & STICKYWEB) != 0) s += "\nSticky Web";
		return s;
	}

	public String stats() {
		String s = "HP:";
		s += "\nAttack:";
		s += "\nDefense:";
		s += "\nSp. Att:";
		s += "\nSp. Def:";
		s += "\nSpeed:";
		return s;
	}
	
	public String boosts() {
		String s = "";
		for (int i = 0; i < 5; i++)
			s += "\n" + (boosts[i] == 0 ? "" : "(" + (boosts[i] > 0 ? "+" + boosts[i] : boosts[i]) + ")");
		for (int i = 5; i < 7; i++)
			if (boosts[i] != 0) s += "\n" + (boosts[i] < 0 ? "" : "+") + boosts[i]; 
		return s;
	}

	@Override
	public String toString() {
		return "{flags:" + flags + ",boosts:" + ArrayUtilities.join(boosts, ",") + "}";
	}
}
