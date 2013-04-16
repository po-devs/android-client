package com.pokebros.android.pokemononline.battle;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.ShallowShownPoke;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

public class ShallowShownTeam implements SerializeBytes {
	public ShallowShownPoke[] pokes = new ShallowShownPoke[6];
	
	public ShallowShownTeam(Bais msg) {
		for (int i = 0; i < 6; i++)
			pokes[i] = new ShallowShownPoke(msg);
	}
	
	public ShallowShownPoke poke(int index) {
		return pokes[index];
	}

	public void serializeBytes(Baos b) {
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
	}
}
