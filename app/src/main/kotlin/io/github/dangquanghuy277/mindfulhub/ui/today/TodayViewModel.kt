package io.github.dangquanghuy277.mindfulhub.ui.today

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.MorningNudges
import io.github.dangquanghuy277.mindfulhub.PlanReminders
import io.github.dangquanghuy277.mindfulhub.core.AppSession
import io.github.dangquanghuy277.mindfulhub.core.Category
import io.github.dangquanghuy277.mindfulhub.core.Comeback
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.FeedBudget
import io.github.dangquanghuy277.mindfulhub.core.MatchNight
import io.github.dangquanghuy277.mindfulhub.core.MorningCheckIn
import io.github.dangquanghuy277.mindfulhub.core.MorningWindow
import io.github.dangquanghuy277.mindfulhub.core.matchOn
import io.github.dangquanghuy277.mindfulhub.core.NightStatus
import io.github.dangquanghuy277.mindfulhub.core.SleepLatency
import io.github.dangquanghuy277.mindfulhub.core.GoodThings
import io.github.dangquanghuy277.mindfulhub.core.NightClock
import io.github.dangquanghuy277.mindfulhub.core.NightPhase
import io.github.dangquanghuy277.mindfulhub.core.NightState
import io.github.dangquanghuy277.mindfulhub.core.PlanItem
import io.github.dangquanghuy277.mindfulhub.core.Sessionizer
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.core.SettingsCodec
import io.github.dangquanghuy277.mindfulhub.core.SleepQuest
import io.github.dangquanghuy277.mindfulhub.core.SleepTimes
import io.github.dangquanghuy277.mindfulhub.core.UnintendedTime
import io.github.dangquanghuy277.mindfulhub.core.UsageInsights
import io.github.dangquanghuy277.mindfulhub.core.WeekPlan
import io.github.dangquanghuy277.mindfulhub.core.Weeks
import io.github.dangquanghuy277.mindfulhub.core.sleepTimes
import io.github.dangquanghuy277.mindfulhub.data.GoodThingEntity
import io.github.dangquanghuy277.mindfulhub.data.ReactionOfferEntity
import io.github.dangquanghuy277.mindfulhub.data.SleepCheckInEntity
import io.github.dangquanghuy277.mindfulhub.data.millisOf
import io.github.dangquanghuy277.mindfulhub.data.toCore
import io.github.dangquanghuy277.mindfulhub.data.toGateBox
import io.github.dangquanghuy277.mindfulhub.gate.BankLaunchActivity
import io.github.dangquanghuy277.mindfulhub.gate.GateStatus
import io.github.dangquanghuy277.mindfulhub.ui.BankApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A good thing as shown at home: [id] lets you delete one logged by mistake. */
data class LoggedGood(val id: Long, val at: Instant, val category: Category, val minutes: Int, val note: String)

/** F32: what kind of hello today deserves. */
enum class Hello { NONE, WELCOME_BACK, FRESH_START }

/** F61 (trial): last night against your own best recent night. */
data class Receipt(val lastNight: Double, val best: Double, val nights: Int, val bedtime: String)

/** F64: last night's phone-down window, ready to show. Apps are labels, not package names. */
data class NightCard(
    val status: NightStatus,
    val down: Instant?,
    val up: Instant?,
    val plannedBed: Instant,
    val interruptions: Int,
    val interruptionMinutes: Double,
    val interruptionApps: List<String>,
    val firstApp: String?,
    val split: List<Pair<Instant, Instant>>,
    val shutdown: Boolean,
    /** F72: the match you planned for this night, if any. */
    val match: MatchNight? = null,
)

/** F13 + F44 (trial). */
data class QuestState(val started: Boolean, val chapter: Int, val times: SleepTimes, val daysLeft: Int)

