package com.fydp.uwaterloo.launchcam;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 3/11/17.
 */

public class GetVideoMetaData extends AsyncTask<String, Void, Void> {
    public AsyncResponse delegate = null;
    String toPrint = "";

    public GetVideoMetaData(AsyncResponse delegate){
        this.delegate = delegate;
    }
    @Override
    protected Void doInBackground(String... params) {
        URL u;
        HttpURLConnection conn;
        try {
            u = new URL(params[0]);
            conn = (HttpURLConnection)u.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if(conn.getResponseCode() != 200){
                Log.e("GetVideoMetaData", "failed cuz request code: "+conn.getResponseCode() );
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
        List<String> videoFileNames = new ArrayList<>();
        List<String> pictureFileNames = new ArrayList<>();

        super.onPostExecute(aVoid);
        try {
            JSONObject obj = new JSONObject(toPrint);
            System.out.println(obj);
            JSONArray jsonArray = obj.getJSONArray("media");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                if("112GOPRO".equals(row.getString("d"))){
                    JSONArray fsArray = row.getJSONArray("fs");
                    for(int j = fsArray.length() - 1; j >= 0; j--){
                        if(videoFileNames.size() >= 20){
                            break;
                        }
                        JSONObject o = fsArray.getJSONObject(j);
                        if(o.getString("n").contains("MP4")){
                            videoFileNames.add(o.getString("n").replace("MP4", "LRV"));
                        }
                        if(o.getString("n").contains("JPG")){
                            videoFileNames.add(o.getString("n"));
                        }
                    }
                }
            }
        } catch (JSONException e) {
        }

        delegate.processFinish(videoFileNames, pictureFileNames);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

}