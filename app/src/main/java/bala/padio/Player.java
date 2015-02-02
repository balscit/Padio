package bala.padio;

/**
 *
 * Copyright 2015 Apache License
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *  Provide media player features in a background service
 *  So music can play in the background
 */
public class Player extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener{
    public final static String PlayerStatusBroadcast = "player.status.name";
    public final static String PlayerStatusMessage = "status";

    private final static String TAG = "Player";
    private final static String CmdStop = "CommandStop";

    private static boolean isPlaying = false;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private String playerUrl;

    public Player() {

    }

    // Play a channel from the url
    private void Play(String url) {
        try {

            if(url == null){
                Log.e(TAG, "Empty url");
                return;
            }

            // request audio focus
            if(audioManager == null){
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            }
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                Log.e(TAG, "Audio focus not granted");
                return;
            }

            // initialize player
            if(mediaPlayer != null) {
                mediaPlayer.reset();
            }
            else{
                mediaPlayer = new MediaPlayer();
            }

            // set playerUrl source and prepare media player
            this.playerUrl = url;
            Uri streamUrl = Uri.parse(getStreamUrl(url));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this, streamUrl);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Stop the player from playing the channel
    private void Stop(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if(audioManager != null){
            audioManager.abandonAudioFocus(this);
        }

        setStatus(false);
    }

    // get the stream url from playlist file (pls)
    private String getStreamUrl(String url){
        String streamUrl=url;
        if(url.toLowerCase().endsWith(".pls")){
            String plsFile = Settings.DownloadFromUrl(url);
            Log.i(TAG, "Pls file: " + plsFile);
            if(plsFile == null){
                Log.e(TAG, "Failed to download: " + url);
            }
            else{
                // using regex to find the File field in the pls file
                Pattern regex = Pattern.compile("File1[\\w]*=([^\\n]*)", Pattern.DOTALL);
                Matcher regexMatcher = regex.matcher(plsFile);
                if (regexMatcher.find()) {
                    streamUrl = regexMatcher.group(1);
                    streamUrl = streamUrl.trim();
                }
                else{
                    Log.e(TAG, "Stream playerUrl not found in the pls file");
                }
            }
        }
        Log.d(TAG, "Stream playerUrl: " + streamUrl);
        return streamUrl;
    }

    // set the player status
    // true - player is playing
    // false - player is stopped
    private void setStatus(boolean status){
        isPlaying = status;

        // notify about the status change
        Intent statusIntent = new Intent(PlayerStatusBroadcast);
        statusIntent.putExtra(PlayerStatusMessage, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isStop = intent.getBooleanExtra(CmdStop, false);

                    if(isStop){
                        Stop();
                    }
                    else {
                        Play(Settings.getSelectedChannelUrl());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            this.Stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        setStatus(true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "Media player error: " + what + ", extra: " + extra);
        Stop();
        Play(playerUrl);
        return true;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.e(TAG, "onAudioFocusChange: " + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null || !mediaPlayer.isPlaying()){
                    Play(playerUrl);
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                Stop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.reset();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    // Stop player using intent
    public static void StopPlayer(){
        if(MainActivity.AppContext != null) {
            Intent playerIntent = new Intent(MainActivity.AppContext, Player.class);
            playerIntent.putExtra(CmdStop, true);
            MainActivity.AppContext.startService(playerIntent);
        }
    }

    // Start player using intent
    public static void StartPlayer(){
        if(MainActivity.AppContext != null) {
            Intent playerIntent = new Intent(MainActivity.AppContext, Player.class);
            MainActivity.AppContext.startService(playerIntent);
        }
    }

    // Toggle player state between play/stop
    public static boolean TogglePlayer(){
        if(isPlaying){
            StopPlayer();
            return false;
        }
        else {
            StartPlayer();
            return  true;
        }
    }

    // Tell whether the player is currently playing a channel
    public static  boolean isPlaying(){
        return  isPlaying;
    }
}

