package com.podevs.android.poAndroid.pokeinfo;


public class DamageClassInfo {
    public enum damageClass {
        Other, //0
        Physical, //1
        Special, // 2
        Varies // 3
    }

    public static String name(int num) {
        return damageClass.values()[num].toString();
    }

    public static int damageClassRes(int num) {
        return InfoConfig.context.getResources().getIdentifier("cat_" + num, "drawable", InfoConfig.pkgName);
    }
}
