package com.example.zhongyu.myapplication;
/*
 * @description
 *   Please write the IMockStepRecognizer module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(12/31/2015)
 */

import android.hardware.SensorEventListener;

public interface IMockStepRecognizer extends SensorEventListener {

    void reset();
}
