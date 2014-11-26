package com.podevs.android.pokemononline;

import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by JonathanJM on 11/22/2014.
 */
public class Settings extends PreferenceActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

    }
}
