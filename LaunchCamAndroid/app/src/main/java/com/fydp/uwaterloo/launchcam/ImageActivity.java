package com.fydp.uwaterloo.launchcam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

/**
 * Created by stephen on 3/12/17.
 */

public class ImageActivity extends AppCompatActivity {
    String url = "";
    ImageView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);
        new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                .execute(url);


    }

}
