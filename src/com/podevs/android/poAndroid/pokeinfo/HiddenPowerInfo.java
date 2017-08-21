package com.podevs.android.poAndroid.pokeinfo;

import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.poAndroid.poke.Poke;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo.Type;

public class HiddenPowerInfo {
	static public int Type(Poke p) {
		return Type(p.gen(), p.dv(0), p.dv(1), p.dv(2), p.dv(3), p.dv(4), p.dv(5));
	}

	static private int Type(Gen gen, int hpDv, int attDv, int defDv, int spAttDv, int spDefDv, int speedDv) {
		if (gen.num > 2) {
			return ((((hpDv & 1) + 2 * (attDv & 1) + 4 * (defDv & 1) + 8 * (speedDv & 1) +
					16 * (spAttDv & 1) + 32 * (spDefDv & 1)) * 15) / 63) + 1;
			// Odd = 1, Even = 0
			// return (Integer.parseInt("" + (spDefDv & 1) + (spAttDv & 1) + (speedDv & 1) + (defDv & 1) + (attDv & 1) + (hpDv & 1), 2)*15)/63; // Interesting way
		} else if (gen.num == 2) {
			return (4 * (attDv % 4) + (defDv % 4)) + 1;
			// Gen 2
			// 4*(Attack mod 4) + (Defense mod 4) + 1;
		} else {
			return 0;
		}
	}

	static byte hiddenPowerConfigurations[][][] = null;

	static public byte[] configurationForType(final int type, Gen gen) {
		if (hiddenPowerConfigurations == null) {
			hiddenPowerConfigurations = new byte[2][Type.Curse.ordinal()-1][];
		}

		if (type == 0 || type >= Type.Curse.ordinal()-1) {
			return null;
		}

		if (hiddenPowerConfigurations[1][type] == null) {
			if (gen.num > 2) {
				for (int i = 63; i >= 0; i--) {
					int gt = Type(gen, i & 1, (i & 2) >> 1, (i & 4) >> 2, (i & 8) >> 3, (i & 16) >> 4, (i & 32) >> 5);
					if (gt == type) {
						hiddenPowerConfigurations[1][type] = new byte[]{(byte) ((i & 1) != 0 ? 31 : 30), (byte) ((i & 2) != 0 ? 31 : 30),
								(byte) ((i & 4) != 0 ? 31 : 30), (byte) ((i & 8) != 0 ? 31 : 30),
								(byte) ((i & 16) != 0 ? 31 : 30), (byte) ((i & 32) != 0 ? 31 : 30)};
						break;
					}
				}
			} else if (gen.num == 2) {
				for (int att = 15; att >= 12; att--) {
					for (int def = 15; def >= 12; def--) {
						int gt = Type(gen, 15, att, def, 15, 15, 15);
						if (gt == type) {
							hiddenPowerConfigurations[0][type] = new byte[] {15, (byte) att, (byte) def, 15, 15, 15};
							break;
						}
					}
				}
			} else if (gen.num < 2) {
				return new byte[] {31, 31, 31, 31, 31, 31};
			}
		}
		return hiddenPowerConfigurations[(gen.num > 2 ? 1 : 0)][type];
	}

	static public byte [][] possibilitiesForType(final int type, Gen gen) {
		byte[][] ret = new byte[0][];
		int size = 0;

		for (int i = 63; i >= 0; i--) {
			int gt = Type(gen, i & 1, (i & 2)/2, (i & 4)/4, (i & 8)/8, (i & 16)/16, (i & 32)/32);
			if (gt == type) {
				byte [][] temp = ret.clone();
				ret = new byte[size+1][];
				System.arraycopy(temp, 0, ret, 0, temp.length);
				ret[size] = new byte[] {(byte)((i&1)+30), (byte)(((i & 2)/2)+30), (byte)(((i & 4)/4)+30), (byte)(((i & 8)/8)+30), (byte)(((i & 16)/16)+30), (byte)(((i & 32)/32)+30)};
				size++;
			}
		}

		return ret;
	}

	private static int floor(int i) {
		return (int) Math.floor((double) i);
	}

	static public int power(Poke p) {
		return power(p.gen(), p.dv(0), p.dv(1), p.dv(2), p.dv(3), p.dv(4), p.dv(5));
	}

	static private int power(Gen gen, int hpDv, int attDv, int defDv, int spAttDv, int spDefDv, int speedDv) {
		if (gen.num >= 6) {
            // Gen 6 made it always 60
            return 60;
        } else
        if (gen.num >= 3) {
			return floor(((((hpDv % 4) >> 1) + 2 *((attDv % 4) >> 1) + 4 * ((defDv % 4) >> 1) +
					8 * ((speedDv % 4) >> 1) + 16 * ((spAttDv % 4) >> 1) + 32 * ((spDefDv % 4) >> 1)) * 40) / 63 + 30);
			// (HP + 2*Attack + 4*Defense + 8*Speed + 16*Special Attack + 32*Special Defense)x40/63 + 30
			// Values are second least significant bit of each IV. Also mod 4.
			// 0 HP
			// 1 Attack
			// 2 Defense
			// 3 Special Attack
			// 4 Special Defense
			// 5 Speed

		} else {
			return floor((5 * (((spAttDv & 8) >> 3) + 2 * ((speedDv & 8) >> 3) + 4 * ((defDv & 8) >> 3) +
					8 * ((attDv & 8) >> 3)) + (defDv % 4)) / 2 + 31);
			// (5*(Special + 2*Speed + 4*Defense + 8*Attack) + Special mod 4)/2 + 31
			// Values are most significant bit of each IV. Also (IV < 8 ? 0 : 1). except Special mod 4
			// Gen 2
			// 0 HP
			// 1 Attack
			// 2 Defense
			// 3 Special Stat = 4
			// 4 Unused Special Stat = 3
			// 5 Speed
		}
	}
}
