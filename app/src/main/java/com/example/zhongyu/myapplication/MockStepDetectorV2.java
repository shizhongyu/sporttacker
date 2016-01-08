package com.example.zhongyu.myapplication;
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
    StepRecognizer _recognizer;
    int stepCount = 0;

    public MockStepDetectorV2(StepListener stepListener) {
        this.stepListener = stepListener;

        init();
    }

    private void init() {
        _recognizer = new StepRecognizer();
        _recognizer.reset();
    }

    @Override
    public void reset() {
        stepCount = 0;
        _recognizer.reset();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            int count = _recognizer.pedometer(event.values[0], event.values[1], event.values[2]);

            if (count == 0) return;

            stepCount += count;

            if (stepListener != null) {
                stepListener.onStep(count);
            }
        }catch (Exception e){
            Log.e("StepRecognizer", "error", e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    interface ICloneable<T> {
        T cloneObj();
    }

    static class Signal implements ICloneable {
        double x;
        double y;
        double z;

        public Signal(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public Object cloneObj() {
            return new Signal(this.x, this.y, this.z);
        }
    }

    static class StepRecognizer {
        static final String TAG = "StepRecognizer";

        //必须是2的幂
        static final int L = 64;
        //分析窗口，大小为1s中的监测数据
        static final int N = 30;
        Signal pre1 = new Signal(-9999, -9999, -9999), pre2 = new Signal(-9999, -9999, -9999);

        double[] rms, rms0;
        //用于判断是否是自动暂停，等红灯原地踏步的
//        double[] rms_xy, rms0_xy;

        //当前信号指针的位置，与x可用值的数目相关
        int signal_index = 0;
        //分析方差和标准差窗口的索引
        int analysis_index = 0;
        //记录signal的数值
        int signal_num = 0;

        Signal[] acc;
        //表示当前的状态
        int motion = -1;
        double[] acc_store //存储后面的信号
                //存储开始时用于分析的那些速度
                , acc_store_origin;

        //acc_store的存储
        int acc_store_index = 0;
        //0是暂停，1是暂停恢复，2是运动常态
        int pause_flag = 2;
        int pause_count = 0;
        int peak_index = 0;//当前峰值所在的索引，用于判断峰值值是否记录到步数中
        //只有在相邻的两个峰值之间的值大于此值时，峰值才有效
//        static final int valid_count = 21;
        //17
        static final int valid_count_for_run = 15;


        static final double thre_energy_static = 2.0;//0.001;
        static final double thre_variance_static = 1.0;
        static final double thre_variance_unregular = 0.9;

        //walk with phone in hand
        static final double thre_energy_walk_hand_max = 11.0;//5.0;
        static final double thre_energy_walk_hand_min = 5.2;//5.0;
        static final double thre_variance_walk_hand_min = 2.0;
        static final double thre_variance_walk_hand_max = 3.5;//2.0
        static final double max_acc_for_walk_hand = 8.5;//8.5;
        //walk with phone in pocket
        static final double thre_energy_walk_pocket = 20.0;
        static final double thre_variance_walk_pocket = 4.0;
        static final double min_acc_for_walk_pocket = 9.9;
        //walk swing
        static final double thre_energy_walk_swing_min = 4.0;
        static final double thre_energy_walk_swing_max = 5.3;
        static final double thre_variance_walk_swing_min = 2.0;
        static final double thre_variance_walk_swing_max = 3.5;
        static final double min_acc_for_walk_swing = 12.0;

        int valid_size1 = 4;

        //run swing
        static final double thre_energy_run_swing = 100;
        static final double thre_variance_run_swing = 9.9;
        static final double max_acc_for_run_swing = 20;
        //run pants
        static final double thre_energy_run_pocket = 60;
        static final double thre_variance_run_pocket = 3.0;
        static final double max_acc_for_run_pocket = 25;

        int valid_size2 = 0;

        int signalCount = 0;

        StepRecognizer() {
            rms = new double[2 * L];
            rms0 = new double[N];
//            rms_xy = new double[2 * L];
//            rms0_xy = new double[N];
            acc = new Signal[2 * L];
            acc_store = new double[N + 2];
            acc_store_origin = new double[L + N];
        }

        static <T> void memset(T[] arr, ICloneable<T> initValue) {
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = initValue.cloneObj();
                }
            }
        }

        static void memset(double[] arr, double initValue) {
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = initValue;
                }
            }
        }

        void reset() {
            //acc = (*Signal)malloc(2*L*sizeof(Signal));
            signal_index = 0;//当前信号指针的位置，与x可用值的数目相关
            analysis_index = 0; //分析方差和标准差窗口的索引
            pre1.x = -9999;
            pre1.y = -9999;
            pre1.z = -9999;
            pre2.x = -9999;
            pre2.y = -9999;
            pre2.z = -9999;
            memset(acc, new Signal(0, 0, 0));
            memset(rms, -1);
            memset(rms0, -1);
//            memset(rms_xy, -1);
//            memset(rms0_xy, -1);
            memset(acc_store_origin, -1);
            memset(acc_store, -1);
            pause_count = 0;
            pause_flag = 2;
            //rms = (*double)malloc(2*L*sizeof(double));

            signalCount = 0;
        }

        /*区分运动状态，是在手上(swing), 还是 text/phone/bag taking还是静止*/
        int motionDetection() { // 0 Swing; 1 Phone in hand or backbag
            double energy = 0;
            double variance = 0;
            double e_value = 0, v_value = 0, average = 0;
            double energy_xy = 0;
            double variance_xy = 0;
            double e_value_xy = 0, v_value_xy = 0, average_xy = 0;
            for (int i = 0; i < N; i++) {
                //printf("rms0: %2f\n", rms0[i]);
                e_value += Math.pow(rms0[i], 2);
                //printf("e value: %2f\n", e_value);
                average += rms0[i];

                e_value_xy += Math.pow(rms0[i], 2);
//                average += rms0_xy[i];
            }
            //printf("e value: %2f\n", e_value);
            energy = e_value / N;
            average = average / N;

            energy_xy = e_value_xy / N;
            average_xy = average_xy / N;
            for (int i = 0; i < N; i++) {
                v_value += Math.pow(rms0[i] - average, 2);
//                v_value_xy += Math.pow(rms0_xy[i] - average_xy, 2);
                //printf("v value: %2f\n", v_value);
            }
            //printf("variance: %2f", variance);
            variance = Math.sqrt(v_value / N);
            variance_xy = Math.sqrt(v_value_xy / N);
            //printf("%d e:%2f v:%2f\n",signal_num,energy_xy, variance_xy);
            //printf("%d e:%2f v:%2f\n",signal_num,energy, variance);
            //判断是行走还是静止状态

            if (energy <= thre_energy_static && variance <= thre_variance_static) {
                pause_count++;
                if (pause_count == 2) {
                    pause_flag = 0;
//                    printf("静止\n");
                }

                return 0;//静止状态
            } else if (variance < thre_variance_unregular) {
//                printf("Uregular state!\n");
                return -1;//不规则运动
            } else {
                pause_count = 0;
                if (pause_flag == 0) {
//                    printf("Start to Walk again!\n");
                    pause_flag = 1;

                    //runflag = true;
                } else if (pause_flag == 1) {
                    pause_flag = 2;
                }
                if (energy >= thre_energy_run_swing && variance >= thre_variance_run_swing) {
                    return 4;
                }
                if (energy >= thre_energy_run_pocket && variance >= thre_variance_run_pocket) {
                    return 5;
                }

                if (energy >= thre_energy_walk_pocket && variance >= thre_variance_walk_pocket) {
                    return 2;
                }
                if (energy >= thre_energy_walk_hand_min && energy <= thre_energy_walk_hand_max && variance >= thre_variance_walk_hand_min && variance <= thre_variance_walk_hand_max) {
                    return 1;
                }
                if (energy >= thre_energy_walk_swing_min && energy <= thre_energy_walk_swing_max && variance >= thre_variance_walk_swing_min && variance <= thre_variance_walk_swing_max) {
                    return 3;
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


        /*计算在当前窗格内的有效低谷数
         *用于如果手机是放在包内或者电话或者短信的情况
         */
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

        /*切比雪夫I型低通滤波器*/
        double preprocess(double a) {
            double ha = (0.0304 * Math.pow(a, 4) + 0.1218 * Math.pow(a, 3) + 0.1827 * Math.pow(a, 2) + 0.12182 * a + 0.0304) / (0.2286 * Math.pow(a, 4) + 0.8012 * Math.pow(a, 3) + 1.4721 * Math.pow(a, 2) - 1.3834 * a + 1);
            //printf("%2f to %2f\n",a,ha);
            return ha;
        }

        //flag表示是否记步，如果是则记步，否则存储，并在第一次到达的时候统计前面的步数
        int pedometer(double a, double b, double c) {
            int step = 0;
            Signal s = new Signal(a, b, c);
            //Signal tem = filterSignal(s);//过滤信号
            Signal tem = s;
            Log.d(TAG, "pedometer: acc.length:" + acc.length + ",signal_index:" + signal_index);
            acc[signal_index].x = tem.x;
            acc[signal_index].y = tem.y;
            acc[signal_index].z = tem.z;
            rms[signal_index] = Math.sqrt(Math.pow(acc[signal_index].x, 2) + Math.pow(acc[signal_index].y, 2) + Math.pow(acc[signal_index].z, 2));
//            rms_xy[signal_index] = Math.sqrt(Math.pow(acc[signal_index].x, 2) + Math.pow(acc[signal_index].y, 2));
            Log.d(TAG, "pedometer: rms, rms_xy");
            signal_index++;
            if (signal_num < L) {
                acc_store_origin[signal_num] = rms[signal_index - 1];
                signal_num++;
                Log.d(TAG, "pedometer: (signal_num < L)");
                return step;//step = 0
            }
            double rms0_value = 0;
            double rms0_xy_value = 0;
            for (int i = 0; i < L; i++) {
                rms0_value += rms[signal_index - i - 1];
//                rms0_xy_value += rms_xy[signal_index - i - 1];
            }
            if (signal_num < L + N) {//初始分析时，此时不记步
                acc_store_origin[signal_num] = rms[signal_index - 1];
                signal_num++;
                step = 0;
                rms0[analysis_index] = rms[signal_index - 1] - rms0_value / L;
//            rms0_xy[analysis_index] = rms_xy[signal_index - 1] - rms0_xy_value / L;
                //printf("rms0:%f\n", rms0[analysis_index]);
                analysis_index++;
                Log.d(TAG, "pedometer: (signal_num < L + N)");
            } else if (signal_num == L + N) {
                Log.d(TAG, "motion:" + motion + ",step:" + step);
                motion = motionDetection();
//                for (int i = 0; i < L + N; i++) {
//                    //printf("%f\n",acc_store_origin[i]);
//                }
                switch (motion) {
                    case 1:
                        step += peakInWindow(acc_store_origin, L + N, max_acc_for_walk_hand, valid_size1);
                        break;
                    case 2:
                        step += bottomInWindow(acc_store_origin, L + N, min_acc_for_walk_pocket, valid_size1);
                        break;
                    case 3:
                        step += bottomInWindow(acc_store_origin, L + N, min_acc_for_walk_swing, valid_size1);
                        break;
                    case 4:
                        step += peakInWindow(acc_store_origin, L + N, max_acc_for_run_swing, valid_size2);
                        break;
                    case 5:
                        step += peakInWindow(acc_store_origin, L + N, max_acc_for_run_pocket, valid_size2);
                        break;
                    default:
                        break;
                }
                acc_store[0] = acc_store_origin[signal_num - 2];
                acc_store[1] = acc_store_origin[signal_num - 1];
                acc_store_index = 2;
                analysis_index = 0;
                signal_num++;
            } else {//signal_num > L+N
                rms0[analysis_index] = rms[signal_index - 1] - rms0_value / L;
//            rms0_xy[analysis_index] = rms_xy[signal_index - 1] - rms0_xy_value / L;
                //printf("rms0:%f\n", rms0[analysis_index]);
                analysis_index++;
                //调整acc_store的值
                acc_store[acc_store_index] = rms[signal_index - 1];
                acc_store_index++;
                Log.d(TAG, "pedometer: 调整acc_store的值(signal_num > L+N)");

                if (analysis_index == N) {
                    for (int i = 0; i < N + 2; i++) {
                        //printf("%f\n",acc_store[i]);
                    }
                    motion = motionDetection();
                    Log.d(TAG, "motion:" + motion);
//                    printf("motion:%d\n", motion);
                    switch (motion) {
                        case 1:
                            step += peakInWindow(acc_store, N + 2, max_acc_for_walk_hand, valid_size1);
                            break;
                        case 2:
                            step += bottomInWindow(acc_store, N + 2, min_acc_for_walk_pocket, valid_size1);
                            break;
                        case 3:
                            step += bottomInWindow(acc_store, N + 2, min_acc_for_walk_swing, valid_size1);
                            break;
                        case 4:
                            step += peakInWindow(acc_store, N + 2, max_acc_for_run_swing, valid_size2);
                            break;
                        case 5:
                            step += peakInWindow(acc_store, N + 2, max_acc_for_run_pocket, valid_size2);
                            break;
                        default:

                            break;
                    }
                    acc_store[0] = acc_store[N];
                    acc_store[1] = acc_store[N + 1];
                    acc_store_index = 2;
                    analysis_index = 0;
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
            Log.d(TAG, "pedometer: step--" + step);
            return step;
        }
    }
}
