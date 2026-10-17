# Stack options

Status: **proposed, for you to choose**, 2026-10-18. Target device: Galaxy S22 Ultra, Android 15,
One UI 7. Laptop: Arch Linux.

The weights are **proposed**, derived from the goals. In order:
1. The gate must be fast and impossible to silently lose (N4, N6).
2. You build this alone.
3. $0 (D-015).
4. Room to grow later (v1.1 digest, a possible desktop app).

**Assumption to confirm:** you're comfortable with Java and TypeScript (lc-trainer is TS), and
Kotlin is new to you but close to Java. If that's wrong, S1 can flip (see its notes).

**What v1.0 actually needs:** v1.0 (gate, sleep, logger, launcher) fetches **no content**, so it
needs **no server**. S3 decides how the v1.1 digest fetches, and whether an optional server is worth it.

## S1. App framework

- **A1 Kotlin + Jetpack Compose**: Google's native toolkit. The gate (accessibility service, usage stats, overlay) is plain Kotlin in the same codebase.
- **A2 Flutter (Dart)**: cross-platform UI. The gate still has to be written in Kotlin and bridged through platform channels, so you'd work in two languages.
- **A3 React Native (TypeScript)**: reuses your TS. The gate is a Kotlin native module behind a JS bridge.
- **A4 Kotlin Multiplatform + Compose Multiplatform**: Kotlin shared across Android, desktop and iOS. The Android side is the same as A1, plus multiplatform setup.

