package com.podevs.android.poAndroid.teambuilder;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.poAndroid.pokeinfo.DamageClassInfo;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;

public class MoveListAdapter implements ListAdapter {
	TeamPoke poke = null;

	public void setPoke(TeamPoke poke) {
		this.poke = poke;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.move_item, null);
		}
		short move = PokemonInfo.moves(poke.uID(), poke.gen.num, poke.gen.subNum)[position];

		TextView nick = (TextView)view.findViewById(R.id.movename);
		nick.setText(MoveInfo.name(move));

		TextView power = (TextView)view.findViewById(R.id.power);
		power.setText("pow: " + MoveInfo.powerString(move));

		TextView pps = (TextView)view.findViewById(R.id.pps);
		pps.setText("pp: " + MoveInfo.pp(move));

		TextView accuracy = (TextView)view.findViewById(R.id.accuracy);
		accuracy.setText("acc: " + MoveInfo.accuracyString(move));

		ImageView type = (ImageView)view.findViewById(R.id.type);
		type.setImageResource(TypeInfo.typeRes(MoveInfo.type(move)));

		TextView damageClass = (TextView)view.findViewById(R.id.damageClass);
		damageClass.setText(DamageClassInfo.name(MoveInfo.damageClass(move)));
/*
		ImageView type = (ImageView)view.findViewById(R.id.type);
		type.setImageResource(DamageClassInfo.damageClassRes(MoveInfo.damageClass(move)));
*/

		if (poke != null) {
			CheckBox check = (CheckBox)view.findViewById(R.id.check);
			check.setChecked(poke.hasMove(move));
		}

		return view;
	}

	public int getCount() {
		return PokemonInfo.moves(poke.uID(), poke.gen.num, poke.gen.subNum).length;
	}

	public Object getItem(int arg0) {
		return PokemonInfo.moves(poke.uID(), poke.gen.num, poke.gen.subNum)[arg0];
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public int getItemViewType(int arg0) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}

	Set<DataSetObserver> observers = new HashSet<DataSetObserver>();

	public void registerDataSetObserver(DataSetObserver arg0) {
		observers.add(arg0);
	}

	public void unregisterDataSetObserver(DataSetObserver arg0) {
		observers.remove(arg0);
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	public void notifyDataSetChanged() {
		for (DataSetObserver obs : observers) {
			obs.onChanged();
		}
	}

}
