package com.podevs.android.poAndroid.chat;

import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.utilities.Bais;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Contains all info regarding a channel: name, id, joined,
 * and if joined a player list and chat history.
 *
 */
public class Channel {
	public String name;
	public int id;
	public int lastSeen = 0;
	// protected boolean isReadyToQuit = false;
	public boolean joined = false;
	public boolean flashed = false;
	public boolean newmessage = false;
	public final static int HIST_LIMIT = 700;
	public static final String TAG = "Pokemon Online Channel";
	public boolean channelEvents = false;
	
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	public LinkedList<SpannableStringBuilder> messageList = new LinkedList<SpannableStringBuilder>();

	public void writeToHist(CharSequence text, boolean clickable, final String command) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class)
			spannable = new SpannableStringBuilder(text);
		else
			spannable = (SpannableStringBuilder)text;
		if (clickable) {
			// With more customization could do something like clickable text that spectates battles and such
			spannable.setSpan(new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					netServ.joinChannel(command.replace("#", ""));
				}
			}, text.toString().indexOf(command), text.toString().indexOf(command) + command.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		write(spannable);
	}

	/**
	 *
	 * @param text Text to write in small font.
	 */

	public void writeToHistSmall(CharSequence text) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class)
			spannable = new SpannableStringBuilder(text);
		else
			spannable = (SpannableStringBuilder)text;
		spannable.setSpan(new RelativeSizeSpan(0.75f), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		write(spannable);
	}

	/**
	 *
	 * @param spannable Spannable to write to history.
	 */

	protected void write(SpannableStringBuilder spannable) {
		synchronized(messageList) {
			messageList.add(spannable);
			lastSeen++;
			while (messageList.size() > HIST_LIMIT)
				messageList.remove();
		}
	}

	/**
	 *
	 * @param text Text to write.
	 * @param left Left index to color.
	 * @param right Right index to color.
	 * @param color Hex color to apply to left-right range.
	 */
	public void writeToHist(CharSequence text, int left, int right, String color, boolean clickable, final String command) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class) {
			spannable = new SpannableStringBuilder(text);
		}
		else
			spannable = (SpannableStringBuilder)text;
		if (clickable) {
			spannable.setSpan(new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					netServ.joinChannel(command.replace("#", ""));
				}
			}, text.toString().indexOf(command), text.toString().indexOf(command) + command.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		try {
			Integer i = 0;
			try {
				i = Color.parseColor(color);
			} catch (Exception e) {
				i = Color.YELLOW;
			}
			spannable.setSpan(new BackgroundColorSpan(i), left, right, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} finally {}

		write(spannable);
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
			if (joined && channelEvents) writeToHistSmall(Html.fromHtml("<i><font color=\"#A0A0A0\">" + p.nick() + " joined the channel.</font></i>"));
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
			writeToHist(Html.fromHtml("<i>Joined channel: <b>" + name + "</b></i>"), false, null);
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
					
					writeToHist(Html.fromHtml("<i>Left channel: <b>" + name + "</b></i>"), false , null);
				}
				// TODO Let pm know
				/* If a pmed players logs out, we receive the log out message before the leave channel one
				 * so there's this work around...
				 */
				PlayerInfo p = netServ.players.get(pid);
				if (p == null) {
					p = new PlayerInfo();
					p.id = pid;
				}
				if (pid != netServ.myid && channelEvents) {
					writeToHistSmall(Html.fromHtml("<i><font color=\"#A0A0A0\">" + p.nick() + " left the channel.</font></i>"));
				}
				// player event
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
