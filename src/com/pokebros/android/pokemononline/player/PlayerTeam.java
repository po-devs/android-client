package com.pokebros.android.pokemononline.player;

import android.content.Context;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.PokeParser;
import com.pokebros.android.pokemononline.poke.Team;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

// Player as represented in the teambuilder.
public class PlayerTeam implements SerializeBytes {
	public TrainerInfo profile;
	public Team team;
	
	public String nick() { return profile.nick; }
	
	public PlayerTeam(Bais msg) {
		/* First: profile */
		profile = new TrainerInfo(msg);
		
		/* Team count and team(s) */
		byte teamCount = msg.readByte();
		for (int i = 0; i < teamCount; i++) {
			if (i == 0) {
				team = new Team(msg);
			} else {
				new Team(msg); // no support for more than one team
			}
		}
	}
	
	public PlayerTeam(Context context, PokeParser p) {
		team = p.getTeam();
		profile = new TrainerInfo(context);
	}
	
	public PlayerTeam(Context context) {
		team = new Team();
		profile = new TrainerInfo(context);
	}
		
	public void serializeBytes(Baos bytes) {
		bytes.putBaos(profile);
		bytes.write(1); // only one team
		bytes.putBaos(team);
	}
}
