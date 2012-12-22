package com.pokebros.android.pokemononline.player;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.PokeParser;
import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;

// Contains all the information about the player.
// Used for logging into the server.
public class FullPlayerInfo implements SerializeBytes {
	public PlayerTeam playerTeam;
	public boolean isDefault = true;
	protected boolean ladderEnabled = true;
	protected boolean showTeam = false;
	protected QColor nameColor = new QColor();
	
	public FullPlayerInfo(Bais msg) {
		playerTeam = new PlayerTeam(msg);
		ladderEnabled = msg.readBool();
		showTeam = msg.readBool();
		nameColor = new QColor(msg);
	}
	
	public FullPlayerInfo(Context context, SharedPreferences prefs) {
		try {
			context.openFileInput("team.xml");
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
	
	public String toString() { return playerTeam.nick(); }
	public String nick() { return playerTeam.nick(); }
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putBaos(playerTeam);
		bytes.putBool(ladderEnabled);
		bytes.putBool(showTeam);
		bytes.putBaos(nameColor);
		return bytes;
	}
}
