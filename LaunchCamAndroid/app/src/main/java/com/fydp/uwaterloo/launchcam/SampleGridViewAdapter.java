package com.fydp.uwaterloo.launchcam;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class SampleGridViewAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> urls = new ArrayList<>();

    public SampleGridViewAdapter(Context context, List<String> listUrl) {
        this.context = context;

        urls.addAll(listUrl);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        String url = getItem(position);


//        if(url.contains("JPG")){
//            System.out.println(url);
//            // Trigger the download of the URL asynchronously into the image view.
//            Picasso.with(context) //
//                    .load(R.drawable.camera_icon_68860) //
//                    .placeholder(R.mipmap.ic_launcher) //
//                    .error(R.drawable.battery_1) //
//                    .fit()
//                    .tag(context) //
//                    .into(view);
//        }else
            if(url.contains("LRV")){

            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(context) //
                    .load("http://10.5.5.9:8080/videos/DCIM/112GOPRO/" + url.replace("LRV", "THM")) //
                    .placeholder(R.mipmap.ic_launcher) //
                    .error(R.drawable.battery_1) //
                    .fit() //
                    .tag(context) //
                    .into(view);
        }


        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}