package io.github.dangquanghuy277.mindfulhub.gate

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.dangquanghuy277.mindfulhub.theme.MindfulTheme

/**
 * A full-screen window drawn by the accessibility service on top of the target app.
 *
 * It uses TYPE_ACCESSIBILITY_OVERLAY rather than "Display over other apps", because Android 15
 * restricts that permission for sideloaded apps (ARCHITECTURE.md, spike item 1). A ComposeView
 * outside an Activity needs its own lifecycle and saved-state owner, provided by [OverlayOwner].
 *
 * While it's up, [quiet] (D-024) stops the app underneath from playing sound, if [quietOn] says so.
 */
class GateOverlay(
    private val service: AccessibilityService,
    private val quiet: QuietBehind,
    private val quietOn: () -> Boolean,
) {
    private val wm = service.getSystemService(WindowManager::class.java)
    private val main = Handler(Looper.getMainLooper())
    // Let go a moment after the screen goes, so "never mind" is already home and the video
    // underneath doesn't pick up again on its way out. A screen shown in between keeps it.
    private val release = Runnable { quiet.release() }
    private var root: View? = null
    private var owner: OverlayOwner? = null

    val isShowing: Boolean get() = root != null

    fun show(onBack: () -> Unit, content: @Composable () -> Unit) {
        dismiss()
        main.removeCallbacks(release)
        if (quietOn()) quiet.hush()
        val o = OverlayOwner().also { it.start() }
        val compose = ComposeView(service).apply {
            setContent {
                MindfulTheme { content() }
            }
        }
        // Back on the overlay means "never mind", not "go back into the app underneath".
        val frame = object : FrameLayout(service) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.action == KeyEvent.ACTION_UP) onBack()
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }
        // Compose resolves these owners from the window's *root* view, so they go on the frame.
        frame.setViewTreeLifecycleOwner(o)
        frame.setViewTreeSavedStateRegistryOwner(o)
        frame.addView(compose)
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            // The postcard (F53) has a text field: keep it above the keyboard. The inset-based
            // replacement needs an Activity window; this overlay isn't one.
            @Suppress("DEPRECATION")
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
        wm.addView(frame, lp)
        root = frame
        owner = o
    }

    fun dismiss() {
        root?.let { runCatching { wm.removeView(it) } }
        owner?.destroy()
        if (root != null) main.postDelayed(release, RELEASE_DELAY_MS)
        root = null
        owner = null
    }

    /** For the gate going away: let go now, there's no next screen. */
    fun destroy() {
        dismiss()
        main.removeCallbacks(release)
        quiet.release()
    }

    private companion object {
        const val RELEASE_DELAY_MS = 700L
    }
}

private class OverlayOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

    fun start() {
        savedState.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun destroy() = registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
}
