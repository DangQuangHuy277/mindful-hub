package io.github.dangquanghuy277.mindfulhub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import androidx.core.content.ContextCompat
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.ui.reaction.ReactionActivity
import java.time.Duration

/**
 * Testing aid, debug builds only, like `DebugBypass` in :gate: tries the D-030 nudge and the F75
 * check on the phone at any hour without adding rows to the study (A6).
 *
 *     adb shell am broadcast -a io.github.dangquanghuy277.mindfulhub.DEBUG_NUDGE --ei sec 60
 *
 * posts the morning notification, clearing itself after `sec` seconds. It belongs to no day, so a
 * tap or a swipe updates nothing.
 *
 *     adb shell am broadcast -a io.github.dangquanghuy277.mindfulhub.DEBUG_REACTION
 *
 * opens the reaction check as a practice run that saves nothing (its score goes to logcat, tag
 * MindfulMorning). Open the app first: Android won't start a screen from the background.
 *
 *     adb shell am broadcast -a io.github.dangquanghuy277.mindfulhub.DEBUG_PREVIEW_MORNING --ez on true
 *
 * shows the morning cards on Today at any hour (last night's card), without logging that they were
 * shown. Lives in memory only; `--ez on false` or a restart ends it.
 *
 * Only the shell can send these: the sender needs DUMP, which apps don't get.
 */
object MorningDebug {
    private const val NUDGE = "io.github.dangquanghuy277.mindfulhub.DEBUG_NUDGE"
    private const val REACTION = "io.github.dangquanghuy277.mindfulhub.DEBUG_REACTION"
    private const val PREVIEW = "io.github.dangquanghuy277.mindfulhub.DEBUG_PREVIEW_MORNING"

    /** True only while previewing on a debug build: morning cards show, and their showing isn't logged. */
    @Volatile
    var previewMorning = false
        private set

    fun register(context: Context) {
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    NUDGE -> MorningNudges.post(ctx, day = "debug", Duration.ofSeconds(intent.getIntExtra("sec", 60).toLong()), MorningNudges.text(Settings()))
                    REACTION -> ctx.startActivity(
                        Intent(ctx, ReactionActivity::class.java)
                            .putExtra(ReactionActivity.EXTRA_PRACTICE, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                    PREVIEW -> previewMorning = intent.getBooleanExtra("on", true)
                }
            }
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(NUDGE).apply { addAction(REACTION); addAction(PREVIEW) }, android.Manifest.permission.DUMP, null, ContextCompat.RECEIVER_EXPORTED,
        )
    }
}
