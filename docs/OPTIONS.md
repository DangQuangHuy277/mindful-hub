# Feature options: everything we could build

Status: **v1.0 approvals recorded**, 2026-10-19 (D-021). Only the "Approved" and "Trial" rows below are
being built; everything else is still an option. Approved work moves to GitHub issues once `gh` is set
up (D-021). This collects every option
from [FEATURES-V1.md](FEATURES-V1.md) (F01–F24), [RESEARCH-TIME.md](RESEARCH-TIME.md) (F25–F37,
F55–F63) and [gamification/RESEARCH-GAMIFICATION.md](gamification/RESEARCH-GAMIFICATION.md) (F38–F54) and
[sleep/RESEARCH.md](sleep/RESEARCH.md) (F64–F71, plus A7 and H1) and
[energy/RESEARCH.md](energy/RESEARCH.md) (F73–F86, plus H2), and adds what's already
built and what depends on what.

**How to read the scores.** Each doc scored with slightly different criteria (max 45, 55 or 65), so
only the percentages are roughly comparable. "Ours" means the idea came from our own brainstorm on
top of the research, not from an existing app.

## Already built

| ID | Feature | Where | Notes |
|---|---|---|---|
| F09 | Usage logger + views (insights, heatmap) | `:core` Usage, Insights, Heatmap; app `UsageScreen` | D-018, D-019 |
| F01 | Open-time gate: pause + "what for?" | `:core` `GatePolicy`; `:gate` `GateAccessibilityService`, `GateOverlay` | Verified on the phone |
| F02 | Time box + stop screen | `:gate` `GateScreens` | Session restore fixed |
| F05 | Escalating delay + rotating wording | `GatePolicy.delayFor`, `PromptCopy` | Only the *length* escalates. F30/F62 would change the *form* |
| F06 | Short-video (Reels) blocking | `:gate` `ScreenDetectors` | Was "Later" in FEATURES-V1, built early |
| F16 | Gate health check | `:gate` | The 24 h cooldown to switch the gate off (N5) isn't in the code yet |

## v1.0: approved and trial (2026-10-19, D-021)

**Approved** = you said yes. **Trial** = built so you can try it; you haven't decided yet. Every row has
its own switch in Settings, so anything can be turned off without a rebuild, and each one lives in its
own code (see ARCHITECTURE.md, "Feature switches") so it can be deleted.

| ID | Feature | Status | Your notes (2026-10-19) |
|---|---|---|---|
| F32 | Comeback, not streaks: copy rules | Approved | — |
| F10 | 1-tap worth-it rating | Approved | Less annoying, and reward leaving on your own: no prompt when you quit early, a small "left early" win instead |
| F26 + F59 | One-tap replacement, sized to the moment | Approved | — |
| A5 | One-tap activity logging | Approved | — |
| F11 + F12 | Weekly review + WHO-5 | Approved | If it fits this phase |
| F27 + F60 | Weekly activity plan + real-world setup kit | Approved | No paper books yet. Answer in D-021: daytime reading on the phone is fine (Substack, e-book); in bed, use something that isn't a lit screen |
| F29 | Group budget + leak watch | Approved | — |
| F03 | Bedtime wind-down lock | Approved | Sometimes you scroll to fall asleep; to research later. Has its own switch, plus an optional "one 10-min scroll" |
| F31 + F53 | Tomorrow-you postcard at night | Approved | — |
| F13 + F44 + F61 | Sleep-shift quest + morning receipt | Trial | Not reviewed yet |
| F17 + F18 | Calm launcher + group time | Trial | Not reviewed yet. Needs your group links |
| F64 | Phone-down night log | Approved (2026-10-20, D-026) | Football at 02:00–04:00 some nights: split nights keep every phone-down stretch ≥ 90 min |
| F65 | Morning check-in (rested 1–5, time to fall asleep) | Approved (2026-10-20, D-026) | Optional "⚽ planned late night" tag, so match nights aren't read as slips |
| F72 (ours) | Match nights: a planned exception to the wind-down lock | Approved (2026-10-20, D-027) | Your ask, for 02:00–04:00 matches. Feeds and X open too, for the half-time talk |

## Sleep (2026-10-20): what's next, from [sleep/RESEARCH.md](sleep/RESEARCH.md)

Scored with the research doc's proposed weights (evidence ×3, fits the problem ×3, low risk ×2,
cheap ×1; max 45). Not approved yet.

