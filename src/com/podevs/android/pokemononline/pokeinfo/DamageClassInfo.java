package com.podevs.android.pokemononline.pokeinfo;


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
/*
    Could load images if any where placed in db/res/drawable
    public static int damageClassRes(int num) {
        return InfoConfig.context.getResources().getIdentifier("damageclass" + num, "drawable", InfoConfig.pkgName);
    }
*/
}
