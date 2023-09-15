package dev.saibotma.jitsi_meet_wrapper

import android.app.Activity
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Bundle
import android.view.View
import io.flutter.plugin.platform.PlatformView
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

internal class NativeView(val activity: Activity, context: Context, id: Int, creationParams: Map<String?, Any?>?) : PlatformView {
    private val jitsiMeetView: JitsiMeetView

    override fun getView(): View {
        return jitsiMeetView
    }

    override fun dispose() {}

    init {


        val userInfo = JitsiMeetUserInfo()
        userInfo.displayName = creationParams?.get("userDisplayName")?.toString()
        userInfo.email = creationParams?.get("userEmail")?.toString()
        if (creationParams?.get("userAvatarURL") != null) {
            userInfo.avatar = URL(creationParams?.get("userAvatarURL")?.toString())
        }
        val optionsBuilder = JitsiMeetConferenceOptions.Builder()
        val audioMuted: Boolean = true
        val videoMuted: Boolean = true
        val featureFlags : HashMap<String, Boolean> = creationParams?.get("featureFlags") as HashMap<String, Boolean>

        optionsBuilder
                .setServerURL(URL(creationParams?.get("serverUrl")?.toString()))
                .setRoom(creationParams?.get("roomNameOrUrl")?.toString())
                .setSubject(creationParams?.get("subject")?.toString())
                .setAudioMuted(audioMuted)
                .setVideoMuted(videoMuted)
                .setUserInfo(userInfo)
        val configOverrides : HashMap<String, Any?> = creationParams?.get("configOverrides") as HashMap<String, Any?>
        configOverrides?.forEach { (key, value) ->
            // Can only be bool, int, array of strings or string according to
            // the overloads of setConfigOverride.
            when (value) {
                is Boolean -> optionsBuilder.setConfigOverride(key, value)
                is Int -> optionsBuilder.setConfigOverride(key, value)
                is Array<*> -> optionsBuilder.setConfigOverride(key, value as Array<out String>)
                is Bundle -> optionsBuilder.setConfigOverride(key, value)
                else -> {
                    println("CONFIG VALUE TYPE UNKNOWN FOR "+ key + ", value.toString()");
                    optionsBuilder.setConfigOverride(key, value.toString())}
            }
        }

        if (featureFlags != null) {
            for ((k, v) in featureFlags.iterator()) {
                optionsBuilder.setFeatureFlag(k,v)
            }
        }
        println("simpleName: " + activity.javaClass.simpleName);
        jitsiMeetView = JitsiMeetView(activity)
        jitsiMeetView.join(optionsBuilder.build())
    }
}