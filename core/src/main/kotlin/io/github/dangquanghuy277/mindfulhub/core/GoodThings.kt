package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** The G5 categories: a balanced life, not a max-productivity one (P18). */
enum class Category(val label: String, val emoji: String) {
    MOVE("Move", "🚶"),
    PEOPLE("People", "💛"),
    READ("Read", "📖"),
    BUILD("Build", "🛠️"),
    REST("Rest", "😌"),
}

/** One logged good thing (A5). [source] says where it came from: a tap at home, the gate, a plan. */
data class GoodThing(val at: Instant, val category: Category, val minutes: Int, val note: String = "", val source: String = "home")

object GoodThings {
    /** Minutes per category over [days] (logical days), in category order; zero-filled. */
    fun minutesByCategory(things: List<GoodThing>, clock: LogicalDayClock, days: Collection<LocalDate>): Map<Category, Int> {
        val inRange = things.filter { clock.dayOf(it.at) in days }
        return Category.entries.associateWith { c -> inRange.filter { it.category == c }.sumOf { it.minutes } }
    }
}

/** What tapping an Instead item does (F26). Android carries it out; `:core` only names it. */
sealed interface InsteadAction {
    /** Starts a countdown in the Clock app, then goes home. */
    data class Timer(val minutes: Int) : InsteadAction
    /** Opens another app, if installed; otherwise it's logged like [Note]. */
    data class OpenApp(val pkg: String) : InsteadAction
    /** Opens the phone dialer. */
    data object Dial : InsteadAction
    /** Nothing to open: closing the app and doing it is the action. */
    data object Note : InsteadAction
}

/**
 * [minutes] is how much time it needs, so the menu can match the moment (F59). [key] is stable, so
 * what was offered and picked can be counted over time (D-025). [parts] says when it fits; an
 * [exercise] item also waits for a meal to settle and stops well before bed.
 */
data class InsteadItem(
    val category: Category,
    val text: String,
    val minutes: Int,
    val action: InsteadAction,
    val key: String = text,
    val parts: Set<DayPart> = DayPart.entries.toSet(),
    val exercise: Boolean = false,
)

enum class Moment(val label: String) { QUICK("1–5 min"), SHORT("~15 min"), LONG("30 min+") }

/** How often an item was offered and picked lately (D-025). */
data class OfferStats(val shown: Int, val picked: Int)

/** Everything the "just bored" list depends on, gathered by the gate (D-025). */
data class InsteadContext(
    val part: DayPart = DayPart.FREE,
    val night: NightPhase = NightPhase.DAY,
    val now: java.time.LocalTime = java.time.LocalTime.NOON,
    val schedule: Schedule = Schedule(),
    /** Minutes until tonight's bedtime. */
    val minutesToBed: Long = Long.MAX_VALUE,
    /** Empty until something has been offered: then the order is the one written below. */
    val stats: Map<String, OfferStats> = emptyMap(),
)

/**
 * F26 + F59: the "just bored" path offers things to *start*, not a list to read, and only things
 * that fit the time you have (Tran 2019: compulsive sessions start in unoccupied moments). Every
 * line says what to do, never "don't" (P27: negation plans backfire). In bed the read is never on
 * the lit screen (Chang 2015, D-021): an audiobook or a book instead.
 *
 * D-025 makes it yours: what fits depends on the part of the day (1–5 min at work, up to half an
 * hour at lunch), exercise follows the timing evidence, and what you pick moves up.
 */
object InsteadMenu {
    const val SUBSTACK = "com.substack.app"
    const val MESSENGER = "com.facebook.orca"

    /** Vigorous exercise ending within an hour of bed might delay sleep (Stutz 2019); keep a margin. */
    const val EXERCISE_BEFORE_BED_MIN = 90L

    private val work = setOf(DayPart.WORK, DayPart.LUNCH)
    private val lunch = setOf(DayPart.LUNCH)
    private val lunchAndFree = setOf(DayPart.LUNCH, DayPart.FREE)
    private val free = setOf(DayPart.FREE)

