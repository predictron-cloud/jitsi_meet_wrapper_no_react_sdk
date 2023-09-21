import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'jitsi_meet_native_view_controller.dart';
import 'package:jitsi_meet_wrapper_platform_interface/jitsi_meeting_listener.dart';
import 'package:jitsi_meet_wrapper_platform_interface/jitsi_meeting_options.dart';
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart';

typedef JitsiMeetNativeViewCreatedCallback = void Function(
    JitsiMeetViewController controller);

class JitsiMeetNativeView extends StatelessWidget {
  const JitsiMeetNativeView({
    Key? key,
    required this.onViewCreated,
    required this.options,
  }) : super(key: key);

  final JitsiMeetNativeViewCreatedCallback onViewCreated;
  final JitsiMeetingOptions options;


  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'plugins.jitsi_meet_wrapper:jitsi_meet_native_view';
    // Pass parameters to the platform side.

    return PlatformViewLink(
      viewType: viewType,
      surfaceFactory:
          (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: options.toMap(),
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }

  /*@override
  Widget build(BuildContext context) {

    return (!kIsWeb && Platform.isIOS) ? UiKitView(
      viewType: viewType,
      creationParams: options.toMap(),
      layoutDirection: TextDirection.ltr,
      creationParamsCodec: const StandardMessageCodec(),
      onPlatformViewCreated: _onPlatformViewCreated,
    ) : AndroidView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: options.toMap(),
      creationParamsCodec: const StandardMessageCodec(),
    );
  }*/

  void _onPlatformViewCreated(int id) => onViewCreated(
        JitsiMeetViewController(
          id: id,
        ),
      );
}
