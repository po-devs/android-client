package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.battle.ListedPokemon;
import com.podevs.android.pokemononline.poke.TeamPoke;

public class EditPokemonFragment extends Fragment {
	ListedPokemon pokeList;
	TeamPoke poke = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.edit_pokemon, container, false);
		
		pokeList = new ListedPokemon((RelativeLayout)v.findViewById(R.id.pokeViewLayout));
		
		pokeList.update(activity().team.poke(activity().currentPoke));
		
		return v;
	}
	
	public void setPoke(TeamPoke poke) {
		pokeList.update(poke);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private TeambuilderActivity activity() {
		return (TeambuilderActivity) getActivity();
	}
}
