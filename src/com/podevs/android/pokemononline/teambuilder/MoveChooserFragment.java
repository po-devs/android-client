package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.TeamPoke;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;

public class MoveChooserFragment extends Fragment {
	ListView moveList = null;
	MoveListAdapter moveAdapter = null;
	int storedHash = 0;
	TeamPoke poke = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.movelist, container, false);
		
		moveList = (ListView)v.findViewById(R.id.moves);
		moveAdapter = new MoveListAdapter(getActivity());
		moveList.setAdapter(moveAdapter);
		
		setPoke(activity().team.poke(activity().currentPoke));

		return v;
	}
	
	public void setPoke(TeamPoke poke) {
		this.poke = poke;
		
		updatePoke();
	}
	
	public void updatePoke() {
		if (storedHash != poke.uID().hashCode()) {
			storedHash = poke.uID().hashCode();
			moveAdapter.setMoves(PokemonInfo.moves(poke.uID(), poke.gen.num));
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	private TeambuilderActivity activity() {
		return (TeambuilderActivity) getActivity();
	}
}
