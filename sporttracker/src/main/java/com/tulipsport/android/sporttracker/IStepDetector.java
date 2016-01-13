package com.tulipsport.android.sporttracker;
/*
 * @description
 *   Please write the IStepDetecor module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(12/31/2015)
 */

public interface IStepDetector {

    void start();

    void reset();

    void stop();

    int getSteps();
}
