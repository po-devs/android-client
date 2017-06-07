package com.podevs.android.poAndroid.teambuilder;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.poAndroid.pokeinfo.*;

import java.util.HashSet;

public class MoveListAdapter implements ListAdapter {
	TeamPoke poke = null;

	public void setPoke(TeamPoke poke) {
		this.poke = poke;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.move_item, null);
		}
        short move;
        if (poke.isHackmon) {
            move = MoveInfo.getAllMoves()[position];
        } else {
            move = PokemonInfo.moves(poke.uID(), poke.gen.num, poke.gen.subNum)[position];
        }

		TextView nick = (TextView)view.findViewById(R.id.movename);
		nick.setText(MoveInfo.name(move));

		TextView power = (TextView)view.findViewById(R.id.power);
		if (move == 237) {
			power.setText("pow: " + HiddenPowerInfo.power(poke));
		} else {
			power.setText("pow: " + MoveInfo.powerString(move));
		}

		TextView pps = (TextView)view.findViewById(R.id.pps);
		pps.setText("pp: " + MoveInfo.pp(move));

		TextView accuracy = (TextView)view.findViewById(R.id.accuracy);
		accuracy.setText("acc: " + MoveInfo.accuracyString(move));

		ImageView type = (ImageView)view.findViewById(R.id.type);
		if (move == 237) {
			if (poke.gen().num < 7) {
				type.setImageResource(TypeInfo.typeRes(HiddenPowerInfo.Type(poke)));
			} else {
				type.setImageResource(TypeInfo.typeRes(poke.hiddenPowerType()));
			}
		} else {
			type.setImageResource(TypeInfo.typeRes(MoveInfo.type(move)));
		}

		TextView damageClass = (TextView)view.findViewById(R.id.damageClass);
		damageClass.setText(DamageClassInfo.name(MoveInfo.damageClass(move)));
/*
		ImageView type = (ImageView)view.findViewById(R.id.type);
		type.setImageResource(DamageClassInfo.damageClassRes(MoveInfo.damageClass(move)));
*/

		if (poke != null) {
			CheckBox check = (CheckBox)view.findViewById(R.id.check);
			check.setChecked(poke.hasMove(move));
		}

		return view;
	}

	public int getCount() {
        if (poke.isHackmon) {
            return MoveInfo.getAllMoves().length;
        } else {
            return PokemonInfo.moves(poke.uID(), poke.gen.num, poke.gen.subNum).length;
        }
	}

	public Object getItem(int arg0) {
        if (poke.isHackmon) {
            return MoveInfo.getAllMoves()[arg0];
        } else {
            return PokemonInfo.moves(poke.uID(), poke.gen.num, poke.gen.subNum)[arg0];
        }
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public int getItemViewType(int arg0) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}

	private HashSet<DataSetObserver> observers = new HashSet<DataSetObserver>();

	public void registerDataSetObserver(DataSetObserver arg0) {
		observers.add(arg0);
	}

	public void unregisterDataSetObserver(DataSetObserver arg0) {
		observers.remove(arg0);
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	public void notifyDataSetChanged() {
		for (DataSetObserver obs : observers) {
			obs.onChanged();
		}
	}

	public String moveInfo(int num) {
		return "Move: " + MoveInfo.name(num)
				+ "\nPower: " + MoveInfo.powerString(num)
				+ "\nAccuracy: " + MoveInfo.accuracyString(num)
				+ "\nClass: " + DamageClassInfo.name(MoveInfo.damageClass(num))
				+ "\nRange: " + MoveInfo.targetString(num)
				+ "\n"
				+ "\nEffect: " + MoveInfo.effect(num);
	}
}
