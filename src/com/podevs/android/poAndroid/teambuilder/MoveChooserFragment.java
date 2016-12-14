package com.podevs.android.poAndroid.teambuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.poAndroid.pokeinfo.HiddenPowerInfo;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.GenInfo;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MoveChooserFragment extends Fragment {
	public interface MoveChooserListener {
		public void onMovesetChanged(boolean stats);
	}

	private ListView moveList = null;
	private MoveListAdapter moveAdapter = null;
    private AutoCompleteTextView moveChoice = null;
    private ArrayAdapter<String> moveChoiceAdapter = null;
	public MoveChooserListener listener = null;
    private ArrayList<String> names = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.movelist, container, false);

		moveList = (ListView)v.findViewById(R.id.moves);
		moveAdapter = new MoveListAdapter();
		moveAdapter.setPoke(poke());

		moveList.setAdapter(moveAdapter);

        moveChoice = (AutoCompleteTextView) v.findViewById(R.id.moveChoice);
        moveChoiceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line);
        updateNames();
        moveChoice.setAdapter(moveChoiceAdapter);

        moveChoice.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                moveList.setSelection(names.indexOf(moveChoiceAdapter.getItem(arg2)));
            }
        });

		moveList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int move = (Short) moveAdapter.getItem(arg2);
				if (!poke().hasMove(move) && poke().addMove(move)) {
					((CheckBox)arg1.findViewById(R.id.check)).setChecked(true);

					/* Hidden Power */
					if (move == 237) {
						buildHiddenPowerDialog();
					} else if (move == 216) { /* return */
						poke().happiness = (byte) 255;
					}
					
					if (listener != null) {
						listener.onMovesetChanged(move == 216);
					}
				} else if (poke().removeMove(move)) {
					((CheckBox)arg1.findViewById(R.id.check)).setChecked(false);

					/* Hidden Power */
					if (move == 237 && poke().gen().num < 7) {
						for (int i = 0; i < 6; i++) {
							poke().DVs[i] = 31;
						}
					} else if (move == 216) { /* return */
						poke().happiness = 0;
					}
					
					if (listener != null) {
						listener.onMovesetChanged(move == 237 || move == 216);
					}
				}
			}
		});

		moveList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				int move = (Short) moveAdapter.getItem(position);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(moveAdapter.moveInfo(move));
				builder.create().show();
				return false;
			}
		});

		return v;
	}

	protected void buildHiddenPowerDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.hiddenpower_choice)
			.setSingleChoiceItems(R.array.hp_array, poke().hiddenPowerType()-1, null)
			.setPositiveButton(R.string.ok, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ListView lw = ((AlertDialog)dialog).getListView();
					int type = lw.getCheckedItemPosition() + 1;
					if (poke().validHiddenPowerType(type)) {
						poke().hiddenPowerType = (byte)type;
					}

					if (poke().gen().num < 7) {
						byte[] config = HiddenPowerInfo.configurationForType(type, poke().gen);
						if (config != null) {
							poke().DVs = config;
							if (listener != null) {
								listener.onMovesetChanged(true);
							}
						}
					}
				}
			});

		builder.create().show();
	}

	public void updatePoke() {
        updateNames();
		moveAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

    public void updateNames() {
        if (poke().isHackmon) {
            names = MoveInfo.makeList(MoveInfo.getAllMoves());
        } else {
            names = MoveInfo.makeList(PokemonInfo.moves(poke().uID, poke().gen.num, poke().gen.subNum));
        }
        if (moveChoiceAdapter != null) {
            moveChoiceAdapter.clear();
            moveChoiceAdapter.addAll(names);
        }
    }


	private TeamPoke poke() {
		return ((EditPokemonActivity) getActivity()).getPoke();
	}
}
