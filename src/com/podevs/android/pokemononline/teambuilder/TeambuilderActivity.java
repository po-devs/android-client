package com.podevs.android.pokemononline.teambuilder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.PokeParser;
import com.podevs.android.pokemononline.poke.Team;

public class TeambuilderActivity extends FragmentActivity {
	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() == 2) {
			viewPager.setCurrentItem(1, true);
			return;
		}

		if (!teamChanged) {
			super.onBackPressed();
			return;
		}
		
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.save_team)
               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       team.save(TeambuilderActivity.this);
                       TeambuilderActivity.this.finish();
                   }
               })
               .setNegativeButton(R.string.no_save, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   TeambuilderActivity.this.finish();
                   }
               });
        // Create the AlertDialog object and return it
        builder.create().show();
	}

	protected ViewPager viewPager;
	
	Team team;
	TeamFragment teamFragment = null;
	TrainerFragment trainerFragment = null;
	EditPokemonFragment pokeFragment = null;
	int currentPoke = -1;

	public boolean teamChanged = false;
	
	public static final int PICKFILE_RESULT_CODE = 1;
	public static final int PICKCOLOR_RESULT_CODE = 2;
	
	private class MyAdapter extends FragmentPagerAdapter
	{
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return 2 + (currentPoke == -1 ? 0 : 1);
		}

		@Override
		public Fragment getItem(int arg0) {
			if (arg0 == 0) {
				return (trainerFragment = new TrainerFragment());
			} else if (arg0 == 1) {
				return (teamFragment = new TeamFragment());
			} else {
				pokeFragment = new EditPokemonFragment();
				pokeFragment.listener = teamFragment;
				return pokeFragment;
			}
		}
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.teambuilder);
        
        viewPager = (ViewPager)findViewById(R.id.viewpager);
		
		try {
			team = (new PokeParser(this)).getTeam();
		} catch (NumberFormatException e) {
			// The file could not be parsed correctly
			Toast.makeText(this, "Invalid team found. Loaded system default.", Toast.LENGTH_LONG).show();
			team = new Team();
		}
        
		viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
    }
    
    private void updateTeam() {
		if (teamFragment != null) {
			teamFragment.updateTeam();
		}
		if (trainerFragment != null) {
			trainerFragment.updateTeam();
		}
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
		} else if (requestCode == IntentIntegrator.REQUEST_CODE) {
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
		} else {
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}
    
    /* Triggers when team imported successfully */
    public void onTeamImported() {
    	setResult(RESULT_OK);
    	updateTeam();
    }
    
    void onImportClicked() {
		new SelectImportMethodDialogFragment().show(getSupportFragmentManager(), "select-import-method");
    };
    
    public void editPoke(int pos) {
    	currentPoke = pos;
    	
    	if (pokeFragment != null) {
    		pokeFragment.setPoke(team.poke(pos));
    	} else {
    		viewPager.getAdapter().notifyDataSetChanged();
    	}
    	
    	viewPager.setCurrentItem(2, true);
    }
}
