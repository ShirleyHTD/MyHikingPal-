package com.example.mycompass;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import static androidx.core.content.ContextCompat.getSystemService;


public class Fragment1 extends Fragment {
    private Sensor mWalkSensor;
    private Sensor mAttitudeSensor;
    private SensorManager mSensorManager;

    private boolean isSensorPresent;
    private Button resetButton;
    private TextView walkCount;
    private Button mapButton;

    SharedPreferences shp;
    private TextView attitudeNumber;
    private int unit = 0;

    SharedPreferences sharedPreferences_counter;
    SharedPreferences.Editor editor;

    private int stepCounterBeforeReset = 0;

    private final SensorEventListener mWalkCountListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            walkCount.setText(String.format("%.0f",(event.values[0]-stepCounterBeforeReset)));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    };

    private final SensorEventListener mAttitudeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            shp = PreferenceManager.getDefaultSharedPreferences(getContext());
            //String unit_string = shp.getString("SWITCH_UNIT", "0");
            unit = Integer.parseInt(shp.getString("SWITCH_UNIT", "0"));
            //String unit_string = shp.getString("SWITCH_LANGUAGES","0");

            float a = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,event.values[0]);

            if(unit==0){
                //String test = getActivity().getString(R.string.attitude_meter);
                attitudeNumber.setText(String.format("%.2f",a) + getActivity().getResources().getString(R.string.attitude_meter));
            }else{
                a = a * (float)3.28;
                attitudeNumber.setText(String.format("%.2f",a) + getActivity().getResources().getString(R.string.attitude_feet));
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //---Inflate the layout for this fragment---
        return inflater.inflate(R.layout.fragment1, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences_counter = getActivity().getSharedPreferences("data",Context.MODE_PRIVATE);
        editor = sharedPreferences_counter.edit();
        stepCounterBeforeReset = sharedPreferences_counter.getInt("stepCounterBeforeReset",0);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        mAttitudeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mWalkSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {

            isSensorPresent = true;
        }else{
            isSensorPresent = false;
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        attitudeNumber = getActivity().findViewById(R.id.Attitude_int);


        walkCount = getActivity().findViewById(R.id.walkCount_int);
        resetButton = getActivity().findViewById(R.id.reset);
        resetButton.setOnClickListener(mButtonListener);

        mapButton = getActivity().findViewById(R.id.map);
        mapButton.setOnClickListener(mMapButtonListener);

    }

    @Override
    public void onResume() {

        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean fastest = prefs.getBoolean("PREF_DELAY_FASTEST", true);


        if(fastest){
            //mSensorManager.registerListener(mSensorEventListener, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mWalkCountListener,mWalkSensor,SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mAttitudeListener, mAttitudeSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }else {
            //mSensorManager.registerListener(mSensorEventListener, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mWalkCountListener,mWalkSensor,SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mAttitudeListener, mAttitudeSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }

        //mSensorManager.registerListener(mWalkCountListener,mWalkSensor,SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onPause() {
        super.onPause();

        if(isSensorPresent) {
            mSensorManager.unregisterListener(mWalkCountListener);
            mSensorManager.unregisterListener(mAttitudeListener);
        }

    }

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stepCounterBeforeReset += Integer.parseInt(walkCount.getText().toString());
            editor.putInt("stepCounterBeforeReset", stepCounterBeforeReset);
            editor.commit();
            walkCount.setText("0");
        }
    };

    private View.OnClickListener mMapButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
//            Intent showMap = new Intent("com.example.mycompass.MapsActivity");
//            startActivity(showMap);
        }
    };




}