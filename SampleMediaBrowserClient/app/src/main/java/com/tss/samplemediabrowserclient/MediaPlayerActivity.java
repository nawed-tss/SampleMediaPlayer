package com.tss.samplemediabrowserclient;

import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaPlayerActivity extends AppCompatActivity {
    final String LOG_TAG = "TSS-MP Client";
    private ImageView playPause;
    private RecyclerView recyclerView_RootItems;
    private RootItemListAdapter rootItemListAdapter;

    private MediaBrowserCompat mediaBrowser;
    private  MediaControllerCompat mediaController;

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

                    // Create a MediaControllerCompat
                    mediaController =
                            new MediaControllerCompat(MediaPlayerActivity.this, token);

                    // Register a Callback to stay in sync
                    Log.i(LOG_TAG, "onCreate: mediaControllerCallback registered");
                    mediaController.registerCallback(mediaControllerCallback);

                    // Save the controller
                    Log.i(LOG_TAG, "onConnected: Setting Media controller");
                    MediaControllerCompat.setMediaController(MediaPlayerActivity.this, mediaController);

                    // Finish building the UI
                    //buildTransportControls();
                    String rootId = mediaBrowser.getRoot();
                    Log.i(LOG_TAG, "onConnected: Root ID -> " + rootId);
                    mediaBrowser.subscribe(rootId, subscriptionCallback);
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
    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.i(LOG_TAG, "onChildrenLoaded: parent id -> " + parentId);
            Log.i(LOG_TAG, "onChildrenLoaded: Children -> " + children);
            if (!children.isEmpty()) {
                rootItemListAdapter = new RootItemListAdapter(mediaBrowser, subscriptionCallback, mediaController, children);
                recyclerView_RootItems.setHasFixedSize(true);
                recyclerView_RootItems.setLayoutManager(new LinearLayoutManager(MediaPlayerActivity.this));
                recyclerView_RootItems.setAdapter(rootItemListAdapter);
                rootItemListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
            Log.e(LOG_TAG, "onError: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediaplayer);

        recyclerView_RootItems = findViewById(R.id.recyclerView_rootItems);

        // Create MediaBrowserServiceCompat
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName("com.tss.samplemediabrowserservice",
                        "com.tss.samplemediabrowserservice.MediaPlaybackService"),
                connectionCallbacks,
                null); // optional Bundle
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect to Media Browser Service i.e MediaPlaybackService
        Log.i(LOG_TAG, "onStart: Connecting to service");
        if (!mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume: ");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        // (see "stay in sync with the MediaSession")
        Log.i(LOG_TAG, "onStop: Disconnecting from service");
//        mediaController = MediaControllerCompat.getMediaController(MediaPlayerActivity.this);
//        if (mediaBrowser != null) {
//            mediaController.unregisterCallback(mediaControllerCallback);
//            mediaBrowser.disconnect();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaBrowser != null) {
            mediaController = MediaControllerCompat.getMediaController(MediaPlayerActivity.this);
            mediaController.getTransportControls().stop();
            mediaController.unregisterCallback(mediaControllerCallback);
            mediaBrowser.disconnect();
        }
    }

    /**
     * Connect your UI to the media controller
     * */
    void buildTransportControls()
    {
        // Grab the view for the play/pause button
//        playPause = (ImageView) findViewById(R.id.play_pause);

        // Attach a listener to the button
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                int pbState = mediaController.getPlaybackState().getState();
                Log.i(LOG_TAG, "onClick: " + pbState);
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                    playPause.setImageDrawable(AppCompatResources
                            .getDrawable(MediaPlayerActivity.this, R.drawable.ic_play));
                } else {
                    mediaController.getTransportControls().play();
                    playPause.setImageDrawable(AppCompatResources
                            .getDrawable(MediaPlayerActivity.this, R.drawable.ic_pause));
                }
            }
        });

        // Display the initial state
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            playPause.setImageDrawable(AppCompatResources
                    .getDrawable(MediaPlayerActivity.this, R.drawable.ic_pause));
        } else {
            playPause.setImageDrawable(AppCompatResources
                    .getDrawable(MediaPlayerActivity.this, R.drawable.ic_play));
        }


    }
}

