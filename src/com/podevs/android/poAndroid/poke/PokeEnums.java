package com.podevs.android.poAndroid.poke;

import com.podevs.android.poAndroid.R;

public class PokeEnums {

	public enum Status { // XXX this is a monster
		Fine { public final int poValue() { return 0; } },
		Paralysed { public final int poValue() { return 1; } }, 
		Asleep { public final int poValue() { return 2; } },
		Frozen { public final int poValue() { return 3; } },
		Burnt { public final int poValue() { return 4; } },
		Poisoned { public final int poValue() { return 5; } },
		Confused { public final int poValue() { return 6; } },
		Attracted { public final int poValue() { return 7; } },
		Wrapped { public final int poValue() { return 8; } },
		Nightmared { public final int poValue() { return 9; } },
		Tormented { public final int poValue() { return 12; } },
		Disabled { public final int poValue() { return 13; } },
		Drowsy { public final int poValue() { return 14; } },
		HealBlocked { public final int poValue() { return 15; }  },
		Sleuthed { public final int poValue() { return 17; } },
		Seeded { public final int poValue() { return 18; } },
		Embargoed { public final int poValue() { return 19; } },
		Requiemed { public final int poValue() { return 20; } },
		Rooted { public final int poValue() { return 21; } },
		Koed  { public final int poValue() { return 31; } };
		public abstract int poValue();
		public final static Status[] poValues() {
			Status[] values = new Status[Status.Koed.poValue()+1];
			for (Status i : Status.values())
				values[i.poValue()] = i;
			return values;
		}
	}

	public enum StatusFeeling {
		FeelConfusion,
		HurtConfusion,
		FreeConfusion,
		PrevParalysed,
		PrevFrozen,
		FreeFrozen,
		FeelAsleep,
		FreeAsleep,
		HurtBurn,
		HurtPoison
	}
	
	public enum StatusKind {
		NoKind,
		SimpleKind,
		TurnKind,
		AttractKind,
		WrapKind
	}

	/* For simplicity issues we keep the same order as in Gender. You can assume it'll stay
   that way for next versions.

   That allows you to do PokemonInfo::Picture(pokenum, (Gender)GenderAvail(pokenum)) */

	public enum GenderAvail {
		NeutralAvail,
		MaleAvail,
		FemaleAvail,
		MaleAndFemaleAvail
	}
	
	public enum Stat {
		HP { public final int rstring() { return R.string.empty; } },
		Attack { public final int rstring() { return R.string.stat_attack; } },
		Defense { public final int rstring() { return R.string.stat_defense; } },
		SpAttack  { public final int rstring() { return R.string.stat_spAttack; } },
		SpDefense { public final int rstring() { return R.string.stat_spDefense; } },
		Speed { public final int rstring() { return R.string.stat_speed; } },
		Accuracy { public final int rstring() { return R.string.stat_accuracy; } },
		Evasion { public final int rstring() { return R.string.stat_evasion; } },
		AllStats { public final int rstring() { return R.string.empty; } };
		public abstract int rstring();
	}
	
	// XXX These should probably go in something like BattleEnums, but I'm not sure
	//           what else would go in it. Putting them here for now.
	public enum Weather {
		NormalWeather,
		Hail,
		Rain,
		SandStorm,
		Sunny,
        HeavySun,
        HeavyRain,
		Delta  // delta stream
	}
	
	public enum WeatherState {
		ContinueWeather,
		EndWeather,
		HurtWeather
	}

	public enum Terrain {
		NoTerrain,
		Electric,
		Grassy,
		Misty,
		Psychic
	}

	public enum TerrainState {
		EndTerrain
	}
}
