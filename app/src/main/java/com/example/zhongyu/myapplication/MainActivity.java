package com.example.zhongyu.myapplication;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tulipsport.android.common.logger.Log;
import com.tulipsport.android.sporttracker.HeightListener;
import com.tulipsport.android.sporttracker.HeightSensorManager;
import com.tulipsport.android.sporttracker.StepListener;
import com.tulipsport.android.sporttracker.StepSensorManager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private TextView testData;
    private TextView gyroscope;
    private TextView tvPressure;
    private TextView tvAltide;
    private Sensor sensor = null;

    private Button startButton,
            suspendButton,
            resetButton,
            finishButton;

    private int count = 0;
    private int count2 = 0;

    private static final int CHANGE = 0X001;
    private static final int CHANGE2 = 0X002;
    private static final int HEIGHT = 0X003;


    StepSensorManager stepDetectorSensorManager;
    StepSensorManager mockStepSensorManager;
    HeightSensorManager heightDectorSensorManager;
    private SensorManager mSensorManager = null;

    SdCardLogNode sdCardLogNode;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHANGE:
                    testData.setText("" + msg.arg1);
                    break;
                case CHANGE2:
                    gyroscope.setText("" + msg.arg1);
                    break;
                case HEIGHT:
                    tvPressure.setText(String.valueOf(msg.arg1));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        sdCardLogNode = new SdCardLogNode(this);
        Log.setLogNode(sdCardLogNode);

        init();
        initActions();

        senSor();
    }


    private void senSor() {
        stepDetectorSensorManager = new StepSensorManager(MainActivity.this, StepSensorManager.STEP_DETECTOR_MODE_SYSTEM);
        mockStepSensorManager = new StepSensorManager(MainActivity.this, StepSensorManager.STEP_DETECTOR_MODE_MOCK);
        heightDectorSensorManager = new HeightSensorManager(MainActivity.this, HeightSensorManager.HEIGHT_DETECTOR_MODE_SYSTEM);

        stepDetectorSensorManager.addStepListener(new StepListener() {
            @Override
            public void onStep(int stepCount) {
                count += stepCount;
                Message msg = new Message();
                msg.what = CHANGE;
                msg.arg1 = count;
                handler.sendMessage(msg);
            }
        });

        mockStepSensorManager.addStepListener(new StepListener() {
            @Override
            public void onStep(int stepCount) {
                count2 += stepCount;
                Message msg = new Message();
                msg.what = CHANGE2;
                msg.arg1 = count2;
                handler.sendMessage(msg);
            }
        });


        heightDectorSensorManager.addHeightListener(new HeightListener() {
            @Override
            public void onHeight(int height) {
                Message msg = new Message();
                msg.what = HEIGHT;
                msg.arg1 = height;
                handler.sendMessage(msg);
            }
        });
    }

    private void init() {
        gyroscope = (TextView) findViewById(R.id.gyroscope);
        testData = (TextView) findViewById(R.id.pedometer);
        tvPressure = (TextView) findViewById(R.id.pressure);
        tvAltide = (TextView) findViewById(R.id.atiuade);
        startButton = (Button) findViewById(R.id.start);
        suspendButton = (Button) findViewById(R.id.suspend);
        resetButton = (Button) findViewById(R.id.reset);
        finishButton = (Button) findViewById(R.id.finish);
    }

    private void initActions() {
        startButton.setOnClickListener(this);
        suspendButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        finishButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                stepDetectorSensorManager.start();
                mockStepSensorManager.start();
                heightDectorSensorManager.start();
                sdCardLogNode.start();
                break;
            case R.id.suspend:
                stepDetectorSensorManager.stop();
                mockStepSensorManager.stop();
                heightDectorSensorManager.stop();
                sdCardLogNode.stop();
                break;
            case R.id.finish:
                stepDetectorSensorManager.stop();
                mockStepSensorManager.stop();
                heightDectorSensorManager.stop();
                stepDetectorSensorManager.reset();
                mockStepSensorManager.reset();
                heightDectorSensorManager.reset();
                sdCardLogNode.stop();

                final EditText editText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(editText)
                        .setTitle("请输入文件名称")
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = editText.getText().toString();
                                //sdCardLogNode.flush(AccelerometerSensorManager.TAG, name);
                            }
                        })
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                //sdCardLogNode.clearTag(AccelerometerSensorManager.TAG);
                            }
                        })
                        .create();
                dialog.show();
                break;
            case R.id.reset:
                count = 0;
                count2 = 0;


                stepDetectorSensorManager.reset();
                mockStepSensorManager.reset();
                heightDectorSensorManager.reset();
                testData.setText("0");
                gyroscope.setText("0");
                tvPressure.setText("0");
                break;
            default:
                break;
        }
    }


}


















