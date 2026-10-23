package io.github.dangquanghuy277.mindfulhub.gate

import android.view.accessibility.AccessibilityNodeInfo
import io.github.dangquanghuy277.mindfulhub.core.Screen

/**
 * Per-app short-video detectors (ARCHITECTURE.md, "One service, per-app detectors").
 *
 * Facebook strips its view IDs (they read "(name removed)"), so the detectors match the
 * accessibility labels of fixed UI controls, never post content. Nothing they read is stored;
 * only the [Screen] label leaves this file. Facebook follows the phone's language, so each label
 * matches English and Vietnamese: the English-only labels missed every Reels visit once the phone
 * was in Vietnamese ("Reels, Tab 2/6"). Verified on Facebook for Android, 2026-10-18 (English) and
 * 583.0.0.39.55, 2026-10-23 (Vietnamese).
 *
 * YouTube keeps its view IDs, which don't change with the language. Verified on 21.42.243.
 */
/**
 * What a detector saw. [escape] is a control that leaves the short-video screen (e.g. Facebook's
 * Home tab), because Back does not leave a *tab* and would re-trigger the block in a loop.
 */
class Detection(val screen: Screen, val escape: AccessibilityNodeInfo? = null)

object ScreenDetectors {
    // "Reels, tab 2 of 6" (English) or "Reels, Tab 2/6" (Vietnamese).
    private val FB_REELS_TAB = Regex("^Reels, [Tt]ab \\d+(?: of |/)\\d+$")
    // Home is always the first tab: "Home, tab 1 of 6" or "Trang chủ, Tab 1/6".
    private val FB_HOME_TAB = Regex("^.+, [Tt]ab 1(?: of |/)\\d+$")
    private val FB_REELS_MARKERS = setOf(
        "Reels tab details", "Navigate to your Reels profile",
        "Chi tiết về tab Reels", "Chuyển đến trang cá nhân Reels của bạn",
    )

    /** The Shorts player, in the Shorts tab or opened from a shelf or link. Gone once you leave. */
    private const val YT_SHORTS_PLAYER = "com.google.android.youtube:id/reel_player_page_container"
    private const val YT_TAB_BAR = "com.google.android.youtube:id/pivot_bar"

    fun detect(pkg: String, root: AccessibilityNodeInfo?): Detection {
        root ?: return Detection(Screen.APP)
        return when (pkg) {
            "com.facebook.katana" -> facebook(root)
            "com.google.android.youtube" -> youtube(root)
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

    /**
     * Shorts, wherever they were opened. Back leaves a Short opened from the feed but not the
     * Shorts *tab*, so the escape is the tab bar's first button (Home) when the bar is there.
     */
    private fun youtube(root: AccessibilityNodeInfo): Detection {
        if (root.findAccessibilityNodeInfosByViewId(YT_SHORTS_PLAYER).isEmpty()) return Detection(Screen.APP)
        var home: AccessibilityNodeInfo? = null
        root.findAccessibilityNodeInfosByViewId(YT_TAB_BAR).firstOrNull()?.let { bar ->
            walk(bar, budget = 20) { node -> (node !== bar && node.isClickable).also { if (it) home = node } }
        }
        return Detection(Screen.SHORT_VIDEO, home)
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
