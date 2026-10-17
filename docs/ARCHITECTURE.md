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
│   · reads view IDs and class names only; **never stores screen text** (N1)           │
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
| `gate_event` | ts, pkg, screen, decision, delay_ms, outcome (proceeded/abandoned/blocked) | G1, F05 |
| `session` | id, pkg, intent, timebox_min, start, end, ended_by (timebox/user/screen_off), rating | G1, G3, G4 |
| `daily_aggregate` | logical_day, pkg, minutes, opens, unintended_min, after_bedtime_min | review, G7 |
| `weekly_review` | week, who5[5], missed_anything, one_change, notes | F11, F12, A4 |
| `plan` | type (IF_THEN/INSTEAD), trigger, text, category (move/people/read/build/rest) | F14, F15 |
| `activity_log` | ts, category, minutes | G5 |
| `setting` | key, value, **effective_from (logical day)** | precommitment (E4, S4) |

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
The service config restricts `packageNames` to the target apps. The classifier reads view IDs
and class names (e.g. a Reels viewer's view ID), never text. Nothing from the screen is stored
except the classification label.

### One service, per-app detectors
There's only one `GateAccessibilityService`. Each target app gets its own small `ScreenDetector`
(FB, IG, YT, Threads). When an app update breaks detection, only that detector changes. Their
UI view IDs *will* change, so each detector has a fallback to "APP_OPEN only" and logs
`detector_miss` so the review can flag it.

## Things the first spike must verify (unknowns)
1. Overlay window type: does `TYPE_ACCESSIBILITY_OVERLAY` from the service work on One UI 7 without "Display over other apps"? (That permission is restricted for sideloaded apps on Android 15.)
2. Reels detection in the FB app: which view IDs identify it? (Learn from Shorts-Blocker and AwayDoomscrollin', after checking their licences.)
3. How far back `UsageStatsManager.queryEvents` goes on this phone. That decides how much baseline we get on day 1.
4. Gate latency from tap to overlay. Target ≤300 ms (N4).

## Build order (from FEATURES-V1.md)
1. **Skeleton + logger (F09)**: modules, Room, UsageStatsImporter, hour × day heatmap. This starts the baseline week.
2. Gate core (F16, F01, F02, F05) + spike items 1, 2 and 4.
3. Sleep (F03, F13).
4. Launcher + group time (F17, F18).
5. If-then, Instead, worth-it (F14, F15, F10).
6. Budgets (F04).
7. Weekly review + WHO-5 (F11, F12).
