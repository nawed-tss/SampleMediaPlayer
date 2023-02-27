package com.tss.samplemediabrowserclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

public class MediaPlayerHandler {
    String LOG_TAG = "TSS-MP MediaPlayerHandler";
    private static MediaPlayerHandler sInstance = null;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    private MediaPlayerComm mMediaPlayerComm;
    protected MediaItemMetadataInterface mMediaItemMetadataInterface;

    /**
     * To receive callbacks from the media session every time its state or metadata change.
     */
    MediaControllerCompat.Callback mediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.i(LOG_TAG, "onMetadataChanged: ");
                    Log.i(LOG_TAG, "onMetadataChanged: MEDIA ID -> " + metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                    Log.i(LOG_TAG, "onMetadataChanged: ARTIST -> " + metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                    Log.i(LOG_TAG, "onMetadataChanged: ALBUM -> " + metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
                    Log.i(LOG_TAG, "onMetadataChanged: DURATION -> " + metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                    Log.i(LOG_TAG, "onMetadataChanged: DISPLAY TITLE -> " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE));
                    Log.i(LOG_TAG, "onMetadataChanged: KEY TITLE -> " + metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));

                    mMediaItemMetadataInterface.onMetadataChanged(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    Log.i(LOG_TAG, "onPlaybackStateChanged: PLAYBACK STATE -> " + state);
                }

                @Override
                public void onSessionDestroyed() {
                    // Disconnects from Media Browser Service i.e MediaPlaybackService
                    Log.i(LOG_TAG, "onSessionDestroyed: Session destroyed");
                    mediaBrowser.disconnect();
                }

                @Override
                public void onSessionReady() {
                    Log.i(LOG_TAG, "onSessionReady: Session ready");
                }

            };

    /**
     * To receive connection callbacks from the media browser service.
     */
    MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    // Get the token for the MediaSession
                    Log.i(LOG_TAG, "onConnected: MediaBrowserService connected");

                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                    Log.i(LOG_TAG, "onConnected: Session token -> " + token);

                    mMediaPlayerComm.onConnected(token);
//                    // Finish building the UI
//                    //buildTransportControls();
                    subscribeToRootId(mediaBrowser.getRoot());
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                    Log.i(LOG_TAG, "onConnectionSuspended: Connection suspended");
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                    Log.i(LOG_TAG, "onConnectionFailed: Connection failed");
                }

            };

    /**
     * To receive subscription callbacks for the media items from MediaBrowser
     */
    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.i(LOG_TAG, "onChildrenLoaded: parent id -> " + parentId);
            Log.i(LOG_TAG, "onChildrenLoaded: Children -> " + children);
            mMediaPlayerComm.onChildrenLoaded(children);
        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
            Log.e(LOG_TAG, "onError: ");
        }
    };


    private MediaPlayerHandler() {
        mMediaPlayerComm = new MediaPlayerActivity();
        mMediaItemMetadataInterface = new PlayerActivity();
    }

    public static MediaPlayerHandler getInstance() {
        if (sInstance == null) {
            sInstance = new MediaPlayerHandler();
        }
        return sInstance;
    }

    public void initializeMediaController(Activity activity, MediaSessionCompat.Token token) {
        Log.i(LOG_TAG, "initializeMediaController: Initializing Media Controller");
        // Create a MediaControllerCompat
        mediaController =
                new MediaControllerCompat(activity, token);

        // Register a Callback to stay in sync
        Log.i(LOG_TAG, "initializeMediaController: mediaControllerCallback registered");
        mediaController.registerCallback(mediaControllerCallback);

        // Save the controller
        Log.i(LOG_TAG, "initializeMediaController: Setting Media controller");
        MediaControllerCompat.setMediaController(activity, mediaController);

    }

    public void initializeMediaBrowser(Context context) {
        Log.i(LOG_TAG, "initializeMediaBrowser: Initializing Media Browser");
        mediaBrowser = new MediaBrowserCompat(context.getApplicationContext(),
                new ComponentName("com.tss.samplemediabrowserservice",
                        "com.tss.samplemediabrowserservice.MediaPlaybackService"),
                connectionCallbacks,
                null);
    }

    public void connectToService() {
        Log.i(LOG_TAG, "connectToService: ");
        if (!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
            Log.i(LOG_TAG, "connectToService: Connected to Service");
        } else {
            Log.i(LOG_TAG, "connectToService: Already connected to Service");
        }
    }

    public void deInitialize() {
        Log.i(LOG_TAG, "deInitialize: ");
        mediaController.getTransportControls().stop();
        mediaController.unregisterCallback(mediaControllerCallback);
        mediaBrowser.disconnect();
    }

    public void subscribeToRootId(String rootId) {
        Log.i(LOG_TAG, "subscribeToRootId: subscribed to Root ID -> " + rootId);
        mediaBrowser.subscribe(rootId, subscriptionCallback);
    }

    public MediaControllerCompat.TransportControls getMediaTransportControls() {
        Log.i(LOG_TAG, "getMediaTransportControls: ");
        return mediaController.getTransportControls();
    }
}
