# Requirements: v0.3

Status: **draft, interview rounds 1–2 applied**, 2026-10-17. Working name `mindful-hub`.
IDs are stable. Every feature traces to a research principle (`P*`, see [RESEARCH.md](RESEARCH.md))
or a goal (`G*`). If it traces to neither, it doesn't get built. Decisions are in
[DECISIONS.md](DECISIONS.md).

Releases: **v1** = the smallest version that changes behaviour, **free to run**. **v1.1** = next.
**Later** = parked. **Won't** = rejected on purpose.

## Who this is for (from the interview)

- Android, mostly on the phone. The triggers are **in bed, right after waking, coding breaks**, and
  free time in general.
- Phone use, from Digital Wellbeing:

| Week | Total | Top apps |
|---|---|---|
| Last week | 35 h 13 m (≈5.0 h/day) | Facebook 9 h 42 m · Mobile Legends 4 h 51 m · Threads 4 h 00 m |
| The week before | 52 h 23 m (≈7.5 h/day) | YouTube 14 h 37 m · Facebook 13 h 10 m · X 8 h 31 m |

  For reference, people in Castelo 2025 started at 5.2 h/day and dropped to 2.7 h/day (P15).
- Real needs: new posts in a few FB groups and pages (end of day is fine), Barça, coding talk,
  HN and Substack, what's trending in Vietnam, and messages and videos your girlfriend sends.
- Short-video feeds: no value, so block them.
- **YouTube** is mostly game streamers, drama and entertainment, with a little coding talk.
  Podcasts and coding talks can come later.
- **Mobile Legends** is played alone, to relax after work.
- **Sleep:** now about 02:00 → 09:45. Target: 00:00–00:30 → 08:00–08:30 (P19, P20).
- Phone: **Samsung Galaxy S22 Ultra** (One UI). Installing the app outside the Play Store is OK.

## The problem, in one paragraph

You open these apps for real reasons. Infinite, engagement-ranked feeds then keep you there,
mostly through *habit* (P2, P14). Self-discipline is weak right now. So the fix can't be "try
harder" (P1). Two things have to be true together:
1. **The real reasons get served somewhere calm and finite** (P4, P5).
2. **The path into the feeds gets friction that calm-you sets in advance** (P2, P3).

Success is not "zero phone". It's **less use you didn't choose**, **better sleep**, and **time that
goes to a mix of things you value** (P14–P18).

## Goals

Targets are **proposed**. Set the real ones after the baseline (A1). Judge over **8–12 weeks**
(P11).

| ID | Goal | Measured as | Proposed target (week 8–12) |
|---|---|---|---|
| G1 | **Less compulsive use** (headline, D-004) | *Unintended minutes per day*: sessions opened as "just bored", time past the time box, and sessions rated 👎 | −50% against baseline |
| G2 | Still informed | Weekly question: "Did I miss something I cared about?" | ≤1 "yes" per month |
| G3 | Feel better | WHO-5 weekly; share of sessions rated "worth it" | WHO-5 stable or rising; ≥75% worth it |
| G4 | In control | Share of sessions that started with an intention *and* ended within the time box | ≥80% |
| G5 | A balanced life, not a max-productivity one | Weekly hours by category: **move · people · read · build · rest** | Every category >0, nothing compulsive (P18) |
| G6 | The hub itself isn't addictive | Time in the hub; hub opens outside digest windows | ≤25 min/day; ≈0 opens outside windows |
| G7 | **Sleep protected** | Phone minutes after bedtime; minutes from waking to the first feed app | ≤10 min after bedtime; ≥30 min before the first feed |
| — | *Context only* | Total screen time per app (never a goal by itself, P14) | — |

## Core loop

