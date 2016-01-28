package com.tulipsport.android.sporttracker;

/**
 * Created by zhongyu on 1/27/2016.
 */
public interface IHeightDetector {

    void start();

    void reset();

    void stop();

    int getHeight();
}
