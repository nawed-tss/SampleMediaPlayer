package com.tss.samplemediabrowserservice;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class MediaPlaybackHelper {
    final String LOG_TAG = "MP-Server MediaPlaybackHelper";
    private static MediaPlaybackHelper sInstance;

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSession;
    private MediaSessionCallback mMediaSessionCallback;
    private PlaybackStateCompat.Builder stateBuilder;

    public boolean hasAudioFocus = false;
    private AudioManager mAudioManager;
    private AudioFocusRequest mAudioFocusRequest;
    AudioManager.OnAudioFocusChangeListener afChangeListener = focusChange -> {
        Log.i(LOG_TAG, "OnAudioFocusChangeListener: " + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS | focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            hasAudioFocus = false;
            if (mMediaPlayer.isPlaying()) {
                Log.i(LOG_TAG, "OnAudioFocusChangeListener: Media player is paused");
                mMediaPlayer.pause();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            //TODO: Fade down volume
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            hasAudioFocus = true;
            Log.i(LOG_TAG, "OnAudioFocusChangeListener: Starting Media player");
            mMediaPlayer.start();
        }
    };

    private MediaPlaybackHelper() {
        Log.i(LOG_TAG, "MediaPlaybackHelper: Initializing Media Player");
        mMediaPlayer = new MediaPlayer();
    }

    public static MediaPlaybackHelper getInstance() {
        if (sInstance == null) {
            sInstance = new MediaPlaybackHelper();
        }
        return sInstance;
    }

    public void initializeMediaSession(Context context) {
        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(context, LOG_TAG);

        // Create a Media Session Callback
        mMediaSessionCallback = new MediaSessionCallback(context);

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        mMediaSession.setCallback(mMediaSessionCallback);
    }

    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void initializeAudioManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(afChangeListener)
                .setAudioAttributes(attrs)
                .build();
    }

    public void requestAudioFocus() {
        int focus = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (focus == AudioManager.AUDIOFOCUS_GAIN) {
            hasAudioFocus = true;
        } else {
            hasAudioFocus = false;
        }
    }

    public void abandonAudioFocus() {
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
    }

    public void stopPlayback() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0,
                        1.0f)
                .setActions(PlaybackStateCompat.ACTION_STOP)
                .build();
        mMediaSession.setPlaybackState(state);
    }

}
