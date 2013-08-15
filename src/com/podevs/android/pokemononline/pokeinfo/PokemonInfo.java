package com.podevs.android.pokemononline.pokeinfo;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.podevs.android.pokemononline.poke.Poke;
import com.podevs.android.pokemononline.poke.ShallowBattlePoke;
import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.FillerByte;
import com.podevs.android.pokemononline.pokeinfo.StatsInfo.Stats;

public class PokemonInfo {
	private static HashMap<String, UniqueID> namesToIds = null;
	private static SparseArray<String> pokeNames = null;
	private static SparseArray<PokeGenData> pokemons[] = null;
	private static SparseArray<PokeData> pokemonsg = null;
	private static int pokeCount = 0;

	/* Data depending on a gen:
	 * ability, type, evolutions, ...
	 */
	private static class PokeGenData {
		byte type1 = -1;
		byte type2 = -1;
	}
	
	/* Global data:
	 * base stats, weight, height, ...
	 */
	private static class PokeData {
		byte stats[] = null;
		byte maxForme = 0;
	}
	
	public static String name(UniqueID uID) {
		int num = uID.hashCode();
		
		loadPokeNames();
		
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
	
	public static int stat(UniqueID uId, int stat) {
		loadStats();
		byte stats[] = pokemonsg.get(uId.hashCode()).stats;
		
		if (stats == null) {
			stats = pokemonsg.get(uId.originalHashCode()).stats;
		}
		return stats[stat];
	}
	
	public static int calcStat(Poke poke, int stat, int gen) {
		if (stat == Stats.Hp.ordinal()) {
			return ((2*stat(poke.uID(), stat) + poke.dv(stat) * (1 + (gen <= 2 ? 1 : 0) ) + poke.ev(stat)/4)*poke.level())/100 + 5
					+ 5 + poke.level();
		} else {
			return ((2*stat(poke.uID(), stat) + poke.dv(stat) * (1 + (gen <= 2 ? 1 : 0) ) + poke.ev(stat)/4)*poke.level())/100 + 5;
		}
	}
	
	private static boolean statsLoaded = false;
	private static void loadStats() {
		loadPokeNames();
		if (statsLoaded) {
			return;
		}
		statsLoaded = true;
		InfoFiller.uIDfill("db/pokes/stats.txt", new Filler() {
			public void fill(int i, String s) {
				byte [] stats = new byte[6];
				int curIndex = 0;
				
				/* faster than using a split (using test project to measure times) */
				for (int j = 0; j < 6; j++) {
					int nextIndex = s.indexOf(' ', curIndex);
					stats[j] = (byte)Integer.parseInt(s.substring(curIndex, nextIndex == - 1 ? s.length() : nextIndex));
					curIndex = nextIndex+1;
				}
				pokemonsg.get(i).stats = stats;
			}
		});
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
	
	public static int iconRes(UniqueID uid) {
		Resources resources = InfoConfig.context.getResources();
		int resID = resources.getIdentifier("pi" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum) +
				"_icon", "drawable", InfoConfig.pkgName);
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum + "_icon",
					"drawable", InfoConfig.pkgName);
		return resID;
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
		if (pokeNames != null) {
			return;
		}
		pokeNames = new SparseArray<String>();
		pokemonsg = new SparseArray<PokemonInfo.PokeData>();
		namesToIds = new HashMap<String, UniqueID>();
		InfoFiller.uIDfill("db/pokes/pokemons.txt", new Filler() {
			public void fill(int i, String s) {
				pokeNames.put(i, s);
				pokemonsg.put(i, new PokeData());
				
				if (i > 16000) {
					pokemonsg.get(i % 65536).maxForme = (byte) (i >> 16);
				} else {
					pokeCount = i;
				}
				namesToIds.put(s, new UniqueID(i));
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

	public static int numberOfPokemons() {
		loadPokeNames();
		
		return pokeCount;
	}
	
	public static int totalNumberOfPokemons() {
		loadPokeNames();
		
		return pokeNames.size();
	}

	public static String[] nameArray() {
		return namesToIds.keySet().toArray(new String[namesToIds.size()]);
	}
}
