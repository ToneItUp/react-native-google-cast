package com.reactnative.googlecast;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.util.HashMap;
import java.util.Map;

public class GoogleCastModule
    extends ReactContextBaseJavaModule implements LifecycleEventListener {

  @VisibleForTesting public static final String REACT_CLASS = "RNGoogleCast";

  protected static final String SESSION_STARTING = "GoogleCast:SessionStarting";
  protected static final String SESSION_STARTED = "GoogleCast:SessionStarted";
  protected static final String SESSION_START_FAILED =
      "GoogleCast:SessionStartFailed";
  protected static final String SESSION_SUSPENDED =
      "GoogleCast:SessionSuspended";
  protected static final String SESSION_RESUMING = "GoogleCast:SessionResuming";
  protected static final String SESSION_RESUMED = "GoogleCast:SessionResumed";
  protected static final String SESSION_ENDING = "GoogleCast:SessionEnding";
  protected static final String SESSION_ENDED = "GoogleCast:SessionEnded";

  protected static final String MEDIA_STATUS_UPDATED =
      "GoogleCast:MediaStatusUpdated";
  protected static final String MEDIA_PLAYBACK_STARTED =
      "GoogleCast:MediaPlaybackStarted";
  protected static final String MEDIA_PLAYBACK_ENDED =
      "GoogleCast:MediaPlaybackEnded";

  private CastSession mCastSession;
  private SessionManagerListener<CastSession> mSessionManagerListener;

  public GoogleCastModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addLifecycleEventListener(this);
    setupCastListener();
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();

    constants.put("SESSION_STARTING", SESSION_STARTING);
    constants.put("SESSION_STARTED", SESSION_STARTED);
    constants.put("SESSION_START_FAILED", SESSION_START_FAILED);
    constants.put("SESSION_SUSPENDED", SESSION_SUSPENDED);
    constants.put("SESSION_RESUMING", SESSION_RESUMING);
    constants.put("SESSION_RESUMED", SESSION_RESUMED);
    constants.put("SESSION_ENDING", SESSION_ENDING);
    constants.put("SESSION_ENDED", SESSION_ENDED);

    constants.put("MEDIA_STATUS_UPDATED", MEDIA_STATUS_UPDATED);
    constants.put("MEDIA_PLAYBACK_STARTED", MEDIA_PLAYBACK_STARTED);
    constants.put("MEDIA_PLAYBACK_ENDED", MEDIA_PLAYBACK_ENDED);

    return constants;
  }

  protected void emitMessageToRN(String eventName,
                                 @Nullable WritableMap params) {
    getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @ReactMethod
  public void getCastState(final Promise promise) {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        CastContext castContext =
            CastContext.getSharedInstance(getReactApplicationContext());
        promise.resolve(castContext.getCastState() - 1);
      }
    });
  }

  @ReactMethod
  public void endSession(final boolean stopCasting, final Promise promise) {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        SessionManager sessionManager =
            CastContext.getSharedInstance(getReactApplicationContext())
                .getSessionManager();
        sessionManager.endCurrentSession(stopCasting);
        promise.resolve(true);
      }
    });
  }

  @ReactMethod
  public void launchExpandedControls() {
    ReactApplicationContext context = getReactApplicationContext();
    Intent intent =
        new Intent(context, GoogleCastExpandedControlsActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  private void setupCastListener() {
    mSessionManagerListener = new GoogleCastSessionManagerListener(this);
  }

  @Override
  public void onHostResume() {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        SessionManager sessionManager =
            CastContext.getSharedInstance(getReactApplicationContext())
                .getSessionManager();
        sessionManager.addSessionManagerListener(mSessionManagerListener,
                                                 CastSession.class);
      }
    });
  }

  @Override
  public void onHostPause() {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        SessionManager sessionManager =
            CastContext.getSharedInstance(getReactApplicationContext())
                .getSessionManager();
        sessionManager.removeSessionManagerListener(mSessionManagerListener,
                                                    CastSession.class);
      }
    });
  }

  @Override
  public void onHostDestroy() {}

  protected void setCastSession(CastSession castSession) {
    this.mCastSession = castSession;
  }

  protected CastSession getCastSession() { return mCastSession; }

  protected void runOnUiQueueThread(Runnable runnable) {
    getReactApplicationContext().runOnUiQueueThread(runnable);
  }
}
