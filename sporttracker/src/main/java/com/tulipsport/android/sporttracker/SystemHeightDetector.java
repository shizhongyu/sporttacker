package com.tulipsport.android.sporttracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by zhongyu on 1/27/2016.
 */
public class SystemHeightDetector implements SensorEventListener, IHeightDetector {
    private static final String TAG = "SystemHeightDetector";

    SensorManager sensorManager = null;
    Sensor sensor;
    HeightListener heightListener;
    int height = 0;

    public SystemHeightDetector(Context context, HeightListener heightListener) {
        this.heightListener = heightListener;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public void start() {
        final boolean batchMode = sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (!batchMode) {
            Log.w(TAG, "Could not register sensor listener in batch mode, " +
                    "falling back to continuous mode.");
        }
    }

    @Override
    public void reset() {
        stop();
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public int getHeight() {
        return height;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            height = (int) event.values[0];
            if(heightListener != null) {
                heightListener.onHeight(height);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
