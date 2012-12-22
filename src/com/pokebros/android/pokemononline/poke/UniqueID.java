package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

public class UniqueID implements SerializeBytes {
	public short pokeNum;
	public byte subNum;
	
	public UniqueID(Bais msg) {
		pokeNum = msg.readShort();
		subNum = msg.readByte();
	}
	
	public UniqueID(int s, int b) {
		pokeNum = (short) s;
		subNum = (byte) b;
	}
	
	public UniqueID() {
		pokeNum = 173;
		subNum = 0;
	}

	public void serializeBytes(Baos bytes) {
		bytes.putShort(pokeNum);
		bytes.write(subNum);
	}
}
