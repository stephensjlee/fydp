package com.fydp.uwaterloo.launchcam;

import android.content.Context;
import android.widget.Toast;

import java.util.UUID;

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
    public static void toast(String msg, Context context){
        Toast.makeText(context, msg,Toast.LENGTH_LONG).show();
    }

}
