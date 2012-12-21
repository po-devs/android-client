package com.pokebros.android.pokemononline.battle;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

public class BattleConf extends SerializeBytes {
	public byte gen = 0;
	public byte mode = 0;
	public int[] ids = new int[2];
	public int clauses;
	
	public int id(int i) { return ids[i]; }
	public byte mode() { return mode; };
	
	public BattleConf(Bais msg) {
		gen = msg.readByte();
		mode = msg.readByte();
		ids[0] = msg.readInt();
		ids[1] = msg.readInt();
		clauses = msg.readInt();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.write(gen);
		b.write(mode);
		b.putInt(ids[0]);
		b.putInt(ids[1]);
		b.putInt(clauses);
		return b;
	}
}
