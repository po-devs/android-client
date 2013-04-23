package com.podevs.android.pokemononline.pokeinfo;

import android.util.SparseArray;

import com.podevs.android.pokemononline.DataBaseHelper;

public class MoveInfo {
	private static SparseArray<String> moveNames = new SparseArray<String>();
	
	public static String name(DataBaseHelper db, int num) {
		if (moveNames.indexOfKey(num) >= 0) {
			return moveNames.get(num);
		}
		
		String name = db.query("SELECT name FROM [Moves] WHERE _id = " + num);
		moveNames.put(num, name);
		
		return name;
	}
}
