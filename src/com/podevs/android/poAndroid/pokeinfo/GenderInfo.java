package com.podevs.android.poAndroid.pokeinfo;

import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.registry.RegistryActivity;

public class GenderInfo {
    private static String [] genders;

    {
        loadGenderNames();
    }

    public enum Gender {
        Neutral,
        Male,
        Female
    }

    public static String name(int num) {
        if (genders == null) {
            loadGenderNames();
        }
        return genders[num];
    }

    private static void loadGenderNames() {
        genders = new String[Gender.values().length];
        String path;
        if (RegistryActivity.localize_assets) {
            path = "db/genders/" + InfoConfig.resources.getString(R.string.asset_localization) + "genders.txt";
            if (!InfoConfig.fileExists(path)) {
                path = "db/genders/genders.txt";
            }
        } else {
            path = "db/genders/genders.txt";
        }
        InfoFiller.fill(path, new InfoFiller.Filler() {
            int index = 0;
            public void fill(int i, String s) {
                genders[index] = s;
                index++;
            }
        });
    }

    public static int indexOf(String gender) {
        if (genders == null) {
            loadGenderNames();
        }
        for (int i = 0; i < genders.length; i++) {
            if (gender.equals(genders[i])) return i;
        }
        return -1;
    }
}
