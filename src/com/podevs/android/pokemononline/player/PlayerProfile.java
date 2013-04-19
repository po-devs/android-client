package com.podevs.android.pokemononline.player;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;

import com.podevs.android.pokemononline.QColor;
import com.podevs.android.pokemononline.SerializeBytes;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

public class PlayerProfile implements SerializeBytes {
	public String nick;
	public QColor color;
	
	public class TrainerInfo implements SerializeBytes {
		public short avatar = 72;
		public String winMsg, loseMsg, tieMsg, info;
		
		public TrainerInfo(Bais msg) {
			/* Version control! */
			Bais b = new Bais(msg.readVersionControlData());
			byte version = b.readByte();
			
			if (version != 0) {
				
			}
			
			Bais network = b.readFlags();

			avatar = b.readShort();
			info = b.readString();
			
			if (network.readBool()) {
				winMsg = b.readString();
				loseMsg = b.readString();
				tieMsg = b.readString();			
			} else {
				winMsg = "";
				loseMsg = "";
				tieMsg = "";
			}
		}
		
		public TrainerInfo(int avatar, String info, String winMsg, String loseMsg, String tieMsg) {
			this.avatar = (short)avatar;
			this.info = info;
			this.winMsg = winMsg;
			this.loseMsg = loseMsg;
			this.tieMsg = tieMsg;
		}

		public void serializeBytes(Baos b) {
			Baos output = new Baos();
			boolean messages = winMsg.length() > 0 || loseMsg.length() > 0 || tieMsg.length() > 0;
			output.putBool(messages);
			output.putShort(avatar);
			output.putString(info);
			if (messages) {
				output.putString(winMsg);
				output.putString(loseMsg);
				output.putString(tieMsg);
			}
			
			b.putVersionControl(0, output);
		}
	}
	
	public TrainerInfo trainerInfo;
	
	public PlayerProfile() {
		Random r = new Random();
		
		nick = "guest" + r.nextInt(65536);
		color = new QColor("white");
		
		trainerInfo = new TrainerInfo(72, "Android player", "", "", "");
	}
	
	public PlayerProfile(Bais msg) {
		nick = msg.readString();
		color = new QColor(msg);

		trainerInfo = new TrainerInfo(msg);
	}
	
	public PlayerProfile(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
		
		Random r = new Random();
		
		nick = prefs.getString("name", "guest" + r.nextInt(65536));
		color = new QColor(prefs.getString("color", "white")); //Default color, so that server won't pick it up
		
		trainerInfo = new TrainerInfo(prefs.getInt("avatar", 72), prefs.getString("info", "Android player."),
				prefs.getString("winMsg", ""), prefs.getString("loseMsg", ""), prefs.getString("tieMsg", ""));
	}
	
	public void serializeBytes(Baos output) {
		output.putString(nick);
		output.putBaos(color);
		output.putBaos(trainerInfo);
	}

	public void save(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString("name", nick);
		editor.putString("info", trainerInfo.info);
		editor.putString("winMsg", trainerInfo.winMsg);
		editor.putString("loseMsg", trainerInfo.loseMsg);
		editor.putString("tieMsg", trainerInfo.tieMsg);
		editor.putInt("avatar", trainerInfo.avatar);
		//editor.putString("color", color.toHexString());
		
		editor.commit();
	}
}
