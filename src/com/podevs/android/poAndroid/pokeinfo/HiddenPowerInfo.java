package com.podevs.android.poAndroid.pokeinfo;

import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.poAndroid.poke.Poke;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo.Type;

public class HiddenPowerInfo {
	static public int Type(Poke p) {
		return ( ( ( (p.dv(0) & 1) + 2 * (p.dv(1) & 1) + 4 * (p.dv(2) & 1) + 8 * (p.dv(5) & 1) + 
				16 * (p.dv(3) & 1) + 32 * (p.dv(4) & 1) ) * 15) / 63 ) + 1;
	}
	
	static public int Type(Gen gen, int hpDv, int attDv, int defDv, int spAttDv, int spDefDv, int speedDv) {
		if (gen.num > 2) {
			return ((((hpDv & 1) + 2 * (attDv & 1) + 4 * (defDv & 1) + 8 * (speedDv & 1) +
					16 * (spAttDv & 1) + 32 * (spDefDv & 1)) * 15) / 63) + 1;
		} else {
			return (4 * (attDv % 4) + (defDv % 4)) + 1;
		}
	}
	
	static byte hiddenPowerConfigurations[][] = null; 
	
	static public byte[] configurationForType(final int type, Gen gen) {
		if (hiddenPowerConfigurations == null) {
			hiddenPowerConfigurations = new byte[Type.Curse.ordinal()-1][];
		}
		
		if (type == 0 || type >= hiddenPowerConfigurations.length) {
			return null;
		}
		
		if (hiddenPowerConfigurations[type] == null) {
			if (gen.num > 2) {
				for (int i = 63; i >= 0; i--) {
					int gt = Type(gen, i & 1, (i & 2) >> 1, (i & 4) >> 2, (i & 8) >> 3, (i & 16) >> 4, (i & 32) >> 5);
					if (gt == type) {
						hiddenPowerConfigurations[type] = new byte[]{(byte) ((i & 1) != 0 ? 31 : 30), (byte) ((i & 2) != 0 ? 31 : 30),
								(byte) ((i & 4) != 0 ? 31 : 30), (byte) ((i & 8) != 0 ? 31 : 30),
								(byte) ((i & 16) != 0 ? 31 : 30), (byte) ((i & 32) != 0 ? 31 : 30)};
						break;
					}
				}
			} else {
				hiddenPowerConfigurations[type] = new byte[]{15, 15, 15, 15, 15, 15};
			}
		}
				
		return hiddenPowerConfigurations[type];
	}

	static public int power(Poke p) {
		if (p.gen().num > 2) {
			return ((((p.dv(0) % 4) >> 1) + 2 *((p.dv(1) % 4) >> 1) + 4 * ((p.dv(2) % 4) >> 1) +
					8 * ((p.dv(5) % 4) >> 1) + 16 * ((p.dv(3) % 4) >> 1) + 32 * ((p.dv(4) % 4) >> 1)) * 40) / 63 + 30;
		} else {
			return ((5 * (((p.dv(3) & 8) >> 3) + 2 * ((p.dv(5) & 8) >> 3) + 4 * ((p.dv(2) & 8) >> 3) +
					8 * ((p.dv(1) & 8) >> 3)) + (p.dv(3) % 4)) / 2 + 31);
		}
	}
}
