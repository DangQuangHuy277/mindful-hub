# Research: energy, food and focus

Status: **proposed**, 2026-10-23. Nothing here is decided yet. It extends the sleep research
([sleep/RESEARCH.md](../sleep/RESEARCH.md)) from the night into the day, and asks: what makes you
sharp at work and rested after it, which of those things can the phone honestly measure, and how do
we collect the data first, so that anything built later rests on it?

Sources marked † were checked online on 2026-10-23: the DOI resolves on Crossref to the right
authors, venue and year, and the numbers quoted here were read in the abstract or full text.
"† (secondary)" means the paper exists, but the number comes from a summary, not the paper itself.
"† (exists)" means the paper was found, but what's said about it here is from memory. Unmarked
statements are from memory. Check them before you rely on them. The reference list, with DOIs, is
in [SOURCES.md](SOURCES.md). Evidence grades (A/B/C) are the ones defined in
[RESEARCH.md](../RESEARCH.md).

New principles start at **P50**. New feature ideas start at **F73**.

**A note on goal G5.** REQUIREMENTS says "a balanced life, not a max-productivity one". This doc
reads "most productive" as *sharp when work needs it, and properly rested after it*, not "more work
hours". Everything below measures energy and recovery, never output per hour. If you want the other
meaning, say so: it would change what we measure.

## Read this first: what tracking energy and food does *not* promise

- **You can't feel sleep debt building up.** On 6 h a night for 14 days, people's attention lapses
  kept getting worse until they matched **2 nights of no sleep at all**. Their sleepiness ratings
  rose in the first days, then barely moved, and couldn't tell 6 h from 4 h (Van Dongen et al.
  2003†, 48 adults, lab, B). So "I feel fine" is not evidence. Any energy tracking needs at least
  one objective measure next to the ratings.
- **Most energy boosts are small, and some are illusions.** For people who drink coffee every day,
  the "boost" is mostly the end of a small withdrawal (James & Rogers 2005†, review; contested).
  Sugar gave no mood benefit at any time point, and made people *more* tired and less alert in the
  first hour (Mantantzis et al. 2019† (secondary), 31 studies, 1,259 people, A). Micro-breaks raise
  vigour (d = .36) but don't measurably raise performance (d = .16, not significant) (Albulescu et
  al. 2022†, A).
- **Tracking too much kills tracking.** The most common mistakes of experienced self-trackers were
  tracking too many things, not tracking the context, and weak rigour (Choe et al. 2014†, 52 talks,
  C). Food journals are the hardest kind to keep (Cordeiro et al. 2015†, 141 journalers and 5,526
  forum posts, C). Even paid research participants answer only **79%** of prompts (Wrzus &
  Neubauer 2022†, 477 studies, A).
- **Counting calories can do harm.** Of 105 people with an eating disorder, about 75% had used
  MyFitnessPal and **73%** of those said it contributed to their disorder (Levinson et al. 2017†
  (secondary), C). Nearly 40% of male users in a fitness sample said the same (Linardon & Messer
  2019†, 122 men, C). This doesn't mean it would harm you, but nothing here needs calories.
- **Your own correlations are weak evidence.** With one data point a day, finding a correlation of
  r = .3 reliably takes about **85 days**; comparing two conditions in 3-week blocks finds only
  large effects (§7). Experiments decide; patterns only suggest.

**What this means for the app:** decide the questions first, then measure few things, mostly as
timestamps, with the same scales every day. Add one short objective test. Then run experiments,
one at a time.

## 0. The questions the data must answer

The first stage of personal tracking is deciding what you want to know, and its problems cascade
into every later stage (Li, Dey & Forlizzi 2010†, C). These are proposed; change them and the
measurement plan changes:

| ID | Question | What it needs |
|---|---|---|
| Q1 | When in the day am I sharpest, and does that move as the sleep shift moves my wake time? | Energy ratings at several times + wake time (F64) + an objective probe |
| Q2 | What does last night cost or buy me the next day? | F64/F65 night data joined to the next day's ratings |
| Q3 | Do caffeine, alcohol or late meals move my phone-down time or my rested rating? | Timestamps of drinks and meals + F64/F65 |
| Q4 | Which breaks bring the afternoon back? | Break events + the afternoon and end-of-day ratings |
| Q5 | Can I switch off after work? | An evening detachment rating + late work-app use |

## 1. What "energy", "sharp" and "rested" mean, and how each is measured

