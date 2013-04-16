package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

public class Gen implements SerializeBytes {
	public byte num;
	public byte subNum;
	
	public Gen(Bais msg) {
		num = msg.readByte();
		subNum = msg.readByte();
	}
	
	public Gen(int s, int b) {
		num = (byte) s;
		subNum = (byte) b;
	}
	
	public Gen() {
		num = 5;
		subNum = 1;
	}

	public void serializeBytes(Baos bytes) {
		bytes.write(num);
		bytes.write(subNum);
	}
}
