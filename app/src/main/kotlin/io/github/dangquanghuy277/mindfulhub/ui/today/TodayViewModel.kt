package io.github.dangquanghuy277.mindfulhub.ui.today

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.PlanReminders
import io.github.dangquanghuy277.mindfulhub.core.AppSession
import io.github.dangquanghuy277.mindfulhub.core.Category
import io.github.dangquanghuy277.mindfulhub.core.Comeback
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.FeedBudget
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
                )
            }
            if (!c.importer.hasPermission()) return@launch
            runCatching { c.importer.importNew() }
            val sessions = withContext(Dispatchers.Default) {
                Sessionizer.sessions(c.usage.events(c.clock.startOf(today.minusDays(8)).minus(Duration.ofHours(3)), now), now)
            }
            val usedBanks = sessions.filter { !it.start.isBefore(now.minus(Duration.ofDays(7))) }.map { it.pkg }.toSet()
            val pinned = BankLaunchActivity.pinned(app)
            val banks = c.gateSettings.pauseFor.filter { it in usedBanks && it !in pinned }.map { BankApp(it, label(it)) }
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
                    receipt = if (s.on(Feature.MORNING_RECEIPT) && night.phase == NightPhase.DAY) receiptFor(sessions, s, today, now) else null,
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
