package com.pokebros.android.pokemononline.pms;

import java.util.Hashtable;

import com.pokebros.android.pokemononline.player.PlayerInfo;

public class PrivateMessageList {
	protected Hashtable<Integer, PrivateMessage> privateMessages = new Hashtable<Integer, PrivateMessage>();
	PlayerInfo me = new PlayerInfo();
	PrivateMessageListListener listener;
	
	public PrivateMessageList(PlayerInfo me) {
		this.me = me;
	}
	
	/**
	 * Creates a PM window with the other guy
	 * @param playerId the other guy's id
	 */
	public void createPM(PlayerInfo info) {
		if (privateMessages.containsKey(info.id)) {
			privateMessages.put(info.id, new PrivateMessage(info, me));
			
			if (listener != null) {
				listener.onNewPM(privateMessages.get(info.id));
			}
		}
	}

	/**
	 * New message
	 * @param playerInfo
	 * @param message
	 */
	public void newMessage(PlayerInfo playerInfo, String message) {
		createPM(playerInfo);
		privateMessages.get(playerInfo.id).addMessage(playerInfo, message);
	}
	
	interface PrivateMessageListListener {
		void onNewPM(PrivateMessage privateMessage);
	}
}
