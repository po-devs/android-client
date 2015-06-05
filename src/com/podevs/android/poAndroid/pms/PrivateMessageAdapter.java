package com.podevs.android.poAndroid.pms;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.pms.PrivateMessage.Message;
import com.podevs.android.poAndroid.pms.PrivateMessage.PrivateMessageListener;

public class PrivateMessageAdapter extends ArrayAdapter<PrivateMessage.Message> implements PrivateMessageListener {
	PrivateMessage pm;
	private String packName = "com.podevs.android.poAndroid";
	
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
                if (pm.privateList != null) {
                    if (pm.privateList.getLastVisiblePosition() > getCount() - 3) {
                        pm.privateList.setSelection(getCount());
                    }
                }
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
		textView.setText(new SpannableStringBuilder(message.message));
		Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setTextIsSelectable(true);
		
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
