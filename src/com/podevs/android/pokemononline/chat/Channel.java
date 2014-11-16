package com.podevs.android.pokemononline.chat;

import java.util.Hashtable;
import java.util.LinkedList;

import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

import com.podevs.android.pokemononline.Command;
import com.podevs.android.pokemononline.NetworkService;
import com.podevs.android.pokemononline.player.PlayerInfo;
import com.podevs.android.utilities.Bais;

/**
 * Contains all info regarding a channel: name, id, joined,
 * and if joined a player list and chat history.
 *
 */
public class Channel {
	public String name;
	public int id;
	public int lastSeen = 0;
	protected boolean isReadyToQuit = false;
	public boolean joined = false;
	public final static int HIST_LIMIT = 700;
	public static final String TAG = "Pokemon Online Channel";
	
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	public LinkedList<SpannableStringBuilder> messageList = new LinkedList<SpannableStringBuilder>();
	
	public void writeToHist(CharSequence text) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class)
			spannable = new SpannableStringBuilder(text);
		else
			spannable = (SpannableStringBuilder)text;
		synchronized(messageList) {
			messageList.add(spannable);
			lastSeen++;
			while (messageList.size() > HIST_LIMIT)
				messageList.remove();
		}
	}

	public void writeToHist(CharSequence text, int left, int right) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class) {
			spannable = new SpannableStringBuilder(text);
		}
		else {spannable = (SpannableStringBuilder)text;}
		synchronized(messageList) {
			try {
				spannable.setSpan(new BackgroundColorSpan((Color.YELLOW)), left, right, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} finally {

			}
			messageList.add(spannable);
			lastSeen++;
			while (messageList.size() > HIST_LIMIT)
				messageList.remove();
		}
	}
	private NetworkService netServ;
	
	public String name(){ return name; }
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n, NetworkService net) {
		id = i;
		name = n;
		netServ = net;
	}

	public void addPlayer(PlayerInfo p) {
		if(p != null) {
			players.put(p.id, p);
			
			if(netServ != null && netServ.chatActivity != null && this.equals(netServ.chatActivity.currentChannel()))
				netServ.chatActivity.addPlayer(p);
		}
		else
			Log.w(TAG, "Tried to add nonexistant player id to channel " + name + ", ignoring");
	}
	
	public void removePlayer(PlayerInfo p){
		if(p != null){
			players.remove(p.id);
			
			if(netServ != null) {
				if (netServ.chatActivity != null && netServ.chatActivity.currentChannel() == this) {
					netServ.chatActivity.removePlayer(p);
				}
				netServ.onPlayerLeaveChannel(p.id);
			}
		}
		else
			Log.w(TAG, "Tried to remove nonexistant player id from channel " + name + ", ignoring");
	}
	
	private void joined() {
		joined = true;
		if (!netServ.joinedChannels.contains(this)) {
			netServ.joinedChannels.addFirst(this);
			netServ.updateJoinedChannels();
			writeToHist(Html.fromHtml("<i>Joined channel: <b>" + name + "</b></i>"));
		}
	}
	
	public void handleChannelMsg(Command c, Bais msg) {
			switch(c) {
			case ChannelPlayers: {
				int numPlayers = msg.readInt();
				for(int i = 0; i < numPlayers; i++) {
					addPlayer(netServ.players.get(msg.readInt()));
				}
				
				joined();
				
				break;
			} case JoinChannel: {
				int pid = msg.readInt();
				if (pid == netServ.myid) { // We joined the channel
					joined();
				}
				addPlayer(netServ.players.get(pid));
				break;
			}
/*			case ChannelMessage: {
				// decorate the message just like Qt client
				String message = msg.readQString();
				// /me like message
				if (message.length() >= 3 && message.substring(0, 3).equals("***")) {
					// TODO: choose a color near magenta which is knows by android html
					message = "<font color='magenta'>" + NetworkService.escapeHtml(message) + "</font>";
					writeToHist(Html.fromHtml(message));
					break;
				}   
				String[] name_message = message.split(":", 2); 
				// decorate only if : is present
				if (name_message.length == 2) {
					PlayerInfo info = netServ.getPlayerByName(name_message[0]);
					String color;
					boolean auth = false;

					// player exists
					if (info != null) {
						color = info.color.toHexString();
						auth = 0 < info.auth && info.auth <= 3;
						System.out.println(color != null ? "playercolor: " + color : "null color");
						if (color == null) {
							color = ColorEnums.defaultPlayerColors[info.id % ColorEnums.defaultPlayerColors.length];
						}   
					} else {
						// special names
						if (name_message[0].equals("~~Server~~"))
							color = "orange";
						else if (name_message[0].equals("Welcome Message"))
							color = "blue";
						else
							color = "#3daa68";
					}
					if (auth) {
						message = "<b><font color='" + color + "'>+<i>"
								+ NetworkService.escapeHtml(name_message[0]) + ":</font></i></b>"
								+ NetworkService.escapeHtml(name_message[1]);
					} else {
						message = "<b><font color='" + color + "'>"
								+ NetworkService.escapeHtml(name_message[0]) + ":</font></b>";
						if (info != null && info.auth > 3) // don't escape html for higher
							message += name_message[1];
						else 
							message += NetworkService.escapeHtml(name_message[1]);
					}
				}
				writeToHist(Html.fromHtml(message));
				break;
			}
			case HtmlChannel:
				writeToHist(Html.fromHtml(msg.readQString()));
				break; */
			case LeaveChannel:
				int pid = msg.readInt();
				if (pid == netServ.myid) { // We left the channel
					players.clear();
					joined = false;

					netServ.joinedChannels.remove(this);
					netServ.updateJoinedChannels();
					
					writeToHist(Html.fromHtml("<i>Left channel: <b>" + name + "</b></i>"));
				}
				/* If a pmed players logs out, we receive the log out message before the leave channel one
				 * so there's this work around...
				 */
				PlayerInfo p = netServ.players.get(pid);
				if (p == null) {
					p = new PlayerInfo();
					p.id = pid;
				}
				removePlayer(p);
				break;
			default:
				break;
			}
		}

	public void clearData() {
		players.clear();
		
		/* Todo: instead use leaveChannel() or make sure during reconnect we don't keep extra players */
	}
}
