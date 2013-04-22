package com.podevs.android.pokemononline.pms;

import java.util.TreeMap;

import com.podevs.android.pokemononline.player.PlayerInfo;

public class PrivateMessageList {
	protected TreeMap<Integer, PrivateMessage> privateMessages = new TreeMap<Integer, PrivateMessage>();
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
		if (!privateMessages.containsKey(info.id)) {
			privateMessages.put(info.id, new PrivateMessage(info, system.currentTimeMillis(), me));
			
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
		privateMessages.get(playerInfo.id).addMessage(playerInfo, system.currentTimeMillis())
	
	interface PrivateMessageListListener {
		void onNewPM(PrivateMessage privateMessage);

		void onRemovePM(int id);
	}

	public void removePM(int id) {
		if (privateMessages.containsKey(id)) {
			privateMessages.remove(id);
			
			if (listener != null) {
				listener.onRemovePM(id);
			}
		}
	}

	public int count() {
		return privateMessages.size();
	}
}
