package io.github.dangquanghuy277.mindfulhub.gate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import io.github.dangquanghuy277.mindfulhub.core.GateSettings

/**
 * Opens a banking app with the gate already off (D-020). It has no screen of its own.
 *
 * Techcombank checks for accessibility services as its process starts, about 600 ms before the
 * gate sees its window, and closes itself if one is on. One UI's launcher reports nothing when
 * you tap an icon, so the gate can't react sooner. This shortcut switches the gate off first,
 * waits until Android has unbound it, and only then starts the bank.
 */
class BankLaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra(EXTRA_PKG)
        val settings = (application as GateHost).gateDeps.settings
        val launch = pkg?.takeIf(settings::pausesGate)?.let(packageManager::getLaunchIntentForPackage)
        if (launch == null) {
            finish()
            return
        }
        GateAccessibilityService.pauseBeforeLaunch(pkg)
        launchWhenOff(launch, deadline = SystemClock.uptimeMillis() + OFF_TIMEOUT_MS)
    }

    /** The bank asks Android which services are bound, so wait for the unbind, not just the setting. */
    private fun launchWhenOff(launch: Intent, deadline: Long) {
        val on = GateAccessibilityService.isConnected || GateStatus.isEnabled(this)
        if (on && SystemClock.uptimeMillis() < deadline) {
            window.decorView.postDelayed({ launchWhenOff(launch, deadline) }, 25)
            return
        }
        startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    companion object {
        private const val EXTRA_PKG = "pkg"
        private const val OFF_TIMEOUT_MS = 1_000L

        /**
         * Lists the installed pause apps as shortcuts on Mindful Hub's icon (long-press it, then
         * drag one to the home screen in place of the bank's own icon).
         */
        fun publishShortcuts(context: Context, settings: GateSettings) {
            val pm = context.packageManager
            val shortcuts = settings.pauseFor.mapNotNull { pkg ->
                val info = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull() ?: return@mapNotNull null
                pkg to info
            }.mapIndexed { rank, (pkg, info) ->
                ShortcutInfoCompat.Builder(context, "bank-$pkg")
                    .setShortLabel(pm.getApplicationLabel(info))
                    .setIcon(IconCompat.createWithBitmap(pm.getApplicationIcon(info).toBitmap()))
                    .setIntent(Intent(context, BankLaunchActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra(EXTRA_PKG, pkg))
                    .setRank(rank)
                    .build()
            }
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts.take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)))
        }
    }
}
