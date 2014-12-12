package com.podevs.android.poAndroid.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.*;
import com.podevs.android.poAndroid.teambuilder.EVSlider.EVListener;


public class PokemonDetailsFragment extends Fragment implements EVListener {
	public interface PokemonDetailsListener {
		public void onPokemonEdited(boolean updateAll);
	}

	private EVSlider sliders[] = null;
	private TextView labels[] = null;
	private Spinner formesChooser = null, itemChooser = null, abilityChooser = null, natureChooser = null, genderChooser = null;
	private CheckBox shinyChooser = null;
	private LinearLayout formesLayout;
	private ArrayAdapter<CharSequence> abilityChooserAdapter, genderChooserAdapter, formesChooserAdapter;
	public PokemonDetailsListener listener = null;

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
		labels = new TextView[]{
			(TextView)v.findViewById(R.id.hplabel),
			(TextView)v.findViewById(R.id.attlabel),
			(TextView)v.findViewById(R.id.deflabel),
			(TextView)v.findViewById(R.id.spattlabel),
			(TextView)v.findViewById(R.id.spdeflabel),
			(TextView)v.findViewById(R.id.speedlabel)
		};
		
		for (EVSlider slider: sliders) {
			slider.listener = this;
		}

		abilityChooser = (Spinner)v.findViewById(R.id.abilitychoice);
		itemChooser = (Spinner)v.findViewById(R.id.itemchoice);
		natureChooser = (Spinner)v.findViewById(R.id.nature);
		genderChooser = (Spinner)v.findViewById(R.id.gender);
		formesChooser = (Spinner)v.findViewById(R.id.formes);
		formesLayout = (LinearLayout)v.findViewById(R.id.formesLayout);
		shinyChooser = (CheckBox)v.findViewById(R.id.shiny);
		
		ArrayAdapter<CharSequence> itemChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		int usefulItems[] = ItemInfo.usefulItems();
		for (int usefulItem : usefulItems) {
			itemChooserAdapter.add(ItemInfo.name(usefulItem));
		}
		itemChooser.setAdapter(itemChooserAdapter);

		// Create an ArrayAdapter using the string array and a default spinner layout
		abilityChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		// Apply the adapter to the spinner
		abilityChooser.setAdapter(abilityChooserAdapter);
		
		genderChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		genderChooser.setAdapter(genderChooserAdapter);

		formesChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		formesChooser.setAdapter(formesChooserAdapter);
		
		ArrayAdapter<CharSequence> natureChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		for (int i = 0; i < NatureInfo.count(); i++) {
			natureChooserAdapter.add(NatureInfo.boostedName(i));
		}
		natureChooser.setAdapter(natureChooserAdapter);

		updatePoke();
		
		natureChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
			                           int arg2, long arg3) {
				poke().nature = (byte) arg2;

				updateStats();
				notifyUpdated();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		formesChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
			                           int arg2, long arg3) {
				poke().setNum(PokemonInfo.number(arg0.getItemAtPosition(arg2).toString()));
				notifyUpdated(true);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		itemChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int orid = poke().uID().hashCode();
				
				poke().setItem((short) ItemInfo.usefulItems()[arg2]);
				notifyUpdated(orid != poke().uID().hashCode());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		abilityChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				short abilities [] = PokemonInfo.abilities(poke().uID(), poke().gen.num);
				for (short ability : abilities) {
					if (AbilityInfo.name(ability) == abilityChooserAdapter.getItem(arg2)) {
						poke().ability = ability;
						notifyUpdated();
						break;
					}
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		genderChooser.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				poke().gender = (byte)(1+arg2);
				notifyUpdated();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		shinyChooser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					poke().shiny = true;
				} else {
					poke().shiny = false;
				}
			}
		});

		return v;
	}
	
	public void notifyUpdated() {
		if (listener != null) {
			listener.onPokemonEdited(false);
		}
	}
	
	public void notifyUpdated(boolean b) {
		if (listener != null) {
			listener.onPokemonEdited(b);
		}
	}

	private TeamPoke poke() {
		return ((EditPokemonActivity) getActivity()).getPoke();
	}

	public void updatePoke() {
		if (poke() == null) {
			return;
		}

		if (PokemonInfo.hasVisibleFormes(poke().uID())) {
			formesLayout.setVisibility(View.VISIBLE);
			formesChooserAdapter.clear();

			for (UniqueID uID : PokemonInfo.formes(poke().uID(), poke().gen)) {
				formesChooserAdapter.add(PokemonInfo.name(uID));
				if (uID.equals(poke().uID())) {
					formesChooser.setSelection(formesChooserAdapter.getCount()-1);
				}
			}
		} else {
			formesLayout.setVisibility(View.GONE);
		}

		for (int i = 0; i < 6; i++) {
			sliders[i].setNum(poke().ev(i));
		}
		
		updateStats();

		if (poke().gen.num >= 3) {
			short[] abilities = PokemonInfo.abilities(poke().uID(), poke().gen.num);
	
			abilityChooserAdapter.clear();
			abilityChooserAdapter.add(AbilityInfo.name(abilities[0]));
			if (abilities[0] == poke().ability) {
				abilityChooser.setSelection(0);
			}
			if (abilities[1]!=0) {
				abilityChooserAdapter.add(AbilityInfo.name(abilities[1]));
				if (abilities[1] == poke().ability) {
					abilityChooser.setSelection(1);
				}
			}
			if (abilities[2]!=0) {
				abilityChooserAdapter.add(AbilityInfo.name(abilities[2]));
				if (abilities[2] == poke().ability) {
					abilityChooser.setSelection(abilityChooserAdapter.getCount()-1);
				}
			}

			natureChooser.setSelection(poke().nature);

			natureChooser.setVisibility(View.VISIBLE);
			abilityChooser.setVisibility(View.VISIBLE);
		} else {
			natureChooser.setVisibility(View.GONE);
			abilityChooser.setVisibility(View.GONE);
		}

		if (poke().gen.num >= 2) {
			genderChooserAdapter.clear();
			int genderChoice = PokemonInfo.gender(poke().uID());
			if (genderChoice == 0) {
				genderChooserAdapter.add("Neutral");
			} else if (genderChoice == 1) {
				genderChooserAdapter.add("Male");
			} else if (genderChoice == 2) {
				genderChooserAdapter.add("Female");
			} else {
				genderChooserAdapter.add("Male");
				genderChooserAdapter.add("Female");

				genderChooser.setSelection(poke().gender == 1 ? 0 : 1);
			}

			int usefulItems[] = ItemInfo.usefulItems();
			int pokeitem = poke().item();
			for (int i = 0; i < usefulItems.length; i++) {
				if (usefulItems[i] == pokeitem) {
					itemChooser.setSelection(i);
					break;
				}
			}
			shinyChooser.setVisibility(View.VISIBLE);
			genderChooser.setVisibility(View.VISIBLE);
			itemChooser.setVisibility(View.VISIBLE);
		} else {
			shinyChooser.setVisibility(View.GONE);
			genderChooser.setVisibility(View.GONE);
			itemChooser.setVisibility(View.GONE);
		}

		shinyChooser.setChecked(poke().shiny);
	}
	
	public void updateStats() {
		for (int i = 0; i < 6; i++) {
			labels[i].setText(StatsInfo.Shortcut(i) + ": " + poke().stat(i));
		}
	}

	public void onEVChanged(int stat, int ev) {
		int totalEVs = poke().totalEVs() - poke().ev(stat) + ev;
		
		if (totalEVs > 510 && poke().gen().num > 2) {
			ev = (510 - (poke().totalEVs() - poke().ev(stat)));
			ev = ev/4*4;
			sliders[stat].setNum(ev);
		}
		
		poke().EVs[stat] = (byte)ev;

		labels[stat].setText(StatsInfo.Shortcut(stat) + ": " + poke().stat(stat));

		notifyUpdated();
	}
}
