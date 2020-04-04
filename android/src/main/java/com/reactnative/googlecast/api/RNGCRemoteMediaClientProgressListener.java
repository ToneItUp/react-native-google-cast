package com.reactnative.googlecast.api;

import com.google.android.gms.cast.framework.media.RemoteMediaClient.ProgressListener;
import com.reactnative.googlecast.types.RNGCPlayerProgress;

public class RNGCRemoteMediaClientProgressListener implements ProgressListener {
    private RNGCRemoteMediaClient client;

    public RNGCRemoteMediaClientProgressListener(RNGCRemoteMediaClient client) {
        this.client = client;
    }

    public void onProgressUpdated(long progressMs, long durationMs) {
        client.sendEvent(RNGCRemoteMediaClient.MEDIA_PROGRESS_UPDATE,
                RNGCPlayerProgress.toJson(progressMs, durationMs));
    }
}