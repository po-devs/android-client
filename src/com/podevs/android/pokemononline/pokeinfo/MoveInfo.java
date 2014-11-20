package com.podevs.android.pokemononline.pokeinfo;

import java.util.ArrayList;

import android.util.SparseArray;

import com.podevs.android.pokemononline.pokeinfo.InfoFiller.Filler;
import com.podevs.android.pokemononline.pokeinfo.InfoFiller.FillerByte;

public class MoveInfo {
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

	private enum cases {
		Accuracy, // 0
		Power, // 1
		PP, // 2
		Type, // 3
		DamageCategory, // 4
		Targeting, // 5
		Contact, // 6
		Priority, // 7
		NameChanges // 8
	}

	private enum gens {
		Gen0,
		Gen1,
		Gen2,
		Gen3,
		Gen4,
		Gen5,
		Gen6
	}

	private enum subGens {
		SubGen0,
		SubGen1,
		SubGen2,
		SubGen3,
		SubGen4
	}
	public static String casename(int num) {
		return cases.values()[num].toString();
	}

	private static int lastGen = 0;
	private static int lastSubGen = 0;
	private static int thisGen = 0;
	private static int thisSubGen = 0;

	public static void checkGenChanges(int caseNumber, int gen, int sub) {
		cases c = cases.values()[caseNumber];
		switch(c) {
			case Accuracy: {
				// all gens have differences
				gens g = gens.values()[gen];
				switch(g) {
					case Gen1: {
						if (lastGen != 1) {
							loadPokeAccuracies("db/moves/1G/accuracy.txt");
							return;
						}
					}
					case Gen2: {
						if (lastGen != 2) {
							loadPokeAccuracies("db/moves/2G/accuracy.txt");
							return;
						}
					}
					case Gen3: {
						if (lastGen != 3) {
							loadPokeAccuracies("db/moves/3G/accuracy.txt");
							return;
						}
					}
					case Gen4: {
						subGens s = subGens.values()[sub];
						switch (s) {
							case SubGen0: {
								if (lastSubGen  != 1) {
									loadPokeAccuracies("db/moves/4G/accuracy.txt");
									return;
								}
							}
							case SubGen1: {
								// WRONG NEEDS NEW DATABASE FILE
								if (lastSubGen  < 2) {
									loadPokeAccuracies("db/moves/4G/accuracy.txt");
									return;
								}
								// WRONG NEEDS NEW DATABASE FILE
							}
							case SubGen2: {
								// Does it use diamond and pearl as a base or platinum??? could be wrong
								if (lastSubGen  < 2) {
									loadPokeAccuracies("db/moves/4G/accuracy.txt");
									return;
								}
							}
						}
					}
					case Gen5: {
						if (lastGen != 5) {
							loadPokeAccuracies("db/moves/5G/accuracy.txt");
							return;
						}
					}
					case Gen6: {
						if (lastGen != 6) {
							loadPokeAccuracies("db/moves/6G/accuracy.txt");
							return;
						}
					}
					return;
				}
			}
			case Power: {
				// All gens
				gens g = gens.values()[gen];
				switch(g) {
					case Gen1: {
						if (lastGen != 1) {
							loadPokePowers("db/moves/1G/power.txt");
							return;
						}
					}
					case Gen2: {
						if (lastGen != 2) {
							loadPokePowers("db/moves/2G/power.txt");
							return;
						}
					}
					case Gen3: {
						// Colo to XD
						// Shadow Rush 90 -> 55
						subGens s = subGens.values()[sub];
						switch (s) {
							case SubGen0: {
								if (lastSubGen < 4) {
									loadPokePowers("db/moves/3G/power.txt");
									return;
								}
							}
							case SubGen1: {
								if (lastSubGen < 4) {
									loadPokePowers("db/moves/3G/power.txt");
									return;
								}
							}
							case SubGen2: {
								if (lastSubGen < 4) {
									loadPokePowers("db/moves/3G/power.txt");
									return;
								}
							}
							case SubGen3: {
								if (lastSubGen < 4) {
									loadPokePowers("db/moves/3G/power.txt");
									return;
								}
							}
							case SubGen4: {
								// WRONG NEEDS NEW DATABASE FILE
								if (lastSubGen != 4) {
									loadPokePowers("db/moves/3G/power.txt");
									return;
								}
								// WRONG NEEDS NEW DATABASE FILE
							}
						}
					}
					case Gen4: {
						if (lastGen != 4) {
							loadPokePowers("db/moves/4G/power.txt");
							return;
						}
					}
					case Gen5: {
						if (lastGen != 5) {
							loadPokePowers("db/moves/5G/power.txt");
							return;
						}
					}
					case Gen6: {
						if (lastGen != 6) {
							loadPokePowers("db/moves/6G/power.txt");
							return;
						}
					}
				}
				return;
			}
			case PP: {
				gens g = gens.values()[gen];
				switch(g) {
					// 3 between 6 all PP changes
					case Gen1: {
						if (lastGen < 4) {
							loadPokePPs("db/moves/3G/pp.txt");
							return;
						}
					}
					case Gen2: {
						if (lastGen < 4) {
							loadPokePPs("db/moves/3G/pp.txt");
							return;
						}
					}
					case Gen3: {
						if (lastGen < 4 ){
							loadPokePPs("db/moves/3G/pp.txt");
							return;
						}
					}
					case Gen4: {
						if (lastGen != 4) {
							loadPokePPs("db/moves/4G/pp.txt");
							return;
						}
					}
					case Gen5: {
						if (lastGen != 5) {
							loadPokePPs("db/moves/5G/pp.txt");
							return;
						}
					}
					case Gen6: {
						if (lastGen != 6) {
							loadPokePPs("db/moves/6G/pp.txt");
							return;
						}
					}
				}
				return;
			}
			case Type: {
				gens g = gens.values()[gen];
				switch(g) {
					// Gen I to II
					// Gen IV to V
					// Gen V to VI
					case Gen1: {
						if (lastGen != 1) {
							loadPokeTypes("db/moves/1G/type.txt");
							return;
						}
					}
					case Gen2: {
						if (!(lastGen > 1 && 5 > lastGen)) {
							loadPokeTypes("db/moves/4G/type.txt");
							return;
						}
					}
					case Gen3: {
						if (!(lastGen > 1 && 5 > lastGen)) {
							loadPokeTypes("db/moves/4G/type.txt");
							return;
						}
					}
					case Gen4: {
						if (!(lastGen > 1 && 5 > lastGen)) {
							loadPokeTypes("db/moves/4G/type.txt");
							return;
						}
					}
					case Gen5: {
						if (lastGen != 5) {
							loadPokeTypes("db/moves/5G/type.txt");
							return;
						}
					}
					case Gen6: {
						if (lastGen != 6) {
							loadPokeTypes("db/moves/6G/type.txt");
							return;
						}
					}
				}
				return;
			}
			case DamageCategory: {
				gens g = gens.values()[gen];
				switch(g) {
					// Gen I to Gen II -> It's change changes in Gen III
					// Gen III to Gen IV
					case Gen1: {
						if (lastGen != 1) {
							loadDamageClasses("db/moves/1G/damage_class.txt");
							return;
						}
					}
					case Gen2: {
						if (!(lastGen > 1 && lastGen < 4)) {
							loadDamageClasses("db/moves/3G/damage_class.txt");
							return;
						}
					}
					case Gen3: {
						if (!(lastGen > 1 && lastGen < 4)) {
							loadDamageClasses("db/moves/3G/damage_class.txt");
							return;
						}
					}
					case Gen4: {
						if (!(lastGen > 3)) {
							loadDamageClasses("db/moves/6G/damage_class.txt");
							return;
						}
					}
					case Gen5: {
						if (!(lastGen > 3)) {
							loadDamageClasses("db/moves/6G/damage_class.txt");
							return;
						}
					}
					case Gen6: {
						if (!(lastGen > 3)) {
							loadDamageClasses("db/moves/6G/damage_class.txt");
							return;
						}
					}
				}
				return;
			}
			/*
			case Targeting: {
				switch(gen) {
					// Gen III to Gen IV
					// Gen IV to Gen V
					// Gen V to Gen VI
				return;
			}
			case Contact: {
				// Gen III to Gen IV
				return;
			}
			case Priority: {
				// Between all Gens
				return;
			}
			*/
				/*
			case NameChanges: {
				// For English
				// Gen II to Gen III
				// Gen V to Gen VI
				return;
			}
			*/
		}
		return;
	}

