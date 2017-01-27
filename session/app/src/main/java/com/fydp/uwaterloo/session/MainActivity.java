package com.fydp.uwaterloo.session;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fydp.uwaterloo.session.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.FutureTask;

public class MainActivity extends AppCompatActivity {
    int numOfBtns = 5;
    Button[] btns = new Button[numOfBtns];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btns[0] = (Button) findViewById(R.id.button0);
        btns[1] = (Button) findViewById(R.id.button1);
        btns[2] = (Button) findViewById(R.id.button2);
        btns[3] = (Button) findViewById(R.id.button3);
        btns[4] = (Button) findViewById(R.id.button4);

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
    }

    void button0() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/info");
    }

    void button1() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart");
    }
    boolean toggle = true;
    void button2() {
        if(toggle) {
            new GetRequest().execute("http://10.5.5.9/gp/gpControl/command/shutter?p=1");
        }else{
            new GetRequest().execute("http://10.5.5.9/gp/gpControl/command/shutter?p=0");
        }
        toggle = !toggle;
    }

    void button3() {
        new GetRequest().execute("http://10.5.5.9/gp/gpControl/status");
    }

    void button4() {
        Toast.makeText(this,
                "btn4", Toast.LENGTH_LONG).show();
    }

    private void msg(String msg) {
        Toast.makeText(MainActivity.this,msg, Toast.LENGTH_LONG).show();
    }

    class GetRequest extends AsyncTask<String, Void, Void>{
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
}
