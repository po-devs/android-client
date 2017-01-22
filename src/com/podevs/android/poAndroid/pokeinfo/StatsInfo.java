package com.podevs.android.poAndroid.pokeinfo;

import com.podevs.android.poAndroid.R;

public class StatsInfo {
	public enum Stats {
		Hp, //0
		Attack, //1
		Defense, //2
		SpAttack, //3
		SpDefense, //4
		Speed //5
	}
	
	static private String[] shortcuts = {
		"HP",
		"Att",
		"Def",
		"SpA",
		"SpD",
		"Spe"
	};

	static private int[] resShortcuts = {
			R.string.stat_hp_short,
			R.string.stat_attack_short,
			R.string.stat_defense_short,
			R.string.stat_spAttack_short,
			R.string.stat_spDefense_short,
			R.string.stat_speed_short
	};
	
	static public String Shortcut(int pos) {
		return shortcuts[pos];
	}

	static public int ShortcutRes(int pos) {
		return resShortcuts[pos];
	}
}
