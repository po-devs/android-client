package com.podevs.android.poAndroid.poke;

import com.podevs.android.poAndroid.pokeinfo.GenInfo;
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

	public Gen(int s) {
		num = (byte) s;
		subNum = (byte) (s/65536);
	}
	
	public Gen() {
		this(GenInfo.genMax(), GenInfo.maxSubgen(GenInfo.genMax()));
	}

	public void serializeBytes(Baos bytes) {
		bytes.write(num);
		bytes.write(subNum);
	}

	@Override
	public int hashCode() {
		/* Using some functions in common with UniqueID (InfoFiller.uidFill), so have to use the
		   same hashing method.
		 */
		return (int)num + (subNum * 65536);
	}

	@Override
	public boolean equals(Object other) {
		try {
			Gen o = (Gen) other;
			return num == o.num && subNum == o.subNum;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
