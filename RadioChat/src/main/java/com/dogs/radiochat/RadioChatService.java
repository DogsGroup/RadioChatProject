package com.dogs.radiochat;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

/**
 * Created by XRPQ48 on 12/8/13.
 */
public class RadioChatService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String ACTION_PLAY = "start";
    MediaPlayer mMediaPlayer = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            String url = "http://........";

            mMediaPlayer = new MediaPlayer(); // initialize it here
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
        return flags;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }
}
