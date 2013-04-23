package com.podevs.android.pokemononline.poke;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

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
	
	@Override
	public boolean equals(Object other) {
		try {
			UniqueID o = (UniqueID) other;
			return pokeNum == o.pokeNum && subNum == o.subNum;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (int)pokeNum + subNum*65536;
	}

	public void serializeBytes(Baos bytes) {
		bytes.putShort(pokeNum);
		bytes.write(subNum);
	}
}
