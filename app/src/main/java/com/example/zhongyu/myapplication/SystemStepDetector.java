package com.example.zhongyu.myapplication;

/**
 * Created by zhongyu on 12/3/2015.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SystemStepDetector implements SensorEventListener, IStepDetector {

    static final String TAG = "SystemStepDetector";

    // max batch latency is specified in microseconds
    private static final int BATCH_LATENCY_0 = 0; // no batching
    private static final int BATCH_LATENCY_10s = 10000000;
    private static final int BATCH_LATENCY_5s = 5000000;

    SensorManager sensorManager;
    Sensor sensor;
    StepListener stepListener;
    int stepCount = 0;
    boolean running = false;

    public SystemStepDetector(Context context, StepListener stepListener) {
        this.stepListener = stepListener;

        // Get the default sensor for the sensor type from the SenorManager
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // A step detector event is received for each step.
            // This means we need to count steps ourselves

            int count = event.values.length;
            stepCount += count;
            if (stepListener != null) {
                stepListener.onStep(count);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void start() {
        if (running) return;

        running = true;
        // Register the listener for this sensor in batch mode.
        // If the max delay is 0, events will be delivered in continuous mode without batching.
        final boolean batchMode = sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_0);

        if (!batchMode) {
            Log.w(TAG, "Could not register sensor listener in batch mode, " +
                    "falling back to continuous mode.");
        }
    }

    @Override
    public void reset() {
        if (running) {
            stop();
        }

        stepCount = 0;
    }

    @Override
    public void stop() {
        if (!running) return;

        running = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public int getSteps() {
        return stepCount;
    }
}
