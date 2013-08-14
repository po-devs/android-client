package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.UniqueID;

public class PokemonChooserFragment extends Fragment {
	public interface PokemonChooserListener {
		public void onPokemonChosen(UniqueID id);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pokemonchooser, container, false);
		ListView pokeList = (ListView)v.findViewById(R.id.pokeList);
		pokeList.setAdapter(new PokeListAdapter());
		
		return v;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
}
