package com.podevs.android.poAndroid.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;

public class ViewRankingAdapter extends ArrayAdapter<ViewRankingAdapter.Ranking> {

    public class Ranking {
        int rank;
        String name;
        int points;

        public Ranking(int rank, String name, int points) {
            this.rank = rank;
            this.name = name;
            this.points = points;
        }

        public String toString() {
            return rank + ". " + name + "  " + points;
        }
    }

    public ViewRankingAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void addRanking(int rank, String name, int points) {
        add(new Ranking(rank, name, points));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_rank, null);
        }

        Ranking ranking = getItem(position);
        if (ranking != null) {

            TextView rank = (TextView) view.findViewById(R.id.rank);
            rank.setText(String.valueOf(ranking.rank));

            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(ranking.name);

            TextView points = (TextView) view.findViewById(R.id.points);
            points.setText(String.valueOf(ranking.points));
        }
        return view;
    }
}
