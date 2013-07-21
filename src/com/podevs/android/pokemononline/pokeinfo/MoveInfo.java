package com.podevs.android.pokemononline.pokeinfo;

import java.util.ArrayList;

import android.util.SparseArray;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.FillerByte;

public class MoveInfo {
	private static class Move {
		String name;
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
	
	public static String name(int num) {
		testLoad(num);
		
		return moveNames.get(num).name;
	}
	
	private static void testLoad(int num) {
		if (moveNames == null) {
			loadPokeMoves();
		}
	}
	
	public static byte type(int num) {
		testLoad(num);
		loadPokeTypes();

		return moveNames.get(num).type;
	}
	
	public static byte pp(int num) {
		testLoad(num);
		loadPokePPs();

		return moveNames.get(num).pp;
	}
	
	public static byte accuracy(int num) {
		testLoad(num);
		loadPokeAccuracies();

		return moveNames.get(num).accuracy;
	}
	
	public static String accuracyString(int num) {
		byte acc = accuracy(num);
		if (acc == 101) {
			return "--";
		} else {
			return String.valueOf(num);
		}
	}
	
	public static byte power(int num) {
		testLoad(num);
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
			return String.valueOf(num);
		}
	}
	
	public static String effect(int num) {
		testLoad(num);
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
		effectsloaded = true;
		InfoFiller.fill("db/moves/5G/effect.txt", new Filler() {
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
		pploaded = true;
		InfoFiller.fill("db/moves/5G/pp.txt", new FillerByte() {
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
		accuracyloaded = true;
		InfoFiller.fill("db/moves/5G/accuracy.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).accuracy = b;
			}
		});
	}
	
	static boolean powerloaded = false;
	private static void loadPokePowers() {
		if (powerloaded) {
			return;
		}
		powerloaded = true;
		InfoFiller.fill("db/moves/5G/power.txt", new FillerByte() {
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
		typeloaded = true;
		InfoFiller.fill("db/moves/5G/type.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).type = b;
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
