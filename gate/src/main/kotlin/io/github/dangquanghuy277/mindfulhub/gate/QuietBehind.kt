package io.github.dangquanghuy277.mindfulhub.gate

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Quiet behind the gate (D-024): while a gate screen is up, the app underneath stops making sound.
 *
 * It asks for transient audio focus, which makes players pause. Pausing matters beyond the
 * sound: a YouTube video still playing when "never mind" goes home turns into a picture-in-picture
 * window. An app that is still starting (YouTube on a cold open) takes focus back when its video
 * starts, so a loss is answered by asking again, a couple of times. If focus is refused (Android
 * 15 can refuse an app that isn't on top) or the app keeps taking it, the music stream is muted
 * instead. That mute is written down first, so a gate that dies while muted unmutes on its next
 * start rather than leaving the phone silent.
 */
class QuietBehind(context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val prefs = context.getSharedPreferences("quiet_behind", Context.MODE_PRIVATE)
    private val main = Handler(Looper.getMainLooper())
    private var active = false
    private var focused = false
    private var retries = 0
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .setOnAudioFocusChangeListener({ change -> onFocusChange(change) }, main)
        .build()

    init {
        // Left muted by a gate that didn't get to release (crash, update, switched off).
        if (prefs.getBoolean(KEY_MUTED, false)) unmute()
    }

    fun hush() {
        if (active) return
        active = true
        retries = 0
        takeFocus()
    }

    fun release() {
        if (!active) return
        active = false
        if (focused) {
            audio.abandonAudioFocusRequest(request)
            focused = false
        }
        if (prefs.getBoolean(KEY_MUTED, false)) unmute()
    }

    private fun takeFocus() {
        focused = audio.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.i(TAG, "quiet behind: focus ${if (focused) "granted" else "refused"} (try ${retries + 1})")
        if (!focused) mute()
    }

    private fun onFocusChange(change: Int) {
        if (!active || change >= 0) return // gains need nothing; only a loss means the app is back
        focused = false
        if (retries++ < MAX_RETRIES) takeFocus() else mute()
    }

    private fun mute() {
        // Already muted, by you or by us: nothing to do, and nothing to undo later.
        if (prefs.getBoolean(KEY_MUTED, false) || audio.isStreamMute(AudioManager.STREAM_MUSIC)) return
        prefs.edit().putBoolean(KEY_MUTED, true).commit()
        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        Log.i(TAG, "quiet behind: muted media")
    }

    private fun unmute() {
        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        prefs.edit().putBoolean(KEY_MUTED, false).apply()
    }

    private companion object {
        const val TAG = "MindfulGate"
        const val MAX_RETRIES = 2
        const val KEY_MUTED = "muted"
    }
}
