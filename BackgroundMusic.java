package com.example.dodgegame;

import static android.app.Service.START_STICKY;
import static android.content.Context.AUDIO_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.dodgegame.R;

public class BackgroundMusic extends Service {
    MediaPlayer m;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mylog", "Start playing");

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        m = MediaPlayer.create(this, R.raw.peppapigbgmusic);
        m.setLooping(true);
        m.start();

        return START_STICKY; // Ensures service continues running
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m != null) {
            m.stop();
            m.release();
            m = null;
        }
    }
}