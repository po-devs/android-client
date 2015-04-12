package com.podevs.android.poAndroid.pokeinfo;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.poAndroid.poke.Poke;
import com.podevs.android.poAndroid.poke.PokeEnums.Gender;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.InfoFiller.Filler;
import com.podevs.android.poAndroid.pokeinfo.InfoFiller.FillerByte;
import com.podevs.android.poAndroid.pokeinfo.StatsInfo.Stats;

import java.util.*;

/**
 * Static data used to store all information regarding pokemon at all times.
 * Uses caching to reduce read from disk time.
 *
 */

public class PokemonInfo {
	/**
	 * MashMap linking pokeNames to UniqueID
	 */
	private static HashMap<String, UniqueID> namesToIds = null;

	/**
	 * SpareArray of pokeNames
	 */
	private static SparseArray<String> pokeNames = null;
	private static SparseArray<PokeGenData> pokemons[] = null;
	private static SparseArray<PokeData> pokemonsg = null;
	private static int pokeCount = 0;

	/**
	 * Number of pokemon in a generation
	 */
	private static int pokeCountg[] = null;

	/**
	 *  Cache of images
	 */
	private static LruCache<String, Drawable> ImageCache = new LruCache<String, Drawable>(40);

	/**
	 * Pokemon Data object
	 */

	private static class PokeGenData {
		byte type1 = -1;
		byte type2 = -1;
		short ability[] = null;
		short moves[] = null;
		String moveString = null;
		byte stats[] = null;
	}

	/**
	 * Extra PokeData object
	 */

	private static class PokeData {
		byte maxForme = 0;
		byte gender = -1;
		String options = null;
	}

	/**
	 * indexOf method for pokeNames SparseArray
	 *
	 * @param pokemonName String to be find in list of pokemon names
	 * @return Index of pokemon name. Returns 0 if not found
	 */

	public static int indexOf(String pokemonName) {
		loadPokeNames();
		for (int i = 0; i < pokeNames.size(); i ++) {
			if (pokeNames.get(pokeNames.keyAt(i)).equals(pokemonName)) {
				return pokeNames.keyAt(i);
			} // pokeNames has random offsets, does it even need these?
		}
		return 0;
	}

	/**
	 * Name of pokemon by dex number
	 *
	 * @param uID UniqueID of pokemon
	 * @return Gets standard name from uID
	 */

	public static String name(UniqueID uID) {
		int num = uID.hashCode();

		loadPokeNames();

		return pokeNames.get(num);
	}

	/**
	 * Checks if
	 *
	 * @param uID UniqueID of pokemon
	 * @return true if pokemon has multiple formes in the teambuilder
	 */

	public static boolean hasVisibleFormes(UniqueID uID) {
		loadPokeNames();

		int num = uID.originalHashCode();
		PokeData data = pokemonsg.get(num);

		//Check the pokemon has formes, and that they are not hidden
		return data.maxForme > 0 && (data.options == null || data.options.indexOf('H') < 0);
	}

    public static boolean hasHackmonFormes(UniqueID uID) {
        loadPokeNames();

        int num = uID.originalHashCode();
        PokeData data = pokemonsg.get(num);

        return data.maxForme > 0;
    }

	/**
	 * Loads formes into list by uID
	 *
	 * @param uniqueID UniqueID of pokemon
	 * @param gen Generation to load
	 * @return list of formes by uID
	 */

	public static List<UniqueID> formes(UniqueID uniqueID, Gen gen) {
		loadPokeNames();
		testLoad(gen.num);

		ArrayList<UniqueID> ret = new ArrayList<UniqueID>();
		PokeData data = pokemonsg.get(uniqueID.originalHashCode());

		for (int i = 0; i <= data.maxForme; i++) {
			UniqueID generated = new UniqueID(uniqueID.pokeNum, i);
			if (exists(generated, gen)) {
				ret.add(generated);
			}
		}

		return ret;
	}

	/**
	 * Checks if uID is valid or exists.
	 *
	 * @param uID UniqueID to check
	 * @return true is uID exists
	 */

	public static boolean exists (UniqueID uID) {
		loadPokeNames();

		return pokeNames.get(uID.hashCode()) != null;
	}

