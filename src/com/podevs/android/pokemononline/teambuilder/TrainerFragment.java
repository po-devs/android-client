package com.podevs.android.pokemononline.teambuilder;

import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.podevs.android.pokemononline.R;
import com.podevs.android.pokemononline.player.PlayerProfile;
import com.podevs.android.pokemononline.poke.Team;
import com.podevs.android.pokemononline.pokeinfo.InfoConfig;
import com.podevs.android.utilities.QColor;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TrainerFragment extends Fragment {
	protected static final String TAG = "Trainer menu";
	PlayerProfile p = null;
	boolean profileChanged = false;

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
		((AutoCompleteTextView)v.findViewById(R.id.teamTier)).setText(((TeambuilderActivity)getActivity()).team.defaultTier);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Set<String> set = getActivity().getSharedPreferences("tiers", Context.MODE_PRIVATE).getStringSet("list", null);
			
			if (set != null) {
				((AutoCompleteTextView)v.findViewById(R.id.teamTier)).setAdapter(new ArrayAdapter<String>(getActivity(),
		                 android.R.layout.simple_dropdown_item_1line, (String[])set.toArray()));
			}
		}

		Button colorButton = (Button)v.findViewById(R.id.color);
		colorButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("org.openintents.action.PICK_COLOR");
				intent.putExtra("org.openintents.extra.COLOR", p.color.colorInt);

				try {
					startActivityForResult(intent, TeambuilderActivity.PICKCOLOR_RESULT_CODE);
				} catch (ActivityNotFoundException e) {
					showDownloadDialog();
				}
			}
		});

		colorButton.setTag(R.id.color, p.color.colorInt);

		final ViewPager avatars = (ViewPager)v.findViewById(R.id.avatarChooser);
		avatars.setAdapter(new AvatarAdapter());
		avatars.setCurrentItem(p.trainerInfo.avatar, true);

		return v;
	}

	private class AvatarAdapter extends PagerAdapter {
		SparseArray<View> items = new SparseArray<View>();

		@Override
		public int getCount() {
			return 301;
		}

		@Override
		public float getPageWidth(int position) {
			return 0.25f;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Resources resources = InfoConfig.resources;
			int id =  resources.getIdentifier("t" + position, "drawable", InfoConfig.pkgName);
			ImageView v = new ImageView(getActivity());
			v.setImageResource(id);

			v.setTag(R.id.avatar, position);
			container.addView(v);
			items.put(position, v);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && position != p.trainerInfo.avatar) {
				v.setAlpha(0.4f);
			}

			v.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int avatar = (Integer) v.getTag(R.id.avatar);
					int oldAv = p.trainerInfo.avatar; 

					p.trainerInfo.avatar = (short)avatar;
					profileChanged = true;

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						if (items.get(oldAv) != null) {
							items.get(oldAv).setAlpha(0.4f);
						}
						v.setAlpha(1.0f);
					}
				}
			});

			return v;

		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (Object)arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
			items.remove(position);
		}
	}

	private AlertDialog showDownloadDialog() {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(getActivity());
		downloadDialog.setTitle("Download color picker");
		downloadDialog.setMessage("The color picker app doesn't seem to be installed!");
		downloadDialog.setPositiveButton("Download", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialogInterface, int i) {
				String packageName = "org.openintents.colorpicker";
				Uri uri = Uri.parse("market://details?id=" + packageName);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException anfe) {
					// Hmm, market is not installed
					Log.w(TAG, "Google Play is not installed; cannot install " + packageName);
				}
			}
		});
		downloadDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {}
		});
		return downloadDialog.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TeambuilderActivity.PICKCOLOR_RESULT_CODE && resultCode == Activity.RESULT_OK) {
			int colorInt = data.getIntExtra("org.openintents.extra.COLOR", p.color.colorInt);
			p.color = new QColor(colorInt);
			profileChanged = true;
		}
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
		p2.color = p.color;
		p2.trainerInfo.avatar = p.trainerInfo.avatar;

		if (profileChanged || !p2.equals(p)) {
			getActivity().setResult(Activity.RESULT_OK);
			p2.save(getActivity());
			p = p2;
			profileChanged = false;
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
