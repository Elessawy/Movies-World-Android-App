package com.example.android.moviesworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by AbdElrahman on 25/9/2015.
 */
public class ReviewAdapter extends BaseAdapter {

    private ArrayList<String []> data;
    private Context mContext;

    public ReviewAdapter(Context c){
        data = new ArrayList<>();
        mContext = c;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String[] getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.review_item, parent, false);
        }

        TextView userNameTextView = (TextView) view.findViewById(R.id.user_name);
        userNameTextView.setText(data.get(position)[DetailFragment.INDEX_REVIEW_AUTHOR]);

        TextView reviewTextView = (TextView) view.findViewById(R.id.review);
        reviewTextView.setText(data.get(position)[DetailFragment.INDEX_REVIEW_CONTENT]);

        return view;
    }

    public void add(ArrayList<String[]> data){
       this.data.addAll(data);
        notifyDataSetChanged();
    }
}
