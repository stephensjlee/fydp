package com.fydp.uwaterloo.launchcam.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fydp.uwaterloo.launchcam.MainActivity;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.Utility;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class BluetoothFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bluetooth_fragment, container, false);
        final EditText editText = (EditText) rootView.findViewById(R.id.msg_bt_et);
        Button sendBtn = (Button) rootView.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = "*"+editText.getText().toString();
                if(((MainActivity) getActivity()).connectedThread != null)
                    ((MainActivity) getActivity()).connectedThread.write(tmp.getBytes());
                else{
                    Utility.toast("No Bluetooth Connection", getActivity());
                }
            }
        });
        return rootView;
    }
}
