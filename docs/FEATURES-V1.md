# v1 feature brainstorm and priority

Status: **proposed**, 2026-10-18. You decide the tiers. The scores come from RESEARCH.md, BASELINE.md
and the build effort on a Samsung S22 Ultra (Android 15).

## The candidates

Every idea we raised so far, plus one new one from your data (F21).

- **F01 Open-time gate**: A pause plus "What for?" when you open a target app. (E1, E2)
- **F02 Time box + stop screen**: Pick 5/10/15/30 min when you open, then a full-screen stop. (E3)
- **F03 Bedtime wind-down lock**: Feeds, games and YouTube's home feed locked from 45 min before planned bedtime. (S1)
- **F04 Daily budgets, changes apply next day**: Calm-you sets the limits, and tempted-you can't raise them today. (E4)
- **F05 Escalating, rotating friction**: The delay grows with each open that day, and the wording changes. (E5)
- **F06 Short-video blocking**: Blocks the Reels and Shorts screens inside the apps. (E8)
- **F07 One-video pass**: A video someone sent you plays, but the next swipe is blocked. (E10)
- **F08 Morning lock**: No feeds until 30 min after your planned wake time. (S2)
- **F09 Usage logger + views**: Logs app opens, sessions and unlocks. Shows findings in words, per-day and per-hour bars, and the heatmap as switchable views (D-018). (A1)
- **F10 1-tap worth-it rating**: 👍 / 😐 / 👎 when a session ends. (A2)
- **F11 Weekly review, one change**: 10 min on Sunday: charts against the baseline, then pick one change. (F3)
- **F12 WHO-5 weekly**: A 5-item well-being check inside the review. (A3)
- **F13 Sleep-shift plan + regularity**: Moves wake and bed times 15 min every 3 days, with a light prompt and a regularity score. (S3, S5)
- **F14 If-then plans at the trigger**: Your own plan shown at the moment of the trigger (in bed, after waking, coding break). (F1, F2)
- **F15 Instead list**: Alternatives you wrote, grouped as move/people/read/build/rest. (R1)
- **F16 Gate self-protection**: 24 h cooldown to switch off, plus a health check if Samsung kills the service. (N5, N6)
- **F17 Calm launcher hub**: One screen of links: group time, X Lists, Substack, HN, VnExpress, all opened through the gate. (E7, B6, B7)
- **F18 Evening group time**: FB groups and pages unlock 21:00–21:20, sorted by newest. (E7)
- **F19 Fetched digest 3×/day**: HN, Substack, VnExpress and Barça pulled in, ranked by your interests, finite. (C1–C8)
- **F20 Hot around me**: Top 5 Vietnam trends once a day, with headlines. (C6)
- **F21 Messenger batching with allowlist**: Messenger notifications delivered 3×/day, except people on the allowlist (e.g. your girlfriend). (new)
- **F22 Save-for-later queue**: Long reads go to reading time. (C7)
- **F23 Single-person (n-of-1) experiments**: A/B weeks for one rule. (F6)
- **F24 Share with partner**: Weekly summary sent to someone you choose. (F5)

## Scoring

Criteria and weights are **proposed**, derived from your goals: change behaviour (evidence, and fit to
your actual usage), build it solo (effort), and keep using it (annoyance).

| Criterion | Why it matters | Weight |
|---|---|---|
| Evidence | Does science say it changes behaviour or well-being? (P*) | ×3 |
| Fit to your data | Does it hit what BASELINE.md shows (bed, 25 FB opens a day, night blocks)? | ×3 |
| Build effort (5 = easy) | You're solo, and v1 has to ship | ×2 |
| Low annoyance (5 = low) | Tools you resent get uninstalled (Lyngs 2020) | ×1 |

Gate (must pass, not weighted): free to run, and no risk to your accounts. All 24 pass. Max score = 45.

