# Architecture: v1.0

Status: **draft**, 2026-10-18. It implements [FEATURES-V1.md](FEATURES-V1.md) v1.0 on the stack
decided in D-017 ([STACK-OPTIONS.md](STACK-OPTIONS.md)). There's no code yet, so there are no
`file:line` references. They'll be added as modules land.

## Modules

```
:app   Compose UI (launcher, review, settings, onboarding) + WorkManager workers
  │      depends on ↓
:gate  GateAccessibilityService · ScreenClassifier · Overlay · TimeboxTimer · HealthMonitor
  │      depends on ↓
:data  Room DB · DataStore · UsageStatsImporter · repositories
  │      depends on ↓
:core  PURE KOTLIN, no Android imports: models · GatePolicy · SleepPlanner · DelayCalculator
       · Metrics · Precommitment · LogicalDay      (all logic takes a Clock, so it's fully unit-testable)

(v1.1) :feed  SourceFetcher interface · RSS · HN · Apify  → plugs into :data, no server (C1)
```

`:core` holds every rule that decides anything. Android modules only *collect* events and *act*
on decisions. That split is what makes a later move to KMP (A4) a build change, not a rewrite.

## Gate flow (the heart of v1.0)

```
┌─ process: target app (Facebook / Instagram / Threads / YouTube / X / ML) ────────────┐
│ user taps the icon → window appears                                                  │
└──────────────────────────────┬───────────────────────────────────────────────────────┘
                               │ AccessibilityEvent WINDOW_STATE_CHANGED / CONTENT_CHANGED
═══════════════════════════════╪══════════ process boundary (system → our app) ══════════
┌─ service: GateAccessibilityService ×1 (:gate) ───────────────────────────────────────┐
│ ▼ config: packageNames = target apps only (privacy + battery)                        │
│ ScreenClassifier(pkg, root node)  → APP_OPEN | REELS | SHORTS | YT_HOME | OTHER      │
│   · matches fixed control labels (FB strips view IDs); **never stores screen text** (N1)│
│ ▼                                                                                    │
│ ╔═ STATE 1: ActiveSession (in memory + mirrored to Room) ═══════════════════════╗    │
│ ║ pkg, intent, timebox end, started-at. Absorbs app switches: leaving and        ║    │
│ ║ coming back within 2 min = same session, so no prompt again (anti-annoyance)  ║    │
│ ╚════════════════════════════════════════════════════════════════════════════════╝    │
│ ▼                                                                                    │
│ ╔═ YOUR RULES: GatePolicy.decide(event, now, EffectiveSettings, TodayCounters) (:core, pure) ═╗
│ ║  ALLOW     ← session active and inside its timebox                                 ║
│ ║  PROMPT    ← new open: delay = DelayCalculator(opens today, profile)              ║
│ ║  BLOCK     ← REELS/SHORTS screen, or wind-down/morning lock for this profile       ║
│ ║  Profiles (D-016): STRICT = FB/IG/Threads · LIGHT = X/YouTube · GAME = ML ·       ║
│ ║                    MESSAGING = Messenger (short pause, no budget)                  ║
│ ╚════════════════════════════════════════════════════════════════════════════════════╝
│ ├─────────────────────┬─────────────────────────┐                                    │
│ ▼ ALLOW               ▼ PROMPT                  ▼ BLOCK                              │
│ nothing               overlay: countdown,       GLOBAL_ACTION_BACK/HOME + overlay    │
│                       "What for?", if-then      message (why + next allowed time)    │
│                       plan, Instead list        and the Instead list                 │
│                       ├─ choose intent + timebox → start session, TimeboxTimer        │
│                       └─ "never mind" → HOME (logged as an abandoned open = a win)    │
│ TimeboxTimer fires → stop overlay → 1-tap 👍/😐/👎 → HOME                              │
└──────────────────────────────┬───────────────────────────────────────────────────────┘
                               ▼ every decision, choice, session and rating
╔═ STATE 2: Room DB (:data) — the source of truth for every metric ═══════════════════╗
║ gate_event · session · app_event · daily_aggregate · weekly_review · plan · setting  ║
╚══════════════════════════════════════╤═══════════════════════════════════════════════╝
                                       │ hourly / nightly
┌─ WorkManager (:app) ─────────────────▼───────────────────────────────────────────────┐
│ UsageStatsImporter: system usage events → app_event (logger, F09)                    │
│ HealthMonitor: target app in front per usage stats BUT no gate_event → gate is dead  │
│                → notification + deep link to re-enable (F16, N6)                     │
│ DailyAggregator: unintended minutes, worth-it share, sleep-window minutes (G1, G4, G7)│
└──────────────────────────────────────────────────────────────────────────────────────┘
```

