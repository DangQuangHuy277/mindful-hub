package io.github.dangquanghuy277.mindfulhub.gate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import androidx.core.content.ContextCompat

/**
 * Testing aid, debug builds only: skips the countdown so a screen behind the gate (Reels, Shorts)
 * can be checked on the phone without waiting it out or adding rows to the study (A6).
 *
 *     adb shell am broadcast -a io.github.dangquanghuy277.mindfulhub.DEBUG_ALLOW --es pkg com.facebook.katana --ei min 10
 *
 * [DUMP] logs the labels the gate sees on screen (tag MindfulGate), for writing detectors. Use it
 * instead of `uiautomator dump`, which unbinds the gate and so forgets the time box.
 *
 *     adb shell am broadcast -a io.github.dangquanghuy277.mindfulhub.DEBUG_DUMP
 *
 * The time box lives in memory only. Only the shell can send it: the sender needs DUMP, which apps
 * don't get. Release builds never register it.
 */
class DebugBypass(
    private val allow: (pkg: String, minutes: Int) -> Unit,
    private val dump: () -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DUMP) return dump()
        val pkg = intent.getStringExtra("pkg") ?: return
        allow(pkg, intent.getIntExtra("min", 10))
    }

    fun register(context: Context): Boolean {
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return false
        ContextCompat.registerReceiver(
            context, this, IntentFilter(ACTION).apply { addAction(DUMP) }, android.Manifest.permission.DUMP, null, ContextCompat.RECEIVER_EXPORTED,
        )
        return true
    }

    companion object {
        const val ACTION = "io.github.dangquanghuy277.mindfulhub.DEBUG_ALLOW"
        const val DUMP = "io.github.dangquanghuy277.mindfulhub.DEBUG_DUMP"
    }
}
