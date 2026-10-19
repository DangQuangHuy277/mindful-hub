package io.github.dangquanghuy277.mindfulhub.core

import java.time.LocalDate
import java.time.LocalTime

/** APPROVED = you said yes (D-021). TRIAL = built so you can try it, not decided yet. */
enum class FeatureStatus { APPROVED, TRIAL }

/**
 * Every feature added after the gate core has a switch (D-021), so an unsure one can be turned off
 * today and deleted later. To remove one for good: delete its entry here, its files (each feature
 * keeps its own), and its card or screen hook. [restricts] marks features that limit you: turning
 * those *off* waits until tomorrow, like any other loosening (P2, E4, S4).
 */
enum class Feature(
    val ref: String,
    val title: String,
    val summary: String,
    val status: FeatureStatus,
    val restricts: Boolean = false,
) {
    COMEBACK("F32", "Comeback, not streaks", "A kind \"welcome back\" after a long day, and a fresh start on Mondays.", FeatureStatus.APPROVED),
    WORTH_IT("F10", "Worth-it rating", "👍 😐 👎 when a time box ends. Leaving early never asks, and counts as a win.", FeatureStatus.APPROVED),
    INSTEAD("F26 + F59", "One-tap instead", "\"Just bored\" offers things you can start in one tap, sized to the time you have.", FeatureStatus.APPROVED),
    GOOD_LOG("A5", "Log good things", "One tap to log a walk, a call, a read, some building or rest.", FeatureStatus.APPROVED),
    REVIEW("F11 + F12", "Weekly review", "Sunday: your week in numbers, WHO-5, and one change.", FeatureStatus.APPROVED),
    WEEK_PLAN("F27 + F60", "Week plan + setup kit", "Plan 3–5 good things into your risky times, then set up the room for them.", FeatureStatus.APPROVED),
    FEED_BUDGET("F29", "Feeds budget", "One daily budget shared by Facebook, Instagram and Threads.", FeatureStatus.APPROVED, restricts = true),
    WIND_DOWN("F03", "Wind-down lock", "Feeds, games and YouTube rest from before bedtime until your wake time.", FeatureStatus.APPROVED, restricts = true),
    POSTCARD("F31 + F53", "Postcard to tomorrow", "At night, write one line about a good thing tomorrow. It waits for you in the morning.", FeatureStatus.APPROVED),
    SLEEP_QUEST("F13 + F44", "Sleep-shift quest", "Moves your wake time 15 min earlier every 3 days, told as chapters.", FeatureStatus.TRIAL),
    MORNING_RECEIPT("F61", "Morning receipt", "A calm card each morning: last night against your own best night.", FeatureStatus.TRIAL),
    CALM_LINKS("F17", "Calm links", "One card of finite, calm sources (HN, VnExpress, Barça news, Substack). Group time (F18) waits for your group links.", FeatureStatus.TRIAL),
    ;

    companion object {
        fun byKey(key: String): Feature? = entries.firstOrNull { it.key == key }
    }

    val key: String get() = "feature." + name.lowercase()
}

/** Everything calm-you sets. Read through [SettingsCodec]; written through [Precommit]. */
data class Settings(
    val enabled: Set<Feature> = Feature.entries.toSet(),
    /**
     * F29: one budget for every STRICT app together, in minutes per logical day. The default is
     * about 30% under your baseline (FB ≈ 1.7 h/day + Threads + IG ≈ 130 min, BASELINE.md).
     * Lowering it applies at once, so starting a little loose is the safe way round.
     */
    val feedBudgetMinutes: Int = 90,
    /** Manual sleep times, used when the sleep quest is off. D-007: start where you are. */
    val bedtime: LocalTime = LocalTime.of(2, 0),
    val wakeTime: LocalTime = LocalTime.of(9, 45),
    /** F03: the lock starts this long before bedtime (S1). */
    val windDownMinutes: Int = 45,
    /** F03: one 10-minute scroll in bed per night, while the "scroll to fall asleep" question is open (D-021). */
    val allowBedScroll: Boolean = false,
    /** F13: the day the quest started; null = not started. */
    val sleepQuestStart: LocalDate? = null,
    /** F13: days added by "hold this chapter". */
    val sleepQuestHoldDays: Int = 0,
) {
    fun on(f: Feature): Boolean = f in enabled
}

