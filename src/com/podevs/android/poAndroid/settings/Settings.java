package com.podevs.android.poAndroid.settings;

import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import com.podevs.android.poAndroid.MessageListAdapter;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.registry.CustomExceptionHandler;
import com.podevs.android.poAndroid.registry.RegistryActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Settings extends PreferenceFragment {
    private static final Pattern hex = Pattern.compile("[#][A-F0-9]{6}");
    private static final String[] keys = {"flashColor", "soundVolume", "shouldWrite", "pokemonNumber", "copyandpaste", "localize"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setListeners();
    }

    private void setListeners() {
        for (String key: keys){
            Preference p = this.findPreference(key);
            p.setOnPreferenceChangeListener(changeListener);
        }
    }

    private Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (key.equals("flashColor")) {
                return dealWithFlashColor(newValue.toString());
            }
            if (key.equals("pokemonNumber")) {
                return dealWithPokemonNumber(newValue.toString());
            }
            if (key.equals("soundVolume")) {
                return dealWithVolume(newValue.toString());
            }
            if (key.equals("shouldWrite")) {
                CustomExceptionHandler.shouldWrite = (Boolean)newValue;
                if ((Boolean) newValue) makeToast(Environment.getExternalStorageDirectory().getAbsolutePath());
                return true;
            }
            if (key.equals("copyandpaste")) {
                Boolean val = (Boolean) newValue;
                MessageListAdapter.copyandpaste = val;
                if (val) {
                    CheckBoxPreference p = (CheckBoxPreference) findPreference("shouldTryTapMenu");
                    if (p.isChecked()) {
                        p.setChecked(false);
                    }
                }
            }
            if (key.equals("localize")) {
                RegistryActivity.localize_assets = (Boolean) newValue;
            }
            return true;
        }
    };

    private Boolean dealWithFlashColor(String color) {
        if (color.length() != 7) {
            makeToast("Enter a valid color Hex String" + "\n" + "Example: #00AF09");
        } else {
            Matcher m = hex.matcher(color);
            if (m.matches()) {
                makeToast(color);
                return true;
            }
        }
        makeToast("Enter a valid color Hex String" + "\n" + "Example: #00AF09");
        return false;
    }

    private Boolean dealWithPokemonNumber(String number) {
        try {
            Integer i = Integer.parseInt(number);
            if (718 >= i && i > 0) {
                makeToast("Pokemon: " + number);
                return true;
            }
            return false;
        } catch (Exception e) {
            makeToast("Enter a valid pokemon number");
            return false;
        }
    }

    private Boolean dealWithVolume(String number) {
        try {
        Integer i = Integer.parseInt(number);
            if (i <= 100 && i >= 0) {
                makeToast("Volume: " + number);
                return true;
            }
            return false;
        } catch (Exception e) {
            makeToast("Select a value between 0 and 100");
            return false;
        }
    }

    private void makeToast(final String s) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Settings", "NULL ACTIVITY: Could not create toast");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
