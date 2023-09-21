package dev.saibotma.jitsi_meet_wrapper

import com.facebook.react.modules.core.PermissionListener
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import org.jitsi.meet.sdk.JitsiMeetActivityInterface


class JitsiStandaloneActivity : FlutterActivity(), JitsiMeetActivityInterface {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine
                .platformViewsController
                .registry
                .registerViewFactory("plugins.jitsi_meet_wrapper:jitsi_meet_native_viewActivity",
                        NativeViewFactory(this))
    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        TODO("Not yet implemented")
    }


}