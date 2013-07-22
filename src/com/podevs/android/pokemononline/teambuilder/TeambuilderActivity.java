package com.podevs.android.pokemononline.teambuilder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.battle.ListedPokemon;
import com.podevs.android.pokemononline.poke.PokeParser;
import com.podevs.android.pokemononline.poke.Team;
import com.podevs.android.pokemononline.pokeinfo.InfoConfig;

public class TeambuilderActivity extends FragmentActivity {
	protected ViewPager viewPager;

	View mainLayout, teamLayout, editPokeLayout;
	ListedPokemon pokeList[] = new ListedPokemon[6];
	
	Team team;
	
	public static final int PICKFILE_RESULT_CODE = 1;
	
	private class MyAdapter extends PagerAdapter
	{
		@Override
		public int getCount() {
			return 3;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			switch (position) {
			case 0: container.addView(mainLayout);return mainLayout;
			case 1: container.addView(teamLayout);return teamLayout;
			case 2: container.addView(editPokeLayout);return editPokeLayout;
			}
			return null;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (Object)arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = (ViewPager) new ViewPager(this);
		mainLayout = getLayoutInflater().inflate(R.layout.teambuilder_root, null);
		teamLayout = getLayoutInflater().inflate(R.layout.battle_teamscreen, null);
		editPokeLayout = getLayoutInflater().inflate(R.layout.edit_pokemon, null);
		
		viewPager.setAdapter(new MyAdapter());
		
		for (int i = 0; i < 6; i++) {
			RelativeLayout whole = (RelativeLayout)teamLayout.findViewById(
					InfoConfig.resources.getIdentifier("pokeViewLayout" + (i+1), "id", InfoConfig.pkgName));
			pokeList[i] = new ListedPokemon(whole);
		}
		
		try {
			team = (new PokeParser(this)).getTeam();
		} catch (NumberFormatException e) {
			// The file could not be parsed correctly
			Toast.makeText(this, "Invalid team found. Loaded system default.", Toast.LENGTH_LONG).show();
			team = new Team();
		}		
		setContentView(viewPager);
		
        Button importbutton = (Button)mainLayout.findViewById(R.id.importteambutton);
        //Register onClick listener
        importbutton.setOnClickListener(listener);
        
        Button editTeamButton = (Button)mainLayout.findViewById(R.id.editteam);
        editTeamButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				viewPager.setCurrentItem(1, true);	
			}
		});
        
        /* Updates the team */
        updateTeam();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == PICKFILE_RESULT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String path = intent.getData().getPath();
			    
				try {
					{
						// Copy imported file to default team location
						FileInputStream team = new FileInputStream(path);
						FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);
	
						byte[] buffer = new byte[1024];
						int length;
						while ((length = team.read(buffer))>0)
							saveTeam.write(buffer, 0, length);
						saveTeam.flush();
						saveTeam.close();
						team.close();
					}
					
					try {
						team = (new PokeParser(this)).getTeam();
						Toast.makeText(this, "Team successfully imported from " + path, Toast.LENGTH_SHORT).show();
						
						/* Tells the activity that the team was successfully imported */
						onTeamImported();
					} catch (NumberFormatException e) {
						Toast.makeText(this, "Team from " + path + " could not be parsed successfully. Is the file a valid team?", Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					System.out.println("Team not found");
					Toast.makeText(this, path + " could not be opened. Does the file exist?", Toast.LENGTH_LONG).show();
				}
			}
		} else {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanResult != null && "QR_CODE".equals(scanResult.getFormatName())) {
				try {
					/* TODO: Maybe avoid writing into team.xml the first try. Maybe give the FullPlayerInfo
					 * Constructor something other than a file handle.
					 */
					byte[] qrRead = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
					if (qrRead == null)
						Toast.makeText(TeambuilderActivity.this, "Team from QR code could not be parsed successfully.", Toast.LENGTH_LONG).show();
					// Discard the first 4 bits. These set the mode of the qr data (always the same for us)
					for(int i = 0; i < qrRead.length - 1; i++)
						// The new byte is your lower 4 bits and the upper 4 bits of the next guy
						qrRead[i] = (byte) (((qrRead[i] & 0xf) << 4) | ((qrRead[i+1] & 0xf0) >>> 4));
					// Read in the length (two bytes)
					int qrLen = ((int)(qrRead[0]) << 8) | ((int)qrRead[1] & 0xff);
					InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(qrRead, 2, qrLen));
					FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = iis.read(buffer))>0)
						saveTeam.write(buffer, 0, length);
					saveTeam.flush();
					saveTeam.close();
	
					try {
						team = (new PokeParser(this)).getTeam();
						Toast.makeText(TeambuilderActivity.this, "Team successfully imported from QR code", Toast.LENGTH_SHORT).show();
						
						/* Tells the activity that the team was successfully imported */
						onTeamImported();
					} catch (NumberFormatException e) {
						Toast.makeText(TeambuilderActivity.this, "Team from QR code could not be parsed successfully. Is the QR code a valid team?", Toast.LENGTH_LONG).show();
						deleteFile("team.xml");
					}
					
					/* Acts as if we imported a new team, i.e. loads from file */
					onTeamImported();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					Toast.makeText(TeambuilderActivity.this, "Team from QR code could not be parsed successfully. Is the QR code a valid team?", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
    
    public void updateTeam() {
    	for (int i = 0; i < 6; i++) {
			pokeList[i].update(team.poke(i), true);
		}
    }
    
    /* Triggers when team imported successfully */
    public void onTeamImported() {
    	setResult(RESULT_OK);
    }
    
    private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
    		if (v == findViewById(R.id.importteambutton)) {
    			new SelectImportMethodDialogFragment().show(getSupportFragmentManager(), "select-import-method");
    		}
    	}
    };
}
