package com.reactnative.googlecast.api;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.media.RemoteMediaClient.ProgressListener;
import com.google.android.gms.common.api.PendingResult;
import com.reactnative.googlecast.types.RNGCJSONObject;
import com.reactnative.googlecast.types.RNGCMediaInfo;
import com.reactnative.googlecast.types.RNGCMediaLoadOptions;
import com.reactnative.googlecast.types.RNGCMediaQueueItem;
import com.reactnative.googlecast.types.RNGCMediaSeekOptions;

import java.util.HashMap;
import java.util.Map;

public class RNGCRemoteMediaClient extends ReactContextBaseJavaModule implements LifecycleEventListener {

  @VisibleForTesting
  public static final String REACT_CLASS = "RNGCRemoteMediaClient";

  public static final String MEDIA_STATUS_UPDATED =
      "GoogleCast:MediaStatusUpdated";
  public static final String MEDIA_PLAYBACK_STARTED =
      "GoogleCast:MediaPlaybackStarted";
  public static final String MEDIA_PLAYBACK_ENDED =
          "GoogleCast:MediaPlaybackEnded";
  public static final String MEDIA_PROGRESS_UPDATE =
          "GoogleCast:MediaProgressUpdate";

  private ProgressListener mProgressListener;

  public RNGCRemoteMediaClient(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addLifecycleEventListener(this);
    this.mProgressListener = new RNGCRemoteMediaClientProgressListener(this);
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();

    constants.put("MEDIA_STATUS_UPDATED", MEDIA_STATUS_UPDATED);
    constants.put("MEDIA_PLAYBACK_STARTED", MEDIA_PLAYBACK_STARTED);
    constants.put("MEDIA_PLAYBACK_ENDED", MEDIA_PLAYBACK_ENDED);
    constants.put("MEDIA_PROGRESS_UPDATE", MEDIA_PROGRESS_UPDATE);

    return constants;
  }

  protected void sendEvent(String eventName) {
    this.sendEvent(eventName, null);
  }

  protected void sendEvent(String eventName, @Nullable WritableMap params) {
    getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  protected void runOnUiQueueThread(Runnable runnable) {
    getReactApplicationContext().runOnUiQueueThread(runnable);
  }

  @ReactMethod
  public void loadMedia(final ReadableMap mediaInfo,
                        final ReadableMap loadOptions, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        client.addProgressListener(mProgressListener, 1000);
        return client.load(RNGCMediaInfo.fromJson(mediaInfo),
                           RNGCMediaLoadOptions.fromJson(loadOptions));
      }
    }, promise);
  }

  @ReactMethod
  public void play(final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.play();
      }
    }, promise);
  }

  @ReactMethod
  public void pause(final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.pause();
      }
    }, promise);
  }

  @ReactMethod
  public void
  queueInsertAndPlayItem(final ReadableMap item, final Integer beforeItemId,
                         final Integer playPosition,
                         final ReadableMap customData, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.queueInsertAndPlayItem(
            RNGCMediaQueueItem.fromJson(item), beforeItemId, playPosition,
            RNGCJSONObject.fromJson(customData));
      }
    }, promise);
  }

  @ReactMethod
  public void seek(final ReadableMap options, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.seek(RNGCMediaSeekOptions.fromJson(options));
      }
    }, promise);
  }

  @ReactMethod
  public void setPlaybackRate(final double playbackRate, final ReadableMap customData, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.setPlaybackRate(playbackRate, RNGCJSONObject.fromJson(customData));
      }
    }, promise);
  }

  @ReactMethod
  public void setStreamMuted(final boolean muted, final ReadableMap customData, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.setStreamMute(muted, RNGCJSONObject.fromJson(customData));
      }
    }, promise);
  }

  @ReactMethod
  public void setStreamVolume(final double volume, final ReadableMap customData, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        return client.setStreamVolume(volume, RNGCJSONObject.fromJson(customData));
      }
    }, promise);
  }

  @ReactMethod
  public void stop(final ReadableMap customData, final Promise promise) {
    with.withX(new With.WithXPromisify<RemoteMediaClient>() {
      @Override
      public PendingResult execute(RemoteMediaClient client) {
        client.removeProgressListener(mProgressListener);
        return client.stop(RNGCJSONObject.fromJson(customData));
      }
    }, promise);
  }

  protected With<RemoteMediaClient> with = new With<RemoteMediaClient>(getReactApplicationContext()) {    
    @Override
    protected RemoteMediaClient getX() {
      final CastSession castSession = CastContext.getSharedInstance(this.getReactContext())
                                          .getSessionManager()
                                          .getCurrentCastSession();

      if (castSession == null) {
        throw new IllegalStateException(("No castSession!"));
      }

      final RemoteMediaClient client = castSession.getRemoteMediaClient();

      if (client == null) {
        throw new IllegalStateException(("No device client!"));
      }

      return client;
    }
  };

  @Override
  public void onHostPause() {}

  @Override
  public void onHostResume() {}

  @Override
  public void onHostDestroy() {
    final CastSession castSession = CastContext.getSharedInstance(getReactApplicationContext())
            .getSessionManager()
            .getCurrentCastSession();

    if (castSession != null) {
      final RemoteMediaClient client = castSession.getRemoteMediaClient();

      if (client != null) {
        client.removeProgressListener(mProgressListener);
      }
    }
  }
}
