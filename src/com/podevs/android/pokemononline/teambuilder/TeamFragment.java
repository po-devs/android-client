package com.podevs.android.pokemononline.teambuilder;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.battle.ListedPokemon;
import com.podevs.android.pokemononline.poke.Team;
import com.podevs.android.pokemononline.pokeinfo.InfoConfig;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class TeamFragment extends Fragment {
	ListedPokemon pokeList[] = new ListedPokemon[6];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.battle_teamscreen, container, false);
		
		for (int i = 0; i < 6; i++) {
			RelativeLayout whole = (RelativeLayout)v.findViewById(
					InfoConfig.resources.getIdentifier("pokeViewLayout" + (i+1), "id", InfoConfig.pkgName));
			pokeList[i] = new ListedPokemon(whole);
		}
		
		updateTeam();
		
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void updateTeam() {
		Team team = ((TeambuilderActivity)getActivity()).team;
		for (int i = 0; i < 6; i++) {
			pokeList[i].update(team.poke(i), true);
		}
	}

}
