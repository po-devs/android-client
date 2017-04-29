package com.podevs.android.poAndroid;

import com.podevs.android.poAndroid.poke.PokeEnums.Status;
import com.podevs.android.poAndroid.poke.PokeEnums.Weather;
import com.podevs.android.poAndroid.poke.PokeEnums.Terrain;

public class ColorEnums {
	public enum QtColor {
		White{ public String toString() { return "#ffffff>"; } },
		Black{ public String toString() { return "#000000>"; } },
		Red{ public String toString() { return "#ff0000>"; } },
		DarkRed{ public String toString() { return "#800000>"; } },
		Green{ public String toString() { return "#00ff00>"; } },
		DarkGreen{ public String toString() { return "#008000>"; } },
		Blue{ public String toString() { return "#0000ff>"; } },
		DarkBlue{ public String toString() { return "#000080>"; } },
		Cyan{ public String toString() { return "#00ffff>"; } },
		DarkCyan{ public String toString() { return "#008080>"; } },
		Magenta{ public String toString() { return "#ff00ff>"; } },
		DarkMagenta{ public String toString() { return "#800080>"; } },
		Yellow{ public String toString() { return "#ffff00>"; } },
		DarkYellow{ public String toString() { return "#808000>"; } },
		Gray{ public String toString() { return "#a0a0a4>"; } },
		DarkGray{ public String toString() { return "#808080>"; } },
		LightGray{ public String toString() { return "#c0c0c0>"; } }
	}
	
	public enum TypeColor {
		Normal { public String toString() { return "#a8a878>"; } },
		Fighting{ public String toString() { return "#c03028>"; } },
		Flying{ public String toString() { return "#a890f0>"; } },
		Poison{ public String toString() { return "#a040a0>"; } },
		Ground{ public String toString() { return "#e0c068>"; } },
		Rock{ public String toString() { return "#b8a038>"; } },
		Bug{ public String toString() { return "#a8b820>"; } },
		Ghost{ public String toString() { return "#705898>"; } },
		Steel{ public String toString() { return "#b8b8d0>"; } },
		Fire{ public String toString() { return "#f08030>"; } },
		Water{ public String toString() { return "#6890f0>"; } },
		Grass{ public String toString() { return "#78c850>"; } },
		Electric{ public String toString() { return "#f8d030>"; } },
		Psychic{ public String toString() { return "#f85888>"; } },
		Ice{ public String toString() { return "#98d8d8>"; } },
		Dragon{ public String toString() { return "#7038f8>"; } },
		Dark{ public String toString() { return "#705848>"; } },
		Fairy{ public String toString() { return "#f088f6>"; } },
		Curse{ public String toString() { return "#68a090>"; } }
	}
		
	public static class StatusColor {
		private static String color;
		public StatusColor(int status) {
			switch (Status.poValues()[status]) {
			case Koed: color = "#171ba>"; break;
			case Fine: color = TypeColor.Normal.toString(); break;
			case Paralysed: color = TypeColor.Electric.toString(); break;
			case Burnt: color = TypeColor.Fire.toString(); break;
			case Frozen: color = TypeColor.Ice.toString(); break;
			case Asleep: color = TypeColor.Psychic.toString(); break;
			case Poisoned: color = TypeColor.Poison.toString(); break;
			case Confused: color = TypeColor.Ghost.toString(); break;
			default: color = ">";
			}
		}
		public String toString() {
			return color;
		}
	}
	
	public static class TypeForWeatherColor {
		private static String color;
		public TypeForWeatherColor(int weather) {
			switch (Weather.values()[weather]) {
				case Hail: color = TypeColor.Ice.toString(); break;
				case Rain: color = TypeColor.Water.toString(); break;
				case SandStorm: color = TypeColor.Rock.toString(); break;
				case Sunny: color = TypeColor.Fire.toString(); break;
				case HeavyRain: color = TypeColor.Water.toString(); break;
				case HeavySun: color = TypeColor.Fire.toString(); break;
				case Delta: color = TypeColor.Flying.toString(); break;
				default: color = TypeColor.Normal.toString(); break;
			}
		}
		public String toString() {
			return color;
		}
	}

	public static class TypeForTerrainColor {
		private static String color;
		public TypeForTerrainColor(int terrain) {
			switch (Terrain.values()[terrain]) {
				case Electric: color = TypeColor.Electric.toString(); break;
				case Grassy: color = TypeColor.Grass.toString(); break;
				case Misty: color = TypeColor.Fairy.toString(); break;
				case Psychic: color = TypeColor.Psychic.toString(); break;
				default: color = TypeColor.Normal.toString(); break;
			}
		}
		public String toString() {
			return color;
		}
	}

	public static String[] defaultPlayerColors = {
		"#5811b1",
		"#399bcd",
		"#0474bb",
		"#f8760d",
		"#a00c9e",
		"#0d762b",
		"#5f4c00",
		"#9a4f6d",
		"#d0990f",
		"#1b1390",
		"#028678",
		"#0324b1"
	};
}