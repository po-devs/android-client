package com.podevs.android.poAndroid.pms;

import android.widget.ListView;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.utilities.StringUtilities;

import java.util.LinkedList;

/**
 * Data containing the information of a private message
 *
 */
public class PrivateMessage {
	PrivateMessageListener listener;
    LinkedList<Message> messages = new LinkedList<PrivateMessage.Message>();
    PlayerInfo me, other;
    protected ListView privateList = null;

	public PrivateMessage(PlayerInfo other, PlayerInfo me) {
		this.other = other;
		this.me = me;
	}

	public void addMessage(PlayerInfo info, String message, Boolean timeStamp) {
		if (info.id == this.id() && !info.nick().equals("???")) {
			this.other = info;
		}

		if (!message.trim().isEmpty()) {
			if (timeStamp) {
				message = "(" + StringUtilities.timeStamp() + ") " + message;
			}

			if (!messages.isEmpty()) {
				Message lastMessage = messages.getLast();
				if (lastMessage != null && lastMessage.sender == info) {
					lastMessage.append(message);
				} else {
					messages.add(new Message(info, message));
				}
			} else {
				messages.add(new Message(info, message));
			}

			if (listener != null) {
				listener.onNewMessage(messages.getLast());
			}
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
		public Message(PlayerInfo info, String message) {
			this.sender = info;
			this.message = message;
		}
		PlayerInfo sender;
		String message;

		void append(String s) {
			message = message + "\n" + s;
		}
	}

	interface PrivateMessageListener {
		void onNewMessage(Message message);
	}
}