package com.pokebros.android.pokemononline.battle;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.ShallowShownPoke;

public class ShallowShownTeam extends SerializeBytes {
	public ShallowShownPoke[] pokes = new ShallowShownPoke[6];
	
	public ShallowShownTeam(Bais msg) {
		for (int i = 0; i < 6; i++)
			pokes[i] = new ShallowShownPoke(msg);
	}
	
	public ShallowShownPoke poke(int index) {
		return pokes[index];
	}

	public Baos serializeBytes() {
		Baos b = new Baos();
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
		return b;
	}
}
