package com.podevs.android.pokemononline.pms;

import java.util.LinkedList;

import com.podevs.android.pokemononline.player.PlayerInfo;

/**
 * Data containing the information of a private message
 *
 */
public class PrivateMessage {
	PrivateMessageListener listener;
	
	public PrivateMessage(PlayerInfo other, PlayerInfo me) {
		this.other = other;
		this.me = me;
	}
	
	public void addMessage(PlayerInfo info, String message) {
		if (info.id == this.id() && info.nick() != "???") {
			this.other = info;
		}
		  
		messages.add(new Message(info, message, System.currentTimeMillis()));
		
		if (listener != null) {
			listener.onNewMessage(messages.getLast());
		}
	}

	@Override
	public int hashCode() {
		return id();
	}
	
	private int id() {
		return other.id;
	}
	
	public class Message {
		public Message(PlayerInfo info, String message, long currentTimeMillis) {
			this.sender = info;
			this.message = message;
			this.time = currentTimeMillis;
		}
		PlayerInfo sender;
		String message;
		long time; //timestamp of the message
	}
	
	LinkedList<Message> messages = new LinkedList<PrivateMessage.Message>();
	PlayerInfo me, other;
	
	interface PrivateMessageListener {
		void onNewMessage(Message message);
	}
}
