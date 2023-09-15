package dev.saibotma.jitsi_meet_wrapper_example

import io.flutter.embedding.android.FlutterActivity
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import com.facebook.react.modules.core.PermissionListener
import io.flutter.embedding.android.FlutterFragmentActivity

class MainActivity: FlutterFragmentActivity(), JitsiMeetActivityInterface {

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        TODO("Not yet implemented")
    }
}
