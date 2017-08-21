package com.podevs.android.poAndroid.pokeinfo;

import android.util.SparseArray;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.pokeinfo.InfoFiller.Filler;
import com.podevs.android.poAndroid.registry.RegistryActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class AbilityInfo {
	private static ArrayList<String> abilityNames = null;
	private static SparseArray<String> abilityMessages = null;
    private static Short[][] allAbilities = null;
	
	public static String name(int item) {
		if (abilityNames == null) {
			loadAbilityNames();
		}
		
		return abilityNames.get(item);
	}

	public static int indexOf(String s) {
		if (abilityNames == null) {
			loadAbilityNames();
		}

		return abilityNames.indexOf(s);
	}
	
	public static String message(int num, int part) {
		if (abilityMessages == null) {
			 loadAbilityMessages();
		}
		
		String parts [] = (abilityMessages.get(num, "")).split("\\|");
		try {
			return parts[part];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return "";
		}
	}

	private static void loadAbilityNames() {
		abilityNames = new ArrayList<String>();
		String path;
		if (RegistryActivity.localize_assets) {
			path = "db/abilities/" + InfoConfig.resources.getString(R.string.asset_localization) + "abilities.txt";
			if (!InfoConfig.fileExists(path)) {
				path = "db/abilities/abilities.txt";
			}
		} else {
			path = "db/abilities/abilities.txt";
		}
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String b) {
				abilityNames.add(b);
			}
		});
        loadAllArray();
	}
	
	private static void loadAbilityMessages() {
		abilityMessages = new SparseArray<String>();
		String path;
		if (RegistryActivity.localize_assets) {
			path = "db/abilities/" + InfoConfig.resources.getString(R.string.asset_localization) + "ability_messages.txt";
			if (!InfoConfig.fileExists(path)) {
				path = "db/abilities/ability_messages.txt";
			}
		} else {
			path = "db/abilities/ability_messages.txt";
		}
		InfoFiller.fill(path, new Filler() {
			public void fill(int i, String b) {
				abilityMessages.put(i, b);
			}
		});
	}

    private static void loadAllArray() {
		allAbilities = new Short[5][];
        allAbilities[4] = new Short[abilityNames.size() - 1];
        for (int i = 0; i <= allAbilities[4].length - 1; i++) {
            allAbilities[4][i] = (short) (i + 1);
        }

        allAbilities[3] = Arrays.copyOf(allAbilities[4], 191);
		allAbilities[2] = Arrays.copyOf(allAbilities[3], 164);
		allAbilities[1] = Arrays.copyOf(allAbilities[2], 123);
		allAbilities[0] = Arrays.copyOf(allAbilities[1], 76);

		for (int i = 0; i < 5; i++) {
			java.util.Arrays.sort(allAbilities[i], new Comparator<Short>() {
				@Override
				public int compare(Short lhs, Short rhs) {
					return name(lhs).compareToIgnoreCase(name(rhs));
				}
			});
		}
    }

    public static Short[] getAllAbilities(int gen) {
        return allAbilities[gen - 3];
    }
}
