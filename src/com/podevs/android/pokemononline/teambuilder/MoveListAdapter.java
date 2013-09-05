package com.podevs.android.pokemononline.teambuilder;

import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.pokeinfo.MoveInfo;
import com.podevs.android.pokemononline.pokeinfo.TypeInfo;

public class MoveListAdapter extends ArrayAdapter<Short> {

	public MoveListAdapter(Context context) {
		super(context, R.layout.move_item);
	}

	public void sortByNick() {
		setNotifyOnChange(false);
		synchronized(this) {
			super.sort(new Comparator<Short>() {
				public int compare(Short m1, Short m2) {
					return MoveInfo.name(m1).compareTo(MoveInfo.name(m2));
				}
			});
		}
		setNotifyOnChange(true);
	}
	
	public void setMoves(short[] moves) {
		setNotifyOnChange(false);
		clear();
		
		for (int i = 0; i < moves.length; i++) {
			add(moves[i]);
		}
		setNotifyOnChange(true);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.move_item, null);
		}
		Short move = getItem(position);
		if (move != null) {
			TextView nick = (TextView)view.findViewById(R.id.movename);
			nick.setText(MoveInfo.name(move));
			
			TextView power = (TextView)view.findViewById(R.id.power);
			power.setText("pow: " + MoveInfo.powerString(move));
			
			TextView pps = (TextView)view.findViewById(R.id.pps);
			pps.setText("pps: " + MoveInfo.pp(move));
			
			TextView accuracy = (TextView)view.findViewById(R.id.accuracy);
			accuracy.setText("acc: " +MoveInfo.accuracyString(move));
			
			ImageView type = (ImageView)view.findViewById(R.id.type);
			type.setImageResource(TypeInfo.typeRes(MoveInfo.type(move)));
		}
		return view;
	}

	@Override
	public void notifyDataSetChanged(){
		//sortByNick();
		super.notifyDataSetChanged();
	}
}
