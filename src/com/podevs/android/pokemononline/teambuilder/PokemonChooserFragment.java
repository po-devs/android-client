package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.Gen;
import com.podevs.android.pokemononline.poke.Team;
import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;

public class PokemonChooserFragment extends Fragment {
	UniqueID chosenId = null;
	ListView pokeList = null;
	String nick = null;
	PokemonChooserListener listener = null;
	AutoCompleteTextView pokeChoice = null;
	Gen gen = null;
	ArrayAdapter<String> pokeChoiceAdapter = null;
	PokeListAdapter pokeListAdapter = null;
	
	public interface PokemonChooserListener {
		public void onPokemonChosen(UniqueID id, String nickname);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pokemonchooser, container, false);
		gen = getTeam().gen;
		pokeList = (ListView)v.findViewById(R.id.pokeList);
		pokeList.setAdapter(pokeListAdapter = new PokeListAdapter(gen));
		
		pokeChoice = (AutoCompleteTextView)v.findViewById(R.id.pokemonChoice);
		pokeChoiceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, PokemonInfo.nameArray(gen));
		pokeChoice.setAdapter(pokeChoiceAdapter);
		
		pokeChoice.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String name = pokeChoiceAdapter.getItem(arg2);
				chosenId = PokemonInfo.number(name);
				
				updateList();
			}
		});
		
		pokeList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				chosenId = new UniqueID(arg2, 0);
				pokeChoice.setText(PokemonInfo.name(chosenId));
				/* We already set a completed item, no need for the dropdown to show */
				pokeChoice.dismissDropDown();
			}
		});
		
		Button ok = (Button)v.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (listener != null && chosenId != null) {
					String nick = pokeChoice.getText().toString();
					
					if (nick.length() == 0) {
						nick = PokemonInfo.name(chosenId);
					}
					
					listener.onPokemonChosen(chosenId, nick);
				}
			}
		});
		
		if (nick != null) {
			setDetails(chosenId, nick);
			nick = null;
		}
		
		return v;
	}

	public void setGen(Gen gen) {
		if (gen.equals(this.gen)) {
			return;
		}
		this.gen = gen;
		pokeListAdapter.setGen(gen);
		pokeChoiceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, PokemonInfo.nameArray(gen));
		pokeChoice.setAdapter(pokeChoiceAdapter);
	}
	
	protected void setDetails(UniqueID number, String nick) {
		chosenId = number;
		if (pokeChoice != null) {
			pokeChoice.setText(nick);
			pokeChoice.dismissDropDown();
			updateList();
		} else {
			this.nick = nick;
		}
	}

	protected void updateList() {
		pokeList.setSelection(chosenId.pokeNum);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void setOnPokemonChosenListener(PokemonChooserListener listener) {
		this.listener = listener;
	}
	public TeambuilderActivity getTeambuilder() {return ((TeambuilderActivity)getActivity());}
	private Team getTeam() {return getTeambuilder().team;}
}
