package com.podevs.android.utilities;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Provides an array adapter, except it supports two text elements. You must supply
 * a Getter<T> on which will be performed get(x, 0) and get(x, 1) to get the elements
 * of the object list
 * @author coyotte508
 *
 * @param <T> The type of the objects you will pass in the constructor
 */
public class TwoViewsArrayAdapter<T> extends ArrayAdapter<T> {
	private int resource;
	private int text1, text2;
	private Getter<T> getter;
	
	/**
	 * Construcotr
	 * @param context
	 * @param resource The layout of the view 
	 * @param text1 The id of the first text element
	 * @param text2 The id of the second text element
	 * @param objects The list of items
	 * @param getter Allows to get(object, 0) and get(object, 1) to get the two texts 
	 */
	public TwoViewsArrayAdapter(Context context, int resource, int text1, int text2, List<T> objects, Getter<T> getter) {
		super(context, resource, objects);
		
		this.text1 = text1;
		this.text2 = text2;
		this.getter = getter;
		this.resource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		T object = getItem(position);
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resource, null);
		}
		
		TextView t1 = (TextView)v.findViewById(text1);
		TextView t2 = (TextView)v.findViewById(text2);
		t1.setText(getter.get(object, 0));
		t2.setText(getter.get(object, 1));
		
		return v;
	}
}
