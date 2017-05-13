package com.podevs.android.poAndroid.chat;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.battle.ChallengeEnums;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.poAndroid.pokeinfo.*;

import java.util.HashSet;

public class ClausesListAdapter implements ListAdapter {
    public boolean[] checked = new boolean[getCount()];

    public void setChecked(int i, boolean checked)
    {
        this.checked[i] = checked;
        //notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.clause_item, null);
        }

        TextView name = (TextView)view.findViewById(R.id.clausename);
        name.setText(getItem(position).toString());

        CheckBox check = (CheckBox)view.findViewById(R.id.clausecheck);
        check.setChecked(checked[position]);

        return view;
    }

    public int getCount() {
        return ChallengeEnums.Clauses.values().length;
    }

    public Object getItem(int arg0) {
        return ChallengeEnums.Clauses.values()[arg0];
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
}
