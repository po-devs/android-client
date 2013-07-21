package com.podevs.android.pokemononline.pokeinfo;

import com.podevs.android.pokemononline.poke.Poke;

public class HiddenPowerInfo {
	static public int hiddenPowerType(Poke p) {
		return ( ( ( (p.dv(0) & 1) + 2 * (p.dv(1) & 1) + 4 * (p.dv(2) & 1) + 8 * (p.dv(5) & 1) + 
				16 * (p.dv(3) & 1) + 32 * (p.dv(4) & 1) ) * 15) / 63 ) + 1;
	}
}
