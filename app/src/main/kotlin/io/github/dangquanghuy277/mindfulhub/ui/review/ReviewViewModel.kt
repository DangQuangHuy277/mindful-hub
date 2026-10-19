package io.github.dangquanghuy277.mindfulhub.ui.review

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.PlanReminders
import io.github.dangquanghuy277.mindfulhub.core.AppProfile
import io.github.dangquanghuy277.mindfulhub.core.Category
import io.github.dangquanghuy277.mindfulhub.core.PlanItem
import io.github.dangquanghuy277.mindfulhub.core.Sessionizer
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.core.UsageAnalytics
import io.github.dangquanghuy277.mindfulhub.core.WeekPlan
import io.github.dangquanghuy277.mindfulhub.core.WeekReview
import io.github.dangquanghuy277.mindfulhub.core.WeekSummary
import io.github.dangquanghuy277.mindfulhub.core.Weeks
import io.github.dangquanghuy277.mindfulhub.core.Who5
import io.github.dangquanghuy277.mindfulhub.core.sleepTimes
import io.github.dangquanghuy277.mindfulhub.data.SetupKitEntity
import io.github.dangquanghuy277.mindfulhub.data.WeeklyReviewEntity
import io.github.dangquanghuy277.mindfulhub.data.millisOf
import io.github.dangquanghuy277.mindfulhub.data.toCore
import io.github.dangquanghuy277.mindfulhub.data.toEntity
import io.github.dangquanghuy277.mindfulhub.data.toGateBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class ReviewUiState(
    val today: LocalDate = LocalDate.MIN,
    val settings: Settings = Settings(),
    val weekStart: LocalDate = LocalDate.MIN,
    val summary: WeekSummary? = null,
    val who5: List<Int?> = List(Who5.items.size) { null },
    val lastWho5: Int? = null,
    val missed: String = "",
    val oneChange: String = "",
    val saved: Boolean = false,
    /** F27: which week the plan section edits. On Sunday that's next week. */
    val planWeek: LocalDate = LocalDate.MIN,
    val planItems: List<PlanItem> = emptyList(),
    val suggestedHours: List<Int> = emptyList(),
    val kitChecked: Set<Int> = emptySet(),
)

class ReviewViewModel(private val app: Application, private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ReviewUiState())
    val state = _state.asStateFlow()

    private val gated: Set<String> by lazy {
        c.gateSettings.profiles.filterValues { it != AppProfile.MESSAGING && it != AppProfile.NONE }.keys
    }

    fun refresh() {
        viewModelScope.launch {
            val now = Instant.now()
            val today = c.clock.dayOf(now)
            val weekStart = Weeks.startOf(today)
            val s = c.settingsStore.on(today)
            val saved = c.reviews.get(weekStart.toString())
            val previous = c.reviews.latest(2).firstOrNull { it.weekStart != weekStart.toString() }
            val planWeek = _state.value.planWeek.takeIf { it != LocalDate.MIN }
                ?: if (today.dayOfWeek == DayOfWeek.SUNDAY) weekStart.plusWeeks(1) else weekStart
            _state.update {
                it.copy(
                    today = today, settings = s, weekStart = weekStart,
                    who5 = saved?.who5?.split(',')?.mapNotNull(String::toIntOrNull)?.takeIf { a -> a.size == 5 } ?: it.who5,
                    missed = saved?.missedAnything ?: it.missed,
                    oneChange = saved?.oneChange ?: it.oneChange,
                    saved = saved != null,
                    lastWho5 = previous?.who5?.split(',')?.mapNotNull(String::toIntOrNull)?.let(Who5::score),
                )
            }
            loadPlan(planWeek)
            if (!c.importer.hasPermission()) return@launch
            runCatching { c.importer.importNew() }
            val history = c.usage.historySince()
            val from = c.clock.startOf(weekStart.minusWeeks(1)).minus(Duration.ofHours(3))
            val sessions = withContext(Dispatchers.Default) { Sessionizer.sessions(c.usage.events(from, now), now) }
            val (boxFrom, boxTo) = c.clock.millisOf(weekStart.minusWeeks(1), weekStart.plusWeeks(1))
            val boxes = c.gateDao.sessionsBetween(boxFrom, boxTo).map { it.toGateBox() }
            val (goodFrom, goodTo) = c.clock.millisOf(weekStart, weekStart.plusWeeks(1))
            val good = c.goodThings.between(goodFrom, goodTo).map { it.toCore() }
            val excluded = excluded()
            val summary = withContext(Dispatchers.Default) {
                WeekReview.summarize(
                    weekStart, now, c.clock, sessions.filter { it.pkg !in excluded }, gated, boxes, good,
                    s.sleepTimes(today).bedtime, history,
                )
            }
            val lastWeek = Weeks.daysOf(weekStart.minusWeeks(1))
            val heat = UsageAnalytics.heatmap(sessions.filter { it.pkg in gated }, c.clock, lastWeek)
            val suggested = WeekPlan.suggestedHours(heat.averageByHour(), heat.hours, s.sleepTimes(today).bedtime)
            _state.update { it.copy(summary = summary, suggestedHours = suggested) }
        }
    }

    private suspend fun loadPlan(week: LocalDate) {
        val items = c.plans.week(week.toString()).map { it.toCore() }
        val kit = c.plans.kit(week.toString())?.checked?.split(',')?.mapNotNull(String::toIntOrNull)?.toSet().orEmpty()
        _state.update { it.copy(planWeek = week, planItems = items, kitChecked = kit) }
        PlanReminders.schedule(app, items, c.clock)
    }

    fun setWho5(item: Int, value: Int) = _state.update { it.copy(who5 = it.who5.toMutableList().also { a -> a[item] = value }, saved = false) }
    fun setMissed(text: String) = _state.update { it.copy(missed = text, saved = false) }
    fun setChange(text: String) = _state.update { it.copy(oneChange = text, saved = false) }

    fun save() {
        val st = _state.value
        viewModelScope.launch {
            val answers = st.who5.takeIf { a -> a.all { it != null } }?.joinToString(",").orEmpty()
            c.reviews.save(WeeklyReviewEntity(st.weekStart.toString(), answers, st.missed.trim(), st.oneChange.trim(), System.currentTimeMillis()))
            _state.update { it.copy(saved = true) }
        }
    }

    fun choosePlanWeek(week: LocalDate) {
        viewModelScope.launch { loadPlan(week) }
    }

    fun addPlanItem(day: DayOfWeek, time: LocalTime, category: Category, minutes: Int, title: String) {
        viewModelScope.launch {
            val week = _state.value.planWeek
            c.plans.upsert(PlanItem(weekStart = week, day = day, time = time, category = category, minutes = minutes, title = title).toEntity())
            loadPlan(week)
        }
    }

    fun deletePlanItem(item: PlanItem) {
        viewModelScope.launch {
            c.plans.delete(item.id)
            PlanReminders.cancel(app, item.id)
            loadPlan(_state.value.planWeek)
        }
    }

    fun toggleKit(index: Int) {
        viewModelScope.launch {
            val st = _state.value
            val next = if (index in st.kitChecked) st.kitChecked - index else st.kitChecked + index
            c.plans.saveKit(SetupKitEntity(st.planWeek.toString(), next.sorted().joinToString(",")))
            _state.update { it.copy(kitChecked = next) }
        }
    }

    private fun excluded(): Set<String> {
        val home = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME)
        val launchers = app.packageManager.queryIntentActivities(home, 0).map { it.activityInfo.packageName }
        return launchers.toSet() + setOf(app.packageName, "android", "com.android.systemui")
    }
}
