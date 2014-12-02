package com.podevs.android.poAndroid.pokeinfo;

import java.util.ArrayList;

import android.util.SparseArray;

import com.podevs.android.poAndroid.pokeinfo.InfoFiller.Filler;

public class AbilityInfo {
	private static ArrayList<String> abilityNames = null;
	private static SparseArray<String> abilityMessages = null;
	
	public static String name(int item) {
		if (abilityNames == null) {
			loadAbilityNames();
		}
		
		return abilityNames.get(item);
	}
	
	public static String message(int num, int part) {
		if (abilityMessages == null) {
			 loadAbilityMessages();
		}
		
		String parts [] = ((String)abilityMessages.get(num, "")).split("\\|");
		try {
			return parts[part];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return "";
		}
	}

	private static void loadAbilityNames() {
		abilityNames = new ArrayList<String>();
		
		InfoFiller.fill("db/abilities/abilities.txt", new Filler() {
			public void fill(int i, String b) {
				abilityNames.add(b);
			}
		});
	}
	
	private static void loadAbilityMessages() {
		abilityMessages = new SparseArray<String>();
		InfoFiller.fill("db/abilities/ability_messages.txt", new Filler() {
			public void fill(int i, String b) {
				abilityMessages.put(i, b);
			}
		});
	}
}
