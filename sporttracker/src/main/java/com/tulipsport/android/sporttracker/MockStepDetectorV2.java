package com.tulipsport.android.sporttracker;
/*
 * @description
 *   Please write the MockStepDectorV2 module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(12/30/2015)
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

public class MockStepDetectorV2 implements IMockStepRecognizer {
    static final String TAG = "MockStepDetectorV2";
    StepListener stepListener;
    int stepCount = 0;

    static {
        System.loadLibrary("sport-tracker");
    }

    public MockStepDetectorV2(StepListener stepListener) {
        this.stepListener = stepListener;

        init();
    }

    private void init() {
        stepCount = 0;
        resetStep();
    }

    @Override
    public void reset() {
        stepCount = 0;
        resetStep();
    }

    public native void resetStep();

    public native int pedometer(float x, float y, float z);

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            int count = pedometer(event.values[0], event.values[1], event.values[2]);

            if (count == 0) return;

            stepCount += count;

            if (stepListener != null) {
                stepListener.onStep(count);
            }
        } catch (Exception e) {
            Log.e("StepRecognizer", "error", e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
