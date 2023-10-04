import 'dart:async';

import 'package:jitsi_meet_wrapper_platform_interface/jitsi_meet_wrapper_platform_interface.dart';

export 'package:jitsi_meet_wrapper_platform_interface/jitsi_meet_wrapper_platform_interface.dart'
    show
        JitsiMeetingOptions,
        JitsiMeetingResponse,
        JitsiMeetingListener;

class JitsiMeetWrapper {
  /// Joins a meeting based on the JitsiMeetingOptions passed in.
  /// A JitsiMeetingListener can be attached to this meeting that will automatically
  /// be removed when the meeting has ended
  static Future<JitsiMeetingResponse> joinMeeting({
    required JitsiMeetingOptions options,
    JitsiMeetingListener? listener,
  }) async {
    assert(options.roomNameOrUrl.trim().isNotEmpty, "room is empty");

    if (options.serverUrl?.isNotEmpty ?? false) {
      assert(Uri.parse(options.serverUrl!).isAbsolute,
          "URL must be of the format <scheme>://<host>[/path], like https://someHost.com");
    }

    return await JitsiMeetWrapperPlatformInterface.instance
        .joinMeeting(options: options, listener: listener);
  }

  static Future<JitsiMeetingResponse> setAudioMuted(bool isMuted) async {
    return await JitsiMeetWrapperPlatformInterface.instance
        .setAudioMuted(isMuted);
  }

  static Future<JitsiMeetingResponse> hangUp() async {
    return await JitsiMeetWrapperPlatformInterface.instance.hangUp();
  }

  static Future<JitsiMeetingResponse> toggleCamera() async {
    return await JitsiMeetWrapperPlatformInterface.instance.toggleCamera();
  }

  static Future<JitsiMeetingResponse> pip(bool enabled) async {
    return await JitsiMeetWrapperPlatformInterface.instance.pip(enabled);
  }

  static Future<JitsiMeetingResponse> setSizeAndPosition(int width, int height, int right, int bottom) async {
    return await JitsiMeetWrapperPlatformInterface.instance.setSizeAndPosition(width, height, right, bottom);
  }

  static Future<JitsiMeetingResponse> toggleKeyboard(bool enabled) async {
    return await JitsiMeetWrapperPlatformInterface.instance.toggleKeyboard(enabled);
  }
}
