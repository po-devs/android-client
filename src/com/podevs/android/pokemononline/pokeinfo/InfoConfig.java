package com.podevs.android.pokemononline.pokeinfo;

import android.content.Context;
import android.content.res.Resources;

public class InfoConfig {
	static public Context context = null;
	static public Resources resources = null;
	final public static String pkgName = "com.podevs.android.pokemononline";
	
	public static void setContext(Context ctx) {
		context = ctx;
		resources = ctx.getResources();
	}
}
