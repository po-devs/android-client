package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.battle.ListedPokemon;
import com.podevs.android.pokemononline.poke.TeamPoke;
import com.podevs.android.pokemononline.poke.UniqueID;
import com.podevs.android.pokemononline.teambuilder.MoveChooserFragment.MoveChooserListener;
import com.podevs.android.pokemononline.teambuilder.PokemonChooserFragment.PokemonChooserListener;
import com.podevs.android.pokemononline.teambuilder.PokemonDetailsFragment.PokemonDetailsListener;

public class EditPokemonFragment extends Fragment implements PokemonChooserListener, PokemonDetailsListener, MoveChooserListener {
	private ListedPokemon pokeList;
	private ViewPager pager = null;
	
	private PokemonChooserFragment pokemonChooser = null;
	private PokemonDetailsFragment pokemonDetails = null;
	private MoveChooserFragment moveChooser = null;

	private boolean pokeChanged = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.edit_pokemon, container, false);
		
		pokeList = new ListedPokemon((RelativeLayout)v.findViewById(R.id.pokeViewLayout));
		
		pager = (ViewPager)v.findViewById(R.id.editpokeviewpager);
		pager.setAdapter(new EditPokeAdapter(getFragmentManager()));
		
		pokeList.setOnImageClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				pager.setCurrentItem(0);
			}
		});
		
		pokeList.setOnDetailsClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pager.setCurrentItem(1);
			}
		});
		
		pokeList.setOnMoveClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pager.setCurrentItem(2);
			}
		});

		updatePoke();
		
		return v;
	}
	
	public void updatePoke() {
		updateHeader();
		if (pokemonChooser != null) {
			pokemonChooser.setDetails(poke().uID(), poke().nick);
		}
		updateDetails();
		if (moveChooser != null) {
			moveChooser.updatePoke();
		}
	}
	
	private void updateDetails() {
		if (pokemonDetails != null) {
			pokemonDetails.updatePoke();
		}
	}

	private void updateHeader() {
		pokeList.update(poke());
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private TeamPoke poke() {
		return ((EditPokemonActivity) getActivity()).getPoke();
	}
	
	class EditPokeAdapter extends FragmentPagerAdapter {

		public EditPokeAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			if (arg0 == 0) {
				pokemonChooser = new PokemonChooserFragment();
				pokemonChooser.setDetails(poke().uID(), poke().nick);
				pokemonChooser.setOnPokemonChosenListener(EditPokemonFragment.this);
				return pokemonChooser;
			} else if (arg0 == 1) {
				pokemonDetails = new PokemonDetailsFragment();
				pokemonDetails.listener = EditPokemonFragment.this;
				return pokemonDetails;
			} else if (arg0 == 2) {
				moveChooser = new MoveChooserFragment();
				moveChooser.listener = EditPokemonFragment.this;
				return moveChooser;
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
		
	}

	public void onPokemonChosen(UniqueID id, String nickname) {
		poke().setNum(id);
		if (nickname.length() > 0) {
			poke().nick = nickname;
		}
		
		updatePoke();
		pokeChanged = true;
		
		pager.setCurrentItem(1);
	}

	public void onPokemonEdited(boolean updateAll) {
		if (!updateAll) {
			updateHeader();
		} else {
			updatePoke();
		}
		pokeChanged = true;
	}

	public void onMovesetChanged(boolean stats) {
		updateHeader();
		
		if (stats) {
			updateDetails();
		}

		pokeChanged = true;
	}

	public boolean hasEdits() {
		return pokeChanged;
	}
}
