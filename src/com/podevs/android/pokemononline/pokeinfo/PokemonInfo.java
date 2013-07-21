package com.podevs.android.pokemononline.pokeinfo;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.podevs.android.pokemononline.poke.ShallowBattlePoke;
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
	
	public static Drawable icon(UniqueID uid) {
		Resources resources = InfoConfig.context.getResources();
		int resID = resources.getIdentifier("pi" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum) +
				"_icon", "drawable", InfoConfig.pkgName);
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum + "_icon",
					"drawable", InfoConfig.pkgName);
		return resources.getDrawable(resID);
	}
	
	public static String sprite(ShallowBattlePoke poke, boolean front) {
        String res;
    	UniqueID uID;
    	if (poke.specialSprites.isEmpty())
    		uID = poke.uID;
    	else
    		uID = poke.specialSprites.peek();
    	if (uID.pokeNum < 0)
    		res = "empty_sprite";
    	else {
        	res = "p" + uID.pokeNum + (uID.subNum == 0 ? "" : "_" + uID.subNum) +
        			(front ? "_front" : "_back");
    		if (InfoConfig.resources.getIdentifier(res + "f", "drawable", InfoConfig.pkgName) != 0)
    			// Special female sprite
    			res = res + "f" + (poke.shiny ? "s" : "");
    	}
        System.out.println("SPRITE: " + res);
        int ident = InfoConfig.resources.getIdentifier(res, "drawable", InfoConfig.pkgName);
        if (ident == 0)
        	// No sprite found. Default to missingNo.
        	return "missingno.png";
        else
        	return res + ".png";
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
