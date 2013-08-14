package com.podevs.android.pokemononline.teambuilder;

import com.podevs.android.pokemononline.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MoveChooserFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.movelist, container, false);
		
		return v;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
}