## Data model (Room)

| Table | Key fields | Feeds |
|---|---|---|
| `app_event` | ts, pkg, type (FOREGROUND/BACKGROUND/SCREEN_ON/UNLOCK), source (usage/gate) | heatmap, health check |
| `gate_event` | ts, pkg, screen, decision, delay_ms, outcome (proceeded/abandoned/blocked/lost), outcome_at, outcome_stage (postcard/countdown/purpose/bored/box/locked) | G1, F05, tuning the delay |
| `session` | id, pkg, intent, timebox_min, start, end, ended_by (timebox/user/restored), rating, gate_event_id | G1, G3, G4 |
| `daily_aggregate` | logical_day, pkg, minutes, opens, unintended_min, after_bedtime_min | review, G7 |
| `weekly_review` | week, who5[5], missed_anything, one_change, notes | F11, F12, A4 |
| `plan` | type (IF_THEN/INSTEAD), trigger, text, category (move/people/read/build/rest) | F14, F15 |
| `setting` | key, value, **effective_from (logical day)** | precommitment (E4, S4), feature switches (D-021) |
| `good_thing` | ts, category, minutes, note, source (home/gate/plan), gate_event_id | A5, G5 |
| `gate_status` | ts, state (connected/destroyed/paused/resumed/resume_failed/health_alert), detail | telling "no prompts" from "gate off" |
| `build_stamp` | ts, version, git sha, installed_at, gate config (profiles, delays, pause list) | which build and gate config were live |
| `plan_item` · `setup_kit` | week, day, time, category, minutes, title, status · ticked items | F27, F60 |

### Research data (v4, 2026-10-19)
The app is also a one-person study: the data has to say which features work and how to tune them.
v4 closed the gaps found in an audit:
- **Export (A6):** Settings → Your data → *Export all data* writes the whole database to
  `Downloads/mindful-hub-<date>.db` (`DataExport`, `VACUUM INTO`). It survives an uninstall.
- **Which config was live:** the gate's profiles and delays are code, not `setting` rows, so each
  start writes a `build_stamp` row when the git sha, install time or `GateSettings` changed.
- **Gate on/off:** `gate_status` records connects, destroys, bank pauses and health alerts.
  `onDestroy` doesn't run when the process is killed; a `CONNECTED` without a `DESTROYED` before it
  marks that gap.
- **Joins:** `session.gateEventId` and `good_thing.gateEventId` point to the prompt or lock they
  came from, instead of matching on package and time.
- **When and where you decided:** `outcomeAt` and `outcomeStage` on `gate_event`. Prompts still
  open when the gate (re)connects are closed as `LOST`.
- **Corrected values:** a bed scroll from the lock sets the lock's outcome to `PROCEEDED` (it stayed
  `BLOCKED`); sessions found overdue after a restart end as `RESTORED`, not `TIMEBOX`, since their
  real end is unknown there (`app_event` has it). `SCREEN_OFF` was never written and is gone.
| `postcard` | night (logical day), text | F31 + F53 |
| `night_log` | night, status, planned bed/wake, down_at, up_at, last/first pkg, interruptions (count, seconds, pkgs), glances, other stretches, shutdowns, screen_on_signal, settled, rules_version | F64 |
| `night_card_shown` | ts, night, settled | F64 exposure |
| `sleep_checkin` | night, asked_at, answered_at, skipped, rested 1–5, latency bucket, planned_late | F65 |

