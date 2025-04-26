package com.example.dodgegame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PlayerController implements SensorEventListener {
    private float playerX, screenWidth, playerWidth;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    public PlayerController(Context context, float x, float screenWidth, float playerWidth) {
        this.playerX = x;
        this.screenWidth = screenWidth;
        this.playerWidth = playerWidth;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float tilt = event.values[0]; // Detect device tilt

            // Move player based on tilt sensitivity
            playerX -= tilt * 5;

            // Ensure player stays within screen boundaries
            playerX = Math.max(0, Math.min(screenWidth - playerWidth, playerX));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public float getPlayerX() {
        return playerX;
    }
}