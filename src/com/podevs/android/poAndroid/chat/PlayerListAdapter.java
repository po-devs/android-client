package com.podevs.android.poAndroid.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.utilities.StringUtilities;

import java.util.Comparator;

public class PlayerListAdapter extends ArrayAdapter<com.podevs.android.poAndroid.player.PlayerInfo>{
	
	public PlayerListAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public void sortByNick() {
		setNotifyOnChange(false);
		synchronized(this) {
			super.sort(new Comparator<PlayerInfo>() {
				public int compare(PlayerInfo pi1, PlayerInfo pi2) {
					if (pi1.auth % 4 > pi2.auth % 4)
						return -1;
					else if (pi1.auth % 4 < pi2.auth % 4)
						return 1;
					else
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

            SpannableString text = new SpannableString("   " + StringUtilities.escapeHtml(player.nick()));
            if (player.auth == 3) {
                if (player.battling()) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b3b);
                    text.setSpan(is, 1, 2 , 0);
                } else if (player.isAway) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b3a);
                    text.setSpan(is, 1, 2 , 0);
                } else {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b3);
                    text.setSpan(is, 1, 2 , 0);
                }
            } else if (player.auth == 2) {
                if (player.battling()) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b2b);
                    text.setSpan(is, 1, 2 , 0);
                } else if (player.isAway) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b2a);
                    text.setSpan(is, 1, 2 , 0);
                } else {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b2);
                    text.setSpan(is, 1, 2 , 0);
                }
            } else if (player.auth == 1) {
                if (player.battling()) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b1b);
                    text.setSpan(is, 1, 2 , 0);
                } else if (player.isAway) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b1a);
                    text.setSpan(is, 1, 2 , 0);
                } else {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b1);
                    text.setSpan(is, 1, 2 , 0);
                }
            } else {
                if (player.battling()) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b0b);
                    text.setSpan(is, 1, 2 , 0);
                } else if (player.isAway) {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b0a);
                    text.setSpan(is, 1, 2 , 0);
                } else {
                    ImageSpan is = new ImageSpan(getContext(), R.drawable.b0);
                    text.setSpan(is, 1, 2 , 0);
                }
            }

            if (NetworkService.pmedPlayers.contains(player.id)) {
                final StyleSpan bis = new StyleSpan(Typeface.BOLD_ITALIC);
                text.setSpan(bis, 1, text.length() , 0);
            }

            nick.setText(text);

			nick.setTextColor(player.color.colorInt);

			/*
			if (defaultColor == -1) {
				defaultColor = nick.getTextColors().getDefaultColor();
			}

			if (player.battling()) {
				nick.setTextColor(battlingColor);
			} else {
				nick.setTextColor(defaultColor);
			}
			*/
		}
		return view;
	}
	
	@Override
	public void notifyDataSetChanged(){
		sortByNick();
		super.notifyDataSetChanged();
	}
}
