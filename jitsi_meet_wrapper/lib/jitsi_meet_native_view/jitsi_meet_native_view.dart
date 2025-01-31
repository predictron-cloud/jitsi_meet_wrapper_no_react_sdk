import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'jitsi_meet_native_view_controller.dart';
import 'package:jitsi_meet_wrapper_platform_interface/jitsi_meeting_listener.dart';
import 'package:jitsi_meet_wrapper_platform_interface/jitsi_meeting_options.dart';
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart';

typedef JItsiMeetNativeViewCreatedCallback = void Function(
    JitsiMeetViewController controller);

class JitsiMeetNativeView extends StatelessWidget {
  const JitsiMeetNativeView({
    Key? key,
    required this.onViewCreated,
    required this.options,
  }) : super(key: key);

  final JItsiMeetNativeViewCreatedCallback onViewCreated;
  final JitsiMeetingOptions options;

  @override
  Widget build(BuildContext context) {

    return (!kIsWeb && Platform.isIOS) ? UiKitView(
      viewType: 'plugins.jitsi_meet_wrapper:jitsi_meet_native_view',
      creationParams: options.toMap(),
      layoutDirection: TextDirection.ltr,
      creationParamsCodec: const StandardMessageCodec(),
      onPlatformViewCreated: _onPlatformViewCreated,
    ) : AndroidView(
      viewType: 'plugins.jitsi_meet_wrapper:jitsi_meet_native_view',
      layoutDirection: TextDirection.ltr,
      creationParams: options.toMap(),
      creationParamsCodec: const StandardMessageCodec(),
    );
  }

  void _onPlatformViewCreated(int id) => onViewCreated(
        JitsiMeetViewController(
          id: id,
        ),
      );
}
