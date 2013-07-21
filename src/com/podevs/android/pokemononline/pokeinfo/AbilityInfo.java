package com.podevs.android.pokemononline.pokeinfo;

import java.util.Vector;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;

public class AbilityInfo {
	private static Vector<String> abilityNames = null;
	
	public static String name(int item) {
		if (abilityNames == null) {
			loadAbilityNames();
		}
		
		return abilityNames.get(item);
	}

	private static void loadAbilityNames() {
		abilityNames = new Vector<String>();
		
		InfoFiller.fill("db/abilities/abilities.txt", new Filler() {
			public void fill(int i, String b) {
				abilityNames.addElement(b);
			}
		});
	}
}
