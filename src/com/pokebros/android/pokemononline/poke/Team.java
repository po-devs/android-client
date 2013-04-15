package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

public class Team implements SerializeBytes {
	protected byte gen = 5;
	protected byte subgen = 1;
	protected String defaultTier;
	protected TeamPoke[] pokes = new TeamPoke[6];
	
	/* Used internally only */
	public Team(Bais msg) {
		defaultTier = msg.readString();
		gen = msg.readByte();
		subgen = msg.readByte();
		for(int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke(msg);
	}
	
	public Team() {
		for (int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke();
	}
	
	public void serializeBytes(Baos bytes) {
		bytes.putString(defaultTier);
		bytes.write(gen);
		bytes.write(subgen);
		for(int i = 0; i < 6; i++)
			bytes.putBaos(pokes[i]);
	}
}