	static boolean loadNewGen = true;
	public static void loadGen(int Gen, int subGen) {
		if (loadNewGen) {
			thisGen = Gen;
			thisSubGen = subGen;
			for (int i = 0; i < 5; i = i + 1) {
				checkGenChanges(i, thisGen, thisSubGen);
			}
		}
		loadNewGen = false;
		lastGen = thisGen;
		lastSubGen = thisSubGen;
	}

	public static void newGen() {
		loadNewGen = true;
	}

	public static String name(int num) {
		testLoad(num);

		return moveNames.get(num).name;
	}

	private static void testLoad(int num) {
		if (moveNames == null) {
			loadPokeMoves();
		}
	}

        public static byte damageClass(int num) {
            testLoad(num);

            return moveNames.get(num).damageClass;
        }

	public static byte type(int num) {
		testLoad(num);

		return moveNames.get(num).type;
	}

	public static byte pp(int num) {
		testLoad(num);


		return moveNames.get(num).pp;
	}

	public static byte accuracy(int num) {
		testLoad(num);
		/*
		loadPokeAccuracies();
		*/
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
		testLoad(num);

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
		InfoFiller.fill("db/moves/6G/effect.txt", new Filler() {
			public void fill(int i, String s) {
				moveNames.get(i).effect = s;
			}
		});
	}
	/*
	static boolean pploaded = false;
	private static void loadPokePPs() {
		if (pploaded) {
			return;
		}
		pploaded = true;
		InfoFiller.fill("db/moves/6G/pp.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).pp = b;
			}
		});
	}
*/
	private static void loadPokePPs(String path) {
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).pp = b;
			}
		});
	}

	private static void loadPokeAccuracies(String path) {
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).accuracy = b;
			}
		});
	}
