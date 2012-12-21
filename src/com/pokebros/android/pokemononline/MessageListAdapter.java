package com.pokebros.android.pokemononline;

import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessageListAdapter extends BaseAdapter {
	LinkedList<TextView> messageViews = new LinkedList<TextView>();
	Channel channel;
	Context context;
    public int lastSeen = 0;
	
	public MessageListAdapter(Channel ch, Context ctxt) {
		super();
		channel = ch;
		context = ctxt;
		synchronized(channel.messageList) {
			ListIterator<SpannableStringBuilder> it = channel.messageList.listIterator();
			for (int i = 0; i < channel.messageList.size(); i++)
				add(it.next());
			lastSeen = channel.lastSeen;
		}
	}
	
	public void add(SpannableStringBuilder span) {
		TextView toAdd = new TextView(context);
		toAdd.setText(span);
		messageViews.add(toAdd);
		if (getCount() > Channel.HIST_LIMIT)
			messageViews.remove();
	}

	public int getCount() {
		return messageViews.size();
	}

	public TextView getItem(int position) {
		return messageViews.get(position);
	}

	public long getItemId(int position) {
		// Required
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		return getItem(position);
	}

	@Override
	public boolean isEnabled(int position)
	{ 
		return false;
	}
}