| Construct | Instrument | Evidence | Cost to you |
|---|---|---|---|
| **Sleepiness / alertness** | **Karolinska Sleepiness Scale (KSS)**, 1 (extremely alert) to 9 (very sleepy, fighting sleep) | Within-person correlations with EEG/EOG signs of sleepiness were very high, but the body signs only appeared near the sleepy end of the scale (Åkerstedt & Gillberg 1990†, 8 people, lab, B). Validated against performance and EEG (Kaida et al. 2006† (exists)) | 1 tap |
| **Energy / vitality** | Single "how much energy?" item, or the Subjective Vitality Scale | Vitality varied day to day with physical symptoms and well-being across 6 studies (Ryan & Frederick 1997†, B). It's the positive side; KSS is the tired side | 1 tap |
| **Mood** | Valence 1–5, or the Affect Grid (pleasure × arousal in one tap) | The Affect Grid had adequate reliability and validity in 4 studies (Russell et al. 1989†, B). Caveat: "high arousal" mixes energetic and tense, so stress can look like energy (from memory, Thayer's model) | 1 tap |
| **Objective alertness** | **PVT-B**: a 3-minute reaction-time test, lapses = responses > 355 ms | Tracked the standard 10-min test through total and partial sleep loss with medium-to-large effects (Basner, Mollicone & Dinges 2011†, 74 people, B). 3-min smartphone and tablet versions exist for sleep-loss testing (Grant et al. 2017† (exists)); a later study found the 3-min test only moderately correlated with the 10-min one (Antler 2022, secondary) | 3 min |
| **Thinking speed and working memory** | Short phone tasks repeated during the day | Averages were as reliable as lab tests (≥ .97), and the moment-to-moment variation was itself reliable (.41–.53) (Sliwinski et al. 2016†, 219 adults, B) | 1–2 min |
| **Focus at work** | "How focused were you?" plus what you did | Information workers' focus peaked mid-afternoon, boredom in early afternoon (Mark et al. 2014†, 32 people, 5 days, logs + probes, C) | 1 tap |
| **Recovery after work** | Detachment: "Could you stop thinking about work?" (Recovery Experience Questionnaire) | Detachment went with less exhaustion and fatigue, better sleep and better task performance, small to medium effects (Wendsche & Lohmann-Haislah 2017†, meta-analysis, 38,124 employees, A). Questionnaire: Sonnentag & Fritz 2007† (exists) | 1 tap |

**Single ratings are noisy.** In experience-sampling data, the error in one emotion rating was about
**a tenth of the scale**, around 27% of all the variation (Dejonckheere et al. 2022†, B). One low
rating means little; the average of a week means something. That's another reason to show weekly,
not daily (P48).

## 2. The shape of the day

- **Two clocks run your day.** The longer you're awake, the more sleep pressure builds. Lapses grew
  almost linearly with time awake beyond **15.8 h** (Van Dongen 2003†). Separately, the body clock
  makes alertness rise and fall by clock time (Schmidt et al. 2007†, review). So every rating needs
  both: hours since phone-up (F64) and clock time.
- **Your chronotype decides your peak.** Late types (like you, with a ~02:00 bedtime) were worse
  than early types *in the morning* on sleepiness, reaction time, executive function and even grip
  strength, measured at 08:00, 14:00 and 20:00 (Facer-Childs et al. 2018†, 56 people, B). While the
  sleep shift is moving your wake time earlier, expect mornings to be your weakest part, and expect
  that to improve.
- **The afternoon dip is partly clock, not lunch.** There's a dip in the mid-afternoon linked to a
  rise in sleep propensity at that time (Monk 2005† (secondary), review). Food can deepen it (see
  §3, sugar), but skipping lunch won't remove it.
- **Implication:** sample energy at fixed points relative to your day (before lunch, mid-afternoon,
  end of work), and read the results as a curve over hours since waking.

## 3. Food and drink: what's worth logging

| What | Evidence | Verdict |
|---|---|---|
| **Caffeine: time and size** | 400 mg taken 6 h before bed still cut objectively measured sleep by more than 1 hour, while the sleep diary showed only 41 min (not significant): you don't notice (Drake et al. 2013†, 12 people, B). Across 24 studies, caffeine cut total sleep by 45 min; a cup of coffee (~107 mg) should come **≥ 8.8 h before bed** (Gardiner et al. 2023†, A). Daily drinkers mostly get back what overnight withdrawal took (James & Rogers 2005†) | **Log**: one tap with size. The last-cup time vs. planned bedtime is the useful number |
| **Alcohol: time and drinks** | Speeds falling asleep but disrupts the second half of the night at all doses (Ebrahim et al. 2013†, review, A). In 4,098 employees, compared with their own sober nights, HRV-based recovery in the first 3 h of sleep fell by **9, 24 and 39 percentage points** for low, moderate and high doses (Pietilä et al. 2018†, B, observational) | **Log**: one tap per drink |
| **Sugary snacks and drinks** | More fatigue and less alertness in the first hour, no mood gain (Mantantzis 2019† (secondary), A) | **Log lightly**: one tap, no amounts. Useful for the afternoon |
| **Meal timing (first and last bite)** | People logging every meal and drink on a phone app ate erratically through the day; half of them ate over a span of more than **14.75 h**, with under 25% of calories before noon. Overweight people who cut it to 10–11 h for 16 weeks lost weight, said they had more energy and slept better, and it lasted a year (Gill & Panda 2015†; B for the pattern, C for the effect: a small group with no control, size from memory: 8) | **Log**: the timestamp of each meal is the data |
| **Meal size** | Bigger midday meals are commonly linked to a deeper afternoon dip (Monk 2005, from memory). A high-glycaemic carb meal 4 h before bed shortened sleep onset (Afaghi et al. 2007† (exists)) | **Log lightly**: light / normal / heavy |
| **Water** | Losing ~1.6% of body mass to dehydration raised fatigue and tension and caused more vigilance errors (Ganio et al. 2011†, 26 men, B). Reviews call the evidence promising but methodologically weak (Masento et al. 2014†, review) | **Don't log** for now: it matters mostly in heat and exercise, and logging every glass is burden |
| **Diet quality** | A 12-week diet-improvement programme helped moderate-to-severe depression a lot (d = −1.16, remission 32% vs 8%) (Jacka et al. 2017† SMILES, 67 people, B) | **Don't track daily.** It's a long-term pattern, not a daily lever, and the study was in depression |
| **Calories and macros** | Burden (Cordeiro 2015†) and harm risk (Levinson 2017†, Linardon 2019†, Simpson & Mazzeo 2017† (exists)) | **Never** |

**How to log food without it becoming a chore** (from Cordeiro 2015† and Choe 2014†): the
*timestamp* is the main data; what and how much are one optional tap. Late entries are fine, but
they're stored as late ("logged at 15:10 for 12:30"), since recalled times are less accurate.
Photos only help memory and add burden, so skip them.

## 4. Movement, breaks and rest at work

| Finding | Source (strength) | What it means for you |
|---|---|---|
| Regular exercise raised energy and lowered fatigue (δ = .37), **but not in studies with a placebo control** | Puetz, O'Connor & Dishman 2006† (A) | Expect a real but modest effect, part of it expectation |
| A single bout of exercise improves thinking only slightly (g = .10), more for some tasks and doses | Chang et al. 2012† (A) | Don't expect a walk to make you smarter. It helps mood and energy more |
| Walking raised creative idea generation for 81% of people, during and shortly after | Oppezzo & Schwartz 2014† (4 experiments, B) | A walk is the right break before brainstorming, not before debugging |
| Micro-breaks (≤ 10 min) raised vigour (d = .36) and lowered fatigue (d = .35). Performance only rose for easy tasks; longer breaks helped performance more | Albulescu et al. 2022† (22 samples, N = 2,335, A) | Breaks are for energy. For hard work, a break needs to be more than 10 minutes |
| Afternoon short breaks raised that day's work engagement; morning breaks didn't. Breaks didn't make up for a bad night | Kühnel et al. 2016† (107 employees, 453 days, B) | Put breaks in the afternoon. Sleep comes first |
| A 15-min lunchtime park walk gave better afternoon concentration and less fatigue; relaxation exercises did too | Sianoja et al. 2018† (97 employees, 10 days, B) | A walk outside after the team lunch is a good default |
| A **10-min nap** improved alertness, mood and performance at once, for up to 155 min. A 30-min nap caused grogginess first | Brooks & Lack 2006† (24 adults, after a short night, B) | If you nap, 10 minutes. Only after a short night |
| The most common energy strategies at work (switching task, **browsing the internet**) weren't linked to more energy. Learning, meaning and good relationships were | Fritz, Lam & Spreitzer 2011† (C) | A phone break is a break that doesn't work. The gate already sees those |
| Using a smartphone for work late at night meant less sleep, more depletion the next morning and lower engagement that day | Lanaj, Johnson & Barnes 2014† (2 experience-sampling studies, B) | Rest after work includes the work apps on the phone |

## 5. What the phone can sense passively

- **Phone use by hour** (already imported) tells when you reach for the phone during work and how
  long the night was (F64). It doesn't tell how you feel.
- **Steps** come from the phone's own step counter, with no wearable. On Android it needs the
  physical-activity permission (from memory). Steps per hour are a proxy for breaks and walks.
- **Location and phone patterns** correlated with depression scores: regularity of movement r =
  −.63, phone use r = .54 (Saeb et al. 2015†, 28 people, C). StudentLife found similar links
  across a 10-week term (Wang et al. 2014†, 48 students, C). These are group-level signals from
  small studies. GPS is also the most sensitive data a phone has, so it isn't worth collecting here.
- **Heart rate and HRV** need a watch or ring. If you have one, Health Connect can share them
  (H2 below). HRV did respond to alcohol in Pietilä 2018†, so it would help with Q3.

## 6. How to collect: what self-tracking research says

| Lesson | Source (strength) | Rule for the app |
|---|---|---|
| Problems in one stage (collecting, combining, reflecting) cascade into the next | Li et al. 2010† (C) | Design collection *and* the weekly view together, from the start |
| Lapses are normal; people stop and restart | Epstein et al. 2015†, 2016† (C) | A missed day is a gap, not a failure. No streaks (P33, P41). Restarting is one tap |
| Track few things, and include the context | Choe 2014† (C) | 2 rating items, 5 event types, nothing else |
| Long questionnaires raised burden and lowered compliance and data quality. **Asking more often didn't** (3, 6 or 9 times a day) | Eisele et al. 2020† (163 students, 14 days, preregistered, B) | Short and frequent beats long and rare: ≤ 10 s per prompt |
| Average compliance 79%; more prompts per day didn't predict lower compliance | Wrzus & Neubauer 2022† (A) | Expect ~1 in 5 prompts missed. Log missed prompts, never fill them in |
| Being asked changes you a little: people got faster, their ratings varied less over time (habituation), and positive affect declined | Eisele et al. 2022† (151 students, B) | Keep the same questions for weeks; read the first few days as warm-up |
| Momentary questions beat end-of-day recall, which is biased | Shiffman, Stone & Hufford 2008† (review, A) | Ask in the moment. The end-of-day card asks only about the day as a whole |
| Paper diaries were often filled in later from memory; electronic ones with timestamps were not | Stone et al. 2003† (exists; from memory) | The timestamp of the answer is stored, not just the answer |

## 7. Analysis: what your own data can and can't tell you

My own calculation (normal approximation, two-sided α = .05, 80% power), assuming each day is an
independent data point:

| To find… | You need… |
|---|---|
| A correlation of r = .5 / .4 / .3 / .2 between two daily numbers | 30 / 47 / **85** / 194 days |
| A difference between two conditions (A vs. B days) of d = 1.0 / 0.8 / 0.5 | 16 / 25 / 63 days *per condition* |
| …with 14 / 21 / 28 days per condition | only effects of d ≥ 1.06 / 0.86 / 0.75 |

Days aren't independent: if today is like yesterday (autocorrelation ρ ≈ .3), each day counts as
about half a day, so these numbers roughly double. Also:

- **Reverse causation is everywhere.** You drink coffee *because* you're tired, so coffee will
  correlate with tiredness. Only an experiment (you change it on purpose) separates them.
- **Look at 20 pairs and one will "work" by chance.** Decide the question (Q1–Q5) before looking.
- **Experiments in daily life are messy.** TummyTrials found people could run reliable food
  self-experiments, but rigour kept clashing with real life (Karkar et al. 2017†, 15 patients, C).
  SleepCoacher's short guided experiments improved sleep as adherence rose (Daskalova et al.
  2016†, C). Kravitz & Duan 2022† (exists) describe how to run personalized (n-of-1) trials.
