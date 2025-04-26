package com.example.dodgegame;

import java.util.Random;

public class GameObject {
    protected int x;
    protected int y;
    protected int speed;

    public GameObject(int startX, int startY, int startSpeed) {
        this.x = startX;
        this.y = startY;
        this.speed = startSpeed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void update() {
        y += speed; // Moves object downward
    }
}

class Enemy extends GameObject {
    private static final Random random = new Random();
    private int screenWidth, screenHeight;

    public Enemy(int screenWidth, int screenHeight) {
        super(random.nextInt(screenWidth), 0, random.nextInt(5) + 3);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void update() {
        super.update();

        if (y > screenHeight) {
            x = random.nextInt(screenWidth);
            y = -50;  // Respawn slightly above the screen for smoother entry
            speed = random.nextInt(5) + 3;
        }
    }

    public boolean checkCollision(float playerX, float playerY, float playerWidth, float playerHeight) {
        return Math.abs(playerX - x) < playerWidth / 2 && Math.abs(playerY - y) < playerHeight / 2;
    }
}