package com.fydp.uwaterloo.launchcam.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.fydp.uwaterloo.launchcam.AsyncResponse;
import com.fydp.uwaterloo.launchcam.CustomListAdapter;
import com.fydp.uwaterloo.launchcam.GetVideoMetaData;
import com.fydp.uwaterloo.launchcam.ImageActivity;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.SampleGridViewAdapter;
import com.fydp.uwaterloo.launchcam.VideoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class StreamingVideoFragment extends Fragment implements AsyncResponse{
    List<String> output = new ArrayList<>();
//    CustomListAdapter adapter;
    SampleGridViewAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    AsyncResponse clazz = this;


    @Override
    public void processFinish(List<String> videoFileNames, List<String> pictureFileNames) {
        this.output.clear();
        this.output.addAll(videoFileNames);
        this.output.addAll(pictureFileNames);
        System.out.println("async repsonse process finsihed");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        new GetVideoMetaData(this).execute("http://10.5.5.9:8080/gp/gpMediaList");


        View rootView = inflater.inflate(R.layout.video_list_layout, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetVideoMetaData(clazz).execute("http://10.5.5.9:8080/gp/gpMediaList");
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        final GridView listview = (GridView) rootView.findViewById(R.id.grid_view);
        adapter = new SampleGridViewAdapter(getActivity(), output);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

//                System.out.println(item);
                if(item.contains("JPG")){
                    Intent myIntent = new Intent(getActivity(), ImageActivity.class);
                    String url = "http://10.5.5.9:8080/videos/DCIM/112GOPRO/" + item;
                    myIntent.putExtra("url", url);
                    getActivity().startActivity(myIntent);
                }else if(item.contains("LRV")){
                    Intent myIntent = new Intent(getActivity(), VideoActivity.class);
                    String url = "http://10.5.5.9:8080/videos/DCIM/112GOPRO/" + item;
                    myIntent.putExtra("url", url);
                    getActivity().startActivity(myIntent);
                }
            }

        });
        return rootView;
    }




}
