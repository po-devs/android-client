package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;

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
		
		AutoCompleteTextView pokeChoice = (AutoCompleteTextView)v.findViewById(R.id.pokemonChoice);
		pokeChoice.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, PokemonInfo.nameArray()));
		
		return v;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
}
