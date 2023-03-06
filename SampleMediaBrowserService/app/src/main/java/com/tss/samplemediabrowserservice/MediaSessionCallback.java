package com.tss.samplemediabrowserservice;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    final String LOG_TAG = "MP-Server MediaSessionCallback";

    private Context mContext;

    private MediaDataSourceProvider mMediaDataSourceProvider;
    private MediaPlaybackHelper mMediaPlaybackHelper;
    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;

    boolean lastPlayed = false;

    MediaSessionCallback(Context context) {
        Log.i(LOG_TAG, "MediaSessionCallback: ");
        mContext = context;
        mMediaDataSourceProvider = MediaDataSourceProvider.getInstance();
        mMediaPlaybackHelper = MediaPlaybackHelper.getInstance();
        mMediaSession = mMediaPlaybackHelper.getMediaSession();
        mMediaPlayer = mMediaPlaybackHelper.getMediaPlayer();
    }

    @Override
    public void onCommand(String command, Bundle extras, ResultReceiver cb) {
        super.onCommand(command, extras, cb);
        Log.i(LOG_TAG, "onCommand: ");
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        Log.i(LOG_TAG, "onMediaButtonEvent: ");
        return super.onMediaButtonEvent(mediaButtonEvent);
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
        Log.i(LOG_TAG, "onPrepare: ");
    }

    @Override
    public void onPrepareFromMediaId(String mediaId, Bundle extras) {
        super.onPrepareFromMediaId(mediaId, extras);
        Log.i(LOG_TAG, "onPrepareFromMediaId: ");
    }

    @Override
    public void onPrepareFromSearch(String query, Bundle extras) {
        super.onPrepareFromSearch(query, extras);
        Log.i(LOG_TAG, "onPrepareFromSearch: ");
    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {
        super.onPrepareFromUri(uri, extras);
        Log.i(LOG_TAG, "onPrepareFromUri: ");
    }

    @Override
    public void onPlay() {
        super.onPlay();
        PlaybackStateCompat playbackStateCompat;

        Log.i(LOG_TAG, "onPlay: Starting Media Player");
        if (!mMediaSession.isActive()) {
            mMediaSession.setActive(true);
        }

        if (mMediaSession.getController().getMetadata() == null) {
            Log.e(LOG_TAG, "onPlay: No Media Data Source Available");
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            Log.i(LOG_TAG, "onPlay: Already playing");
            return;
        }

        if (!mMediaPlaybackHelper.hasAudioFocus) {
            Log.i(LOG_TAG, "onPlay: Requesting Audio Focus");
            mMediaPlaybackHelper.requestAudioFocus();
        }

        switch (mMediaSession.getController().getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_PLAYING: {
                Log.i(LOG_TAG, "onPlay: Media is already playing");
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                Log.i(LOG_TAG, "onPlay: Media Player Resumed");
                mMediaPlayer.start();
                playbackStateCompat = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mMediaPlayer.getCurrentPosition(), mMediaPlayer.getPlaybackParams().getSpeed())
                        .setActions(PlaybackStateCompat.ACTION_PAUSE)
                        .build();
                mMediaSession.setPlaybackState(playbackStateCompat);
                break;
            }
            case PlaybackStateCompat.STATE_STOPPED: {
                Log.i(LOG_TAG, "onPlay: Media Player Started");
                try {
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    playbackStateCompat = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                            .setActions(PlaybackStateCompat.ACTION_STOP)
                            .build();
                    mMediaSession.setPlaybackState(playbackStateCompat);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            default:
        }
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
        Log.i(LOG_TAG, "onPlayFromMediaId: ");

        lastPlayed = true;
        Uri mediaUri = mMediaDataSourceProvider.getMediaItemUri(mediaId);
        Log.i(LOG_TAG, "onPlayFromMediaId: Media Uri -> " + mediaUri);
        if (mediaUri != null) {

            try {

                if (!mMediaSession.isActive()) {
                    mMediaSession.setActive(true);
                }

                if (!mMediaPlaybackHelper.hasAudioFocus) {
                    Log.i(LOG_TAG, "onPlay: Requesting Audio Focus");
                    mMediaPlaybackHelper.requestAudioFocus();
                }

                Log.i(LOG_TAG, "onPlayFromMediaId: Setting Metadata");
                MediaMetadataCompat metadata = mMediaDataSourceProvider.getMediaItemMetadata(mediaId);
                mMediaSession.setMetadata(metadata);

                // Reset media player
                Log.i(LOG_TAG, "onPlayFromMediaId: Media player reset");
                mMediaPlayer.reset();

                // Set the data source
                Log.i(LOG_TAG, "onPlayFromMediaId: Setting data source");
                mMediaPlayer.setDataSource(mContext, mediaUri);

                // Prepare the media
                Log.i(LOG_TAG, "onPlayFromMediaId: Preparing Media player");
                mMediaPlayer.prepare();

                // Start playback
                Log.i(LOG_TAG, "onPlayFromMediaId: Starting Media player");
                mMediaPlayer.start();

                PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY)
                        .build();
                Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
                mMediaSession.setPlaybackState(playbackStateCompat);

            } catch (IOException e) {
                Log.i(LOG_TAG, "onPlayFromMediaId: error while starting the media player");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
        super.onPlayFromSearch(query, extras);
        Log.i(LOG_TAG, "onPlayFromSearch: ");
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        Log.i(LOG_TAG, "onPlayFromUri: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause: ");
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, mMediaPlayer.getCurrentPosition(), mMediaPlayer.getPlaybackParams().getSpeed())
                    .setActions(PlaybackStateCompat.ACTION_PAUSE)
                    .build();
            Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
            mMediaSession.setPlaybackState(playbackStateCompat);
            Log.i(LOG_TAG, "onPause: Media Player is paused");
        } else {
            Log.i(LOG_TAG, "onPause: Media player is not playing");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop: ");
        mMediaPlaybackHelper.abandonAudioFocus();
        mMediaSession.setActive(false);
        mMediaPlayer.stop();

        PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_STOP)
                .build();
        Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
        mMediaSession.setPlaybackState(playbackStateCompat);
        Log.i(LOG_TAG, "onStop: Media Session and Media Player is stopped");

    }

    @Override
    public void onSkipToQueueItem(long id) {
        super.onSkipToQueueItem(id);
        Log.i(LOG_TAG, "onSkipToQueueItem: ");
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        Log.i(LOG_TAG, "onSkipToNext: ");
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        Log.i(LOG_TAG, "onSkipToPrevious: ");
    }

    @Override
    public void onFastForward() {
        super.onFastForward();
        Log.i(LOG_TAG, "onFastForward: ");
    }

    @Override
    public void onRewind() {
        super.onRewind();
        Log.i(LOG_TAG, "onRewind: ");
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        if (mMediaPlayer != null) {
            Log.i(LOG_TAG, "onSeekTo: Requested pos -> " + pos);
            int duration = mMediaPlayer.getDuration();
            Log.i(LOG_TAG, "onSeekTo: Media Duration -> " + duration);
            int seekPos = (int) Math.min(pos, duration);
            Log.i(LOG_TAG, "onSeekTo: Passed pos -> " + pos);
            mMediaPlayer.seekTo(seekPos, MediaPlayer.SEEK_CLOSEST_SYNC);

            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, pos, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build();
            mMediaSession.setPlaybackState(playbackStateCompat);

        }
    }


    @Override
    public void onSetRating(RatingCompat rating) {
        super.onSetRating(rating);
        Log.i(LOG_TAG, "onSetRating: ");
    }

    @Override
    public void onSetRating(RatingCompat rating, Bundle extras) {
        super.onSetRating(rating, extras);
        Log.i(LOG_TAG, "onSetRating: ");
    }

    @Override
    public void onSetPlaybackSpeed(float speed) {
        super.onSetPlaybackSpeed(speed);
        Log.i(LOG_TAG, "onSetPlaybackSpeed: ");
    }

    @Override
    public void onSetCaptioningEnabled(boolean enabled) {
        super.onSetCaptioningEnabled(enabled);
        Log.i(LOG_TAG, "onSetCaptioningEnabled: ");
    }

    @Override
    public void onSetRepeatMode(int repeatMode) {
        super.onSetRepeatMode(repeatMode);
        Log.i(LOG_TAG, "onSetRepeatMode: ");
    }

    @Override
    public void onSetShuffleMode(int shuffleMode) {
        super.onSetShuffleMode(shuffleMode);
        Log.i(LOG_TAG, "onSetShuffleMode: ");
    }

    @Override
    public void onCustomAction(String action, Bundle extras) {
        super.onCustomAction(action, extras);
        Log.i(LOG_TAG, "onCustomAction: ");
    }

    @Override
    public void onAddQueueItem(MediaDescriptionCompat description) {
        super.onAddQueueItem(description);
        Log.i(LOG_TAG, "onAddQueueItem: ");
    }

    @Override
    public void onAddQueueItem(MediaDescriptionCompat description, int index) {
        super.onAddQueueItem(description, index);
        Log.i(LOG_TAG, "onAddQueueItem: ");
    }

    @Override
    public void onRemoveQueueItem(MediaDescriptionCompat description) {
        super.onRemoveQueueItem(description);
        Log.i(LOG_TAG, "onRemoveQueueItem: ");
    }

}
