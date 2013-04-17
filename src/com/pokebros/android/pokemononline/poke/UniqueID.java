package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

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
		pokeNum = 0;
		subNum = 0;
	}

	public void serializeBytes(Baos bytes) {
		bytes.putShort(pokeNum);
		bytes.write(subNum);
	}
}
