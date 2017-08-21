package com.podevs.android.poAndroid;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
	
	public void reset() {
		subTiers = new ArrayList<Tier>();
	}
	
	public ArrayAdapter<Tier> getArrayAdapter(Context c, int textViewResId) {
		return new ArrayAdapter<Tier>(c, textViewResId, subTiers);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void save(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences("tiers", Context.MODE_PRIVATE);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			prefs.edit().putStringSet("list", getTierSet()).apply();
		}
	}

	private Set<String> getTierSet() {
		Set<String> ret = new HashSet<String>();
		
		getTierSet(ret);
		
		return ret;
	}

	private void getTierSet(Set<String> ret) {
		if (subTiers.size() > 0) {
			for (Tier t: subTiers) {
				t.getTierSet(ret);
			}
		} else {
			ret.add(name);
		}
	}
}
