package com.podevs.android.poAndroid.chat;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.podevs.android.poAndroid.player.UserInfo;

public class ControlPanelGroup {
    private final EditText searchEdit;

    private final TextView status;
    private final TextView auth;
    private final TextView ip;
    private final TextView seen;
    private final ListView aliases;
    private final ArrayAdapter<String> aliasesAdapter;

    ControlPanelGroup(EditText searchEdit, TextView status, TextView auth, TextView ip, TextView seen, ListView aliases, ArrayAdapter<String> aliasesAdapter) {
        this.searchEdit = searchEdit;
        this.status = status;
        this.auth = auth;
        this.ip = ip;
        this.seen = seen;
        this.aliases = aliases;
        this.aliasesAdapter = aliasesAdapter;
    }

    void setUserInfo(UserInfo info) {
        String statusString = info.flags == 0 ? "On" : "Off";
        statusString = statusString + " - " + info.os;
        status.setText(statusString);
        String authString = "";
        switch (info.auth) {
            case 0: authString = "User"; break;
            case 1: authString = "Mod"; break;
            case 2: authString = "Admin"; break;
            case 3: authString = "Owner"; break;
            case 4: authString = "Hidden"; break;
        }
        auth.setText(authString);
        ip.setText(info.ip);
        seen.setText(info.date);

        aliasesAdapter.clear();
    }

    void addAlias(String name) {
        try {
            aliasesAdapter.add(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
