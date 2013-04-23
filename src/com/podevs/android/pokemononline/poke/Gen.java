package com.podevs.android.pokemononline.poke;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

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
