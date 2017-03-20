package com.fydp.uwaterloo.launchcam.AsyncTasks;

import android.os.AsyncTask;
import android.widget.Button;

import com.fydp.uwaterloo.launchcam.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Said Afifi on 12-Mar-17.
 */

public class ConnectRequest extends AsyncTask<String, Void, Void> {
    String toPrint = "";
    private Button buttonColor;

    @Override
    protected Void doInBackground(String... params) {
        int port = 9;

        String goPro_IP = "10.5.5.9";
        String data = "FFFFFFFFFFFFf6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397f6dd9e2d1397";

        byte[] dataToSend= new byte[data.length()/2];
        for(int i = 0; i < data.length(); i+=2){
            String hex = "0x" + data.substring(i, i+2);
            int numba = Long.decode(hex).intValue();
            dataToSend[i/2]=(byte)numba;
            System.out.println(dataToSend[i/2]);
        }


        System.out.println("----------");


        System.out.println("Connecting to " + goPro_IP + " on port " + port);
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(goPro_IP);
            if (addr.isReachable(1000))
                System.out.println("host is reachable");
            else
                System.out.println("host is not reachable");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DatagramPacket out = new DatagramPacket(dataToSend, dataToSend.length, addr, 9);

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            socket.setSendBufferSize(dataToSend.length);
            socket.send(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("finished async");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.buttonColor.setBackgroundResource(R.drawable.ic_power_settings_new_black_48dp_on);

    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

    public void setButtonColor(Button buttonColor) {
        this.buttonColor = buttonColor;
    }
}
