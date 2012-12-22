package com.pokebros.android.pokemononline.player;

import android.content.Context;
import android.widget.Toast;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.PokeParser;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.Team;

// Player as represented in the teambuilder.
public class PlayerTeam extends SerializeBytes {
	public String nick;
	protected String info = "This is the default team. Please import your own team from the PC client.";
	protected String loseMsg = "";
	protected String winMsg = "";
	protected String defaultTier = "";
	protected String tier = "";
	protected Team team;
	short avatar = 72;
	
	public String nick() { return nick; }
	
	public PlayerTeam(Bais msg) {
		nick = msg.readString();
		info = msg.readString();
		loseMsg = msg.readString();
		winMsg = msg.readString();
		avatar = msg.readShort();
		defaultTier = msg.readString();
		team = new Team(msg);
	}
	
	public PlayerTeam(Context context, PokeParser p) {
		nick = p.getNick();
		if (nick == null) {		
			Toast.makeText(context, "No trainer name found. Please enter a unique name before connecting to a server.", Toast.LENGTH_LONG).show();
			nick = "";
		}
		info = p.getInfo();
		loseMsg = p.getLoseMsg();
		winMsg = p.getWinMsg();
		avatar = p.getAvatar();
		defaultTier = p.getDefaultTier();
		team = new Team(p);
	}
	
	public PlayerTeam(Context context, String name) {
		nick = name;
		if (nick == null) {		
			Toast.makeText(context, "No trainer name found. Please enter a unique name before connecting to a server.", Toast.LENGTH_LONG).show();
			nick = "";
		}
		team = new Team();
	}
	
	public String toString() {
		return nick;
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putString(nick);
		bytes.putString(info);
		bytes.putString(loseMsg);
		bytes.putString(winMsg);	
		bytes.putShort(avatar);
		bytes.putString(defaultTier);
		bytes.putBaos(team);
		return bytes;
	}

}
