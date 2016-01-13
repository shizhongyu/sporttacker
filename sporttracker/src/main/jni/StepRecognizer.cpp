//
//  main.c
//  PedometerForAndroid1
//
//  Created by 于金萍 on 16/1/7.
//  Copyright © 2016年 于金萍. All rights reserved.
//

#include <stdio.h>

#include <stdlib.h>
#include <math.h>
#include <jni.h>

typedef struct Signal {
    double x;
    double y;
    double z;
} Signal;

const int L = 64;
//必须是2的幂
const int N = 30;
//分析窗口，大小为1s中的监测数据
Signal pre1 = {-9999, -9999, -9999}, pre2 = {-9999, -9999, -9999};
double rms0[N];
double rms[2 * L];
//double rms_xy[2*L], rms0_xy[N];//用于判断是否是自动暂停，等红灯原地踏步的
int signal_index = 0;
//当前信号指针的位置，与x可用值的数目相关
int analysis_index = 0;
//0 //分析方差和标准差窗口的索引
int signal_num = 0;
//记录signal的数值
Signal acc[2 * L];
int motion = -1;
//表示当前的状态
double acc_store_origin[L + N];
//存储开始时用于分析的那些速度
double acc_store[N + 2];
//存储后面的信号
int acc_store_index = 0;
//acc_store的存储
int pause_flag = 2;
//0是暂停，1是暂停恢复，2是运动常态
int pause_count = 0;
//int valid_count = 0;//5;
const double thre_energy_static = 2.0;
const double thre_variance_static = 1.0;
const double thre_variance_unregular = 0.9;

/*
 const double thre_energy_static_xy = 0.001;
 const double thre_energy_for_walk = 0.03;
 const double thre_energy_for_swing = 0.14;
 const double thre_energy_for_run = 5.0;
 
 
 const double thre_variance_for_run = 2.0;
 const double thre_variance_for_walk = 0.105;
 const double thre_variance_static_xy = 0.04;
 */
/*const double max_acc_for_1 = 12;//2.18;
 const double max_acc_for_2 = 2.6;//1.3;
 const double max_acc_for_3 = 1.7;//1.58;//1.58
 const double max_acc_for_4 = 1.4;//1.2;
 const double max_acc_for_5 = 2.0;//1.2;
 const double max_acc_for_6 = 1.7;//1.3;
 const double max_acc_for_7 = 1.6;//1.1;*/

//walk with phone in hand
/*const double thre_energy_walk_hand_max = 11.0;//5.0;
const double thre_energy_walk_hand_min = 4.8;//5.0;
const double thre_variance_walk_hand_min = 2.0;
const double thre_variance_walk_hand_max = 3.5;//2.0
const double max_acc_for_walk_hand = 8.5;//8.5;//8.5;
//walk with phone in pocket
const double thre_energy_walk_pocket = 2.0;
const double thre_variance_walk_pocket = 1.0;
const double max_acc_for_walk_pocket = 20;
//walk swing
const double thre_energy_walk_swing_min = 5.0;//4.0;
const double thre_energy_walk_swing_max = 60.0;//5.3;
const double thre_variance_walk_swing_min = 1.0;//2.0;
const double thre_variance_walk_swing_max = 3.5;
const double max_acc_for_walk_swing = 13.0;//12.0;*/

const double thre_energy_walk_hand_min = 6.0;
const double thre_energy_walk_hand_max = 12.0;
const double thre_variance_walk_hand_min = 2.1;
const double thre_variance_walk_hand_max = 3.3;
const double max_acc_for_walk_hand = 10.9;//8.5;

const double thre_energy_walk_swing_min = 3.0;
const double thre_energy_walk_swing_max = 6.0;
const double max_acc_for_walk_swing = 9.4;

const double thre_energy_walk_pants_min = 12.0;
const double thre_energy_walk_pants_max = 30.0;
const double thre_variance_walk_pants_min = 3.0;
const double thre_variance_walk_pants_max = 8.0;
const double min_acc_for_walk_pants = 4.8;