/** Settings are stored as text key/value rows; this keeps the mapping in one tested place. */
object SettingsCodec {
    const val FEED_BUDGET = "feedBudgetMinutes"
    const val BEDTIME = "bedtime"
    const val WAKE = "wakeTime"
    const val WIND_DOWN = "windDownMinutes"
    const val BED_SCROLL = "allowBedScroll"
    const val QUEST_START = "sleepQuestStart"
    const val QUEST_HOLD = "sleepQuestHoldDays"

    fun decode(values: Map<String, String>): Settings {
        val d = Settings()
        val enabled = Feature.entries.filter { f -> values[f.key]?.toBooleanStrictOrNull() ?: true }.toSet()
        return Settings(
            enabled = enabled,
            feedBudgetMinutes = values[FEED_BUDGET]?.toIntOrNull() ?: d.feedBudgetMinutes,
            bedtime = values[BEDTIME]?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: d.bedtime,
            wakeTime = values[WAKE]?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: d.wakeTime,
            windDownMinutes = values[WIND_DOWN]?.toIntOrNull() ?: d.windDownMinutes,
            allowBedScroll = values[BED_SCROLL]?.toBooleanStrictOrNull() ?: d.allowBedScroll,
            sleepQuestStart = values[QUEST_START]?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            sleepQuestHoldDays = values[QUEST_HOLD]?.toIntOrNull() ?: d.sleepQuestHoldDays,
        )
    }
}

/** One stored setting: [value] counts from logical day [effectiveFrom] on. */
data class SettingRow(val key: String, val value: String, val effectiveFrom: LocalDate)

object SettingsHistory {
    /** The value of each key on [day]: the row with the latest start on or before it. */
    fun valuesOn(rows: List<SettingRow>, day: LocalDate): Map<String, String> =
        rows.filter { !it.effectiveFrom.isAfter(day) }
            .groupBy { it.key }
            .mapValues { (_, r) -> r.maxBy { it.effectiveFrom }.value }
}

/**
 * Precommitment lives in the data model (ARCHITECTURE.md): a change that *loosens* a rule takes
 * effect on the next logical day; a change that tightens it, or isn't a rule at all, applies today.
 */
object Precommit {
    fun effectiveFrom(key: String, old: String?, new: String, today: LocalDate): LocalDate =
        if (loosens(key, old, new)) today.plusDays(1) else today

    fun loosens(key: String, old: String?, new: String): Boolean {
        Feature.byKey(key)?.let { f ->
            val wasOn = old?.toBooleanStrictOrNull() ?: true
            return f.restricts && wasOn && new == "false"
        }
        return when (key) {
            SettingsCodec.FEED_BUDGET -> (new.toIntOrNull() ?: 0) > (old?.toIntOrNull() ?: Settings().feedBudgetMinutes)
            SettingsCodec.WIND_DOWN -> (new.toIntOrNull() ?: 0) < (old?.toIntOrNull() ?: Settings().windDownMinutes)
            SettingsCodec.BED_SCROLL -> new == "true" && old != "true"
            SettingsCodec.BEDTIME -> later(old?.let(LocalTime::parse) ?: Settings().bedtime, LocalTime.parse(new))
            SettingsCodec.WAKE -> later(old?.let(LocalTime::parse) ?: Settings().wakeTime, LocalTime.parse(new))
            else -> false
        }
    }

    /**
     * True if [new] is later in the night than [old]. Times are compared on a night that starts at
     * noon, so 01:00 is later than 23:30.
     */
    private fun later(old: LocalTime, new: LocalTime): Boolean {
        fun key(t: LocalTime) = (t.toSecondOfDay() - 12 * 3600 + 86_400) % 86_400
        return key(new) > key(old)
    }
}
