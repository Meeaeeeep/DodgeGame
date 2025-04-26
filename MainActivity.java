package com.example.dodgegame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private GameSurface gameSurface;
    private SoundEffects soundEffects;
    private PlayerController playerController;
    private int screenWidth, screenHeight;
    private final float playerWidth = 300; // Fixed player width

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        gameSurface = new GameSurface(this, screenWidth, screenHeight);
        setContentView(gameSurface);

        // Start background music
        startService(new Intent(this, BackgroundMusic.class));

        // Initialize sound effects
        soundEffects = new SoundEffects(this);

        // Initialize player controller with actual width
        playerController = new PlayerController(this, screenWidth / 2.0f, screenWidth, playerWidth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    public class GameSurface extends SurfaceView implements Runnable {
        private Thread gameThread;
        private SurfaceHolder holder;
        private volatile boolean running = false;
        private Bitmap playerImage, damagedPlayerImage, currentPlayerImage, background, endScreenBackground, enemy;
        private float enemyX, enemyY, enemySpeed;
        private Random random;
        private Paint timerPaint, livesPaint;
        private long startTime;
        private boolean gameOver = false;
        private int playerLives = 2;
        private float playerX, playerY;
        private Handler handler;
        long finalTime;

        public GameSurface(Context context, int screenWidth, int screenHeight) {
            super(context);
            holder = getHolder();
            random = new Random();
            handler = new Handler();

            // Load images
            background = BitmapFactory.decodeResource(getResources(), R.drawable.peppabg);
            background = Bitmap.createScaledBitmap(background, screenWidth, screenHeight, false);
            endScreenBackground = BitmapFactory.decodeResource(getResources(), R.drawable.suzysheepbg);
            endScreenBackground = Bitmap.createScaledBitmap(endScreenBackground, screenWidth, screenHeight, false);

            playerImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.suzysheep), 300, 300, false);
            damagedPlayerImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.suzysheeptwo), 300, 300, false);
            currentPlayerImage = playerImage;

            enemy = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.evilpeppapig), 250, 250, false);

            enemyX = random.nextInt(screenWidth - 250);
            enemyY = 0;
            enemySpeed = 10;

            // Initialize player position
            playerX = screenWidth / 2.0f - 150;
            playerY = screenHeight - 300;

            // Setup timer display
            timerPaint = new Paint();
            timerPaint.setTextSize(70);
            timerPaint.setColor(android.graphics.Color.BLACK);

            // Setup lives display
            livesPaint = new Paint();
            livesPaint.setTextSize(70);
            livesPaint.setColor(android.graphics.Color.RED);

            startTime = System.currentTimeMillis(); // Start timer

            // Detect screen taps for speed boost
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        enemySpeed += 10;  // Boost enemy speed
                    }
                    return true;
                }
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
            while (running) {
                if (!holder.getSurface().isValid()) {
                    continue;
                }

                Canvas canvas = holder.lockCanvas();
                if (canvas == null) {
                    continue;
                }

                if (!gameOver) {
                    canvas.drawBitmap(background, 0, 0, null);

                    // Calculate time survived
                    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

                    // Draw timer
                    canvas.drawText("Time: " + elapsedTime + "s", 50, 100, timerPaint);

                    // Draw lives count
                    canvas.drawText("Lives: " + playerLives, 50, 200, livesPaint);

                    // Draw player
                    canvas.drawBitmap(currentPlayerImage, playerController.getPlayerX(), playerY, null);

                    // Enemy movement
                    enemyY += enemySpeed;
                    if (enemyY > getHeight()) {
                        enemyX = random.nextInt(getWidth() - 250);
                        enemyY = 0;
                    }
                    canvas.drawBitmap(enemy, enemyX, enemyY, null);

                    // Collision detection
                    if (Math.abs(playerController.getPlayerX() - enemyX) < 50 && Math.abs(playerY - enemyY) < 50) {
                        soundEffects.playCollisionSound();
                        playerLives--;

                        if (playerLives == 1) {
                            currentPlayerImage = damagedPlayerImage;
                            handler.postDelayed(() -> currentPlayerImage = playerImage, 2000);  // Restore image after 2 seconds
                        }
                        if (playerLives == 0) {
                            gameOver = true;
                            finalTime = (System.currentTimeMillis() - startTime) / 1000;
                        }
                    }
                } else {
                    canvas.drawBitmap(endScreenBackground, 0, 0, null);
                    //long finalTime = (System.currentTimeMillis() - startTime) / 1000;
                    canvas.drawText("Game Over!", getWidth() / 2 - 150, getHeight() / 2, timerPaint);
                    canvas.drawText("You survived: " + finalTime + "s", getWidth() / 2 - 200, getHeight() / 2 + 100, timerPaint);
                }

                holder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void resume() {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}