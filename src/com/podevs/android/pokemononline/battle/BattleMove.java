package com.podevs.android.pokemononline.battle;

import android.graphics.Color;

import com.podevs.android.pokemononline.DataBaseHelper;
import com.podevs.android.pokemononline.ColorEnums.TypeColor;
import com.podevs.android.pokemononline.pokeinfo.MoveInfo;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class BattleMove implements SerializeBytes {
	public byte currentPP = 0;
	public byte totalPP = 0;
	public short num = 0;
	public String name = "No Move";
	public byte type = (byte) Type.Curse.ordinal();
	private String power = "--";
	private String accuracy = "--";
	private String description = "";
	private String effect = "";
	
	public String toString() {
		return name;
	}
	
	public int getColor() {
		String s = TypeColor.values()[getType()].toString();
		s = s.replaceAll(">", "");
		return Color.parseColor(s);
	}
	
	public String getTypeString() {
		return Type.values()[getType()].toString();
	}
	
	public byte getType() {
		return type;
	}
	
	public BattleMove() {}
	
	public BattleMove(int n, DataBaseHelper db) {
		num = (short) n;
		name = MoveInfo.name(n);
		type = MoveInfo.type(n);
		totalPP = (byte) (MoveInfo.pp(n)*8/5);
		power = MoveInfo.power(n);
		effect = MoveInfo.effect(n);
		accuracy = MoveInfo.accuracy(n);
	}
	
	public BattleMove(BattleMove bm) {
		currentPP = bm.currentPP;
		totalPP = bm.totalPP;
		num = bm.num;
		name = bm.name;
		type = bm.type;
		power = bm.power;
		accuracy = bm.accuracy;
		description = bm.description;
		effect = bm.effect;
	}
	
	public BattleMove(Bais msg, DataBaseHelper db) {
		this(msg.readShort(), db);
		currentPP = msg.readByte();
		totalPP = msg.readByte();
	}
	
	public void serializeBytes(Baos b) {
		b.putShort(num);
		b.write(currentPP);
		b.write(totalPP);
	}
	
	public String descAndEffects() {
		String s = "";
		s += "Power: " + power;
		s += "\nAccuracy: " + accuracy;
		s += "\n";
		s += "\nDescription: " + description;
		s += "\nEffect: " + effect;
		return s;
	}
}