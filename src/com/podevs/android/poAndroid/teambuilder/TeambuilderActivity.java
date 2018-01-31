package com.podevs.android.poAndroid.teambuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.*;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.Themes;
import com.podevs.android.poAndroid.poke.*;
import com.podevs.android.poAndroid.pokeinfo.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeambuilderActivity extends FragmentActivity {

	ProgressDialog progressDialog;

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
	public static final int COLORPICKER_RESULT_CODE = 3;
	public static final int POKEEDIT_RESULT_CODE = 605;
	public static final int EXPORT_PERMISSION_CODE = 10;
	public static final int IMPORT_PERMISSION_CODE = 11;
	
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
		String selection = PreferenceManager.getDefaultSharedPreferences(this).getString("theme_list", "-1");
		TypedArray ar = getResources().obtainTypedArray(R.array.themeentriesid);
		int len = ar.length();
		int[] resIds = new int[len];
		for (int i = 0; i < len; i++)
			resIds[i] = ar.getResourceId(i, 0);
		ar.recycle();
		try {
			Integer ii = Integer.parseInt(selection);
			Themes.themeTeambuilder = resIds[ii];
		} catch (Exception e) {
			Themes.themeTeambuilder = R.style.AppTheme;
		}
		setTheme(Themes.themeTeambuilder);
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

	// This function will allow you do download txt from web.
	private void downloadTeam(final URL Link) {
		new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) Link.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                InputStream in = c.getInputStream();
                final ByteArrayOutputStream bo = new ByteArrayOutputStream();

                byte[] buffer = new byte[2304]; // 2^11 + 2^8 hopefully this is enough space for information!!
                in.read(buffer); // Read from Buffer
                bo.write(buffer); // Write Into Buffer
                in.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bo.flush();
                            String textToParse = new String(trim(bo.toByteArray()));
                            bo.close();
                            if (textToParse.contains("?xml version=\"1.0\"")) {
                                team = new PokeParser(TeambuilderActivity.this, textToParse, false).getTeam();
                            } else {
                                // New parsing type
                                team = importableParse(textToParse);
                            }
                            MoveInfo.forceSetGen(team.gen.num, team.gen.subNum);
							ItemInfo.setGeneration(team.gen.num);
                            updateTeam();
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }).start();
	}

	private Team importableParse(String textToParse) {

		/*
		Tyranitar (M) @ Choice Scarf		0 	Bruh (Raikou) @ Choice Specs
		Lvl: 100							1
		Trait: Sand Stream					2
		IVs: 0 Spd							3
		EVs: 4 HP / 252 Atk / 252 Spd		4
		Jolly Nature (+Spd, -SAtk)			5
		- Stone Edge						6
		- Crunch							7
		- Superpower						8
		- Pursuit							9
		*/
		String[] stats = {" HP", " Atk", " Def", " SAtk", " SDef", " Spd", " HP", " Atk", " Def", " SpA", " SpD", " Spe"};
		Team newTeam = new Team();
		textToParse = textToParse.replace("\r\n", "\n").replace("\r", "\n").replaceAll("(\n.\n)", "\n\n");
		String[] newPokesToParse = textToParse.split("\n\n");
		Pattern p;
		Matcher m;
		for (int i = 0; i < newPokesToParse.length; i ++) {
			TeamPoke newPoke = new TeamPoke();
			String[] parseList = newPokesToParse[i].split("\n");
			newPoke.ability = PokemonInfo.abilities(newPoke.uID(), newPoke.gen.num)[0];
			boolean movesNext = false;
			boolean IVsGiven = false;
			int I = 0;
			for (String s: parseList) {
                s = s.trim();
				if (movesNext && s.contains("- ")) {
					if (s.contains("(No Move")) {
						newPoke.moves[I] = new TeamMove(0);
					}
					else {
						s = s.replace("- ", "");
						if (s.contains("Hidden Power")) {
							p = Pattern.compile(".*(\\[.*\\])");
							m = p.matcher(s);
							if (m.find()) {
								String hiddenPowerType = m.group(1);
								s = s.replace(hiddenPowerType, "").trim();
								if (!IVsGiven) {
									hiddenPowerType = hiddenPowerType.substring(1, hiddenPowerType.length() - 1);
									String[] hiddenPowers = {"Fighting","Flying","Poison","Ground","Rock","Bug","Ghost","Steel","Fire","Water","Grass","Electric","Psychic","Ice","Dragon","Dark"};
									int Type = 16; // Dark
									for (int K = 0; K < hiddenPowers.length; K++) {
										if (hiddenPowers[K].equals(hiddenPowerType)) {
											Type = K + 1;
											break;
										}
									}
									if (newPoke.gen.num < 7) {
										byte[] IVs = HiddenPowerInfo.configurationForType(Type, newPoke.gen);
										if (IVs != null) {
											newPoke.DVs = IVs;
										}
									}
									newPoke.hiddenPowerType = (byte)Type;
								}
							}
						}
						newPoke.moves[I] = new TeamMove(MoveInfo.indexOf(s));
						// If move has return make it 255 happiness
						if (newPoke.moves[I].num() == 216) {
							newPoke.happiness = (byte) 255;
						}
					}
					I++;
				} else if (s.contains("@")) {
					p = Pattern.compile("(.*) @");
					m = p.matcher(s);
					if (m.find()) {
						String pokemonLine = m.group(1);
						if (pokemonLine.contains("(") || pokemonLine.contains(")")) {
							String genderOrRealName = pokemonLine.substring(pokemonLine.indexOf("(") + 1, pokemonLine.indexOf(")"));
							if (genderOrRealName.length() > 1) {
								newPoke.uID = new UniqueID(PokemonInfo.indexOf(genderOrRealName));
								newPoke.gender = 0;
								pokemonLine = pokemonLine.replace(" (" + genderOrRealName + ")", "");
								if (pokemonLine.contains("(") || pokemonLine.contains(")")) {
									genderOrRealName = pokemonLine.substring(pokemonLine.indexOf("(") + 1, pokemonLine.indexOf(")"));
									if (genderOrRealName.equals("M")) {
										newPoke.gender = 1;
									} else if (genderOrRealName.equals("F")) {
										newPoke.gender = 2;
									}
								}
							} else {
								if (genderOrRealName.equals("M")) {
									newPoke.gender = 1;
								} else if (genderOrRealName.equals("F")) {
									newPoke.gender = 2;
								}
							}
						} else {
							newPoke.gender = 0;
						}
						pokemonLine = pokemonLine.replaceAll("( \\(.*\\))", "");
						if (newPoke.uID().pokeNum == (short) 0) {
							newPoke.uID = new UniqueID(PokemonInfo.indexOf(pokemonLine));
						}
						newPoke.nick = pokemonLine;
					}
					p = Pattern.compile("@ (.*)");
					m = p.matcher(s);
					if (m.find()) {
						String itemLine = m.group(1);
						if (!itemLine.contains("(No Item)")){
							newPoke.item = (short) ItemInfo.indexOf(itemLine);
						} else {
							newPoke.item = 0; // newPoke.item = 15; // leftovers
						}
					}
				} else if (s.contains("Lvl:") || s.contains("Level:")) {
					newPoke.level = Byte.parseByte(s.split(" ")[1]);
				} else if (s.contains("IVs:")) {
					s = s.replace("IVs: ", "");
					String[] DVs = s.split(" / ");
					for (String ss: DVs) {
						String statName = " " + ss.split(" ")[1];
						int value = Integer.parseInt(ss.split(" ")[0]);
						int statIndex = Arrays.asList(stats).indexOf(statName) % 6;
						if (statIndex != -1) {
							newPoke.EVs[statIndex] = (byte) value;
						}
					}
					IVsGiven = true;
				} else if (s.contains("EVs:")) {
					s = s.replace("EVs: ", "");
					String[] EVs = s.split(" / ");
					for (String ss: EVs) {
						String statName = " " + ss.split(" ")[1];
						int value = Integer.parseInt(ss.split(" ")[0]);
						int statIndex = Arrays.asList(stats).indexOf(statName) % 6;
						if (statIndex != -1) {
							newPoke.EVs[statIndex] = (byte) value;
						}
					}
				} else if (s.contains(" Nature")) {
                    s = s.substring(0, s.indexOf(" Nature"));
					newPoke.nature = (byte) NatureInfo.indexOf(s);
					movesNext = true;
				} else if (s.contains("Trait:") || s.contains("Ability:")) {
					s = s.replace("Trait: ", "").replace("Ability: ", "");
					if (s.contains("(No Ability")) {
						newPoke.ability = 0; //newPoke.ability = PokemonInfo.abilities(newPoke.uID(), newPoke.gen.num)[0];
					} else {
						newPoke.ability = (short) AbilityInfo.indexOf(s);
					}
					movesNext = true;
				} else if (s.contains("Shiny: Yes")) {
					newPoke.shiny = true;
				}
			}
			newTeam.setPoke(i, newPoke);
		}
		return newTeam;
	}


	private static byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0) {
			--i;
		}
		return Arrays.copyOf(bytes, i + 1);
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
    					getSharedPreferences("team", 0).edit().putString("file", file).apply();
    					team = new PokeParser(TeambuilderActivity.this, file, true).getTeam();
						MoveInfo.forceSetGen(team.gen.num, team.gen.subNum);
                        ItemInfo.setGeneration(team.gen.num);
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

    					getSharedPreferences("team", 0).edit().putString("files", TextUtils.join("|", copy)).apply();
    					
    					if (defaultFile().equals(lw.getItemAtPosition(w).toString())) {
    						getSharedPreferences("team", 0).edit().putString("file", copy[0]).apply();
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
	    	    .setMessage("Name of your new team: ")
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
    	case R.id.export_team : {
			if (Build.VERSION.SDK_INT >= 23) {
				if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
					ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXPORT_PERMISSION_CODE);
				} else {
					handleExportTeam();
				}
			} else {
				handleExportTeam();
			}
			break;
		}
            case R.id.switch_place: {
                final View view = LayoutInflater.from(this).inflate(R.layout.switch_place, null);
                final EditText[] inputs = new EditText[2];
                inputs[0] = (EditText) view.findViewById(R.id.from_slot);
                inputs[1] = (EditText) view.findViewById(R.id.to_slot);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.switch_place)
                        .setView(view)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    int spot1 = Integer.parseInt(inputs[0].getText().toString()) - 1;
                                    int spot2 = Integer.parseInt(inputs[1].getText().toString()) - 1;
                                    if (spot1 < 0 || spot2 < 0) throw new NumberFormatException();
                                    if (spot1 > 5 || spot2 > 5) throw new NumberFormatException();
                                    TeamPoke tempPoke = team.poke(spot2);
                                    team.setPoke(spot2, team.poke(spot1));
                                    team.setPoke(spot1, tempPoke);
                                    updateTeam();
                                } catch (NumberFormatException e) {
                                    Toast.makeText(TeambuilderActivity.this, "Enter Valid number from 1 to 6", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialog.cancel();
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
		getSharedPreferences("team", 0).edit().putString("file", string).apply();
		
		CharSequence teams[] = getTeamFiles();
		
		for (CharSequence team : teams) {
			if (team.equals(string)) {
				return;
			}
		}
		
		getSharedPreferences("team", 0).edit().putString("files", string+"|"+TextUtils.join("|", teams)).apply();
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
						team = (new PokeParser(this, "import.xml", true)).getTeam();
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
				//TeamPoke poke = new TeamPoke(new Bais(intent.getExtras().getByteArray("pokemon")), team.gen);
                TeamPoke poke = intent.getExtras().getParcelable("pokemon");

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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.load_team)
				.setSingleChoiceItems(R.array.import_array, -1, null)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ListView lw = ((AlertDialog)dialog).getListView();
						int w = lw.getCheckedItemPosition();

						switch (w) {
							case 0: {
								if (Build.VERSION.SDK_INT >= 23) {
									if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
										ActivityCompat.requestPermissions(TeambuilderActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMPORT_PERMISSION_CODE);
									} else {
										buildFileImportDialog();
									}
								} else {
									buildFileImportDialog();
								}
								break;
							}
							case 1: buildDownloadDialog(); break;
						}
					}
				});
		builder.create().show();
    }

    private void buildFileImportDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.importteam)
				.setSingleChoiceItems(R.array.import_file_array, -1, null)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						int option = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
						if (option == 0) { // From file
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

							intent.setType("file/*.xml");
							intent = Intent.createChooser(intent, "File explorer");
							startActivityForResult(intent, TeambuilderActivity.PICKFILE_RESULT_CODE);
						} else if (option == 1) { // From File 2
							buildFile2ImportDialog();
						}
					}
				})
				.setNegativeButton(R.string.cancel, null);
		builder.show();
	}

	private void buildFile2ImportDialog() {
		File poFolder = new File(Environment.getExternalStorageDirectory() + "/PokemonOnline");
		boolean success = true;
		if (!poFolder.exists()) {
			success = poFolder.mkdirs();
		}
		if (success) {
			final ArrayList<File> importFiles = new ArrayList<>();
			File[] files = poFolder.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					String filename = file.getName();
					String extension = filename.substring(filename.lastIndexOf(".") + 1);
					if (extension.equals("xml")) {
						importFiles.add(file);
					}
				}
				if (importFiles.size() > 0) {
					CharSequence[] names = new CharSequence[importFiles.size()];
					for (int i = 0; i < importFiles.size(); i++) {
						names[i] = importFiles.get(i).getName();
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(this);

					builder.setTitle("Select File")
							.setSingleChoiceItems(names, -1, null)
							.setPositiveButton(R.string.ok, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									int option = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
									File selected = importFiles.get(option);
									String path = selected.getPath();
									try {
										{
											// Copy imported file to default team location
											FileInputStream team = new FileInputStream(path);
											FileOutputStream saveTeam = openFileOutput("import.xml", Context.MODE_PRIVATE);

											byte[] buffer = new byte[2048];
											int length;
											while ((length = team.read(buffer)) > 0)
												saveTeam.write(buffer, 0, length);
											saveTeam.flush();
											saveTeam.close();
											team.close();
										}

										try {
											team = (new PokeParser(TeambuilderActivity.this, "import.xml", true)).getTeam();
											Toast.makeText(TeambuilderActivity.this, "Team successfully imported from " + path, Toast.LENGTH_SHORT).show();

											onTeamImported();
										} catch (NumberFormatException e) {
											Toast.makeText(TeambuilderActivity.this, "Team from " + path + " could not be parsed successfully. Is the file a valid team?", Toast.LENGTH_LONG).show();
										}
									} catch (IOException e) {
										System.out.println("Team not found");
										Toast.makeText(TeambuilderActivity.this, path + " could not be opened. Does the file exist?", Toast.LENGTH_LONG).show();
									}
								}
							})
							.setNegativeButton(R.string.cancel, null);
					builder.show();
				}
			}
		}
	}

	private void buildDownloadDialog() {
        final EditText input = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle(R.string.download_team)
                .setMessage(R.string.raw_link)
                .setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String link = input.getText().toString();

                        try {
                            URL Link = new URL(link);
                            progressDialog = ProgressDialog.show(TeambuilderActivity.this, "", "Downloading. Please wait...", true);
                            downloadTeam(Link);
                        } catch (MalformedURLException e) {
                            Toast.makeText(TeambuilderActivity.this, R.string.valid_link, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();

    }

    public void editPoke(int pos) {
    	Intent intent = new Intent(this, EditPokemonActivity.class);
	    //intent.putExtra("pokemon", new Baos().putBaos(team.poke(pos)).toByteArray());
        intent.putExtra("pokemon", team.poke(pos));
	    intent.putExtra("slot", pos);
		intent.putExtra("tier", team.defaultTier);
	    startActivityForResult(intent, POKEEDIT_RESULT_CODE);
    }

	public void onGenChanged() {
		MoveInfo.newGen();
		MoveInfo.forceSetGen(this.team.gen.num, this.team.gen.subNum);
        ItemInfo.setGeneration(team.gen.num);
		updateTeam();
		PokemonInfo.resetGen6();
       // Runtime.getRuntime().gc();
	}

	private void handleExportTeam() {
		final CharSequence [] files = getTeamFiles();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.export_team)
				.setSingleChoiceItems(files, Arrays.asList(files).indexOf(defaultFile()), null)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ListView lw = ((AlertDialog)dialog).getListView();
						int w = lw.getCheckedItemPosition();

						String file = lw.getItemAtPosition(w).toString();
						File poFolder = new File(Environment.getExternalStorageDirectory() + "/PokemonOnline");
						boolean success = true;
						if (!poFolder.exists()) {
							success = poFolder.mkdirs();
						}
						if (success) {
							File newFile = new File(Environment.getExternalStorageDirectory() + "/PokemonOnline/" + file);
							try {
								FileOutputStream fw = new FileOutputStream(newFile);
								FileInputStream fis = openFileInput(file);
								final byte[] b = new byte[4096];
								for (int r; (r = fis.read(b)) != -1;) {
									fw.write(b, 0, r);
								}
								fw.close();
								fis.close();
								Toast.makeText(TeambuilderActivity.this, R.string.export_success, Toast.LENGTH_LONG).show();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				});
		builder.create().show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch(requestCode) {
				case EXPORT_PERMISSION_CODE: handleExportTeam(); break;
				case IMPORT_PERMISSION_CODE: buildFileImportDialog(); break;
			}
		}
	}
}