    val all: List<InsteadItem> = listOf(
        InsteadItem(Category.REST, "Breathe out slowly, 10 times", 1, InsteadAction.Note, "breathe"),
        InsteadItem(Category.MOVE, "Stand up, stretch, refill your water", 2, InsteadAction.Note, "stretch"),
        InsteadItem(Category.MOVE, "Walk one floor of stairs, up and down", 2, InsteadAction.Note, "stairs", work),
        InsteadItem(Category.REST, "Look at something far away for a minute", 1, InsteadAction.Note, "look_far", work),
        InsteadItem(Category.MOVE, "Push-ups and a plank, 5 minutes", 5, InsteadAction.Timer(5), "pushups", free, exercise = true),
        InsteadItem(Category.REST, "Close your eyes for 5 minutes", 5, InsteadAction.Timer(5), "eyes_5", lunchAndFree),
        InsteadItem(Category.PEOPLE, "Send someone a short voice note", 2, InsteadAction.OpenApp(MESSENGER), "voice_note", lunchAndFree),
        InsteadItem(Category.BUILD, "Write down your next coding step", 2, InsteadAction.Note, "next_step", free),
        InsteadItem(Category.REST, "Eyes closed for 15 minutes", 15, InsteadAction.Timer(15), "nap_15", lunch),
        InsteadItem(Category.READ, "Read one saved Substack article", 15, InsteadAction.OpenApp(SUBSTACK), "read", lunchAndFree),
        InsteadItem(Category.MOVE, "Walk for 10 minutes, phone in your pocket", 10, InsteadAction.Timer(10), "walk_10", free),
        InsteadItem(Category.MOVE, "15-minute bodyweight circuit: push-ups, squats, plank", 15, InsteadAction.Timer(15), "circuit", free, exercise = true),
        InsteadItem(Category.PEOPLE, "Call someone you like", 15, InsteadAction.Dial, "call", free),
        InsteadItem(Category.PEOPLE, "Eat with the team, phone in your pocket", 30, InsteadAction.Note, "eat_team", lunch),
        InsteadItem(Category.BUILD, "One 25-minute lc-trainer session on the laptop", 25, InsteadAction.Timer(25), "lc_trainer", free),
        InsteadItem(Category.MOVE, "A 30-minute walk", 30, InsteadAction.Timer(30), "walk_30", free),
    )

    private val bedtime: List<InsteadItem> = listOf(
        InsteadItem(Category.REST, "Put the phone on the charger, lights off", 1, InsteadAction.Note, "bed_charger"),
        InsteadItem(Category.READ, "An audiobook or podcast, screen off", 20, InsteadAction.Timer(20), "bed_audio"),
        InsteadItem(Category.READ, "A few pages of a book or e-ink reader", 15, InsteadAction.Note, "bed_book"),
        InsteadItem(Category.REST, "Breathe out slowly, 10 times", 1, InsteadAction.Note, "breathe"),
    )

    /** The time chips for this part of the day: at work there's only time for something quick. */
    fun momentsFor(part: DayPart): List<Moment> = when (part) {
        DayPart.WORK -> listOf(Moment.QUICK)
        DayPart.LUNCH, DayPart.FREE -> Moment.entries
    }

    fun defaultMoment(part: DayPart): Moment = if (part == DayPart.WORK) Moment.QUICK else Moment.SHORT

    fun forMoment(moment: Moment, ctx: InsteadContext = InsteadContext()): List<InsteadItem> {
        if (ctx.night != NightPhase.DAY) return bedtime
        val fits = all.filter { item ->
            ctx.part in item.parts && item.minutes in range(moment) && (!item.exercise || exerciseOk(ctx))
        }
        return rank(fits, ctx.stats)
    }

    fun exerciseOk(ctx: InsteadContext): Boolean =
        !ctx.schedule.nearMeal(ctx.now) && ctx.minutesToBed > EXERCISE_BEFORE_BED_MIN

    private fun range(moment: Moment): IntRange = when (moment) {
        Moment.QUICK -> 0..5
        Moment.SHORT -> 6..15
        Moment.LONG -> 16..Int.MAX_VALUE
    }

    /**
     * Gentle learning (D-025): order by pick rate with one pick and one skip already counted, so
     * an item needs several offers before it moves, and a skipped one sinks but is never hidden.
     * Ties keep the written order.
     */
    fun rank(items: List<InsteadItem>, stats: Map<String, OfferStats>): List<InsteadItem> {
        if (stats.isEmpty()) return items
        fun score(i: InsteadItem): Double = stats[i.key].let { s -> ((s?.picked ?: 0) + 1.0) / ((s?.shown ?: 0) + 2.0) }
        return items.sortedByDescending(::score) // stable: equal scores keep their order
    }
}

/** F29: one budget shared by every STRICT app (Allcott 2022: a limit on one app leaks to the others). */
object FeedBudget {
    /** Minutes of [strict] apps in [from, to). */
    fun usedMinutes(sessions: List<AppSession>, strict: Set<String>, from: Instant, to: Instant): Double =
        sessions.filter { it.pkg in strict }.sumOf { s ->
            val start = maxOf(s.start, from)
            val end = minOf(s.end, to)
            if (end.isAfter(start)) Duration.between(start, end).toMillis() / 60_000.0 else 0.0
        }

    fun left(budgetMinutes: Int, used: Double): Double = (budgetMinutes - used).coerceAtLeast(0.0)
}