- **So:** run one experiment at a time, in blocks of ≥ 1 week, with the order decided in advance,
  the outcome picked before you start, and an honest note at the end that only large effects
  could show up. F67 (bed-scroll experiment) is already queued; don't overlap the two.

## 8. Risks, and how to show the data

The sleep display rules (sleep/RESEARCH.md §6) apply, plus:

1. **No daily energy score, no "readiness" number.** Single ratings are noisy (Dejonckheere 2022†)
   and scores invite chasing (P48).
2. **No calories, and no food labelled good or bad.** Food appears only as times and markers on
   your energy curve (Levinson 2017†).
3. **Rate before you see data**, as F65 does (P45).
4. **Weekly, compared with your own past.** Never with norms (P37).
5. **The hub must stay small** (G6, P39). Every prompt is ≤ 10 s, expires quietly, and is never
   repeated.
6. **Name what's measured.** "You rated 6/9 sleepy at 15:00", not "your energy was low".

## 9. Beyond the phone: a watch, your work tracker, and other sources

Added 2026-10-23, after you asked whether a device or your activity data could replace answering
questions.

### Can sensors replace the questions?

**Not for feelings.** Energy, mood and focus *are* how you feel, so a sensor can only guess them,
and the guesses are poor:

- In 271 people, models built on passive phone data predicted daily mood at **median R² ≈ 0**
  across the group. Built per person, they did better than chance for 81% of people, but strongly
  (AUC > .80) for only **12%** (Pratap et al. 2018†, B). GPS mobility was the most useful feature.
- A 2026 thesis with phones, smart rings and daily ratings found the same: the links between
  sensor data and feelings varied a lot between people, personal models beat group models, and
  **sleep and physical activity** were the most consistent signals (Berkemeier 2026, secondary).
