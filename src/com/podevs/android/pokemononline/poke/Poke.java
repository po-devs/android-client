package com.podevs.android.pokemononline.poke;

public interface Poke {

	int ability();

	int item();

	int totalHP();

	int currentHP();

	CharSequence nick();

	UniqueID uID();

	Move move(int j);

	int hiddenPowerType();

	int dv(int i);

	int ev(int i);

	int level();

	int  nature();
}
