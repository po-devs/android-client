package com.podevs.android.poAndroid.pokeinfo;

import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.pokeinfo.StatsInfo.Stats;
import com.podevs.android.poAndroid.registry.RegistryActivity;

public class NatureInfo {
	private static String[] natures = null;

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

	public static int indexOf(String s) {
		return Nature.valueOf(s).ordinal();
	}

	public static String name(int num) {
		if (natures == null) {
			loadNatureNames();
		}

		return natures[num];
		//return Nature.values()[num].toString();
	}

	public static int count() {
		return Nature.Quirky.ordinal()+1;
	}

	private static int convertToStat(int num) {
		switch(num) {
		case 0: return Stats.Hp.ordinal();
		case 1: return Stats.Attack.ordinal();
		case 2: return Stats.Defense.ordinal();
		case 3: return Stats.Speed.ordinal();
		case 4: return Stats.SpAttack.ordinal();
		case 5: default: return Stats.SpDefense.ordinal();
		}
	}

	private static int boosted (int num) {
		return convertToStat((num / 5) + 1);
	}

	private static int hindered (int num) {
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

	static int boostStat(int base, int nature, int stat) {
		int boost = (0 + boosted(nature) == stat ? 1 : 0) + (hindered(nature) == stat ? -1 : 0);
		return base * (10+boost)/10;
	}

	private static void loadNatureNames() {
		natures = new String[Nature.values().length];
		String path;
		if (RegistryActivity.localize_assets) {
			path = "db/natures/" + InfoConfig.resources.getString(R.string.asset_localization) + "nature.txt";
			if (!InfoConfig.fileExists(path)) {
				path = "db/natures/nature.txt";
			}
		} else {
			path = "db/natures/nature.txt";
		}
		InfoFiller.fill(path, new InfoFiller.Filler() {
			int index = 0;
			public void fill(int i, String s) {
				natures[index] = s;
				index++;
			}
		});
	}
}
