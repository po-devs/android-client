package com.podevs.android.poAndroid.pokeinfo;

import android.util.SparseArray;
import android.util.SparseIntArray;
import com.podevs.android.poAndroid.poke.Gen;

public class GenInfo {
	private static int genMin = 0;
	private static int genMax = 0;
	private static SparseArray<String> genNames = null;
	private static SparseArray<String> versionNames = null;
	private static SparseIntArray maxSubgen = null;

	public static Gen lastGen() {
		return new Gen(genMax, maxSubgen(genMax));
	}

	public static int genMin() {
		loadGenNames();
		return genMin;
	}

	public static int genMax() {
		loadGenNames();
		return genMax;
	}

	public static int maxSubgen(int gen) {
		loadGenNames();
		return maxSubgen.get(gen);
	}

	public static String name(int gen) {
		loadGenNames();
		return genNames.get(gen);
	}

	public static Gen version(String name) {
		return new Gen(versionNames.keyAt(versionNames.indexOfValue(name)));
	}

	public static String name(Gen gen) {
		loadGenNames();
		return versionNames.get(gen.hashCode());
	}

	private static void loadGenNames() {
		if (genNames != null) {
			return;
		}
		genNames = new SparseArray<String>();
		versionNames = new SparseArray<String>();
		maxSubgen = new SparseIntArray();

		InfoFiller.fill("db/gens/gens.txt", new InfoFiller.Filler() {
			@Override
			public void fill(int i, String s) {
				genNames.put(i, s);
				if (i < genMin || (genMin == 0)) {
					genMin = i;
				}
				if (i > genMax || (genMax == 0)) {
					genMax = i;
				}
				genNames.put(i, s);
			}
		});

		InfoFiller.uIDfill("db/gens/versions.txt", new InfoFiller.Filler() {
			@Override
			public void fill(int i, String s) {
				int gen = i % 65536;
				int subgen = i / 65536;

				if (maxSubgen.get(gen) < subgen) {
					maxSubgen.put(gen, subgen);
				}

				versionNames.put(i, s);
			}
		});
	}
}
