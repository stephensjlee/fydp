package com.fydp.uwaterloo.launchcam.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.fydp.uwaterloo.launchcam.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class StreamingDataFragment extends Fragment {
    private static final int HISTORY_SIZE = 400;            // number of points to plot in history

    private XYPlot aprHistoryPlot = null;
    private TextView tv = null;

    private SimpleXYSeries altitude;
    private SimpleXYSeries altitudeHistory = null;

    private Redrawer redrawer;
    private ArrayList<Byte> packetToHandle;
    private static final byte S = 83;
    private static final byte P = 80;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.real_time_graph, container, false);


        altitude = new SimpleXYSeries("A");

        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);
        tv = (TextView) rootView.findViewById(R.id.statstv);


        altitudeHistory = new SimpleXYSeries("Az.");
        altitudeHistory.useImplicitXVals();

        aprHistoryPlot.setRangeBoundaries(900, 1050, BoundaryMode.AUTO);
        aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.AUTO);
        aprHistoryPlot.addSeries(altitudeHistory,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 200), null, null, null));

        aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        aprHistoryPlot.setDomainStepValue(HISTORY_SIZE / 10);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Sample Index");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Angle (Degs)");
        aprHistoryPlot.getRangeLabelWidget().pack();

        aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
        aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

        final PlotStatistics histStats = new PlotStatistics(1000, false);

        aprHistoryPlot.addListener(histStats);

        packetToHandle = new ArrayList<>();
        redrawer = new Redrawer(
                Arrays.asList(new Plot[]{aprHistoryPlot}),
                100, false);
        return rootView;
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
        super.onDestroy();
    }

    // Called whenever a new orSensor reading is taken.

    public void handleBTData(byte[] readBuf) {
        for (int i = 0; i < readBuf.length; i++) {
            if (readBuf[i] == S) {
                packetToHandle.clear();
            } else if (readBuf[i] == P) {
                processPacket();
            } else {
                packetToHandle.add(readBuf[i]);
            }
        }
    }

    private void processPacket() {
        try {
            // get checksum value
            byte[] checkSumBytes = {packetToHandle.get(packetToHandle.size() - 2),
                    packetToHandle.get(packetToHandle.size() - 1)};
            byte checksum = Byte.parseByte(new String(checkSumBytes), 16);

            // calculate checksum
            byte actualChecksum = 0;
            for (int i = 0; i < packetToHandle.size() - 2; i++) {
                actualChecksum = (byte)((actualChecksum & 0xFF) + (packetToHandle.get(i) & 0xFF));
            }

            // compare, then parse and render
            if (checksum == actualChecksum) {
                // parse
                float value = parseData();
                // use the data appropriately
                draw(value);
            }

        } catch (Exception ignored) {

        } finally {
            // clear processed packet
            packetToHandle.clear();
        }
    }

    private float parseData() throws Exception {

        List<Byte> msgBytes = packetToHandle.subList(0, packetToHandle.size() - 2);
        byte[] msgBytesPrimitive = new byte[msgBytes.size()];
        for (int i = 0; i < msgBytes.size(); i++) {
            msgBytesPrimitive[i] = msgBytes.get(i);
        }
        String string = new String(msgBytesPrimitive);
        return Float.parseFloat(string);
    }

    public void draw(float value) {
        //Log.d("data", ""+value);
        // get rid the oldest sample in history:
        if (altitudeHistory.size() > HISTORY_SIZE) {
            altitudeHistory.removeFirst();
        }

        // add the latest history sample:
        altitudeHistory.addLast(null, value);

        // update level data:
        altitude.setModel(Arrays.asList(value), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
//        tv.setText(String.valueOf(value));
    }

}
