package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.TeamPoke;
import com.podevs.android.pokemononline.pokeinfo.AbilityInfo;
import com.podevs.android.pokemononline.pokeinfo.NatureInfo;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;


public class PokemonDetailsFragment extends Fragment {
	private TeamPoke poke = null;
	private EVSlider sliders[] = null;
	private Spinner itemChooser = null;
	private Spinner abilityChooser = null;
	private Spinner natureChooser = null;
	private ArrayAdapter<CharSequence> abilityChooserAdapter, natureChooserAdapter;
	private CheckBox happiness = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pokemoninfo, container, false);

		sliders = new EVSlider[]{
				new EVSlider(v.findViewById(R.id.hpev), 0),
				new EVSlider(v.findViewById(R.id.attev), 1),
				new EVSlider(v.findViewById(R.id.defev), 2),
				new EVSlider(v.findViewById(R.id.spattev), 3),
				new EVSlider(v.findViewById(R.id.spdefev), 4),
				new EVSlider(v.findViewById(R.id.speedev), 5)
		};

		happiness = (CheckBox)v.findViewById(R.id.happiness);
		abilityChooser = (Spinner)v.findViewById(R.id.abilitychoice);
		itemChooser = (Spinner)v.findViewById(R.id.itemchoice);
		natureChooser = (Spinner)v.findViewById(R.id.nature);

		// Create an ArrayAdapter using the string array and a default spinner layout
		abilityChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		abilityChooserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		abilityChooser.setAdapter(abilityChooserAdapter);
		
		natureChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		for (int i = 0; i < NatureInfo.count(); i++) {
			natureChooserAdapter.add(NatureInfo.boostedName(i));
		}
		natureChooser.setAdapter(natureChooserAdapter);
		
		setPoke(activity().team.poke(activity().currentPoke));
		
		happiness.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				poke.happiness = (byte)(isChecked ? 255 : 0);
			}
		});
		
		natureChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				poke.nature = (byte)arg2;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		return v;
	}

	public void setPoke(TeamPoke poke) {
		this.poke = poke;
		updatePoke();
	}
	
	public void setPoke(TeamPoke poke, boolean update) {
		this.poke = poke;
		if (update) {
			updatePoke();
		}
	}

	private TeambuilderActivity activity() {
		return (TeambuilderActivity) getActivity();
	}

	public void updatePoke() {
		if (poke == null) {
			return;
		}

		for (int i = 0; i < 6; i++) {
			sliders[i].setNum(poke.ev(i));
		}

		short[] abilities = PokemonInfo.abilities(poke.uID(), poke.gen.num);

		abilityChooserAdapter.clear();
		abilityChooserAdapter.add(AbilityInfo.name(abilities[0]));
		if (abilities[1]!=0) {
			abilityChooserAdapter.add(AbilityInfo.name(abilities[1]));
		}
		if (abilities[2]!=0) {
			abilityChooserAdapter.add(AbilityInfo.name(abilities[2]));
		}
		
		if (poke.happiness == 0) {
			happiness.setChecked(false);
		} else {
			happiness.setChecked(true);
		}
		
		natureChooser.setSelection(poke.nature);
	}
}
