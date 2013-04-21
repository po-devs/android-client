package com.podevs.android.pokemononline.pokeinfo;

import android.util.SparseArray;

import com.podevs.android.pokemononline.DataBaseHelper;
import com.podevs.android.pokemononline.poke.UniqueID;

public class PokemonInfo {
	private static SparseArray<String> pokeNames = new SparseArray<String>();
	
	public static String name(DataBaseHelper db, UniqueID uID) {
		int num = uID.hashCode();
		
		if (pokeNames.indexOfKey(num) >= 0) {
			return pokeNames.get(num);
		}
		
		String name = db.query("SELECT name FROM [pokemons] WHERE Num = " + uID.pokeNum + " AND Forme = " + uID.subNum);
		pokeNames.put(num, name);
		
		return name;
	}
}
