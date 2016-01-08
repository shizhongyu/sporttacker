package com.example.zhongyu.myapplication;
/*
 * @description
 *   Please write the AccelerometerSensorManager module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(12/30/2015)
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tulipsport.android.common.logger.Log;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerSensorManager implements SensorEventListener {

    public static final String TAG = "AccelerometerSensorMgr";
    static AccelerometerSensorManager _instance;

    public static AccelerometerSensorManager singleTon(Context context) {
        if (_instance == null) {
            synchronized (AccelerometerSensorManager.class) {
                if (_instance == null) {
                    _instance = new AccelerometerSensorManager(context);
                }
            }
        }
        return _instance;
    }

    Context context;
    SensorManager sensorManager;
    List<SensorEventListener> listeners;
    boolean running = false;

    public AccelerometerSensorManager(Context context) {
        this.context = context;
        // Get the default sensor for the sensor type from the SenorManager
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        listeners = new ArrayList<>();
    }

    private void start() {
        if (running) return;

        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mSensor != null) {
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            running = true;
        } else {
            Log.e(TAG, "This system don't has the accelerometer sensor!");
        }
    }

    private void stop() {
        if (running) {
            sensorManager.unregisterListener(this);
            running = false;
        }
    }

    public void addListener(SensorEventListener sensorEventListener) {
        if (!listeners.contains(sensorEventListener)) {
            listeners.add(sensorEventListener);

            if (listeners.size() == 1) {
                start();
            }
        } else {
            Log.w(TAG, "The sensor event listener has already been added!");
        }
    }

    public void removeListener(SensorEventListener sensorEventListener) {
        if (!listeners.remove(sensorEventListener)) {
            Log.w(TAG, "The sensor event listener is not in the list!");
        } else {
            if (listeners.size() == 0) {
                stop();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, event.values[0] + "," + event.values[1] + "," + event.values[2]);

        for (SensorEventListener listener : listeners) {
            try {
                listener.onSensorChanged(event);
            } catch (Exception ignore) {
                Log.e(TAG, "onSensorChanged Error", ignore);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        for (SensorEventListener listener : listeners) {
            try {
                listener.onAccuracyChanged(sensor, accuracy);
            } catch (Exception ignore) {
                Log.e(TAG, "onAccuracyChanged Error", ignore);
            }
        }
    }
}
