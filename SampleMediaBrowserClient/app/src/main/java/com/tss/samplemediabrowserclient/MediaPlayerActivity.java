package com.tss.samplemediabrowserclient;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaPlayerActivity extends AppCompatActivity implements MediaPlayerComm {
    final String LOG_TAG = "TSS-MP Client";

    private RecyclerView recyclerView_RootItems;
    private RootItemListAdapter rootItemListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediaplayer);
        Log.i(LOG_TAG, "onCreate: ");

        recyclerView_RootItems = findViewById(R.id.recyclerView_rootItems);

        MediaPlayerHandler.getInstance().initializeMediaBrowser(MediaPlayerActivity.this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect to Media Browser Service i.e MediaPlaybackService
        Log.i(LOG_TAG, "onStart: Connecting to service");
        MediaPlayerHandler.getInstance().connectToService();
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
        Log.i(LOG_TAG, "onStop: ");
        // (see "stay in sync with the MediaSession")
//        mediaController = MediaControllerCompat.getMediaController(MediaPlayerActivity.this);
//        if (mediaBrowser != null) {
//            mediaController.unregisterCallback(mediaControllerCallback);
//            mediaBrowser.disconnect();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy: ");
        // de-init
        MediaPlayerHandler.getInstance().deInitialize();
    }

    @Override
    public void onConnected(MediaSessionCompat.Token token) {
        Log.i(LOG_TAG, "onConnected: ");
        //MediaPlayerHandler.getInstance().initializeMediaController(MediaPlayerActivity.this, token);
    }

    @Override
    public void onChildrenLoaded(List<MediaBrowserCompat.MediaItem> children) {
        Log.i(LOG_TAG, "onChildrenLoaded: ");
        if (!children.isEmpty()) {
            rootItemListAdapter = new RootItemListAdapter(MediaPlayerActivity.this, children);
            recyclerView_RootItems.setHasFixedSize(true);
            recyclerView_RootItems.setLayoutManager(new LinearLayoutManager(MediaPlayerActivity.this));
            recyclerView_RootItems.setAdapter(rootItemListAdapter);
            rootItemListAdapter.notifyDataSetChanged();
        } else {
            Log.e(LOG_TAG, "onChildrenLoaded: Media Item list is empty");
        }
    }
}

