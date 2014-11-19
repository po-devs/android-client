package com.podevs.android.pokemononline.teambuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.poke.PokeParser;
import com.podevs.android.pokemononline.poke.Team;
import com.podevs.android.pokemononline.poke.TeamPoke;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TeambuilderActivity extends FragmentActivity {

	@Override
	public void onBackPressed() {
		if (!teamChanged) {
			super.onBackPressed();
			return;
		}
		
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.save_team_q)
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

	public boolean teamChanged = false;
	
	public static final int PICKFILE_RESULT_CODE = 1;
	public static final int PICKCOLOR_RESULT_CODE = 2;
	public static final int POKEEDIT_RESULT_CODE = 605;
	
	private class MyAdapter extends FragmentPagerAdapter
	{
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int arg0) {
			if (arg0 == 0) {
				return (trainerFragment = new TrainerFragment());
			} else if (arg0 == 1) {
				return (teamFragment = new TeamFragment());
			} else {
				return null;
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
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tboptions, menu);
        return true;
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.new_team:
    		team = new Team();
    		updateTeam();
    		break;
    	case R.id.load_team: {
    		final CharSequence [] files = getTeamFiles();
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.load_team)
    			.setSingleChoiceItems(files, Arrays.asList(files).indexOf(defaultFile()), null)
    			.setPositiveButton(R.string.ok, new OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					ListView lw = ((AlertDialog)dialog).getListView();
    					int w = lw.getCheckedItemPosition();
    					
    					String file = lw.getItemAtPosition(w).toString();
    					getSharedPreferences("team", 0).edit().putString("file", file).commit();
    					team = new PokeParser(TeambuilderActivity.this, file).getTeam();
    					updateTeam();
    				}
    			});
    		builder.create().show();
    		break;
    	}
    	case R.id.delete_team: {
    		final CharSequence [] files = getTeamFiles();
    		if (files.length <= 1) {
    			Toast.makeText(this, "There's only one team, you can't delete it!", Toast.LENGTH_SHORT).show();
    			break;
    		}
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.delete_team)
    			.setSingleChoiceItems(files, Arrays.asList(files).indexOf(defaultFile()), null)
    			.setPositiveButton(R.string.ok, new OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					ListView lw = ((AlertDialog)dialog).getListView();
    					int w = lw.getCheckedItemPosition();
    					
    					String copy[] = new String[files.length-1];
    					for (int i = 0; i < w; i++) {
    						copy[i] = files[i].toString();
    					}
    					for (int i = w; i < copy.length; i++) {
    						copy[i] = files[i+1].toString();
    					}

    					getSharedPreferences("team", 0).edit().putString("files", TextUtils.join("|", copy)).commit();
    					
    					if (defaultFile().equals(lw.getItemAtPosition(w).toString())) {
    						getSharedPreferences("team", 0).edit().putString("file", copy[0]).commit();
    					}
    				}
    			});
    		builder.create().show();
    		break;
    	}
    	case R.id.save_team: {
    		// Set an EditText view to get user input 
    		final EditText input = new EditText(this);
    		
    		new AlertDialog.Builder(this)
	    	    .setTitle(R.string.save_team_as)
	    	    .setMessage("Name of your neam team: ")
	    	    .setView(input)
	    	    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int whichButton) {
	    	            String file = input.getText().toString();
	    	            
	    	            if (file.contains("|") || file.contains(".") || file.contains("/")) {
	    	            	Toast.makeText(TeambuilderActivity.this, "Team name can't have dots, pipes, or slashes.", Toast.LENGTH_SHORT).show();
	    	            	return;
	    	            }
	    	            
	    	            if (file.equals("import")) {
	    	            	Toast.makeText(TeambuilderActivity.this, "This is a restricted team name! :)", Toast.LENGTH_SHORT).show();
	    	            	return;
	    	            }
	    	            
	    	            try {
	    	            	openFileOutput(file+".xml", 0).close();
	    	            } catch (Exception e) {
	    	            	Toast.makeText(TeambuilderActivity.this, "Error with the team name: " + e.toString(), Toast.LENGTH_LONG).show();
	    	            	return;
						}
	    	            
	    	            setTeamFile(file+".xml");
	    	            team.save(TeambuilderActivity.this);
	    	        }
	    	    }).show();
    		break;
    	}
        }
        return true;
    }
	
	private String defaultFile() {
		return getSharedPreferences("team", 0).getString("file", "team.xml");
	}

	private CharSequence[] getTeamFiles() {
		return getSharedPreferences("team", 0).getString("files", "team.xml").split("\\|");
	}
	
	private void setTeamFile(String string) {
		getSharedPreferences("team", 0).edit().putString("file", string).commit();
		
		CharSequence teams[] = getTeamFiles();
		
		for (CharSequence team : teams) {
			if (team.equals(string)) {
				return;
			}
		}
		
		getSharedPreferences("team", 0).edit().putString("files", string+"|"+TextUtils.join("|", teams)).commit();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == PICKFILE_RESULT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String path = intent.getData().getPath();
			    
				try {
					{
						// Copy imported file to default team location
						FileInputStream team = new FileInputStream(path);
						FileOutputStream saveTeam = openFileOutput("import.xml", Context.MODE_PRIVATE);
	
						byte[] buffer = new byte[1024];
						int length;
						while ((length = team.read(buffer))>0)
							saveTeam.write(buffer, 0, length);
						saveTeam.flush();
						saveTeam.close();
						team.close();
					}
					
					try {
						team = (new PokeParser(this, "import.xml")).getTeam();
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
		} else if (requestCode == POKEEDIT_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				int slot = intent.getIntExtra("slot", 0);
				TeamPoke poke = new TeamPoke(new Bais(intent.getExtras().getByteArray("pokemon")));

				team.setPoke(slot, poke);
				teamChanged = true;

				updateTeam();
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
    }
    
    public void editPoke(int pos) {
    	Intent intent = new Intent(this, EditPokemonActivity.class);
	    intent.putExtra("pokemon", new Baos().putBaos(team.poke(pos)).toByteArray());
	    intent.putExtra("slot", pos);
	    startActivityForResult(intent, POKEEDIT_RESULT_CODE);
    }

	public void onGenChanged() {
		updateTeam();
		PokemonInfo.resetgen6();
	}
}
