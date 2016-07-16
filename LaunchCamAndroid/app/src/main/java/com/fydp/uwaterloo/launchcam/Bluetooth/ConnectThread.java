package com.fydp.uwaterloo.launchcam.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.fydp.uwaterloo.launchcam.Utility;

import java.io.IOException;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    String tag = Utility.tag;
    BluetoothAdapter btAdapter;
    Handler mHandler;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter btAdapter, Handler mHandler) {
        this.btAdapter = btAdapter;
        this.mHandler = mHandler;
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        Log.i(tag, "construct");
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(Utility.MY_UUID);
        } catch (IOException e) {
            Log.i(tag, "get socket failed");

        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        mHandler.obtainMessage(Utility.SUCCESS_CONNECT, mmSocket).sendToTarget();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
