package com.pokebros.android.pokemononline;

import java.util.Hashtable;
import java.util.LinkedList;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.pokebros.android.pokemononline.player.PlayerInfo;

public class Channel {
	protected String name;
	protected int id;
	protected int events = 0;
	public int lastSeen = 0;
	protected boolean isReadyToQuit = false;
	public boolean joined = false;
	public final static int HIST_LIMIT = 1000;
	
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	LinkedList<SpannableStringBuilder> messageList = new LinkedList<SpannableStringBuilder>();
	
	public void writeToHist(CharSequence text) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class)
			spannable = new SpannableStringBuilder(text);
		else
			spannable = (SpannableStringBuilder)text;
		synchronized(messageList) {
			messageList.add(spannable);
			lastSeen++;
			if (messageList.size() > HIST_LIMIT)
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
			
			if(netServ != null && netServ.chatActivity != null && this.equals(netServ.joinedChannels.peek()))
				netServ.chatActivity.addPlayer(p);
		}
		else
			System.out.println("Tried to add nonexistant player id" +
					"to channel " + name + ", ignoring");
	}
	
	public void removePlayer(PlayerInfo p){
		if(p != null){
			players.remove(p.id);
			
			if(netServ != null && netServ.chatActivity != null)
				netServ.chatActivity.removePlayer(p);
		}
		else
			System.out.println("Tried to remove nonexistant player id" +
					"from channel " + name + ", ignoring");
	}
	
	public void updatePlayer(PlayerInfo p) {
		if (players.containsKey(p.id)) {
			if (netServ != null && netServ.chatActivity != null && this.equals(netServ.joinedChannels.peek()))
				netServ.chatActivity.updatePlayer(p);
		}
	}
	
	public void handleChannelMsg(Command c, Bais msg) {
			switch(c) {
			case ChannelPlayers: {
				int numPlayers = msg.readInt();
				for(int i = 0; i < numPlayers; i++) {
					addPlayer(netServ.players.get(msg.readInt()));
				}
				break;
			} case JoinChannel: {
				PlayerInfo p = netServ.players.get(msg.readInt());
				if (p.id == netServ.mePlayer.id) { // We joined the channel
					netServ.joinedChannels.addFirst(this);
					joined = true;
					if (netServ.chatActivity != null) {
						netServ.chatActivity.populateUI(true);
						netServ.chatActivity.progressDialog.dismiss();
					}
					writeToHist(Html.fromHtml("<i>Joined channel: <b>" + name + "</b></i>"));
				}
				addPlayer(p);
				Log.d("JoinChannel", "Added " + p);
				break;
			} case BattleList: {
				int numBattles = msg.readInt();
				for (int i = 0; i < numBattles; i++) {
					// TODO
					int battleId = msg.readInt();
					byte mode = msg.readByte();
					int player1 = msg.readInt();
					int player2 = msg.readInt();
				}
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
				break;
			case LeaveChannel:
				PlayerInfo p = netServ.players.get(msg.readInt());
				if (p.id == netServ.mePlayer.id) { // We left the channel
					players.clear();
					joined = false;
					// XXX this runtime complexity sucks
					netServ.joinedChannels.remove(this);
					if (netServ.chatActivity != null) {
						netServ.chatActivity.populateUI(true);
					}
					writeToHist(Html.fromHtml("<i>Left channel: <b>" + name + "</b></i>"));
				}
				removePlayer(p);
				break;
			default:
				break;*/
			}
		}
}