## Design rules that aren't obvious

### "Today" is a logical day, not a calendar day
You're often up at 01:00–03:00. Facebook use at 01:30 belongs to *the day before*: the same
budget, the same review, and it counts as "after bedtime" (G7). `:core` defines
`LogicalDay(boundary = 04:00)`. Every budget, aggregate and precommitment uses it. This is the
easiest bug to get wrong, so it gets the most tests: crossing midnight, a wind-down window that
spans two calendar days, and DST (Vietnam has none, but the code shouldn't assume that).

### Precommitment lives in the data model, not the UI
A settings change is written with `effective_from = tomorrow's logical day`.
`GatePolicy` reads `EffectiveSettings(today)` only, so there's no code path that loosens a rule
the same day (P2). Tightening a rule applies immediately; only loosening waits.

### The gate must fail loudly, never silently
Samsung can switch off an accessibility service after updates or battery "optimization". The
cross-check works because the two signals are independent: usage stats come from the system and
gate events come from our service. A gap shows up within an hour. **Metric to watch:**
`health_gap_minutes` per day. It should be 0.

### Reading the screen is a privacy liability, so minimize it
Facebook strips its view IDs (they read `(name removed)`), so detectors match the accessibility
labels of fixed *controls* ("Reels, tab 2 of 6", "Reels tab details"), never post content. Only
gated apps are inspected, at most once per 800 ms. Nothing from the screen is stored except the
classification label. (The service receives window events from all apps, so it can tell when you
*leave* a gated app; it reads only their package name.)

### One service, per-app detectors
There's only one `GateAccessibilityService`. Each target app gets its own small `ScreenDetector`
(FB, IG, YT, Threads). When an app update breaks detection, only that detector changes. Their
UI view IDs *will* change, so each detector has a fallback to "APP_OPEN only" and logs
`detector_miss` so the review can flag it.

## Spike results (Galaxy S22 Ultra, One UI 7, 2026-10-18)
1. ✅ `TYPE_ACCESSIBILITY_OVERLAY` works without "Display over other apps".
2. ✅ FB Reels **tab**: no view IDs; detected by the selected "Reels, tab N of M" label or the
   "Reels tab details" button. Back does *not* leave a tab, so the block taps FB's "Home, tab 1 of
   N" instead (otherwise it loops). Still to check: Instagram.
   **2026-10-23:** FB follows the phone's language. In Vietnamese the labels read "Reels, Tab 2/6"
   and "Chi tiết về tab Reels", so the English-only match missed every Reels visit after 10-18
   (8 blocks that day, none since). Labels now match both. A reel opened from a feed post is
   caught too (Back leaves it). YouTube Shorts: the `reel_player_page_container` view ID (YouTube
   keeps IDs), escaping via the tab bar's first button (Home); a Shorts *shelf* on Home is not
   blocked. Test with the debug-only `DEBUG_ALLOW` / `DEBUG_DUMP` broadcasts (`DebugBypass.kt`),
   not `uiautomator dump`: it unbinds the gate.
3. ✅ `queryEvents` returned history back to 9 Oct: **~9 days** on day 1.
4. ✅ Prompt latency **136–270 ms** after the window event (N4).
5. ⚠️ After D-021 the prompt came **650–700 ms** after the event (2026-10-19). Three waits had
   joined the critical path: the F29 feeds read (usage import + sessionizing, run on the main
   thread so its 250 ms timeout couldn't cut it, and its 2-minute cache nearly always missed),
   the prompt's `insertEvent` awaited before `overlay.show`, and the night reads one after another.
   Fix: feeds minutes are refreshed off the main thread whenever you leave a STRICT app, and a
   decision uses today's value at once (it only waits, ≤250 ms, when there is none); the prompt
   and lock screens are shown first and logged after, with their handlers awaiting the row; the
   bed-scroll and postcard reads run in parallel, and the feeds value is read when the service
   connects. Decisions are unchanged. Measured after the fix: **137–223 ms**, including the first
   open after a reinstall (`adb logcat -s MindfulGate:I`).

## Gotchas found on the device
- **`uiautomator dump` suspends other accessibility services**, which re-creates the gate. Don't
  use it while testing a running session.
- **Launchers emit a late window event** after the target app's first window. Leaving is only
  confirmed after 600 ms away, or the prompt restarts. The event can also land while the gate is
  still reading the database for the first open of the day; trusting it skipped the prompt
  entirely. Decisions that would let you through now check the window list
  (`flagRetrieveInteractiveWindows`) instead of the last event.
- **The gate can't see an app launch before the app's first window.** One UI's launcher sends no
  `TYPE_VIEW_CLICKED` for icon taps, and the splash window isn't in the window list. A cold start
  runs the app's code about 600 ms before the gate hears of it, which is why banks get a shortcut
  that pauses the gate first (D-020).
- **Time boxes must be persisted**: an in-memory timer is lost on any service re-bind. Open
  sessions are reloaded from Room on connect.
- `adb install` (streamed) dropped the USB link on this phone; `adb push` + `pm install` works.

## Feature switches (D-021)

Every feature added after the gate core has a switch in **Settings**, and its own code, so an unsure
one can be turned off today and deleted later.
- **The switch list** is `Feature` in `:core` `Features.kt`. Each entry has an F-number, a status
  (APPROVED or TRIAL) and `restricts`. Switches are stored like any other setting, in the `setting`
  table with an `effectiveFrom` day (`SettingsStore`, `:data` `GoodSideData.kt`).
- **Precommitment**: `Precommit.loosens` decides. Switching off a restriction (F29, F03), raising the
  budget, a later bedtime or wake time, a shorter wind-down, or allowing the bed scroll waits until
  tomorrow's logical day. Everything else, including every trial switch, applies at once.
- **The gate reads the switches on every decision**: it keeps the setting rows current from a Room
  `Flow` and decodes them for the decision's own logical day, so a waiting change starts at 04:00 by
  itself. With every switch off, `GateRules()` is the default and the gate behaves as before.

**Where each feature lives** (to remove one: delete these, its `Feature` entry and its card hook):

| Feature | `:core` | `:data` table | `:gate` | `:app` |
|---|---|---|---|---|
| F32 comeback | `Comeback` (Review.kt) | — | — | `HelloCard` (TodayScreen.kt) |
| F10 worth-it + left early | `GateBox.leftEarly` | `session` (existing) | `noteLeaving`, `GateStop(askRating)` | `PauseWins.leftEarly` (InsightsView) |
| F26 + F59 instead | `InsteadMenu`, `Moment` (GoodThings.kt), `Schedule`, `DayPart` (DayParts.kt) | `good_thing`, `instead_offer` | `InsteadPanel`, `InsteadHooks`, `doInstead` | "Your week" in Settings |
| A5 good things | `Category`, `GoodThings` | `good_thing` | — | `GoodLogCard` |
| F11 + F12 review | `WeekReview`, `UnintendedTime`, `Who5` (Review.kt) | `weekly_review` | — | `ui/review` |
| F27 + F60 plan + kit | `WeekPlan`, `SetupKit` (Plan.kt) | `plan_item`, `setup_kit` | — | `PlanCard`, `KitCard`, `PlanTodayCard`, `PlanReminders.kt` |
| F29 feeds budget | `FeedBudget`, `GatePolicy.capTo`, `LockReason.BUDGET` | — (usage history) | `feedMinutes`, `showLocked` | `FeedsCard`, leak line in `WeekCard` |
| F03 wind-down | `NightClock`, `LockReason.WIND_DOWN`, `Purpose.BED_SCROLL` (Night.kt) | — | `showLocked` | Settings steppers |
| F31 + F53 postcard | `GateRules.postcardDue` | `postcard` | `PostcardBox`, `savePostcard` | `PostcardCard` |
| D-025 instead learns | `InsteadMenu.rank`, `OfferStats` | `instead_offer` (InsteadData.kt) | `insteadHooks`, `refreshInsteadStats` | — |
| F13 + F44 sleep quest (trial) | `SleepQuest`, `Settings.sleepTimes` | — (settings) | — | `QuestCard` |
| F61 morning receipt (trial) | — | — | — | `receiptFor`, `ReceiptCard` |
| F64 night log | `NightLog`, `NightRules`, `NightFacts` (NightLog.kt); `SCREEN_ON`/`SHUTDOWN`/`STARTUP` in `UsageEventType` | `night_log`, `night_card_shown` (SleepData.kt) | — | `nightCardFor`, `NightLogCard`; `UsageImportWorker` keeps it current |
| F65 morning check-in | `MorningCheckIn`, `SleepLatency` (CheckIn.kt) | `sleep_checkin` (SleepData.kt) | — | `checkInFor`, `CheckInCard`; hides F61 and F64 until answered |
| F72 match nights | `MatchNight`, `MatchNights`, `Settings.matchOn` (Match.kt); `GateRules.matchUntil`, `GatePolicy.MATCH_BOX` | — (setting `matchNights`) | `rulesFor` | `MatchNightsCard` (Settings); ticks F65's tag, a line on F64's card |
| F17 calm links (trial) | `CalmLinks` (Plan.kt) | — | — | `CalmLinksCard` |

**Copy rules (F32)**, for every screen: suggest ("want to…?", "could"), never order. State the task
fact ("42 min after bedtime"), never a verdict about you. Nothing resets: no streaks, counts only go
up. A slip day gets "welcome back" and one next step. Every Instead line says what to *do*, never
"don't" (P27). Rationale: RESEARCH-TIME.md P24, P27, P33.

**Unverified on the phone** (built and unit-tested, not yet tried on the S22): typing in the overlay
(the postcard field), the Clock app's timer from the gate, the toast after leaving early, plan
reminders, the F64 and F65 morning cards (both only show 04:00–14:00; the F64 rules were run over
the real history from 9–20 Oct, see D-026).

### Night log (F64): how a night is found
```
app_event (usage import, hourly)        SCREEN_ON · UNLOCK · FOREGROUND · SCREEN_OFF · SHUTDOWN
   ▼ NightLog.screenUses                 screen-on stretches; before the first SCREEN_ON (old history)
   │                                     an app or unlock opens one. Named by the app used longest
   ▼ use = unlocked OR ≥ 60 s            shorter, locked screen-ons are glances, not uses
   ▼ gaps between uses
   ▼ core = gap covering most of planned bed→wake (ties: most of the window bed−4 h … wake+5 h)
   │        ≥ 3 h, else NO_CLEAR_NIGHT;   no events at all in the window = NO_DATA (never a perfect night)
   ▼ grow across uses ≤ 20 min that start before the planned wake, if ≥ 45 min of rest follows
   ▼ down_at · up_at · interruptions · other stretches ≥ 90 min before the wake (split nights)
╔═ night_log (settled once later events can't change it; rules_version = NightRules.version) ═╗
```
Changing any number in `NightRules` must bump its `version`; stored nights keep the version that
made them, and unsettled or older-version nights are recomputed.

## Usage views (D-018)
`:app` `ui/views/UsageView.kt` holds the registry. Each view gets one `UsageSnapshot`: sessions for
two weeks, the clock, the app filter, the bedtime and today's gate counts. From that it derives its
own model, so a new view touches no other code. Rules that state facts (`UsageInsights`) live in
`:core` and are unit-tested, because a wrong sentence misleads more than a wrong pixel.

## Build order (from FEATURES-V1.md)
1. **Skeleton + logger (F09)**: modules, Room, UsageStatsImporter, hour × day heatmap. This starts the baseline week.
2. Gate core (F16, F01, F02, F05) + spike items 1, 2 and 4.
3. Sleep (F03, F13).
4. Launcher + group time (F17, F18).
5. If-then, Instead, worth-it (F14, F15, F10).
6. Budgets (F04).
7. Weekly review + WHO-5 (F11, F12).
