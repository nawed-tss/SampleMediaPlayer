package com.tss.samplemediabrowserclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity implements PlayerActivityComm {
    String LOG_TAG = "MP-Client PlayerActivity";

    Intent intent;

    MediaPlayerHandler mediaPlayerHandler;
    MediaControllerCompat mMediaController;
    MediaBrowserCompat.MediaItem mMediaItem;
    long mMediaItemDuration = 0;

    TextView tvMediaTitle;
    TextView tvMediaArtist;
    TextView tvMediaAlbum;
    TextView tvMediaId;
    TextView tvMediaElapsedTime;

    ImageButton ibPlay;
    ImageButton ibPause;
    ImageButton ibStop;
    ImageButton ibSkipPrevious;
    ImageButton ibFastRewind;
    ImageButton ibFastForward;
    ImageButton ibSkipNext;
    ImageButton ibRepeat;
    ImageButton ibReplay30;
    ImageButton ibForward30;
    ImageButton ibShuffle;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaController != null) {
                long currentPos = mMediaController.getPlaybackState().getPosition();
                if (currentPos == mMediaItemDuration) {
                    mediaPlayerHandler.getMediaTransportControls().stop();
                    mHandler.removeCallbacks(this);
                }
                updateElapsedTime();
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Log.i(LOG_TAG, "onCreate: ");

        tvMediaTitle = findViewById(R.id.tv_mediaTitle);
        tvMediaArtist = findViewById(R.id.tv_mediaArtist);
        tvMediaAlbum = findViewById(R.id.tv_mediaAlbum);
        tvMediaId = findViewById(R.id.tv_mediaId);
        tvMediaElapsedTime = findViewById(R.id.tv_elapsedTime);

        ibPlay = findViewById(R.id.ib_play);
        ibPause = findViewById(R.id.ib_pause);
        ibStop = findViewById(R.id.ib_stop);
        ibSkipPrevious = findViewById(R.id.ib_skipPrevious);
        ibFastRewind = findViewById(R.id.ib_fastRewind);
        ibFastForward = findViewById(R.id.ib_fastForward);
        ibSkipNext = findViewById(R.id.ib_skipNext);
        ibRepeat = findViewById(R.id.ib_repeat);
        ibReplay30 = findViewById(R.id.ib_replay_30);
        ibForward30 = findViewById(R.id.ib_forward_30);
        ibShuffle = findViewById(R.id.ib_shuffle);

        intent = getIntent();
        mMediaItem = intent.getParcelableExtra("mediaItem");

        mediaPlayerHandler = MediaPlayerHandler.getInstance();
        mediaPlayerHandler.setPlayerActivityInstance(PlayerActivity.this);
        mMediaController = mediaPlayerHandler.getMediaController();
        MediaPlayerHandler.getInstance().getMediaTransportControls()
                .playFromMediaId(mMediaItem.getMediaId(), null);

        setPlayerControls();
    }

    private void setPlayerControls() {
        ibPlay.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Play");
            mediaPlayerHandler.getMediaTransportControls().play();
        });

        ibPause.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Pause");
            mediaPlayerHandler.getMediaTransportControls().pause();
        });

        ibStop.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Stop");
            mediaPlayerHandler.getMediaTransportControls().stop();
        });

        ibSkipPrevious.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Skip Previous");
            mediaPlayerHandler.getMediaTransportControls().skipToPrevious();

        });

        ibFastRewind.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Fast Rewind");
            mediaPlayerHandler.getMediaTransportControls().rewind();

        });

        ibFastForward.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Fast Forward");
            mediaPlayerHandler.getMediaTransportControls().fastForward();
        });

        ibSkipNext.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Skip Next");
            mediaPlayerHandler.getMediaTransportControls().skipToNext();
        });

        ibRepeat.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Repeat");

        });

        ibReplay30.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Replay 30");
            // Get the current playback state
            PlaybackStateCompat playbackState = mediaPlayerHandler.getMediaController().getPlaybackState();

            // If playback is not active, do nothing
            if (playbackState == null || playbackState.getState() != PlaybackStateCompat.STATE_PLAYING) {
                return;
            }
            // Calculate the new playback position
            long currentPosition = playbackState.getPosition();
            long newPosition = currentPosition - 30000;

            Log.i(LOG_TAG, "setPlayerControls: Current position -> " + currentPosition);
            Log.i(LOG_TAG, "setPlayerControls: New requested position -> " + newPosition);

            mediaPlayerHandler.getMediaTransportControls().seekTo(newPosition);
        });

        ibForward30.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Forward 30");
            // Get the current playback state
            PlaybackStateCompat playbackState = mediaPlayerHandler.getMediaController().getPlaybackState();

            // If playback is not active, do nothing
            if (playbackState == null || playbackState.getState() != PlaybackStateCompat.STATE_PLAYING) {
                return;
            }

            // Calculate the new playback position
            long currentPosition = playbackState.getPosition();
            long newPosition = currentPosition + 30000;

            Log.i(LOG_TAG, "setPlayerControls: Current position -> " + currentPosition);
            Log.i(LOG_TAG, "setPlayerControls: New requested position -> " + newPosition);

            // Skip to the new position
            mediaPlayerHandler.getMediaController().getTransportControls().seekTo(newPosition);
        });

        ibShuffle.setOnClickListener(v -> {
            Log.i(LOG_TAG, "setPlayerControls: Requested Shuffle");
        });
    }


    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.i(LOG_TAG, "onMetadataChanged: ");
        String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        String mediaArtist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String mediaAlbum = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        long mediaDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)/60;
        String mediaTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);

        mMediaItemDuration = mediaDuration;
        tvMediaTitle.setText(mediaTitle);
        tvMediaArtist.setText(mediaArtist);
        tvMediaAlbum.setText(mediaAlbum);
        tvMediaId.setText(mediaId);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.i(LOG_TAG, "onPlaybackStateChanged: ");

        if (state == null) {
            return;
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING: {
                mHandler.postDelayed(mUpdateTimeRunnable, 1000);
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                mHandler.removeCallbacks(mUpdateTimeRunnable);
                break;
            }
            case PlaybackStateCompat.STATE_STOPPED: {
                mHandler.removeCallbacks(mUpdateTimeRunnable);
                tvMediaElapsedTime.setText("00:00");
                break;
            }
        }
    }

    private void updateElapsedTime() {
        long position = mMediaController.getPlaybackState().getPosition();
        String elapsedTime = DateUtils.formatElapsedTime(position / 1000);
        tvMediaElapsedTime.setText(elapsedTime);
    }

}