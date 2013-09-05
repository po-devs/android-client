package com.podevs.android.pokemononline.pokeinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.SparseArray;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;

public class ItemInfo {
	private static SparseArray<String> itemNames = new SparseArray<String>();
	private static SparseArray<String> itemMessages = null;
	private static int usefulItems[] = null;
	
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
	
	public static int[] usefulItems() {
		if (usefulItems == null) {
			loadUsefulItems();
		}
		
		return usefulItems;
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
	
	private static void loadUsefulItems() {
		final ArrayList<Integer> items = new ArrayList<Integer>();
		
		InfoFiller.fill("db/items/item_useful.txt", new Filler() {
			public void fill(int i, String s) {
				items.add(Integer.valueOf(i));
			}
		});
		
		/* Sort item names */
		Collections.sort(items, new Comparator<Integer>() {
			public int compare(Integer lhs, Integer rhs) {
				return name(lhs).compareTo(name(rhs));
			}
		});
		
		usefulItems = new int[items.size()];
		
		for (int i = 0; i < items.size(); i++) {
			usefulItems[i] = items.get(i).intValue();
		}
	}
}
