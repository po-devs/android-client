package com.podevs.android.poAndroid.chat;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.utilities.StringUtilities;

import java.util.Comparator;

public class ChannelListAdapter extends ArrayAdapter<com.podevs.android.poAndroid.chat.Channel>{
	
	public ChannelListAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public void addChannel(Channel ch) {
		add(ch);
	}
	
	public void removeChannel(Channel ch){
		remove(ch);
	}
	
	public void sortByName() {
		setNotifyOnChange(false);
		super.sort(new Comparator<Channel>() {
			public int compare(Channel ch1, Channel ch2) {
				if (ch1.joined && !ch2.joined)
					return -1;
				else if (!ch1.joined && ch2.joined)
					return 1;
				else if (Character.isLetter(ch1.name().charAt(0)) && !Character.isLetter(ch2.name().charAt(0)))
					return -1;
				else if (!Character.isLetter(ch1.name().charAt(0)) && Character.isLetter(ch2.name().charAt(0)))
					return 1;
				else
					return ch1.name().compareToIgnoreCase(ch2.name());
			}
		});
		setNotifyOnChange(true);
	}



	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.row_simple_chan, null);
		}
		Channel channel = getItem(position);
		if (channel != null) {
			TextView nick = (TextView)view.findViewById(R.id.channel_list_name);
			nick.setText(Html.fromHtml(
					(channel.flashed ? "<font color='red'>" : channel.newmessage ? "<font color='#268a1e'" : "" ) +
						(channel.joined ? "<b><i>" : "" ) +
						StringUtilities.escapeHtml(channel.name()) +
						(channel.joined ? "</i></b>" : "" ) +
					(channel.flashed || channel.newmessage ? "</font>" : "" )));
		}
		return view;
	}
	
	@Override
	public void notifyDataSetChanged(){
		sortByName();
		super.notifyDataSetChanged();
	}
}
