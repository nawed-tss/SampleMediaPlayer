package com.tss.samplemediabrowserclient;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

public interface MediaPlayerComm {

//    void onConnected(MediaSessionCompat.Token token);

    void onChildrenLoaded(List<MediaBrowserCompat.MediaItem> children);
}
