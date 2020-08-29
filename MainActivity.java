package com.example.mycompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private CompassView mCompassView;
    private SensorManager mSensorManager;
    private int mScreenRotation;
    private float[] mNewestValues;
    private Sensor rotationVector;

    private Sensor mWalkSensor;
    private boolean isSensorPresent;

    private TextView walkCount;

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            mNewestValues = calculateOrientation(sensorEvent.values);
            mCompassView.refresh(mNewestValues[0],mNewestValues[1],mNewestValues[2]);
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private final SensorEventListener mWalkCountListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mCompassView.refreshWalkCount(event.values[0]);
            //walkCount.setText(String.valueOf((int)event.values[0]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setApllicale("en");
        setLanguage();
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_main_horizon);
        }else{
            setContentView(R.layout.activity_main);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        walkCount = findViewById(R.id.walkCount_int);
        mCompassView = findViewById(R.id.compassView);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreenRotation = display.getRotation();
        mNewestValues = new float[] {0, 0, 0};
        rotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mWalkSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        }else{
            isSensorPresent = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_setting:
                Intent intent = new Intent(this, PreferencesActivity.class);
                ////startActivity(intent);
                startActivityForResult(intent, 0);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private void setApllicale(String localCode){
//        Resources res = getResources();
//        DisplayMetrics dm = res.getDisplayMetrics();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
//
//            res.getConfiguration().setLocale(new Locale(localCode.toLowerCase()));
//        }else{
//            res.getConfiguration().locale = new Locale(localCode.toLowerCase());
//        }
//        res.updateConfiguration(res.getConfiguration(), dm);
//    }

    private void setLanguage(){
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        int languageIndex = Integer.parseInt(prefs.getString("SWITCH_LANGUAGES", "0"));

        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale locale;
        if(languageIndex == 0) {
            locale = Locale.CHINESE;

        }
        else {
            locale = Locale.ENGLISH;
        }
        Locale.setDefault(locale);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new LocaleList());
            createConfigurationContext(config);
            resources.updateConfiguration(config, dm);
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(locale);
            resources.updateConfiguration(config, dm);
        }
        else {
            config.locale = locale;
            resources.updateConfiguration(config, dm);
        }
        //getBaseContext().getResources().updateConfiguration(config, dm);
    }


    private float[] calculateOrientation(float[] values) {
        float[] rotationMatrix = new float[9];
        float[] remappedMatrix = new float[9];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values);
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;
        switch (mScreenRotation) {
            case (Surface.ROTATION_90):
                x_axis = SensorManager.AXIS_Y;
                y_axis = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                y_axis = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                x_axis = SensorManager.AXIS_MINUS_Y;
                y_axis = SensorManager.AXIS_X;
                break;
            default: break;
        }
        SensorManager.remapCoordinateSystem(rotationMatrix, x_axis, y_axis, remappedMatrix);
        SensorManager.getOrientation(remappedMatrix, orientation);
        values[0] = (float) Math.toDegrees(orientation[0]);
        values[1] = (float) Math.toDegrees(orientation[1]);
        values[2] = (float) Math.toDegrees(orientation[2]);
        return values;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fastest = prefs.getBoolean("PREF_DELAY_FASTEST", true);

        //setApllicale("en");
        if(fastest){
            mSensorManager.registerListener(mSensorEventListener, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mWalkCountListener,mWalkSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }else {
            mSensorManager.registerListener(mSensorEventListener, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mWalkCountListener,mWalkSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
        if(isSensorPresent) {
            mSensorManager.unregisterListener(mWalkCountListener);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        LinearLayout tmp = findViewById(R.id.fragmenntMain);
        if(newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE){
            tmp.setOrientation(LinearLayout.HORIZONTAL);
        }else{
            tmp.setOrientation(LinearLayout.VERTICAL);
        }
        super.onConfigurationChanged(newConfig);
    }

}
