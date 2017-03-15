package com.fydp.uwaterloo.launchcam.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.fydp.uwaterloo.launchcam.MainActivity;
import com.fydp.uwaterloo.launchcam.R;
import com.fydp.uwaterloo.launchcam.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class StreamingDataFragment extends Fragment {
    private static final int HISTORY_SIZE = 400;            // number of points to plot in history
    private static final String BLUETOOTH_STR_LOG = "l";

    private ArrayList<String> currentData;

    private ArrayAdapter<String> fileListAdapter;
    private ArrayList<String> fileNames;
    private XYPlot aprHistoryPlot = null;
    private Spinner graphToDraw;

    private SimpleXYSeries altitude;
    private SimpleXYSeries altitudeHistory = null;

    private Redrawer redrawer;
    private String FILE_NAMES = "FILE_NAMES";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.real_time_graph, container, false);

        altitude = new SimpleXYSeries("A");

        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);
        Button getDataBtn = (Button) rootView.findViewById(R.id.btn_getData);
        Button storeBtn = (Button) rootView.findViewById(R.id.btn_store);
        Button clearGraphBtn = (Button) rootView.findViewById(R.id.btn_clearGraph);
        graphToDraw = (Spinner) rootView.findViewById(R.id.sp_graphSpinner);
        ListView fileList = (ListView) rootView.findViewById(R.id.lv_fileList);
        currentData = new ArrayList<>();
        loadFileNames();
        fileListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, fileNames);
        fileList.setAdapter(fileListAdapter);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.graph_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        graphToDraw.setAdapter(adapter);
        graphToDraw.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                altitudeHistory.setTitle(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getDataBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String tmp = BLUETOOTH_STR_LOG;
                if (((MainActivity) getActivity()).connectedThread != null)
                    ((MainActivity) getActivity()).connectedThread.write(tmp.getBytes());
                else {
                    Utility.toast("No Bluetooth Connection", getActivity());
                }
            }
        });
        storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeCurrentData();
            }
        });
        clearGraphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
            }
        });

        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                fileLongPress((TextView) view, position);
                return false;
            }
        });
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fileClick(((TextView) view).getText().toString());
            }
        });

        altitudeHistory = new SimpleXYSeries("Raw");
        altitudeHistory.useImplicitXVals();

        aprHistoryPlot.setRangeBoundaries(900, 1050, BoundaryMode.AUTO);
        aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.AUTO);
        aprHistoryPlot.addSeries(altitudeHistory,
                new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));

        aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        aprHistoryPlot.setDomainStepValue(HISTORY_SIZE / 10);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Cycles (seconds * 100)");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Height Feet");
        aprHistoryPlot.getRangeLabelWidget().pack();

        aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
        aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

        final PlotStatistics histStats = new PlotStatistics(1000, false);

        aprHistoryPlot.addListener(histStats);

        redrawer = new Redrawer(
                Arrays.asList(new Plot[]{aprHistoryPlot}),
                100, false);

        return rootView;
    }

    private void clearData() {
        int size = altitudeHistory.size();
        for (int i = 0; i < size; i++) {
            altitudeHistory.removeFirst();
        }
        currentData.clear();
    }

    int currentLogNum = 0;

    private void fileClick(final String fileName) {
        clearData();
        ++currentLogNum;
        final int threadCount = currentLogNum;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File path = getActivity().getExternalFilesDir(null);
                File file = new File(path, fileName);
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (currentLogNum != threadCount) break;
                        SystemClock.sleep(10);
                        final String finalLine = line;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                handleDataToDraw(finalLine);
                            }
                        });
                    }
                    br.close();
                } catch (IOException e) {
                    Log.e("StreamingData", "Read File: ", e);
                }
            }
        }).start();
    }

    private void fileLongPress(TextView view, int position) {
        String fileName = view.getText().toString();
        try {
            File path = getActivity().getExternalFilesDir(null);
            File file = new File(path, fileName);
            if (file.delete()) {
                Log.d("StreamingData", "File was deleted");
            } else {
                Log.e("StreamingData", "File was not deleted");
            }
        } catch (Exception e) {
            Log.e("StreamingData", "onItemLongClick: ", e);
        }
        fileNames.remove(position);
        fileListAdapter.notifyDataSetChanged();
    }

    private void loadFileNames() {
        fileNames = new ArrayList<>();
        File path = getActivity().getExternalFilesDir(null);
        for (File file : path.listFiles()) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
    }

    private void storeCurrentData() {
        final String[] clone = new String[currentData.size()];
        for (int i = 0; i < clone.length; i++) {
            clone[i] = currentData.get(i);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = "";
                try {
                    fileName = DateFormat.getDateTimeInstance().format(new Date()) + ".log";
                    File path = getActivity().getExternalFilesDir(null);
                    File file = new File(path, fileName);
                    FileOutputStream stream = new FileOutputStream(file);

                    for (String line : clone) {
                        stream.write(line.getBytes());
                        stream.write("\n".getBytes());
                    }
                    stream.close();
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
                final String finalFileName = fileName;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (!finalFileName.equals("")) {
                            fileNames.add(0, finalFileName);
                            fileListAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        redrawer.start();
    }

    @Override
    public void onPause() {
        redrawer.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        redrawer.finish();
        currentData.clear();
        super.onDestroy();
    }

    // Called whenever a new orSensor reading is taken.

    public void handleDataToDraw(String readBuf) {
        try {
            currentData.add(readBuf);
            float value = parseData(readBuf, graphToDraw.getSelectedItemPosition());
            // use the data appropriately
            draw(value);
        } catch (Exception e) {
            Utility.toast("Incorrect input", getActivity());
        }
    }

    float last = 0;

    private float parseData(String readBuf, int graphNum) {
        try {
            String[] split = readBuf.split(",");
            last = Float.parseFloat(split[graphNum]);
            return last;
        } catch (Exception e) {
            Log.d(Utility.tag, "parseData: Failed to parse");
        }
        return last;
    }

    public void draw(float value) {
        // get rid the oldest sample in history:
        if (value > 2000) value = 2000;
        if (value < -5) value = -5;

        if (altitudeHistory.size() > HISTORY_SIZE) {
            altitudeHistory.removeFirst();
        }

        // add the latest history sample:
        altitudeHistory.addLast(null, value);

        // update level data:
        altitude.setModel(Arrays.asList(value), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    }

}