| ID | Feature | Score | Depends on | Notes |
|---|---|---|---|---|
| F68 | Bedtime if-then plan (MCII), shown in wind-down | 84% | F03 | Two RCTs on bedtime procrastination (P47) |
| F66 | Weekly sleep-timing card: regularity (SRI), wake-time spread, mid-sleep, social jetlag | 82% | F64, F11 | Is S3 in REQUIREMENTS. Needs your work days for social jetlag |
| F67 (ours) | Bed-scroll experiment: alternate weeks with the bed scroll on and off | 80% | F64, F65, F03, F23 | Answers your F03 note. After ~2 weeks of baseline |
| A7 | Google Sleep API cross-check | 64% | F64 | Free signal from Play Services, not publicly validated |
| H1 | Health Connect sleep reader (any tracker or watch) | 60% | F64 | Only to check F64 against a tracker; you found the apps didn't help before |

## Energy, food and focus (2026-10-23): the measurement layer, from [energy/RESEARCH.md](energy/RESEARCH.md)

Same weights as the sleep table (evidence ×3, fits your question ×3, low burden and risk ×2, cheap
×1; max 45), proposed. Not approved yet. The research doc recommends F73 + F74 + F76 + F77 as a
3-week baseline that shows nothing back, F75 as a 2-week trial, then F78, F79 and F81, then
experiments one at a time.

| ID | Feature | Score | Depends on | Notes |
|---|---|---|---|---|
| F79 | Caffeine cut-off line: last coffee by bedtime − 8.8 h | 93% | F74, F03 | **Not needed (D-029)**: you rarely drink coffee |
| F73 | Energy check: KSS alertness + mood, at lunch and after work (never in work hours, D-029) | 82% | F64 | The outcome measure everything else needs. One new notification per workday (lunch); the after-work one is F76 |
| F74 | Food log, after the fact (D-029): lunch size, snacks by part of day (sweet or not); a rare drink or coffee | 82% | F76 | Asked in the after-work card, not tapped at work. No calories |
| F76 | After-work card: alertness + mood now, focus 1–5, main thing done, afternoon break, snacks; "could you switch off?" at night | 82% | F03 | Merges F73's last anchor and F74. About 15 s, once a day |
| F81 | Lunch-walk nudge: a 10–15 min walk outside after lunch, offered on the lunch card (D-029) | 80% | F73, F26 | Moved from mid-afternoon, which is work time. Lunchtime park walks: Sianoja 2018 |
| F75 | Reaction-time check (PVT-B, 3 min), optional. **Built as a trial (2026-10-23, D-030)** | 76% | — | You can't feel sleep debt (P50). Without a watch it's the only objective measure (D-028). Morning only, after F65, as a mini-game with fixed rules (2026-10-23). Trial for 2 weeks |
| F80 (ours) | Daytime experiments on F23: lunch walk, light lunch, fewer sweet snacks | 76% | F23, F73 | One at a time, after F67 |
| F78 | Weekly energy map: alertness by hours since waking, with meal and snack markers | 73% | F73, F74, F11 | After ≥ 2 weeks. No score |
| F77 | Steps per hour from the phone's step counter | 71% | — | New permission (physical activity) |
| F82 | Work-PC focus log (ActivityWatch), joined by time | 60% | — | **Parked (D-028)**: no work data for now |
| H2 | Wearable reader: sleep timing, resting HR, night HRV, steps (Health Connect or Gadgetbridge) | 73% | — | **Parked (D-028)**: no watch for now |
| F83 | Work-tracker import: block times + a category, never task titles | 67% | — | **Parked (D-028)**: no work data for now |
| F84 | Calendar meeting load (times only) | 64% | — | **Parked (D-028)**: no work data for now |
| F85 | Morning camera HRV (1 min, fingertip) | 64% | — | The only night-recovery signal left without a watch (D-028). After the baseline, if Q2/Q3 need it |
| F86 | Typing and click speed on the PC (timings only) | 60% | F82 | **Parked (D-028)**: no work data for now |

Out: calorie or macro counting (27%), GPS mobility (36%), photo food diary (40%), water logging.

## Proposed for v1.1 (not approved): the good-week layer and stronger friction

