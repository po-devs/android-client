package com.podevs.android.poAndroid.poke;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

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
	
	public void serializeBytes(Baos b) {
		b.putBaos(uID);
		b.write(level);
		b.write(gender);
		b.putBool(item);
	}
}
