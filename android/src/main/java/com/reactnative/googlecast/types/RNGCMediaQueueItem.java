package com.reactnative.googlecast.types;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.media.MediaQueue;
import com.google.android.gms.common.images.WebImage;
import com.google.android.gms.common.util.ArrayUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RNGCMediaQueueItem {
  public static MediaQueueItem fromJson(final ReadableMap json) {
    MediaQueueItem.Builder builder;

    if (json.hasKey("mediaInfo")) {
      builder = new MediaQueueItem.Builder((RNGCMediaInfo.fromJson(json.getMap("mediaInfo"))));
    } else {
      try {
        builder = new MediaQueueItem.Builder(new JSONObject());
      } catch (JSONException e) {
        // just to satisfy the compiler, this won't happen
        builder = new MediaQueueItem.Builder(RNGCMediaInfo.fromJson(json));
      }
    }

    if (json.hasKey("activeTrackIds")) {
      List<Object> trackIds = json.getArray("activeTrackIds").toArrayList();
      long[] activeTrackIds = new long[trackIds.size()];
      for (int i = 0; i < trackIds.size(); i++) {
        activeTrackIds[i] = (long) trackIds.get(i);
      }
      builder.setActiveTrackIds(activeTrackIds);
    }

    if (json.hasKey("autoplay")) {
      builder.setAutoplay(json.getBoolean("autoplay"));
    }

    if (json.hasKey("customData")) {
      builder.setCustomData(RNGCJSONObject.fromJson(json.getMap("customData")));
    }

    if (json.hasKey("playbackDuration")) {
      builder.setPlaybackDuration(json.getDouble("playbackDuration"));
    }

    if (json.hasKey("preloadTime")) {
      builder.setPreloadTime(json.getDouble("preloadTime"));
    }

    if (json.hasKey("startTime")) {
      builder.setStartTime(json.getDouble("startTime"));
    }

    return builder.build();
  }

  public static WritableMap toJson(final MediaQueueItem item) {
    final WritableMap json = new WritableNativeMap();

    if(item == null) {
      return json;
    }

    final WritableArray activeTrackIds = Arguments.createArray();
    long[] activeTracks = item.getActiveTrackIds();
    if (activeTracks != null) {
      for (long activeTrackId : activeTracks) {
        activeTrackIds.pushInt((int) activeTrackId);
      }
    }
    json.putArray("activeTrackIds", activeTrackIds);

    json.putBoolean("autoplay", item.getAutoplay());

    json.putMap("customData", RNGCJSONObject.toJson(item.getCustomData()));

    json.putInt("itemId", item.getItemId());

    json.putMap("mediaInfo", RNGCMediaInfo.toJson(item.getMedia()));

    json.putDouble("playbackDuration", item.getPlaybackDuration());

    json.putDouble("preloadTime", item.getPreloadTime());

    json.putDouble("startTime", item.getStartTime());

    return json;
  }
}
