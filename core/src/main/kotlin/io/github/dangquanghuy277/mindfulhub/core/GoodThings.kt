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

/** [minutes] is how much time it needs, so the menu can match the moment (F59). */
data class InsteadItem(val category: Category, val text: String, val minutes: Int, val action: InsteadAction)

enum class Moment(val label: String) { QUICK("~1 min"), SHORT("~10 min"), LONG("30 min+") }

/**
 * F26 + F59: the "just bored" path offers things to *start*, not a list to read, and only things
 * that fit the time you have (Tran 2019: compulsive sessions start in unoccupied moments). Every
 * line says what to do, never "don't" (P27: negation plans backfire). In bed the read is never on
 * the lit screen (Chang 2015, D-021): an audiobook or a book instead.
 */
object InsteadMenu {
    const val SUBSTACK = "com.substack.app"
    const val MESSENGER = "com.facebook.orca"

    val all: List<InsteadItem> = listOf(
        InsteadItem(Category.REST, "Breathe out slowly, 10 times", 1, InsteadAction.Note),
        InsteadItem(Category.MOVE, "Stand up, stretch, refill your water", 2, InsteadAction.Note),
        InsteadItem(Category.PEOPLE, "Send someone a short voice note", 2, InsteadAction.OpenApp(MESSENGER)),
        InsteadItem(Category.BUILD, "Write down your next coding step", 2, InsteadAction.Note),
        InsteadItem(Category.MOVE, "Walk for 10 minutes, phone in your pocket", 10, InsteadAction.Timer(10)),
        InsteadItem(Category.REST, "Close your eyes for 5 minutes", 5, InsteadAction.Timer(5)),
        InsteadItem(Category.READ, "Read one saved Substack article", 15, InsteadAction.OpenApp(SUBSTACK)),
        InsteadItem(Category.PEOPLE, "Call someone you like", 15, InsteadAction.Dial),
        InsteadItem(Category.BUILD, "One 25-minute lc-trainer session on the laptop", 25, InsteadAction.Timer(25)),
        InsteadItem(Category.MOVE, "A 30-minute walk or workout", 30, InsteadAction.Timer(30)),
    )

    private val bedtime: List<InsteadItem> = listOf(
        InsteadItem(Category.REST, "Put the phone on the charger, lights off", 1, InsteadAction.Note),
        InsteadItem(Category.READ, "An audiobook or podcast, screen off", 20, InsteadAction.Timer(20)),
        InsteadItem(Category.READ, "A few pages of a book or e-ink reader", 15, InsteadAction.Note),
        InsteadItem(Category.REST, "Breathe out slowly, 10 times", 1, InsteadAction.Note),
    )

    fun forMoment(moment: Moment, night: NightPhase = NightPhase.DAY): List<InsteadItem> {
        if (night != NightPhase.DAY) return bedtime
        return when (moment) {
            Moment.QUICK -> all.filter { it.minutes <= 2 }
            Moment.SHORT -> all.filter { it.minutes in 3..15 }
            Moment.LONG -> all.filter { it.minutes >= 15 }
        }
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
