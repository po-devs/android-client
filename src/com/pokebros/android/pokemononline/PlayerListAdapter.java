package com.pokebros.android.pokemononline;

import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pokebros.android.pokemononline.player.PlayerInfo;

public class PlayerListAdapter extends ArrayAdapter<com.pokebros.android.pokemononline.player.PlayerInfo>{
	
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
			nick.setText(player.nick());
		}
		return view;
	}
	
	@Override
	public void notifyDataSetChanged(){
		sortByNick();
		super.notifyDataSetChanged();
	}
}
