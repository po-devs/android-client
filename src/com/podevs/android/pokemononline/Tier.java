package com.podevs.android.pokemononline;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;

public class Tier {
	public byte level = 0;
	public String name = "null";
	public ArrayList<Tier> subTiers = new ArrayList<Tier>();
	public Tier parentTier = null;
	
	public Tier(byte level, String name) {
		this.level = level;
		this.name = name;
		subTiers = new ArrayList<Tier>();
	}
	
	public Tier() { subTiers = new ArrayList<Tier>(); }
	
	public String toString() { return name + (subTiers.size() > 0 ? "..." : ""); }
	
	public void addSubTier(Tier t) {
		subTiers.add(t);
	}
	
	public ArrayAdapter<Tier> getArrayAdapter(Context c, int textViewResId) {
		ArrayAdapter<Tier> aat = new ArrayAdapter<Tier>(c, textViewResId, subTiers);
		return aat;
	}
}
