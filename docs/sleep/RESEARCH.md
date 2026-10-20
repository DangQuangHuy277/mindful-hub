# Research: sleep tracking

Status: **proposed**, 2026-10-20. Nothing here is decided yet. This builds on P16, P19 and P20 in
[RESEARCH.md](../RESEARCH.md) and on what's already built for sleep (F03 wind-down, F13 + F44 sleep
quest, F61 morning receipt). It asks: what can a phone honestly measure about your sleep, which
numbers are worth showing, and how do we show them without making sleep worse?

Sources marked † were checked online on 2026-10-20: the DOI resolves on Crossref to the right
authors, venue and year, and the numbers quoted here were read in the abstract or full text.
"† (secondary)" means the paper exists, but the number comes from a review, an editorial or a
press summary, not the paper itself. Unmarked statements are from memory. Check them before you
rely on them. The full reference list, with DOIs, is in [SOURCES.md](SOURCES.md). The evidence
grades (A/B/C) are the ones defined in [RESEARCH.md](../RESEARCH.md).

New principles start at **P42**. New feature ideas start at **F64**.

## Read this first: what sleep tracking does *not* promise

- **Nothing without a sensor on the body detects *wake* well.** Even research wrist actigraphy
  finds sleep 96.5% of the time but finds wake only 32.9% of the time (Marino et al. 2013†, 77
  people, 232,849 epochs vs. polysomnography (PSG)). Phone-on-mattress apps are worse.
- **No phone app has shown it can stage sleep.** In a PSG study, Sleep Cycle (accelerometer *and*
  microphone modes) wasn't reliable even for sleep vs. wake, and none of the 4 apps tested could
  detect REM (Fino et al. 2020†, 21 adults, 2 nights). Another app's "light" and "deep" sleep
  didn't correlate with PSG at all (r = 0.02 and 0.18; Bhat et al. 2015†).
- **A phone "sleep graph" is not a hypnogram.** Without a body sensor, the curve can only reflect
  movement or sound, so its "deep sleep" means "not moving", not a measured sleep stage.
- **Numbers can make sleep worse.** People with insomnia who were *told* (falsely) that they had
  slept badly felt worse that day: alertness d = 0.79, sleepiness d = 0.55 (Gavriloff et al.
  2018†, RCT, n = 63). "Orthosomnia" (chasing a perfect tracker score) was first described in
  patients who stayed in bed longer to raise their scores (Baron et al. 2017†, 3 cases).
- **What a phone *can* measure is timing and regularity.** That matters: regularity predicted
  all-cause mortality better than duration did (Windred et al. 2024†, 60,977 people). Timing is
  also exactly what your sleep shift (S5) changes.

**What this means for the app:** measure *when* the phone goes down and comes back, and *how
regular* that is. Ask *you* how you slept. Don't show stages, cycles or a nightly score.

## 1. Sleep/wake detection without a wearable

How good each method is, against PSG unless noted. Sensitivity = sleep correctly called sleep.
Specificity = wake correctly called wake. For you, specificity is the number that matters, because
lying awake in bed is the problem you're trying to see.