const double thre_energy_walk_up_min = 30.0;
const double thre_energy_walk_up_max = 59.9;
const double thre_variance_walk_up_min = 3.0;
const double thre_variance_walk_up_max = 8.0;
const double min_acc_for_walk_up = 5.0;


int valid_size1 = 3;
int valid_size3 = 4;
//run swing
const double thre_energy_run_swing = 100;
const double thre_variance_run_swing = 9.9;
const double max_acc_for_run_swing = 20;
//run pants
const double thre_energy_run_pocket = 60;
const double thre_variance_run_pocket = 3.0;
const double max_acc_for_run_pocket = 25;

int valid_size2 = 0;

void init() {
    //acc = (*Signal)malloc(2*L*sizeof(Signal));
    signal_index = 0;//当前信号指针的位置，与x可用值的数目相关
    analysis_index = 0; //分析方差和标准差窗口的索引
    pre1.x = -9999;
    pre1.y = -9999;
    pre1.z = -9999;
    pre2.x = -9999;
    pre2.y = -9999;
    pre2.z = -9999;
    memset(acc, -1, 2 * L * sizeof(Signal));
    memset(rms, -1, 2 * L * sizeof(double));
    memset(rms0, -1, N * sizeof(double));
    // memset(rms_xy,-1, 2*L*sizeof(double));
    //memset(rms0_xy,-1, N*sizeof(double));
    memset(acc_store_origin, -1, (L + N) * sizeof(double));
    memset(acc_store, -1, (N + 2) * sizeof(double));

    pause_count = 0;
    pause_flag = 2;
    //rms = (*double)malloc(2*L*sizeof(double));
}

/*Hanning Recursive Smoothing*/
Signal filterSignal(Signal s) {

    if (pre1.x == -9999 && pre2.x == -9999) {
        pre2.x = s.x;
        pre2.y = s.y;
        pre2.z = s.z;
        return s;
    }
    else if (pre1.x == -9999 && pre2.x != -9999) {
        Signal tem = {(s.x + pre2.x) / 2, (s.y + pre2.y) / 2, (s.z + pre2.z) / 2};
        pre1.x = pre2.x;
        pre1.y = pre2.y;
        pre1.z = pre2.z;
        pre2.x = tem.x;
        pre2.y = tem.y;
        pre2.z = tem.z;
        return tem;
    }
    else {
        Signal tem = {(s.x + 2 * pre2.x + pre1.x) / 4, (s.y + 2 * pre2.y + pre1.y) / 4,
                      (s.z + 2 * pre2.z + pre1.z) / 4};
        pre1.x = pre2.x;
        pre1.y = pre2.y;
        pre1.z = pre2.z;
        pre2.x = tem.x;
        pre2.y = tem.y;
        pre2.z = tem.z;
        return tem;

    }
}

