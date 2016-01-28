package com.tulipsport.android.sporttracker;
/*
 * @description
 *   Please write the MockStepDetector module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(12/31/2015)
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class MockStepDetector implements IStepDetector, SensorEventListener, StepListener {

    static final String TAG = "MockStepDetector";

    Context context;
    StepListener stepListener;
    AccelerometerSensorManager accelerometerSensorManager;
    IMockStepRecognizer mockStepRecognizer;

    int stepCount = 0;

    public MockStepDetector(Context context, StepListener stepListener) {
        this.context = context;
        this.stepListener = stepListener;
        this.accelerometerSensorManager = AccelerometerSensorManager.singleTon(context);
        this.mockStepRecognizer = new MockStepDetectorV2(this);
    }

    @Override
    public void start() {
        accelerometerSensorManager.addListener(this);
    }

    @Override
    public void reset() {
        this.stepCount = 0;
        mockStepRecognizer.reset();
    }

    @Override
    public void stop() {
        accelerometerSensorManager.removeListener(this);
    }

    @Override
    public int getSteps() {
        return this.stepCount;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mockStepRecognizer != null) {
            mockStepRecognizer.onSensorChanged(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mockStepRecognizer != null) {
            mockStepRecognizer.onAccuracyChanged(sensor, accuracy);
        }
    }

    @Override
    public void onStep(int stepCount) {
        if (stepListener != null) {
            stepListener.onStep(stepCount);
        }

        this.stepCount += stepCount;
    }
}
