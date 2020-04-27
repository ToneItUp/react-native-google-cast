package com.reactnative.googlecast.types;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.VideoInfo;
import com.google.android.gms.common.images.WebImage;

public class RNGCMediaStatus {
  public static WritableMap toJson(final MediaStatus status) {
    final WritableMap json = Arguments.createMap();

    if (status == null) {
      return json;
    }

    json.putInt("currentItemId", status.getCurrentItemId());

    MediaQueueItem queueItem = status.getQueueItemById(status.getCurrentItemId());
    if (queueItem != null) {
      json.putMap("currentQueueItem", RNGCMediaQueueItem.toJson(queueItem));
    }

    json.putMap("customData", RNGCJSONObject.toJson(status.getCustomData()));

    json.putString("idleReason",
                   RNGCMediaPlayerIdleReason.toJson(status.getIdleReason()));

    json.putBoolean("isMuted", status.isMute());

    json.putInt("loadingItemId", status.getLoadingItemId());

    json.putMap("mediaInfo", RNGCMediaInfo.toJson(status.getMediaInfo()));

    json.putDouble("playbackRate", status.getPlaybackRate());

    json.putString("playerState",
                   RNGCPlayerState.toJson(status.getPlayerState()));

    json.putInt("preloadedItemId", status.getPreloadedItemId());

    final WritableArray queueItems = Arguments.createArray();
    for (MediaQueueItem item: status.getQueueItems()) {
      queueItems.pushMap(RNGCMediaQueueItem.toJson(item));
    }
    json.putArray("queueItems", queueItems);

    json.putString("queueRepeatMode",
                   RNGCMediaRepeatMode.toJson(status.getQueueRepeatMode()));

    json.putInt("streamPosition", (int)status.getStreamPosition());

    VideoInfo videoInfo = status.getVideoInfo();
    if (videoInfo != null) {
      json.putMap("videoInfo", RNGCVideoInfo.toJson(videoInfo));
    }

    json.putDouble("volume", status.getStreamVolume());

    return json;
  }
}
