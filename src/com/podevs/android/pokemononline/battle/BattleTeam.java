package com.podevs.android.pokemononline.battle;

import com.podevs.android.pokemononline.DataBaseHelper;
import com.podevs.android.pokemononline.poke.Gen;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class BattleTeam implements SerializeBytes {
	String nick = "";
	String info = "";
	Gen gen = new Gen();
	
	public BattlePoke[] pokes = new BattlePoke[6];
	int[] indexes = new int[6];
	
	public BattleTeam(Bais msg, DataBaseHelper db, Gen gen) {
		for(int i = 0; i < 6; i++) {
			pokes[i] = new BattlePoke(msg, db, gen);
			pokes[i].teamNum = (byte)i;
		}
	}
	
	public void serializeBytes(Baos b) {
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
	}
}
