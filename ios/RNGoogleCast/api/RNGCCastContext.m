#import "RNGCCastContext.h"
#import "../types/RCTConvert+GCKCastState.m"
#import "../types/RCTConvert+GCKMediaInformation.m"
#import "../types/RCTConvert+GCKMediaLoadOptions.m"
#import "../types/RCTConvert+GCKMediaStatus.m"

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>

@implementation RNGCCastContext {
  bool hasListeners;
  NSUInteger currentItemID;
  bool playbackStarted;
  bool playbackEnded;
  GCKSessionManager *_sessionManager;
}

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup {
  return NO;
}

- (instancetype)init {
  if (self = [super init]) {
    _sessionManager = [GCKCastContext sharedInstance].sessionManager;
    [[NSNotificationCenter defaultCenter]
      addObserver:self
         selector:@selector(castDeviceDidChange:)
             name:kGCKCastStateDidChangeNotification
           object:[GCKCastContext sharedInstance]];
  }
  return self;
}

- (NSDictionary *)constantsToExport {
  return @{
    @"CAST_STATE_CHANGED": CAST_STATE_CHANGED,
    @"MEDIA_STATUS_UPDATED" : MEDIA_STATUS_UPDATED,
    @"MEDIA_PLAYBACK_STARTED" : MEDIA_PLAYBACK_STARTED,
    @"MEDIA_PLAYBACK_ENDED" : MEDIA_PLAYBACK_ENDED,
  };
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    CAST_STATE_CHANGED,
    MEDIA_STATUS_UPDATED,
    MEDIA_PLAYBACK_STARTED,
    MEDIA_PLAYBACK_ENDED,
  ];
}

// Will be called when this module's first listener is added.
- (void)startObserving {
  hasListeners = YES;
  // Set up any upstream listeners or background tasks as necessary
  dispatch_async(dispatch_get_main_queue(), ^{
   [_sessionManager addListener:self];
  });
}

// Will be called when this module's last listener is removed, or on dealloc.
- (void)stopObserving {
  hasListeners = NO;
  // Remove upstream listeners, stop unnecessary background tasks
  // FIXME: this crashes on (hot) reload
  dispatch_async(dispatch_get_main_queue(), ^{
   [_sessionManager removeListener:self];
  });
}

# pragma mark - GCKCastContext methods

RCT_REMAP_METHOD(getCastState,
                 getCastStateWithResolver: (RCTPromiseResolveBlock) resolve
                 rejecter: (RCTPromiseRejectBlock) reject) {
  dispatch_async(dispatch_get_main_queue(), ^{
    GCKCastState state = [GCKCastContext.sharedInstance castState];
    resolve([RCTConvert fromGCKCastState:state]);
  });
}

RCT_REMAP_METHOD(showCastDialog,
                 showCastDialogWithResolver: (RCTPromiseResolveBlock) resolve
                 rejecter: (RCTPromiseRejectBlock) reject) {
  dispatch_async(dispatch_get_main_queue(), ^{
    [GCKCastContext.sharedInstance presentCastDialog];
    resolve(@(YES));
  });
}

RCT_REMAP_METHOD(showExpandedControls,
                 showExpandedControlsWithResolver: (RCTPromiseResolveBlock) resolve
                 rejecter: (RCTPromiseRejectBlock) reject) {
  dispatch_async(dispatch_get_main_queue(), ^{
    [GCKCastContext.sharedInstance presentDefaultExpandedMediaControls];
    resolve(@(YES));
  });
}

RCT_EXPORT_METHOD(showIntroductoryOverlay:(id)options
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) reject) {
  dispatch_async(dispatch_get_main_queue(), ^{
    if (!options[@"once"]) {
      [GCKCastContext.sharedInstance clearCastInstructionsShownFlag];
    }
    
    resolve(@([GCKCastContext.sharedInstance presentCastInstructionsViewControllerOnce]));
  });
}

RCT_EXPORT_METHOD(endSession
                  : (BOOL)stopCasting resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_main_queue(), ^{
    if ([_sessionManager endSessionAndStopCasting:stopCasting]) {
      resolve(@(YES));
    } else {
      NSError *error = [NSError errorWithDomain:NSCocoaErrorDomain
                                           code:GCKErrorCodeNoMediaSession
                                       userInfo:nil];
      reject(@"no_session", @"No castSession!", error);
    }
  });
}

- (void)switchToLocalPlayback {
  NSLog(@"switchToLocalPlayback");
  if (_sessionManager.currentCastSession) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [_sessionManager.currentCastSession.remoteMediaClient removeListener:self];
    });
  }
}

- (void)switchToRemotePlayback {
  NSLog(@"switchToRemotePlayback");
  dispatch_async(dispatch_get_main_queue(), ^{
    [_sessionManager.currentCastSession.remoteMediaClient addListener:self];
  });
}

- (void)castDeviceDidChange:(NSNotification *)notification {
  if (!hasListeners) return;

  GCKCastState state = [GCKCastContext sharedInstance].castState;
  [self sendEventWithName:CAST_STATE_CHANGED
                     body:[RCTConvert fromGCKCastState:state]];
}

#pragma mark - GCKSessionManagerListener

- (void)sessionManager:(GCKSessionManager *)sessionManager didStartSession:(GCKSession *)session {
  NSLog(@"MediaViewController: sessionManager didStartSession %@", session);
  [self switchToRemotePlayback];
}

- (void)sessionManager:(GCKSessionManager *)sessionManager didResumeSession:(GCKSession *)session {
  NSLog(@"MediaViewController: sessionManager didResumeSession %@", session);
  [self switchToRemotePlayback];
}

- (void)sessionManager:(GCKSessionManager *)sessionManager
         didEndSession:(GCKSession *)session
             withError:(NSError *)error {
  NSLog(@"session ended with error: %@", error);
  [self switchToLocalPlayback];
}

- (void)sessionManager:(GCKSessionManager *)sessionManager
    didFailToStartSessionWithError:(NSError *)error {
  [self switchToLocalPlayback];
}

- (void)sessionManager:(GCKSessionManager *)sessionManager
    didFailToResumeSession:(GCKSession *)session
                 withError:(NSError *)error {
  [self switchToLocalPlayback];
}

#pragma mark - GCKRemoteMediaClientListener

- (void)remoteMediaClient:(GCKRemoteMediaClient *)client
     didUpdateMediaStatus:(GCKMediaStatus *)mediaStatus {
      if (currentItemID != mediaStatus.currentItemID) {
        // reset item status
        currentItemID = mediaStatus.currentItemID;
        playbackStarted = false;
        playbackEnded = false;
      }

      NSDictionary *status = [RCTConvert fromGCKMediaStatus:mediaStatus];

      [self sendEventWithName:MEDIA_STATUS_UPDATED body:@{@"mediaStatus" : status}];

      if (!playbackStarted &&
          mediaStatus.playerState == GCKMediaPlayerStatePlaying) {
        [self sendEventWithName:MEDIA_PLAYBACK_STARTED
                          body:@{@"mediaStatus" : status}];
        playbackStarted = true;
      }

      if (!playbackEnded &&
          mediaStatus.idleReason == GCKMediaPlayerIdleReasonFinished) {
        [self sendEventWithName:MEDIA_PLAYBACK_ENDED
                          body:@{@"mediaStatus" : status}];
        playbackEnded = true;
      }
}
@end
