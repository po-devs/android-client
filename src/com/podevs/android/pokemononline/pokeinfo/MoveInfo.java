package com.podevs.android.pokemononline.pokeinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.SparseArray;

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
	
	private static SparseArray<Move> moveNames = new SparseArray<Move>();
	
	public static String name(int num) {
		testLoad(num);
		
		return moveNames.get(num).name;
	}
	
	private static void testLoad(int num) {
		if (moveNames.indexOfKey(num) < 0) {
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
	
	private static void loadPokeEffects() {
		fill("db/moves/5G/effect.txt", new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).effect = s;
			}
		});
	}

	public static interface Filler {
		void fill(int i, String s);
	}
	
	public static abstract class FillerByte implements Filler {
		public void fill(int i, String s) {
			fillIntByte(i, (byte)Integer.parseInt(s));
		}
		
		abstract void fillIntByte(int i, byte b);
	}
	
	private static void fill(String file, Filler filler) {
		InputStream assetsDB = null;
		try {
			assetsDB = getContext().getAssets().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(assetsDB, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str.length() > 0 && (int)str.charAt(0) == 65279) {
					str = str.substring(1);
				}
				
				int spaceIndex = str.indexOf(' ');
				
				if (spaceIndex < 0) {
					break;
				}
				
				filler.fill(Integer.parseInt(str.substring(0, spaceIndex)), str.substring(spaceIndex + 1));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadPokePPs() {
		fill("db/moves/5G/pp.txt", new FillerByte() {
			@Override
			void fillIntByte(int i, byte b) {
				moveNames.get(i).pp = b;
			}
		});
	}
	
	private static void loadPokeAccuracies() {
		fill("db/moves/5G/accuracy.txt", new Filler() {
			public void fill(int i, String b) {
				moveNames.get(i).accuracy = b;
			}
		});
	}
	
	private static void loadPokePowers() {
		fill("db/moves/5G/power.txt", new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).power = s;
			}
		});
	}

	private static void loadPokeTypes() {
		fill("db/moves/5G/type.txt", new FillerByte() {
			@Override
			void fillIntByte(int i, byte b) {
				moveNames.get(i).type = b;
			}
		});
	}

	private static void loadPokeMoves() {
		InputStream assetsDB = null;
		try {
			assetsDB = getContext().getAssets().open("db/moves/moves.txt");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(assetsDB, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str.length() > 0 && (int)str.charAt(0) == 65279) {
					str = str.substring(1);
				}
				
				int spaceIndex = str.indexOf(' ');
				
				if (spaceIndex < 0) {
					break;
				}
				
				int key = Integer.parseInt(str.substring(0, spaceIndex));
				String val = str.substring(spaceIndex + 1); 
				moveNames.put(key, new Move(val));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Context getContext() {
		return InfoConfig.context;
	}
}
