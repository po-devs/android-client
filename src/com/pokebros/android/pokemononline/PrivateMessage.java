package com.pokebros.android.pokemononline;

import java.util.LinkedList;
import java.util.List;

import com.pokebros.android.pokemononline.player.PlayerInfo;

/**
 * Data containing the information of a private message
 *
 */
public class PrivateMessage {
	public PrivateMessage(PlayerInfo other, PlayerInfo me) {
		this.other = other;
		this.me = me;
	}
	
	public void addMessage(PlayerInfo info, String message) {
		if (info.id == this.id()) {
			if (info.nick() != "???") this.other = info;
		} else {
			this.me = info;
		}
		
		messages.add(new Message(info, message, System.currentTimeMillis()));
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
	
	List<Message> messages = new LinkedList<PrivateMessage.Message>();
	PlayerInfo me, other;
}
