package com.fydp.uwaterloo.launchcam.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fydp.uwaterloo.launchcam.R;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class StreamingDataFragment  extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }
}
