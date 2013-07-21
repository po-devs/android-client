package com.podevs.android.pokemononline.pokeinfo;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;

import android.util.SparseArray;

public class ItemInfo {
	private static SparseArray<String> itemNames = new SparseArray<String>();
	
	public static String name(int item) {
		if (itemNames.indexOfKey(item) < 0) {
			loadItemNames();
		}
		
		return itemNames.get(item);
	}

	private static void loadItemNames() {
		InfoFiller.fill("db/items/items.txt", new Filler() {
			public void fill(int i, String b) {
				itemNames.put(i, b);
			}
		});
		InfoFiller.fill("db/items/berries.txt", new Filler() {
			public void fill(int i, String b) {
				itemNames.put(8000+i, b);
			}
		});
	}
}
