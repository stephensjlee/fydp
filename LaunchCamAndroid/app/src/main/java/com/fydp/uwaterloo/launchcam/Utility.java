package com.fydp.uwaterloo.launchcam;

import android.content.Context;
import android.widget.Toast;

import com.fydp.uwaterloo.launchcam.Model.CameraModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.Subscriber;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class Utility {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String BT_CAP_NAME = "HC-05";
    public static final String BT_CAP_ADD = "HC-05";
    public static final int SUCCESS_CONNECT = 0;
    public static final int MESSAGE_READ = 1;
    public static String tag = "debugging";
    public static final Subscriber<CameraModel> defaultSubscriber = new Subscriber<CameraModel>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(CameraModel cameraModel) {}
    };
    public static void toast(String msg, Context context){
        Toast.makeText(context, msg,Toast.LENGTH_LONG).show();
    }

    public static final Map<String, String> RESOLUTION = new HashMap<String, String>()
    {{
        put("1080", "9");
        put("960", "10");
        put("720", "12");
        put("WVGA", "13");
    }};

    public static final Map<String, String> FRAME_RATE = new HashMap<String, String>()
    {{
        put("240", "0");
        put("120", "1");
        put("60", "5");
        put("30", "8");
    }};

    public static final Map<String, String> FOV = new HashMap<String, String>()
    {{
        put("Wide", "0");
        put("Medium", "1");
        put("Narrow", "2");
        put("Linear", "4");
    }};

    public static final Map<String, String> FR_DATA = new HashMap<String, String>()
    {{
        put("1080", "30,60");
        put("960", "30,60");
        put("720", "30,60,100");
        put("WVGA", "120");
    }};

    public static final Map<String, String> FOV_DATA = new HashMap<String, String>()
    {{
        put("1080", "Wide,Medium");
        put("960", "Wide");
        put("720", "Wide,Medium");
        put("WVGA", "Wide");
    }};
}
