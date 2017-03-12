package com.fydp.uwaterloo.launchcam.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fydp.uwaterloo.launchcam.AsyncResponse;
import com.fydp.uwaterloo.launchcam.GetVideoMetaData;
import com.fydp.uwaterloo.launchcam.MainActivity;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;


/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class BluetoothFragment extends Fragment {
    int numOfBtns = 7;
    Button[] btns = new Button[numOfBtns];
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.bluetooth_fragment, container, false);
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
//        return rootView;

        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.bluetooth_fragment, container, false);

        btns[0] = (Button) rootView.findViewById(R.id.button0);
        btns[1] = (Button) rootView.findViewById(R.id.button1);
        btns[2] = (Button) rootView.findViewById(R.id.button2);
        btns[3] = (Button) rootView.findViewById(R.id.button3);
        btns[4] = (Button) rootView.findViewById(R.id.button4);
        btns[5] = (Button) rootView.findViewById(R.id.button5);
        btns[6] = (Button) rootView.findViewById(R.id.button6);

        btns[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button0();
            }
        });
        btns[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button1();
            }
        });
        btns[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button2();
            }
        });
        btns[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button3();
            }
        });
        btns[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button4();
            }
        });
        btns[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button5();
            }
        });
        btns[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    button6();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return rootView;

    }

    void button0() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/info");
    }

    void button1() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart");
    }
    boolean toggle = true;
    void button2() {
//        if(toggle) {
//            new GetRequest().execute("http://10.5.5.9/gp/gpControl/command/shutter?p=1");
//        }else{
//            new GetRequest().execute("http://10.5.5.9/gp/gpControl/command/shutter?p=0");
//        }
//        toggle = !toggle;
//
//        VideoView mVideoView = (VideoView) findViewById(R.id.videoStream);
//
//        mVideoView.setBufferSize(1);
//        String path = "http://je7.bugaboo.tv/liveedgech7_partner/720p/playlist.m3u8";
//        mVideoView.setVideoPath(path);
//        mVideoView.setMediaController(new io.vov.vitamio.widget.MediaController(this));
//        mVideoView.requestFocus();
//        mVideoView.start();

    }

    void button3() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/status");
    }

    //get video metadata
    void button4() {
//        new GetVideoMetaData().execute("http://10.5.5.9:8080/gp/gpMediaList");
    }

    /**
     * Power Off the GoPro
     */
    void button5() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/command/system/sleep");
    }

    /**
     * Power On the GoPro
     */
    void button6() throws Exception {

        new ConnectRequest().execute();
    }


    class GetRequest extends AsyncTask<String, Void, Void> {
        String toPrint = "";
        @Override
        protected Void doInBackground(String... params) {
            URL u;
            HttpURLConnection conn;
            try {
                u = new URL(params[0]);
                conn = (HttpURLConnection)u.openConnection();
                System.out.println("test");
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if(conn.getResponseCode() != 200){
                    System.out.println("failed cuz request code: " + conn.getResponseCode());
                }
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                while((output=br.readLine()) != null){
                    toPrint+=output+"\n";
                }

                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                JSONObject obj = new JSONObject(toPrint);
                String value = obj.getJSONObject("status").getString("31");
                msg(value);
            } catch (JSONException e) {
                msg(toPrint);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    class ConnectRequest extends AsyncTask<String, Void, Void>{
        String toPrint = "";
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
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    private void msg(String msg) {
        Toast.makeText(getActivity(),msg, Toast.LENGTH_LONG).show();
    }



}
