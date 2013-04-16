package com.pokebros.android.pokemononline.player;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;

import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

public class PlayerProfile implements SerializeBytes {
	public String nick, winMsg, loseMsg, tieMsg, info;
	public short avatar = 72;
	
	public PlayerProfile(Bais msg) {
		nick = msg.readString();
		avatar = msg.readShort();
		info = msg.readString();
		winMsg = msg.readString();
		loseMsg = msg.readString();
		tieMsg = msg.readString();
	}
	
	public PlayerProfile(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
		
		Random r = new Random();
		
		nick = prefs.getString("name", "guest" + r.nextInt(65536));
		avatar = (short) prefs.getInt("avatar", 72);
		info = prefs.getString("info", "This is the default team. Please import your own team from the PC client.");
		winMsg = prefs.getString("winMsg", "");
		loseMsg = prefs.getString("loseMsg", "");
		tieMsg = prefs.getString("tieMsg", "");
	}
	
	public void serializeBytes(Baos output) {
		output.putString(nick);
		output.putShort(avatar);
		output.putString(info);
		output.putString(winMsg);
		output.putString(loseMsg);
		output.putString(tieMsg);
	}

	public void save(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString("name", nick);
		editor.putString("info", info);
		editor.putString("winMsg", winMsg);
		editor.putString("loseMsg", loseMsg);
		editor.putString("tieMsg", tieMsg);
		editor.putInt("avatar", avatar);
		
		editor.commit();
	}
}
