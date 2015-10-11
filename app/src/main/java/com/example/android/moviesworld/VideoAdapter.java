package com.example.android.moviesworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by AbdElrahman on 23/9/2015.
 */
public class VideoAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String[]> data;

    public VideoAdapter(Context c){
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
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.video_title);
        textView.setText(data.get(position)[DetailFragment.INDEX_VIDEO_NAME]);

        return view;
    }

    public void add(ArrayList<String[]> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}
