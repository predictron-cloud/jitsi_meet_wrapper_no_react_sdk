package dev.saibotma.jitsi_meet_wrapper

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.jitsi.meet.sdk.*
import java.net.URL

// Got most of this from the example:
// https://github.com/jitsi/jitsi-meet-sdk-samples/blob/18c35f7625b38233579ff34f761f4c126ba7e03a/android/kotlin/JitsiSDKTest/app/src/main/kotlin/net/jitsi/sdktest/MainActivity.kt
class JitsiMeetWrapperPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var methodChannel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private val eventStreamHandler = JitsiMeetWrapperEventStreamHandler.instance
    private var activity: Activity? = null
    private lateinit var pluginBinding: FlutterPlugin.FlutterPluginBinding

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "jitsi_meet_wrapper")
        methodChannel.setMethodCallHandler(this)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "jitsi_meet_wrapper_events")
        eventChannel.setStreamHandler(eventStreamHandler)

        pluginBinding = flutterPluginBinding;
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "joinMeeting" -> joinMeeting(call, result)
            "setAudioMuted" -> setAudioMuted(call, result)
            "hangUp" -> hangUp(call, result)
            "pip" -> pip(call, result)
            "setSizeAndPosition" -> setSizeAndPosition(call, result)
            "toggleKeyboard" -> toggleKeyboard(call, result)
            "toggleCamera" -> toggleCamera(call, result)
            else -> result.notImplemented()
        }
    }

    private fun setAudioMuted(call: MethodCall, result: Result) {
        val isMuted = call.argument<Boolean>("isMuted") ?: false

        val muteBroadcastIntent: Intent = BroadcastIntentHelper.buildSetAudioMutedIntent(isMuted)
        LocalBroadcastManager.getInstance(activity!!.applicationContext).sendBroadcast(muteBroadcastIntent)

        result.success("Successfully set audio muted to: $isMuted")
    }


    private fun pip(call: MethodCall, result: Result) {
        val enabled = call.argument<Boolean>("enabled") ?: false

        val muteBroadcastIntent = Intent("org.jitsi.meet.PIP")
        muteBroadcastIntent.putExtra("enabled", enabled)
        LocalBroadcastManager.getInstance(activity!!.applicationContext).sendBroadcast(muteBroadcastIntent)
        result.success("Successfully set pip mode: $enabled")
    }

    private fun toggleKeyboard(call: MethodCall, result: Result) {
        val enabled = call.argument<Boolean>("enabled") ?: false

        val muteBroadcastIntent = Intent("org.jitsi.meet.toggleKeyboard")
        muteBroadcastIntent.putExtra("enabled", enabled)
        LocalBroadcastManager.getInstance(activity!!.applicationContext).sendBroadcast(muteBroadcastIntent)
        result.success("Successfully toggleKeyboard: $enabled")
    }



    private fun setSizeAndPosition(call: MethodCall, result: Result) {
        val width = call.argument<Int>("width") ?: 0
        val height = call.argument<Int>("height") ?: 0
        val bottom = call.argument<Int>("bottom")
        val right = call.argument<Int>("right")

        val muteBroadcastIntent = Intent("org.jitsi.meet.setSizeAndPosition")
        muteBroadcastIntent.putExtra("width", width)
        muteBroadcastIntent.putExtra("height", height)
        if (bottom != null) {
            muteBroadcastIntent.putExtra("bottom", bottom!!)
        }
        if (right != null) {
            muteBroadcastIntent.putExtra("right", right!!)
        }
        LocalBroadcastManager.getInstance(activity!!.applicationContext).sendBroadcast(muteBroadcastIntent)
        result.success("Successfully set size and position")
    }

    private fun hangUp(call: MethodCall, result: Result) {
        val hangUpIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager.getInstance(activity!!.applicationContext).sendBroadcast(hangUpIntent)

        result.success("Successfully hung up.")
    }

    private fun toggleCamera(call: MethodCall, result: Result) {
        val hangUpIntent: Intent = BroadcastIntentHelper.buildToggleCameraIntent()
        LocalBroadcastManager.getInstance(activity!!.applicationContext).sendBroadcast(hangUpIntent)

        result.success("Successfully toggled camera.")
    }

    private fun joinMeeting(call: MethodCall, result: Result) {
        // As a general rule: Don't set options when they are null as
        // this could override (reset) configurations from the URL.

        val room = call.argument<String>("roomName")!!
        if (room.isBlank()) {
            result.error(
                    "400",
                    "room can not be null or empty",
                    "room can not be null or empty"
            )
            return
        }

        val serverUrlString: String? = call.argument("serverUrl")
        val serverUrl = if (serverUrlString != null) URL(serverUrlString) else null

        val subject: String? = call.argument("subject")
        val token: String? = call.argument("token")
        val isAudioMuted: Boolean? = call.argument("isAudioMuted")
        val isAudioOnly: Boolean? = call.argument("isAudioOnly")
        val isVideoMuted: Boolean? = call.argument("isVideoMuted")

        val displayName: String? = call.argument("userDisplayName")
        val email: String? = call.argument("userEmail")
        val userAvatarUrlString: String? = call.argument("userAvatarUrl")
        val userInfo = JitsiMeetUserInfo().apply {
            if (displayName != null) this.displayName = displayName
            if (email != null) this.email = email
            if (userAvatarUrlString != null) avatar = URL(userAvatarUrlString)
        }

        val options = JitsiMeetConferenceOptions.Builder().run {
            setRoom(room)
            if (serverUrl != null) setServerURL(serverUrl)
            if (subject != null) setSubject(subject)
            if (token != null) setToken(token)
            if (isAudioMuted != null) setAudioMuted(isAudioMuted)
            if (isAudioOnly != null) setAudioOnly(isAudioOnly)
            if (isVideoMuted != null) setVideoMuted(isVideoMuted)
            if (displayName != null || email != null || userAvatarUrlString != null) {
                setUserInfo(userInfo)
            }

            val featureFlags = call.argument<HashMap<String, Any?>>("featureFlags")
            featureFlags?.forEach { (key, value) ->
                // Can only be bool, int or string according to
                // the overloads of setFeatureFlag.
                when (value) {
                    is Boolean -> setFeatureFlag(key, value as Boolean)
                    is Int -> setFeatureFlag(key, value as Int)
                    else -> setFeatureFlag(key, value.toString())
                }
            }

            val configOverrides = call.argument<HashMap<String, Any?>>("configOverrides")
            configOverrides?.forEach { (key, value) ->
                // Can only be bool, int, array of strings or string according to
                // the overloads of setConfigOverride.
                print("configOverrides. k: $key, v:$value")
                when (value) {
                    is Boolean -> setConfigOverride(key, value as Boolean)
                    is Int -> setConfigOverride(key, value as Int)
                    is Array<*> -> setConfigOverride(key, value as Array<out String>)
                    else -> setConfigOverride(key, value.toString())
                }
            }

            build()
        }

        JitsiMeetWrapperActivity.launch(activity!!, options)
        result.success("Successfully joined room: $room")
    }

    override fun onDetachedFromActivity() {
        this.activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        println("accctivity"+ this.activity!!.javaClass.simpleName)

        pluginBinding
                .platformViewRegistry
                .registerViewFactory("plugins.jitsi_meet_wrapper:jitsi_meet_native_view", NativeViewFactory(binding.activity))

        /*methodChannel = MethodChannel(binding.binaryMessenger, "plugins.jitsi_meet_wrapper:jitsi_meet_native_view_method")
        methodChannel.setMethodCallHandler(this)

        eventChannel = EventChannel(binding.binaryMessenger, "plugins.jitsi_meet_wrapper:jitsi_meet_native_view_event")
        eventChannel.setStreamHandler(eventStreamHandler)*/
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }

}
