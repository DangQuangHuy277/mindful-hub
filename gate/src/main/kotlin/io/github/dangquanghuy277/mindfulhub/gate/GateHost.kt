package io.github.dangquanghuy277.mindfulhub.gate

import android.content.ComponentName
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import io.github.dangquanghuy277.mindfulhub.core.GateSettings
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.data.GateDao
import io.github.dangquanghuy277.mindfulhub.data.GoodThingDao
import io.github.dangquanghuy277.mindfulhub.data.PostcardDao
import io.github.dangquanghuy277.mindfulhub.data.SettingsStore
import java.time.Instant

/** What the gate needs from the app. The Application implements [GateHost] (manual DI, S7). */
class GateDeps(
    val gate: GateDao,
    val clock: LogicalDayClock,
    val settings: GateSettings,
    /** D-021: feature switches and calm-you's settings, with precommitment. */
    val rules: SettingsStore,
    val goodThings: GoodThingDao,
    val postcards: PostcardDao,
    /** F29: minutes in STRICT apps so far today, from the system's usage history. */
    val feedMinutesToday: suspend (now: Instant) -> Double,
)

interface GateHost {
    val gateDeps: GateDeps
}

object GateStatus {
    /** True if the user has switched the gate on in Settings > Accessibility. */
    fun isEnabled(context: Context): Boolean {
        val me = ComponentName(context, GateAccessibilityService::class.java)
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    /**
     * True if the app may switch the gate on by itself. Android only gives WRITE_SECURE_SETTINGS
     * over adb: `adb shell pm grant <package> android.permission.WRITE_SECURE_SETTINGS`, once.
     */
    fun canSwitch(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    /** Switches the gate on, keeping any other enabled services. False without [canSwitch]. */
    fun turnOn(context: Context): Boolean {
        if (!canSwitch(context)) return false
        val me = ComponentName(context, GateAccessibilityService::class.java)
        val cr = context.contentResolver
        return runCatching {
            val others = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
                .split(':').filter { it.isNotBlank() && ComponentName.unflattenFromString(it) != me }
            Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, (others + me.flattenToString()).joinToString(":"))
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1)
        }.onFailure { Log.w("MindfulGate", "could not switch the gate on", it) }.isSuccess
    }
}
