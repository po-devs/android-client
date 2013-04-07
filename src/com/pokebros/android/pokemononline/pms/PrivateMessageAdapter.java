package com.pokebros.android.pokemononline.pms;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pokebros.android.pokemononline.pms.PrivateMessage.Message;
import com.pokebros.android.pokemononline.pms.PrivateMessage.PrivateMessageListener;

public class PrivateMessageAdapter extends ArrayAdapter<PrivateMessage.Message> implements PrivateMessageListener {
	PrivateMessage pm;
	private String packName = "com.pokebros.android.pokemononline";
	
	public PrivateMessageAdapter(Context context, PrivateMessage pm) {
		super(context, 0, pm.messages);
		this.pm = pm;
		pm.listener = this;
	}

	public void onNewMessage(final Message message) {
		new Handler(getContext().getMainLooper()).post(new Runnable() {
			public void run() {
				//add(message);
				notifyDataSetChanged();
			}
		});
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new TextView(getContext());
		}
		
		TextView textView = (TextView) convertView;

		Message message = getItem(position);
		textView.setText(message.message);
		
		int left = 0, right = 0;
		Resources resources = getContext().getResources();
		if (message.sender == pm.me) {
			right = resources.getIdentifier("t" + message.sender.avatar, "drawable", packName);
		} else {
			left = resources.getIdentifier("t" + message.sender.avatar, "drawable", packName);
		}
		textView.setCompoundDrawablesWithIntrinsicBounds(left, 0, right, 0);
		textView.setContentDescription(message.sender.nick() + ": " + message.message);

		return textView;
	}
}
