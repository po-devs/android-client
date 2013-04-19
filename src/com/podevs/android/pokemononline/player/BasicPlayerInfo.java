package com.podevs.android.pokemononline.player;

import com.podevs.android.pokemononline.SerializeBytes;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

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
