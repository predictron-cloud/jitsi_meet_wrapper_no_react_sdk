package dev.saibotma.jitsi_meet_wrapper

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import android.R

import android.graphics.Rect

import android.view.MotionEvent





class JitsiMeetWrapperActivity : CustomJitsiMeetActivity() {
    private val eventStreamHandler = JitsiMeetWrapperEventStreamHandler.instance
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            this@JitsiMeetWrapperActivity.onBroadcastReceived(intent)
        }
    }

    companion object {
        fun launch(context: Context, options: JitsiMeetConferenceOptions?) {
            val intent = Intent(context, JitsiMeetWrapperActivity::class.java)
            intent.action = "org.jitsi.meet.CONFERENCE"
            intent.putExtra("JitsiMeetConferenceOptions", options)
            if (context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForBroadcastMessages()
        eventStreamHandler.onOpened()

        // Set as a translucent activity
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.decorView.setBackgroundResource(android.R.color.transparent)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Capture any touches not inside your RelativeLayout (overlay_container)
        val rect = Rect()
        jitsiView.getGlobalVisibleRect(rect)
        return if (!rect.contains(event.getRawX().toInt(), event.getRawY().toInt())) {
            false // Do not consume the touch
        } else super.onTouchEvent(event)
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()
        for (eventType in BroadcastEvent.Type.values()) {
            intentFilter.addAction(eventType.action)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(this.broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            val data = event.data
            when (event.type!!) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> eventStreamHandler.onConferenceJoined(data)
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> eventStreamHandler.onConferenceTerminated(data)
                BroadcastEvent.Type.CONFERENCE_WILL_JOIN -> eventStreamHandler.onConferenceWillJoin(data)
                BroadcastEvent.Type.AUDIO_MUTED_CHANGED -> eventStreamHandler.onAudioMutedChanged(data)
                BroadcastEvent.Type.PARTICIPANT_JOINED -> eventStreamHandler.onParticipantJoined(data)
                BroadcastEvent.Type.PARTICIPANT_LEFT -> eventStreamHandler.onParticipantLeft(data)
                BroadcastEvent.Type.ENDPOINT_TEXT_MESSAGE_RECEIVED -> eventStreamHandler.onEndpointTextMessageReceived(data)
                BroadcastEvent.Type.SCREEN_SHARE_TOGGLED -> eventStreamHandler.onScreenShareToggled(data)
                BroadcastEvent.Type.PARTICIPANTS_INFO_RETRIEVED -> eventStreamHandler.onParticipantsInfoRetrieved(data)
                BroadcastEvent.Type.CHAT_MESSAGE_RECEIVED -> eventStreamHandler.onChatMessageReceived(data)
                BroadcastEvent.Type.CHAT_TOGGLED -> eventStreamHandler.onChatToggled(data)
                BroadcastEvent.Type.VIDEO_MUTED_CHANGED -> eventStreamHandler.onVideoMutedChanged(data)
                BroadcastEvent.Type.READY_TO_CLOSE -> {}
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.broadcastReceiver)
        eventStreamHandler.onClosed()
        super.onDestroy()
    }
}