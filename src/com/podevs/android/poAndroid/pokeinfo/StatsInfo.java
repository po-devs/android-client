package com.podevs.android.poAndroid.pokeinfo;

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
	
	static public String Shortcut(int pos) {
		return shortcuts[pos];
	}
}
