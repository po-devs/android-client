package com.podevs.android.pokemononline.battle;

import android.graphics.Color;

import com.podevs.android.pokemononline.ColorEnums.TypeColor;
import com.podevs.android.pokemononline.poke.Move;
import com.podevs.android.pokemononline.pokeinfo.DamageClassInfo;
import com.podevs.android.pokemononline.pokeinfo.MoveInfo;
import com.podevs.android.pokemononline.pokeinfo.TypeInfo.Type;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class BattleMove implements SerializeBytes, Move {
	public byte currentPP = 0;
	public byte totalPP = 0;
	public short num = 0;
	public byte type = (byte) Type.Curse.ordinal();
	
	public String toString() {
		return MoveInfo.name(num);
	}
	
	public int getColor() {
		String s = TypeColor.values()[getType()].toString();
		s = s.replaceAll(">", "");
		return Color.parseColor(s);
	}
	
	public byte getType() {
		return type;
	}
	
	public BattleMove() {}
	
	public BattleMove(int n) {
		num = (short) n;
		type = MoveInfo.type(n);
		totalPP = (byte) (MoveInfo.pp(n)*8/5);
	}
	
	public BattleMove(BattleMove bm) {
		currentPP = bm.currentPP;
		totalPP = bm.totalPP;
		num = bm.num;
		type = bm.type;
	}
	
	public BattleMove(Bais msg) {
		this(msg.readShort());
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
		s += "Power: " + MoveInfo.powerString(num);
		s += "\nAccuracy: " + MoveInfo.accuracyString(num);
		s += "\nClass: " + DamageClassInfo.name(MoveInfo.damageClass(num));
		s += "\n";
		s += "\nEffect: " + MoveInfo.effect(num);
		return s;
	}

	public int num() {
		return num;
	}
}