data class TodayUiState(
    val today: LocalDate = LocalDate.MIN,
    val settings: Settings = Settings(),
    val night: NightState? = null,
    val gateEnabled: Boolean = true,
    val banksToPin: List<BankApp> = emptyList(),
    val hello: Hello = Hello.NONE,
    val postcard: String? = null,
    val receipt: Receipt? = null,
    /** F65: the night to ask about, while it's unanswered. Sleep numbers wait until it's done. */
    val checkInNight: LocalDate? = null,
    /** F72: last night was a planned match night, so "planned late night" starts ticked. */
    val checkInPlannedLate: Boolean = false,
    val nightCard: NightCard? = null,
    /** F75 (trial): offer the morning reaction check, after the check-in and before work. */
    val reactionDue: Boolean = false,
    val quest: QuestState? = null,
    val feedUsed: Double? = null,
    val plan: List<PlanItem> = emptyList(),
    val hasPlanThisWeek: Boolean = false,
    val good: List<LoggedGood> = emptyList(),
    val weekGood: Map<Category, Int> = emptyMap(),
    val reviewDue: Boolean = false,
)

class TodayViewModel(private val app: Application, private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(TodayUiState())
    val state = _state.asStateFlow()

    private val gated: Set<String> by lazy {
        c.gateSettings.profiles.filterValues { it.name != "MESSAGING" && it.name != "NONE" }.keys
    }

    fun refresh() {
        viewModelScope.launch {
            val now = Instant.now()
            val today = c.clock.dayOf(now)
            val s = c.settingsStore.on(today)
            val night = NightClock.state(now, c.clock, s)
            val weekStart = Weeks.startOf(today)
            val (todayFrom, todayTo) = c.clock.millisOf(today, today.plusDays(1))
            val (weekFrom, weekTo) = c.clock.millisOf(weekStart, weekStart.plusDays(7))
            val goodToday = c.goodThings.between(todayFrom, todayTo)
            val goodWeek = c.goodThings.between(weekFrom, weekTo).map { it.toCore() }
            val plan = c.plans.week(weekStart.toString()).map { it.toCore() }
            val checkInNight = checkInFor(s, now)
            val reactionDue = reactionDueFor(s, now, today, checkInNight)
            if (s.on(Feature.WEEK_PLAN)) PlanReminders.schedule(app, plan, c.clock, now)
            _state.update {
                it.copy(
                    today = today, settings = s, night = night,
                    gateEnabled = GateStatus.isEnabled(app),
                    good = goodToday.map { g -> LoggedGood(g.id, Instant.ofEpochMilli(g.ts), Category.valueOf(g.category), g.minutes, g.note) },
                    weekGood = GoodThings.minutesByCategory(goodWeek, c.clock, Weeks.daysOf(weekStart)),
                    plan = if (s.on(Feature.WEEK_PLAN)) WeekPlan.today(plan, today) else emptyList(),
                    hasPlanThisWeek = plan.isNotEmpty(),
                    reviewDue = s.on(Feature.REVIEW) && today.dayOfWeek == java.time.DayOfWeek.SUNDAY &&
                        c.reviews.get(weekStart.toString()) == null,
                    quest = questOf(s, today),
                    postcard = postcardFor(s, now, today),
                    checkInNight = checkInNight,
                    reactionDue = reactionDue,
                    checkInPlannedLate = checkInNight?.let { n -> c.settingsStore.on(n).matchOn(n) != null } ?: false,
                )
            }
            if (!c.importer.hasPermission()) return@launch
            runCatching { c.importer.importNew() }
            val sessions = withContext(Dispatchers.Default) {
                Sessionizer.sessions(c.usage.events(c.clock.startOf(today.minusDays(8)).minus(Duration.ofHours(3)), now), now)
            }
            val usedBanks = sessions.filter { !it.start.isBefore(now.minus(Duration.ofDays(7))) }.map { it.pkg }.toSet()
            val pinned = BankLaunchActivity.pinned(app)
            // Only banks still installed: usage history remembers uninstalled ones (TCBS, 2026-10-19).
            val banks = c.gateSettings.pauseFor
                .filter { it in usedBanks && it !in pinned && app.packageManager.getLaunchIntentForPackage(it) != null }
                .map { BankApp(it, label(it)) }
            val feedUsed = if (s.on(Feature.FEED_BUDGET)) {
                FeedBudget.usedMinutes(sessions, c.strictApps, c.clock.startOf(today), now)
            } else {
                null
            }
            _state.update {
                it.copy(
                    banksToPin = banks,
                    feedUsed = feedUsed,
                    hello = if (s.on(Feature.COMEBACK)) helloFor(sessions, today) else Hello.NONE,
                    receipt = if (s.on(Feature.MORNING_RECEIPT) && night.phase == NightPhase.DAY && checkInNight == null) receiptFor(sessions, s, today, now) else null,
                    nightCard = if (checkInNight == null) nightCardFor(s, today, now) else null,
                )
            }
        }
    }

    /** F32: a slip yesterday earns a welcome back; otherwise a Monday earns a fresh start. Never both. */
    private suspend fun helloFor(sessions: List<AppSession>, today: LocalDate): Hello {
        val days = (8 downTo 1).map { today.minusDays(it.toLong()) }
        val (from, to) = c.clock.millisOf(days.first(), today)
        val boxes = c.gateDao.sessionsBetween(from, to).map { it.toGateBox() }
        val byDay = withContext(Dispatchers.Default) { UnintendedTime.byDay(sessions, gated, boxes, c.clock, days) }
        return when {
            Comeback.wasSlip(byDay, today.minusDays(1)) -> Hello.WELCOME_BACK
            Comeback.isFreshStart(today) -> Hello.FRESH_START
            else -> Hello.NONE
        }
    }

    /** F61: only in the morning, and only once there are nights to compare. */
    private fun receiptFor(sessions: List<AppSession>, s: Settings, today: LocalDate, now: Instant): Receipt? {
        if (now.atZone(ZoneId.systemDefault()).hour >= 14) return null
        val nights = (7 downTo 1).map { today.minusDays(it.toLong()) }
        val bedtime = s.sleepTimes(today.minusDays(1)).bedtime
        val use = UsageInsights.dayUse(sessions, c.clock, nights, bedtime) { it in gated }
        val last = use.last().afterBedtime
        val earlier = use.dropLast(1)
        if (earlier.isEmpty()) return null
        return Receipt(last, earlier.minOf { it.afterBedtime }, earlier.size, bedtime.toString())
    }

    /** F65: last night's check-in, while unanswered. Its first showing is logged as the ask. */
    private suspend fun checkInFor(s: Settings, now: Instant): LocalDate? {
        if (!s.on(Feature.MORNING_CHECKIN)) return null
        val night = MorningCheckIn.nightToAsk(now, c.clock) ?: return null
        val row = c.checkIns.get(night.toString())
        if (row?.answeredAt != null) return null
        if (row == null) c.checkIns.insertAsked(SleepCheckInEntity(night.toString(), askedAt = now.toEpochMilli()))
        return night
    }

    /**
     * F75: after the check-in (so the result can't colour it, P45), inside the morning window (never
     * in work hours, D-029), until today's run is complete. The first showing is logged.
     */
    private suspend fun reactionDueFor(s: Settings, now: Instant, today: LocalDate, checkInNight: LocalDate?): Boolean {
        if (!s.on(Feature.REACTION_CHECK) || checkInNight != null) return false
        if (!MorningWindow.contains(today, s, c.clock, now)) return false
        if (c.reactions.completedOn(today.toString()) > 0) return false
        c.reactions.offered(ReactionOfferEntity(today.toString(), now.toEpochMilli()))
        return true
    }

    fun answerCheckIn(rested: Int, latency: SleepLatency, plannedLate: Boolean) = checkIn { night, at ->
        c.checkIns.answer(night, at, rested, latency.name, plannedLate)
    }

    fun skipCheckIn(plannedLate: Boolean) = checkIn { night, at -> c.checkIns.skip(night, at, plannedLate) }

    private fun checkIn(write: suspend (String, Long) -> Unit) {
        val night = _state.value.checkInNight ?: return
        viewModelScope.launch {
            write(night.toString(), System.currentTimeMillis())
            MorningNudges.clear(app)
            refresh()
        }
    }

    /** F64: in the morning, once the phone has come back up (or there's nothing to wait for). */
    private suspend fun nightCardFor(s: Settings, today: LocalDate, now: Instant): NightCard? {
        if (!s.on(Feature.NIGHT_LOG) || now.atZone(ZoneId.systemDefault()).hour >= 14) return null
        withContext(Dispatchers.IO) { c.nightLog.update(now) }
        val row = c.nightLog.get(today.minusDays(1)) ?: return null
        val status = NightStatus.valueOf(row.status)
        if (status == NightStatus.FOUND && row.upAt == null) return null
        c.nightLog.markShown(row, now)
        fun ms(v: Long?) = v?.let(Instant::ofEpochMilli)
        return NightCard(
            status = status,
            down = ms(row.downAt),
            up = ms(row.upAt),
            plannedBed = Instant.ofEpochMilli(row.plannedBed),
            interruptions = row.interruptions,
            interruptionMinutes = row.interruptionSec / 60.0,
            interruptionApps = row.interruptionPkgs.split(",").filter { it.isNotBlank() }.distinct().map(::label),
            firstApp = row.firstPkg?.let(::label),
            split = row.otherStretches.split(",").filter { it.isNotBlank() }.map {
                val (a, b) = it.split("-")
                Instant.ofEpochMilli(a.toLong()) to Instant.ofEpochMilli(b.toLong())
            },
            shutdown = row.shutdowns > 0,
            match = c.settingsStore.on(today.minusDays(1)).matchOn(today.minusDays(1)),
        )
    }

    private fun questOf(s: Settings, today: LocalDate): QuestState? {
        if (!s.on(Feature.SLEEP_QUEST)) return null
        val start = s.sleepQuestStart ?: return QuestState(false, 1, SleepQuest.times(1), SleepQuest.DAYS_PER_CHAPTER)
        val chapter = SleepQuest.chapter(start, s.sleepQuestHoldDays, today)
        return QuestState(true, chapter, s.sleepTimes(today), SleepQuest.daysLeft(start, s.sleepQuestHoldDays, today))
    }

    /** F53: last night's postcard, shown in the morning hours of the next day. */
    private suspend fun postcardFor(s: Settings, now: Instant, today: LocalDate): String? {
        if (!s.on(Feature.POSTCARD)) return null
        val hour = now.atZone(ZoneId.systemDefault()).hour
        if (hour >= 15) return null
        return c.postcards.forNight(today.minusDays(1).toString())?.text
    }

    fun logGood(category: Category, minutes: Int, note: String = "") {
        viewModelScope.launch {
            c.goodThings.insert(GoodThingEntity(ts = System.currentTimeMillis(), category = category.name, minutes = minutes, note = note))
            refresh()
        }
    }

    fun deleteGood(id: Long) {
        viewModelScope.launch {
            c.goodThings.delete(id)
            refresh()
        }
    }

    /** F27: marking a planned item done also logs it as a good thing (A5). */
    fun markPlan(item: PlanItem, status: String?) {
        viewModelScope.launch {
            c.plans.setStatus(item.id, status)
            if (status == WeekPlan.DONE && item.status != WeekPlan.DONE) {
                c.goodThings.insert(
                    GoodThingEntity(ts = System.currentTimeMillis(), category = item.category.name, minutes = item.minutes, note = item.title, source = "plan"),
                )
            }
            PlanReminders.cancel(app, item.id)
            refresh()
        }
    }

    fun startQuest() = setAndRefresh(SettingsCodec.QUEST_START, _state.value.today.toString())

    /** "Hold this chapter": the step repeats; nothing is lost (P33). */
    fun holdChapter() = setAndRefresh(SettingsCodec.QUEST_HOLD, (_state.value.settings.sleepQuestHoldDays + SleepQuest.DAYS_PER_CHAPTER).toString())

    private fun setAndRefresh(key: String, value: String) {
        viewModelScope.launch {
            c.settingsStore.set(key, value, c.clock.dayOf(Instant.now()))
            refresh()
        }
    }

    private fun label(pkg: String): String = runCatching {
        val pm = app.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)
}
