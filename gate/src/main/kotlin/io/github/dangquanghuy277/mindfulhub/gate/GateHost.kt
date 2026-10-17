package io.github.dangquanghuy277.mindfulhub.gate

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import io.github.dangquanghuy277.mindfulhub.core.GateSettings
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.data.GateDao

/** What the gate needs from the app. The Application implements [GateHost] (manual DI, S7). */
class GateDeps(val gate: GateDao, val clock: LogicalDayClock, val settings: GateSettings)

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
}
