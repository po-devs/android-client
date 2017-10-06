package com.podevs.android.poAndroid.battle;

import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class BattleTeam implements SerializeBytes {

	public BattlePoke[] pokes = new BattlePoke[6];

	public BattleTeam(Bais msg, Gen gen) {
		for(int i = 0; i < 6; i++) {
			pokes[i] = new BattlePoke(msg, gen);
			pokes[i].teamNum = (byte)i;
		}
	}
	
	public void serializeBytes(Baos b) {
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
	}
}
