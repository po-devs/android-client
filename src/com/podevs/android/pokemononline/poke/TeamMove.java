package com.podevs.android.pokemononline.poke;

import com.podevs.android.pokemononline.pokeinfo.MoveInfo;

public class TeamMove implements Move {
	short num;
	
	public TeamMove(int i) {
		num = (short)i;
	}

	public int num() {
		return num;
	}
	
	public String toString() {
		return MoveInfo.name(num);
	}
}
