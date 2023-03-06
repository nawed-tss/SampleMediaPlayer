package com.tss.samplemediabrowserservice;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaDescription;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import androidx.media.MediaBrowserServiceCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MediaDataSourceProvider {
    final String LOG_TAG = "MP-Server MediaDataSourceProvider";
    private static MediaDataSourceProvider sInstance;

    private AsyncTask mPendingTask;
    private ContentResolver mResolver;

    private static String EXTERNAL = "external";
    private static String INTERNAL = "internal";

    private static final Uri[] ALL_AUDIO_URI = new Uri[]{
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    };

    private MediaDataSourceProvider() {

    }

    public static MediaDataSourceProvider getInstance() {
        if (sInstance == null) {
            sInstance = new MediaDataSourceProvider();
        }
        return sInstance;
    }

    public void initialize(Context context) {
        mResolver = context.getContentResolver();
    }

    List<String> mediaItemIdList = new ArrayList<>();
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

    public void clearMediaItemIdList() {
        mediaItemIdList.clear();
    }

    public void addMediaItemId(String mediaItemId) {
        mediaItemIdList.add(mediaItemId);
    }

    public List<String> getMediaItemIdList() {
        return mediaItemIdList;
    }

    public int getMediaItemIndex(String mediaItemId) {
        return mediaItemIdList.indexOf(mediaItemId);
    }

    public String getMediaId(int indexOfMediaItem) {
        return mediaItemIdList.get(indexOfMediaItem);
    }

    public List<MediaBrowserCompat.MediaItem> onQueryByKey(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {


        List<MediaBrowserCompat.MediaItem> mediaItemList = new ArrayList<>();

        String[] projection = new String[]{
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

        // Define the selection criteria
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE ?";

        // Define the selection arguments
        String[] selectionArgs = {parentId};

//        Uri[] itemURI = new Uri[]{MediaStore.Audio.Media.getContentUri(INTERNAL, Long.parseLong(parentId)),
//                MediaStore.Audio.Media.getContentUri(EXTERNAL, Long.parseLong(parentId))};

        for (Uri uri : ALL_AUDIO_URI) {
            Cursor cursor = mResolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.getCount() > 0) {
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
                    mediaItemList.add(mediaItem);

                    Uri mediaUri = mediaItem.getDescription().getMediaUri();
                    Log.i(LOG_TAG, "onLoadChildren: Adding media uri to data source: " + mediaUri);
                    addMediaItemUri(mediaItem.getMediaId(), mediaUri);
                    addMediaItemMetadata(mediaItem.getMediaId(), mediaMetadata);
                    addMediaItemId(mediaItem.getMediaId());
                }
                cursor.close();
            }
        }

        return mediaItemList;
    }

    public void onQueryByFolder(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        FileBrowserTask query = new FileBrowserTask(result, ALL_AUDIO_URI, mResolver);
        Log.i(LOG_TAG, "onQueryByFolder: Starting query for folders");
        queryInBackground(result, query);
    }

    public static class FileBrowserTask extends AsyncTask<Void, Void, Void> {
        public final String LOG_TAG = "MP-Server FileBrowserTask";
        private static final String[] COLUMNS = {MediaStore.Audio.AudioColumns.DATA};
        private MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> mResult;
        private Uri[] mUris;
        private ContentResolver mResolver;

        public FileBrowserTask(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result, Uri[] uris,
                               ContentResolver resolver) {
            mResult = result;
            mUris = uris;
            mResolver = resolver;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Set<String> paths = new HashSet<>();
            Cursor cursor = null;
            for (Uri uri : mUris) {
                try {
                    cursor = mResolver.query(uri, COLUMNS, null, null, null);
                    if (cursor != null) {
                        int pathColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
                        while (cursor.moveToNext()) {
                            // We want to de-dupe paths of each of the songs so we get just a list
                            // of containing directories.
                            String fullPath = cursor.getString(pathColumn);
                            int fileNameStart = fullPath.lastIndexOf(File.separator);
                            if (fileNameStart < 0) {
                                continue;
                            }
                            String dirPath = fullPath.substring(0, fileNameStart);
                            paths.add(dirPath);
                        }
                    }
                } catch (SQLiteException e) {
                    Log.e(LOG_TAG, "Failed to execute query " + e);  // Stack trace is noisy.
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            // Take the list of deduplicated directories and put them into the results list with
            // the full directory path as the key so we can match on it later.
            List<MediaBrowserCompat.MediaItem> results = new ArrayList<>();
            for (String path : paths) {
                int dirNameStart = path.lastIndexOf(File.separator) + 1;
                String dirName = path.substring(dirNameStart, path.length());
                MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                        .setMediaId(path + "%")  // Used in a like query.
                        .setTitle(dirName)
                        .setSubtitle(path)
                        .build();
                results.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
            }
            mResult.sendResult(results);
            return null;
        }

    }

    private void queryInBackground(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result,
                                   AsyncTask<Void, Void, Void> task) {
        result.detach();
        if (mPendingTask != null) {
            Log.i(LOG_TAG, "queryInBackground: Cancelling pending task");
            mPendingTask.cancel(true);
        }
        mPendingTask = task;
        task.execute();
    }

}
