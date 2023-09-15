import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:jitsi_meet_wrapper/jitsi_meet_wrapper.dart';

/// Allow create a interface for web view and attach it as a child
/// optional param `extraJS` allows setup another external JS libraries
/// or Javascript embebed code
class JitsiMeetConferencing extends StatelessWidget {
  final Map<String, dynamic> options;
  const JitsiMeetConferencing(this.options, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'JitsiNativeView';

    return AndroidView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: options,
      creationParamsCodec: const StandardMessageCodec(),
    );
  }
}
