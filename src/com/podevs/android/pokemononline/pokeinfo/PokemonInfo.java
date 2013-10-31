package com.podevs.android.pokemononline.pokeinfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.podevs.android.pokemononline.poke.Gen;
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
		short ability[] = null;
		short moves[] = null;
		String moveString = null;
		byte stats[] = null;
	}
	
	/* Global data:
	 * base stats, weight, height, ...
	 */
	private static class PokeData {
		byte maxForme = 0;
		byte gender = 0;
	}
	
	public static String name(UniqueID uID) {
		int num = uID.hashCode();
		
		loadPokeNames();
		
		return pokeNames.get(num);
	}
	
	public static int type1(UniqueID uID, int gen) {
		testLoad(gen);
		
		int type = pokemons[gen].get(uID.hashCode()).type1;
		if (type == -1) {
			type = pokemons[gen].get(uID.originalHashCode()).type1;
		}
		return type;
	}
	
	public static int type2(UniqueID uID, int gen) {
		testLoad(gen);
		
		int type = pokemons[gen].get(uID.hashCode()).type2;
		if (type == -1) {
			type = pokemons[gen].get(uID.originalHashCode()).type2;
		}
		return type;
	}
	
	public static int stat(UniqueID uId, int stat, int gen) {
		loadStats(gen);
		byte stats[] = pokemons[gen].get(uId.hashCode()).stats;
		
		if (stats == null) {
			stats = pokemons[gen].get(uId.originalHashCode()).stats;
		}
		return (stats[stat] + 256) % 256;
	}
	
	public static short[] moves(UniqueID uId, int gen) {
		testLoadMoves(gen);
		convertMoveStringIfNeeded(uId, gen);
		
		if (pokemons[gen].get(uId.hashCode()).moves == null && uId.subNum != 0) {
			return moves(uId.original(), gen);
		}
		
		short ret[] = pokemons[gen].get(uId.hashCode()).moves;
		
		if (ret == null) {
			return new short[0];
		} else {
			return ret;
		}
	}
	
	private static void convertMoveStringIfNeeded(UniqueID uId, int gen) {
		PokeGenData poke = pokemons[gen].get(uId.hashCode());
		if (poke.moveString != null) {
			String moves[] = poke.moveString.split(" ");
			
			Arrays.sort(moves, new Comparator<String>() {
				public int compare(String lhs, String rhs) {
					return MoveInfo.name(Integer.parseInt(lhs)).compareTo(MoveInfo.name(Integer.parseInt(rhs)));
				}
			});
			
			poke.moves = new short[moves.length];
			
			for (int i = 0; i < moves.length; i++) {
				poke.moves[i] = (short)Integer.parseInt(moves[i]);
			}
			poke.moveString = null;
		}
	}

	private static void testLoadMoves(final int gen) {
		testLoad(gen);
		
		if (pokemons[gen].get(1).moveString != null || pokemons[gen].get(1).moves != null) {
			return;
		}
		
		InfoFiller.uIDfill("db/pokes/" + gen + "G/all_moves.txt", new Filler() {
			public void fill(int i, String s) {
				pokemons[gen].get(i).moveString = s;
			}
		});
	}

	public static int calcStat(Poke poke, int stat, int gen) {
		if (stat == Stats.Hp.ordinal()) {
			return ((2*stat(poke.uID(), stat, gen) + poke.dv(stat) * (1 + (gen <= 2 ? 1 : 0) ) + poke.ev(stat)/4)*poke.level())/100 + 5
					+ 5 + poke.level();
		} else {
			int base = ((2*stat(poke.uID(), stat, gen) + poke.dv(stat) * (1 + (gen <= 2 ? 1 : 0) ) + poke.ev(stat)/4)*poke.level())/100 + 5;
			
			return NatureInfo.boostStat(base, poke.nature(), stat);
		}
	}
	
	private static void loadStats(final int gen) {
		testLoad(gen);
		if (pokemons[gen].get(1).stats != null) {
			return;
		}
		InfoFiller.uIDfill("db/pokes/" + gen + "stats.txt", new Filler() {
			public void fill(int i, String s) {
				byte [] stats = new byte[6];
				int curIndex = 0;
				
				/* faster than using a split (using test project to measure times) */
				for (int j = 0; j < 6; j++) {
					int nextIndex = s.indexOf(' ', curIndex);
					stats[j] = (byte)Integer.parseInt(s.substring(curIndex, nextIndex == - 1 ? s.length() : nextIndex));
					curIndex = nextIndex+1;
				}
				pokemons[gen].get(i).stats = stats;
			}
		});
	}

	public static Drawable icon(UniqueID uid) {
		return InfoConfig.context.getResources().getDrawable(iconRes(uid));
	}
	
	public static int iconRes(UniqueID uid) {
		Resources resources = InfoConfig.context.getResources();
		int resID = resources.getIdentifier("pi_" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum), "drawable", InfoConfig.pkgName);
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum,	"drawable", InfoConfig.pkgName);
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
	private static void testLoad(int gen) {
		if (pokemons == null) {
			pokemons = new SparseArray[Gen.maxGen+1];
		}
		if (pokemons[gen] == null) {
			pokemons[gen] = new SparseArray<PokemonInfo.PokeGenData>();
		}
		if (pokemons[gen].indexOfKey(1) == (byte)-1) {
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

	public static short[] abilities(UniqueID id, int gen) {
		testLoadAbilities(id, gen);
		
		short abilities[] = pokemons[gen].get(id.hashCode()).ability;
		
		if (abilities == null) {
			abilities = pokemons[gen].get(id.originalHashCode()).ability;
		}
		return abilities;
	}

	private static boolean abilitiesLoaded = false;
	private static void testLoadAbilities(UniqueID id, final int gen) {
		if (!abilitiesLoaded) {
			testLoad(gen);
			
			InfoFiller.uIDfill("db/pokes/" + gen  + "G/ability1.txt", new Filler() {
				public void fill(int i, String s) {
					pokemons[gen].get(i).ability = new short[3];
					pokemons[gen].get(i).ability[0] = Short.parseShort(s);
				}
			});
			InfoFiller.uIDfill("db/pokes/" + gen  + "G/ability2.txt", new Filler() {
				public void fill(int i, String s) {
					pokemons[gen].get(i).ability[1] = Short.parseShort(s);
				}
			});
			InfoFiller.uIDfill("db/pokes/" + gen  + "G/ability3.txt", new Filler() {
				public void fill(int i, String s) {
					pokemons[gen].get(i).ability[2] = Short.parseShort(s);
				}
			});
			
			abilitiesLoaded = true;
		}
	}

	public static UniqueID number(String name) {
		return namesToIds.get(name);
	}

	public static int gender(UniqueID uID) {
		testLoadGenders();
		return pokemonsg.get(uID.hashCode()).gender;
	}
	
	private static boolean gendersLoaded = false;
	private static void testLoadGenders() {
		if (!gendersLoaded) {
			loadPokeNames();
			gendersLoaded = true;
			
			InfoFiller.uIDfill("db/pokes/gender.txt", new FillerByte() {
				@Override
				void fillByte(int i, byte b) {
					pokemonsg.get(i).gender = b;
				}
			});
		}
	}
}
