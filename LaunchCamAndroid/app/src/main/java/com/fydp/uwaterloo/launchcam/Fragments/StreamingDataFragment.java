package com.fydp.uwaterloo.launchcam.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
import java.util.Arrays;

/**
 * Created by Said Afifi on 15-Jul-16.
 */
public class StreamingDataFragment  extends Fragment {
    private static final int HISTORY_SIZE = 300;            // number of points to plot in history

    private XYPlot aprHistoryPlot = null;

    private CheckBox hwAcceleratedCb;
    private CheckBox showFpsCb;

    private SimpleXYSeries altitude;
    private SimpleXYSeries altitudeHistory = null;

    private Redrawer redrawer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.real_time_graph, container, false);


        altitude = new SimpleXYSeries("A");


        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);

        altitudeHistory = new SimpleXYSeries("Az.");
        altitudeHistory.useImplicitXVals();

        aprHistoryPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);
        aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        aprHistoryPlot.addSeries(altitudeHistory,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 200), null, null, null));

        aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        aprHistoryPlot.setDomainStepValue(HISTORY_SIZE/10);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Sample Index");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Angle (Degs)");
        aprHistoryPlot.getRangeLabelWidget().pack();

        aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
        aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

        // setup checkboxes:
        hwAcceleratedCb = (CheckBox) rootView.findViewById(R.id.hwAccelerationCb);
        final PlotStatistics levelStats = new PlotStatistics(1000, false);
        final PlotStatistics histStats = new PlotStatistics(1000, false);

        aprHistoryPlot.addListener(histStats);

        showFpsCb = (CheckBox) rootView.findViewById(R.id.showFpsCb);
        showFpsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                levelStats.setAnnotatePlotEnabled(b);
                histStats.setAnnotatePlotEnabled(b);
            }
        });


        //TODO: replace with bluetooth data
//        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
//        for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION)) {
//            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
//                orSensor = sensor;
//            }
//        }

        // if we can't access the orientation sensor then exit:
//        if (orSensor == null) {
//            System.out.println("Failed to attach to orSensor.");
//            cleanup();
//        }
//
//        sensorMgr.registerListener(this, orSensor, SensorManager.SENSOR_DELAY_UI);

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

    private void cleanup() {
        // aunregister with the orientation sensor before exiting:
//        sensorMgr.unregisterListener(this);
//        finish();
    }


    // Called whenever a new orSensor reading is taken.
//    @Override
//    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
//
//        // update level data:
//        altitude.setModel(Arrays.asList(
//                new Number[]{sensorEvent.values[0]}),
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
//
//        pLvlSeries.setModel(Arrays.asList(
//                new Number[]{sensorEvent.values[1]}),
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
//
//        rLvlSeries.setModel(Arrays.asList(
//                new Number[]{sensorEvent.values[2]}),
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
//
//        // get rid the oldest sample in history:
//        if (rollHistorySeries.size() > HISTORY_SIZE) {
//            rollHistorySeries.removeFirst();
//            pitchHistorySeries.removeFirst();
//            altitudeHistory.removeFirst();
//        }
//
//        // add the latest history sample:
//        altitudeHistory.addLast(null, sensorEvent.values[0]);
//        pitchHistorySeries.addLast(null, sensorEvent.values[1]);
//        rollHistorySeries.addLast(null, sensorEvent.values[2]);
//    }

}
