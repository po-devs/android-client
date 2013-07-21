package com.podevs.android.pokemononline.pokeinfo;

import android.util.SparseArray;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;

public class AbilityInfo {
	private static SparseArray<String> abilityNames = new SparseArray<String>();
	
	public static String name(int item) {
		if (abilityNames.indexOfKey(item) < 0) {
			loadAbilityNames();
		}
		
		return abilityNames.get(item);
	}

	private static void loadAbilityNames() {
		InfoFiller.fill("db/abilities/abilities.txt", new Filler() {
			public void fill(int i, String b) {
				abilityNames.put(i, b);
			}
		});
	}
}
