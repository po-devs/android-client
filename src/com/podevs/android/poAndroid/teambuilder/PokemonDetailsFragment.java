package com.podevs.android.poAndroid.teambuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.podevs.android.utilities.ImageSpinnerAdapter;
import com.podevs.android.utilities.SpinnerData;

import java.util.ArrayList;


public class PokemonDetailsFragment extends Fragment implements EVListener {
	public interface PokemonDetailsListener {
		public void onPokemonEdited(boolean updateAll);
	}

	private EVSlider sliders[] = null;
	private TextView labels[] = null, levelChooser = null, happinessChooser = null;
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
		levelChooser = (TextView)v.findViewById(R.id.level);;
		happinessChooser = (TextView)v.findViewById(R.id.happiness);

		ArrayList<SpinnerData> temp = new ArrayList<SpinnerData>();
		int usefulItems[] = ItemInfo.usefulItems();
		for (int usefulItem : usefulItems) {
			SpinnerData tempData = new SpinnerData(ItemInfo.name(usefulItem), usefulItem);
			temp.add(tempData);
		}
		ImageSpinnerAdapter itemChooserAdapter = new ImageSpinnerAdapter(getActivity(), R.layout.row_item, temp, getResources());

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

		levelChooser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input = new EditText((getActivity()));

				new AlertDialog.Builder((getActivity()))
						.setTitle(R.string.download_team)
						.setMessage("Change Level")
						.setView(input)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String link = input.getText().toString();
								int i = Integer.parseInt(link);
								if (i > 100) i = 100;
								poke().level = (byte) i;
								levelChooser.setText(" Lvl: " + i);
								updateStats();
							}
						}).show();
			}
		});

		happinessChooser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input = new EditText((getActivity()));

				new AlertDialog.Builder((getActivity()))
						.setTitle("Change Happiness")
						.setMessage("Happiness: ")
						.setView(input)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String link = input.getText().toString();
								int i = Integer.parseInt(link);
								if (i > 255) i = 255;
								poke().happiness = (byte) i;
								happinessChooser.setText("Happy: " + i);
							}
						}).show();
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

		TeamPoke tempPoke = poke();

		if (PokemonInfo.hasVisibleFormes(tempPoke.uID())) {
			formesLayout.setVisibility(View.VISIBLE);
			formesChooserAdapter.clear();

			for (UniqueID uID : PokemonInfo.formes(tempPoke.uID(), tempPoke.gen)) {
				formesChooserAdapter.add(PokemonInfo.name(uID));
				if (uID.equals(tempPoke.uID())) {
					formesChooser.setSelection(formesChooserAdapter.getCount()-1);
				}
			}
		} else {
			formesLayout.setVisibility(View.GONE);
		}

		for (int i = 0; i < 6; i++) {
			sliders[i].setNum(tempPoke.ev(i));
		}
		
		updateStats();

		if (tempPoke.gen.num >= 3) {
			short[] abilities = PokemonInfo.abilities(poke().uID(), tempPoke.gen.num);
	
			abilityChooserAdapter.clear();
			abilityChooserAdapter.add(AbilityInfo.name(abilities[0]));
			if (abilities[0] == tempPoke.ability) {
				abilityChooser.setSelection(0);
			}
			if (abilities[1]!=0) {
				abilityChooserAdapter.add(AbilityInfo.name(abilities[1]));
				if (abilities[1] == tempPoke.ability) {
					abilityChooser.setSelection(1);
				}
			}
			if (abilities[2]!=0) {
				abilityChooserAdapter.add(AbilityInfo.name(abilities[2]));
				if (abilities[2] == tempPoke.ability) {
					abilityChooser.setSelection(abilityChooserAdapter.getCount()-1);
				}
			}

			natureChooser.setSelection(tempPoke.nature);

			natureChooser.setVisibility(View.VISIBLE);
			abilityChooser.setVisibility(View.VISIBLE);
		} else {
			natureChooser.setVisibility(View.GONE);
			abilityChooser.setVisibility(View.GONE);
		}

		tempPoke = poke();

		if (tempPoke.gen.num >= 2) {
			genderChooserAdapter.clear();
			int genderChoice = PokemonInfo.gender(tempPoke.uID());
			if (genderChoice == 0) {
				tempPoke.gender = 0;
				genderChooserAdapter.add("Neutral");
			} else if (genderChoice == 1) {
				genderChooserAdapter.add("Male");
			} else if (genderChoice == 2) {
				genderChooserAdapter.add("Female");
			} else {
				genderChooserAdapter.add("Male");
				genderChooserAdapter.add("Female");

				genderChooser.setSelection(tempPoke.gender == 1 ? 0 : 1);
			}

			int usefulItems[] = ItemInfo.usefulItems();
			int pokeitem = tempPoke.item();
			for (int i = 0; i < usefulItems.length; i++) {
				if (usefulItems[i] == pokeitem) {
					itemChooser.setSelection(i);
					break;
				}
			}
			shinyChooser.setChecked(tempPoke.shiny);
			happinessChooser.setText("Happy: " + (tempPoke.happiness & 0xFF));

			shinyChooser.setVisibility(View.VISIBLE);
			genderChooser.setVisibility(View.VISIBLE);
			itemChooser.setVisibility(View.VISIBLE);
			happinessChooser.setVisibility(View.VISIBLE);
		} else {
			shinyChooser.setVisibility(View.GONE);
			genderChooser.setVisibility(View.GONE);
			itemChooser.setVisibility(View.GONE);
			happinessChooser.setVisibility(View.GONE);
		}

		levelChooser.setText(" Lvl: " + tempPoke.level());
	}
	
	public void updateStats() {
		TeamPoke tempPoke = poke();
		for (int i = 0; i < 6; i++) {
			labels[i].setText(StatsInfo.Shortcut(i) + ": " + tempPoke.stat(i));
		}
	}

	public void onEVChanged(int stat, int ev) {
		TeamPoke tempPoke = poke();
		int totalEVs = tempPoke.totalEVs() - tempPoke.ev(stat) + ev;
		
		if (totalEVs > 510 && poke().gen().num > 2) {
			ev = (510 - (tempPoke.totalEVs() - tempPoke.ev(stat)));
			ev = ev/4*4;
			sliders[stat].setNum(ev);
		}
		
		poke().EVs[stat] = (byte)ev;

		labels[stat].setText(StatsInfo.Shortcut(stat) + ": " + tempPoke.stat(stat));

		notifyUpdated();
	}
}