/*区分运动状态，是在手上(swing), 还是 text/phone/bag taking还是静止*/
int motionDetection() { // 0 Swing; 1 Phone in hand or backbag
    double energy = 0;
    double variance = 0;
    double e_value = 0, v_value = 0, average = 0;

    for (int i = 0; i < N; i++) {
        //printf("rms0: %2f\n", rms0[i]);
        e_value += pow(rms0[i], 2);
        //printf("e value: %2f\n", e_value);
        average += rms0[i];

        //e_value_xy += pow(rms0[i],2);
        //average += rms0_xy[i];
    }
    //printf("e value: %2f\n", e_value);
    energy = e_value / N;
    average = average / N;

    // energy_xy = e_value_xy/N;
    // average_xy = average_xy/N;
    for (int i = 0; i < N; i++) {
        v_value += pow(rms0[i] - average, 2);
        //v_value_xy += pow(rms0_xy[i] - average_xy, 2);
        //printf("v value: %2f\n", v_value);
    }
    //printf("variance: %2f", variance);
    variance = sqrt(v_value / N);
    //variance_xy = sqrt(v_value_xy/N);
    //printf("%d e:%2f v:%2f\n",signal_num,energy_xy, variance_xy);
//    printf("e:%2f v:%2f\n", energy, variance);
    //判断是行走还是静止状态

    if (energy <= thre_energy_static && variance <= thre_variance_static) {
        pause_count++;
        if (pause_count == 2) {
            pause_flag = 0;
        }

        return 0;//静止状态
    }
    else if (variance < thre_variance_unregular) {
        //printf("Uregular state!\n");
        return -1;//不规则运动
    }
    else {
        pause_count = 0;
        if (pause_flag == 0) {
            pause_flag = 1;

            //runflag = true;
        }
        else if (pause_flag == 1) {
            pause_flag = 2;
        }
        if (energy >= thre_energy_run_swing && variance >= thre_variance_run_swing) {
            return 4;
        }
        if (energy >= thre_energy_run_pocket && variance >= thre_variance_run_pocket) {
            return 5;
        }
        if (energy >= thre_energy_walk_up_min && energy <= thre_energy_walk_up_max &&
            variance >= thre_variance_walk_up_min && variance <= thre_variance_walk_up_max) {
            return 6;
        }

        if (energy >= thre_energy_walk_pants_min && energy <= thre_energy_walk_pants_max &&
            variance >= thre_variance_walk_pants_min && variance <= thre_variance_walk_pants_max) {
            return 3;
        }
        if (energy >= thre_energy_walk_hand_min && energy <= thre_energy_walk_hand_max &&
            variance >= thre_variance_walk_hand_min && variance <= thre_variance_walk_hand_max) {
            return 1;
        }


        if (energy >= thre_energy_walk_swing_min && energy <= thre_energy_walk_swing_max &&
            variance >= thre_variance_walk_hand_min && variance <= thre_variance_walk_hand_max) {
            return 2;
        }

        return 0;

    }
}

/*按峰值记步*/
int peakInWindow(double a[], int n, double max, int valid_count) {
    int step = 0;
    int count = 0;
    //double sum = a[0];
    for (int i = 1; i < n - 1; i++) {
        //sum += a[i];
        //printf("%f\n", a[i]);
        count++;
        if (a[i] > a[i + 1] && a[i] > a[i - 1] && a[i] > max && count > valid_count) {
            step++;
            count = 0;
        }
    }
    //sum += a[n-1];
    //printf("avg: %f \n", sum/n);
    return step;
}

int bottomInWindow(double a[], int n, double min, int valid_count) {
    int step = 0;
    int count = 0;
    //double sum = a[0];
    for (int i = 1; i < n - 1; i++) {
        //sum += a[i];
        //printf("%f\n", a[i]);
        count++;
        if (a[i] < a[i + 1] && a[i] < a[i - 1] && a[i] < min && count > valid_count) {
            step++;
            count = 0;
        }
    }
    //sum += a[n-1];
    //printf("avg: %f \n", sum/n);
    return step;
}

int forMotion(double a, double b, double c) {
    int motion = -1;
    return motion;
}

