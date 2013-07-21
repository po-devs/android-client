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
	public void testPokemons() {
		InfoConfig.context = getContext();
		
		assertEquals("Pikachu", PokemonInfo.name(new UniqueID(25, 0)));
		assertEquals(12, PokemonInfo.type1(new UniqueID(25, 0), 5));
		assertEquals(17, PokemonInfo.type2(new UniqueID(25, 0), 5));
	}

	public void testMoves() {
		InfoConfig.context = getContext();
		
		assertEquals("Focus Punch", MoveInfo.name(264));
		assertEquals("150", MoveInfo.power(264));
		assertEquals(Type.Fighting.ordinal(), MoveInfo.type(264));
		assertEquals("%f can no longer escape!", MoveInfo.message(12, 0));
		assertEquals("It's a one hit KO!", MoveInfo.message(43, 1));
	}
	
	public void testAbilities() {
		InfoConfig.context = getContext();
		
		assertEquals("Stall", AbilityInfo.name(100));
		assertEquals("%s's Effect Spore activates!", AbilityInfo.message(16, 0));
	}
	
	public void testItems() {
		InfoConfig.context = getContext();
		
		assertEquals("Odd Incense", ItemInfo.name(20));
		assertEquals("Leppa Berry", ItemInfo.name(8005));
		assertEquals("The %i sharply raised %s's %st!", ItemInfo.message(8009, 0));
		assertEquals("The %i sharply lowered %s's %st!", ItemInfo.message(8009, 1));
	}
}
