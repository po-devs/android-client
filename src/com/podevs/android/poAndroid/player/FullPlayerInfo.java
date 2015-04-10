package com.podevs.android.poAndroid.player;

import android.content.Context;
import android.widget.Toast;
import com.podevs.android.poAndroid.poke.PokeParser;
import com.podevs.android.poAndroid.poke.Team;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.QColor;
import com.podevs.android.utilities.SerializeBytes;

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
	
	public FullPlayerInfo(Context context) {
		profile = new PlayerProfile(context);
		
		try {
			team = (new PokeParser(context)).getTeam();
			isDefault = false;
		} catch (NumberFormatException e) {
			// The file could not be parsed correctly
			e.printStackTrace();
			Toast.makeText(context, "Invalid team found. Loaded system default.", Toast.LENGTH_LONG).show();
			
			//String teamS = StringUtilities.getFileContent(context, context.getSharedPreferences("team", 0).getString("file", "team.xml"));
			team = new Team();
		}
	}
	
	public FullPlayerInfo() {
		team = new Team();
		profile = new PlayerProfile();
	}

	public String nick() { return profile.nick; }
	public QColor color() { return profile.color; }
	
	public void serializeBytes(Baos bytes) {
		bytes.putBaos(profile);
		bytes.write(1); // only one team
		bytes.putBaos(team);
	}
}
