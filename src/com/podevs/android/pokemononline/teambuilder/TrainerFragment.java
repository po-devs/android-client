package com.podevs.android.pokemononline.teambuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.podevs.android.pokemononline.R;

public class TrainerFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.teambuilder_root, container, false);
		
        Button importbutton = (Button)v.findViewById(R.id.importteambutton);
        //Register onClick listener
        importbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((TeambuilderActivity)getActivity()).onImportClicked();
			}
		});
        
        Button editTeamButton = (Button)v.findViewById(R.id.editteam);
        editTeamButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				((TeambuilderActivity)getActivity()).viewPager.setCurrentItem(1, true);	
			}
		});
        
		return v;
	}

	@Override
	public void onPause() {
		/* Implement user changes */
		super.onPause();
	}
}
