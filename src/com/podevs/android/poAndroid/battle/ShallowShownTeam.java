package com.podevs.android.poAndroid.battle;

import com.podevs.android.poAndroid.poke.ShallowShownPoke;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class ShallowShownTeam implements SerializeBytes {
	public ShallowShownPoke[] pokes = new ShallowShownPoke[6];
	
	public ShallowShownTeam(Bais msg, byte gen) {
		for (int i = 0; i < 6; i++)
			pokes[i] = new ShallowShownPoke(msg, gen);
	}
	
	public ShallowShownPoke poke(int index) {
		return pokes[index];
	}

	public void serializeBytes(Baos b) {
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
	}
}
