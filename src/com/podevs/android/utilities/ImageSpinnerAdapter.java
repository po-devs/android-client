package com.podevs.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;

import java.util.ArrayList;

/**
 * Created by JonathanJM on 3/5/2015.
 */
public class ImageSpinnerAdapter extends ArrayAdapter<SpinnerData> {
    private Activity activity;
    private ArrayList<SpinnerData> data;
    public Resources resources;
    LayoutInflater inflater;
    final static String pkgName = "com.podevs.android.poAndroid";


    public ImageSpinnerAdapter(Activity spinner, int id, ArrayList<SpinnerData> objects, Resources resources) {

        super(spinner, id, objects);

        activity = spinner;
        data = objects;
        this.resources = resources;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

        View row = inflater.inflate(R.layout.row_item, parent, false);

        SpinnerData value = data.get(position);

        TextView text = (TextView)row.findViewById(R.id.text);
        ImageView image = (ImageView)row.findViewById(R.id.image);

        text.setText(value.text);
        image.setImageResource(resources.getIdentifier("i" + value.key, "drawable" ,pkgName));

        return row;
    }
}
