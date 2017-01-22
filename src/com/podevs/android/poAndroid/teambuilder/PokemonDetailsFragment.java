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
import java.util.List;


public class PokemonDetailsFragment extends Fragment implements EVListener {
	public interface PokemonDetailsListener {
		public void onPokemonEdited(boolean updateAll);
	}

	private EVSlider sliders[] = null;
	private TextView levelChooser = null, happinessChooser = null;
	private Spinner formesChooser = null, itemChooser = null, abilityChooser = null, natureChooser = null, genderChooser = null;
	private CheckBox shinyChooser = null;
	private LinearLayout formesLayout;
	private ArrayAdapter<CharSequence> abilityChooserAdapter, genderChooserAdapter;
	public PokemonDetailsListener listener = null;
    private Button manualIVButton = null;
    private ToggleButton hackmonButton = null;
    private FormListAdapter formListAdapter;

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

		/*
		labels = new TextView[]{
			(TextView)v.findViewById(R.id.hplabel),
			(TextView)v.findViewById(R.id.attlabel),
			(TextView)v.findViewById(R.id.deflabel),
			(TextView)v.findViewById(R.id.spattlabel),
			(TextView)v.findViewById(R.id.spdeflabel),
			(TextView)v.findViewById(R.id.speedlabel)
		};
		*/
		
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
        manualIVButton = (Button) v.findViewById(R.id.manualiv);
        hackmonButton = (ToggleButton) v.findViewById(R.id.hackmon);

		ArrayList<SpinnerData> temp = new ArrayList<SpinnerData>();
		int usefulItems[] = ItemInfo.getUsefulThisGeneration();
		for (int usefulItem : usefulItems) {
			SpinnerData tempData = new SpinnerData(ItemInfo.name(usefulItem), usefulItem);
			temp.add(tempData);
		}
		ImageSpinnerAdapter itemChooserAdapter = new ImageSpinnerAdapter(getActivity(), R.layout.row_item, temp, getResources());

		itemChooser.setAdapter(itemChooserAdapter);


		abilityChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);

		abilityChooser.setAdapter(abilityChooserAdapter);
		
		genderChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		genderChooser.setAdapter(genderChooserAdapter);

		//formesChooserAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
		formesChooser.setAdapter(formListAdapter = new FormListAdapter(getActivity(), R.layout.forminlist_item, poke().gen()));
		
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
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				poke().setNum((UniqueID) arg0.getItemAtPosition(arg2));
				notifyUpdated(true);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		itemChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int orid = poke().uID().hashCode();
				
				poke().setItem((short) ItemInfo.getUsefulThisGeneration()[arg2]);
				notifyUpdated(orid != poke().uID().hashCode());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		abilityChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
                if (!poke().isHackmon) {
                    short abilities[] = PokemonInfo.abilities(poke().uID(), poke().gen.num);
                    for (short ability : abilities) {
                        if (AbilityInfo.name(ability) == abilityChooserAdapter.getItem(arg2)) {
                            poke().ability = ability;
                            notifyUpdated();
                            break;
                        }
                    }
                } else {
                    Short[] abilities = AbilityInfo.getAllAbilities(poke().gen().num);
                    for (Short ability : abilities) {
                        if (AbilityInfo.name(ability) == abilityChooserAdapter.getItem(arg2)) {
                            poke().ability = ability;
                            notifyUpdated();
                            break;
                        }
                    }
                }
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		genderChooser.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				poke().gender = (byte) GenderInfo.indexOf((String) genderChooserAdapter.getItem(arg2));
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
						.setTitle(R.string.change_level)
						.setView(input)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String link = input.getText().toString();
                                int i = 1;
                                if (link.length() != 0) {
                                    try {
                                        i = Integer.parseInt(link);
                                    } catch (Exception e) {
                                        makeToast("Enter valid number");
                                    }
                                }
								if (i > 100) i = 100;
                                if (i < 1) i = 1;
								poke().level = (byte) i;
								String s = getString(R.string.level_short) + " " + i;
								levelChooser.setText(s);
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
						.setTitle(R.string.change_happiness)
						.setView(input)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String link = input.getText().toString();
                                int i = 0;
								if (link.length() != 0) {
                                    try {
                                        i = Integer.parseInt(link);
                                    } catch (Exception e) {
                                        makeToast("Enter valid number between 0 and 255");
                                    }
                                }
								if (i > 255) i = 255;
								if (i < 0) i = 0;
								poke().happiness = (byte) i;
								String s = getString(R.string.happiness_short) + " " + i;
								happinessChooser.setText(s);
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

        manualIVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = LayoutInflater.from(getActivity()).inflate(R.layout.iv_changer, null);
                final EditText[] inputs = new EditText[6];
                inputs[0] =  (EditText) view.findViewById(R.id.iv1);
                inputs[1] = (EditText) view.findViewById(R.id.iv2);
                inputs[2] = (EditText) view.findViewById(R.id.iv3);
                inputs[3] = (EditText) view.findViewById(R.id.iv4);
                inputs[4] = (EditText) view.findViewById(R.id.iv5);
                inputs[5] = (EditText) view.findViewById(R.id.iv6);
                final byte gen = poke().gen().num;
                for (int i = 0; i < 6; i++) {
                    inputs[i].setText(String.valueOf(poke().dv(i)));
                }
                /*
                for (int i = 0; i < 6; i++) {
                    inputs[i].addTextChangedListener(new TextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            String test = "";
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            String test = "";
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            String test = "";
                        }
                    });
                }
                */
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.manual_iv)
                        .setView(view)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < 6; i++) {
                                    int temp;
                                    if (inputs[i].getText().toString().length() == 0) {
                                        temp = 0;
                                    } else {
                                        temp = Integer.decode(inputs[i].getText().toString());
                                        if (gen > 2) {
                                            if (temp > 31) temp = 31;
                                        } else {
                                            if (temp > 15) temp = 15;
                                        }
                                    }
                                    poke().DVs[i] = (byte) temp;
                                }
                                if (!poke().validHiddenPowerType(poke().hiddenPowerType())) {
                                	poke().hiddenPowerType = (byte)HiddenPowerInfo.Type(poke());
                                }
                                updateStats();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialog.cancel();
                            }
                        });
                builder.create().show();
            }
        });

        hackmonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hackmonButton.isChecked()) {
                    poke().isHackmon = true;
                } else {
                    poke().isHackmon = false;
                    resetEVs();
                }
                notifyMoveFragment();
                updatePoke();
            }
        });

        return v;
    }

    public void notifyMoveFragment() {
        ((MoveChooserFragment) getFragmentManager().getFragments().get(3)).updatePoke();
    }

    private void makeToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
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

	private String tier() {
		return ((EditPokemonActivity) getActivity()).getTier();
	}

	public void updatePoke() {
		if (poke() == null) {
			return;
		}

		TeamPoke tempPoke = poke();

		if ((tempPoke.gen().num == 6 && tempPoke.gen().subNum == 1 || tempPoke.gen().num >= 7) || tier().equals("All Gen Hackmons")) {
			hackmonButton.setEnabled(true);
			hackmonButton.setChecked(tempPoke.isHackmon);
		} else {
			hackmonButton.setEnabled(false);
			hackmonButton.setChecked(false);
		}

		if (PokemonInfo.hasVisibleFormes(tempPoke.uID(), tempPoke.gen) || (poke().isHackmon && PokemonInfo.hasHackmonFormes(tempPoke.uID()))) {
			formesLayout.setVisibility(View.VISIBLE);
			formListAdapter.clear();

			List<UniqueID> list;
			if (poke().isHackmon) list = PokemonInfo.formesHackmon(tempPoke.uID(), tempPoke.gen);
			else                  list = PokemonInfo.formes(tempPoke.uID(), tempPoke.gen);
			for (UniqueID uID : list) {
				formListAdapter.add(uID);
				if (uID.equals(tempPoke.uID())) {
					formesChooser.setSelection(formListAdapter.getCount()-1);
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
	
			abilityChooserAdapter.clear();
            if (!poke().isHackmon) {
                short[] abilities = PokemonInfo.abilities(poke().uID(), tempPoke.gen.num);

                abilityChooserAdapter.add(AbilityInfo.name(abilities[0]));
                if (abilities[0] == tempPoke.ability) {
                    abilityChooser.setSelection(0);
                }
                if (abilities[1] != 0) {
                    abilityChooserAdapter.add(AbilityInfo.name(abilities[1]));
                    if (abilities[1] == tempPoke.ability) {
                        abilityChooser.setSelection(1);
                    }
                }
                if (abilities[2] != 0) {
                    abilityChooserAdapter.add(AbilityInfo.name(abilities[2]));
                    if (abilities[2] == tempPoke.ability) {
                        abilityChooser.setSelection(abilityChooserAdapter.getCount() - 1);
                    }
                }
            } else {
                Short[] abilities = AbilityInfo.getAllAbilities(poke().gen().num);

                for (int i = 0; i <= abilities.length - 1; i++) {
                    abilityChooserAdapter.add(AbilityInfo.name(abilities[i]));
                    if (abilities[i] == tempPoke.ability) {
                        abilityChooser.setSelection(i);
                    }
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
            if (poke().isHackmon) {
                genderChooserAdapter.add(GenderInfo.name(0));
                genderChooserAdapter.add(GenderInfo.name(1));
                genderChooserAdapter.add(GenderInfo.name(2));
                genderChooser.setSelection(tempPoke.gender == 1 ? 0 : 1);
            } else {
                int genderChoice = PokemonInfo.gender(tempPoke.uID());
                if (genderChoice == 0) {
                    tempPoke.gender = 0;
                    genderChooserAdapter.add(GenderInfo.name(0));
                } else if (genderChoice == 1) {
                    genderChooserAdapter.add(GenderInfo.name(1));
                } else if (genderChoice == 2) {
                    genderChooserAdapter.add(GenderInfo.name(2));
                } else {
                    genderChooserAdapter.add(GenderInfo.name(1));
                    genderChooserAdapter.add(GenderInfo.name(2));

                    genderChooser.setSelection(tempPoke.gender == 1 ? 0 : 1);
                }
            }

			int usefulItems[] = ItemInfo.getUsefulThisGeneration();
			int pokeitem = tempPoke.item();
			for (int i = 0; i < usefulItems.length; i++) {
				if (usefulItems[i] == pokeitem) {
					itemChooser.setSelection(i);
					break;
				}
			}
			shinyChooser.setChecked(tempPoke.shiny);
			String s = getString(R.string.happiness_short) + " " + (tempPoke.happiness & 0xFF);
			happinessChooser.setText(s);

			sliders[4].setVisibility(View.VISIBLE);
			shinyChooser.setVisibility(View.VISIBLE);
			genderChooser.setVisibility(View.VISIBLE);
			itemChooser.setVisibility(View.VISIBLE);
			happinessChooser.setVisibility(View.VISIBLE);
            manualIVButton.setEnabled(true);
		} else {
			sliders[4].setVisibility(View.GONE);
			shinyChooser.setVisibility(View.GONE);
			genderChooser.setVisibility(View.GONE);
			itemChooser.setVisibility(View.GONE);
			happinessChooser.setVisibility(View.GONE);
            manualIVButton.setEnabled(false);
		}

		String s = getString(R.string.level_short) + " " + tempPoke.level();
		levelChooser.setText(s);
	}
	
	public void updateStats() {
		TeamPoke tempPoke = poke();
		for (int i = 0; i < 6; i++) {
			sliders[i].setTotal(tempPoke.stat(i));
		}
	}

	public void onEVChanged(int stat, int ev) {
		TeamPoke tempPoke = poke();
		int totalEVs = tempPoke.totalEVs() - tempPoke.ev(stat) + ev;

        if (!tempPoke.isHackmon) {
            if (totalEVs > 510 && tempPoke.gen().num > 2) {
                ev = (510 - (tempPoke.totalEVs() - tempPoke.ev(stat)));
                ev = ev / 4 * 4;
            }
        }

        if (ev > 252) ev = 252;
		poke().EVs[stat] = (byte)ev;

		sliders[stat].setNum(ev);
		sliders[stat].setTotal(tempPoke.stat(stat));

		notifyUpdated();
	}

    private void resetEVs() {
        for (int i = 0; i < 6; i++) {
            sliders[i].setNum(0);
            poke().EVs[i] = 0;
            //labels[i].setText(StatsInfo.Shortcut(i) + ": " + poke().stat(i));
			sliders[i].setTotal(poke().stat(i));
        }
    }
}
