package com.podevs.android.poAndroid.teambuilder;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo.Type;

import java.util.HashSet;

public class PokeListAdapter implements ListAdapter {
	private Gen gen = null;
	private HashSet<DataSetObserver> observers = new HashSet<DataSetObserver>();

	PokeListAdapter(Gen gen) {
		super();
		this.gen = gen;
	}

	public int getCount() {
		return PokemonInfo.numberOfPokemons(gen)+1;
	}

	public Object getItem(int pos) {
		return pos;
	}

	public long getItemId(int pos) {
		return pos;
	}

	public int getItemViewType(int arg0) {
		return 0;
	}

	public void setGen(Gen g) {
		if (g.equals(gen)) {
			return;
		}
		gen = g;
		for (DataSetObserver obs : observers) {
			obs.onInvalidated();
		}
	}

	public View getView(int pos, View convertView, ViewGroup arg2) {
		if (convertView == null) {
			convertView = ViewGroup.inflate(arg2.getContext(), R.layout.pokeinlist_item, null);
		}

		UniqueID poke = new UniqueID(pos,0);
		ImageView image = (ImageView)convertView.findViewById(R.id.image);
		image.setImageDrawable(PokemonInfo.iconDrawable(poke));
		((TextView)convertView.findViewById(R.id.pokename)).setText(PokemonInfo.name(poke));
		((ImageView)convertView.findViewById(R.id.type1)).setImageResource(TypeInfo.typeRes(PokemonInfo.type1(poke, gen.num)));
		
		int type2 = PokemonInfo.type2(poke, gen.num);
		ImageView itype2 = ((ImageView)convertView.findViewById(R.id.type2));
		
		itype2.setImageResource(TypeInfo.typeRes(type2));
		itype2.setVisibility(type2 == Type.Curse.ordinal() ? View.INVISIBLE : View.VISIBLE);
		
		return convertView;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return false;
	}

	public void registerDataSetObserver(DataSetObserver arg0) {
		observers.add(arg0);
	}

	public void unregisterDataSetObserver(DataSetObserver arg0) {
		observers.remove(arg0);
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int arg0) {
		return true;
	}

}
