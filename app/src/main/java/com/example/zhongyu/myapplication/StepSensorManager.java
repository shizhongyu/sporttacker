package com.example.zhongyu.myapplication;

/**
 * Created by zhongyu on 12/3/2015.
 */

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StepSensorManager implements StepListener, IStepDetector {
    static final int STEP_DETECTOR_MODE_SYSTEM = 1;
    static final int STEP_DETECTOR_MODE_MOCK = 1 << 1;
    static final int STEP_DETECTOR_MODE_AUTO = STEP_DETECTOR_MODE_SYSTEM | STEP_DETECTOR_MODE_MOCK;

    IStepDetector stepDetector;
    private List<StepListener> stepListeners = new ArrayList<>();

    public StepSensorManager(Context context) {
        this(context, STEP_DETECTOR_MODE_AUTO);
    }

    public StepSensorManager(Context context, int mode) {
        int supportMode = STEP_DETECTOR_MODE_MOCK;
        if (isKitkatWithStepSensor(context)) {
            supportMode |= STEP_DETECTOR_MODE_SYSTEM;
        }

        if ((supportMode & mode) == 0) {
            mode = STEP_DETECTOR_MODE_MOCK;
        }

        switch (mode) {
            case STEP_DETECTOR_MODE_SYSTEM:
                stepDetector = new SystemStepDetector(context, this);
                break;
            case STEP_DETECTOR_MODE_MOCK:
                stepDetector = new MockStepDetector(context, this);
                break;
        }
    }

    @Override
    public void start() {
        stepDetector.start();
    }

    @Override
    public void stop() {
        stepDetector.stop();
    }

    @Override
    public void reset() {
        stepDetector.reset();
    }

    @Override
    public int getSteps() {
        return stepDetector.getSteps();
    }

    /**
     * add step listener
     *
     * @param stepListeners
     */
    public void addStepListener(StepListener... stepListeners) {
        this.stepListeners.addAll(Arrays.asList(stepListeners));
    }

    @Override
    public void onStep(int stepCount) {
        if (stepListeners != null) {
            int count = stepListeners.size();
            for (int i = 0; i < count; i++) {
                stepListeners.get(i).onStep(stepCount);
            }
        }
    }

    /**
     * Returns true if this device is supported. It needs to be running Android KitKat (4.4) or
     * higher and has a step counter and step detector sensor.
     * This check is useful when an app provides an alternative implementation or different
     * functionality if the step sensors are not available or this code runs on a platform version
     * below Android KitKat. If this functionality is required, then the minSDK parameter should
     * be specified appropriately in the AndroidManifest.
     *
     * @return True iff the device can run this sample
     */
    public static boolean isKitkatWithStepSensor(Context cxt) {

        // Require at least Android KitKat
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        PackageManager packageManager = cxt.getPackageManager();
        return currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }
}
