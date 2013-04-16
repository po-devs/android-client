package com.pokebros.android.pokemononline;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.Toast;

import com.pokebros.android.pokemononline.player.FullPlayerInfo;

/**
 * Lets the user select a file, if import is successful overwrite "team.xml" with
 * imported team and calls activity.onTeamImportedFromFile if it implemens
 * OnTeamImportedFromFileListener.
 * 
 * @author coyotte508
 *
 */
public class ImportTeamFromFileDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Set an EditText view to get user input
		final EditText input = new EditText(getActivity());
		input.append(Environment.getExternalStorageDirectory().getPath());

		builder.setTitle("Team Import")
		.setMessage("Please type the path to your team.")
		.setView(input)
		.setPositiveButton("Import", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,	int whichButton) {
				String path = input.getText().toString();

				if (path != null) {
					try {
						/* First test if the team is valid */
						SharedPreferences prefs = getActivity().getSharedPreferences("team", Context.MODE_PRIVATE);
						prefs.edit().putString("teamFile", path); //but does not commit!
						
						FullPlayerInfo fullPlayerInfo = new FullPlayerInfo(getActivity(), prefs);
						
						if (!fullPlayerInfo.isDefault) {
							Toast.makeText(getActivity(), "Team successfully imported from " + path, Toast.LENGTH_SHORT).show();
								
							// Copy imported file to default team location
							FileInputStream team = new FileInputStream(path);
							FileOutputStream saveTeam = getActivity().openFileOutput("team.xml", Context.MODE_PRIVATE);

							byte[] buffer = new byte[1024];
							int length;
							while ((length = team.read(buffer))>0)
								saveTeam.write(buffer, 0, length);
							saveTeam.flush();
							saveTeam.close();
							team.close();
							
							/* Tells the activity that the team was successfully imported */
							OnTeamImportedFromFileListener listener = (OnTeamImportedFromFileListener) getActivity();
							if (listener != null) {
								listener.onTeamImportedFromFile(fullPlayerInfo);
							}
						} else {
							Toast.makeText(getActivity(), "Team from " + path + " could not be parsed successfully. Is the file a valid team?", Toast.LENGTH_LONG).show();
						}
					} catch (IOException e) {
						System.out.println("Team not found");
						Toast.makeText(getActivity(), path + " could not be opened. Does the file exist?", Toast.LENGTH_LONG).show();
					}
				}
			}})
		.setNegativeButton("Cancel", null);
		
		return builder.create();
	}
	
	public interface OnTeamImportedFromFileListener {
		void onTeamImportedFromFile(FullPlayerInfo fullPlayerInfo);
	}
}
