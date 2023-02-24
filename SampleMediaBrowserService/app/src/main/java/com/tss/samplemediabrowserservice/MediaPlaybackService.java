package com.tss.samplemediabrowserservice;

import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedMetaData;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaPlaybackService extends MediaBrowserServiceCompat  implements MediaPlayer.OnErrorListener {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    final String LOG_TAG = "TSS-MP Server";

    MyDataSourceProvider mDataSourceProvider;
    AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    MediaPlayer mediaPlayer;
    boolean hasAudioFocus = false;
    private PlaybackStateCompat.Builder stateBuilder;

    private AudioFocusRequest audioFocusRequest;

    AudioManager.OnAudioFocusChangeListener afChangeListener = focusChange -> {
        Log.i(LOG_TAG, "OnAudioFocusChangeListener: " + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS | focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            hasAudioFocus =  false;
            if (mediaPlayer.isPlaying()) {
                Log.i(LOG_TAG, "OnAudioFocusChangeListener: Media player is paused");
                mediaPlayer.pause();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            hasAudioFocus = true;
            Log.i(LOG_TAG, "OnAudioFocusChangeListener: Starting Media player");
            mediaPlayer.start();
        }
    };

    /**
     * To receive callbacks from media session by media controller.
     */
    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
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
            Log.i(LOG_TAG, "onPlay: starting media player");
            mediaPlayer.start();
            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
                    .setActions(PlaybackStateCompat.ACTION_PLAY)
                    .build();
            mediaSession.setPlaybackState(playbackStateCompat);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            Log.i(LOG_TAG, "onPlayFromMediaId: ");

            Uri mediaUri = mDataSourceProvider.getMediaItemUri(mediaId);
            Log.i(LOG_TAG, "onPlayFromMediaId: Media Uri -> " + mediaUri);
            if (mediaUri != null) {
                if (!hasAudioFocus) {
                    Log.i(LOG_TAG, "onPlayFromMediaId: Requesting audio focus");

                    int result = audioManager.requestAudioFocus(audioFocusRequest);
                    Log.i(LOG_TAG, "onPlayFromMediaId: requestAudioFocus -> " + result);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        Log.i(LOG_TAG, "onPlayFromMediaId: Audio focus granted");
                        hasAudioFocus = true;
                    }
                }

                try {
                    if (!mediaSession.isActive()) {
                        mediaSession.setActive(true);
                    }

                    // Reset media player
                    Log.i(LOG_TAG, "onPlayFromMediaId: Media player reset");
                    mediaPlayer.reset();

                    // Set the data source
                    Log.i(LOG_TAG, "onPlayFromMediaId: Setting data source");
                    mediaPlayer.setDataSource(MediaPlaybackService.this, mediaUri);

                    // Prepare the media
                    Log.i(LOG_TAG, "onPlayFromMediaId: Preparing Media player");
                    mediaPlayer.prepare();

                    // Start playback
                    Log.i(LOG_TAG, "onPlayFromMediaId: Starting Media player");
                    mediaPlayer.start();

                    Log.i(LOG_TAG, "onPlayFromMediaId: Setting Metadata");
                    MediaMetadataCompat metadata = mDataSourceProvider.getMediaItemMetadata(mediaId);
                    mediaSession.setMetadata(metadata);

                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), mediaPlayer.getPlaybackParams().getSpeed())
                            .setActions(PlaybackStateCompat.ACTION_PLAY)
                            .build();
                    Log.i(LOG_TAG, "onPlayFromMediaId: Setting Playback state");
                    mediaSession.setPlaybackState(playbackStateCompat);

                } catch (IOException e) {
                    Log.i(LOG_TAG, "onPlayFromMediaId: error while starting the media player");
                    e.printStackTrace();
                    // Handle error
                }
                // Set error listener
                mediaPlayer.setOnErrorListener(MediaPlaybackService.this);
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
            // Update metadata and state
            // pause the player (custom call)
            // mediaPlayer.pause();
            // unregister BECOME_NOISY BroadcastReceiver
            // unregisterReceiver(myNoisyAudioStreamReceiver);
            // Take the service out of the foreground, retain the notification
            // service.stopForeground(false);
            mediaPlayer.pause();
            Log.i(LOG_TAG, "onPause: Media Player is paused");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.i(LOG_TAG, "onStop: ");
            // AudioManager am = (AudioManager) MediaPlaybackService.this.getSystemService(Context.AUDIO_SERVICE);
            // Abandon audio focus
            // am.abandonAudioFocusRequest(audioFocusRequest);
            //unregisterReceiver(myNoisyAudioStreamReceiver);
            // Stop the service
            // service.stopSelf();
            // Set the session inactive  (and update metadata and state)
            // mediaSession.setActive(false);
            // stop the player (custom call)
            // player.stop();
            // Take the service out of the foreground
            // service.stopForeground(false);
            mediaSession.setActive(false);
            mediaPlayer.stop();
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
            Log.i(LOG_TAG, "onSeekTo: ");
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
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate: ");

        mDataSourceProvider = new MyDataSourceProvider();

        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(mediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

        mediaPlayer = new MediaPlayer();

        // Request audio focus for playback, this registers the afChangeListener
        audioManager = (AudioManager) MediaPlaybackService.this.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(afChangeListener)
                .setAudioAttributes(attrs)
                .build();

        Log.i(LOG_TAG, "onCreate: Audio Focus instance created " + audioFocusRequest);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy: Releasing media player");
        mediaPlayer.release();

        Log.i(LOG_TAG, "onDestroy: Releasing audio focus");
        audioManager.abandonAudioFocusRequest(audioFocusRequest);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.i(LOG_TAG, "onGetRoot: client-id -> " + clientPackageName + " client-uid -> " + clientUid);
        mediaSession.setActive(true);
        Log.i(LOG_TAG, "onGetRoot: Media Session is active");
        return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.i(LOG_TAG, "onLoadChildren: parent id -> " + parentId);
        //  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        // Assume for example that the music catalog is already loaded/cached.
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
            mediaItems.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setMediaId("MEDIA_ID_MUSICS_BY_SONGS")
                            .setTitle("Browse Songs")
                            .setIconUri(Uri.parse("android.resource://com.tss.samplemediabrowserservice/drawable/ic_play"))
                            .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ));
            mediaItems.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setMediaId("MEDIA_ID_MUSICS_BY_ALBUM")
                            .setTitle("Browse Album")
                            .setIconUri(Uri.parse("android.resource://com.tss.samplemediabrowserservice/drawable/ic_play"))
                            .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ));
            mediaItems.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setMediaId("MEDIA_ID_MUSICS_BY_GENRE")
                            .setTitle("Browse Genre")
                            .setIconUri(Uri.parse("android.resource://com.tss.samplemediabrowserservice/drawable/ic_play"))
                            .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ));
            mediaItems.add(new MediaBrowserCompat.MediaItem(
                    new MediaDescriptionCompat.Builder()
                            .setMediaId("MEDIA_ID_MUSICS_BY_ARTIST")
                            .setTitle("Browse Artist")
                            .setIconUri(Uri.parse("android.resource://com.tss.samplemediabrowserservice/drawable/ic_play"))
                            .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ));

        } else if (parentId.equals("MEDIA_ID_MUSICS_BY_SONGS")){
            Log.i(LOG_TAG, "onLoadChildren: Getting songs...");
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
            //String selection = "is_music != 0 AND title != ''";

            // Display audios in alphabetical order based on their display name.
            try {
                Log.i(LOG_TAG, "onLoadChildren: URI -> " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

                String[] projection = new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.DATE_MODIFIED,
                };
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = getApplicationContext().getContentResolver()
                        .query(uri,
                                projection, null, null, null);

                if (cursor != null && cursor.getCount() > 0) {
                    Log.i(LOG_TAG, "onLoadChildren: Query results:");
                    while (cursor.moveToNext()) {
                        // Use an ID column from the projection to get
                        // a URI representing the media item itself.
                        MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)))
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)))
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)))
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))
                                .build();

                        MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaMetadata.getDescription(),
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                        mediaItems.add(mediaItem);

                        Uri mediaUri = mediaItem.getDescription().getMediaUri();
                        Log.i(LOG_TAG, "onLoadChildren: Adding media uri to data source: " + mediaUri);
                        mDataSourceProvider.addMediaItemUri(mediaItem.getMediaId(), mediaUri);
                        mDataSourceProvider.addMediaItemMetadata(mediaItem.getMediaId(), mediaMetadata);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "onLoadChildren: Exception while querying albums");
                e.printStackTrace();
            }

        }
        result.sendResult(mediaItems);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public static class MyDataSourceProvider {

        // Map of media IDs to media metadata
        private final Map<String, MediaMetadataCompat> mMediaMetadataMap = new HashMap<>();
        // Map of media IDs to media URIs
        private final Map<String, Uri> mMediaUriMap = new HashMap<>();

        // Add a new media item to the data source provider
        public void addMediaItemUri(String mediaId, Uri uri) {
            mMediaUriMap.put(mediaId, uri);
        }

        // Get the media URI for the specified media ID
        public Uri getMediaItemUri(String mediaId) {
            return mMediaUriMap.get(mediaId);
        }

        // Add a new media item to the data source provider
        public void addMediaItemMetadata(String mediaId, MediaMetadataCompat metadata) {
            mMediaMetadataMap.put(mediaId, metadata);
        }

        // Get the media URI for the specified media ID
        public MediaMetadataCompat getMediaItemMetadata(String mediaId) {
            return mMediaMetadataMap.get(mediaId);
        }
    }
}
