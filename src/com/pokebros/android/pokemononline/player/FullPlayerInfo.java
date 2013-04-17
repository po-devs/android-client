package com.pokebros.android.pokemononline.player;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.PokeParser;
import com.pokebros.android.pokemononline.poke.Team;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

// Contains all the information about the player.
// Used for logging into the server.
public class FullPlayerInfo implements SerializeBytes {
	public PlayerProfile profile;
	public Team team;
	
	
	
	public boolean isDefault = true;
	
	public FullPlayerInfo(Bais msg) {
		/* First: profile */
		profile = new PlayerProfile(msg);
		
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
	
	public FullPlayerInfo(Context context, SharedPreferences prefs) {
		profile = new PlayerProfile(context);
		
		try {
			context.openFileInput(prefs.getString("teamFile", "team.xml"));
			team = (new PokeParser(context)).getTeam();
			isDefault = false;
		} catch (FileNotFoundException e) {
			Toast.makeText(context, "No team found. Loaded system default.", Toast.LENGTH_LONG).show();
			team = new Team();
		} catch (NumberFormatException e) {
			// The file could not be parsed correctly
			Toast.makeText(context, "Invalid team found. Loaded system default.", Toast.LENGTH_LONG).show();
			team = new Team();
		}
	}
	
	public String nick() { return profile.nick; }
	public QColor color() { return profile.color; }
	
	public void serializeBytes(Baos bytes) {
		bytes.putBaos(profile);
		bytes.write(1); // only one team
		bytes.putBaos(team);
	}
}