	/**
	 * Number of pokemon in specified generation
	 *
	 * @param gen Generation
	 * @return Number of pokemon in Generation given
	 */

	public static int numberOfPokemons(Gen gen) {
		testLoad(gen.num);
		return pokeCountg[gen.num];
	}

	/**
	 *
	 * @param uID UniqueID to check
	 * @param gen Generation to check in
	 * @return true is exists
	 */

	public static boolean exists (UniqueID uID, Gen gen) {
		testLoad(gen.num);

		return exists(uID) && pokemons[gen.num].get(uID.hashCode()) != null;
	}

	/**
	 * Gets first type of Pokemon by gen
	 *
	 * @param uID Unique Id
	 * @param gen Gen to check in
	 * @return type of uID in gen
	 */

	public static int type1(UniqueID uID, int gen) {
		testLoad(gen);

		int type = -1;
		try {
			type = pokemons[gen].get(uID.hashCode()).type1;
			if (type == -1) {
				type = pokemons[gen].get(uID.originalHashCode()).type1;
			}
		} catch (NullPointerException e) {
			type = pokemons[gen].get((new UniqueID()).hashCode()).type1;
			Log.e("PokemonInfo", "NULL original uID:" + uID);
		}
		return type;
	}

	/**
	 * Gets second type of Pokemon by gen
	 *
	 * @param uID Unique Id
	 * @param gen Gen to check in
	 * @return type of uID in gen
	 */

	public static int type2(UniqueID uID, int gen) {
		testLoad(gen);

		int type = -1;
		try {
			type = pokemons[gen].get(uID.hashCode()).type2;
			if (type == -1) {
				type = pokemons[gen].get(uID.originalHashCode()).type2;
			}
		} catch (NullPointerException e) {
			type = pokemons[gen].get((new UniqueID()).hashCode()).type2;
			Log.e("PokemonInfo", "NULL original uID:" + uID);
		}
		return type;
	}

	/**
	 * Gets stat for Pokemon
	 *
	 * @param uId UniqueID
	 * @param stat 0-5
	 * @param gen gen
	 * @return stat of uID in gen
	 */

	public static int stat(UniqueID uId, int stat, int gen) {
		loadStats(gen);
		byte stats[] = pokemons[gen].get(uId.hashCode()).stats;

		if (stats == null) {
			stats = pokemons[gen].get(uId.originalHashCode()).stats;
		}
		return (stats[stat] + 256) % 256;
	}

	/**
	 * Gets moves for Pokemon
	 *
	 * @param uId UniqueID
	 * @param gen int generation
	 * @param sub sub generation
	 * @return short[] of moves for uID
	 */

