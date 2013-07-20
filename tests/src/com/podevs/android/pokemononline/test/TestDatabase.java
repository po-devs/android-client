package com.podevs.android.pokemononline.test;

import android.test.AndroidTestCase;

import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.pokeinfo.InfoConfig;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;

public class TestDatabase extends AndroidTestCase {
	public void testPokemonNames() {
		InfoConfig.context = getContext();
		
		assertEquals("Pikachu", PokemonInfo.name(new UniqueID(25, 0)));
	}
}
