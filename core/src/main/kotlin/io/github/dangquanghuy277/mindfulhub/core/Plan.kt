package io.github.dangquanghuy277.mindfulhub.core

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * F27: one planned good thing, an appointment rather than a wish (behavioural activation, P28).
 * [status] is null until you mark it; "DONE" also logs it as a good thing (A5).
 */
data class PlanItem(
    val id: Long = 0,
    val weekStart: LocalDate,
    val day: DayOfWeek,
    val time: LocalTime,
    val category: Category,
    val minutes: Int,
    val title: String,
    val status: String? = null,
)

object WeekPlan {
    const val DONE = "DONE"
    const val SKIPPED = "SKIPPED"

    /** The logical day an item falls on. */
    fun dateOf(item: PlanItem): LocalDate = item.weekStart.plusDays(item.day.value - 1L)

    fun instantOf(item: PlanItem, clock: LogicalDayClock): Instant = clock.instantOn(dateOf(item), item.time)

    fun today(items: List<PlanItem>, today: LocalDate): List<PlanItem> =
        items.filter { dateOf(it) == today }.sortedBy { it.time }

    /**
     * Suggested slots: the evening hours where your heatmap shows the most gated use, as starting
     * points for the plan. Bedtime is left out on purpose: that slot belongs to sleep (P19), and
     * the wind-down and postcard cover it.
     */
    fun suggestedHours(averageByHour: List<Double>, hours: List<Int>, bedtime: LocalTime, count: Int = 3): List<Int> {
        val bedHour = bedtime.hour
        return hours.indices
            .filter { val h = hours[it]; h in 7..22 && h != bedHour && (h + 1) % 24 != bedHour }
            .sortedByDescending { averageByHour[it] }
            .take(count)
            .map { hours[it] }
            .sorted()
    }
}

/**
 * F17 (trial): the calm launcher, links only (FEATURES-V1: "links only, no fetching"). These are
 * the real reasons you open the feeds (REQUIREMENTS, "Real needs"), served somewhere finite (P4).
 * A link with [pkg] opens that app; otherwise [url] opens in the browser.
 */
data class CalmLink(val emoji: String, val title: String, val url: String, val pkg: String? = null)

object CalmLinks {
    val defaults = listOf(
        CalmLink("🟧", "Hacker News", "https://news.ycombinator.com/front"),
        CalmLink("📰", "VnExpress", "https://vnexpress.net"),
        CalmLink("🔵🔴", "Barça news (Vietnamese)", "https://news.google.com/search?q=Barcelona&hl=vi&gl=VN&ceid=VN:vi"),
        CalmLink("✉️", "Substack", "https://substack.com/inbox", pkg = "com.substack.app"),
    )
}

/**
 * F60: set up the room, not the willpower (P1, Duckworth 2018). A short checklist at the end of the
 * plan. No paper books yet (D-021), so the read item names the options that aren't a lit screen.
 */
object SetupKit {
    val defaults = listOf(
        "Charger across the room, an arm's reach plus a step from the bed (Z3)",
        "Tonight's read by the bed: a paperback, an e-ink reader, or earbuds for an audiobook",
        "Shoes and a jacket by the door for the walks you planned",
        "Laptop open on lc-trainer where you'll sit, so building starts in one move",
        "A glass of water on the desk",
    )
}
