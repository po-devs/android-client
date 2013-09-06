package com.podevs.android.pokemononline.pokeinfo;

import com.podevs.android.pokemononline.poke.Poke;

public class HiddenPowerInfo {
	static public int hiddenPowerType(Poke p) {
		return ( ( ( (p.dv(0) & 1) + 2 * (p.dv(1) & 1) + 4 * (p.dv(2) & 1) + 8 * (p.dv(5) & 1) + 
				16 * (p.dv(3) & 1) + 32 * (p.dv(4) & 1) ) * 15) / 63 ) + 1;
	}
	
	static byte hiddenPowerConfigurations[][] = null; 
	
	static public byte[] configurationForType(final int type) {
		if (hiddenPowerConfigurations == null) {
			hiddenPowerConfigurations = new byte[17][];
		}
		
		if (type == 0 || type >= hiddenPowerConfigurations.length) {
			return null;
		}
		
		if (hiddenPowerConfigurations[type] == null) {
			InfoFiller.fill("db/types/type" + type + "_hp.txt", new InfoFiller.Filler() {
				public void fill(int i, String s) {
					if (hiddenPowerConfigurations[type] != null) {
						return;
					}
					
					hiddenPowerConfigurations[type] = new byte[6];
					hiddenPowerConfigurations[type][0] = (byte)i;
					
					String[] spl = s.split(" ");
					
					for (int j = 0; j < 5 && j < spl.length; j++) {
						hiddenPowerConfigurations[type][j+1] = (byte)Integer.parseInt(spl[j]);
					}
				}
			});
		}
				
		return hiddenPowerConfigurations[type];
	}
}
