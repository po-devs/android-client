package com.podevs.android.pokemononline.pokeinfo;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;

import android.util.SparseArray;

public class ItemInfo {
	private static SparseArray<String> itemNames = new SparseArray<String>();
	private static SparseArray<String> itemMessages = null;
	
	public static String name(int item) {
		if (itemNames.indexOfKey(item) < 0) {
			loadItemNames();
		}
		
		return itemNames.get(item);
	}
	
	public static String message(int num, int part) {
		if (itemMessages == null) {
			 loadItemMessages();
		}
		
		String parts [] = ((String)itemMessages.get(num, "")).split("\\|");
		try {
			return parts[part];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return "";
		}
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
	
	private static void loadItemMessages() {
		itemMessages = new SparseArray<String>();
		InfoFiller.fill("db/items/item_messages.txt", new Filler() {
			public void fill(int i, String b) {
				itemMessages.put(i, b);
			}
		});
		InfoFiller.fill("db/items/berry_messages.txt", new Filler() {
			public void fill(int i, String b) {
				itemMessages.put(8000+i, b);
			}
		});
	}
}
