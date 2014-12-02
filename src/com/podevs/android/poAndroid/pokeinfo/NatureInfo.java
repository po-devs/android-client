package com.podevs.android.poAndroid.pokeinfo;

import com.podevs.android.poAndroid.pokeinfo.StatsInfo.Stats;



public class NatureInfo {

	public enum Nature {
		Hardy,
		Lonely,
		Brave,
		Adamant,
		Naughty,
		Bold,
		Docile,
		Relaxed,
		Impish,
		Lax,
		Timid,
		Hasty,
		Serious,
		Jolly,
		Naive,
		Modest,
		Mild,
		Quiet,
		Bashful,
		Rash,
		Calm,
		Gentle,
		Sassy,
		Careful,
		Quirky
	}

	public static String name(int num) {
		return Nature.values()[num].toString();
	}

	public static int count() {
		return Nature.Quirky.ordinal()+1;
	}

	public static int convertToStat(int num) {
		switch(num) {
		case 0: return Stats.Hp.ordinal();
		case 1: return Stats.Attack.ordinal();
		case 2: return Stats.Defense.ordinal();
		case 3: return Stats.Speed.ordinal();
		case 4: return Stats.SpAttack.ordinal();
		case 5: default: return Stats.SpDefense.ordinal();
		}
	}

	public static int boosted (int num) {
		return convertToStat((num / 5) + 1);
	}

	public static int hindered (int num) {
		return convertToStat((num % 5) + 1);
	}

	public static CharSequence boostedName(int i) {
		int boosted = boosted(i);
		int hindered = hindered(i);

		if (boosted == hindered) {
			return name(i);
		} else {
			return name(i) + " (+" + StatsInfo.Shortcut(boosted) + ", -" + StatsInfo.Shortcut(hindered) + ")";
		}
	}

	public static int boostStat(int base, int nature, int stat) {
		int boost = (0 + boosted(nature) == stat ? 1 : 0) + (hindered(nature) == stat ? -1 : 0);
		return base * (10+boost)/10;
	}
}
