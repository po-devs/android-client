package com.podevs.android.poAndroid.pokeinfo;

import android.util.SparseArray;
import com.podevs.android.poAndroid.pokeinfo.InfoFiller.Filler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

	public static int indexOf(String s) {
		if (itemNames.indexOfKey(1) < 0) {
			loadItemNames();
		}

		for (int i = 0; i < itemNames.size(); i ++) {
			if (itemNames.get(itemNames.keyAt(i)).equals(s)) {
				return itemNames.keyAt(i);
			}
		}
		return 15;
	}

	public static int indexOf(int i) {
		return itemNames.keyAt(i);
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
	
	private static short plates[] = new short[] {
		0,
        188,
        196,
        201,
        187,
        199,
        192,
        198,
        193,
        189,
        197,
        194,
        202,
        195,
        191,
        185,
        186,
        330,
        0
	};
	
	public static short plateForType(int type) {
	    return plates[type];
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
		
		final ArrayList<Integer> berries = new ArrayList<Integer>();
		
		InfoFiller.fill("db/items/berries.txt", new Filler() {
			public void fill(int i, String s) {
				berries.add(8000+Integer.valueOf(i));
			}
		});
		
		/* Sort item names */
		Collections.sort(berries, new Comparator<Integer>() {
			public int compare(Integer lhs, Integer rhs) {
				return name(lhs).compareTo(name(rhs));
			}
		});
		
		usefulItems = new int[items.size()+berries.size()];

		for (int i = 0; i < items.size(); i++) {
			usefulItems[i] = items.get(i).intValue();
		}
		
		for (int i = 0; i < berries.size(); i++) {
			usefulItems[i+items.size()] = berries.get(i).intValue();
		}
	}

	public static int plateType(short item) {
		for (int i = 0; i < plates.length; i++) {
			if (item == plates[i]) {
				return i;
			}
		}
		return 0;
	}
}
