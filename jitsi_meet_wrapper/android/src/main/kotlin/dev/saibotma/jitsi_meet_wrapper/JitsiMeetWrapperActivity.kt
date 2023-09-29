package dev.saibotma.jitsi_meet_wrapper

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import android.view.Gravity





class JitsiMeetWrapperActivity : JitsiMeetActivity() {
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
        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setTitle("")
        super.onCreate(savedInstanceState)
        setTitle("")
        supportActionBar?.hide()
        registerForBroadcastMessages()
        eventStreamHandler.onOpened()

        // Set as a translucent activity
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.decorView.setBackgroundResource(android.R.color.transparent)
    }

    override fun onBackPressed() {
        print("onBackPresseddddd")
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()
        for (eventType in BroadcastEvent.Type.values()) {
            intentFilter.addAction(eventType.action)
        }
        intentFilter.addAction("org.jitsi.meet.PIP");
        intentFilter.addAction("org.jitsi.meet.setSizeAndPosition");
        LocalBroadcastManager.getInstance(this).registerReceiver(this.broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            // enterPictureInPictureMode()
            if (intent!!.getAction() == "org.jitsi.meet.setSizeAndPosition") {
                val width = intent.getExtras()!!.getInt("width");
                val height = intent.getExtras()!!.getInt("height");
                val left = intent.getExtras()!!.getInt("left");
                val top = intent.getExtras()!!.getInt("top");
                var layoutParams = window.getAttributes();
                // Here, you can change the width and height to your desired values
                layoutParams.width = width;  // in pixels
                layoutParams.height = height; // in pixels

                if (left != null && top != null) {
                    layoutParams.gravity = Gravity.TOP or Gravity.LEFT
                    layoutParams.x = left
                    layoutParams.y = top
                }

                window.setAttributes(layoutParams);
                return
            }
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