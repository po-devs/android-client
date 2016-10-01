package com.podevs.android.poAndroid;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.chat.Channel;

import java.util.LinkedList;
import java.util.ListIterator;

public class MessageListAdapter extends BaseAdapter {
	final LinkedList<TextView> messageViews = new LinkedList<TextView>();
	public Channel channel;
	Context context;
    public int lastSeen = 0;
	public static boolean copyandpaste = false;
	
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

        //Too buggy. Needs custom gesture
        if (copyandpaste) {
            try {
                toAdd.setTextIsSelectable(true);
            } catch (RuntimeException e) {
				e.printStackTrace();
				return;
            }
        }

        if (toAdd.getLinksClickable()) toAdd.setMovementMethod(LinkMovementMethod.getInstance());

		//Linkify.addLinks(toAdd, Linkify.WEB_URLS);
		Linkify.addLinks(toAdd, NetworkService.urlPattern, "");

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
