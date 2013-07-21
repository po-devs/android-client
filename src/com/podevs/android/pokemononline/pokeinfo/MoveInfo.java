package com.podevs.android.pokemononline.pokeinfo;

import java.util.Vector;

import android.util.SparseArray;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.FillerByte;

public class MoveInfo {
	private static class Move {
		String name;
		byte type = -1;
		byte pp = -1;
		String accuracy = null;
		String power = null;
		String effect = null;
		
		Move(String name) {
			this.name = name;
		}
	}
	
	private static Vector<Move> moveNames = null;
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
		if (moveNames.get(num).type == (byte)-1) {
			loadPokeTypes();
		}
		return moveNames.get(num).type;
	}
	
	public static byte pp(int num) {
		testLoad(num);
		if (moveNames.get(num).pp == (byte)-1) {
			loadPokePPs();
		}
		return moveNames.get(num).pp;
	}
	
	public static String accuracy(int num) {
		testLoad(num);
		if (moveNames.get(num).accuracy == null) {
			loadPokeAccuracies();
		}
		return moveNames.get(num).accuracy;
	}
	
	public static String power(int num) {
		testLoad(num);
		if (moveNames.get(num).power == null) {
			loadPokePowers();
		}
		return moveNames.get(num).power;
	}
	
	public static String effect(int num) {
		testLoad(num);
		if (moveNames.get(num).effect == null) {
			loadPokeEffects();
		}
		return moveNames.get(num).effect;
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
	
	private static void loadPokeEffects() {
		InfoFiller.fill("db/moves/5G/effect.txt", new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).effect = s;
			}
		});
	}

	private static void loadPokePPs() {
		InfoFiller.fill("db/moves/5G/pp.txt", new FillerByte() {
			@Override
			void fillIntByte(int i, byte b) {
				moveNames.get(i).pp = b;
			}
		});
	}
	
	private static void loadPokeAccuracies() {
		InfoFiller.fill("db/moves/5G/accuracy.txt", new Filler() {
			public void fill(int i, String b) {
				moveNames.get(i).accuracy = b;
			}
		});
	}
	
	private static void loadPokePowers() {
		InfoFiller.fill("db/moves/5G/power.txt", new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).power = s;
			}
		});
	}

	private static void loadPokeTypes() {
		InfoFiller.fill("db/moves/5G/type.txt", new FillerByte() {
			@Override
			void fillIntByte(int i, byte b) {
				moveNames.get(i).type = b;
			}
		});
	}

	private static void loadPokeMoves() {
		moveNames = new Vector<Move>();
		InfoFiller.fill("db/moves/moves.txt", new Filler() {
			public void fill(int i, String b) {
				moveNames.addElement(new Move(b));
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
