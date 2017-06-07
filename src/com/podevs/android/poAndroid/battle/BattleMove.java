package com.podevs.android.poAndroid.battle;

import android.graphics.Color;
import com.podevs.android.poAndroid.ColorEnums.TypeColor;
import com.podevs.android.poAndroid.poke.Move;
import com.podevs.android.poAndroid.pokeinfo.DamageClassInfo;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo.Target;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo.Type;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class BattleMove implements SerializeBytes, Move {
	public byte currentPP = 0;
	public byte totalPP = 0;
	public short num = 0;
	public byte power = 0;
	public byte accuracy = 0;
	public byte damageClass = 0;
	public Target range = Target.ChosenTarget;
	public byte type = (byte) Type.Curse.ordinal();
	
	public String toString() {
		return MoveInfo.name(num);
	}
	
	public int getColor() {
		String s = TypeColor.values()[getType()].toString();
		s = s.replaceAll(">", "");
		return Color.parseColor(s);
	}

	public String getHexColor() {
		String s = TypeColor.values()[getType()].toString();
		s = s.replaceAll(">", "");
		return s;
	};
	
	public byte getType() {
		return type;
	}
	
	public BattleMove() {}
	
	public BattleMove(int n) {
		num = (short) n;
		power = MoveInfo.power(n);
		accuracy = MoveInfo.accuracy(n);
		damageClass = MoveInfo.damageClass(n);
		range = MoveInfo.target(n);
		type = MoveInfo.type(n);
		totalPP = (byte) (MoveInfo.pp(n)*8/5);
	}
	
	public BattleMove(BattleMove bm) {
		currentPP = bm.currentPP;
		totalPP = bm.totalPP;
		num = bm.num;
		power = bm.power;
		accuracy = bm.accuracy;
		damageClass = bm.damageClass;
		range = bm.range;
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
		String zeffect = MoveInfo.zDescription(num);
		s += "Power: " + MoveInfo.powerToString(power);
		s += "\nAccuracy: " + MoveInfo.accuracyToString(accuracy);
		s += "\nClass: " + DamageClassInfo.name(damageClass);
		s += "\nRange: " + MoveInfo.targetToString(range);
		s += "\n";
		s += "\nEffect: " + MoveInfo.effect(num);
		if (zeffect.length() > 0) {
			s += "\nZ-Effect: " + zeffect;
		}
		return s;
	}

	public int num() {
		return num;
	}

	public String stringPP() {
		if (totalPP == 0) {
			return "??/??";
		} else {
			return currentPP + "/" + totalPP;
		}
	}
}