| # | Feature | Evidence ×3 | Fit ×3 | Effort ×2 | Annoyance ×1 | **Weighted total** | Tier |
|---|---|---|---|---|---|---|---|
| F09 | Usage logger + heatmap | 4/5 Harkin 2016: recorded monitoring d = .47 | 5/5 needed: dumpsys only keeps 24 h | 5/5 UsageStatsManager | 5/5 passive | **42/45 (93%)** | **v1.0** |
| F04 | Daily budgets, changes apply next day | 5/5 Allcott 2022 RCT: limit-setting cut use substantially | 4/5 FB 1.7 h/day average | 4/5 counters + settings | 3/5 strict, but it's your own rule | **38/45 (84%)** | **v1.0** |
| F13 | Sleep-shift plan + regularity | 4/5 Windred 2023 (A), Burgess 2003 | 5/5 02:00–09:45 now | 4/5 schedule + phone-based estimate | 3/5 slips will happen | **38/45 (84%)** | **v1.0** |
| F14 | If-then plans at the trigger | 5/5 Gollwitzer & Sheeran: d = .65 | 4/5 the triggers are known | 4/5 content inside F01 | 3/5 repetitive | **38/45 (84%)** | **v1.0** |
| F18 | Evening group time | 3/5 P4 + D-001 | 5/5 FB is your #1 app; groups are the reason | 5/5 part of F17 | 3/5 a fixed time can clash | **37/45 (82%)** | **v1.0** |
| F01 | Open-time gate | 4/5 one sec PNAS RCT: 36% of opens dismissed | 5/5 68 self-started FB opens in 24 h | 3/5 accessibility service + overlay | 3/5 annoying, and the effect wears off | **36/45 (80%)** | **v1.0** |
| F11 | Weekly review, one change | 4/5 Harkin 2016 meta-analysis | 4/5 the loop that tunes everything | 4/5 charts | 4/5 once a week | **36/45 (80%)** | **v1.0** |
| F02 | Time box + stop screen | 3/5 Rixen: stopping comes from outside the app; Allcott: limits work | 5/5 unbroken 60-min FB blocks at 01–03 | 4/5 a timer on top of F01 | 3/5 can feel bossy | **35/45 (78%)** | **v1.0** |
| F03 | Bedtime wind-down lock | 3/5 pilot RCT (He 2020) + bedtime procrastination (Kroese) | 5/5 2 h 24 m of FB after midnight | 4/5 schedule + F01 | 3/5 night-you will resent it | **35/45 (78%)** | **v1.0** |
| F15 | Instead list | 3/5 Castelo 2025 displacement; Singh 2023 | 4/5 "bored" is the main reason you open | 5/5 a list | 4/5 optional | **35/45 (78%)** | **v1.0** |
| F17 | Calm launcher hub | 3/5 Lyngs 2020: keep the info, drop the feed | 4/5 your real reasons for opening | 5/5 links only, no fetching | 4/5 helpful, not restrictive | **35/45 (78%)** | **v1.0** |
| F05 | Escalating, rotating friction | 3/5 Meinhardt 2026: pop-up effect fades fast | 4/5 25 FB opens/day | 5/5 small add-on to F01 | 3/5 long delays annoy | **34/45 (76%)** | **v1.0** |
| F10 | 1-tap worth-it rating | 3/5 Lukoff 2018 experience sampling | 4/5 the core of the G1 metric (D-004) | 5/5 one sheet | 3/5 prompt fatigue | **34/45 (76%)** | **v1.0** |
| F16 | Gate self-protection | 4/5 precommitment (Allcott 2022) | 4/5 the gate is worthless if it silently dies | 3/5 Samsung battery quirks | 4/5 invisible | **34/45 (76%)** | **v1.0** |
| F12 | WHO-5 weekly | 3/5 validated scale; measurement only | 3/5 needs a baseline early | 5/5 a form | 4/5 1 min | **32/45 (71%)** | v1.1 |
| F08 | Morning lock | 2/5 no direct study | 4/5 FB 21 min right after waking at 12 | 4/5 schedule | 3/5 can feel strict | **29/45 (64%)** | v1.1 |
| F21 | Messenger batching with allowlist | 4/5 Fitz 2019 field RCT | 3/5 93 interruptions in 24 h | 3/5 notification listener | 2/5 risky for relationships | **29/45 (64%)** | v1.1 |
| F22 | Save-for-later queue | 2/5 indirect | 3/5 Substack habit | 4/5 a list | 5/5 none | **28/45 (62%)** | v1.1 |
| F20 | Hot around me | 2/5 supports G2 only | 3/5 you asked for it | 4/5 one RSS | 4/5 low | **27/45 (60%)** | Later |
| F06 | Short-video blocking | 3/5 design reasoning (P6); no head-to-head RCT | 3/5 you say they have no value; the data can't separate them | 2/5 fragile detection of the screen inside the app | 4/5 you want it | **26/45 (58%)** | Later |
| F19 | Fetched digest 3×/day | 3/5 Fitz 2019 batching; Robertson/Rathje on ranking | 3/5 Substack app already used; X/FB not fetchable | 2/5 fetching, ranking, UI, offline: the biggest build | 4/5 pleasant | **26/45 (58%)** | Later |
| F23 | Single-person (n-of-1) experiments | 2/5 method, not an intervention | 3/5 useful from month 2 | 3/5 stats UI | 4/5 optional | **25/45 (56%)** | Later |
| F24 | Share with partner | 3/5 Harkin: reported monitoring > private | 2/5 you want self first | 3/5 export/share | 3/5 privacy | **24/45 (53%)** | Later |
| F07 | One-video pass | 2/5 indirect (P8) | 3/5 her videos matter to you | 1/5 hardest to detect reliably | 4/5 removes a reason to bypass the block | **21/45 (47%)** | Later |

