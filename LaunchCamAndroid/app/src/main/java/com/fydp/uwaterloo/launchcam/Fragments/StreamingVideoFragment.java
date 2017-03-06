package com.fydp.uwaterloo.launchcam.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fydp.uwaterloo.launchcam.R;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class StreamingVideoFragment extends Fragment {
    VideoView mVideoView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.video_stream_layout, container, false);

////        setContentView(R.layout.main);
//        mVideoView = (io.vov.vitamio.widget.VideoView) rootView.findViewById(R.id.lilwayne);
////                findViewById(R.id.vitamio_videoView);
//        mVideoView.setBufferSize(1);
//        String path = "http://10.5.5.9:8080/live/amba.m3u8";
//        mVideoView.setVideoPath(path);
//        mVideoView.setMediaController(new io.vov.vitamio.widget.MediaController(getActivity()));
//        mVideoView.requestFocus();
//        mVideoView.start();
        return rootView;
    }
}
