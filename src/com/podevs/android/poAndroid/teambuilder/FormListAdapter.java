package com.podevs.android.poAndroid.teambuilder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.Gen;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;
import com.podevs.android.utilities.SpinnerData;

import java.util.ArrayList;

public class FormListAdapter extends ArrayAdapter<UniqueID> {
    private Gen gen = null;
    LayoutInflater inflater;
    final static String pkgName = "com.podevs.android.poAndroid";


    public FormListAdapter(Activity spinner, int id, Gen gen) {
        super(spinner, id);

        this.gen = gen;

        inflater = (LayoutInflater) spinner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.forminlist_item, parent, false);
        }

        UniqueID uID = getItem(position);

        ((TextView)convertView.findViewById(R.id.pokename)).setText(PokemonInfo.name(uID));
        ((ImageView)convertView.findViewById(R.id.type1)).setImageResource(TypeInfo.typeRes(PokemonInfo.type1(uID, gen.num)));

        int type2 = PokemonInfo.type2(uID, gen.num);
        ImageView itype2 = ((ImageView)convertView.findViewById(R.id.type2));

        itype2.setImageResource(TypeInfo.typeRes(type2));
        itype2.setVisibility(type2 == TypeInfo.Type.Curse.ordinal() ? View.INVISIBLE : View.VISIBLE);

        return convertView;
    }

    @Override
    public void clear() {
        super.clear();
    }
}