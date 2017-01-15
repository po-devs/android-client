package com.podevs.android.poAndroid.chat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.podevs.android.poAndroid.NetworkService;

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
    int intCurrentPage;
    int intMaxPage;

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

        intCurrentPage = startingPage;
        intMaxPage = total;
    }

    public void updateViewRanking(final String name, final int points) {
        adapter.addRanking(currentRank, name, points);
        currentRank++;
    }

    public void setupButton(final NetworkService netServ) {
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intCurrentPage > 1) {
                    netServ.requestRanking(editRankerTier.getText().toString(), intCurrentPage - 1);
                    adapter.clear();
                }
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intCurrentPage < intMaxPage) {
                    netServ.requestRanking(editRankerTier.getText().toString(), intCurrentPage + 1);
                    adapter.clear();
                }
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netServ.requestRanking(editRankerTier.getText().toString(), editRankerName.getText().toString());
                adapter.clear();
            }
        });
    }
}
