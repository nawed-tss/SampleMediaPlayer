package com.tss.samplemediabrowserclient;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RootItemListAdapter extends RecyclerView.Adapter<RootItemListAdapter.RootItemViewHolder> {
    final String LOG_TAG = "TSS-MP Client";
    MediaBrowserCompat mediaBrowser;
    MediaBrowserCompat.SubscriptionCallback subscriptionCallback;
    List<MediaBrowserCompat.MediaItem> rootItemList;
    MediaControllerCompat mediaController;

    RootItemListAdapter(MediaBrowserCompat mediaBrowser, MediaBrowserCompat.SubscriptionCallback subscriptionCallback, MediaControllerCompat mediaController, List<MediaBrowserCompat.MediaItem> rootItemList) {
        this.mediaBrowser = mediaBrowser;
        this.subscriptionCallback = subscriptionCallback;
        this.rootItemList = rootItemList;
        this.mediaController = mediaController;
    }

    @NonNull
    @Override
    public RootItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.root_item_layout, parent, false);
        return new RootItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RootItemViewHolder holder, int position) {
        MediaBrowserCompat.MediaItem mediaItem = rootItemList.get(position);
        holder.textView_RootItemName.setText(mediaItem.getDescription().getTitle());
        holder.itemView.setOnClickListener(v -> {
            assert mediaItem.getMediaId() != null;
            Log.i(LOG_TAG, "onBindViewHolder: media id -> " + mediaItem.getMediaId());
            if (mediaItem.isBrowsable()) {
                Log.i(LOG_TAG, "onBindViewHolder: media is BROWSABLE");
                mediaBrowser.subscribe(mediaItem.getMediaId(), subscriptionCallback);
            }else if (mediaItem.isPlayable()) {
                Log.i(LOG_TAG, "onBindViewHolder: media is PLAYABLE");
                mediaController.getTransportControls().playFromMediaId(mediaItem.getMediaId(), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rootItemList.size();
    }

    static class RootItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView_RootItemName;
        public RootItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_RootItemName = itemView.findViewById(R.id.tv_rootName);
        }
    }
}
