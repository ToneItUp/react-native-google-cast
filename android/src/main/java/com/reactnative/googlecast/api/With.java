package com.reactnative.googlecast.api;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;

abstract class With<X> {  
  private static @Nullable ReactApplicationContext reactContext;

  public With() {
    this(null);
  }

  public With(ReactApplicationContext context) {
    this.reactContext = context;
  }

  protected ReactApplicationContext getReactContext() {
    return this.reactContext;
  };
  
  protected interface WithX<X> {
    void execute(X x);
  }

  protected interface WithXPromisify<X> {
    @Nullable
    PendingResult execute(X client);
  }

  protected void withX(final WithX<X> runnable) throws IllegalStateException {
    withX(new WithXPromisify<X>() {
      @Override
      public PendingResult execute(X x) {
        runnable.execute(x);
        return null;
      }
    }, null);
  }

  protected void withX(final WithX<X> runnable, final Promise promise) {
    withX(new WithXPromisify<X>() {
      @Override
      public PendingResult execute(X x) {
        runnable.execute(x);
        return null;
      }
    }, promise);
  }

  protected void withX(final WithXPromisify<X> runnable,
                            final @Nullable Promise promise) throws IllegalStateException {
    ReactApplicationContext context = getReactContext();
    if(context == null) {
      throw new IllegalStateException("No react context available for runOnUiQueueThread");
    }

    context.runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        try {
          PendingResult pendingResult = runnable.execute(getX());
          if (pendingResult != null) {
            RNGCPendingResult.promisifyResult(pendingResult, promise);
          }
        } catch (Exception e) {
          if (promise != null) {
            promise.reject(e);
          } else {
            throw e;
          }
        }
      }
    });
  }

  abstract protected X getX() throws IllegalStateException;
}