| Method | What it uses | Accuracy | Source (strength) |
|---|---|---|---|
| **Wrist actigraphy** (Cole-Kripke, Sadeh, Oakley) | Wrist movement counts per epoch, a weighted window of neighbouring epochs, and a threshold | Cole-Kripke: **88%** of minutes right; r = 0.82 for sleep % and 0.90 for sleep latency (41 people, algorithm tuned on 20, tested on 21). Across modern validation: sensitivity **0.965**, specificity **0.329**, and actigraphy found **36.8** min of wake after sleep onset (WASO) where PSG found **49.1** | Cole et al. 1992† (B); Sadeh, Sharkey & Carskadon 1994† (36 people, both wrists; sleep scored better than wake, B); Marino et al. 2013† (A–B). Oakley 1997 is a manufacturer technical report (Mini Mitter / Actiwatch); I couldn't find it online, so its numbers aren't quoted here |
| **Clinical position on actigraphy** | — | The AASM *suggests* (conditional, not strong) actigraphy for estimating sleep patterns in insomnia, circadian rhythm disorders and suspected insufficient sleep. It recommends *against* using it in place of EMG for periodic limb movements | Smith et al. 2018† AASM guideline (A; recommendation wording partly secondary) |
| **Phone on the mattress** (accelerometer) | Bed movement picked up through the mattress | Sleep Time app: sensitivity **89.9%**, specificity **50%**, sleep/wake accuracy 85.9%, but only **45.9%** of epochs got the right stage. It overestimated sleep latency by 15.6 min and deep sleep by 11.1%. Sleep efficiency r = −0.13 with PSG (20 adults). MotionX 24/7 (phone under the sheet, 78 children with suspected sleep apnea): overestimated total sleep by **106 min** and sleep efficiency by 17% | Bhat et al. 2015† (C); Toon et al. 2016† (B); Fino & Mazzetti 2019† review (B) |
| **Sleep Cycle** (accelerometer or microphone) | Movement or movement sounds | Neither mode reached sufficient sleep/wake reliability. Of the 4 apps tested, only "Smart Alarm" gave epoch-by-epoch output, and it was still poor at wake and deep sleep. No app detected REM | Fino et al. 2020† (21 adults at home, portable PSG, C). The only PSG study of Sleep Cycle I found |
| **Screen and touch events** (passive phone use) | Screen on/off, unlocks, touches | Screen on/off alone: **0.89** accuracy against armband sleep trackers (400+ people; Bayesian model). Touches ("tappigraphy"): sleep onset and wake time from touches correlated with wrist actigraphy at **R² = 0.9** (79 people, ~1,400 nights). Touches stayed above zero for about **2 h** around the estimated sleep boundaries, and nearly everyone used the phone *inside* the window that actigraphy called sleep. 9 people over 97 days: phone-use patterns tracked sleep timing, duration and sleep debt | Cuttone et al. 2017† (B); Borger, Huber & Ghosh 2019† (B); Abdullah et al. 2014† (C) |
| **Many phone sensors combined** (light, sound, motion, screen) | Random forest, free phone placement | Sleep vs. wake **81.8%** from sensors, **86.9%** from time of day *alone*, 88.8% combined. Errors (median absolute deviation, vs. self-report): **43 min** for sleep start, **38 min** for sleep end, **58 min** for duration. Missing data was twice as common during sleep (phone off or dead) | Saeb et al. 2017† (208 people, 6 weeks, B). Toss 'n' turn: 93% sleep/wake accuracy vs. diaries, but only 27 people, and personal models needed ~3 days of labels (Min et al. 2014†, C). Chen et al. 2013† is the earlier "best effort sleep" model; I couldn't read its numbers |
| **Microphone, sleep sounds** (breathing, snoring, movement) | Audio features per 30 s epoch | Pro directional mic 1 m above the head, 250 lab patients (most with moderate sleep apnea): sleep/wake **91.7%**, κ = 0.68. Wake/NREM/REM 86.9%, κ = 0.69. Phone mic, deep learning: 4 stages **κ = 0.485** on smartphone recordings (about 68% agreement), 2 stages 89.4% on PSG-room audio. iSleep (7 people, 51 nights): >90% for classifying events (movement, cough, snore), not stages | Dafna, Tarasiuk & Zigel 2018† (B); Hong et al. 2022† (B; 4 of 8 authors work for Asleep Inc., which sells this); Hao, Xing & Zhou 2013† (C) |
| **Sonar** (phone speaker + mic, inaudible sound) | Chest movement from reflected sound | Apnea and hypopnea event counts correlated with PSG at ICC 0.95–0.99 (37 sleep-clinic patients). This is for apnea screening, not everyday tracking. Licensed to ResMed | Nandakumar, Gollakota & Watson 2015† (C) |

**Bottom line for section 1.** Every method finds *sleep* well and *wake* badly. The phone's own
use is the one signal that is *certain* in one direction: if you are tapping, you are awake.
But "the phone is down" is not "asleep". You can lie awake without it, and Borger 2019 found
touches stay common for about 2 hours around sleep. A stopwatch on the phone measures your
**phone-free window**, an upper bound on sleep, not sleep.

## 2. Sleep stages and "sleep cycles"

- **The cycle.** Sleep moves through NREM (N1, N2, N3 "deep" or slow-wave) and REM in cycles of
  roughly 90 minutes. Early cycles hold most of the deep sleep, and later cycles hold more REM.
  Cycle length varies between cycles, nights and people (Feinberg & Floyd 1979† exists and
  studied exactly these across-night trends; the numbers here are from memory). A fixed
  "90-minute" rule is a teaching simplification, not a clock you can set an alarm by.
- **What PSG needs.** Stages are defined by brain waves (EEG), eye movements and muscle tone. A
  phone measures none of these. Wrist and ring devices infer stages from movement, heart rate and
  heart rate variability.
