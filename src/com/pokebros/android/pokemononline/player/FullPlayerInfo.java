package com.pokebros.android.pokemononline.player;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.PokeParser;

// Contains all the information about the player.
// Used for logging into the server.
public class FullPlayerInfo implements SerializeBytes {
	public PlayerTeam playerTeam;
	public boolean isDefault = true;
	
	public FullPlayerInfo(Bais msg) {
		playerTeam = new PlayerTeam(msg);
	}
	
	public FullPlayerInfo(Context context, SharedPreferences prefs) {
		try {
			context.openFileInput(prefs.getString("teamFile", "team.xml"));
			playerTeam = new PlayerTeam(context, new PokeParser(context));
			isDefault = false;
		} catch (FileNotFoundException e) {
			Toast.makeText(context, "No team found. Loaded system default.", Toast.LENGTH_LONG).show();
			playerTeam = new PlayerTeam(context, prefs.getString("lastName", null));
		} catch (NumberFormatException e) {
			// The file could not be parsed correctly
			Toast.makeText(context, "Invalid team found. Loaded system default.", Toast.LENGTH_LONG).show();
			playerTeam = new PlayerTeam(context, prefs.getString("lastName", null));
		}
	}
	
	public String nick() { return playerTeam.nick(); }
	
	public void serializeBytes(Baos bytes) {
		bytes.putBaos(playerTeam);
	}
}