| Criterion (why it matters) | Weight | A1 Kotlin + Compose | A2 Flutter | A3 React Native | A4 KMP + Compose MP |
|---|---|---|---|---|---|
| **Gate: $0 to run** (hard requirement, D-015) | must pass | ✅ free | ✅ | ✅ | ✅ |
| Native access for the gate (accessibility, usage stats, overlay and foreground service are the core of v1) | ×3 | 5/5 first-class | 2/5 gate in Kotlin anyway, plus channels | 2/5 gate as a native module | 5/5 Android target = A1 |
| Solo dev speed (your skills, tooling, docs) | ×2 | 4/5 Kotlin ≈ Java, mature tooling | 3/5 Dart is new | 4/5 your TS skills | 3/5 multiplatform setup overhead |
| Performance of the gate overlay (must appear within 300 ms (N4)) | ×2 | 5/5 native overlay | 4/5 overlay is native; the engine adds startup cost | 3/5 JS bridge, cold start | 5/5 native |
| Reference code (open-source Reels/Shorts blockers to learn from) | ×1 | 5/5 Shorts-Blocker is Kotlin/Compose | 2/5 few examples | 2/5 few examples | 4/5 same Kotlin examples |
| Reuse beyond Android (desktop or iOS later; you're Android-only today) | ×1 | 2/5 Android only | 4/5 iOS and desktop UI | 4/5 iOS UI | 5/5 desktop and iOS share the logic |
| **Weighted total** | | **40/45 (89%)** | **26/45 (58%)** | **26/45 (58%)** | **40/45 (89%)** |

**What the table shows.** It's a **tie between A1 and A4 (40/45)**. Flutter and React Native lose because the gate must be native Kotlin anyway, so they add a second language and a bridge to the riskiest part.
**Recommendation: A1 now, laid out so it's ready for A4.** Business rules (budgets, sleep plan, scoring) go in a **pure-Kotlin `:core` module** with no Android imports. Moving to KMP later is then a build change, not a rewrite.
**Flips:** if reuse beyond Android counts ×2, A4 wins (45 vs 42). If you'd strongly rather write TypeScript, A3 rises, but the gate still needs Kotlin.

## S2. How the gate sees app opens

- **B1 AccessibilityService**: the system pushes window-change events and the screen's view tree, so the gate sees the app *and* the screen (Reels, the YouTube home feed). The open-source Shorts-Blocker and AwayDoomscrollin' blockers use this.
- **B2 UsageStatsManager polling + overlay**: a foreground service asks "which app is in front?" every ~0.5–1 s.
- **B3 Local VPN DNS blocking**: blocks network domains. It can't show the "what for?" prompt, and Facebook uses the same domains for everything.
- **B4 Samsung's built-in tools** (Digital Wellbeing timers, Modes and Routines): no custom intention flow.

| Criterion (why it matters) | Weight | B1 Accessibility | B2 Usage polling | B3 VPN DNS | B4 Samsung tools |
|---|---|---|---|---|---|
| **Gate: Supports the intention prompt and custom rules** (E1–E5) | must pass | ✅ | ✅ | ❌ no prompt | ❌ no custom flow |
| Detection latency (N4: a slow gate gets bypassed by reflex) | ×3 | 5/5 event-driven, instant | 3/5 0.5–1 s lag | 4/5  | 5/5  |
| Sees screens inside apps (needed for Reels, Shorts and YouTube home (E8, E12)) | ×3 | 5/5 full view tree | 1/5 package only | 1/5  | 1/5  |
| Reliability on Samsung (N6: a gate that silently dies is worthless) | ×2 | 4/5 system-bound; Samsung sometimes switches it off after updates, so a health check is needed | 3/5 foreground service; Samsung kills them | 3/5  | 5/5  |
| Battery (you'll keep it on 24/7) | ×1 | 4/5 filter to target packages | 2/5 constant polling | 3/5  | 5/5  |
| Setup friction (one-time) | ×1 | 3/5 "Allow restricted settings" for sideloaded installs; not needed when installed via adb | 4/5 usage access + overlay | 4/5  | 5/5  |
| **Weighted total** | | **45/50 (90%)** | **24/50 (48%)** | **out (fails gate)** | **out (fails gate)** |

B3 and B4 fail the gate. B4 stays as the Week-0 stopgap (Z4).
**Recommendation: B1 for the gate. UsageStatsManager still powers the logger (A1) and acts as a cross-check:** if usage shows FB in front while the accessibility service is silent, the gate is dead, so warn and re-prompt (F16).

## S3. Content fetching and the optional server (for v1.1 onward)

- **C1 On-device**: WorkManager on the phone calls Apify and the RSS feeds. No server.
- **C2 GitHub Actions cron**: a scheduled workflow in a *private* repo fetches and writes JSON, and the phone pulls it.
- **C3 Cloudflare Workers cron + KV**: free plan, 100k requests a day, 5 cron triggers, **10 ms CPU per cron run** (fetch waiting doesn't count, parsing does).
- **C4 Oracle Cloud Always Free VM**: a real Linux box. You maintain it, and idle free instances can be reclaimed.
- **C5 Laptop cron (Arch)**: only runs while the laptop is on.
- **C6 Supabase free**: Postgres + edge functions + pg_cron. Free projects pause after a week without activity.

| Criterion (why it matters) | Weight | C1 On-device | C2 GitHub Actions | C3 CF Workers | C4 Oracle VM | C5 Laptop | C6 Supabase |
|---|---|---|---|---|---|---|---|
| **Gate: $0 to run** (hard requirement, D-015) | must pass | ✅ | ✅ free minutes | ✅ | ✅ card needed to sign up | ✅ | ✅ |
| Fetch reliability (the digest must arrive on time even if the phone is idle or Samsung is aggressive) | ×3 | 3/5 WorkManager can defer; the app stays active because of the gate service | 4/5 cron can lag minutes to hours; fine for daily | 5/5 reliable cron | 4/5 up, unless reclaimed | 1/5 off or asleep | 3/5 pauses when inactive |
| Privacy (usage data and tokens stay with you (N1)) | ×2 | 5/5 all on the phone | 3/5 data in a private repo | 3/5 Cloudflare-hosted | 4/5 your VM | 5/5 local | 3/5 hosted |
| Ops burden (you're solo) | ×2 | 5/5 nothing to run | 4/5 YAML + script | 3/5 10 ms CPU cap means splitting the parsing | 1/5 patching and security | 4/5 systemd timer | 3/5 SQL + functions |
| Headroom (ranking, LLM summaries, a future desktop client) | ×1 | 2/5 phone CPU and battery | 3/5 any language | 3/5 KV/D1 free | 5/5 anything | 4/5 local CPU | 4/5 Postgres |
| **Weighted total** | | **31/40 (78%)** | **29/40 (72%)** | **30/40 (75%)** | **27/40 (68%)** | **25/40 (62%)** | **25/40 (62%)** |

**What the table shows.** It's close at the top: **C1 31/40 (78%)**, C3 30/40 (75%), C2 29/40 (72%).
**Recommendation: C1, no server, for v1.1.** Fetch code goes behind a `SourceFetcher` interface so it can move to C3 or C2 unchanged in shape.
**Trigger to add the optional server:** digests arriving late on more than 2 days in a week (measured), or the arrival of LLM summaries or a desktop client.
**Flip:** if reliability counts ×4, C3 edges ahead (35 vs 34).

## S4–S9. Smaller choices

Same scoring, compact. Weights: Fit ×2, Simplicity ×2, Maturity ×1 (max 25).

### S4. Local storage

| Option | Fit ×2 | Simplicity ×2 | Maturity ×1 | **Total** |
|---|---|---|---|---|
| Room (SQLite) | 5/5 relational event logs, Flow queries | 4/5 annotations, migrations | 5/5 Google, standard | **23/25** |
| SQLDelight | 5/5 same, and multiplatform-ready | 3/5 SQL files, more setup | 4/5 mature | **20/25** |
| DataStore only | 1/5 key-value; wrong for logs | 5/5 trivial | 5/5 standard | **17/25** |

**Room** for logs and sessions, **DataStore** for settings. If S1 flips to A4, SQLDelight replaces Room.

### S5. Timers and background work

| Option | Fit ×2 | Simplicity ×2 | Maturity ×1 | **Total** |
|---|---|---|---|---|
| Timers inside the accessibility service + WorkManager | 5/5 time-box end and bedtime checks run in the always-alive service; daily aggregation and fetches in WorkManager | 4/5 two mechanisms | 5/5 standard | **23/25** |
| AlarmManager exact alarms | 3/5 precise, but needs a special permission and is heavier | 3/5  | 5/5  | **17/25** |
| WorkManager only | 2/5 15-min minimum period; too coarse for time boxes | 5/5  | 5/5  | **19/25** |

Bedtime rules are checked **when an app opens**, so no alarm is needed. Only the stop screen at the end of a time box needs a timer.

### S6. Charts (heatmap, review)

| Option | Fit ×2 | Simplicity ×2 | Maturity ×1 | **Total** |
|---|---|---|---|---|
| Vico | 5/5 Compose-native charts | 4/5 library API | 4/5 active | **22/25** |
| Custom Compose Canvas | 4/5 a heatmap is just rectangles | 3/5 you write the axes | 5/5 no dependency | **19/25** |
| MPAndroidChart | 3/5 View-based, not Compose | 3/5 interop | 3/5 stale | **15/25** |

**Vico**, with Canvas for the hour × day heatmap.

### S7. Dependency injection

| Option | Fit ×2 | Simplicity ×2 | Maturity ×1 | **Total** |
|---|---|---|---|---|
| Manual (AppContainer) | 4/5 a few services | 5/5 no magic | 5/5 plain Kotlin | **23/25** |
| Hilt | 4/5 standard on Android | 2/5 annotations, kapt/ksp, slow builds | 5/5 Google | **17/25** |
| Koin | 4/5 multiplatform-ready | 4/5 DSL | 4/5 mature | **20/25** |

**Manual** for now, and **Koin** if the app grows or moves to KMP.

### S8. Install and updates

| Option | Fit ×2 | Simplicity ×2 | Maturity ×1 | **Total** |
|---|---|---|---|---|
| adb install from the laptop | 5/5 also avoids the restricted-settings prompt (not flagged as sideloaded) | 5/5 one command | 5/5  | **25/25** |
| GitHub Releases + Obtainium | 4/5 updates without the laptop, but flagged as sideloaded, so allow restricted settings once | 4/5  | 4/5  | **20/25** |
| Play Console internal testing | 0/5 ❌ $25 one-time fee breaks $0 | 2/5  | 5/5  | **out (fails $0)** |

**adb install** during development, plus Obtainium once it's stable. Play is out (not free).

### S9. Backup of your data

| Option | Fit ×2 | Simplicity ×2 | Maturity ×1 | **Total** |
|---|---|---|---|---|
| Android Auto Backup (Google Drive, 25 MB per app) | 4/5 automatic, survives a phone reset | 5/5 a manifest flag | 5/5 system | **23/25** |
| Manual CSV/JSON export (A6) | 3/5 you have to remember | 5/5  | 5/5  | **21/25** |
| Sync to a server | 3/5 only if S3 adds one | 2/5  | 4/5  | **14/25** |

**Auto Backup + export (A6).** Data stays in your own Google account.

## Proposed stack (if you accept the recommendations)

| Layer | Choice |
|---|---|
| Language and UI | Kotlin 2.x, Jetpack Compose, Material 3 |
| Modules | `:core` (pure Kotlin rules: budgets, sleep plan, scoring) · `:gate` (accessibility service, overlay) · `:data` (Room, DataStore, UsageStats) · `:app` (UI) |
| Gate | AccessibilityService + overlay activity. UsageStatsManager as logger and cross-check |
| Storage | Room + DataStore, Auto Backup |
| Background | Timers inside the service + WorkManager |
| Charts | Vico + Canvas heatmap |
| Tests | JUnit for `:core` (most logic), Robolectric for `:data`, a few Compose UI tests |
| Build and CI | Gradle (Kotlin DSL) on Arch. GitHub Actions free minutes for CI |
| Install | `adb install` (no restricted-settings prompt) |
| Server | **None in v1.0.** v1.1: on-device fetch (C1). Optional Cloudflare Workers later if the trigger fires |

Reference code: the Shorts-Blocker (GitHub) and AwayDoomscrollin' (F-Droid) blockers show how Reels and Shorts screens are detected. **Check their licences before copying anything.**
