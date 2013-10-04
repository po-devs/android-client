package com.podevs.android.pokemononline.pokeinfo;

import com.podevs.android.pokemononline.poke.Gen;
import com.podevs.android.pokemononline.poke.Poke;
import com.podevs.android.pokemononline.pokeinfo.TypeInfo.Type;

public class HiddenPowerInfo {
	static public int Type(Poke p) {
		return ( ( ( (p.dv(0) & 1) + 2 * (p.dv(1) & 1) + 4 * (p.dv(2) & 1) + 8 * (p.dv(5) & 1) + 
				16 * (p.dv(3) & 1) + 32 * (p.dv(4) & 1) ) * (p.gen().num <= 5 ? 15 : 16)) / 63 ) + 1;
	}
	
	static public int Type(Gen gen, int hpDv, int attDv, int defDv, int spAttDv, int spDefDv, int speedDv) {
		return ( ( ( (hpDv & 1) + 2 * (attDv & 1) + 4 * (defDv & 1) + 8 * (speedDv & 1) + 
				16 * (spAttDv & 1) + 32 * (spDefDv & 1) ) * (gen.num <= 5 ? 15 : 16)) / 63 ) + 1;
	}
	
	static byte hiddenPowerConfigurations[][] = null; 
	
	static public byte[] configurationForType(final int type, Gen gen) {
		if (hiddenPowerConfigurations == null) {
			hiddenPowerConfigurations = new byte[Type.Curse.ordinal()][];
		}
		
		if (type == 0 || type >= hiddenPowerConfigurations.length) {
			return null;
		}
		
		if (hiddenPowerConfigurations[type] == null) {
		    for (int i = 63; i >= 0; i--) {
		        int gt = Type(gen, i & 1, (i & 2)>>1, (i & 4)>>2, (i & 8)>>3, (i & 16)>>4, (i & 32)>>5);
		        if (gt == type) {
		        	hiddenPowerConfigurations[type] = new byte[]{(byte)((i&1)!=0?31:30), (byte)((i&2)!=0?31:30),
		        			(byte)((i&4)!=0?31:30),(byte)((i&8)!=0?31:30),
		        			(byte)((i&16)!=0?31:30),(byte)((i&32)!=0?31:30)};
		        }
		    }
		}
				
		return hiddenPowerConfigurations[type];
	}
}