```
 calm-you sets rules (Sunday review) ─────────────────────────────────────────────┐
   │ budgets, bedtime, if-then plans, interests, digest times                     │
   ▼                                                                              │
 ┌──────── trigger: in bed · just woke up · coding break · bored ──────────────┐  │
 │                                                                             │  │
 │  opens FB/IG/Threads/X/YouTube/ML              opens the hub                │  │
 │        │                                            │                       │  │
 │        ▼                                            ▼                       │  │
 │  GATE (E): pause → "what for?" →            DIGEST (C): 3×/day, finite,     │  │
 │  time box · budget · bedtime rules          ends "done, next at 19:00"      │  │
 │        │                                     evening: GROUP TIME (E7)       │  │
 │        ├─ "bored" → Instead list (R1): walk, book, lc-trainer, call her…    │  │
 │        ├─ "she sent me something" → one-video pass (E10)                    │  │
 │        ▼                                                                    │  │
 │  time box ends → 1-tap "worth it?" (A2)                                     │  │
 └─────────────────────────────────────────────────────────────────────────────┘  │
   │ logs: unintended min, worth-it, sleep-window use, life-balance hours, WHO-5  │
   ▼                                                                              │
 weekly review (F3): compare with baseline → change ONE thing ────────────────────┘
```

## Week 0: setup that needs no code (start now)

The research says changing your surroundings beats willpower (P1). These steps use features that
already exist, so they work before any code is written. They do **not** count as the baseline;
see A1.

| ID | Action | Why |
|---|---|---|
| Z1 | YouTube: turn **autoplay off**. Keep watch history and recommendations (D-008) | P6 |
| Z2 | Make two X Lists, "Barça" and "Coding". Bookmark the lists, not the home timeline | P4, D-002 |
| Z3 | **Charge the phone away from the bed**, at least an arm's reach plus one step | P19 |
| Z4 | Samsung Digital Wellbeing → Bedtime mode at the *current* sleep-plan bedtime (grayscale + Do Not Disturb). It's easy to override, so treat it as a stopgap until S1 exists | P19 |
| Z5 | Same wake time every day, weekends too, with an alarm. Get outside light within 30 min of waking | P19, P20 |

## v1

