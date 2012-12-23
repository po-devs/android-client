package com.pokebros.android.pokemononline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.zxing.integration.android.IntentIntegrator;

public class SelectImportMethodDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle("Import team...")
		.setSingleChoiceItems(new CharSequence[]{"From file", "From QR code"}, -1, null)
		.setPositiveButton("Import", new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int option = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
				if (option == 0) { // From File
					DialogFragment fragment = new ImportTeamFromFileDialogFragment();
					fragment.show(getActivity().getSupportFragmentManager(), "team-from-file");
				} else if (option == 1) { // From QR Code
					AlertDialog result = IntentIntegrator.initiateScan(getActivity());
					if (result != null)
						result.show();
				}
			}
		})
		.setNegativeButton(R.string.cancel, null);

		return builder.create();
	}
}
