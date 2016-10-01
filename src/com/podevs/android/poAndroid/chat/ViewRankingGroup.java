package com.podevs.android.poAndroid.chat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ViewRankingGroup {
    EditText editRankerName;
    EditText editRankerTier;

    Button buttonLeft;
    Button buttonRight;
    TextView currentPage;
    Button searchButton;

    ListView rankingList;
    ViewRankingAdapter adapter;

    int currentRank;

    public ViewRankingGroup(EditText editRankerName, EditText editRankerTier, Button buttonLeft, Button buttonRight, TextView currentPage, Button searchButton, ListView rankingList, ViewRankingAdapter adapter) {
        this.editRankerName = editRankerName;
        this.editRankerTier = editRankerTier;
        this.buttonLeft = buttonLeft;
        this.buttonRight = buttonRight;
        this.currentPage = currentPage;
        this.searchButton = searchButton;
        this.rankingList = rankingList;
        this.adapter = adapter;
    }

    public void updateViewRanking(final int startingPage, final int startingRank, final int total) {
        String s = startingPage + "/" + total;
        currentPage.setText(s);
        currentRank = startingRank;
    }

    public void updateViewRanking(final String name, final int points) {
        adapter.addRanking(currentRank, name, points);
        currentRank++;
    }
}
