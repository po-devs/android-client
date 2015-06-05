package com.podevs.android.poAndroid.teambuilder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

/**
 * Just a wrapper for EditPokemonFragment.
 *
 * Opens an activity to edit a pokemon; if edited, give the new pokemon.
 *
 * Input/output: "pokemon": byte[], serialized representation of a TeamPoke
 *               "slot": int, the slot associated with the pokemon (returns the same given in entry, defaults to 0)
 */
public class EditPokemonActivity extends FragmentActivity {
	private TeamPoke poke = null;
	private int slot = 0;

	public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            slot = savedInstanceState.getInt("slot", 0);
            poke = savedInstanceState.getParcelable("pokemon");
        } else {
            //poke = new TeamPoke(new Bais(getIntent().getExtras().getByteArray("pokemon")));
            poke = getIntent().getExtras().getParcelable("pokemon");
            slot = getIntent().getIntExtra("slot", 0);
        }

		super.onCreate(savedInstanceState);

		setContentView(R.layout.editpokemonactivity);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("slot", slot);
        outState.putParcelable("pokemon", poke);
        super.onSaveInstanceState(outState);
    }

    @Override
	public void onBackPressed() {
		EditPokemonFragment frag = (EditPokemonFragment)getSupportFragmentManager().findFragmentById(R.id.editpokemonfragment);

		if (frag != null && !frag.hasEdits()) {
			setResult(RESULT_CANCELED);
		} else {
			Intent resIntent = new Intent();
			//resIntent.putExtra("pokemon", new Baos().putBaos(poke).toByteArray());
            resIntent.putExtra("pokemon", poke);
			resIntent.putExtra("slot", slot);

			setResult(RESULT_OK, resIntent);
		}

		finish();
	}

	public TeamPoke getPoke() {
		return poke;
	}
}