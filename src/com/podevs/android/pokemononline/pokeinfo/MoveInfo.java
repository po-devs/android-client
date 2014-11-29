package com.podevs.android.pokemononline.pokeinfo;

import java.util.ArrayList;

import android.util.SparseArray;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.FillerByte;

public class MoveInfo extends GenInfo {
	public static class Move {
		public String name;
		byte damageClass = 0;
		byte type = 0;
		byte pp = 5;
		byte accuracy = 0;
		byte power = 0;
		String effect = null;

		Move(String name) {
			this.name = name;
		}
	}


	private static ArrayList<Move> moveNames = null;
	private static SparseArray<String> moveMessages = null;

	private static int thisGen = genMax();
	private static int thisSubGen = 6;

	public static void newGen() {
		moveNames = null; // Because 0 will not overwrite, i.e. type Normal (0) will not overwrite already saved type Fairy (17).
		testLoad();
		pploaded = false;
		damageClassloaded = false;
		powerloaded = false;
		typeloaded = false;
		accuracyloaded = false;
		effectsloaded = false;
	}

	public static void forceSetGen(int Gen, int SubGen) {
		testLoad();
		thisGen = Gen;
		thisSubGen = SubGen;
	}

	public static String name(int num) {
		return moveNames.get(num).name;
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
		byte acc = accuracy(num);
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

	public static String powerString(int num) {
		byte pow = power(num);
		if (pow == 0) {
			return "--";
		} else if (pow == 1) {
			return "???";
		} else {
			return String.valueOf(pow >= 0 ? pow : (pow + 255));
		}
	}

	public static String effect(int num) {
		loadPokeEffects();

		String effect = moveNames.get(num).effect;
		return effect == null ? "" : effect;
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
		String path = "db/moves/" + thisGen + "G/effect.txt";
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).effect = s;
			}
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

	private static void loadPokeMoves() {
		moveNames = new ArrayList<Move>();
		InfoFiller.fill("db/moves/moves.txt", new Filler() {
			public void fill(int i, String b) {
				moveNames.add(new Move(b));
			}
		});
	}

	private static void loadMoveMessages() {
		moveMessages = new SparseArray<String>();
		InfoFiller.fill("db/moves/move_message.txt", new Filler() {
			public void fill(int i, String b) {
				moveMessages.put(i, b);
			}
		});
	}
}