Tiers: ≥34 → v1.0, 28–33 → v1.1, <28 → Later.

## What the table shows

- **v1.0 is "gate + sleep + mirror + calm launcher".** The top scorers all hit the one thing your
  data screams: self-started Facebook opens and night-time blocks.
- **The fetched digest (F19) lands in Later**, even though it was the original "central app" idea.
  It is the biggest build, and its main sources are either already healthy (Substack) or can't be
  fetched (FB groups, X). The **calm launcher (F17)** gives you the central screen in v1 at a
  fraction of the cost.
- **Sensitivity.** If effort only counts ×1, F19 scores 24/40 (60%) and stays in Later. If you rate
  its fit 5/5 ("I really want the digest"), it scores 32/45 (71%) and moves up to v1.1, but not v1.0.
  The top of the table doesn't change under any plausible weights, because evidence and fit both
  favour the gate and sleep features.
- **F21 (Messenger batching)** is evidence-backed, but risky for your relationship until the
  allowlist exists. It goes to v1.1.
- **F06 short-video blocking** sits at 26/45 (58%), in Later, because detecting Reels inside the app
  is fragile. If the stack spike shows detection is reliable (effort 4), it scores 30 and moves to
  v1.1. Until then, the open-time gate and time box already cover the apps that host Reels.

## Proposed v1.0 build order

1. **F09** logger + heatmap: gives a baseline week before anything else changes.
2. **F16 + F01 + F02 + F05**: the gate core, protected from silently dying.
3. **F03 + F13**: sleep.
4. **F17 + F18**: calm launcher and group time.
5. **F14 + F15 + F10**: what to do instead, and the worth-it rating.
6. **F04**: budgets, once a week of data shows sensible numbers.
7. **F11**: the weekly review.

**One proposed exception: pull F12 (WHO-5, v1.1, 32/45) into v1.0.** It costs almost nothing, and a
well-being baseline can't be recorded after the fact.

## Update 2026-10-18: re-scoring after D-010

Apify makes the public FB pages and X fetchable, so **F19 (fetched digest)** changes:
- **Fit 3 → 5/5:** Barça pages and X are now in the hub, and those are the reasons you open FB and X.
- **Effort 2 → 3/5:** Apify returns clean JSON, so we write no scraper. The ranking UI is still to build.

New total: 3·3 + 5·3 + 3·2 + 4·1 = **34/45 (76%) → v1.0, right at the threshold.**
F18 (group time) stays in v1.0 for the private groups, now scheduled for weekends and match days.

Proposed v1.0 build order, step 4 becomes: **F17 + F18 + F19-lite**. F19-lite is one evening digest
with the 2 pages, your X Lists, HN and Substack. It's finite, with no engagement counts. Going to
3 digests a day waits for v1.1, so the Apify budget can be measured first.

## Update 2026-10-18 (D-015): X is out of v1

F19 drops to **31/45 → v1.1**, because the digest has no X content without a paid API. Step 4 of the
v1.0 build order goes back to **F17 + F18**: the calm launcher (including your X Lists as gated
links) and group time. In v1.1, F19 starts with the FB pages, HN, Substack, VnExpress, Trends and
Barça RSS.
