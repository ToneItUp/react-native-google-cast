package com.reactnative.googlecast.types;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.common.images.WebImage;

public class RNGCPlayerProgress {
    public static WritableMap toJson(final long progressMs, final long durationMs) {
        final WritableMap json = Arguments.createMap();
        json.putInt("duration", (int)(durationMs / 1000));
        json.putInt("progress", (int)(progressMs / 1000));
        return json;
    }
}