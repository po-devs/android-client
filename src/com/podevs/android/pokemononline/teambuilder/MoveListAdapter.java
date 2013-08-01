package com.podevs.android.pokemononline.teambuilder;

import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.pokeinfo.MoveInfo;

public class MoveListAdapter extends ArrayAdapter<MoveInfo.Move> {

	public MoveListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public void sortByNick() {
		setNotifyOnChange(false);
		synchronized(this) {
			super.sort(new Comparator<MoveInfo.Move>() {
				public int compare(MoveInfo.Move m1, MoveInfo.Move m2) {
					return m1.name.compareTo(m2.name.toLowerCase());
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
			view = inflater.inflate(R.layout.move_item, null);
		}
		MoveInfo.Move move = getItem(position);
		if (move != null) {
			TextView nick = (TextView)view.findViewById(R.id.movename);

			nick.setText(move.name);
		}
		return view;
	}

	@Override
	public void notifyDataSetChanged(){
		sortByNick();
		super.notifyDataSetChanged();
	}
}
