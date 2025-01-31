import 'package:flutter/material.dart';
import 'package:jitsi_meet_wrapper/jitsi_meet_wrapper.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Meeting(),
    );
  }
}

class Meeting extends StatefulWidget {
  const Meeting({Key? key}) : super(key: key);

  @override
  _MeetingState createState() => _MeetingState();
}

class _MeetingState extends State<Meeting> {
  final serverText = TextEditingController(text: 'https://meet.element.io');
  final roomText = TextEditingController(text: "jitsi-meet-wrapper-test-room");
  final subjectText = TextEditingController(text: "My Plugin Test Meeting");
  final tokenText = TextEditingController();
  final userDisplayNameText = TextEditingController(text: "Plugin Test User");
  final userEmailText = TextEditingController(text: "fake@email.com");
  final userAvatarUrlText = TextEditingController();

  bool isAudioMuted = true;
  bool isAudioOnly = false;
  bool isVideoMuted = false;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Jitsi Meet Wrapper Test')),
        body: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          child: buildMeetConfig(),
        ),
      ),
    );
  }

  Widget buildMeetConfig() {
    return SingleChildScrollView(
      child: Column(
        children: <Widget>[
          const SizedBox(height: 16.0),
          _buildTextField(
            labelText: "Server URL",
            controller: serverText,
            hintText: "Hint: Leave empty for meet.jitsi.si",
          ),
          const SizedBox(height: 16.0),
          _buildTextField(labelText: "Room", controller: roomText),
          const SizedBox(height: 16.0),
          _buildTextField(labelText: "Subject", controller: subjectText),
          const SizedBox(height: 16.0),
          _buildTextField(labelText: "Token", controller: tokenText),
          const SizedBox(height: 16.0),
          _buildTextField(
            labelText: "User Display Name",
            controller: userDisplayNameText,
          ),
          const SizedBox(height: 16.0),
          _buildTextField(
            labelText: "User Email",
            controller: userEmailText,
          ),
          const SizedBox(height: 16.0),
          _buildTextField(
            labelText: "User Avatar URL",
            controller: userAvatarUrlText,
          ),
          const SizedBox(height: 16.0),
          CheckboxListTile(
            title: const Text("Audio Muted"),
            value: isAudioMuted,
            onChanged: _onAudioMutedChanged,
          ),
          const SizedBox(height: 16.0),
          CheckboxListTile(
            title: const Text("Audio Only"),
            value: isAudioOnly,
            onChanged: _onAudioOnlyChanged,
          ),
          const SizedBox(height: 16.0),
          CheckboxListTile(
            title: const Text("Video Muted"),
            value: isVideoMuted,
            onChanged: _onVideoMutedChanged,
          ),
          const Divider(height: 48.0, thickness: 2.0),
          Column(
            children: [
              ElevatedButton(
                onPressed: () => _joinMeeting(),
                child: const Text(
                  "Join Meeting",
                  style: TextStyle(color: Colors.white),
                ),
                style: ButtonStyle(
                  backgroundColor:
                      MaterialStateColor.resolveWith((states) => Colors.blue),
                ),
              ),
              ElevatedButton(
                onPressed: () => JitsiMeetWrapper.hangUp(),
                child: const Text(
                  "Hang up",
                  style: TextStyle(color: Colors.white),
                ),
                style: ButtonStyle(
                  backgroundColor:
                  MaterialStateColor.resolveWith((states) => Colors.blue),
                ),
              ),
              ElevatedButton(
                onPressed: () => _onAudioMutedChanged(true),
                child: const Text(
                  "PIP",
                  style: TextStyle(color: Colors.white),
                ),
                style: ButtonStyle(
                  backgroundColor:
                  MaterialStateColor.resolveWith((states) => Colors.blue),
                ),
              ),
              ElevatedButton(
                onPressed: () => _onAudioMutedChanged(false),
                child: const Text(
                  "Exit PIP",
                  style: TextStyle(color: Colors.white),
                ),
                style: ButtonStyle(
                  backgroundColor:
                  MaterialStateColor.resolveWith((states) => Colors.blue),
                ),
              ),
              ElevatedButton(
                onPressed: () => {JitsiMeetWrapper.toggleCamera()},
                child: const Text(
                  "Toggle Camera",
                  style: TextStyle(color: Colors.white),
                ),
                style: ButtonStyle(
                  backgroundColor:
                  MaterialStateColor.resolveWith((states) => Colors.blue),
                ),
              ),
            ],
          ),
          const SizedBox(height: 48.0),
        ],
      ),
    );
  }

  _onAudioOnlyChanged(bool? value) {
    setState(() {
      isAudioOnly = value!;
    });
  }

  _onAudioMutedChanged(bool? value) {
    if(value == true) {
      JitsiMeetWrapper.setSizeAndPosition(100, 200, 20, 20);
    } else {
      JitsiMeetWrapper.setSizeAndPosition(800, 1300, 150, 100);
    }
    print('audio!!!!');

    setState(() {
      isAudioMuted = value!;
    });
  }

  _onVideoMutedChanged(bool? value) {
    setState(() {
      isVideoMuted = value!;
    });
  }

  _joinMeeting() async {
    String? serverUrl = serverText.text.trim().isEmpty ? null : serverText.text;

    Map<String, Object> featureFlags =  {
      'chat.enabled': false,
      'prejoinpage.enabled': false,
      'invite.enabled': false,
      'help.enabled': false,
      'car-mode.enabled': false,
      'settings.enabled': false,
      'meeting-name.enabled': false,
      'security-options.enabled': false,
      'tile-view.enabled': false,
      'toolbox.enabled': true,
      'speakerstats.enabled': false,
      'android.screensharing.enabled': false,
      'live-streaming.enabled': false,
      'video-share.enabled': false,
      'reactions.enabled': false,
      'raise-hand.enabled': false,
      'pip.enabled': true,
      //recording.enabled true
      //audio-mute.enabled : false
      'video-mute.enabled': false, //todo: does not work?
    };

    // Define meetings options here
    var options = JitsiMeetingOptions(
      roomNameOrUrl: roomText.text,
      serverUrl: serverUrl,
      subject: subjectText.text,
      token: tokenText.text,
      isAudioMuted: isAudioMuted,
      isAudioOnly: isAudioOnly,
      isVideoMuted: isVideoMuted,
      userDisplayName: userDisplayNameText.text,
      userEmail: userEmailText.text,
      featureFlags: featureFlags,
      configOverrides: {
        'defaultLanguage': 'ru',
        'subject': 'lalalala',
        'hideConferenceSubject': false,
        'hideConferenceTimer': false,
        'toolbarButtons': ['microphone', 'camera'],
        'TOOLBAR_BUTTONS': ['microphone', 'camera'],
        'MAIN_TOOLBAR_BUTTONS': ['microphone', 'camera'],
        'BUTTONS_WITH_NOTIFY_CLICK': ['microphone', 'camera'],
        'buttonsWithNotifyClick': ['microphone', 'camera'],
        'customToolbarButtons': ['microphone', 'camera'],
      }
    );

    debugPrint("JitsiMeetingOptions: $options");
    await JitsiMeetWrapper.joinMeeting(
      options: options,
      listener: JitsiMeetingListener(
        onOpened: () async => {
          await JitsiMeetWrapper.setSizeAndPosition(200, 300, 100, 100)
        },
        onConferenceWillJoin: (url) {
          debugPrint("onConferenceWillJoin: url: $url");
        },
        onConferenceJoined: (url) {
          debugPrint("onConferenceJoined: url: $url");
        },
        onConferenceTerminated: (url, error) {
          debugPrint("onConferenceTerminated: url: $url, error: $error");
        },
        onAudioMutedChanged: (isMuted) {
          debugPrint("onAudioMutedChanged: isMuted: $isMuted");
        },
        onVideoMutedChanged: (isMuted) {
          debugPrint("onVideoMutedChanged: isMuted: $isMuted");
        },
        onScreenShareToggled: (participantId, isSharing) {
          debugPrint(
            "onScreenShareToggled: participantId: $participantId, "
            "isSharing: $isSharing",
          );
        },
        onParticipantJoined: (email, name, role, participantId) {
          debugPrint(
            "onParticipantJoined: email: $email, name: $name, role: $role, "
            "participantId: $participantId",
          );
        },
        onParticipantLeft: (participantId) {
          debugPrint("onParticipantLeft: participantId: $participantId");
        },
        onParticipantsInfoRetrieved: (participantsInfo, requestId) {
          debugPrint(
            "onParticipantsInfoRetrieved: participantsInfo: $participantsInfo, "
            "requestId: $requestId",
          );
        },
        onChatMessageReceived: (senderId, message, isPrivate) {
          debugPrint(
            "onChatMessageReceived: senderId: $senderId, message: $message, "
            "isPrivate: $isPrivate",
          );
        },
        onChatToggled: (isOpen) => debugPrint("onChatToggled: isOpen: $isOpen"),
        onClosed: () => debugPrint("onClosed"),
      ),
    );
  }

  Widget _buildTextField({
    required String labelText,
    required TextEditingController controller,
    String? hintText,
  }) {
    return TextField(
      onTap: () => JitsiMeetWrapper.pip(true),
      onEditingComplete: () => JitsiMeetWrapper.pip(false),
      controller: controller,
      decoration: InputDecoration(
          border: const OutlineInputBorder(),
          labelText: labelText,
          hintText: hintText),
    );
  }
}