/*
	static boolean accuracyloaded = false;
	private static void loadPokeAccuracies() {
		if (accuracyloaded) {
			return;
		}
		accuracyloaded = true;
		InfoFiller.fill("db/moves/6G/accuracy.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).accuracy = b;
			}
		});
	}
*/

	private static void loadPokePowers(String path) {
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).power = b;
			}
		});
	}
	/*

	static boolean powerloaded = false;
	private static void loadPokePowers() {
		if (powerloaded) {
			return;
		}
		powerloaded = true;
		InfoFiller.fill("db/moves/6G/power.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).power = b;
			}
		});
	}
*/
	/*
	static boolean typeloaded = false;
	private static void loadPokeTypes() {
		if (typeloaded) {
			return;
		}
		typeloaded = true;
		InfoFiller.fill("db/moves/6G/type.txt", new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).type = b;
			}
		});
	}
*/
	private static void loadPokeTypes(String path) {
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).type = b;
			}
		});
	}

	private static void loadDamageClasses(String path) {
		InfoFiller.fill(path, new FillerByte() {
			@Override
			void fillByte(int i, byte b) {
				moveNames.get(i).damageClass = b;
			}
		});
	}
/*
	static boolean damageClassloaded = false;
        private static void loadDamageClasses() {
            if (damageClassloaded) {
                return;
            }
            damageClassloaded = true;
            InfoFiller.fill("db/moves/6G/damage_class.txt", new FillerByte() {
                @Override
                void fillByte(int i, byte b) {
                    moveNames.get(i).damageClass = b;
                }
            });
        }
*/
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