int Pedometer(double a, double b, double c) {//flag表示是否记步，如果是则记步，否则存储，并在第一次到达的时候统计前面的步数
    int step = 0;
    Signal s = {a, b, c};
    //Signal tem = filterSignal(s);//过滤信号
    Signal tem = s;
    acc[signal_index].x = tem.x;
    acc[signal_index].y = tem.y;
    acc[signal_index].z = tem.z;
    rms[signal_index] = sqrt(pow(acc[signal_index].x, 2) + pow(acc[signal_index].y, 2) +
                             pow(acc[signal_index].z, 2));
    //rms_xy[signal_index] = sqrt(pow(acc[signal_index].x,2) + pow(acc[signal_index].y,2));
    signal_index++;
    if (signal_num < L) {
        acc_store_origin[signal_num] = rms[signal_index - 1];
        signal_num++;
        return step;//step = 0
    }
    //printf("水平方向%f\n", rms_xy[signal_index-1]);
    double rms0_value = 0;
    //double rms0_xy_value = 0;
    for (int i = 0; i < L; i++) {
        rms0_value += rms[signal_index - i - 1];
        //rms0_xy_value += rms_xy[signal_index-i-1];
    }

    if (signal_num < L + N) {//初始分析时，此时不记步
        acc_store_origin[signal_num] = rms[signal_index - 1];
        signal_num++;
        rms0[analysis_index] = rms[signal_index - 1] - rms0_value / L;
        //rms0_xy[analysis_index] = rms_xy[signal_index-1] - rms0_xy_value/L;
        //printf("rms0:%f\n", rms0[analysis_index]);
        analysis_index++;
        step = 0;
    }
    else if (signal_num == L + N) {
        motion = motionDetection();
        for (int i = 0; i < L + N; i++) {
            //printf("%f\n",acc_store_origin[i]);
        }
        //printf("motion:%d\n",motion);
        switch (motion) {
            case 1:
                step += peakInWindow(acc_store_origin, L + N, max_acc_for_walk_hand, valid_size1);
                break;
            case 2:
                step += peakInWindow(acc_store_origin, L + N, max_acc_for_walk_swing, valid_size1);
                break;
            case 3:
                step += bottomInWindow(acc_store_origin, L + N, min_acc_for_walk_pants,
                                       valid_size3);
                break;
            case 4:
                step += peakInWindow(acc_store_origin, L + N, max_acc_for_run_swing, valid_size2);
                break;
            case 5:
                step += peakInWindow(acc_store_origin, L + N, max_acc_for_run_pocket, valid_size3);
                break;
            case 6:
                step += bottomInWindow(acc_store, N + 2, min_acc_for_walk_up, valid_size3);

            default:
                break;
        }
        acc_store[0] = acc_store_origin[signal_num - 2];
        acc_store[1] = acc_store_origin[signal_num - 1];
        acc_store_index = 2;
        analysis_index = 0;
        signal_num++;
    }
    else {//signal_num > L+N
        //调整acc_store的值
        acc_store[acc_store_index] = rms[signal_index - 1];
        acc_store_index++;
        rms0[analysis_index] = rms[signal_index - 1] - rms0_value / L;
        //rms0_xy[analysis_index] = rms_xy[signal_index-1] - rms0_xy_value/L;
        //printf("rms0:%f\n", rms0[analysis_index]);
        analysis_index++;
        if (analysis_index == N) {
            for (int i = 0; i < N + 2; i++) {
                //printf("%f\n",acc_store[i]);
            }
            motion = motionDetection();
            switch (motion) {
                case 1:
                    step += peakInWindow(acc_store, N + 2, max_acc_for_walk_hand, valid_size1);
                    break;
                case 2:
                    step += peakInWindow(acc_store, N + 2, max_acc_for_walk_swing, valid_size1);
                    break;
                case 3:
                    step += bottomInWindow(acc_store, N + 2, min_acc_for_walk_pants, valid_size3);
                    break;
                case 4:
                    step += peakInWindow(acc_store, N + 2, max_acc_for_run_swing, valid_size2);
                    break;
                case 5:
                    step += peakInWindow(acc_store, N + 2, max_acc_for_run_pocket, valid_size2);
                    break;
                case 6:
                    step += bottomInWindow(acc_store, N + 2, min_acc_for_walk_up, valid_size3);
                    break;
                default:

                    break;
            }
            acc_store[0] = acc_store[N];
            acc_store[1] = acc_store[N + 1];
            acc_store_index = 2;
            analysis_index = 0;
            //analysis_index = 1;
        }
        signal_num++;
    }

    if (signal_index == 2 * L) {
        for (int i = 0; i < L; i++) {
            acc[i].x = acc[i + L].x;
            acc[i].y = acc[i + L].y;
            acc[i].z = acc[i + L].z;

        }
        signal_index = L;
    }
    return step;
}

extern "C" {
    JNIEXPORT void JNICALL
    Java_com_tulipsport_android_sporttracker_MockStepDetectorV2_resetStep(JNIEnv *env,
                                                                          jobject instance) {
        init();
    }

    JNIEXPORT jint JNICALL
    Java_com_tulipsport_android_sporttracker_MockStepDetectorV2_pedometer(JNIEnv *env, jobject instance,
                                                                          jfloat x, jfloat y,
                                                                          jfloat z) {
        return Pedometer(x, y, z);
    }
}