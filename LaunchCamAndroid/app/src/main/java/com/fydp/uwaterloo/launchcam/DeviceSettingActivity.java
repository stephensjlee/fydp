package com.fydp.uwaterloo.launchcam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fydp.uwaterloo.launchcam.Service.CameraService;
import com.fydp.uwaterloo.launchcam.Service.ServiceFactory;

import java.util.Arrays;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Sahil on 3/13/2017.
 */

public class DeviceSettingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner res_spinner, fr_spinner, fov_spinner;
    CameraService service;
    String currentRes, currentFR, currentFOV = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        Intent intent = getIntent();
        currentRes = intent.getStringExtra("resolution");
        currentFR = intent.getStringExtra("frameRate");
        currentFOV = intent.getStringExtra("fov");

        service = ServiceFactory.createRetrofitService(CameraService.class, CameraService.SERVICE_ENDPOINT);

        res_spinner = (Spinner) findViewById(R.id.spinner_res);
        res_spinner.setOnItemSelectedListener(this);
        setInitialValue(res_spinner, currentRes);

        fr_spinner = (Spinner) findViewById(R.id.spinner_fr);
        setFrameRateOptions(currentRes);
        fr_spinner.setOnItemSelectedListener(this);
        setInitialValue(fr_spinner, currentFR);

        fov_spinner = (Spinner) findViewById(R.id.spinner_fov);
        setFieldViewOptions(currentRes);
        fov_spinner.setOnItemSelectedListener(this);
        setInitialValue(fov_spinner, currentFOV);
    }

    private void setInitialValue(Spinner mySpinner, String currentOption) {
        ArrayAdapter myAdap = (ArrayAdapter) mySpinner.getAdapter(); //cast to an ArrayAdapter
        int spinnerPosition = myAdap.getPosition(currentOption);
        //set the default according to value
        mySpinner.setSelection(spinnerPosition);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String selectedVal = parent.getItemAtPosition(pos).toString();
        Log.d("selectedVal", selectedVal);
        Log.d("parentID", String.valueOf(parent.getId()));
        Log.d("SelectedOption", res_spinner.getSelectedItem().toString());
        if(selectedVal == null){return;}
        switch (parent.getId()){
            case R.id.spinner_res:
                Log.d("Resolution", Utility.RESOLUTION.get(selectedVal));
                setResolution(Utility.RESOLUTION.get(selectedVal));
                // the frame rate and field of view depend on the resolution
                setFieldViewOptions(selectedVal);
                setFrameRateOptions(selectedVal);
                break;
            case R.id.spinner_fr:
                Log.d("Frame Rate", Utility.FRAME_RATE.get(selectedVal));
                setFrameRate(Utility.FRAME_RATE.get(selectedVal));
                break;
            case R.id.spinner_fov:
                Log.d("FOV", Utility.FOV.get(selectedVal));
                setFOV(Utility.FOV.get(selectedVal));
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void setResolution(String resolution){
        service.setResolution(resolution).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Utility.getDefaultSub());
    }

    private void setFrameRate(String frameRate){
        service.setFrameRate(frameRate).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Utility.getDefaultSub());
    }

    private void setFOV(String fov){
        service.setFOV(fov).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Utility.getDefaultSub());
    }

    private void setFrameRateOptions(String key){
        String[] fr_options = Utility.FR_DATA.get(key).split(",");
        List<String> fr_list = Arrays.asList(fr_options);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, fr_list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fr_spinner.setAdapter(dataAdapter);
    }

    private void setFieldViewOptions(String key){
        String[] fov_options = Utility.FOV_DATA.get(key).split(",");
        List<String> fov_list = Arrays.asList(fov_options);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, fov_list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fov_spinner.setAdapter(dataAdapter);
    }
}
