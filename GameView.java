package com.example.dodgegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View implements Runnable {
    private Thread gameThread;
    private volatile boolean running = false;
    private List<Enemy> enemies;
    private PlayerController playerController;
    private SoundEffects soundEffects;
    private Paint timerPaint;
    private float screenWidth, screenHeight;
    private boolean speedUp;
    private float playerY, playerWidth;
    private long startTime;
    private boolean gameOver = false;

    public GameView(Context context, float screenWidth, float screenHeight, float playerWidth) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.playerY = screenHeight - 250;
        this.playerWidth = playerWidth;
        this.enemies = new ArrayList<>();
        this.speedUp = false;

        // Initialize PlayerController with actual width
        float playerX = screenWidth / 2.0f - playerWidth / 2;
        this.playerController = new PlayerController(context, playerX, screenWidth, playerWidth);
        this.soundEffects = new SoundEffects(context);

        // Timer display
        timerPaint = new Paint();
        timerPaint.setColor(Color.BLUE);
        timerPaint.setTextSize(50);
        timerPaint.setTextAlign(Paint.Align.LEFT);

        // Spawn enemies
        for (int i = 0; i < 5; i++) {
            enemies.add(new Enemy((int) screenWidth, (int) screenHeight));
        }

        // Start timer
        startTime = System.currentTimeMillis();

        // Handle touch for speed boost
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                speedUp = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                speedUp = false;
            }
            return true;
        });

        startGameLoop();
    }

    public void startGameLoop() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameLoop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running && !gameOver) {
            postInvalidate();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!gameOver) {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            canvas.drawText("Time: " + elapsedTime + "s", 50, 100, timerPaint);

            // Draw player
            canvas.drawCircle(playerController.getPlayerX(), playerY, playerWidth / 2, timerPaint);

            // Move enemies and check collisions
            for (Enemy enemy : enemies) {
                enemy.update();
                canvas.drawCircle(enemy.getX(), enemy.getY(), 40, timerPaint);
                if (enemy.checkCollision(playerController.getPlayerX(), playerY, playerWidth, 50)) {
                    gameOver = true;
                    soundEffects.playCollisionSound();
                    stopGameLoop();
                }
            }
        } else {
            long finalTime = (System.currentTimeMillis() - startTime) / 1000;
            canvas.drawText("Game Over!", screenWidth / 2 - 150, screenHeight / 2, timerPaint);
            canvas.drawText("You survived: " + finalTime + "s", screenWidth / 2 - 200, screenHeight / 2 + 100, timerPaint);
        }
    }
}