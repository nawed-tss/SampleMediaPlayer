package com.tss.samplemediabrowserclient;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public interface PlayerActivityComm {
    void onMetadataChanged(MediaMetadataCompat metadata);

    void onPlaybackStateChanged(PlaybackStateCompat state);
}
