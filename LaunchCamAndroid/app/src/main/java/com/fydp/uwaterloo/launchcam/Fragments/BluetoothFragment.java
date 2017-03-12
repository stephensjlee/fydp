package com.fydp.uwaterloo.launchcam.Fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.fydp.uwaterloo.launchcam.MainActivity;
import com.fydp.uwaterloo.launchcam.Model.CameraModel;
import com.fydp.uwaterloo.launchcam.Model.CameraStatusModel;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.Service.CameraService;
import com.fydp.uwaterloo.launchcam.Service.ServiceFactory;
import com.fydp.uwaterloo.launchcam.Utility;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class BluetoothFragment extends Fragment implements View.OnClickListener{

    View rootView = null;
    private boolean isRecording = false;
    CameraService service;

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
        return rootView;
    }

    @Override
    public void onClick(View view) {

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

                if(!isRecording){
                    // send command to record
                    service.record(1).subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                } else{
                    // send command to stop recording
                    service.record(0).subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                }
                isRecording = !isRecording;
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
                Utility.toast("Bluetooth", getActivity());
                break;
            case R.id.mediaSwitch:
//                Utility.toast("Record", getActivity());
                FloatingActionButton recordBtn = (FloatingActionButton) rootView.findViewById(R.id.record_fab);
                Button mediaSwitch = (Button) rootView.findViewById(R.id.mediaSwitch);
                if(mediaSwitch.getTag().equals(R.drawable.ic_videocam_black_36dp)){
                    mediaSwitch.setBackgroundResource(R.drawable.ic_camera_alt_black_36dp);
                    recordBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_deep_teal_200)));
                    mediaSwitch.setTag(R.drawable.ic_camera_alt_black_36dp);
                } else{
                    mediaSwitch.setBackgroundResource(R.drawable.ic_videocam_black_36dp);
                    recordBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary_dark_material_dark)));
                    mediaSwitch.setTag(R.drawable.ic_videocam_black_36dp);
                }
                break;
            default:
                break;
        }
    }
}
