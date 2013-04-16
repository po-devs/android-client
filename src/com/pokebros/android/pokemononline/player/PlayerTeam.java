package com.pokebros.android.pokemononline.player;

import android.content.Context;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.PokeParser;
import com.pokebros.android.pokemononline.poke.Team;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

// Player as represented in the teambuilder.
public class PlayerTeam implements SerializeBytes {
	public PlayerProfile profile;
	public Team team;
	
	public String nick() { return profile.nick; }
	
	public PlayerTeam(Bais msg) {
		profile = new PlayerProfile(msg);
		team = new Team(msg);
	}
	
	public PlayerTeam(Context context, PokeParser p) {
		team = p.getTeam();
		profile = new PlayerProfile(context);
	}
	
	public PlayerTeam(Context context) {
		team = new Team();
		profile = new PlayerProfile(context);
	}
		
	public void serializeBytes(Baos bytes) {
		bytes.putBaos(profile);
		bytes.putBaos(team);
	}
}
