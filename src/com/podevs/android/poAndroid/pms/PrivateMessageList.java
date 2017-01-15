package com.podevs.android.poAndroid.pms;

import com.podevs.android.poAndroid.player.PlayerInfo;

import java.util.TreeMap;

public class PrivateMessageList {
	protected TreeMap<Integer, PrivateMessage> privateMessages = new TreeMap<Integer, PrivateMessage>();
	PlayerInfo me = new PlayerInfo();
	PrivateMessageListListener listener;
	
	public PrivateMessageList(PlayerInfo me) {
		this.me = me;
	}
	
	/**
	 * Creates a PM window with the other guy
	 * @param info the other guy's id
	 */
	public void createPM(PlayerInfo info) {
		if (!privateMessages.containsKey(info.id)) {
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
		privateMessages.get(playerInfo.id).addMessage(playerInfo, message, PrivateMessageActivity.getTimeStampPM());
	}
	
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