- **Wearables with heart sensors do better, but only moderately.** Six wrist devices (Apple Watch
  8, Fitbit Charge 5 and Sense, Garmin, Whoop, Withings) on 62 adults: sensitivity >90% but
  specificity **29–52%**, and 4-stage agreement **κ = 0.21–0.53** (fair to moderate). Most
  differed significantly from PSG on total sleep, efficiency, WASO and light sleep (Schyvens et
  al. 2025†, B). Seven devices including three bedside (non-wearable) sensors, 34 adults:
  sensitivity ≥0.93, specificity 0.18–0.54, and worse on disrupted nights (Chinoy et al. 2021†,
  B). Oura's own team reports 79% 4-stage accuracy, but needed heart-rate and temperature
  features; movement alone gave 57% (Altini & Kinnunen 2021†, 106 people, both authors from
  Oura, B).
- **In-bed phone use specifically.** When people read, watched videos and texted on a phone in
  bed, every tracker misclassified some of that still wakefulness as sleep. Oura and Fitbit did it
  least (Willoughby et al. 2024†, 63 adults, B). A phone app relying on mattress movement can't
  tell lying still with a phone from sleeping, but the phone's own event log can.
- **Phone apps.** See section 1: no app passed PSG for stages (Fino 2020†, Bhat 2015†).
- **Clinical bodies agree.** The AASM says consumer sleep technology is not for diagnosis, but can
  start a conversation with a clinician (Khosla et al. 2018† exists; wording from memory).
  Reviews: de Zambotti et al. 2019†, Kolla et al. 2016†, Ko et al. 2015†.

## 3. Circadian rhythm, chronotype and regularity

| Measure | How it works | What it predicts | Can we get it passively? |
|---|---|---|---|
| **MEQ** (morningness-eveningness questionnaire) | 19 multiple-choice questions on preferred times | A trait-like preference score. Validated on 48 people against body-temperature peaks | No. One-off questionnaire. Horne & Östberg 1976† (abstract) |
| **MCTQ / µMCTQ** (Munich chronotype) | Sleep times on work days and free days. **Chronotype = MSFsc**: the mid-point of sleep on free days, corrected for catch-up sleep | The µMCTQ has 6 questions, matches the full MCTQ within 35 min, has good test-retest reliability, and correlates with actimetry and melatonin onset, especially on free days | Mostly yes: mid-sleep comes from the phone-free window. Roenneberg et al. 2003†; Ghotbi et al. 2020† (µMCTQ); Roenneberg et al. 2019† (self-critical review) |
| **Mid-sleep** | (sleep onset + wake) / 2 | Phase of your body clock; it moves with the sleep shift | Yes, from the phone-free window |
| **Social jetlag** | \|mid-sleep on free days − mid-sleep on work days\| | Linked with higher BMI in ~65k MCTQ entries (Roenneberg et al. 2012†, observational; per-hour figures only secondary). Wittmann et al. 2006† introduced the term | Yes, if you tag which days are work days |
| **Sleep Regularity Index (SRI)** | The chance you're in the same state (asleep or awake) at any two times 24 h apart, scaled from −100 to 100 (100 = perfectly regular) | 61 students over 30 days: SRI correlated with grades (r = 0.37), and the least regular had a melatonin onset ~2.5 h later, with no difference in sleep *duration* (Phillips et al. 2017†, B). In 1,978 older adults, irregularity went with cardiometabolic risk, stress and depression (Lunsford-Avery et al. 2018†, B). In 60,977 people, the top 4 SRI quintiles had **20–48%** lower all-cause mortality than the bottom one, and SRI beat duration (Windred et al. 2024†, A, observational) | Yes. It needs a sleep/wake state per minute, which the phone-free window gives |
| **Other regularity metrics** | Standard deviation of times, interdaily stability (IS), composite phase deviation (CPD) | SD and IS measure multi-day variability and need **>1 week** for unbiased estimates. CPD and SRI measure day-to-day changes. SRI and IS also capture naps and night awakenings; SD and CPD don't | Fischer, Klerman & Phillips 2021† (simulation study, B) |
| **Consensus** | — | The NSF panel agreed that consistent sleep and wake timing matter for health, safety and performance, and that weekend catch-up sleep may help when weekday sleep is short | Sletten et al. 2023† (63 papers, Delphi, A for consensus) |

**Light.** Half of the melatonin suppression happened at **<30 lux**, ordinary indoor light. People
differed **>50-fold** in sensitivity (Phillips et al. 2019†, 55 people, lab, B). Morning light
advances the clock (Burgess 2003, already P20).

