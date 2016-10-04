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

	public byte getEffectiveness(byte gen, byte type1, byte type2) {
		loadTypeEffectiveness();
		return effectiveness[gen-1][type1][type2];
	}

	public byte getEffectiveness(byte gen, byte type1, byte type2, byte type3) {
		loadTypeEffectiveness();
		return (byte) (effectiveness[gen-1][type1][type2] * effectiveness[gen-1][type1][type3]);
	}
}
