package com.tss.samplemediabrowserclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity implements MediaItemMetadataInterface {
    String LOG_TAG = "TSS-MP PlayerActivity";

    Intent intent;
    MediaBrowserCompat.MediaItem mMediaItem;

    TextView tvMediaTitle;
    TextView tvMediaArtist;
    TextView tvMediaAlbum;
    TextView tvMediaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Log.i(LOG_TAG, "onCreate: ");

        tvMediaTitle = findViewById(R.id.tv_mediaTitle);
        tvMediaArtist = findViewById(R.id.tv_mediaArtist);
        tvMediaAlbum = findViewById(R.id.tv_mediaAlbum);
        tvMediaId = findViewById(R.id.tv_mediaId);

        intent = getIntent();
        mMediaItem = intent.getParcelableExtra("mediaItem");

        MediaPlayerHandler.getInstance().getMediaTransportControls()
                .playFromMediaId(mMediaItem.getMediaId(), null);
    }


    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.i(LOG_TAG, "onMetadataChanged: ");
        String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        String mediaArtist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String mediaAlbum = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        long mediaDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        String mediaTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);


        tvMediaTitle.setText(mediaTitle);
        tvMediaArtist.setText(mediaArtist);
        tvMediaAlbum.setText(mediaAlbum);
        tvMediaId.setText(mediaId);
    }
}