	public static short[] moves(UniqueID uId, int gen, int sub) {
		testLoadMoves(gen, sub);
		convertMoveStringIfNeeded(uId, gen);

		if (pokemons[gen].get(uId.hashCode()).moves == null && uId.subNum != 0) {
			return moves(uId.original(), gen, sub);
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

	/**
	 * Ensures data pokemons array is filled
	 *
	 * @param gen int generation
	 * @param sub sub generation
	 */

	private static void testLoadMoves(final int gen, final int sub) {
		testLoad(gen);
		if (pokemons[gen].get(1).moveString != null || pokemons[gen].get(1).moves != null) {
			return;
		}
		String test;
		if (gen == 6 && sub == 1) {
			test = "db/pokes/6G/Subgen 1/all_moves.txt";
			} else {
			test = "db/pokes/" + gen + "G/all_moves.txt";
		}
		/*
		String path = "db/pokes/" + gen + "G/Subgen " + sub + "all_moves.txt";

		 */
		InfoFiller.uIDfill(test, new Filler() {
			public void fill(int i, String s) {
				PokeGenData poke = pokemons[gen].get(i);
				if (poke != null) {
					pokemons[gen].get(i).moveString = s;
				}
			}
		});
	}

	/**
	 * A temporary method to account for move changes between XY and ORAS.
	 */

	public static void resetGen6() {
		pokemons[6] = null;
	}

	/**
	 * Calculate the specified stat of a known pokemon in a specified generation (You?)
	 *
	 * @param poke Pokemon to check
	 * @param stat Which stat 0-5
	 * @param gen int generation
	 * @return stat
	 */

	public static int calcStat(Poke poke, int stat, int gen) {
		if (stat == Stats.Hp.ordinal()) {
			return ((2*stat(poke.uID(), stat, gen) + poke.dv(stat) * (1 + (gen <= 2 ? 1 : 0) ) + poke.ev(stat)/4)*poke.level())/100 + 5 + 5 + poke.level();
		} else {
			int base = ((2*stat(poke.uID(), stat, gen) + poke.dv(stat) * (1 + (gen <= 2 ? 1 : 0) ) + poke.ev(stat)/4)*poke.level())/100 + 5;

			return NatureInfo.boostStat(base, poke.nature(), stat);
		}
	}

	/**
	 * Calculate the specified stat of an unknown pokemon in a specified generation (Enemy?)
	 *
	 * @param ID Unique ID
	 * @param stat which stat 0-5
	 * @param gen int generation
	 * @param level pokemon level
	 * @param minmax 0 minimized, 1 maximized.
	 * @return calculated stat
	 */

	public static int calcMinMaxStat(UniqueID ID, int stat, int gen, int level, int minmax) {
		if (stat == 0) {
			return (int) Math.floor(((2*stat(ID, stat, gen) + (minmax == 1 ? 31 : 0) * (1 + (gen <= 2 ? 1 : 0) ) + (minmax == 1 ? 252 : 0)/4)*level)/100 + 5 + 5 + level);
		} else {
			double base = Math.floor(((2*stat(ID, stat, gen) + (minmax == 1 ? 31 : 0) * (1 + (gen <= 2 ? 1 : 0) ) + (minmax == 1 ? 252 : 0)/4)*level)/100 + 5);
			return (int) Math.floor(base + (minmax == 1 ? 1 : -1)*(base/10));
		}
	}

	/**
	 * Loads stats for generation
	 *
	 * @param gen int generation
	 */

	private static void loadStats(final int gen) {
		testLoad(gen);
		if (pokemons[gen].get(1).stats != null) {
			return;
		}
		InfoFiller.uIDfill("db/pokes/" + gen + "G/stats.txt", new Filler() {
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

	/**
	 * @param uid Unique
	 * @return resource identifier of uid
	 */

	private static int iconRes(UniqueID uid) {
		Resources resources = InfoConfig.resources;
		int resID = resources.getIdentifier("pi_" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum), "drawable", InfoConfig.pkgName);
		if (resID == 0)
			resID = resources.getIdentifier("pi_" + uid.pokeNum, "drawable", InfoConfig.pkgName);
		return resID;
	}

	/**
	 * Attempts to get Drawable of a UniqueID. Handles missing resource errors
	 *
	 * @param uid UniqueID
	 * @return Drawable of uid
	 */

	public static Drawable iconDrawable(UniqueID uid) {
		try {
			return InfoConfig.resources.getDrawable(iconRes(uid));
		} catch (Resources.NotFoundException e) {
			return InfoConfig.resources.getDrawable(iconRes(uid.original()));
		} catch (NullPointerException e) {
			Log.e("PokemonInfo", "NULL ICON" + uid);
			return InfoConfig.resources.getDrawable(iconRes(new UniqueID()));
		}
	}

	/**
	 * For BattleActivity to return a pokeball instead of a missigno icon. Implements caching
	 *
	 * @return drawable of "pi_status.png"
	 */

	public static Drawable iconDrawablePokeballStatus() {
		String iconKey = "pi_status";
		Drawable drawable = ImageCache.get(iconKey);
		if (drawable == null) {
			int i = InfoConfig.resources.getIdentifier(iconKey, "drawable", InfoConfig.pkgName);
			drawable = InfoConfig.resources.getDrawable(i);
			ImageCache.put(iconKey, drawable);
		}
		return drawable.mutate().getConstantState().newDrawable();
	}

	/**
	 * Returns drawable of specified UniqueID. Implements caching
	 *
	 * @param uid UniqueID
	 * @return drawable of uid
	 */

	public static Drawable iconDrawableCache(UniqueID uid) {
		String iconKey = "pi" + uid.toString();
		Drawable drawable = ImageCache.get(iconKey);
		if (drawable == null) {
			drawable = iconDrawable(uid);
			ImageCache.put(iconKey, drawable);
		}
		return drawable.mutate().getConstantState().newDrawable();
	}

	/**
	 * Get drawable from resources.
	 *
	 * @throws android.content.res.Resources.NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @param itemId Local resource name.
	 * @return Local drawable.
	 */

	private static Drawable itemDrawable(String itemId) {
		Resources resource = InfoConfig.resources;
		Integer identifier = resource.getIdentifier(itemId, "drawable", InfoConfig.pkgName);
		return resource.getDrawable(identifier);
	}

	/**
	 * Attempts to get Drawable from Cache, if not it reads from file then writes it to cache.
	 *
	 *
	 * @param itemId
	 * @return Drawable from cache or directly from local.
	 */

    public static Drawable itemDrawableCache(Integer itemId) {
		String itemKey = "i" + itemId.toString();
		Drawable drawable =  ImageCache.get(itemKey);
		if (drawable == null) {
			drawable = itemDrawable(itemKey);
			ImageCache.put(itemKey, drawable);
		}
		return drawable;
	}

	/**
	 * Get drawable from resources.
	 *
	 * @throws android.content.res.Resources.NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @param key Local resource name.
	 * @return Local drawable.
	 */

	private static Drawable genderDrawable(String key) {
		Resources resource = InfoConfig.resources;
		Integer identifier = resource.getIdentifier(key, "drawable", InfoConfig.pkgName);
		return resource.getDrawable(identifier);
	}


	/**
	 * Attempts to get Drawable from Cache, if not it reads from file then writes it to cache.
	 *
	 * @param gender 0, 1 or 2
	 * @return Drawable from cache or directly from local.
	 */

	public static Drawable genderDrawableCache(Integer gender) {
		String genderKey = "battle_gender" + gender.toString();
		Drawable drawable = ImageCache.get(genderKey);
		if (drawable == null) {
			drawable = genderDrawable(genderKey);
			ImageCache.put(genderKey, drawable);
		}
		return drawable;
	}

	/**
	 *
	 *
	 * @param poke pokemon
	 * @param front front or back
	 * @return string for resources.getIdentifier()
	 */

	public static String sprite(ShallowBattlePoke poke, boolean front) {
		String res;
		UniqueID uID;
		if (poke.specialSprites.isEmpty()) {
			uID = poke.uID;
		} else {
			uID = poke.specialSprites.peek();
		}
		if (uID.pokeNum < 0) {
			res = "empty_sprite";
		} else {
			res = "p" + uID.pokeNum + (uID.subNum == 0 ? "" : "_" + uID.subNum) +
					(front ? "_front" : "_back");
			if (poke.gender == Gender.Female.ordinal()) {
				if (InfoConfig.resources.getIdentifier(res + "f", "drawable", InfoConfig.pkgName) != 0 && poke.gender == Gender.Female.ordinal())
					// Special female sprite
					res = res + "f";
			}
			if (poke.shiny) {
				if (InfoConfig.resources.getIdentifier(res + "s", "drawable", InfoConfig.pkgName) != 0) {
					res += (poke.shiny ? "s" : "");
				}
			}
		}
		int ident = InfoConfig.resources.getIdentifier(res, "drawable", InfoConfig.pkgName);
		if (ident == 0) {
			// No sprite found. Default to missingNo.
			if (front) {
				return "missingno.png";
			} else {
				return sprite(poke, true);
			}
		} else {
			return res + ".png";
		}
	}

	/**
	 * Returns the status of the ImageCache
	 *
	 * @return ImageCache.toString()
	 */
	public static String cacheStatus() {
		return ImageCache.toString();
	}

	/**
	 * Init arrays
	 *
	 * @param gen int Generation
	 */

	@SuppressWarnings("unchecked")
	private static void testLoad(int gen) {
		if (pokemons == null) {
			pokemons = new SparseArray[GenInfo.genMax()+1];
		}
		if (pokemons[gen] == null) {
			pokemons[gen] = new SparseArray<PokemonInfo.PokeGenData>();
		}
		if (pokeCountg == null) {
			pokeCountg = new int[GenInfo.genMax()+1];
		}
		if (pokemons[gen].indexOfKey(1) == (byte)-1) {
			loadTypes(gen);
		}
	}

	/**
	 * Fill SparseArrays involving names
	 */

	private static void loadPokeNames() {
		if (pokeNames != null) {
			return;
		}
		pokeNames = new SparseArray<String>();
		pokemonsg = new SparseArray<PokemonInfo.PokeData>();
		namesToIds = new HashMap<String, UniqueID>();
		InfoFiller.uIDfill("db/pokes/pokemons.txt", new InfoFiller.OptionsFiller() {
			public void fill(int i, String s, String options) {
				pokeNames.put(i, s);
				pokemonsg.put(i, new PokeData());

				if (i > 16000) {
					pokemonsg.get(i % 65536).maxForme = (byte) (i >> 16);
				} else if (i > pokeCount) {
					pokeCount = i;
				}
				pokemonsg.get(i).options = options;
				namesToIds.put(s, new UniqueID(i));
			}
		});
	}

	/**
	 * Fill type data for specified generation
	 *
	 * @param gen int generation
	 */

	private static void loadTypes(final int gen) {
		pokeCountg[gen] = 0;
		/* First load all the released pokemon and prepare the "data containers" */
		InfoFiller.uIDfill("db/pokes/" + gen  + "G/released.txt", new Filler() {
			public void fill(int i, String s) {
				pokemons[gen].put(i, new PokeGenData());
				if (i < 16000 && i > pokeCountg[gen]) {
					pokeCountg[gen] = i;
				}
			}
		}, true);
		InfoFiller.uIDfill("db/pokes/" + gen  + "G/type1.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
                try {
                    pokemons[gen].get(i).type1 = b;
                } catch (Exception e) {
                    Log.e("PokemonInfo", "Impossible to load type 1 for gen " + gen + ", poke: " + i);
                }
            }
		});
		InfoFiller.uIDfill("db/pokes/" + gen  + "G/type2.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				pokemons[gen].get(i).type2 = b;
			}
		});
	}