## 4. Sleep quality: what makes a defensible score

- **Self-report is the clinical standard for quality.** The PSQI (Buysse et al. 1989†) covers one
  month in 7 components. From memory: a global score >5 means poor sleep. The Consensus Sleep
  Diary (Carney et al. 2012†) standardises the nightly morning questions: when you went to bed,
  tried to sleep, how long it took, awakenings, final wake, rise time, and a quality rating.
- **The sleep-continuity measures with expert consensus.** The NSF quality panel (277 studies)
  agreed that **sleep latency, awakenings >5 min, WASO and sleep efficiency** are good indicators
  of quality. There was little or no consensus on sleep architecture (stages) or naps (Ohayon et
  al. 2017†, A for consensus). The cut-offs reported in summaries for adults: latency ≤30 min,
  WASO ≤20 min, efficiency ≥85%, ≤1 awakening (†, secondary; check the paper before showing them).
  Normative values by age: Ohayon et al. 2004† meta-analysis.
- **Definitions.** Sleep efficiency = total sleep time ÷ time in bed. Latency = time from trying
  to sleep to asleep. WASO = wake time after first falling asleep. Note that a phone can't measure
  any of these without knowing when you actually fell asleep.
- **Duration.** Adults 18–64: 7–9 h recommended (Hirshkowitz et al. 2015† exists; range from
  memory).
- **Self-report has its own bias.** In 669 adults, self-reports averaged 6.8 h against 6.0 h
  measured. People who slept 5 h over-reported by **1.2 h**, people who slept 7 h by 0.4 h, and
  the correlation was only 0.47 (Lauderdale et al. 2008†, B). So a morning check-in is the right
  measure of how you *felt* about the night, and a poor measure of how long you slept.
- **A defensible score** (design reasoning, not a study): use the components with consensus
  (regularity, timing, your morning rating) and keep them separate. Buysse 2014† calls sleep health
  multi-dimensional (from memory: regularity, satisfaction, alertness, timing, efficiency,
  duration). Combining them into one number hides what changed and invites score-chasing (§6).
  A score built on time in bed rewards exactly what Baron 2017 warned against: staying in bed
  longer.

### Smart alarms (waking in light sleep)

- **The idea is sound in the lab.** Sleep inertia (the grogginess after waking) is worse when you
  wake from slow-wave (deep) sleep, and worse after sleep loss and at a bad circadian time (Tassi &
  Muzet 2000†; Hilditch & McHill 2019† review).
- **But no trial shows a smart alarm works.** The one controlled test I found: a mattress sensor
  started a light ramp when it detected light sleep 15–30 min before the alarm. In 29 adults over 4
  lab nights, there was **no overall difference** in vigilance, sleepiness or mood vs. a normal
  alarm. The sensor's light-sleep sensitivity was only 0.57. The study was funded by the company
  that made it (Campanella et al. 2024†, *Clocks & Sleep*, C). Its authors call commercial
  sleep-stage smart alarms "not validated".
- **Why it can't do much in the morning anyway** (from memory, the standard sleep architecture):
  deep sleep is concentrated in the first half of the night. In the last 30 minutes before a
  normal wake time you're mostly in N2 or REM already, so the alarm is choosing between similar
  states using a sensor that can't tell them apart (§1).
