package com.podevs.android.poAndroid.chat;

import android.text.Html;
import android.view.View;
import android.widget.*;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.player.UserInfo;

public class ControlPanelGroup {
    private final EditText searchEdit;

    private final TextView status;
    private final TextView auth;
    private final TextView ip;
    private final TextView seen;
    private final ListView aliases;
    private final ArrayAdapter<String> aliasesAdapter;

    private final Button kickButton;
    private final Button muteButton;
    private final Button tempButton;
    private final Button banButton;

    public ControlPanelGroup(EditText searchEdit, TextView status, TextView auth, TextView ip, TextView seen, ListView aliases, ArrayAdapter<String> aliasesAdapter, Button kickButton, Button muteButton, Button tempButton, Button banButton) {
        this.searchEdit = searchEdit;
        this.status = status;
        this.auth = auth;
        this.ip = ip;
        this.seen = seen;
        this.aliases = aliases;
        this.aliasesAdapter = aliasesAdapter;
        this.kickButton = kickButton;
        this.muteButton = muteButton;
        this.tempButton = tempButton;
        this.banButton = banButton;
    }

    void setUserInfo(UserInfo info, final NetworkService netServ) {
        status.setText(Html.fromHtml(info.statusText()));
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

        kickButton.setEnabled(info.online());

        if (info.banned()) {
            banButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = searchEdit.getText().toString();
                    int id = netServ.getID(name);
                    if (id == -1) {
                        netServ.playerBan(name);
                    } else {
                        netServ.playerBan(id);
                    }
                }
            });
        } else {
            banButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = searchEdit.getText().toString();
                    netServ.playerUnban(name);
                }
            });
        }
    }

    void addAlias(String name) {
        try {
            aliasesAdapter.add(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
