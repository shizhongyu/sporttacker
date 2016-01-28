package com.tulipsport.android.sporttracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by zhongyu on 1/28/2016.
 */
public class GpsHeightDetector implements SensorEventListener, IHeightDetector, HeightListener {
    private static final String TAG = "GpsHeightDetector";
    Context context;
    private HeightListener heightListener;
    Sensor sensor;
    SensorManager sensorManager;

    public GpsHeightDetector(Context context, HeightListener heightListener) {
        this.context = context;
        this.heightListener = heightListener;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        sensor = sensorManager.getDefaultSensor(Sensor.Type)
    }


    @Override
    public void start() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void stop() {

    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onHeight(int height) {

    }
}