	/*
	public static int numberOfPokemons() {
		loadPokeNames();

		return pokeCount;
	}

	public static int totalNumberOfPokemons() {
		loadPokeNames();

		return pokeNames.size();
	}
	*/

	/**
	 *
	 *
	 * @param gen Generation to load
	 * @return Array of names
	 */

	public static String[] nameArray(Gen gen) {
		String ret[] = new String[numberOfPokemons(gen) + 1]; //+1 for missingno

		for (HashMap.Entry<String, UniqueID> entry : namesToIds.entrySet()) {
			if (entry.getValue().subNum == 0 && entry.getValue().pokeNum < ret.length) {
				ret[entry.getValue().pokeNum] = entry.getKey();
			}
		}
		return ret;
	}

	/**
	 * Get allowed abilities for pokemon in specified gen
	 *
	 * @param id UniqueID
	 * @param gen int generation
	 * @return abilities
	 */

	public static short[] abilities(UniqueID id, int gen) {
		testLoadAbilities(id, gen);

		short abilities[] = pokemons[gen].get(id.hashCode()).ability;

		if (abilities == null) {
			abilities = pokemons[gen].get(id.originalHashCode()).ability;
		}
		return abilities;
	}

	/**
	 * Fill Ability array
	 *
	 * @param id UniqueID
	 * @param gen int Generation
	 */

