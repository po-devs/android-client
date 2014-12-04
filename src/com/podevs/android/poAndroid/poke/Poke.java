package com.podevs.android.poAndroid.poke;

public interface Poke {

	int ability();

	int item();

	int totalHP();

	int currentHP();

	CharSequence nick();

	UniqueID uID();
	
	Gen gen();

	Move move(int j);

	int hiddenPowerType();

	int dv(int i);

	int ev(int i);

	int level();

	int nature();

	int gender();
}
