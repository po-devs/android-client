package com.podevs.android.pokemononline.chat;

import java.util.Comparator;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.podevs.android.pokemononline.NetworkService;
import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.player.PlayerInfo;
import com.podevs.android.utilities.StringUtilities;

public class PlayerListAdapter extends ArrayAdapter<com.podevs.android.pokemononline.player.PlayerInfo>{
	
	static final int battlingColor = Color.parseColor("#5d838c");
	static int defaultColor = -1;
	
	public PlayerListAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public void sortByNick() {
		setNotifyOnChange(false);
		synchronized(this) {
			super.sort(new Comparator<PlayerInfo>() {
				public int compare(PlayerInfo pi1, PlayerInfo pi2) {
					return pi1.nick().toLowerCase().compareTo(pi2.nick().toLowerCase());
				}
			});
		}
		setNotifyOnChange(true);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.row_simple, null);
		}
		PlayerInfo player = getItem(position);
		if (player != null) {
			TextView nick = (TextView)view.findViewById(R.id.player_list_name);
			
			if (NetworkService.pmedPlayers.contains(player.id)) {
				nick.setText(Html.fromHtml("<b><i>"  + StringUtilities.escapeHtml(player.nick()) + "</i></b>" ));
			} else {
				nick.setText(player.nick());
			}
			
			if (defaultColor == -1) {
				defaultColor = nick.getTextColors().getDefaultColor();
			}
			
			if (player.battling()) {
				nick.setTextColor(battlingColor);
			} else {
				nick.setTextColor(defaultColor);
			}
		}
		return view;
	}
	
	@Override
	public void notifyDataSetChanged(){
		sortByNick();
		super.notifyDataSetChanged();
	}
}
