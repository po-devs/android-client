package com.podevs.android.pokemononline.test;

import android.test.AndroidTestCase;

import com.podevs.android.pokemononline.battle.Type;
import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.pokeinfo.AbilityInfo;
import com.podevs.android.pokemononline.pokeinfo.InfoConfig;
import com.podevs.android.pokemononline.pokeinfo.ItemInfo;
import com.podevs.android.pokemononline.pokeinfo.MoveInfo;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;

public class TestDatabase extends AndroidTestCase {
	public void testPokemonNames() {
		InfoConfig.context = getContext();
		
		assertEquals("Pikachu", PokemonInfo.name(new UniqueID(25, 0)));
	}

	public void testMoves() {
		InfoConfig.context = getContext();
		
		assertEquals("Focus Punch", MoveInfo.name(264));
		assertEquals("150", MoveInfo.power(264));
		assertEquals(Type.Fighting.ordinal(), MoveInfo.type(264));
	}
	
	public void testAbilityNames() {
		InfoConfig.context = getContext();
		
		assertEquals("Stall", AbilityInfo.name(100));
	}
	
	public void testItemNames() {
		InfoConfig.context = getContext();
		
		assertEquals("Odd Incense", ItemInfo.name(20));
		assertEquals("Leppa Berry", ItemInfo.name(8005));
	}
}