| ID | Feature | Score | Depends on | Notes |
|---|---|---|---|---|
| F39 + F49 (ours) | Weekly quests with a comeback bloom | 82% / 83% | F27 | The game board |
| F48 (ours) | Roots, not streaks (habit-strength curve) | 85% | A5, habits stored with a context | Top-scored gamification idea |
| F38 | Good-week garden widget | 77% | A5, F39 | Needs your call on D-019 (cute display vs. "no game") |
| F63 (ours) | Freed-minutes ledger | 76% | A5, F11 | — |
| F28 + F57 (ours) | Bounded escape hatch + note from calm-you | 69% / 67% | F29 or F03 | Makes strict rules survivable |
| F62 (ours) | Risk-weighted friction | 65% | F09 heatmap | Replaces "grows with every open" in F05 |
| F30 | Rotating gate forms (+ "why it changed" note) | 62% | F01 | Fix for the week-3 plateau |
| F35 | Temptation bundle (streams while walking) | 71% | F01 | — |
| F58 (ours) | Reset sprint with a taper | 71% | F23 | Finds out which rules you actually need |
| F23 | Single-person (n-of-1) A/B weeks | 56% | F11 | Needed to *measure* every other item, so it may deserve a higher tier |
| F33 / F42 | Partner check-in / co-op goal | 65% / 74% | F11 | Ask your girlfriend first. **F42 extends F24** |
| F21 | Messenger batching with allowlist | 64% | notification listener | — |
| F22 | Save-for-later queue | 62% | — | — |
| F19 | Fetched digest | 69% (after D-015) | Apify | — |
| F08 | Morning lock | 64% | F01 | F61 (morning receipt) may be enough |

## Later

| ID | Feature | Score | Why later |
|---|---|---|---|
| F43 | Personal best | 72% | Overlaps F61 and F63 |
| F40 | Cumulative milestones | 72% | Nice, but F48 covers competence better |
| F51 (ours) | Calibration game | 71% | **Supersedes F36** (prediction check, 62%) |
| F54 (ours) | Fresh-window starter | 69% | Most useful at a life change |
| F52 (ours) | Seasons | 68% | Only if the good-week layer fades by week 6 |
| F56 (ours) | Split box with a break | 65% | Test on YouTube first |
| F50 (ours) | Left-early leaf | 63% | The only reward on the gated side; n-of-1 test only |
| F41 | Earned trust | 62% | Design reasoning only |
| F25 | Worth-it curve time boxes | 60% | Needs weeks of F10 data first |
| F55 (ours) | Feature-aware time box | 56% | Feed-screen detection is fragile |
| F34 | Urge-surf gate form | 51% | Thin evidence; one form inside F30 |
| F20 | Hot around me | 60% | — |
| F07 | One-video pass | 47% | Hardest to detect |
| F71 | Smart wake window | 44% | No validated trial (P49) |
| F69 | Own mattress accelerometer log | 44% | Needs the phone in bed (against S1), adds little (P42) |
| F70 | On-device sleep sounds | 27% | Mic all night; snoring isn't the problem. Open question: drop F69–F71? |

## Out (fails a must-pass gate)

| ID | Feature | Why |
|---|---|---|
| F37 / F45 | Earn screen time with exercise or phone-free points | Makes the feed the prize (P26) |
| F46 | A pet or tree that suffers when you scroll | Punishment (P37) |
| F47 | Leaderboard with friends | Social comparison backfires (P37, P30) |
| — | Seeds for redirected urges | Rewards opening a gated app |

## Merges at a glance

- **F15 → F26 + F59**: the Instead list becomes one-tap and sized to the moment.
- **F04 → F29**: one budget for the feed apps instead of one per app.
- **F24 → F33 → F42**: share with a partner becomes a shared goal.
- **F36 → F51**: the prediction check becomes the calibration game.
- **F31 + F53**: one night screen. **F13 + F44 + F61**: the sleep plan as a quest, plus the morning receipt.
- **F05 → F30 / F62**: friction changes its form and goes where the risk is, instead of just getting longer.
- **W3 / D-019 → F32 + F48 + F49**: no streaks anywhere; roots and comebacks instead.

## Open decisions for you

1. Install `gh` so approved items can become GitHub issues (D-021)?

2. Is the garden widget (F38) OK under D-019, or does "no streaks, no confetti" also mean no game display?
3. Should F23 (n-of-1 experiments) move up, so every new feature is measured from the start?
4. Is your girlfriend in for F42 (a shared goal)?
