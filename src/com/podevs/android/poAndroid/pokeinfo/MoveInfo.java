package com.podevs.android.poAndroid.pokeinfo;

import android.util.SparseArray;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.pokeinfo.InfoFiller.Filler;
import com.podevs.android.poAndroid.pokeinfo.InfoFiller.FillerByte;
import com.podevs.android.poAndroid.registry.RegistryActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MoveInfo extends GenInfo {
	public enum Target {
		ChosenTarget, //0
		PartnerOrUser, //1
		Partner, //2
		MeFirstTarget, //3
		AllButSelf, //4
		Opponents, //5
		TeamParty, //6
		User, //7
		All, //8
		RandomTarget, //9
		Field, //10
		OpposingTeam, //11
		TeamSide, //12
		IndeterminateTarget //13
	}

	public enum Flags {
		ContactFlag(1), // Is the move a contact move
		ChargeFlag(2), // Is the move a charging move? not used by PO yet
		RechargeFlag(4), // Is the move a recharging move? not used by PO yet
		ProtectableFlag(8), //Can the move be protected against
		MagicCoatableFlag(16), //Can the move be magic coated
		SnatchableFlag(32), //Can the move be snatched
		MemorableFlag(64), //Can the move be mirror moves
		PunchFlag(128), //Is the move boosted with Iron Fist
		SoundFlag(256), //Is the move blocked with SoundProof
		FlyingFlag(512), //Is the move an invulnerable move (shadow force/...)? not used by PO yet
		UnthawingFlag(1024), // Does the user of this move unthaw when frozen?
		FarReachFlag(2048), // Can this move reach targets far across in triples?
		HealingFlag(4096), //Can this move be blocked with Heal Block
		MischievousFlag(8192), // Can this move bypass substitute?
		BiteFlag(16384),//Strong jaw moves
		PowderFlag(32768), //Powder moves
		BallFlag(65536), //Ball moves for Bulletproof
		LaunchFlag(131072), //Moves that get boosted by Mega Launcher
		DanceFlag(262144) // Dancing moves for ability Dancer
		;

		int value;

		Flags(int num)
		{
			this.value = num;
		}

		public int getValue()
		{
			return value;
		}
	}

	public static class Move {
		public String name;
		byte damageClass = 0;
		byte type = 0;
		byte pp = 5;
		byte accuracy = 0;
		byte power = 0;
		byte zpower = 0;
		int flags = 0;
		String effect = null;
		String zeffect = null;
		Target range = Target.ChosenTarget;

		Move(String name) {
			this.name = name;
		}
	}


	private static ArrayList<Move> moveNames = null;
	private static SparseArray<String> moveMessages = null;
    private static Short[] allMoves = null;

	private static int thisGen = genMax();
	// private static int thisSubGen = 6;

	public static void newGen() {
		moveNames = null; // Because 0 will not overwrite, i.e. type Normal (0) will not overwrite already saved type Fairy (17).
		testLoad();
		pploaded = false;
		damageClassloaded = false;
		powerloaded = false;
		typeloaded = false;
		accuracyloaded = false;
		effectsloaded = false;
		targetsloaded = false;
		flagsloaded = false;
		genmovelistloaded = false;
	}

	public static void forceSetGen(int Gen, int SubGen) {
		testLoad();
		thisGen = Gen;
		//thisSubGen = SubGen;
	}

	public static String name(int num) {
		return moveNames.get(num).name;
	}

	public static String zName(int num, boolean zmove) {
		String ret = moveNames.get(num).name;

		if (zmove && num != 0 && (power(num) == 0 || num == 693 || num == 382)) { //Extreme Evoboost and Me First
			ret = "Z-" + ret;
		}

		return ret;
	}

	public static int indexOf(String s) {
		testLoad();
		for (int i = 0; i < moveNames.size(); i ++) {
			if (moveNames.get(i).name.equals(s)) {
				return i;
			}
		}
		return 0;
	}

	private static void testLoad() {
		if (moveNames == null) {
			loadPokeMoves();
		}
	}

    public static byte damageClass(int num) {
		loadDamageClasses();
        return moveNames.get(num).damageClass;
    }

	public static byte type(int num) {
		loadPokeTypes();
		return moveNames.get(num).type;
	}

	public static byte pp(int num) {
		loadPokePPs();
		return moveNames.get(num).pp;
	}

	public static byte accuracy(int num) {
		loadPokeAccuracies();
		return moveNames.get(num).accuracy;
	}

	public static String accuracyString(int num) {
		return accuracyToString(accuracy(num));
	}

	public static String accuracyToString(int acc) {
		if (acc == 101) {
			return "--";
		} else {
			return String.valueOf(acc);
		}
	}

	public static byte power(int num) {
		loadPokePowers();
		return moveNames.get(num).power;
	}

	public static byte zPower(int num) {
		loadPokeZPowers();
		return moveNames.get(num).zpower;
	}

	public static String powerString(int num) {
		return powerToString(power(num));
	}

	public static String powerToString(int pow) {
		if (pow == 0) {
			return "--";
		} else if (pow == 1) {
			return "???";
		} else {
			return String.valueOf(pow >= 0 ? pow : (pow + 256));
		}
	}

	public static String effect(int num) {
		loadPokeEffects();

		String effect = moveNames.get(num).effect;
		return effect == null ? "" : effect;
	}

	public static String zDescription(int num) {
		loadZEffects();

		String zeffect = moveNames.get(num).zeffect;
		return zeffect == null ? "" : zeffect;
	}

	public static Target target(int num) {
		loadTargets();

		return moveNames.get(num).range;
	}

	public static String targetString(int num) {
		return targetToString(target(num));
	}

	public static String targetToString(Target t) {
		switch (t) {
			case ChosenTarget:
				return "Single Target";
			case PartnerOrUser:
				return "Self or Ally";
			case Partner:
				return "Single Ally";
			case MeFirstTarget:
				return "Single Target";
			case AllButSelf:
				return "All But Self";
			case Opponents:
				return "Adjacent Foes";
			case TeamParty:
				return "User's Team";
			case User:
				return "Self";
			case All:
				return "All";
			case RandomTarget:
				return "Random";
			case Field:
				return "Field";
			case OpposingTeam:
				return "All Foes";
			case TeamSide:
				return "All Allies";
			case IndeterminateTarget:
				return "Self";
			default:
				return "---";
		}
	}

	public static int flags(int num)
	{
		loadFlags();

		return moveNames.get(num).flags;
	}

	public static String message(int num, int part) {
		if (moveMessages == null) {
			loadMoveMessages();
		}

		String parts [] = ((String)moveMessages.get(num, "")).split("\\|");
		try {
			return parts[part];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return "";
		}
	}

	static boolean effectsloaded = false;
	private static void loadPokeEffects() {
		if (effectsloaded) {
			return;
		}
		testLoad();
		effectsloaded = true;
		String path;

		if (RegistryActivity.localize_assets) {
			path = "db/moves/" + InfoConfig.resources.getString(R.string.asset_localization) + thisGen + "G/effect.txt";
			if (!InfoConfig.fileExists(path)) {
				path = "db/moves/" + thisGen + "G/effect.txt";
			}
		} else {
			path = "db/moves/" + thisGen + "G/effect.txt";
		}
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).effect = s;
			}
		});
	}

	static boolean zeffectsloaded = false;
	private static void loadZEffects() {
		if (zeffectsloaded) {
			return;
		}
		testLoad();
		zeffectsloaded = true;
		String path = "db/moves/" + thisGen + "G/zeffect.txt";
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String s) { moveNames.get(i).zeffect = s; }
		});
	}

	static boolean pploaded = false;
	private static void loadPokePPs() {
		if (pploaded) {
			return;
		}
		testLoad();
		pploaded = true;
		String path = "db/moves/" + thisGen + "G/pp.txt";
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).pp = b;
			}
		});
	}

	static boolean accuracyloaded = false;
	private static void loadPokeAccuracies() {
		if (accuracyloaded) {
			return;
		}
		testLoad();
		accuracyloaded = true;
		String path = "db/moves/" + thisGen + "G/accuracy.txt";
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).accuracy = b;
			}
		});
	}

	private static void loadPokePowers(String path) {
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).power = b;
			}
		});
	}

	static boolean powerloaded = false;
	private static void loadPokePowers() {
		if (powerloaded) {
			return;
		}
		testLoad();
		powerloaded = true;
		String path = "db/moves/" + thisGen + "G/power.txt";
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).power = b;
			}
		});
	}

	static boolean zpowerloaded = false;
	private static void loadPokeZPowers() {
		if (zpowerloaded) {
			return;
		}
		testLoad();
		zpowerloaded = true;
		String path = "db/moves/" + thisGen + "G/zpower.txt";
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).zpower = b;
			}
		});
	}

	static boolean typeloaded = false;
	private static void loadPokeTypes() {
		if (typeloaded) {
			return;
		}
		testLoad();
		typeloaded = true;
		String path = "db/moves/" + thisGen + "G/type.txt";
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).type = b;
			}
		});
	}

	static boolean damageClassloaded = false;
        private static void loadDamageClasses() {
            if (damageClassloaded) {
                return;
            }
			testLoad();
            damageClassloaded = true;
			String path = "db/moves/" + thisGen + "G/damage_class.txt";
            InfoFiller.fill(path, new FillerByte() {
                @Override
                void fillByte(int i, byte b) {
                    moveNames.get(i).damageClass = b;
                }
            });
        }

	static boolean targetsloaded = false;
	private static void loadTargets() {
		if (targetsloaded) {
			return;
		}
		testLoad();
		targetsloaded = true;
		String path = "db/moves/" + thisGen + "G/range.txt";
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).range = Target.values()[b];
			}
		});
	}

	static boolean flagsloaded = false;
	private static void loadFlags() {
		if (flagsloaded) {
			return;
		}
		testLoad();
		flagsloaded = true;
		String path = "db/moves/" + thisGen + "G/flags.txt";
		InfoFiller.fill(path, new InfoFiller.FillerInt() {
			@Override
			void fillInt(int i, int b) {
				moveNames.get(i).flags = b;
			}
		});
	}

	private static void loadPokeMoves() {
		moveNames = new ArrayList<Move>();
		String path;
		if (RegistryActivity.localize_assets) {
			path = "db/moves/" + InfoConfig.resources.getString(R.string.asset_localization) + "moves.txt";
			if (!InfoConfig.fileExists(path)) {
				path = "db/moves/moves.txt";
			}
		} else {
			path = "db/moves/moves.txt";
		}
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String b) {
				moveNames.add(new Move(b));
			}
		});
	}

	static boolean genmovelistloaded = false;
	private static void fillAllMoves() {
		if (genmovelistloaded) {
			return;
		}
		allMoves = new Short[moveNames.size()];

		for (int i = 0; i <= allMoves.length - 1; i++) {
			allMoves[i] = (short) 0;
		}

		InfoFiller.plainFill("db/moves/" + thisGen + "G/moves.txt", new Filler() {
			@Override
			public void fill(int i, String s) {
				allMoves[i] = (short) i;
			}
		});

		allMoves = trim(allMoves);

		java.util.Arrays.sort(allMoves, new Comparator<Short>() {
			@Override
			public int compare(Short lhs, Short rhs) {
				return name(lhs).compareToIgnoreCase(name(rhs));
			}
		});

		genmovelistloaded = true;
	}

	private static Short[] trim(Short[] shorts) {
		int i = shorts.length - 1;
		while (i >= 0 && shorts[i] == 0) {
			--i;
		}

		return Arrays.copyOf(shorts, i + 1);
	}

	private static void loadMoveMessages() {
		moveMessages = new SparseArray<String>();
		String path;
		if (RegistryActivity.localize_assets) {
			path = "db/moves/" + InfoConfig.resources.getString(R.string.asset_localization) + "move_message.txt";
			if (!InfoConfig.fileExists(path)) {
				path = "db/moves/move_message.txt";
			}
		} else {
			path = "db/moves/move_message.txt";
		}
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String b) {
				moveMessages.put(i, b);
			}
		});
	}

    public static Short[] getAllMoves() {
		if (!genmovelistloaded) {
			testLoad();
			fillAllMoves();
		}
		return allMoves;
	}

    public static ArrayList<String> makeList(short[] input) {
        ArrayList<String> nameList = new ArrayList<String>(input.length);
        for (int i: input) {
            nameList.add(name(i));
        }
        return nameList;
    }

    public static ArrayList<String> makeList(Short[] input) {
        ArrayList<String> nameList = new ArrayList<String>(input.length);
        for (int i: input) {
            nameList.add(name(i));
        }
        return nameList;
    }
}
