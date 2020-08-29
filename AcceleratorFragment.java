package com.example.mycompass;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

public class AcceleratorFragment extends Fragment {
    private SensorManager mSensorManager;
    private Sensor mAccelerator;
    private Queue<MovementData> queue = new ArrayDeque<>();
    private AcceleratorGraphView acceleratorGraphView;
    //final float TIME_GAP = 20000;

    private final SensorEventListener mAcceleratorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
//            queue.add(new MovementData(event.values,event.timestamp));
//            if((event.timestamp-queue.peek().getTime()) > TIME_GAP){
//                queue.remove();
//            }

            long timeInMillis = (new Date()).getTime()
                    + (event.timestamp - System.nanoTime()) / 1000000;
            acceleratorGraphView.add(new MovementData(event.values,System.currentTimeMillis()));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    };

    public void onCreate(Bundle savedInstanceState) {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerator = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        super.onCreate(savedInstanceState);
    }

    public void onStart() {
        acceleratorGraphView = getActivity().findViewById(R.id.acceleratorFragment_xml);
        super.onStart();
    }

    @Override
    public void onResume() {

        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean fastest = prefs.getBoolean("PREF_DELAY_FASTEST", true);


        if(fastest){
            mSensorManager.registerListener(mAcceleratorListener, mAccelerator,SensorManager.SENSOR_DELAY_FASTEST);
        }else {
            mSensorManager.registerListener(mAcceleratorListener, mAccelerator,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //---Inflate the layout for this fragment---
        return inflater.inflate(R.layout.accelerator_fragment, container, false);
    }
}
