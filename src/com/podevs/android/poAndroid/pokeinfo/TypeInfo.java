package com.podevs.android.poAndroid.pokeinfo;


public class TypeInfo {
	public enum Type {
		Normal, //0
		Fighting, //1
		Flying, //2
		Poison, //3
		Ground, //4
		Rock, //5
		Bug, //6
		Ghost, //7
		Steel, //8
		Fire, //9
		Water, //10
		Grass, //11
		Electric, //12
		Psychic, //13
		Ice, //14
		Dragon, //15
		Dark, //16
		Fairy, //17
		Curse //18
	}

	public static String name(int num) {
		return Type.values()[num].toString();
	}

	public static int typeRes(int num) {
		return InfoConfig.context.getResources().getIdentifier("type" + num, "drawable", InfoConfig.pkgName);
	}


	private static byte effectiveness[][][];
	public static void loadTypeEffectiveness() {
		if (effectiveness == null) {
			effectiveness = new byte[GenInfo.genMax()][Type.values().length][Type.values().length];
			for (int i = 0; i < effectiveness.length; i++) {
				String path = "db/types/" + (i + 1) + "G/typestable.txt";
				final int g = i;
				InfoFiller.fill(path, new InfoFiller.Filler() {
					@Override
					public void fill(int k, String s) {
						String[] split = s.split(" ");
						for (int j = 0; j < split.length; j++) {
							effectiveness[g][k][j] = Byte.parseByte(split[j]);
						}
					}
				});
			}
		}
	}

	public int getEffectiveness(int in, byte gen, byte type1, byte type2) {
		loadTypeEffectiveness();
		/*
		 * 0 = No effect
		 * 1 = Half Damage
		 * 2 = Normal Damage
		 * 4 = Double Damage
		 */
		switch (effectiveness[gen-1][type1][type2]) {
			case 0: return 0;
			case 1: return in/2;
			case 2: return in;
			case 4: return in*2;
		}
		return -1;
	}

	public int getEffectiveness(int in, byte gen, byte type1, byte type2, byte type3) {
		loadTypeEffectiveness();
		/*
		 * 0 * 0 = 0 No effect
		 * 0 * 1 = 0 No effect
		 * 0 * 2 = 0 No effect
		 * 0 * 4 = 0 No effect
		 *
		 * 1 * 1 = 1 1/4x Damage
		 * 1 * 2 = 2 1/2x Damage
		 * 1 * 4 = 4 Normal Damage
		 *
		 * 2 * 2 = 4 Normal Damage
		 * 2 * 4 = 8 2x damage
		 *
		 * 4 * 4 = 16 4x damage
		 */
		switch (effectiveness[gen-1][type1][type2] * effectiveness[gen-1][type1][type3]) {
			case 0: return 0;
			case 1: return in/4;
			case 2: return in/2;
			case 4: return in;
			case 8: return in*2;
			case 16: return in*4;
		}
		return -1;
	}
}
