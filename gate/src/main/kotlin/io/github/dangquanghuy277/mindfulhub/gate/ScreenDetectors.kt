package io.github.dangquanghuy277.mindfulhub.gate

import android.view.accessibility.AccessibilityNodeInfo
import io.github.dangquanghuy277.mindfulhub.core.Screen

/**
 * Per-app short-video detectors (ARCHITECTURE.md, "One service, per-app detectors").
 *
 * Facebook strips its view IDs (they read "(name removed)"), so the detectors match the
 * accessibility labels of fixed UI controls, never post content. Nothing they read is stored;
 * only the [Screen] label leaves this file. Labels are English; a Vietnamese UI would need its own.
 * Verified on Facebook for Android, 2026-10-18 (uiautomator dump of the Reels tab).
 */
/**
 * What a detector saw. [escape] is a control that leaves the short-video screen (e.g. Facebook's
 * Home tab), because Back does not leave a *tab* and would re-trigger the block in a loop.
 */
class Detection(val screen: Screen, val escape: AccessibilityNodeInfo? = null)

object ScreenDetectors {
    private val FB_REELS_TAB = Regex("^Reels, tab \\d+ of \\d+$")
    private val FB_HOME_TAB = Regex("^Home, tab \\d+ of \\d+$")
    private val FB_REELS_MARKERS = setOf("Reels tab details", "Navigate to your Reels profile")

    fun detect(pkg: String, root: AccessibilityNodeInfo?): Detection {
        root ?: return Detection(Screen.APP)
        return when (pkg) {
            "com.facebook.katana" -> facebook(root)
            else -> Detection(Screen.APP)
        }
    }

    private fun facebook(root: AccessibilityNodeInfo): Detection {
        var reels = false
        var home: AccessibilityNodeInfo? = null
        walk(root, budget = 400) { node ->
            val desc = node.contentDescription?.toString() ?: return@walk false
            if (desc in FB_REELS_MARKERS || (node.isSelected && FB_REELS_TAB.matches(desc))) reels = true
            if (FB_HOME_TAB.matches(desc)) home = node
            reels && home != null
        }
        return if (reels) Detection(Screen.SHORT_VIDEO, home?.let(::clickableSelfOrParent)) else Detection(Screen.APP)
    }

    private fun clickableSelfOrParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var n: AccessibilityNodeInfo? = node
        repeat(4) {
            if (n?.isClickable == true) return n
            n = n?.parent
        }
        return null
    }

    /** Depth-first walk that stops when [visit] returns true or after [budget] nodes. */
    private fun walk(root: AccessibilityNodeInfo, budget: Int, visit: (AccessibilityNodeInfo) -> Boolean) {
        val stack = ArrayDeque<AccessibilityNodeInfo>().apply { add(root) }
        var seen = 0
        while (stack.isNotEmpty() && seen < budget) {
            val node = stack.removeLast()
            seen++
            if (visit(node)) return
            for (i in node.childCount - 1 downTo 0) node.getChild(i)?.let(stack::addLast)
        }
    }
}