- Even a personal model needs your ratings to learn from. So the two taps stay. What sensors can
  do is replace the questions about **behaviour and the body**: when you slept, how much you
  moved, how your heart recovered at night.

**Vendor energy scores are opaque.** Samsung's Energy Score combines activity, sleep, and heart rate
and HRV during sleep. Its validation is a Samsung–University of Georgia collaboration that found
"a significant correlation" with cognitive tests and self-reports; I found no independent
peer-reviewed check or published formula (secondary, Samsung and UGA press pages). Store it if you
get one, and test it against your own ratings, but don't make it the outcome (P48).

### What a watch would add

| Signal | How good | Answers |
|---|---|---|
| **Sleep timing and duration** (actually asleep, not phone-down) | Six devices in 53 adults agreed with PSG on sleep vs. wake 86–89% of the time (κ = .30–.51): **valid for timing and duration**, not for stages (Miller, Sargent & Roach 2022†, B). Wake detection is still weak (sleep/RESEARCH.md §2) | Q2, and fixes P42's "phone-down isn't asleep" |
| **Resting heart rate and HRV at night** | WHOOP's HR matched ECG well; its HRV error came close to the smallest change worth noticing, so small day-to-day changes can be noise (Bellenger et al. 2021†, B). Cheap bands are less validated (from memory) | Q2, Q3 (alcohol lowered night-time HRV recovery in Pietilä 2018†) |
| **Steps and workouts** | The phone already counts steps if it's in your pocket (F77). A watch also counts when it isn't | Q4 |

Things to know before buying (not verified for any specific cheap model):

- **Wear it at night or don't bother.** Sleep and night HRV are most of the value, so a band
  you'll charge in the morning and sleep with beats a better watch that charges overnight.