	private static void testLoadAbilities(UniqueID id, final int gen) {
		if (gen >= 3) {
			testLoad(gen);
			if (pokemons[gen].get(id.originalHashCode()).ability != null) {
				return;
			}

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
		}
	}

	/**
	 * Search for pokemon name
	 *
	 * @param name Pokemon Name
	 * @return UniqueID of pokemon
	 */

	public static UniqueID number(String name) {
		return namesToIds.get(name);
	}

	/**
	 * Return possible genders for a pokemon
	 *
	 * @param uID UniqueID
	 * @return gender
	 */

	public static int gender(UniqueID uID) {
		testLoadGenders();

        byte gender = pokemonsg.get(uID.hashCode()).gender;
        if (gender == -1) {
            gender = pokemonsg.get(uID.originalHashCode()).gender;
        }

		return gender == -1 ? 0 : gender;
	}

	private static boolean gendersLoaded = false;

	/**
	 * Load Gender information
	 */

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

    private static class LruCache<K, V> {
        // ONLY USE STRING KEYS PLEASE
        private final LinkedHashMap<K, V> map;
        private final ArrayList<String> history;

        /** Size of this cache in units. Not necessarily the number of elements. */
        private int size;
        private int maxSize;

        private int putCount;
        private int createCount;
        private int evictionCount;
        private int hitCount;
        private int missCount;

        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *     the maximum number of entries in the cache. For all other caches,
         *     this is the maximum sum of the sizes of the entries in this cache.
         */
        public LruCache(int maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("maxSize <= 0");
            }
            this.maxSize = maxSize;
            this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
            this.history = new ArrayList<String>(maxSize);
        }

