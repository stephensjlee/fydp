package com.fydp.uwaterloo.launchcam.Fragments;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fydp.uwaterloo.launchcam.AsyncTasks.ConnectRequest;
import com.fydp.uwaterloo.launchcam.DeviceSettingActivity;
import com.fydp.uwaterloo.launchcam.ImageActivity;
import com.fydp.uwaterloo.launchcam.MainActivity;
import com.fydp.uwaterloo.launchcam.Model.CameraModel;
import com.fydp.uwaterloo.launchcam.Model.CameraStatusModel;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.Service.CameraService;
import com.fydp.uwaterloo.launchcam.Service.ServiceFactory;
import com.fydp.uwaterloo.launchcam.Utility;
import com.fydp.uwaterloo.launchcam.VideoActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.http.HEAD;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.R.attr.mode;
import static android.content.ContentValues.TAG;
import static android.os.Looper.getMainLooper;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class BluetoothFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener{

    View rootView = null;
    private boolean isRecording = false;
    CameraService service;
    private final String VIDEO_MODE = "videoMode";
    private final String PICTURE_MODE = "pictureMode";
    long startTime;

    TextView timerTv;
    Handler clockHandler;
    Runnable clockRunner = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds     = seconds % 60;
            timerTv.setText(String.format("%d:%02d.%03d", minutes, seconds, millis%1000));
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

        timerTv = (TextView) rootView.findViewById(R.id.timer);

        // Periodic Status Update
        service = ServiceFactory.createRetrofitService(CameraService.class, CameraService.SERVICE_ENDPOINT);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatus();
            }
        }, 2000, 5000);

        clockHandler = new Handler(getMainLooper());


//        Spinner spinner2 = (Spinner) rootView.findViewById(R.id.spinnerTest);
//        List<String> list = new ArrayList<String>();
//        list.add("list 1");
//        list.add("list 2");
//        list.add("list 3");
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_spinner_dropdown_item, list);
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner2.setAdapter(dataAdapter);

        Spinner spinner1 = (Spinner) rootView.findViewById(R.id.spinnerTest);
        spinner1.setOnItemSelectedListener(this);

        return rootView;
    }

    private void updateStatus() {
        service.getStatus()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CameraStatusModel>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(CameraStatusModel cameraStatusModel) {
                        CameraStatusModel.StatusModel statusModel = cameraStatusModel.getStatusModel();
                        CameraStatusModel.SettingsModel settingsModel = cameraStatusModel.getSettingsModel();
                        // update battery percentage
                        ImageView batteryIcon = (ImageView) rootView.findViewById(R.id.battery);
                        switch (statusModel.getBattery()){
                            case 3:
                                // full
                                batteryIcon.setImageResource(0);
                                batteryIcon.setImageResource(R.drawable.battery_4);
                                break;
                            case 2:
                                // halfway
                                batteryIcon.setImageResource(0);
                                batteryIcon.setImageResource(R.drawable.battery_3);
                                break;
                            case 1:
                                // low
                                batteryIcon.setImageResource(0);
                                batteryIcon.setImageResource(R.drawable.battery_2);
                                break;
                            case 4:
                                // charging
                                // need to find a pic
                                break;
                            default:
                                break;
                        }

                        // update video resolution and frame rate
                        TextView resolution = (TextView) rootView.findViewById(R.id.resolution);
                        String vidResolution = "";
                        switch (settingsModel.getVideoResolution()){
                            case 9:
                                vidResolution = "1080";
                                break;
                            case 10:
                                vidResolution = "960";
                                break;
                            case 12:
                                vidResolution = "720";
                                break;
                            case 13:
                                vidResolution = "WVGA";
                                break;
                            default:
                                break;
                        }
                        // update video resolution and frame rate
                        String frameRate = "";
                        switch (settingsModel.getFrameRate()){
                            case 0:
                                frameRate = "240";
                                break;
                            case 1:
                                frameRate = "120";
                                break;
                            case 5:
                                frameRate = "60";
                                break;
                            case 8:
                                frameRate = "30";
                                break;
                            default:
                                break;
                        }
                        resolution.setText(String.format("%s-%sHz", vidResolution, frameRate));
                        // update field of view
                        TextView fov = (TextView) rootView.findViewById(R.id.field_of_view);
                        switch (settingsModel.getFieldOfView()){
                            case 0:
                                fov.setText("Wide");
                                break;
                            case 1:
                                fov.setText("Medium");
                                break;
                            case 2:
                                fov.setText("Narrow");
                                break;
                            case 4:
                                fov.setText("Linear");
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        FloatingActionButton recordBtn = (FloatingActionButton) rootView.findViewById(R.id.record_fab);

        switch (view.getId()) {
            case R.id.record_fab:
                startTime = System.currentTimeMillis();

                if(recordBtn.getTag().equals(VIDEO_MODE)){
                    if(!isRecording){
                        // send command to record
                        triggerShutter();
                        clockHandler.postDelayed(clockRunner, 10);
                    } else{
                        // send command to stop recording
                        stopRecording();
                        clockHandler.removeCallbacks(clockRunner);
                    }
                } else{
                    // picture mode commands
                    triggerShutter();
                }
                break;
            case R.id.options_btn:
                Intent myIntent = new Intent(getActivity(), DeviceSettingActivity.class);
                getActivity().startActivity(myIntent);
                break;
            case R.id.bluetooth_btn:
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Utility.toast(parent.getSelectedItem().toString(), getActivity());
        if(parent.getItemAtPosition(pos).toString().equals("1080")){
            service.setResolution("9").subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        } else{
            service.setResolution("12").subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

//    public void addListenerOnSpinnerItemSelection() {
////        Spinner spinner1 = (Spinner) rootView.findViewById(R.id.spinnerTest);
////        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
//    }
}
