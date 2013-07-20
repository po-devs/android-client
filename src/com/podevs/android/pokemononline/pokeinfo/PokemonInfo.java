package com.podevs.android.pokemononline.pokeinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.SparseArray;

import com.podevs.android.pokemononline.poke.UniqueID;

public class PokemonInfo {
	private static SparseArray<String> pokeNames = new SparseArray<String>();
	
	public static String name(UniqueID uID) {
		int num = uID.hashCode();
		
		if (pokeNames.indexOfKey(num) < 0) {
			loadPokeNames();
		}
		
		return pokeNames.get(num);
	}
	
	private static void loadPokeNames() {
		InputStream assetsDB = null;
		try {
			assetsDB = getContext().getAssets().open("db/pokes/pokemons.txt");
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
				
				int key = new UniqueID(str.substring(0, spaceIndex)).hashCode();
				String val = str.substring(spaceIndex + 1); 
				pokeNames.put(key, val);
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