        /**
         * Sets the size of the cache.
         * @param maxSize The new maximum size.
         *
         * @hide
         */
        public void resize(int maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("maxSize <= 0");
            }

            synchronized (this) {
                this.maxSize = maxSize;
            }
            trimToSize(maxSize);
        }

        /**
         * Returns the value for {@code key} if it exists in the cache or can be
         * created by {@code #create}. If a value was returned, it is moved to the
         * head of the queue. This returns null if a value is not cached and cannot
         * be created.
         */
        public final V get(K key) {

            if (key == null) {
                throw new NullPointerException("key == null");
            }

            V mapValue;
            synchronized (this) {
                mapValue = map.get(key);
                if (mapValue != null) {
                    hitCount++;
                    return mapValue;
                }
                missCount++;
            }

        /*
         * Attempt to create a value. This may take a long time, and the map
         * may be different when create() returns. If a conflicting value was
         * added to the map while create() was working, we leave that value in
         * the map and release the created value.
         */

            addHistory((String) key);

            V createdValue = create(key);
            if (createdValue == null) {
                return null;
            }

            synchronized (this) {
                createCount++;
                mapValue = map.put(key, createdValue);

                if (mapValue != null) {
                    // There was a conflict so undo that last put
                    map.put(key, mapValue);
                } else {
                    size += safeSizeOf(key, createdValue);
                }
            }

            if (mapValue != null) {
                entryRemoved(false, key, createdValue, mapValue);
                return mapValue;
            } else {
                trimToSize(maxSize);
                return createdValue;
            }
        }

        private void addHistory(String key) {
            if (!history.contains(key)) {
                history.add((String) key);
            }
        }

        /**
         * Caches {@code value} for {@code key}. The value is moved to the head of
         * the queue.
         *
         * @return the previous value mapped by {@code key}.
         */
        public final V put(K key, V value) {
            if (key == null || value == null) {
                throw new NullPointerException("key == null || value == null");
            }

            addHistory((String) key);

            V previous;
            synchronized (this) {
                putCount++;
                size += safeSizeOf(key, value);
                previous = map.put(key, value);
                if (previous != null) {
                    size -= safeSizeOf(key, previous);
                }
            }

            if (previous != null) {
                entryRemoved(false, key, previous, value);
            }

            trimToSize(maxSize);
            return previous;
        }

        /**
         * Remove the eldest entries until the total of remaining entries is at or
         * below the requested size.
         *
         * @param maxSize the maximum size of the cache before returning. May be -1
         *            to evict even 0-sized elements.
         */
        public void trimToSize(int maxSize) {
            while (true) {
                K key;
                V value;
                synchronized (this) {
                    if (size < 0 || (map.isEmpty() && size != 0)) {
                        throw new IllegalStateException(getClass().getName()
                                + ".sizeOf() is reporting inconsistent results!");
                    }

                    if (size <= maxSize) {
                        break;
                    }

                    map.remove(history.get(0));
                    history.remove(0);

                    map.remove(history.get(0));
                    history.remove(0);

                    map.remove(history.get(0));
                    history.remove(0);


                    size -= 3;

                    evictionCount += 3;
                }
            }
        }

        /**
         * Removes the entry for {@code key} if it exists.
         *
         * @return the previous value mapped by {@code key}.
         */
        public final V remove(K key) {
            if (key == null) {
                throw new NullPointerException("key == null");
            }

            V previous;
            synchronized (this) {
                previous = map.remove(key);
                if (previous != null) {
                    size -= safeSizeOf(key, previous);
                }
            }

            if (previous != null) {
                entryRemoved(false, key, previous, null);
            }

            return previous;
        }

        /**
         * Called for entries that have been evicted or removed. This method is
         * invoked when a value is evicted to make space, removed by a call to
         * {@link #remove}, or replaced by a call to {@link #put}. The default
         * implementation does nothing.
         *
         * <p>The method is called without synchronization: other threads may
         * access the cache while this method is executing.
         *
         * @param evicted true if the entry is being removed to make space, false
         *     if the removal was caused by a {@link #put} or {@link #remove}.
         * @param newValue the new value for {@code key}, if it exists. If non-null,
         *     this removal was caused by a {@link #put}. Otherwise it was caused by
         *     an eviction or a {@link #remove}.
         */
        protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {}

        /**
         * Called after a cache miss to compute a value for the corresponding key.
         * Returns the computed value or null if no value can be computed. The
         * default implementation returns null.
         *
         * <p>The method is called without synchronization: other threads may
         * access the cache while this method is executing.
         *
         * <p>If a value for {@code key} exists in the cache when this method
         * returns, the created value will be released with {@link #entryRemoved}
         * and discarded. This can occur when multiple threads request the same key
         * at the same time (causing multiple values to be created), or when one
         * thread calls {@link #put} while another is creating a value for the same
         * key.
         */
        protected V create(K key) {
            return null;
        }

        private int safeSizeOf(K key, V value) {
            int result = sizeOf(key, value);
            if (result < 0) {
                throw new IllegalStateException("Negative size: " + key + "=" + value);
            }
            return result;
        }

        /**
         * Returns the size of the entry for {@code key} and {@code value} in
         * user-defined units.  The default implementation returns 1 so that size
         * is the number of entries and max size is the maximum number of entries.
         *
         * <p>An entry's size must not change while it is in the cache.
         */
        protected int sizeOf(K key, V value) {
            return 1;
        }

        /**
         * Clear the cache, calling {@link #entryRemoved} on each removed entry.
         */
        public final void evictAll() {
            trimToSize(-1); // -1 will evict 0-sized elements
        }

        /**
         * For caches that do not override {@link #sizeOf}, this returns the number
         * of entries in the cache. For all other caches, this returns the sum of
         * the sizes of the entries in this cache.
         */
        public synchronized final int size() {
            return size;
        }

        /**
         * For caches that do not override {@link #sizeOf}, this returns the maximum
         * number of entries in the cache. For all other caches, this returns the
         * maximum sum of the sizes of the entries in this cache.
         */
        public synchronized final int maxSize() {
            return maxSize;
        }

        /**
         * Returns the number of times {@link #get} returned a value that was
         * already present in the cache.
         */
        public synchronized final int hitCount() {
            return hitCount;
        }

        /**
         * Returns the number of times {@link #get} returned null or required a new
         * value to be created.
         */
        public synchronized final int missCount() {
            return missCount;
        }

        /**
         * Returns the number of times {@link #create(Object)} returned a value.
         */
        public synchronized final int createCount() {
            return createCount;
        }

        /**
         * Returns the number of times {@link #put} was called.
         */
        public synchronized final int putCount() {
            return putCount;
        }

        /**
         * Returns the number of values that have been evicted.
         */
        public synchronized final int evictionCount() {
            return evictionCount;
        }

        /**
         * Returns a copy of the current contents of the cache, ordered from least
         * recently accessed to most recently accessed.
         */
        public synchronized final Map<K, V> snapshot() {
            return new LinkedHashMap<K, V>(map);
        }

        @Override public synchronized final String toString() {
            int accesses = hitCount + missCount;
            int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
            return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
                    maxSize, hitCount, missCount, hitPercent);
        }
    }

}
