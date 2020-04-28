#import <GoogleCast/GoogleCast.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

static NSString *const CAST_STATE_CHANGED =
@"GoogleCast:CastStateChanged";
static NSString *const MEDIA_STATUS_UPDATED = @"GoogleCast:MediaStatusUpdated";
static NSString *const MEDIA_PLAYBACK_STARTED =
    @"GoogleCast:MediaPlaybackStarted";
static NSString *const MEDIA_PLAYBACK_ENDED = @"GoogleCast:MediaPlaybackEnded";

@interface RNGCCastContext
    : RCTEventEmitter <RCTBridgeModule,
        GCKCastDeviceStatusListener,
        GCKSessionManagerListener,
        GCKRemoteMediaClientListener>
@end
