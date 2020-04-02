package com.reactnative.googlecast.types;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaTrack;

import java.util.ArrayList;
import java.util.List;

public class RNGCMediaInfo {
  public static MediaInfo fromJson(final ReadableMap json) {
    final List<MediaTrack> mediaTracks = new ArrayList<MediaTrack>();
    if (json.hasKey("mediaTracks")) {
      for (Object mediaTrack: json.getArray("mediaTracks").toArrayList()) {
        mediaTracks.add(RNGCMediaTrack.fromJson((ReadableMap)mediaTrack));
      }
    }
    return new MediaInfo.Builder(json.getString("contentId"))
            .setContentType(json.getString("contentType"))
            .setMetadata(RNGCMediaMetadata.fromJson(json.getMap("metadata")))
            .build();
  }

  public static WritableMap toJson(final MediaInfo info) {
    final WritableMap json = new WritableNativeMap();

    // adBreakClips

    // adBreaks

    json.putString("contentId", info.getContentId());

    json.putString("contentType", info.getContentType());

    json.putMap("customData", RNGCJSONObject.toJson(info.getCustomData()));

    json.putString("entity", info.getEntity());

    WritableArray mediaTracks = Arguments.createArray();
    for (MediaTrack track: info.getMediaTracks()) {
      mediaTracks.pushMap(RNGCMediaTrack.toJson(track));
    }
    json.putArray("mediaTracks", mediaTracks);

    json.putMap("metadata", RNGCMediaMetadata.toJson(info.getMetadata()));

    json.putInt("streamDuration", (int) info.getStreamDuration());

    json.putString("streamType", RNGCMediaStreamType.toJson(info.getStreamType()));

    // testTrackStyle

    // vmapAdsRequest

    return json;
  }
}
