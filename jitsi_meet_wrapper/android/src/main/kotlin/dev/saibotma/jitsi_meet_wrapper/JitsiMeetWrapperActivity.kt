package dev.saibotma.jitsi_meet_wrapper

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions


class JitsiMeetWrapperActivity : JitsiMeetActivity() {
    private var width: Int = -2;
    private var height: Int = -2;
    private var right: Int = -2;
    private var bottom: Int = -2;
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
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide();
        }
        supportActionBar?.hide()
        registerForBroadcastMessages()
        eventStreamHandler.onOpened()

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
        intentFilter.addAction("org.jitsi.meet.toggleKeyboard");
        LocalBroadcastManager.getInstance(this).registerReceiver(this.broadcastReceiver, intentFilter)
    }

    private fun toggleKeyboard(show: Boolean) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        /* var view = findViewById<View>(android.R.id.content)
         view.requestFocus();*/
        var view = getJitsiView();
        if (show) {
            if (!imm.isAcceptingText()) {
                // Show keyboard
                imm.showSoftInput(window.decorView.rootView, 0)
            }
        } else {
            if (imm.isAcceptingText()) {
                // Hide keyboard
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Pass all touch events to the underlying activity or view.
        return false
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent == null) {
            return
        }
        try {
            if (intent.action == "org.jitsi.meet.setSizeAndPosition") {
                setSizeAndPosition(intent.getExtras()!!.getInt("width"),
                        intent.getExtras()!!.getInt("height"),
                        intent.getExtras()!!.getInt("right"),
                        intent.getExtras()!!.getInt("bottom"))
                return
            }

            if (intent.action == "org.jitsi.meet.toggleKeyboard") {
                toggleKeyboard(intent.extras!!.getBoolean("enabled"))
                return
            }
            if (intent.action == "org.jitsi.meet.PIP") {
                enterPip(intent.extras!!.getBoolean("enabled"))
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
                BroadcastEvent.Type.CONFERENCE_BLURRED -> eventStreamHandler.onScreenShareToggled(data)
                BroadcastEvent.Type.CONFERENCE_FOCUSED -> eventStreamHandler.onScreenShareToggled(data)
                BroadcastEvent.Type.READY_TO_CLOSE -> {}
            }
        } catch (e: Exception) {
            print("Exception ${e.cause}, stacktrace: ${e.stackTrace}");
        }
    }

    private fun enterPip(enabled: Boolean) {
        if (enabled) {
            if (!isInPictureInPictureMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pipBuilder = PictureInPictureParams.Builder()
                    pipBuilder.setActions(emptyList());
                    enterPictureInPictureMode(pipBuilder.build());
                } else {
                    enterPictureInPictureMode()
                }
            }
        } else {
            print("Exit from pip mode somehow...")
            val startIntent = Intent(this@JitsiMeetWrapperActivity, JitsiMeetWrapperActivity::class.java)
            startIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            this.startActivity(startIntent)
        }
    }

    private fun setSizeAndPosition(width: Int, height: Int, right: Int, bottom: Int) {
        this.width = width;
        this.height = height;
        this.right = right;
        this.bottom = bottom;
        val layoutParams = window.attributes;
        // Here, you can change the width and height to your desired values
        if (width != null && height != null) {
            layoutParams.width = width;  // in pixels
            layoutParams.height = height; // in pixels
        }

        if (right != null && bottom != null) {
            layoutParams.gravity = Gravity.BOTTOM or Gravity.RIGHT
            layoutParams.x = right
            layoutParams.y = bottom
        }

        window.attributes = layoutParams;
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        if (isInPictureInPictureMode) {
            print("Enter pipppp:" + newConfig!!.screenHeightDp + ", " + newConfig.screenHeightDp)
            val layoutParams = window.attributes;
            layoutParams.width = -2;  // in pixels
            layoutParams.height = -2; // in pixels
            layoutParams.x = 0
            layoutParams.y = 0
            window.attributes = layoutParams;
        } else {
            print("exit pip:" + newConfig!!.screenHeightDp + ", " + newConfig.screenHeightDp)
            val layoutParams = window.attributes;
            layoutParams.width = width;  // in pixels
            layoutParams.height = height; // in pixels
            layoutParams.x = right
            layoutParams.y = bottom
            window.attributes = layoutParams;
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.broadcastReceiver)
        eventStreamHandler.onClosed()
        super.onDestroy()
    }
}