package com.pokebros.android.pokemononline.player;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

// Short version of a player, only nick and info
public class BasicPlayerInfo implements SerializeBytes {
	public String nick = "", info = "";
	
	public BasicPlayerInfo(Bais msg) {
		nick = msg.readString();
		info = msg.readString();
	}
	
	public void serializeBytes(Baos b) {
		b.putString(nick);
		b.putString(info);
	}
}
