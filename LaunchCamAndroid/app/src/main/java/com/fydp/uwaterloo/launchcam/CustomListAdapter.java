package com.fydp.uwaterloo.launchcam;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private List listData;
    private LayoutInflater layoutInflater;

    public CustomListAdapter(Context context, List listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String string = (String) listData.get(position);
        holder.headlineView.setText(string);

        if (holder.imageView != null) {
            if(string.contains("LRV")){
                new DownloadImageTask(holder.imageView)
                        .execute("http://10.5.5.9:8080/videos/DCIM/112GOPRO/" + string.replace("LRV", "THM"));
            }
        }
        return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        ImageView imageView;
    }
}