### A. Baseline and measurement
| ID | Requirement | Why |
|---|---|---|
| A1 | Baseline: pull the usage history Android keeps (`adb dumpsys usagestats` spike, then the app's own logger) and build an **hour × day heatmap** per app. Then run **1 week** with logging only and no gate. Week 0 changes (Z) are marked on the timeline so their effect can be separated | P13 |
| A2 | When a gated session ends, ask one question with one tap: "Worth it? 👍 / 😐 / 👎". Nothing more | P13, P14, G1 |
| A3 | WHO-5 every week, in the review | G3 |
| A4 | Every week: "Did I miss something I cared about? What?" | G2, D-001 |
| A5 | Log the time per G5 category, one tap each (e.g. "walked 30 min"). lc-trainer is linked in v1.1 | P16, P18, G5 |
| A6 | All data stays local and can be exported (CSV or JSON) | N1 |

### B. Sources (all free, D-002)
| ID | Requirement | Why |
|---|---|---|
| B1 | Hacker News: top stories filtered by your coding interests | G2 |
| B2 | Substack: the publications you list (RSS) | G2 |
| B3 | Vietnamese news: the VnExpress sections you pick (RSS) | G2, P4 |
| B4 | Trending in Vietnam: Google Trends RSS (`geo=VN`) | G2, P4 |
| B5 | Barça news: Google News RSS (Vietnamese). Fixtures and results from football-data.org if its free key works | G2 |
| B6 | **Public FB pages** (BarcaVietnamExpress, Coviahe): fetched through Apify, the 5 newest per page, once a day (D-010). Private groups: not ingested; reached in group time (E7) | N2, N3 |
| B7 | **X (v1)**: not ingested. Gated links to your "Barça" and "Coding" X Lists, built from SOURCES.md (D-015). Ingestion through an API is parked until v1 proves useful | N3 |
| B9 | Barça fixtures (free source still to be chosen) drive match-day group time (E7) and replace live-score posts | E7, P6 |

### C. Digest
| ID | Requirement | Why |
|---|---|---|
| C1 | 3 digests a day at times you set (default 07:30 *after the morning lock*, 12:30, 19:00). Content is frozen between digests | P5 |
| C2 | Each digest is **finite** (≤15 items) and ends with "You're done, next digest at X". No infinite scroll, no pull-to-refresh | P6 |
| C3 | Ranking uses your declared interests and source priority, **never engagement**. No like or view counts | P7 |
| C4 | Simple keyword and source rules to down-weight outrage bait (free). An LLM rewrite of headlines moves to Later, because v1 must be free | P7 |
| C5 | Item summaries use the source's own RSS excerpt (free) | P4 |
| C6 | **Hot around me**: once a day, the top 5 Vietnam trends with their linked news headlines | G2, P4 |
| C7 | "Save for later" sends long reads to a queue for reading time (a G5 "read" activity) | G5 |
| C10 | **Our own recommender** (D-011): candidates come from the seed network and topic searches, ranked by your 👍/👎. A discovery slot of 2 per digest, and up to 3 suggested accounts in each weekly review, which you accept or reject | P22, P7, P12 |
| C8 | The only notification is "Digest ready" (optional) | P5 |

### D. The hub limits itself
| ID | Requirement | Why |
|---|---|---|
| D1 | Opening the hub outside a digest window shows "Next digest at X" and the saved queue. Nothing new appears | P5, G6 |
| D2 | No autoplay, no video feed, no comments or likes in the hub | P6, P7 |
| D3 | The hub's own time is tracked and shown in the review | G6 |

### E. Gate (Android)
| ID | Requirement | Why |
|---|---|---|
| E1 | Opening Facebook, Instagram, Threads, X, YouTube or Mobile Legends brings up a pause (default 5 s) and "What for?". The options are: group time · message someone · something someone sent me · post something · watch a specific video · play N matches · *just bored* | P3 |
| E2 | "Just bored" shows the Instead list (R1) and when the next digest is due. You can still go on, but with a longer delay. You decide | P9, P12 |
| E3 | You pick a time box when you enter (5/10/15 min, or "N matches" for games). When it ends, a full-screen stop appears, then A2 | P6, P17 |
| E4 | Calm-you sets a daily budget for each feed app. **Changes apply the next day** | P2 |
| E5 | The delay grows with each open that day, and the wording rotates. Keep it explicit | P3 |
| E7 | **Group time** for *private* groups: 20 min, by default **only on weekends and Barça match days** (D-010). The hub shows a button for each group, which opens it sorted by newest (URL to verify) | D-001, D-010, P4 |
| E8 | Short-video feeds are **blocked**: Reels (FB and IG), YouTube Shorts, and TikTok if it's installed | P2, P6 |
| E9 | Messaging (Messenger, IG DMs) gets a light gate: a short pause, no budget | P8 |
| E10 | **One-video pass**: a video opened from a chat (e.g. one your girlfriend sent) plays, and swiping to the next one is blocked or ends the pass. Feasibility to verify in the stack spike | P8, E8 |
| E11 | Mobile Legends counts as **rest** (D-006): the gate asks "how many matches?", gives a one-tap stop when they're done, and checks the sleep window. No daily minute budget | P17, P21 |
| E12 | YouTube (D-008): Shorts are blocked (E8). The gate's intent is "watch: a specific stream, channel or video" with a time box (15/30/60 min). Recommendations stay on, and the home feed is hidden only during wind-down (S1) | P6, P12 |

### S. Sleep and mornings (bed is your #1 trigger)
| ID | Requirement | Why |
|---|---|---|
| S1 | **Wind-down**: from 45 min before tonight's planned bedtime, feeds, games and YouTube are locked. Messaging stays open with a light gate. The hub shows only "Good night", the reading queue and a reminder to put the phone on the charger away from the bed | P16, P19, G7 |
| S2 | **Morning lock**: feed apps stay locked until 30 min after your planned wake time. The morning digest opens when the lock ends. Shows a "get outside light" prompt | P20, G7 (the morning lock itself is a design choice from P1; there is no direct study yet) |
| S3 | Shown in the review: phone minutes after bedtime, actual first and last phone use against the plan, and a phone-based sleep **regularity** estimate | P19, G7 |
| S4 | Changes to bedtime or the lock apply the next day | P2 |
| S5 | **Sleep-shift plan** (D-007): start where you actually are (≈02:00 → 09:45). Move the **wake time** 15 min earlier every 3 days, and bedtime follows. The target (00:00–00:30 → 08:00–08:30) is 6 steps, about 3 weeks, or longer if a step is held. Hold a step if the previous one didn't stick. Same times every day of the week | P19, P20, P11 |
| S6 | Late-night slips show the facts and the next step only (F4). One late night doesn't reset the plan | P12 |

### F. Coaching
| ID | Requirement | Why |
|---|---|---|
| F1 | Onboarding: pre-filled if-then plans for your triggers ("If in bed → book or sleep", "If coding break → walk 5 min", "If just woke → no feed, read the digest after"), which you edit | P9 |
| F2 | The matching plan appears at the trigger moment | P9 |
| F3 | **Weekly review** (Sunday, ~10 min): G1 and G7 first, then worth-it share, WHO-5, the G5 balance chart, "missed anything?", then choose **one** change. The review is saved | P10, P13, D-004 |
| F4 | A bad day shows the facts and one next step. No shame, no streaks | P11, P12 |

### R. Reclaimed time
| ID | Requirement | Why |
|---|---|---|
| R1 | An **Instead** list, grouped by G5 category, with evidence-based defaults you can edit: **move** (a walk without the phone, a short workout), **people** (call or meet your girlfriend or friends), **read** (a book chapter, a saved Substack article), **build** (a 25-min lc-trainer session or personal project), **rest** (sleep, let your mind wander on a walk) | P16, P18, P8 |

## v1.1
| ID | Requirement | Why |
|---|---|---|
| F5 | Share the weekly summary with someone you choose (e.g. your girlfriend), with you picking which fields | P10 |
| F6 | Single-person (n-of-1) experiments: alternate weeks with and without one rule, and compare | P13 |
| R2 | Link to lc-trainer so practice time logs automatically as "build" | G5 |
| R3 | Personalized Instead suggestions, ranked by what you actually did and rated 👍 | P12, G5 |
| C9 | Free on-device or free-tier LLM summaries and neutral headlines, if quality is good enough | P7 |

## Later
- Desktop feed hiding for facebook.com and x.com (D-003).
- Mindfulness micro-practice at the gate. It needs its own evidence review first.

## Won't
| ID | What | Why |
|---|---|---|
| W1 | Infinite scroll, pull-to-refresh, autoplay | P6 |
| W2 | Engagement ranking; like, view or share counts | P7 |
| W3 | Streaks that reset to zero; "good" or "bad" labels on time | P12, P14 |
| W4 | Headless scraping of Facebook with your account | N2 |
| W5 | Social features inside the hub | P7, G6 |
| W6 | Paid APIs in v1 | N3 |
| W7 | Maximizing "productive hours" as a goal | P18 |

## Non-functional requirements
| ID | Requirement |
|---|---|
| N1 | Local-first. Usage and well-being data stay on the phone |
| N2 | Never put your Facebook or X accounts at risk: no automated logged-in scraping |
| N3 | v1 costs **$0** a month, a hard requirement (D-015): no paid APIs, no server. Apify stays inside its free $5/month credit (account hard cap, plus `maxTotalChargeUsd` on every run, plus a monthly spend meter in the review) |
| N4 | The gate appears within about 300 ms of an app opening |
| N5 | Turning the gate off inside the app needs a 24 h cooldown (P2) |
| N6 | It must survive Samsung's battery optimization: setup walks you through adding the app to "Never sleeping apps" and allowing restricted settings (needed for the accessibility permission on a self-installed app). A health check warns if the gate stops running |
