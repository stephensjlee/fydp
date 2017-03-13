package com.fydp.uwaterloo.launchcam.Fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fydp.uwaterloo.launchcam.AsyncTasks.ConnectRequest;
import com.fydp.uwaterloo.launchcam.MainActivity;
import com.fydp.uwaterloo.launchcam.Model.CameraModel;
import com.fydp.uwaterloo.launchcam.Model.CameraStatusModel;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.Service.CameraService;
import com.fydp.uwaterloo.launchcam.Service.ServiceFactory;
import com.fydp.uwaterloo.launchcam.Utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;
import static android.os.Looper.getMainLooper;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class BluetoothFragment extends Fragment implements View.OnClickListener{

    View rootView = null;
    private boolean isRecording = false;
    CameraService service;
    private final String VIDEO_MODE = "videoMode";
    private final String PICTURE_MODE = "pictureMode";

    TextView timerTv;
    Handler clockHandler;
    Runnable clockRunner = new Runnable() {
        @Override
        public void run() {
            timerTv.setText(new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date()));
            clockHandler.postDelayed(this, 1);
        }
    };

    public enum Modes {
        VIDEO(0), PICTURE(1), MULTISHOT(2);

        private final int id;
        Modes(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.bluetooth_fragment, container, false);

        // Record Button
        FloatingActionButton recordBtn = (FloatingActionButton) rootView.findViewById(R.id.record_fab);
        recordBtn.setOnClickListener(this);
        // Options Button
        Button optionsBtn = (Button) rootView.findViewById(R.id.options_btn);
        optionsBtn.setOnClickListener(this);
        // Bluetooth Button
        Button bluetoothBtn = (Button) rootView.findViewById(R.id.bluetooth_btn);
        bluetoothBtn.setOnClickListener(this);
        // Switch Toggle
        Button camVidSwitch = (Button) rootView.findViewById(R.id.mediaSwitch);
        camVidSwitch.setOnClickListener(this);

        //
        timerTv = (TextView) rootView.findViewById(R.id.timer);

//        final EditText editText = (EditText) rootView.findViewById(R.id.msg_bt_et);
//        Button sendBtn = (Button) rootView.findViewById(R.id.send_btn);
//        sendBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String tmp = "*"+editText.getText().toString();
//                if(((MainActivity) getActivity()).connectedThread != null)
//                    ((MainActivity) getActivity()).connectedThread.write(tmp.getBytes());
//                else{
//                    Utility.toast("No Bluetooth Connection", getActivity());
//                }
//            }
//        });
//        FloatingActionButton bluetoothBtn = (FloatingActionButton) rootView.findViewById(R.id.bluetooth_fab);
//        bluetoothBtn.setColorFilter(Color.BLACK);

//        ImageView bluetoothBtn = (ImageView) rootView.findViewById(R.id.bluetooth_btn);
//        redCircle.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY );
        service = ServiceFactory.createRetrofitService(CameraService.class, CameraService.SERVICE_ENDPOINT);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatus();
            }
        }, 2000, 5000);
        clockHandler = new Handler(getMainLooper());
        return rootView;
    }

    private void updateStatus() {
        Log.d("updateStatus", "updateStatus: ");
    }

    @Override
    public void onClick(View view) {
        FloatingActionButton recordBtn = (FloatingActionButton) rootView.findViewById(R.id.record_fab);

        switch (view.getId()) {
            case R.id.record_fab:
//                Utility.toast("Record", getActivity());
                ImageView batteryIcon = (ImageView) rootView.findViewById(R.id.battery);
                if(batteryIcon.getTag().equals(R.drawable.battery_4)){
                    batteryIcon.setImageResource(0);
                    batteryIcon.setImageResource(R.drawable.battery_2);
                    batteryIcon.setTag(R.drawable.battery_2);
                } else{
                    batteryIcon.setImageResource(0);
                    batteryIcon.setImageResource(R.drawable.battery_4);
                    batteryIcon.setTag(R.drawable.battery_4);
                }



                clockHandler.postDelayed(clockRunner, 10);

                if(recordBtn.getTag().equals(VIDEO_MODE)){
                    if(!isRecording){
                        // send command to record
                        triggerShutter();
                    } else{
                        // send command to stop recording
                        stopRecording();
                    }
                } else{
                    // picture mode commands
                    triggerShutter();
                }

//                isRecording = !isRecording;
                break;
            case R.id.options_btn:
//                Utility.toast("Options", getActivity());
                ImageView wifiIcon = (ImageView) rootView.findViewById(R.id.wifi);
                if(wifiIcon.getTag().equals(R.drawable.wifi_full)){
                    wifiIcon.setImageResource(0);
                    wifiIcon.setImageResource(R.drawable.wifi_low);
                    wifiIcon.setTag(R.drawable.wifi_low);
                } else{
                    wifiIcon.setImageResource(0);
                    wifiIcon.setImageResource(R.drawable.wifi_full);
                    wifiIcon.setTag(R.drawable.wifi_full);
                }
                service.getStatus()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<CameraStatusModel>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(CameraStatusModel cameraStatusModel) {
                                Log.d("getStatus",  ""+cameraStatusModel.getStatusModel().getBattery() );
                            }
                        });

                break;
            case R.id.bluetooth_btn:
//                Utility.toast("Bluetooth", getActivity());
                new ConnectRequest().execute();
                break;
            case R.id.mediaSwitch:
                Button mediaSwitch = (Button) rootView.findViewById(R.id.mediaSwitch);
                if(mediaSwitch.getTag().equals(R.drawable.ic_videocam_black_36dp)){
                    mediaSwitch.setBackgroundResource(R.drawable.ic_camera_alt_black_36dp);
                    mediaSwitch.setTag(R.drawable.ic_camera_alt_black_36dp);
                    recordBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_deep_teal_200)));
                    recordBtn.setTag(VIDEO_MODE);
                    setPrimaryMode(Modes.VIDEO.getValue());
                } else{
                    mediaSwitch.setBackgroundResource(R.drawable.ic_videocam_black_36dp);
                    mediaSwitch.setTag(R.drawable.ic_videocam_black_36dp);
                    recordBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary_dark_material_dark)));
                    recordBtn.setTag(PICTURE_MODE);
                    // if switch to camera mode in middle of recording, stop recording
                    if(isRecording){
                        stopRecording();
                    }
                    setPrimaryMode(Modes.PICTURE.getValue());
                }
                break;
            default:
                break;
        }
    }

    private void stopRecording() {
        // send command to stop recording
        service.record(0).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        isRecording = false;
    }

    /**
     * Starts recording in video mode
     * Takes a picture in picture mode
     */
    private void triggerShutter() {
        // send command to stop recording
        service.record(1).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        isRecording = true;
    }

    /**
     * Video: mode = 0
     * Picture: mode = 1
     * MultiShot: mode = 2
     * @param mode
     */
    private void setPrimaryMode(int mode) {
        // send command to stop recording
        service.primaryMode(mode).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
