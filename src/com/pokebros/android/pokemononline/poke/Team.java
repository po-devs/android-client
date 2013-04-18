package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

public class Team implements SerializeBytes {
	protected byte gen = 5;
	protected byte subgen = 1;
	protected String defaultTier;
	protected TeamPoke[] pokes = new TeamPoke[6];
	
	/* Used internally only */
	public Team(Bais msg) {
		int version = (int) msg.read();
		Bais b = new Bais(msg.readQByteArray());
		
		if (version == 0) {
			defaultTier = b.readBool() ? b.readString() : "";
			gen = b.readByte();
			subgen = b.readByte();
			for(int i = 0; i < 6; i++)
				pokes[i] = new TeamPoke(b);
		}
	}
	
	public Team() {
		for (int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke();
	}
	
	public void serializeBytes(Baos bytes) {
		Baos b = new Baos();
		
		b.write(defaultTier.length() > 0 ? 1 : 0);
		if (defaultTier.length() > 0) {
			b.putString(defaultTier);
		}
		
		b.write(gen);
		b.write(subgen);
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
		
		bytes.putVersionControl(0, b);
	}

	public boolean isValid() {
		return pokes[0].uID.pokeNum != 0;
	}
}