- **What works better:** a regular wake time (P19, Sletten 2023), enough sleep (sleep loss
  deepens inertia), and morning light (P20; light after waking also helped in Hilditch's lab work).

## 5. Evening phone and social media use vs. sleep

| Question | What the evidence says | Source (strength) |
|---|---|---|
| Is it linked? | Yes, in young adults. A systematic review of 42 moderate- or high-quality studies (ages 16–25): phone, computer, internet and social media use went with shorter sleep and poorer quality, and night phone use with later bedtimes and daytime tiredness. TV, consoles and tablets didn't show the same link. Causal direction unclear. In 1,788 US young adults, the top quartile of social media *frequency* had 2.92× the odds of sleep disturbance | Brautsch et al. 2023† (A for association); Levenson et al. 2016† (B, cross-sectional) |
| Adults too? | Texting or calling after lights out predicted worse PSQI, especially latency and efficiency (844 Flemish adults, 18–94) | Exelmans & Van den Bulck 2016† (B, cross-sectional; secondary detail) |
| Is it the light? | **Contested, probably smaller than claimed.** Lab: reading on a light-emitting e-reader for hours before bed delayed sleep, melatonin and the clock vs. a paper book (Chang et al. 2015†, B). Real life: iPhone Night Shift made **no difference** to any sleep outcome over 7 nights (167 students, actigraphy); only the *no-phone* group slept better, and only among those already sleeping >6.8 h (Duraccio et al. 2021†, B). The NSF panel reached **no consensus** that screen light impairs anyone's sleep (Hartstein et al. 2024†) | — |
| Is it the content / engagement? | The NSF panel agreed that pre-bed *content* impairs children's and adolescents' sleep, and that behavioural interventions help. It reached **no consensus on adults** for screen time, light or content. In 101 adolescents, fear of missing out drove late social media use (later bedtime) *and* pre-sleep arousal (slower sleep onset) | Hartstein et al. 2024† (consensus); Scott & Woods 2018† (C) |
| Bedtime procrastination | Going to bed later than intended with nothing forcing it. It predicted insufficient sleep beyond demographics and self-regulation (Kroese et al. 2014†, n = 177, B). Meta-analysis of 43 studies: moderate links with **low self-control** (z = −0.39) and **evening chronotype** (z = 0.43), and with shorter sleep (z = −0.31), worse quality (z = −0.35) and more fatigue (z = 0.32). Evidence certainty rated **low** (GRADE) | Hill et al. 2022† (A–B, correlational) |
| "Revenge" bedtime procrastination | Staying up to reclaim "me-time" after a day you didn't control. **No solid peer-reviewed base yet**: what I found was theses, small studies in minor journals, and blogs. Treat it as a plausible hypothesis that fits your pattern, not a finding | (search, 2026-10-20) |
| Does scrolling help you fall asleep? (your F03 note) | No good evidence that it does. Adults do report using media as a sleep aid; one survey found no link with sleep duration either way (Exelmans & Van den Bulck 2016b, *Behav Sleep Med*, † (secondary)). The main cost is likely displacement: every minute scrolling is a minute of later sleep. This one is worth testing on yourself (F67) | — |

**Interventions that worked (or didn't):**

| Intervention | Result | Source (strength) |
|---|---|---|
| No phone for 30 min before bed, 4 weeks | Shorter sleep latency, longer sleep, better PSQI, less pre-sleep arousal, better mood and working memory | He et al. 2020† (n = 38 pilot RCT, C–B; already P16) |
| Phone stop time 1 h before bed, 1 school week (adolescents) | About 17 min earlier lights out and 19 min more sleep | Bartel, Scheeren & Gradisar 2019† (secondary: a thesis from the same lab; abstract not retrieved) |
| MCII (mental contrasting + if-then plans) for bedtime | Raised commitment and cut nightly bedtime-procrastination minutes in **two** RCTs (N = 383 and 221, undergraduates, self-report) | Valshtein, Oettingen & Gollwitzer 2020† (B) |
| Whole-family screen limit (≤3 h/week for 2 weeks) | Children moved 46 min/day more. But **no difference in EEG-measured sleep** | Pedersen et al. 2022† *JAMA Pediatrics* (cluster RCT, 89 families, B). A useful null: cutting screens doesn't automatically fix sleep |
| Blue-light filter | No effect (see above) | Duraccio 2021† |

## 6. Risks: orthosomnia and how to show the data

- **The case reports.** Three patients became preoccupied with tracker data. They spent more time
  in bed to improve their scores, which deepens insomnia; one had a tracker showing poor sleep
  while PSG showed plenty of deep sleep (Baron et al. 2017†, C).
- **Feedback alone moves how you feel.** Sham "you slept badly" feedback worsened next-day
  alertness (d = 0.79) and sleepiness (d = 0.55) in insomnia (Gavriloff 2018†, B). The reverse
  also happens: people told they'd had good sleep scored better on cognitive tests (Draganich &
  Erdal 2014†; details from memory).
- **But tracking with good guidance can help.** In 113 people with insomnia symptoms, 5 weeks of
  wearable feedback *plus guidance on how to interpret it* lowered insomnia severity more than
  sleep education did (d = 0.51), without changing the gap between felt and measured sleep (Spina
  et al. 2023†, RCT, B). The guidance is the active part, not the gadget.
- **Users already distrust and misread sleep data** (Ravichandran et al. 2017†, CHI; content from
  memory: people misinterpreted device output and anxiety was a theme).

**How to show it** (design rules derived from the above):
1. **No nightly score and no stages.** The phone can't measure them (§1–2), and false precision
   is what drives orthosomnia.
2. **Name the thing measured.** "Phone down 01:12 → up 09:20", not "slept 8 h 8 min".
3. **Weekly, not nightly.** Regularity needs >1 week anyway (Fischer 2021). A single bad night
   doesn't move a weekly number much.
4. **Compare with your own past, never with norms or other people.** Fits P37 and D-019.
5. **Your rating comes first.** The morning check-in asks how you feel *before* showing any data,
   so the data can't bias the answer (Gavriloff 2018).
6. **Never suggest more time in bed to fix a number.** Time-in-bed scores reward exactly what
   Baron 2017 warned against.
7. **Escalate to a person, not a metric.** If you report loud snoring with gasping or daytime
   sleepiness, the app says "worth checking with a doctor", not a score.

## Principles

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P42 | **A phone measures phone-free time, not sleep** | Marino 2013† (A–B): even actigraphy finds only 33% of wake. Bhat 2015†, Toon 2016†, Fino 2020† (C–B): mattress and mic apps are worse. Borger 2019† (B): touches mark sleep boundaries well (R² = 0.9 vs. actigraphy) but stay common ~2 h around them. Saeb 2017† (B): ~40 min errors from phone sensors | Call it the **phone-down window**. Show its edges (last use, first use) and interruptions. Never call it "sleep duration" |
| P43 | **Timing and regularity are what's worth measuring, and a phone can measure them** | Windred 2024† (A): regularity beat duration for mortality. Phillips 2017† (B), Lunsford-Avery 2018† (B), Sletten 2023† consensus. Fischer 2021† (B): >1 week for unbiased SD/IS | Weekly regularity (SRI on the phone-down window, plus the spread of wake times), mid-sleep, and social jetlag. They're also the measures that show whether the sleep shift (S5) is working |
| P44 | **Don't show stages or cycles** | Fino 2020† (no app detected REM); Bhat 2015† (light/deep r ≈ 0); Schyvens 2025† (even wrist wearables κ = 0.21–0.53); Ohayon 2017† (no consensus that architecture indicates quality) | No hypnogram, no "deep sleep" minutes, no "cycles" graph |
| P45 | **How you felt is the quality measure; ask it before showing data** | Consensus Sleep Diary (Carney 2012†); PSQI (Buysse 1989†); NSF quality consensus (Ohayon 2017†); Lauderdale 2008† (B): self-report overestimates duration, so ask about feeling, not hours | A 2-tap morning check-in (rested 1–5, rough time to fall asleep), asked before the morning receipt (F61) shows any numbers |
| P46 | **The phone costs sleep mainly by pushing bedtime later and keeping you engaged; light is a small part** | Brautsch 2023† (A, association); Scott & Woods 2018† (C, FoMO → later bedtime + arousal); Duraccio 2021† (B, Night Shift null); Hartstein 2024† (no consensus on light) | Target *when* the phone goes down and *what* is open late (feeds), as F03 already does. Don't spend effort on blue-light filters |
| P47 | **Bedtime procrastination responds to plans and simple limits** | Valshtein 2020† (B, 2 RCTs, MCII); He 2020† (C–B, 30 min no phone); Bartel 2019† (secondary); Hill 2022† (A–B, self-control and evening chronotype correlate) | An if-then plan for the moment you'd normally keep scrolling (P9), plus the wind-down lock. Expect a modest effect. Your evening chronotype makes this harder, so the sleep shift and the plan go together |
| P48 | **Sleep feedback can harm or help; guidance and framing decide which** | Gavriloff 2018† (B, harm from negative feedback); Baron 2017† (C); Spina 2023† (B, benefit with guidance) | The six display rules in §6. Each card explains what the number can and can't tell you |
| P49 | **Skip the smart alarm; a regular wake time does the job** | Campanella 2024† (C, null overall, industry-funded); Hilditch & McHill 2019† (review); sensors can't find light sleep (P44) | A fixed wake time from the sleep plan, plus morning light (P20). No "wake in light sleep" feature |

## What this means for Mindful Hub

### Signals we could collect on Android

| Signal | Source | Status | Use | Cost and risk |
|---|---|---|---|---|
| **Screen off, unlock, app foreground/background** | `UsageStatsManager.queryEvents` | ✅ Already imported: `SCREEN_OFF`, `UNLOCK`, `FOREGROUND`, `BACKGROUND` ([UsageStatsImporter.kt](../../data/src/main/kotlin/io/github/dangquanghuy277/mindfulhub/data/UsageStatsImporter.kt)) | Last use before the night, first use after it, night interruptions, and which apps ran late | Free. **Gap:** `SCREEN_INTERACTIVE` (screen on) isn't mapped yet. A screen that lights up for a notification without an unlock is invisible today, which is fine for "awake and using", but worth adding so night interruptions can be measured |
| **Charger plugged in/out** | `ACTION_POWER_CONNECTED` / battery status | Not collected | S1 asks you to charge the phone away from the bed. Plugged in near bedtime and unplugged in the morning makes a strong "phone was parked" signal | Small. It's only a proxy: it can't tell *where* the charger is |
| **Accelerometer** | `SensorManager`, phone on the mattress | Not collected | Movement through the night | Needs an all-night foreground service (and, on Samsung, the battery exemption N6 already covers). Evidence says it adds little (P42) and it requires the phone *in* the bed, which contradicts S1 |
| **Microphone** | `AudioRecord`, on-device classification | Not collected | Snoring and sleep-talk counts; a rough sleep/wake | Needs a microphone foreground service with a persistent notification (from memory; check current Android rules). Very sensitive data, even processed on-device. Also needs the phone near the bed. Evidence for stages is weak (κ ≈ 0.49) |
| **Self-report** | Morning check-in, weekly review | Weekly review + WHO-5 approved (F11 + F12) | Felt quality, rough latency, awakenings, "did I use the phone in bed?" | The best-evidenced quality measure (P45). Costs you ~5 s a day |

### Metrics: show, show with care, don't show

| Metric | Verdict | Why |
|---|---|---|
| Last phone use, first phone use, phone-down window | **Show** | Directly measured, honest label (P42). It's the G7 metric's natural partner |
| Minutes on the phone after planned bedtime | **Show** (already G7) | Directly measured |
| Night interruptions (unlocks between last and first use) | **Show** | Directly measured |
| Weekly regularity: SRI on the phone-down window, wake-time spread | **Show weekly** | Strongest evidence (P43). Label it as phone-based |
| Mid-sleep and social jetlag | **Show weekly** | Tracks the sleep shift; MCTQ-style measures validated against melatonin (Ghotbi 2020) |
| Your morning rating, trend | **Show weekly** | P45 |
| "Sleep duration" | **Show only as "phone-down window"** | Not measurable from a phone (P42) |
| Sleep latency, WASO, efficiency | **Only as self-reported** | Needs a real sleep-onset time we don't have |
| Stages, cycles, a nightly score | **Don't show** | P44, P48 |

### Feature options

Each one is self-contained behind its own Settings switch, as the project requires.

- **F64 Phone-down night log** (passive): each morning, compute last use, first use, the longest
  screen-off stretch and night interruptions from the events already imported, plus
  `SCREEN_INTERACTIVE` and charger events. No new permission. (P42)
- **F65 Morning check-in, 2 taps**: "How rested? 1–5" and "Time to fall asleep: <15 / 15–30 /
  30–60 / >60 min", from the Consensus Sleep Diary. It shows before F61's receipt, so the data
  can't bias your rating. Skippable, with no reminder if you skip. (P45, P48)
- **F66 Weekly sleep-timing card** in the weekly review: SRI on the phone-down window, the spread
  of wake times, mid-sleep against the sleep-plan target, and social jetlag. Compared only with
  your own previous weeks. (P43)
- **F67 (ours) Bed-scroll experiment**: answers your F03 note, "sometimes you scroll to fall
  asleep". Alternate weeks with the optional bed scroll allowed and not allowed (F23-style A/B).
  Compare the phone-down time, F65's latency and rating, and next-day WHO-5. Your own data
  decides, because the literature can't (§5).
- **F68 Bedtime if-then plan (MCII)**: a one-time guided exercise. Picture the good morning, name
  the obstacle ("one more video"), then write the if-then ("If I reach for YouTube after 00:30,
  then I put the phone on the charger and open the book"). It's shown in wind-down. (P47, P9)
- **F69 Mattress accelerometer log** (optional): a movement trace through the night. (P42 says
  it adds little.)
- **F70 On-device sleep sounds** (optional): counts of snoring and talking, with no audio stored.
- **F71 Smart wake window**: the alarm rings in "light sleep" up to 30 min early. (P49 says don't.)

Weights are **proposed** (you haven't set them): your goals put sleep timing first (REQUIREMENTS
S5, G7), and the project rules put research evidence and not becoming a new hook first. A
must-pass gate: **no raw audio or movement data leaves the phone** (all options pass if built
on-device).

| Option | Evidence it works (×3): grades above | Fits your problem: 02:00 bedtime, FB after midnight (×3) | Low risk: orthosomnia, privacy, battery (×2) | Cheap to build (×1) | **Weighted total** |
|---|---|---|---|---|---|
| F64 Phone-down log | 4/5 phone-use signals validated vs. actigraphy (B) | 5/5 measures exactly the late use | 4/5 no new data; label must stay honest | 5/5 events already imported | **40/45 (89%)** |
| F65 Morning check-in | 5/5 the clinical diary standard | 4/5 gives the "how did it feel" the shift needs | 4/5 asked before data; 1 tap of effort | 5/5 one card | **40/45 (89%)** |
| F68 Bedtime MCII plan | 4/5 two RCTs, plus if-then d = .65 (P9) | 4/5 targets "one more video" | 5/5 nothing measured | 4/5 one guided screen + wind-down text | **38/45 (84%)** |
| F66 Weekly timing card | 4/5 regularity strongly linked (observational A) | 5/5 shows whether the shift works | 3/5 a number about sleep; weekly framing limits harm | 4/5 SRI + mid-sleep math in `:core` | **37/45 (82%)** |
| F67 Bed-scroll experiment | 3/5 no direct literature; the method is n-of-1 (P13) | 5/5 your own open question | 4/5 time-boxed, your choice | 4/5 reuses F03's bed scroll + F23 | **36/45 (80%)** |
| F71 Smart wake window | 1/5 no validated trial; sensors can't find light sleep | 2/5 wake time is set by the plan anyway | 4/5 harmless | 3/5 needs the accelerometer service | **20/45 (44%)** |
| F69 Mattress accelerometer | 2/5 specificity ≤50% | 2/5 needs the phone *in* bed, against S1 | 3/5 battery; looks more precise than it is | 2/5 all-night service | **20/45 (44%)** |
| F70 Sleep sounds | 2/5 events OK, stages weak (κ ≈ 0.49) | 1/5 snoring isn't your problem | 1/5 microphone all night | 1/5 an on-device audio model | **12/45 (27%)** |

**What the table shows.** The cheap options win: F64, F65, F68 and F66 use data you already have
or a few taps, and they target timing, which is your actual problem. Everything that needs a new
sensor scores below 50%, because the evidence says those sensors don't add accurate information
and they need the phone in the bed. **Recommendation:** F64 + F65 first (they produce the data
everything else needs), then F66 in the weekly review and F68 in wind-down, with F67 run as the
first experiment once 2 weeks of baseline exist. The ranking is stable: even giving "cheap to
build" zero weight, the sensor options stay last. Only if you suspected sleep apnea would F70 make
sense, and then the right step is a doctor and a home sleep test, not an app.

### What to log for the n-of-1 study

Following the project's exposure → action → outcome → live config → gaps rule:

| What | Fields | Why |
|---|---|---|
| **Night facts** (one row per logical night) | night date, last use (ts, pkg), first use (ts, pkg), longest screen-off start/end, interruptions (count, minutes, pkgs), charger in/out ts, minutes after planned bedtime | Store the raw edges, not just derived numbers, so SRI or a better algorithm can be recomputed later |
| **Algorithm version** | which window rule produced the row | Changing the rule must not silently change history |
| **Exposure** | when a sleep card or receipt was shown, which one, and for how long | So "no change" can be told apart from "never saw it", and so we can test whether *seeing* sleep data changes ratings (P48) |
| **Check-in** | asked ts, answered ts or skipped, rested 1–5, latency bucket, "phone in bed?" (y/n) | Asked-and-skipped is data too |
| **Live config per night** | planned bedtime and wake (sleep-quest chapter, held days), wind-down minutes, bed scroll allowed (y/n), which sleep features were on, F67 arm | Settings are already versioned by logical day; the experiment arm has to be too |
| **Gaps** | phone off or rebooted (`DEVICE_SHUTDOWN`/`DEVICE_STARTUP` events), no events at all, travel or a timezone change, a night you marked "not a normal night" | A gap must never read as a long, perfect night. Saeb 2017 found missing data twice as common during sleep |
| **Outcomes** | next-day WHO-5 (weekly), worth-it ratings and unintended minutes the next day, first-feed time after waking (G7) | Links the night to the day, which is the point of sleeping better |

### Open questions for you

1. Which days are work days? Social jetlag needs that tag (one-off setting).
2. Does the phone charge in the bedroom? It decides how much the charger signal means.
3. Do you want the sound and movement options at all, even scored low? If not, we drop F69–F71.
