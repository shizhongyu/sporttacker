package com.tulipsport.android.sporttracker;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhongyu on 1/27/2016.
 */
public class HeightSensorManager implements HeightListener, IHeightDetector {

    public static final int HEIGHT_DETECTOR_MODE_SYSTEM = 1;
    public static final int HEIGHT_DETECTOR_MODE_GPS = 1 << 1;
    public static final int HEIGHT_DETECTOR_MODE_AUTO = HEIGHT_DETECTOR_MODE_GPS | HEIGHT_DETECTOR_MODE_SYSTEM;

    private static final String TAG = "HeightSensorManager";
    IHeightDetector heightDetector;
    private List<HeightListener> heightListeners = new ArrayList<>();

    public HeightSensorManager(Context context) {
        this(context, HEIGHT_DETECTOR_MODE_AUTO);
    }

    public HeightSensorManager(Context context, int mode) {
        int supportMode = HEIGHT_DETECTOR_MODE_GPS;

        supportMode |= HEIGHT_DETECTOR_MODE_SYSTEM;

        if ((supportMode & mode) == 0) {
            mode = HEIGHT_DETECTOR_MODE_GPS;
        }

        switch (mode) {
            case HEIGHT_DETECTOR_MODE_SYSTEM:
                heightDetector = new SystemHeightDetector(context, this);
                break;
            case HEIGHT_DETECTOR_MODE_GPS:
                heightDetector = new GpsHeightDetector(context, this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onHeight(int height) {
        if(heightListeners != null) {
            int count = heightListeners.size();
            for (int i = 0; i < count; i++) {
                heightListeners.get(i).onHeight(height);
            }
        }
     }

    @Override
    public void start() {
        heightDetector.start();
    }

    @Override
    public void reset() {
        heightDetector.reset();
    }

    @Override
    public void stop() {
        heightDetector.stop();
    }

    @Override
    public int getHeight() {
        return heightDetector.getHeight();
    }

    public void addHeightListener(HeightListener... heightListeners) {
        this.heightListeners.addAll(Arrays.asList(heightListeners));
    }


}




















