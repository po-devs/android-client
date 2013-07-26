package com.podevs.android.pokemononline.teambuilder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.player.PlayerProfile;
import com.podevs.android.pokemononline.poke.Team;

public class TrainerFragment extends Fragment {
	PlayerProfile p = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		p = new PlayerProfile(getActivity());
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
        
        ((EditText)v.findViewById(R.id.name)).append(p.nick);
        ((EditText)v.findViewById(R.id.trainerInfo)).setText(p.trainerInfo.info);
        ((EditText)v.findViewById(R.id.winning_message)).setText(p.trainerInfo.winMsg);
        ((EditText)v.findViewById(R.id.losing_message)).setText(p.trainerInfo.loseMsg);
        ((EditText)v.findViewById(R.id.teamTier)).setText(((TeambuilderActivity)getActivity()).team.defaultTier);
        
		return v;
	}

	@Override
	public void onPause() {
		/* Implement user changes */
		PlayerProfile p2 = new PlayerProfile();
		View v = getView();
		p2.nick = ((EditText)v.findViewById(R.id.name)).getText().toString();
		p2.trainerInfo.info = ((EditText)v.findViewById(R.id.trainerInfo)).getText().toString();
		p2.trainerInfo.winMsg = ((EditText)v.findViewById(R.id.winning_message)).getText().toString();
		p2.trainerInfo.loseMsg = ((EditText)v.findViewById(R.id.losing_message)).getText().toString();
		
        if (!p2.equals(p)) {
        	getActivity().setResult(Activity.RESULT_OK);
        	p2.save(getActivity());
        	p = p2;
        }
        
        if (!((EditText)v.findViewById(R.id.teamTier)).getText().toString()
        		.equals(getTeam().defaultTier)) {
        	getTeam().defaultTier = ((EditText)v.findViewById(R.id.teamTier)).getText().toString();
        	getTeam().save(getActivity());
        }
        
		super.onPause();
	}

	private Team getTeam() {
		return ((TeambuilderActivity)getActivity()).team;
	}

	public void updateTeam() {
		((EditText)getView().findViewById(R.id.teamTier)).setText(getTeam().defaultTier);
	}
}
