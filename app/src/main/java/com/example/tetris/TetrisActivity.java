package com.example.tetris;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TetrisActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TetrisGame tetrisGame;
    private SurfaceView surfaceView;
    private TextView scoreTextView;
    private Button leftButton, rightButton, upButton, downButton, pauseButton;
    private Handler handler;
    private Runnable gameRunnable;
    private boolean isPaused = false;
    private long lastUpdateTime = 0;
    private static final long SENSOR_UPDATE_INTERVAL = 250; // 传感器更新间隔时间（毫秒）
    private boolean isSensorEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        initializeViews();
        initializeGame();
        initializeSensor();
        setupButtonListeners();
        startGameLoop();
    }

    private void initializeViews() {
        surfaceView = findViewById(R.id.tetrisSurfaceView);
        scoreTextView = findViewById(R.id.scoreTextView);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        pauseButton = findViewById(R.id.pauseButton);
    }

    private void initializeGame() {
        tetrisGame = new TetrisGame(this, surfaceView, scoreTextView);
    }

    private void initializeSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (isSensorEnabled) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void setupButtonListeners() {
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tetrisGame.moveLeft();
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tetrisGame.moveRight();
            }
        });

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tetrisGame.rotate();
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tetrisGame.fastDrop();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPaused) {
                    pauseGame();
                } else {
                    resumeGame();
                }
            }
        });
    }

    private void startGameLoop() {
        handler = new Handler();
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    tetrisGame.update();
                    handler.postDelayed(this, 300);
                }
            }
        };
        handler.post(gameRunnable);
    }

    private void pauseGame() {
        isPaused = true;
        showPauseMenu();
    }

    private void resumeGame() {
        isPaused = false;
        handler.post(gameRunnable);
    }

    private void showPauseMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("游戏暂停");

        // 设置对话框消息
        String[] items = {"启用传感器"};
        boolean[] checkedItems = {isSensorEnabled};

        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                isSensorEnabled = isChecked;
                if (isSensorEnabled) {
                    sensorManager.registerListener(TetrisActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    sensorManager.unregisterListener(TetrisActivity.this);
                }
            }
        });

        builder.setPositiveButton("继续游戏", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resumeGame();
            }
        });

        builder.setNegativeButton("退出到主菜单", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();  // 关闭当前Activity返回主菜单
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime > SENSOR_UPDATE_INTERVAL) {
                float x = event.values[0];
                tetrisGame.handleShake(x);
                lastUpdateTime = currentTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不需要实现
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorEnabled) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorEnabled) {
            sensorManager.unregisterListener(this);
        }
    }
}
