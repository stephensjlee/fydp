package com.fydp.uwaterloo.launchcam;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Created by stephen on 3/11/17.
 */

public class VideoActivity extends AppCompatActivity {
    String url = "";
    private static VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MediaController controller = new MediaController(this);


        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_stream);

        videoView =
                (VideoView) findViewById(R.id.videoView);

        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);

        videoView.setVideoPath(url);

        videoView.start();
    }



}