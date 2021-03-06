package com.fydp.uwaterloo.launchcam;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.fydp.uwaterloo.launchcam.Adapters.AppPagerAdapter;
import com.fydp.uwaterloo.launchcam.Bluetooth.ConnectThread;
import com.fydp.uwaterloo.launchcam.Bluetooth.ConnectedThread;
import com.fydp.uwaterloo.launchcam.Fragments.StreamingDataFragment;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private AppPagerAdapter mAppPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public ConnectedThread connectedThread;
    BluetoothDevice capsuleBluetoothDevice = null;
    BluetoothAdapter mBluetoothAdapter;

    private static Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mAppPagerAdapter = new AppPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mAppPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                if (capsuleBluetoothDevice != null){
                    ConnectThread connect = new ConnectThread(capsuleBluetoothDevice, mBluetoothAdapter, mHandler);
                    connect.start();
                }else{
                    Utility.toast("Please pair bluetooth device first", getApplicationContext());
                }
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Utility.toast("No Blutooth Available", getApplicationContext());
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            if(Utility.BT_CAP_NAME.equals(pairedDevice.getName())){
                capsuleBluetoothDevice = pairedDevice;
            }
            Log.d("Test", "onCreate: " +pairedDevice.getName());
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Utility.SUCCESS_CONNECT:
                        // DO something
                        connectedThread = new ConnectedThread((BluetoothSocket) msg.obj, mHandler);
                        connectedThread.start();
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        Log.i(Utility.tag, "connected");
                        break;
                    case Utility.MESSAGE_READ:
                        String readBuf = (String) msg.obj;
                        Log.d("TEST", "handleMessage: "+readBuf);
                        ((StreamingDataFragment)mAppPagerAdapter.getRegisteredFragment(1)).handleDataToDraw(readBuf);
                        break;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(connectedThread != null){
            connectedThread.cancel();
        }
    }
}
