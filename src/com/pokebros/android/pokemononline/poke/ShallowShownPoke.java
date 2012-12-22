package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

public class ShallowShownPoke implements SerializeBytes {
	public boolean item;
	public UniqueID uID;
	public byte level;
	public byte gender;
	
	public ShallowShownPoke(Bais msg) {
		uID = new UniqueID(msg);
		level = msg.readByte();
		gender = msg.readByte();
		item = msg.readBool();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putBaos(uID);
		b.write(level);
		b.write(gender);
		b.putBool(item);
		return b;
	}
}
