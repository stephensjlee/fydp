package com.fydp.uwaterloo.launchcam.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.fydp.uwaterloo.launchcam.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler mHandler) {
        this.mHandler = mHandler;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        StringBuilder readMessage = new StringBuilder();

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                String read = new String(buffer, 0, bytes);
                readMessage.append(read);

                if (read.contains("\n")) {
                    String msg = readMessage.toString();
                    String[] split = msg.split("\n");
                    mHandler.obtainMessage(Utility.MESSAGE_READ, -1, -1, split[0])
                            .sendToTarget();
                    readMessage.setLength(0);
                    if ( split.length > 1 ){
                        readMessage.append(split[1]);
                    }
                }

            } catch (IOException e) {
                Log.e(this.getName(), "Connection Lost", e);
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