- **Choose by data access, not features.** The data has to reach Mindful Hub on the phone. Two
  routes: **Health Connect** (Samsung Health writes steps, sleep and heart rate; a developer-forum
  report says not all heart-rate readings arrive, and I couldn't confirm HRV for the Galaxy Fit3),
  or **Gadgetbridge**, an open-source app that talks to many bands (all Mi Bands, some Amazfit)
  without the vendor's cloud and keeps the data on the phone. Check the exact model on the
  Gadgetbridge device list, or check Health Connect's permissions screen after pairing.
- **If you buy one, buy it before the baseline starts**, so the 3 weeks of baseline include it.

### What your work time tracker can and can't tell you

It records time per task. That's **activity, not productivity**. Developer productivity can't be
captured by one metric; activity is one of five dimensions (with satisfaction, performance,
communication, and efficiency and flow) (Forsgren et al. 2021† SPACE, C). What it *can* add:

- **When work starts and stops**, and work after hours (Q5; Lanaj 2014†).
- **Fragmentation**: how often you switch tasks. Developers switched activity every 0.3–2 minutes;
  more user input went with feeling productive, and email, planned meetings and non-work sites
  with feeling less so. Developers fell roughly into **morning, low-at-lunch and afternoon
  people**, found by pairing the logs with self-ratings (Meyer et al. 2017†, 20 developers, 11
  days, C). That's Q1, and it needed both the logs and the ratings.
- **Long blocks on one task** (say ≥ 60 min) as a rough "deep work" count, to compare with your
  focus rating.

**Import times and a category only, never task titles.** Task titles can name customers, and
customer data must not leave the work systems. Your employer's rules for exporting the tracker's
data come first. Open question: is the tracker automatic or manual, and can it export a file?

### Other ways to measure yourself

| Source | What it gives | Evidence (strength) |
|---|---|---|
| **Phone camera HRV**, 1 min after waking (fingertip on the camera) | HRV without a watch. $0 | Smartphone camera rMSSD at rest agreed with ECG (CV 6.4%, "trivial" differences) (Plews et al. 2017†, 29 adults, B; one author makes the HRV4Training app). Lying still, same time each day. Building our own reliable version is real work |
| **Typing and click speed** on your computer (timings only, never the keys) | A passive "sharpness" signal | Across 3 million nights, keystroke and click speed in web search tracked time of day, chronotype and prior sleep; 2 nights under 6 h went with lower performance for **6 days** (Althoff et al. 2017†, B). A timing logger on a work laptop needs your employer's OK |
| **Calendar meeting times** (times only) | Meeting load as context | Planned meetings went with lower perceived productivity (Meyer 2017†) |
| **Continuous glucose monitor**, 2 weeks | Glucose after each meal | Even people with normal glucose spent 15% of the time in prediabetic ranges (Hall et al. 2018†, B), but nobody has shown that this explains how energetic people feel. It costs money and only answers part of Q3 |
| **Blood tests** (iron, vitamin D, thyroid) | Rules out medical causes | If tiredness persists for weeks, that's a doctor's question, not an app's |

### Comparing the sources

Weights are **proposed**: what it answers among Q1–Q5 (×3) and how valid it is (×3) matter most;
your daily effort (×2) and cost plus whether Mindful Hub can read it locally (×2) come next. Max 50.
The must-pass gate stays: **data stays on your devices** (N1).

| Source | Answers Q1–Q5 (×3) | Validity (×3) | Low daily effort (×2) | Cost and access (×2) | **Weighted total** |
|---|---|---|---|---|---|
| Galaxy Watch (mid-price) | 4/5 sleep, night HR/HRV, activity: Q2, Q3, Q4 | 4/5 sleep timing valid; HR good | 3/5 charging competes with night wear | 3/5 more expensive; Health Connect gaps | **36/50 (72%)** |
| Cheap band (Mi Band, Galaxy Fit) | 4/5 same signals | 3/5 sleep timing likely fine; HRV less validated | 4/5 a week's battery, worn at night | 3/5 cheap, but data path to check per model | **35/50 (70%)** |
| Phone camera HRV | 3/5 night recovery only: Q2, Q3 | 4/5 validated at rest | 3/5 1 min every morning | 4/5 $0, but hard to build well | **35/50 (70%)** |
| Typing/click speed on the PC | 4/5 passive sharpness: Q1, Q2 | 3/5 strong at population scale, noisy per person | 5/5 passive | 1/5 a timing logger on a work laptop; employer policy | **33/50 (66%)** |
| Calendar meeting times | 2/5 context for Q1 | 3/5 linked to perceived productivity | 5/5 passive | 4/5 one permission, times only | **33/50 (66%)** |
| Work tracker export (times only) | 3/5 Q1 context, Q5 late work | 3/5 accurate times, but activity ≠ productivity | 5/5 you already do it | 2/5 export unknown; customer names must be stripped | **32/50 (64%)** |
| Passive mood prediction, no questions | 1/5 can't answer how you feel | 1/5 median R² ≈ 0; strong for 12% of people | 5/5 nothing to do | 3/5 still needs your ratings to learn | **22/50 (44%)** |
| Continuous glucose monitor | 2/5 food only, part of Q3 | 2/5 glucose is accurate; its link to energy is unproven | 3/5 a sensor on your arm + meal logging | 1/5 costs money; data access varies | **20/50 (40%)** |

**What the table shows.** No source replaces the two taps: the one that tries to (passive mood
prediction) comes near the bottom. The wearables and the camera HRV win because they add **sleep
timing and night recovery**, the two things the phone measures worst, and those drive Q2 and Q3.
The watch and the band are within one point of each other, so the choice is about price and
whether you'll wear it at night. If daily effort got ×3, they'd tie at 39/55. The phone camera
scores the same as the band at the proposed weights, but asks for a minute every morning and
doesn't see the night itself. The work tracker and calendar
are cheap context, worth adding if export is easy.

## 10. The morning reaction test: what backs it

Added 2026-10-23, after you picked the morning and asked for the evidence.

| Claim | Evidence (strength) |
|---|---|
| **The test detects lost sleep** | The 3-min PVT-B tracked the 10-min PVT through total and partial sleep loss, with medium-to-large effects (Basner, Mollicone & Dinges 2011†, 74 people, B). Response speed was the most sensitive measure (Basner & Dinges 2011† (exists), *Sleep*; its introduction reports test-retest ICCs above .8 for lapses and little learning, secondary) |
| **It catches what you can't feel** | On 6 h a night, lapses kept rising for 2 weeks while sleepiness ratings flattened (Van Dongen 2003†, B) |
| **The morning is where sleep debt shows most** | After weeks of 5.6 h a night, performance within 2 min of waking was **~10% worse** than on 8 h, and stayed worse through the next 70 min. Sleepiness ratings didn't differ between the groups. Waking during the biological night made it worse (McHill et al. 2019†, 26 people, lab, B) |
| **Grogginess after waking fades slowly, so test at the same point every morning** | After a full 8 h, sleep inertia took **2–4 h** to nearly clear; the time constant was 1.17 h for thinking speed and 0.67 h for felt alertness. Getting up, eating, showering and room light didn't speed it up (Jewett et al. 1999†, B). Review: Hilditch & McHill 2019† (exists) |
| **It works outside a lab** | In police recruits wearing actigraphy for a week, each extra hour of sleep the night before lowered the chance of a lapse on a handheld 5-min PVT by about 3.5% (Neylan et al. 2010†, secondary). A touchscreen PVT responded to staying awake the same way as the lab device (Arsintescu et al. 2017†, small lab study, secondary) |
| **Late types are slower in the morning anyway** | Facer-Childs 2018† (§2). So compare each morning only with your own mornings, never with norms or with your afternoons |

**What's not studied:**

- **No study runs this exact setup**: one person, a daily 3-min phone test at home, used to track their own sleep. The parts are validated; the whole is our n-of-1 design (P13).
- **Nobody has tested whether a game look changes the result.** Keeping the timing rules identical to PVT-B (a random 1–4 s from each response to the next star, including 1 s showing the reaction time; lapse = 355 ms or more; a guess under 100 ms is a false start; 3 min (checked in the paper's methods)) is what lets the evidence above still apply.
- **Your score will mix grogginess and sleep debt.** That's fine for tracking, since McHill 2019 shows the debt still shows up on top of the grogginess, but only if the test happens at the same delay after the first unlock every day. Store that delay with every run.

### How long must it be? (2026-10-23)

You asked whether 3 minutes is needed. Shortening a fixed test costs quality; testing on fewer
mornings doesn't.

| Evidence | Source (strength) |
|---|---|
| Even 3 min costs something: vs. the 10-min PVT, effect sizes dropped by 22.7% on average (6.9–67.8%), judged an acceptable trade | Basner, Mollicone & Dinges 2011† (B) |
| Speed measures hold up in shorter tests; lapse measures need the longer ones | Basner & Dinges 2011† (exists; via a search summary) |
| The first 2 min of the 10-min PVT still showed impairment over a night awake on mean RT, fastest and slowest 10%, but not on lapses | Loh et al. 2004† (secondary) |
| An adaptive 3-min test (PVT-BA) stops once the result is clear: **1 min 43 s** on average (17 s to 3 min), agreeing with the full 3 min at κ = .85 (43 people, sleep restriction). It outputs three grades (high/medium/low lapses + false starts), and early stops can miss a late decline | Basner 2022† (B); the 10-min version: Basner & Dinges 2012† (exists) |
| No fixed 1- or 2-minute phone PVT has been validated | (searched; none found) |

Options, weights **proposed** from your concern (annoyance ×3), validity ×3, precision for your own
trend ×2; max 40:

| Option | Validated (×3) | Precision for your trend (×2) | Low annoyance (×3) | **Total** |
|---|---|---|---|---|
| 3 min on 3 set mornings a week | 5/5 the validated test | 4/5 full runs, fewer of them | 4/5 9 min a week | **35/40 (88%)** |
| 3 min every morning | 5/5 | 5/5 most data | 2/5 3 min daily | **31/40 (78%)** |
| Adaptive PVT-BA every morning | 4/5 one study, close agreement | 3/5 three grades; normal mornings all read "high" | 4/5 ~1 min 43 s | **30/40 (75%)** |
| Fixed 2 min | 3/5 speed held up in Loh 2004 | 3/5 fewer trials | 4/5 | **27/40 (68%)** |
| Fixed 1 min | 2/5 not validated | 2/5 | 5/5 | **25/40 (63%)** |

**Your call (2026-10-23): 3 minutes every morning for a week, then decide by how it feels.** If it
annoys, the fallback that keeps quality is fewer mornings, not fewer minutes. Store the week's
abandoned runs and skipped offers (`reaction_run`, `reaction_offer`): they're the annoyance data.

## Principles

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P50 | **You can't feel sleep debt, so pair ratings with one objective test** | Van Dongen 2003† (B): lapses matched 2 nights without sleep while sleepiness ratings flattened. Åkerstedt 1990† (B): body signs only at the sleepy end. Basner 2011† (B): PVT-B works in 3 min | An optional 3-min reaction-time test, compared only with your own past |
| P51 | **Read energy on two clocks, hours since waking and clock time, and expect late types to be weakest in the morning** | Van Dongen 2003†; Schmidt 2007† (review); Facer-Childs 2018† (B); Monk 2005† (secondary); Mark 2014† (C) | Every rating is joined to that morning's phone-up time (F64) |
| P52 | **Short and frequent beats long and rare; keep the questions the same** | Eisele 2020† (B); Wrzus & Neubauer 2022† (A); Dejonckheere 2022† (B); Eisele 2022† (B); Shiffman 2008† (A) | ≤ 2 items per prompt, ≤ 3 prompts per workday, fixed wording for weeks; missed prompts logged |
| P53 | **Log when things happen, not what they contain** | Gill & Panda 2015† (B/C); Cordeiro 2015† (C); Choe 2014† (C) | One-tap events with automatic timestamps; size optional; late entries flagged |
| P54 | **For food, log only what plausibly moves energy and sleep: caffeine, alcohol, sugar, meal times. Never calories** | Drake 2013† (B); Gardiner 2023† (A); Pietilä 2018† (B); Ebrahim 2013† (A); Mantantzis 2019† (A); Levinson 2017†, Linardon 2019† (C) | Five event types; the last-caffeine time against the planned bedtime |
| P55 | **Breaks and movement raise energy more than output; take them in the afternoon** | Albulescu 2022† (A); Kühnel 2016† (B); Sianoja 2018† (B); Puetz 2006† (A); Chang 2012† (A); Brooks & Lack 2006† (B); Fritz 2011† (C) | An afternoon walk or 10-min rest in the Instead list; a phone break doesn't count as a break |
| P56 | **Rest after work means detachment, including from work on the phone** | Wendsche 2017† (A); Lanaj 2014† (B) | An evening "could you switch off?" item; work-app use after hours shown weekly |
| P57 | **Your correlations are hypotheses; experiments decide, and they take weeks** | §7 calculation; Karkar 2017† (C); Daskalova 2016† (C); Choe 2014† (C) | One experiment at a time, pre-planned blocks, outcome chosen in advance |
| P58 | **Sensors measure behaviour and the body, not feelings; keep the two taps** | Pratap 2018† (B): passive mood prediction median R² ≈ 0; Berkemeier 2026 (secondary); Miller 2022† (B): wearables valid for sleep timing | Devices replace questions about sleep and activity, never the ratings. A vendor score is stored and tested, not shown as the outcome |
| P59 | **Time on tasks is activity, not productivity** | Forsgren 2021† SPACE (C); Meyer 2017† (C) | Work-tracker data is context next to your focus rating; only times and a category are imported |

## What this means for Mindful Hub

### The measurement plan: five layers, about a minute a day

| Layer | What | When | Your effort | Status |
|---|---|---|---|---|
| **0. Passive** | Phone use by hour, phone-down night (F64), work-hours phone use (gate), **steps per hour** (new), charger | Always | 0 s | Steps are new; the rest exists |
| **1. Events** | One tap when it happens: ☕ caffeine (S/M/L), 🍺 drink, 🍽 meal (light/normal/heavy), 🍬 sweet, 😴 nap. 🚶 walks already exist in A5 | When it happens | ~2 s each, ~15 s a day | New (F74) |
| **2. Momentary** | KSS alertness + mood 1–5 | Before lunch (~11:30), mid-afternoon (~15:00), end of work (~19:00, merged with layer 4). Workdays only to start | ~5 s each | New (F73) |
| **3. Objective** | 3-min reaction-time test (PVT-B) | Once a day, same anchor (suggest mid-afternoon), optional | 3 min | New (F75), trial |
| **4. Day and week** | End of work: focus 1–5, "main thing done?" yes/partly/no. Evening: "could you switch off?" 1–5. Weekly: WHO-5 (exists) | Once a day / week | ~10 s a day | New (F76); WHO-5 exists |

Without the reaction test, that's under a minute a day. With it, about four.

### How to ask: comparing prompt schemes

- **S1 Fixed anchors**: a quiet notification at set points of your day, which disappears after 60 min.
- **S2 Random times**: one random moment inside each window, the classic experience-sampling design.
- **S3 At the gate**: ask the rating when you open a gated app, since you're already on the screen.
- **S4 Only when you want**: no prompts; you rate whenever you open the hub.

Weights are **proposed**: an unbiased sample matters most because every answer to Q1–Q4 depends on
it; annoyance, compliance and fitting the app's spirit (G6, less phone) come next.

| Scheme | Unbiased sample (×3): ratings must cover the day, not only certain moods | Low annoyance (×2) | Compliance (×2) | Fits the app (×2): G6, less phone | **Weighted total** |
|---|---|---|---|---|---|
| S1 Fixed anchors | 4/5 covers the day; fixed times may line up with habits | 3/5 up to 2 notifications a day | 4/5 predictable | 3/5 a notification is still a pull | **32/45 (71%)** |
| S2 Random | 5/5 the gold standard | 2/5 surprises are more annoying | 3/5 harder to keep up | 3/5 same pull | **31/45 (69%)** |
| S3 At the gate | 1/5 only when you're about to open a feed, and tiredness may drive those opens | 4/5 you're already there | 5/5 always seen | 4/5 no extra pull | **29/45 (64%)** |
| S4 Only when you want | 1/5 you rate when you remember, usually when it's extreme | 5/5 nothing | 1/5 lapses quickly | 5/5 nothing | **25/45 (56%)** |

**What the table shows.** Only the two schemes that pick the moment for you give a fair picture of
your day, and fixed anchors are easier to live with. **Recommendation: S1**, with the end-of-work
anchor merged into the F76 card so that only two notifications a day are new. S1 still wins if
annoyance gets ×3 (35/50 vs. S3's 33/50). S3 would win only if you decide the bias doesn't matter.

### Which energy question: comparing scales

| Scale | Validated (×3) | Fast (×2) | Picks up change (×2) | Captures more than sleepiness (×1) | **Weighted total** |
|---|---|---|---|---|---|
| **KSS + mood 1–5** (two taps) | 5/5 KSS is validated against EEG and performance | 3/5 two taps, one a list of 9 | 4/5 9 steps | 4/5 alertness and mood | **33/40 (83%)** |
| KSS alone | 5/5 | 3/5 a list of 9 | 4/5 | 2/5 only sleepiness | **31/40 (78%)** |
| Affect Grid (one tap) | 4/5 Russell 1989† | 4/5 one tap, needs learning | 4/5 9 × 9 | 3/5 arousal mixes energy with stress | **31/40 (78%)** |
| Energy 1–5 + mood 1–5 | 3/5 no single validated item | 5/5 two quick taps | 3/5 coarse | 4/5 | **29/40 (73%)** |

**Recommendation: KSS + mood.** KSS links straight to the sleep side and to most of the research
above. In the UI it reads "How alert are you right now?", with the standard labels on steps 1, 3,
5, 7 and 9; the stored number is the KSS score.

### Feature options

Each one is self-contained behind its own Settings switch, as the project requires.

- **F73 Energy check**: KSS + mood at fixed anchors (S1), two taps, expires after 60 min. (P51, P52)
- **F74 One-tap intake log**: caffeine, drink, meal, sweet, nap, from the hub and a quick-settings
  tile, so logging doesn't need the app opened. Late entries flagged. (P53, P54)
- **F75 Reaction-time check** (PVT-B, 3 min): optional, once a day, compared only with your own
  past. Raw reaction times are stored. (P50)
- **F76 End-of-work card**: focus 1–5 and "main thing done?". At night, "could you switch off?" in
  the wind-down. (P56)
- **F77 Steps per hour** from the phone's step counter. (P55)
- **F78 Weekly energy map** in the weekly review, after ≥ 2 weeks of data: your KSS curve by hours
  since waking, with caffeine and meal markers. No score. (P51)
- **F79 Caffeine cut-off line**: in the evening plan, "last coffee by HH:MM" (planned bedtime −
  8.8 h), and later caffeine shown in F64's morning card as a fact. Only useful if you drink coffee
  or tea. (P54)
- **F80 (ours) Daytime experiments**: F23's A/B weeks (and F67's setup), applied to daytime levers. Pick one change (caffeine cut-off,
  afternoon walk, 10-min nap, light lunch), get a pre-planned block schedule, see one result at the
  end with an honest "what this can show". (P57)
- **F81 Afternoon break nudge**: at the mid-afternoon anchor, F73's card offers "10-min walk
  outside" or "10-min rest" from the existing Instead list. (P55)
- **F82 Work-PC focus log** (ActivityWatch, open source, local): active-window and away time on
  your work computer, joined by time. Passive, but it's a second device and a work machine. (P56)
- **H2 Wearable reader** (Health Connect or a Gadgetbridge export): sleep timing, resting heart
  rate, night HRV, steps, and any vendor score as a stored column. Only if you get a watch. (§9, P58)
- **F83 Work-tracker import**: start, stop and duration per block, with a category you map once;
  task titles never leave the work systems. (§9, P59)
- **F84 Calendar meeting load**: meeting start and end times from the phone's calendar, no titles. (§9)
- **F85 Morning camera HRV**: 1 min, fingertip on the camera, only if there's no watch. (§9)
- **F86 Typing speed on the PC**: key and click timings only, never content, only with your
  employer's OK. Could be part of F82. (§9)
- **Out**: calorie or macro counting, photo food diary, GPS mobility, water logging.

Weights are **proposed**, the same as in the sleep doc: evidence ×3, fits your question (sharp at
work, rested after) ×3, low burden and risk ×2, cheap to build ×1; max 45. Must-pass gates: **data
stays on the phone** (N1) and **$0** (N3). All options pass if built on-device.

| Option | Evidence (×3) | Fits your question (×3) | Low burden and risk (×2) | Cheap (×1) | **Weighted total** |
|---|---|---|---|---|---|
| F79 Caffeine cut-off | 5/5 PSG study + meta-analysis | 4/5 bedtime is the core problem, *if* you drink coffee | 5/5 one line of text | 5/5 needs F74 only | **42/45 (93%)** |
| F73 Energy check | 4/5 the standard EMA method; KSS validated | 5/5 it is the outcome everything else needs | 3/5 2 notifications a day; reactivity | 4/5 one card + scheduling | **37/45 (82%)** |
| F74 Intake log | 4/5 caffeine and alcohol effects are strong | 4/5 answers Q3 | 4/5 taps only; you'll forget some | 5/5 one table + buttons | **37/45 (82%)** |
| F76 End-of-work card | 3/5 diary studies; self-rated focus isn't checked against output | 5/5 the "work" half of your question | 4/5 once a day | 5/5 one card | **37/45 (82%)** |
| F81 Afternoon break nudge | 4/5 micro-break and lunch-walk studies | 4/5 answers Q4 | 4/5 rides on F73's prompt | 4/5 reuses Instead | **36/45 (80%)** |
| F75 Reaction-time check | 5/5 validated against sleep loss; P50 says it's needed | 4/5 "sharp" directly, but only vigilance | 2/5 3 min a day is the heaviest item | 3/5 needs accurate timing | **34/45 (76%)** |
| F80 Daytime experiments (on F23) | 3/5 feasible in studies; power limits (§7) | 5/5 the only way to answer "does X work for me" | 4/5 time-boxed, your choice | 2/5 scheduling + analysis + UI | **34/45 (76%)** |
| F78 Weekly energy map | 3/5 design reasoning; Gill & Panda's chart helped | 5/5 answers Q1 | 3/5 a number about you, weekly limits harm | 3/5 a chart | **33/45 (73%)** |
| H2 Wearable reader (sleep, HR, HRV) | 4/5 sleep timing valid (Miller 2022); HRV responds to alcohol | 4/5 Q2, Q3 | 3/5 vendor scores invite chasing | 3/5 needs a watch and a data path | **33/45 (73%)** |
| F77 Steps per hour | 3/5 steps are a proxy for activity | 3/5 supports Q4 | 5/5 passive | 4/5 one permission + sensor | **32/45 (71%)** |
| F83 Work-tracker import | 3/5 activity ≠ productivity (SPACE) | 3/5 Q1 context, Q5 | 5/5 no extra effort | 2/5 export unknown; strip customer names | **30/45 (67%)** |
| F84 Calendar meeting load | 3/5 meetings linked to lower perceived productivity | 2/5 context | 5/5 passive | 4/5 one permission | **29/45 (64%)** |
| F85 Morning camera HRV | 4/5 validated at rest | 3/5 Q2, Q3 | 3/5 1 min a day | 2/5 hard to build reliably | **29/45 (64%)** |
| F82 Work-PC focus log | 3/5 logging used in Mark 2014; screen time is a weak signal (P14) | 4/5 sees work focus directly | 2/5 work machine and employer policy | 2/5 second device, join by time | **27/45 (60%)** |
| F86 Typing speed on the PC | 3/5 population-scale evidence | 4/5 passive sharpness | 2/5 work laptop policy | 2/5 a desktop logger | **27/45 (60%)** |
| Photo food diary | 2/5 | 2/5 | 2/5 the most abandoned kind of tracking | 2/5 | **18/45 (40%)** |
| GPS mobility | 3/5 linked to mood in small studies | 1/5 not your question | 1/5 most sensitive data on the phone | 2/5 | **16/45 (36%)** |
| Calorie counting | 2/5 for weight, not for energy | 1/5 | 1/5 burden and harm risk | 1/5 | **12/45 (27%)** |

**What the table shows.** The measurement layer (F73, F74, F76) and the one-line caffeine rule
score highest: cheap, close to the evidence, and they produce the data everything else needs.
F79's lead depends on you drinking coffee or tea; if you don't, drop it. F75 is the option that
moves most with the weights: if burden got ×3, it would fall to the bottom half, so it should start
as a 2-week trial. A wearable (H2) scores well *if* you'll wear it at night (§9); everything that needs location, food contents or a work-laptop logger scores ≤ 60%.

**Recommendation, in order:**
1. **Weeks 1–3, baseline:** F73 + F74 + F76 + F77 on. Show nothing back except "saved". Try F75
   for the first 2 weeks and keep it only if it doesn't annoy you.
2. **After 3 weeks:** F78 in the weekly review; F79 if you drink caffeine; F81 at the afternoon
   anchor.
3. **Then experiments, one at a time:** F67 (bed scroll, already queued) first, then F80's first
   experiment, probably caffeine cut-off or the afternoon walk, depending on what the baseline
   shows.

### Phone-only plan, with quiet work hours (D-028, D-029)

No watch, no work data, no taps between 10:00 and 19:00 except at lunch, and no caffeine or alcohol
buttons (you rarely drink either).

| When | What you do | Taps |
|---|---|---|
| Morning | F65 check-in (built): rested 1–5, time to fall asleep | 2 |
| Lunch (11:45–13:00) | F73: alertness (KSS) + mood. F81 offers a 10–15 min walk outside | 2 |
| After work (from 19:00) | F76: alertness + mood now, focus 1–5, main thing done, afternoon break (none / short / walk), lunch size, snacks (part of day, sweet or not) | ~6, ~15 s |
| Wind-down | "Could you switch off after work?" 1–5 | 1 |
| Morning, right after the check-in | F75 reaction test, 3 min, as a mini-game with fixed rules (your pick, 2026-10-23). Once a day only | — |
| Passive | F64 night, phone use by hour (incl. pickups in work hours), F77 steps | 0 |

| Question | Answered by | What's missing |
|---|---|---|
| Q1 When am I sharpest? | Lunch vs. after-work ratings + F75, by hours since phone-up | No live afternoon rating; the dip is only seen in recall |
| Q2 What does last night cost me? | F64 + F65 → next day's lunch and after-work ratings, F75 | Actual sleep and night recovery |
| Q3 Do food and snacks move my energy or my night? | Lunch size and snacks → after-work rating, F64, F65 | Exact snack times |
| Q4 Which breaks help? | Lunch walk (F81) and the afternoon-break answer → after-work rating | Breaks are self-reported once a day |
| Q5 Can I switch off after work? | Wind-down rating + evening phone use by hour | Late work on the computer |

### What to log for the n-of-1 study

Following the project's exposure → action → outcome → live config → gaps rule:

| What | Fields | Why |
|---|---|---|
| **Prompt** (one row per scheduled prompt) | id, anchor, scheduled at, shown at, answered at / expired at / dismissed at, source (notification, hub) | A missed prompt is data. Compliance by anchor tells if a time is wrong for you |
| **Rating** | prompt id, KSS 1–9, mood 1–5, hours since that morning's phone-up (computed, but F64's row id stored) | Join to the night by id, not by date |
| **Event** | id, kind (caffeine, drink, meal, sweet, nap), size, happened at, logged at, late flag | Store when it happened *and* when you logged it; recalled times are less accurate |
| **Reaction test** | id, prompt id, started at, every reaction time (raw), false starts, lapses > 355 ms, phone model, app build | Raw times let a better scoring rule be applied later; the phone's touch latency is constant, so only same-phone comparisons count |
| **Day card** | logical day, focus 1–5, main thing done, switched off 1–5, answered at | The outcomes for Q2, Q4, Q5 |
| **Steps** | hour, steps, source | Store per hour, never only daily totals |
| **Live config** | anchor times, which switches were on, experiment arm, scale wording version | Settings are already versioned by logical day; the experiment arm and wording version must be too |
| **Gaps** | notifications blocked, phone off, a day you mark "not a normal day" (sick, travel, holiday) | No prompt answered must never read as "nothing to report" |

### Open questions for you

1. **Is "productive" the right word?** This doc reads it as sharp when work needs it and rested
   after it, not more output (G5). Right?
2. **Coffee or tea:** how many a day, and when is the last one? This decides F79.
3. **Will you buy a watch or band, and will you wear it at night?** If yes, buy it before the baseline starts; H2 then replaces F85. See §9.
4. **Notifications:** are two quiet prompts per workday acceptable?
5. **Reaction test:** willing to try 3 min a day for 2 weeks?
6. **Work PC:** would you want F82 or F86, and does your employer allow an activity logger on that machine? Is your task tracker automatic or manual, and can it export a file (F83)?
7. **What does a good workday look like for you?** It decides F76's questions: focus hours, the one
   main thing done, or something else.
