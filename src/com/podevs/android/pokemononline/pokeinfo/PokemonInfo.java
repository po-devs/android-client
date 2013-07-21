package com.podevs.android.pokemononline.pokeinfo;

import android.util.SparseArray;

import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.FillerByte;

public class PokemonInfo {
	private static SparseArray<String> pokeNames = new SparseArray<String>();
	private static SparseArray<PokeGenData> pokemons[] = null;

	private static class PokeGenData {
		byte type1 = -1;
		byte type2 = -1;
	}
	
	public static String name(UniqueID uID) {
		int num = uID.hashCode();
		
		if (pokeNames.indexOfKey(num) < 0) {
			loadPokeNames();
		}
		
		return pokeNames.get(num);
	}
	
	public static int type1(UniqueID uID, int gen) {
		testLoad(uID, gen);
		
		int type = pokemons[gen].get(uID.hashCode()).type1;
		if (type == -1) {
			type = pokemons[gen].get(uID.originalHashCode()).type1;
		}
		return type;
	}
	
	public static int type2(UniqueID uID, int gen) {
		testLoad(uID, gen);
		
		int type = pokemons[gen].get(uID.hashCode()).type2;
		if (type == -1) {
			type = pokemons[gen].get(uID.originalHashCode()).type2;
		}
		return type;
	}
	
	@SuppressWarnings("unchecked")
	private static void testLoad(UniqueID uID, int gen) {
		if (pokemons == null) {
			pokemons = new SparseArray[6];
		}
		if (pokemons[gen] == null) {
			pokemons[gen] = new SparseArray<PokemonInfo.PokeGenData>();
		}
		if (pokemons[gen].indexOfKey(uID.hashCode()) == (byte)-1) {
			loadTypes(gen);
		}
	}

	private static void loadPokeNames() {
		InfoFiller.uIDfill("db/pokes/pokemons.txt", new Filler() {
			public void fill(int i, String s) {
				pokeNames.put(i, s);
			}
		});
	}
	
	private static void loadTypes(final int gen) {
		/* First load all the released pokemon and prepare the "data containers" */
		InfoFiller.uIDfill("db/pokes/" + gen  + "G/released.txt", new Filler() {
			public void fill(int i, String s) {
				pokemons[gen].put(i, new PokeGenData());
			}
		});
		InfoFiller.uIDfill("db/pokes/" + gen  + "G/type1.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				pokemons[gen].get(i).type1 = b;
			}
		});
		InfoFiller.uIDfill("db/pokes/" + gen  + "G/type2.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				pokemons[gen].get(i).type2 = b;
			}
		});
	}
}
