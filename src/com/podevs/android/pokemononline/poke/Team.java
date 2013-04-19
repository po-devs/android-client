package com.podevs.android.pokemononline.poke;

import com.podevs.android.pokemononline.SerializeBytes;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

public class Team implements SerializeBytes {
	public Gen gen = new Gen();

	protected String defaultTier;
	protected TeamPoke[] pokes = new TeamPoke[6];
	
	/* Used internally only */
	public Team(Bais msg) {
		Bais b = new Bais(msg.readVersionControlData());
		int version = (int) b.read();
		
		if (version == 0) {
			defaultTier = b.readBool() ? b.readString() : "";
			gen = new Gen(b);
			for(int i = 0; i < 6; i++) {
				pokes[i] = new TeamPoke(b, gen);
			}
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
		
		b.putBaos(gen);
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
		
		bytes.putVersionControl(0, b);
	}

	public boolean isValid() {
		return pokes[0].uID.pokeNum != 0;
	}
}
