package com.example.dodgegame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundEffects {
    private SoundPool soundPool;
    private int collisionSoundId;

    public SoundEffects(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(5)
                .build();

        collisionSoundId = soundPool.load(context, R.raw.sound_effectboom, 1);
    }

    public void playCollisionSound() {
        if (collisionSoundId != 0) {
            soundPool.play(collisionSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }
}