package com.tss.samplemediabrowserservice;

import static com.tss.samplemediabrowserservice.MediaPlaybackConstants.ALBUMS_ID;
import static com.tss.samplemediabrowserservice.MediaPlaybackConstants.ARTISTS_ID;
import static com.tss.samplemediabrowserservice.MediaPlaybackConstants.FOLDERS_ID;
import static com.tss.samplemediabrowserservice.MediaPlaybackConstants.GENRES_ID;
import static com.tss.samplemediabrowserservice.MediaPlaybackConstants.ROOT_ID;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    final String LOG_TAG = "MP-Server MediaPlaybackService";

    private BrowserRoot mBrowseRoot;
    private final List<MediaBrowserCompat.MediaItem> mRootItems = new ArrayList<>();

    private MediaDataSourceProvider mMediaDataSourceProvider;
    private MediaPlaybackHelper mMediaPlaybackHelper;
    private String mLastCategory = "";


//    /**
//     * To receive callbacks from media session by media controller.
//     */
//    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
//        @Override
//        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
//            super.onCommand(command, extras, cb);
//            Log.i(LOG_TAG, "onCommand: ");
//        }
//
//        @Override
//        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
//            Log.i(LOG_TAG, "onMediaButtonEvent: ");
//            return super.onMediaButtonEvent(mediaButtonEvent);
//        }
//
//        @Override
//        public void onPrepare() {
//            super.onPrepare();
//            Log.i(LOG_TAG, "onPrepare: ");
//        }
//
//        @Override
//        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
//            super.onPrepareFromMediaId(mediaId, extras);
//            Log.i(LOG_TAG, "onPrepareFromMediaId: ");
//        }
//
//        @Override
//        public void onPrepareFromSearch(String query, Bundle extras) {
//            super.onPrepareFromSearch(query, extras);
//            Log.i(LOG_TAG, "onPrepareFromSearch: ");
//        }
//
//        @Override
//        public void onPrepareFromUri(Uri uri, Bundle extras) {
//            super.onPrepareFromUri(uri, extras);
//            Log.i(LOG_TAG, "onPrepareFromUri: ");
//        }
//
//        @Override
//        public void onPlay() {
//            super.onPlay();
//            Log.i(LOG_TAG, "onPlay: starting media player");
//            if (!mediaSession.isActive()) {
//                mediaSession.setActive(true);
//            }
//
//            if (mediaPlayer.isPlaying()) {
//                Log.i(LOG_TAG, "onPlay: Already playing");
//                return;
//            }
//
//            if (playerState == PlayerState.PAUSED) {
//                mediaPlayer.start();
//            }
//
//            if (playerState == PlayerState.STOPPED) {
//                requestForAudioFocus();
//                try {
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            playerState = PlayerState.PLAYING;
//            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
//                    .setActions(PlaybackStateCompat.ACTION_PLAY)
//                    .build();
//            mediaSession.setPlaybackState(playbackStateCompat);
//        }
//
//        @Override
//        public void onPlayFromMediaId(String mediaId, Bundle extras) {
//            super.onPlayFromMediaId(mediaId, extras);
//            Log.i(LOG_TAG, "onPlayFromMediaId: ");
//
//            Uri mediaUri = mDataSourceProvider.getMediaItemUri(mediaId);
//            Log.i(LOG_TAG, "onPlayFromMediaId: Media Uri -> " + mediaUri);
//            if (mediaUri != null) {
//
//                try {
//
//                    if (!mediaSession.isActive()) {
//                        mediaSession.setActive(true);
//                    }
//
//                    requestForAudioFocus();
//
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Setting Metadata");
//                    MediaMetadataCompat metadata = mDataSourceProvider.getMediaItemMetadata(mediaId);
//                    mediaSession.setMetadata(metadata);
//
//                    // Reset media player
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Media player reset");
//                    mediaPlayer.reset();
//
//                    // Set the data source
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Setting data source");
//                    mediaPlayer.setDataSource(MediaPlaybackService.this, mediaUri);
//
//                    // Prepare the media
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Preparing Media player");
//                    mediaPlayer.prepare();
//
//                    // Start playback
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Starting Media player");
//                    mediaPlayer.start();
//
//                    CURRENT_PLAYING_MEDIA_INDEX = mDataSourceProvider.getMediaItemIndex(mediaId);
//
//                    playerState = PlayerState.PLAYING;
//                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
//                            .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
//                            .setActions(PlaybackStateCompat.ACTION_PLAY)
//                            .build();
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
//                    mediaSession.setPlaybackState(playbackStateCompat);
//
//                } catch (IOException e) {
//                    Log.i(LOG_TAG, "onPlayFromMediaId: error while starting the media player");
//                    e.printStackTrace();
//                    // Handle error
//                }
//                // Set error listener
//                mediaPlayer.setOnErrorListener(MediaPlaybackService.this);
//            }
//        }
//
//        void requestForAudioFocus() {
//            if (!hasAudioFocus) {
//                Log.i(LOG_TAG, "onPlayFromMediaId: Requesting audio focus");
//
//                int result = audioManager.requestAudioFocus(audioFocusRequest);
//                Log.i(LOG_TAG, "onPlayFromMediaId: requestAudioFocus -> " + result);
//
//                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                    Log.i(LOG_TAG, "onPlayFromMediaId: Audio focus granted");
//                    hasAudioFocus = true;
//                }
//            }
//        }
//
//        @Override
//        public void onPlayFromSearch(String query, Bundle extras) {
//            super.onPlayFromSearch(query, extras);
//            Log.i(LOG_TAG, "onPlayFromSearch: ");
//        }
//
//        @Override
//        public void onPlayFromUri(Uri uri, Bundle extras) {
//            super.onPlayFromUri(uri, extras);
//            Log.i(LOG_TAG, "onPlayFromUri: ");
//        }
//
//        @Override
//        public void onPause() {
//            super.onPause();
//            Log.i(LOG_TAG, "onPause: ");
//            // Update metadata and state
//            // pause the player (custom call)
//            // mediaPlayer.pause();
//            // unregister BECOME_NOISY BroadcastReceiver
//            // unregisterReceiver(myNoisyAudioStreamReceiver);
//            // Take the service out of the foreground, retain the notification
//            // service.stopForeground(false);
//            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.pause();
//                playerState = PlayerState.PAUSED;
//                PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
//                        .setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
//                        .setActions(PlaybackStateCompat.ACTION_PAUSE)
//                        .build();
//                Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
//                mediaSession.setPlaybackState(playbackStateCompat);
//                Log.i(LOG_TAG, "onPause: Media Player is paused");
//            } else {
//                Log.i(LOG_TAG, "onPause: Media player is not playing");
//            }
//        }
//
//        @Override
//        public void onStop() {
//            super.onStop();
//            Log.i(LOG_TAG, "onStop: ");
//            // AudioManager am = (AudioManager) MediaPlaybackService.this.getSystemService(Context.AUDIO_SERVICE);
//            // Abandon audio focus
//            // am.abandonAudioFocusRequest(audioFocusRequest);
//            //unregisterReceiver(myNoisyAudioStreamReceiver);
//            // Stop the service
//            // service.stopSelf();
//            // Set the session inactive  (and update metadata and state)
//            // mediaSession.setActive(false);
//            // stop the player (custom call)
//            // player.stop();
//            // Take the service out of the foreground
//            // service.stopForeground(false);
//
//            audioManager.abandonAudioFocusRequest(audioFocusRequest);
//            mediaSession.setActive(false);
//            mediaPlayer.stop();
//            Log.i(LOG_TAG, "onStop: Position ->" + mediaPlayer.getCurrentPosition());
//
//            playerState = PlayerState.STOPPED;
//            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
//                    .setActions(PlaybackStateCompat.ACTION_STOP)
//                    .build();
//            Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
//            mediaSession.setPlaybackState(playbackStateCompat);
//            Log.i(LOG_TAG, "onStop: Media Session and Media Player is stopped");
//
//        }
//
//        @Override
//        public void onSkipToQueueItem(long id) {
//            super.onSkipToQueueItem(id);
//            Log.i(LOG_TAG, "onSkipToQueueItem: ");
//        }
//
//        @Override
//        public void onSkipToNext() {
//            super.onSkipToNext();
//            Log.i(LOG_TAG, "onSkipToNext: ");
//            if (mediaPlayer != null) {
//
//                if (!mediaSession.isActive()) {
//                    mediaSession.setActive(true);
//                }
//
//                requestForAudioFocus();
//
//                int currentIndex = CURRENT_PLAYING_MEDIA_INDEX;
//                int nextIndex = (currentIndex + 1) % mDataSourceProvider.getMediaItemList().size();
//
//                Log.i(LOG_TAG, "onPlayFromMediaId: Setting Metadata");
//                MediaMetadataCompat metadata = mDataSourceProvider.getMediaItemMetadata(mDataSourceProvider.getMediaId(nextIndex));
//                mediaSession.setMetadata(metadata);
//
//                mediaPlayer.reset();
//                try {
//                    mediaPlayer.setDataSource(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//
//                    playerState = PlayerState.PLAYING;
//                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
//                            .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
//                            .setActions(PlaybackStateCompat.ACTION_PLAY)
//                            .build();
//                    mediaSession.setPlaybackState(playbackStateCompat);
//
//                    CURRENT_PLAYING_MEDIA_INDEX = nextIndex;
//
//                } catch (IOException e) {
//                    Log.i(LOG_TAG, "onSkipToNext: Failed to play next media -> " + e);
//                }
//            }
//        }
//
//        @Override
//        public void onSkipToPrevious() {
//            super.onSkipToPrevious();
//            Log.i(LOG_TAG, "onSkipToPrevious: ");
//            if (mediaPlayer != null) {
//
//                if (!mediaSession.isActive()) {
//                    mediaSession.setActive(true);
//                }
//
//                requestForAudioFocus();
//
//                int currentIndex = CURRENT_PLAYING_MEDIA_INDEX;
//                int nextIndex = (currentIndex - 1) % mDataSourceProvider.getMediaItemList().size();
//
//                Log.i(LOG_TAG, "onPlayFromMediaId: Setting Metadata");
//                MediaMetadataCompat metadata = mDataSourceProvider.getMediaItemMetadata(mDataSourceProvider.getMediaId(nextIndex));
//                mediaSession.setMetadata(metadata);
//
//                mediaPlayer.reset();
//                try {
//                    mediaPlayer.setDataSource(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//
//                    playerState = PlayerState.PLAYING;
//                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
//                            .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
//                            .setActions(PlaybackStateCompat.ACTION_PLAY)
//                            .build();
//                    mediaSession.setPlaybackState(playbackStateCompat);
//
//                    CURRENT_PLAYING_MEDIA_INDEX = nextIndex;
//
//                } catch (IOException e) {
//                    Log.i(LOG_TAG, "onSkipToNext: Failed to play next media -> " + e);
//                }
//            }
//        }
//
//        @Override
//        public void onFastForward() {
//            super.onFastForward();
//            Log.i(LOG_TAG, "onFastForward: ");
//            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                int currentPosition = mediaPlayer.getCurrentPosition();
//                int duration = mediaPlayer.getDuration();
//                int fastForwardTime = Math.min(currentPosition + 10000, duration);
//                mediaPlayer.seekTo(fastForwardTime);
//            }
//        }
//
//        @Override
//        public void onRewind() {
//            super.onRewind();
//            Log.i(LOG_TAG, "onRewind: ");
//            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                int currentPosition = mediaPlayer.getCurrentPosition();
//                int duration = mediaPlayer.getDuration();
//                int rewindTime = Math.max(currentPosition - 10000, duration);
//                mediaPlayer.seekTo(rewindTime);
//            }
//        }
//
//        @Override
//        public void onSeekTo(long pos) {
//            super.onSeekTo(pos);
//            Log.i(LOG_TAG, "onSeekTo: " + pos);
//            if (mediaPlayer != null) {
//                int duration = mediaPlayer.getDuration();
//                Log.i(LOG_TAG, "onSeekTo: Media Duration -> " + duration);
////                int seekPos = Math.min((int) pos, duration);
//                mediaPlayer.seekTo((int) pos);
//            }
//        }
//
//
//        @Override
//        public void onSetRating(RatingCompat rating) {
//            super.onSetRating(rating);
//            Log.i(LOG_TAG, "onSetRating: ");
//        }
//
//        @Override
//        public void onSetRating(RatingCompat rating, Bundle extras) {
//            super.onSetRating(rating, extras);
//            Log.i(LOG_TAG, "onSetRating: ");
//        }
//
//        @Override
//        public void onSetPlaybackSpeed(float speed) {
//            super.onSetPlaybackSpeed(speed);
//            Log.i(LOG_TAG, "onSetPlaybackSpeed: ");
//        }
//
//        @Override
//        public void onSetCaptioningEnabled(boolean enabled) {
//            super.onSetCaptioningEnabled(enabled);
//            Log.i(LOG_TAG, "onSetCaptioningEnabled: ");
//        }
//
//        @Override
//        public void onSetRepeatMode(int repeatMode) {
//            super.onSetRepeatMode(repeatMode);
//            Log.i(LOG_TAG, "onSetRepeatMode: ");
//        }
//
//        @Override
//        public void onSetShuffleMode(int shuffleMode) {
//            super.onSetShuffleMode(shuffleMode);
//            Log.i(LOG_TAG, "onSetShuffleMode: ");
//        }
//
//        @Override
//        public void onCustomAction(String action, Bundle extras) {
//            super.onCustomAction(action, extras);
//            Log.i(LOG_TAG, "onCustomAction: ");
//        }
//
//        @Override
//        public void onAddQueueItem(MediaDescriptionCompat description) {
//            super.onAddQueueItem(description);
//            Log.i(LOG_TAG, "onAddQueueItem: ");
//        }
//
//        @Override
//        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
//            super.onAddQueueItem(description, index);
//            Log.i(LOG_TAG, "onAddQueueItem: ");
//        }
//
//        @Override
//        public void onRemoveQueueItem(MediaDescriptionCompat description) {
//            super.onRemoveQueueItem(description);
//            Log.i(LOG_TAG, "onRemoveQueueItem: ");
//        }
//    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate: ");

        Log.i(LOG_TAG, "onCreate: Initializing Media Data Source Provider");
        mMediaDataSourceProvider = MediaDataSourceProvider.getInstance();
        mMediaDataSourceProvider.initialize(this);
        
        mBrowseRoot = new BrowserRoot(ROOT_ID, null);

        Log.i(LOG_TAG, "onCreate: Root Media Items Attached");
        addRootItems();

        // Initialize Media Session
        Log.i(LOG_TAG, "onCreate: Initializing Media Session");
        mMediaPlaybackHelper = MediaPlaybackHelper.getInstance();
        mMediaPlaybackHelper.initializeMediaSession(this);
        
        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaPlaybackHelper.getMediaSession().getSessionToken());

        // Request audio focus for playback, this registers the afChangeListener
        mMediaPlaybackHelper.initializeAudioManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy: Disabling Media Session");
        mMediaPlaybackHelper.getMediaSession().setActive(false);

        Log.i(LOG_TAG, "onDestroy: Stopping Playback");
        mMediaPlaybackHelper.stopPlayback();

        Log.i(LOG_TAG, "onDestroy: Releasing media player");
        mMediaPlaybackHelper.getMediaPlayer().release();

        Log.i(LOG_TAG, "onDestroy: Releasing audio focus");
        mMediaPlaybackHelper.abandonAudioFocus();
    }

    private void addRootItems() {
        // TODO: Set Icon Uri later

        MediaDescriptionCompat folders = new MediaDescriptionCompat.Builder()
                .setMediaId(FOLDERS_ID)
                .setTitle("Folders")
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(folders, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaDescriptionCompat albums = new MediaDescriptionCompat.Builder()
                .setMediaId(ALBUMS_ID)
                .setTitle("Albums")
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(albums, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaDescriptionCompat artists = new MediaDescriptionCompat.Builder()
                .setMediaId(ARTISTS_ID)
                .setTitle("Artists")
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(artists, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaDescriptionCompat genres = new MediaDescriptionCompat.Builder()
                .setMediaId(GENRES_ID)
                .setTitle("Genres")
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(genres, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.i(LOG_TAG, "onGetRoot: client-id -> " + clientPackageName + " client-uid -> " + clientUid);
        mMediaPlaybackHelper.getMediaSession().setActive(true);
        Log.i(LOG_TAG, "onGetRoot: Media Session is active");
        return mBrowseRoot;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.i(LOG_TAG, "onLoadChildren: parent id -> " + parentId);
        //  Browsing not allowed

        List<MediaBrowserCompat.MediaItem> mediaItemList = new ArrayList<>();

        switch (parentId) {
            case ROOT_ID:
                result.sendResult(mRootItems);
                mLastCategory = parentId;
                break;
            case FOLDERS_ID:
                mMediaDataSourceProvider.onQueryByFolder(parentId, result);
                mLastCategory = parentId;
                break;
            case ALBUMS_ID:
                //mDataModel.onQueryByAlbum(parentId, result);
                mLastCategory = parentId;
                break;
            case ARTISTS_ID:
                //mDataModel.onQueryByArtist(parentId, result);
                mLastCategory = parentId;
                break;
            case GENRES_ID:
                //mDataModel.onQueryByGenres(parentId, result);
                mLastCategory = parentId;
                break;
            default:
                Log.i(LOG_TAG, "onLoadChildren: onQueryByKey");
                result.sendResult(mMediaDataSourceProvider.onQueryByKey(parentId, result));
        }
    }
}
