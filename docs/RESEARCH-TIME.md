# Research: "just enough" time, and where the rest goes

Status: **proposed**, 2026-10-18. Nothing here is decided yet. This builds on
[RESEARCH.md](RESEARCH.md) (P1–P22) and [FEATURES-V1.md](FEATURES-V1.md) (F01–F24), and doesn't
repeat them. It answers two questions: how does the app help you stop at "enough" in Facebook, X,
Instagram, YouTube, Messenger and Mobile Legends, and how does it move the freed time into reading,
building, moving, people and rest (G5)?

Sources marked † were checked online on 2026-10-18: authors, venue, sample size and the headline
numbers. "† (abstract)" means only the abstract, a publisher listing or the authors' own summary was
checked, not the full text. Unmarked sources are cited from memory. Check them before relying on them.

Evidence strength is the same as RESEARCH.md:
- **A**: a meta-analysis, a large RCT, or a large causal study.
- **B**: a single field experiment or a mid-sized RCT.
- **C**: a small HCI study, a qualitative study, or a design framework.

**About the IDs.** The brief said new principles start at P14, but RESEARCH.md already uses P14–P22.
So new principles here start at **P23**. Features start at **F25**.

## Read this first: what this research does *not* promise

- **Most effects stop when the intervention stops.** The Milkman et al. 2021† *Nature* megastudy (61,293
  gym members, 54 four-week programmes, A) found that 45% of the programmes raised gym visits by 9–27%,
  but only 8% had a measurable effect after the four weeks. In Okeke et al. 2018† (MobileHCI, 50
  people, 3 weeks, C), vibrations past a Facebook limit cut Facebook time by over 20%. Use went back
  to normal as soon as the vibrations stopped. Plan for features that stay on, not for "a course".
- **A limit on one app pushes about half the time to the apps without a limit.** Allcott, Gentzkow &
  Song 2022† (*AER*, 1,933 analysed, A): the limit tool cut use of the targeted apps by 22 min/day, and
  12 of those minutes moved to other apps (P34). Any "minutes saved" number that only counts the gated
  apps overstates the win.
- **The digital self-control tool literature is young and weak.** Monge Roffarello & De Russis 2023†
  (*ACM TOCHI*, systematic review and meta-analysis of 43 studies) found that the experiments are
  usually short (around 3 weeks), within-subject, rarely have a control group, rarely remove the tool
  again to test what lasts, and mostly recruit young university students. The pooled effect looks
  large in fragments I could see, but given those flaws, don't trust its size.
- **Experts can't predict which nudge works.** In the Milkman megastudy, impartial forecasters failed to
  predict which programmes would work best. This strengthens P13: test on yourself.
- **"Enough" has no research-backed number.** No study gives a healthy number of social media minutes
  per day. The evidence supports *how* to decide "enough" (your own purpose and your own worth-it
  ratings, P23), not a target.

## 1. Defining "enough"

Three ways to define "enough" were compared in the literature:
- **A personal daily budget** (calm-you sets X min/day). Allcott 2022†: 78% of people offered the limit
  tool set binding limits and kept them for the whole 12 weeks. Use fell 22 min/day (16%). Hiniker et
  al. 2016† *MyTime* (CHI, 23 people, 2 weeks, C): time in apps people rated a poor use of time fell
  21%, and time in apps they rated a good use stayed the same.
- **A per-session time box.** Okeke 2018† (above): a nudge past the limit cut time by 20%, but not the
  number of opens. Kim et al. 2019† *GoalKeeper* (IMWUT, 36 people, 4 weeks, C, abstract): stronger
  lockouts cut use more than warnings did, and caused more frustration and pressure.
- **Intention at opening.** Grüning et al. 2023 (P3, already in RESEARCH.md): 36% of opens dismissed.
  Haliburton et al. 2024† (CHI, 1,039 one sec users over 13.4 weeks on average, logs with no control
  group, B): open attempts fell steeply over the first ~20 days, then flattened. Users who did open went
  through more "on purpose". Xu et al. 2022† *TypeOut* (CHI, 54 people, 10 weeks, C, abstract): typing
  a short self-affirmation to unlock cut app use by over 50%, and opens and duration by over 25%, more
  than either notification-only or typing-only versions.

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P23 | **"Enough" is the point where the session stops paying, and you define it by purpose, then check it with your own ratings** | Lukoff et al. 2018† *IMWUT* (45 people, 86,402 sessions, B): the motivation to achieve a specific purpose *declined over the course of a session*, especially in passive social media and entertainment. Cho et al. 2021† *CSCW* Best Paper (29 people, feature-level logs + ESM, C): "following" feeds (Facebook News Feed, Stories) *quickly deplete their rewards*, and bite-sized recommendations (Facebook Watch) lead to "just a bit more". Nelson & Meyvis 2008† (*JMR*, lab, B, abstract): breaks interrupt hedonic adaptation and make a pleasant experience enjoyable again. Allcott 2022† + MyTime† (above): daily limits work, and the cut lands on the apps people value least | Decide "enough" per session from the purpose (the time box, F02), with a daily ceiling behind it (F04). Learn from your worth-it ratings (F10) *where* in a session they drop, and offer that as the default box. A break between two short boxes beats one long one. Feature-level matters: the feed and Watch are the expensive parts of Facebook, not groups or messages (E8, E7 already point this way) |
| P24 | **Goals work best when they're yours, specific, a bit hard and visible to someone. Feedback must be about the task, never about you** | Epton, Currie & Armitage 2017† *J. Consulting & Clinical Psychology* meta-analysis (141 papers, 384 effects, N = 16,523 in RCTs, A): goal setting d = .34 [.28, .41], and larger when the goal is difficult, public, or a group goal. Kluger & DeNisi 1996† *Psych. Bulletin* (607 effects, 23,663 observations, A): feedback improves performance on average (d = .41), but **more than a third of feedback interventions made performance worse**, and they got worse the more they pointed attention at the self instead of the task | Goals come from calm-you in the weekly review, in numbers you can check ("FB after midnight ≤ 10 min"). Feedback states the task fact ("42 min of FB after midnight, plan was 10") and the next step, never a verdict about you. This refines P10 |

## 2. Commitments and escape hatches

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P25 | **Commit when calm, keep the commitment strong, and make the way out bounded and costly instead of free or impossible** | Allcott 2022† (A): people *wanted* commitment. 78% set binding limits with no reward for doing so, they valued access at $4.20 per 3 weeks, and only 4% opted out. Limit changes applied the next day. Beshears et al. 2020† *J. Public Economics* (field experiment, B, abstract): when offered a commitment savings account, people put *more* money into versions with *stricter* withdrawal rules, which fits people who know they're present-biased. John 2020† *Management Science* (913 people, Philippines, B, abstract): people picked commitments that were too weak, and **55% defaulted and lost money**. Giné, Karlan & Zinman 2010† *AEJ: Applied* (smokers, B, abstract): only 11% took up a self-funded quit contract, but it raised the 6-month quit rate by about 3 points, and the effect held at a surprise 12-month test. Royer, Stehr & Sydnor 2015† *AEJ: Applied* (B, abstract): a 4-week gym incentive left little lasting effect *unless* a commitment contract was offered at the end, and those changes lasted. Kovacs, Wu & Bernstein 2021† *CHI* (8,000+ HabitLab users, B, abstract): over time users switched to easier interventions, while expecting to switch back to harder ones "next time" | Commitments are set in the weekly review and apply the next day (E4, S4 already do this). There is **one** escape hatch: a small, fixed number of passes per week, each logged and shown in the review. There are no money stakes, because self-chosen penalties fail and cost money (John 2020). Never ask "make this easier?" in the tempted moment. Ask in the weekly review instead, because the in-the-moment answer drifts easy (Kovacs 2021). This strengthens P2 and N5 |

## 3. Rewards, and what happens when they stop

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P26 | **Rewards can build a habit that outlasts them, but don't pay for the good things with screen time, and don't turn intrinsic activities into chores for points** | Allcott 2022† (A): paying people to cut use reduced it by **56 min/day (39%)** while paid, and still by **19 min/day** in the 3 weeks after and **12 min/day** in the 3 weeks after that, with no detectable shift to other apps. Charness & Gneezy 2009† *Econometrica* (2 field experiments, B, abstract): paying for gym visits raised attendance after the payments ended, only among people who weren't already regulars. Mitchell et al. 2020† *BJSM* meta-analysis (23 RCTs, A–B): small incentives raised steps by 607/day during the programme and 514/day after (9 studies), but only 4 of 18 studies found a post-programme benefit by vote count. Milkman 2021† (A): the best of 54 programmes was a tiny **bonus for returning after a missed workout**. Deci, Koestner & Ryan 1999† *Psych. Bulletin* (128 studies, A): expected tangible rewards lowered later free-choice interest (d ≈ −.28 to −.40), while positive verbal feedback raised it (d = .33). The size of this effect is still disputed (Cameron et al. 2001). Moral licensing ("I was good, so I can indulge"): Blanken et al. 2015† (91 studies, d = .31), but bias-corrected estimates are near zero and large replications found nothing (Kuper & Bott 2019; numbers seen second-hand only). So licensing is a weak worry, not a proven one | The hub can't pay you, and it shouldn't invent a currency. Use *informational* rewards: competence feedback in words ("3 walks this week, more than last week"). Make the strongest moment a **comeback** after a slip, not a streak. Don't use "earn scroll minutes by exercising": it makes the feed the prize and exercise the price, which is the overjustification pattern (design reasoning on top of Deci 1999; no direct test) |

## 4. Replacement behaviours

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P27 | **Plan what to do *instead*. A "don't" plan backfires** | Adriaanse, van Oosten, de Ridder, de Wit & Evers 2011† *PSPB* (lab experiments, B, abstract): "if X, then I won't do Y" plans produced an *ironic rebound* and strengthened the habit, especially when the habit was strong. Adriaanse, Gollwitzer, de Ridder, de Wit & Kroese 2011† *PSPB* (3 experiments, B, abstract): "if X, then I'll do Z instead" plans removed the habit's head start. Habit reversal training, whose core is a *competing response*: Bate et al. 2011† meta-analysis (18 studies, 575 people, tics/nail-biting/stuttering, A for that domain): d = 0.80. It's a clinical-habit analogy, not a phone study | Every if-then plan and every gate line names the replacement ("If bored in bed → audiobook, 15 min"), never "don't open FB". The Instead action has to be **startable in one tap from the gate**: start a walk timer, open the book app, open lc-trainer, call her. A list you have to read and then act on is weaker. This refines P9 and F15 |
| P28 | **Schedule the good activities in advance. Don't wait to feel like it** (behavioural activation) | Mazzucchelli, Kane & Rees 2010† *J. Positive Psychology* meta-analysis (20 RCTs, 1,353 people, A): behavioural activation raised well-being, g = 0.52, **in non-clinical people too**. Brailovskaia et al. 2022† *J. Public Health* (RCT, N = 642, 2 weeks, B): −30 min/day social media, +30 min/day exercise, or both. Each helped, **the combination helped most**, and press summaries report effects still visible at 6 months. Temptation bundling, i.e. allowing a treat only during the "should" activity: Milkman, Minson & Volpp 2014† *Management Science* (B): gym-only audiobooks raised visits 51% at first, and the effect faded. Kirgios et al. 2020† (N = 6,792, B, abstract): giving audiobooks plus the suggestion to bundle raised weekly workouts 10–12%, lasting up to 17 weeks after. Fresh start: Dai, Milkman & Riis 2014† *Management Science* (B, abstract): people start goals more often after temporal landmarks (Mondays, new months, birthdays) | In the Sunday review, put 3–5 activities into your actual risky slots from the heatmap, as appointments, not wishes. Pair reducing with adding: the evidence favours "less FB *and* a walk" over "less FB". Offer one temptation bundle (e.g. game streams only while on a walk or workout). New plans start on Mondays. This strengthens P16 and G5 |

## 5. Making the future cost visible

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P29 | **People know habits form, but act as if they don't. Make tomorrow concrete at the moment of choice** | Allcott 2022† (A): people correctly *predicted* that the bonus would have lasting effects, but between the announcement and the start of the bonus they barely cut back (12% of what forward-looking behaviour would predict). The authors call this projection bias. The control group also kept underestimating their next-period use, by about 4%, even after seeing their past use. Rösch, Stramaccia & Benoit 2022† *J. Exp. Psych.: General* meta-analysis (48 articles, 174 effects, A, abstract): **episodic future thinking**, i.e. vividly imagining a specific future event, made choices more patient, g = .44 [.33, .55]. Secondary sources say positive future images work and negative ones don't (Rösch et al., not checked) | At the night gate, show one concrete fact about tomorrow and a positive image prompt ("Alarm at 08:30. What's the first good thing you'll do tomorrow?"), not a threat. In the weekly review, ask for a prediction of next week's unintended minutes, then show the gap the week after. This is lab evidence on money and health choices, so the transfer to phone use is untested. Measure it (P13) |

## 6. Social accountability

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P30 | **One person who sees your goals helps. A leaderboard of minutes can backfire** | Epton 2017† (A): public and group goals beat private ones. Harkin 2016 (P10): reported progress d = .47 vs. unreported .19. Ko et al. 2015† *NUGU* (CSCW, 62 people, 3 weeks, C, abstract): limiting phone use *together*, sharing limits in a group, cut use more than the same app without the social part. Patel et al. 2019† *JAMA Internal Medicine* STEP UP (RCT, 602 adults, 36 weeks, A–B): support, collaboration and competition all raised steps, and **competition** raised them most (+920/day). Only the competition arm kept an effect after the game stopped. Schultz et al. 2007† *Psychological Science* (field, B): telling households the average use made *low* users use more (boomerang). Adding approval for the low users stopped that | A weekly summary of *your goals and activities* to one person you choose (your girlfriend), who can react with one tap. Never a ranking of raw minutes against other people: it makes "average" a target. Competition is the strongest arm in STEP UP, but it clashes with P12 and your "self first" preference, so it's parked |

## 7. Mindfulness and urge surfing

This is the evidence review that REQUIREMENTS.md "Later" asked for ("Mindfulness micro-practice at the
gate needs its own evidence review first").

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P31 | **Urge surfing may change what you do with an urge, not the urge itself. The evidence is thin** | Bowen & Marlatt 2009† *Psych. of Addictive Behaviors* (123 student smokers, B, abstract): after brief mindfulness instructions, urges were *no lower*, but people smoked fewer cigarettes over the next 7 days. Goldberg et al. 2021† *Perspectives on Psych. Science* umbrella review (44 meta-analyses, A, abstract): mindfulness beat passive controls, but less often beat active ones. Throuvala et al. 2020 (seen only in a search summary): a 10-day mindfulness programme reduced phone *distraction* in students, but not habitual use or social media time. The one sec pause (P3) already includes a breathing animation, so its effect isn't separable | Offer a 20–30 s "notice the urge, watch it peak and fade" as **one of the rotating gate forms** (P32), not as the default. Judge it with an n-of-1 test (F23). Verdict on the "Later" item: worth a cheap test, not a feature on its own |

## 8. Habituation: keeping friction working

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P32 | **The same friction stops working within weeks. Changing its form keeps it alive, but annoys more, so say why it changed** | Haliburton 2024† (B): open attempts fell steeply for ~20 days, then levelled off. Users continued past the pause 67% of the time, and the dismissal rate *fell* over time. Some users said they simply got used to the wait. 26.9% took breaks from the app, usually about 2 days, most often starting on a Friday. Use spiked just before a break, went up during it, and recovered quickly once the friction was back. Kovacs, Wu & Bernstein 2018† *CSCW* (HabitLab, 217 people within-subject, then 409 for 5 weeks, B; slides checked): with a static intervention, time per site crept up from 116 s (day 1) to 183 s (day 3). Rotating interventions gave 96 s/day vs. 146 s/day static, **but** one-week retention fell from 74% to 52%. A just-in-time notice explaining the rotation **halved** that attrition (abstract). Anderson et al. 2015† *CHI* and Vance et al. 2018† *MIS Quarterly* (fMRI + eye tracking + a 3-week field study, B, abstract): attention to a repeated warning drops after the *second* view. **Polymorphic** warnings, whose appearance changes, kept adherence high after 3 weeks. Attention partly recovered between workdays. Meinhardt 2026 (P3): pop-up effects fade fast | Rotate the *form* of the gate (wait, typed intention, urge surf, "what instead?" question), not just the wording. `PromptCopy` already rotates text. Show a one-line "the pause changes so it keeps working" note when the form changes. Expect a plateau around week 3, and don't read it as failure. Watch for the "Friday break" pattern: an F16 health-check warning that the gate is off, plus a weekend plan. Spend strong friction where it matters (night, bed) rather than all day. This strengthens P3 and F05 |

## 9. What backfires

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P33 | **Shame, orders and broken streaks make people give up. Self-compassion makes them try again** | Kluger & DeNisi 1996† (A): self-focused feedback is how feedback ends up hurting. Ma, Ebesu & Dillard 2026† *J. Communication* meta-analysis (35 studies, N = 10,658, A, from a news write-up): freedom-threatening language ("you must") raised perceived threat, which raised reactance, which lowered persuasion. The last link was weak, and heterogeneity was high. Silverman & Barasch 2023† *J. Consumer Research* (7 studies, B, abstract): showing a *broken* streak lowered later engagement compared with showing an intact one. The drop was worse when people blamed themselves, and smaller when the streak could be "repaired". Breines & Chen 2012† *PSPB* (4 experiments, B, abstract): after a failure, self-compassion led to more motivation to improve and more study time than a self-esteem boost. Abstinence violation effect (Marlatt; from memory): one lapse seen as "I've failed" turns into a full relapse | Copy rules for every screen: suggest ("want to…?", "could"), don't order. State the task fact, not a judgement. No streak counters at all (W3 already says this). After a slip day, the next open says "welcome back", shows the one next step, and the week's plan carries on. A new week is a fresh start. This strengthens P12, F4, S6 and W3 |
| P34 | **Time you take from one app partly flows to the apps you didn't guard** | Allcott 2022† (A): the limit cut the target apps by 22 min/day, and **12 min/day moved to other apps** where people had set fewer limits. Self-reported use on other devices rose 4.2 min/day (marginally significant). The *bonus* showed no substitution. Kovacs et al. 2019† *CHI* "Conservation of procrastination" (HabitLab, B, abstract): more interventions on the target site didn't raise time on other sites, or shift time between browser and phone. Allcott 2020 (RESEARCH.md): deactivating Facebook also *lowered* other online time. So substitution is real for app limits, and smaller or absent for broader changes | Count a **group**: "feeds" (FB, IG, Threads, and facebook.com in Chrome) share one budget instead of a budget per app. The review shows a "time in ungated apps" line, so moved time is visible. Your baseline has 6.4 h/week of unexplained Chrome and 4 h/week of Threads, which are the obvious leaks. X stays lighter (D-016), but its minutes are watched |

## How this changes what's already there

| Existing item | Effect | Why |
|---|---|---|
| P2 precommitment | **Strengthened, with a caveat** | 78% kept their limits and only 4% opted out (Allcott 2022). But half the saved time leaked to other apps (P34) |
| P3 open-time friction | **Strengthened on the long term, weakened on "it keeps working"** | Haliburton 2024: effects last 13+ weeks, but they plateau around week 3 and dismissals fall (P32) |
| P9 if-then plans | **Refined** | The plan must name a replacement, never a negation (P27) |
| P10 track and report | **Refined** | Feedback about the self can make things worse. Public and hard goals do better (P24) |
| P11 habits take months | **Strengthened** | Buyalskaya et al. 2023† *PNAS* (30,000+ gym-goers, A): the gym habit took *months*, hospital handwashing *weeks*. Lally's 66 days holds |
| P12 autonomy, no shame | **Strengthened** | Reactance meta-analysis, broken-streak studies, self-compassion (P33) |
| P13 measure on yourself | **Strengthened** | Megastudy forecasters failed to pick the winners |
| P16 what replaces the time | **Strengthened, and it now needs scheduling** | Behavioural activation g = .52, Brailovskaia's combination arm (P28) |
| F04 daily budgets | **Weakened as specified** | A budget per app invites substitution. Make it a group budget (F29) |
| F05 escalating friction | **Partly contradicted** | Only the delay *length* grows today (`GatePolicy.delayFor`, linear up to 60 s). The research says to change the *form*, and to explain the change (P32, F30) |
| F15 Instead list | **Strengthened, but too passive** | It has to be one-tap actionable and scheduled (P27, P28, F26, F27) |
| F24 share with partner | **Strengthened** | Public goals (Epton), reported monitoring (Harkin), NUGU. It moves up (F33) |
| D-016 X gets a lighter gate | **Flagged** | If Facebook gets strict and X stays light, Allcott's substitution result predicts some time moves to X and Threads. Watch the ungated line (P34) before tightening anything |
| Gate copy (`PromptCopy`) | **Mostly fine** | The lines are questions, not orders. "You opened this %d times today" is a fact, but keep it task-framed and never add a judgement (P33) |
| "Later: mindfulness at the gate" | **Answered** | It becomes one rotating form, with an n-of-1 test (P31) |

## What industry does with this

Only **one sec** has peer-reviewed outcome data. Every other number below is the company's own,
usually from a self-selected survey with no control group. "Draws on" is my mapping to the research,
not the company's claim, unless stated otherwise.

| App | What it does | Draws on | Outcome data |
|---|---|---|---|
| **one sec**† | A breathing pause before the app opens. You can then go on or close it | P3, P31 | **Peer-reviewed.** Grüning 2023 *PNAS* (280 users, 6 weeks): 36% of opens dismissed, 37% fewer attempts. Haliburton 2024 *CHI* (1,039 users, 13 weeks): lasting fewer attempts, plateau around week 3. The company's "57% less social media" comes from the PNAS study as reported by the company |
| **ScreenZen**† | Free. A pause screen with a set delay, an optional "what are you seeking?" intention, daily time and open-count limits, cooldowns between sessions, and a multi-step gesture to override a limit | P3, P25 (bounded override) | None found. Reviews only |
| **Opal**† | Blocking sessions, "Deep Focus" that's hard to cancel, gems, focus score, leaderboard | P25, gamification | **Marketing.** It claims "1 h 23 min/day saved" from an in-app survey of a sample of users, with self-reported baselines and no control group. The survey's own numbers don't reconcile (2,000 members vs. "10% of the base") |
| **Freedom**† | Scheduled cross-device blocking, a locked mode | P1, P25 | **Marketing.** "2.5 h/day more productive" from a survey of 2,500 users with an undisclosed method |
| **Clearspace**† | A pause before opening, strict mode (1 min–48 h), accountability partners, and "earn screen time" with push-ups, squats or steps | P3, P30. The earn mechanic goes *against* P26 | None found |
| **Forest**† | A tree grows while you stay off the phone and dies if you leave. Real trees get planted | Loss aversion (the company's framing), P26 | None. The only study is a 5-person usability test |
| **Android Digital Wellbeing / Apple Screen Time**† | App timers (the app greys out), Bedtime / Wind Down (greyscale + DND), Downtime. Apple's "Ignore limit" is a free override | P2, P19, greyscale | None published by Google or Apple. Apple's free override is widely bypassed (vendor blogs and press, anecdotal). Greyscale on its own has RCT support: Zimmermann & Sobolev 2023† *CBSN* (pre-registered, 112 people, B): it cut screen time immediately, more than self-set time limits did, with no measurable change in well-being |
| **Jomo**† | Rules, "breaks" (wait, type or password), a strict mode that locks rules for days, journaling (distracting vs. productive), and "Squads" with a daily screen time scoreboard | P25, P10, P30. The scoreboard risks the P30 boomerang | None found |
| **Brick**† | A $59 NFC tag. Tap to block, and you have to walk back to the tag to unblock. **5 emergency unbricks** per device, then you email support | P1, **P25 bounded escape hatch** | None. Reviews say knowing the overrides exist changes how you treat them |
| **Roots**† | Limits by time or opens, schedules, "Monk Mode" until tomorrow, **12-hour wait before you can edit a block**, uninstall protection, a streak with **"cheat days"** | P25 (a delay on weakening), P33 (repairable streak) | **Marketing.** "30% or more" less screen time |
| **Ash**† | I couldn't find a screen-time app called Ash. The closest match is Slingshot AI's **Ash**, an AI therapy chatbot (launched July 2025, free) | Not a time-use tool | A company preprint (≈300 people) reports symptom improvement. STAT News coverage was critical. **Tell me if you meant a different Ash** |
| **Duolingo** (as a warning) | Streaks, streak freezes, streak-saver notifications | The opposite of P33: a streak as a retention hook | Only second-hand churn numbers that conflict. No public experiment |

**What the industry pattern shows**
- **Escape hatches converge on "bounded and costly".** Brick's 5 unbricks, Roots' 12-hour edit delay,
  Jomo's multi-day strict mode and ScreenZen's gesture all sit between a free override (Apple, easily
  bypassed) and no way out (which Kim 2019 found frustrating). That's P25.
- **Rewards drift into currencies.** Clearspace sells feed minutes for push-ups. Opal and Forest add
  gems and trees. None publishes what happens when people stop. The research (P26) points the other
  way.
- **Social features are scoreboards.** Jomo Squads and Opal leaderboards compare raw minutes, which is
  the setup where Schultz 2007 found a boomerang. Nobody ships "one partner sees your goals", even
  though that has the best evidence.
- **Nobody fills the freed time.** Every app above subtracts. None schedules what replaces the time,
  which is where the strongest evidence in this document sits (P27, P28). That's the gap Mindful Hub
  can fill.

## Candidate features

- **F25 Worth-it curve time boxes**: The app learns, for each app and purpose, at which session minute
  your worth-it ratings drop. It offers that as the default time box, with a short break before a
  second box. (P23, P13)
- **F26 One-tap replacement at the gate**: On "just bored", the gate offers 2–3 Instead actions as
  buttons that *start* something: a 10-min walk timer, the book app, lc-trainer, "call her". It logs
  whether you did it. All copy says "instead do Y", never "don't". (P27, P9, P28)
- **F27 Weekly activity plan**: In the Sunday review, you place 3–5 activities into your real risky
  slots from the heatmap (e.g. 21:00 walk, 23:30 audiobook in bed). They show as appointments, with a
  gentle reminder, and get marked done or skipped. Plans start Monday. (P28, P16, P24)
- **F28 Bounded escape hatch**: 3 passes a week, 10 min each, past any budget or lock except the sleep
  window. Each pass is logged and shown in the review. Weakening a rule is only possible in the review
  and applies the next day. (P25)
- **F29 Group budget + leak watch**: One "feeds" budget shared by FB, IG, Threads and facebook.com in
  Chrome. The review shows a "time in ungated apps" line next to it, so moved time is visible. (P34, P2)
- **F30 Rotating gate forms**: The gate rotates between forms (wait, typed intention, "what instead?",
  urge surf), with a one-line note explaining why it changes. It's strongest at night. (P32, P3)
- **F31 Tomorrow-you at night**: After bedtime, the gate shows tomorrow's alarm time and one positive
  prompt ("What's the first good thing you'll do tomorrow?") before the usual pause. (P29, P19)
- **F32 Comeback, not streaks**: Copy rules for every screen (suggest, task facts, no verdicts). A
  "welcome back + one next step" after a slip day, and a Monday fresh-start note. No counters that
  reset. (P33, P26)
- **F33 Partner check-in**: F24 made concrete. A weekly card with your goals, the activities you did,
  and one line from you goes to one person you choose. They react with one tap. There are no minute
  rankings. (P30, P24)
- **F34 Urge-surf form**: A 20–30 s guided "notice the urge" as one of F30's forms, with its own n-of-1
  test. (P31)
- **F35 Temptation bundle**: YouTube game streams get a "watch while walking or working out" purpose
  with a longer time box. Honour-system, logged as "move". (P28)
- **F36 Prediction check**: The review asks "how many unintended minutes next week?", then shows the
  gap the week after. (P29, P24)
- **F37 Earn scroll time with exercise** (Clearspace-style): included only to show why it's out (see
  the gate below). (P26)

## Our own ideas, built from the research

F25–F36 apply the research fairly directly. These go a step further: each starts from a finding and
asks "what would we build if we took this seriously?" No app in the industry table does them. The
chain is shown as **finding → idea**.

- **F55 Feature-aware time box.** *Cho 2021: the "following" feed and Watch drain value fastest,
  while groups and messages don't (P23).* → The time box only counts down while you're in the feed or
  Watch, not in a group or a chat. You're budgeting the expensive part of Facebook, not the useful
  part. (P23, E7, E8)
- **F56 Split box with a break.** *Nelson & Meyvis: a break makes a pleasant experience enjoyable
  again. Rixen: sessions rarely end by your own choice (P6).* → Instead of one 30-min YouTube box, the
  gate offers 2 × 15 min with a 5-min off-screen break in between. You get more out of the second half,
  and the break is a natural exit. (P23, P6)
- **F57 Note from calm-you.** *Allcott 2022: people want commitments (P25). Rösch 2022: a concrete
  self-image makes choices more patient (P29).* → When you set a rule in the review, you write one line
  on why ("I want to wake at 8 and feel good"). Before tempted-you uses an escape pass (F28), the gate
  shows that line, in your own words. (P25, P29)
- **F58 Reset sprint with a taper.** *Allcott 2022: a 3-week incentive left a lasting drop (P26).
  Haliburton 2024: most change happens in the first ~3 weeks (P32). Monge Roffarello 2023: studies
  rarely test what lasts when a tool is removed.* → A planned 3-week sprint with strong friction, then
  rules loosen step by step over 3 more weeks while the app watches what holds. You find out which
  rules you actually need, and only keep those. (P26, P32, P13)
- **F59 One-minute replacements.** *Tran 2019: compulsive sessions start in unoccupied moments
  (waiting, dull tasks). P27: plan the replacement.* → The Instead list is matched to how long you
  have. In a 1-minute moment (toilet, waiting, a build compiling) it offers 1-minute things: one
  lc-trainer card, look out the window, a voice note to her. A 20-minute walk is offered only when
  there's 20 minutes. (P27, P9)
- **F60 Real-world setup kit.** *Duckworth et al. 2018: changing the situation beats willpower (P1).
  Hofmann 2012: people with good self-control avoid temptation rather than resist it.* → The weekly
  plan (F27) ends with a 2-minute physical checklist: book on the pillow, shoes by the door, charger
  across the room (Z3), lc-trainer open on the laptop. The phone app's job is to set up the room, not
  to win the fight later. (P1, P28, P19)
- **F61 Morning receipt.** *Kluger & DeNisi: feedback should be about the task (P24). Allcott:
  people act as if they don't see the cost coming (P29).* → On waking, one calm card: last night's
  minutes after bedtime, sleep start and wake time, next to *your own* best recent night. It's your data
  as the "future cost" evidence, not a generic warning. It never says "you failed". (P29, P24, P19)
- **F62 Risk-weighted friction.** *Haliburton and Kovacs: friction wears off with repetition (P32).
  Vance 2018: attention recovers between days. BASELINE: the harm is concentrated at night.* → Instead
  of a delay that grows with every open, friction is spent where your heatmap shows the risk: strong at
  night, in bed and after the 3rd open in an hour; near zero for a quick daytime check. Less exposure
  means less habituation where it matters. (P32, P3, P19)
- **F63 Freed-minutes ledger.** *Castelo 2025 and Allcott 2020: what the time turns into matters
  (P16). Harkin 2016: recorded progress works (P10).* → The weekly review shows the unintended minutes
  you freed against baseline *next to* where they went: "5 h freed: 2 h walking, 1 h reading, 2 h not
  logged". It makes the swap visible, which is the whole point of G5. (P16, P10, D-004)

## Scoring

Criteria and weights are **proposed**. The first four match FEATURES-V1.md. One is new, because this
round's goal is not only less time but *better* time.

| Criterion | Why it matters | Weight |
|---|---|---|
| Evidence | Does science say it changes behaviour or well-being? (P*) | ×3 |
| Fit to your data | Does it hit what BASELINE.md shows: bed, ~25 self-started FB opens a day, "bored" as the main reason, 6.4 h/week of unexplained Chrome? | ×3 |
| Fills the freed time (new) | Does it move the time into G5 activities, not just subtract? This is half of what you asked for | ×2 |
| Build effort (5 = easy) | You're solo, and the gate core already exists in `:gate` and `:core` | ×2 |
| Low annoyance (5 = low) | Tools you resent get uninstalled (Kovacs 2018: rotation cost 22 points of retention) | ×1 |

Gate (must pass, not weighted): free to run, no risk to your accounts, and **no shaming and no
"currency" for good activities** (P26, P33). F37 fails the gate, because it makes the feed the prize
and exercise the price. The other 21 pass. Max score = 55. Ideas marked **(ours)** come from the
"Our own ideas" section above.

| # | Feature | Evidence ×3 | Fit ×3 | Fills time ×2 | Effort ×2 | Annoyance ×1 | **Weighted total** |
|---|---|---|---|---|---|---|---|
| F26 | One-tap replacement at the gate | 4/5 Adriaanse 2011: replacement plans beat habits, negation backfires; P9 d = .65 | 5/5 "bored" is your main reason; the gate already has the bored path | 5/5 the time goes straight into a G5 action | 4/5 extends `JUST_BORED` + `InsteadDefaults` with intents | 4/5 optional, and it gives you something | **49/55 (89%)** |
| F27 | Weekly activity plan | 5/5 BA meta-analysis g = .52 in non-clinical people; Brailovskaia RCT, combination best | 4/5 the risky slots are known (20–23 h, 01–03 h), but the bed slot is for sleep, not activities | 5/5 this is the whole point | 3/5 planner UI + reminders + done/skip log | 3/5 planning is a chore; skipped items have to stay guilt-free | **46/55 (84%)** |
| F60 | **(ours)** Real-world setup kit | 4/5 Duckworth 2018 (A): changing the situation beats willpower; Hofmann 2012 | 4/5 bed is your #1 trigger; charger away from the bed (Z3) is already a week-0 step | 4/5 the book on the pillow *is* the replacement | 5/5 a checklist at the end of the plan | 4/5 2 min a week | **46/55 (84%)** |
| F29 | Group budget + leak watch | 5/5 Allcott 2022 RCT: 12 of 22 min moved to other apps | 5/5 Chrome 6.4 h/week unexplained, Threads 4 h, X light by design | 1/5 subtracts only | 4/5 UsageStats + one group counter; Chrome domains need the accessibility tree | 3/5 a shared budget runs out faster | **43/55 (78%)** |
| F31 | Tomorrow-you at night | 3/5 EFT meta-analysis g = .44, but lab choices, not phones; Allcott projection bias | 5/5 bed is your #1 trigger; 2 h 24 m of FB after midnight | 3/5 sleep counts as rest | 5/5 copy + alarm time inside the existing night gate | 3/5 night-you may resent it | **43/55 (78%)** |
| F59 | **(ours)** One-minute replacements | 3/5 Tran 2019 triggers; P27 replacement plans | 4/5 coding breaks, waiting, the toilet are your known triggers | 4/5 small G5 actions that fit the moment | 5/5 a duration tag on Instead items | 4/5 it fits instead of nagging | **43/55 (78%)** |
| F63 | **(ours)** Freed-minutes ledger | 3/5 Castelo and Allcott on displacement; Harkin on recorded progress | 4/5 shows where the time actually goes | 4/5 makes the swap visible | 4/5 needs A5 logging and the baseline | 5/5 a chart in the review | **42/55 (76%)** |
| F32 | Comeback, not streaks | 4/5 broken-streak studies (7), self-compassion, reactance meta-analysis; Milkman's best arm was a comeback bonus | 3/5 slips will happen (P11), but this isn't a specific data pattern | 2/5 indirect: keeps you trying | 5/5 copy rules + one message | 5/5 kind by design | **40/55 (73%)** |
| F35 | Temptation bundle | 4/5 Kirgios 2020 (N = 6,792): +10–12% workouts, lasting 17 weeks | 3/5 YouTube is mostly streamers, so the bundle fits; exercise habit unknown | 4/5 turns watching into moving | 3/5 a new purpose + honour log | 4/5 opt-in | **39/55 (71%)** |
| F61 | **(ours)** Morning receipt | 3/5 task-focused feedback (Kluger); projection bias (Allcott) | 5/5 2 h 24 m of FB after midnight; the sleep shift | 2/5 indirect | 4/5 one card from data you already log | 3/5 a bad night is shown in the morning, so the tone has to be right | **39/55 (71%)** |
| F58 | **(ours)** Reset sprint with a taper | 4/5 Allcott bonus persisted after it ended; Haliburton ~3-week window; the missing withdrawal phase | 4/5 tells you which rules you actually need | 2/5 indirect | 4/5 a schedule of settings | 3/5 3 strict weeks | **39/55 (71%)** |
| F28 | Bounded escape hatch | 4/5 Allcott 78% kept limits; Beshears: stricter preferred; John: weak commitments fail | 4/5 weak self-discipline right now; makes F04/S1 survivable | 1/5 subtracts only | 4/5 a counter + log | 4/5 it lowers resentment of locks | **38/55 (69%)** |
| F57 | **(ours)** Note from calm-you | 3/5 precommitment (Allcott); future-self imagery (Rösch); the note itself is untested | 4/5 weak self-discipline; escape-pass moments | 1/5 subtracts only | 5/5 one text field + one screen | 4/5 your own words | **37/55 (67%)** |
| F33 | Partner check-in | 4/5 Epton: public goals; Harkin d = .47; NUGU; STEP UP | 3/5 you said self first; she's already in your daily life | 3/5 she sees the activities, not just the minutes | 3/5 export/share card + reaction | 3/5 privacy, and it's a relationship risk if it's framed badly | **36/55 (65%)** |
| F62 | **(ours)** Risk-weighted friction | 3/5 habituation (Haliburton, Kovacs, Vance); the weighting is design reasoning | 5/5 your harm is concentrated at night, in bed | 1/5 subtracts only | 3/5 risk model from the heatmap | 4/5 less friction in the daytime | **36/55 (65%)** |
| F56 | **(ours)** Split box with a break | 3/5 Nelson & Meyvis (lab); Lukoff | 4/5 YouTube streams and 60-min FB blocks | 2/5 the break can be a short walk | 4/5 a two-part timer | 3/5 a break mid-stream can annoy | **36/55 (65%)** |
| F30 | Rotating gate forms | 4/5 Kovacs 2018 rotation, polymorphic warnings, Haliburton plateau, TypeOut | 4/5 25 opens a day is exactly where habituation bites | 1/5 subtracts only | 3/5 3–4 new overlay forms | 2/5 rotation raised attrition from 26% to 48% at one week | **34/55 (62%)** |
| F36 | Prediction check | 3/5 Allcott: people underestimate by about 4%; no direct test of the fix | 3/5 a review add-on | 1/5 subtracts only | 5/5 one question + one number | 4/5 once a week | **34/55 (62%)** |
| F25 | Worth-it curve time boxes | 3/5 Lukoff, Cho, Nelson & Meyvis; tuned boxes themselves are untested | 4/5 unbroken 60-min FB blocks | 1/5 subtracts only | 3/5 needs weeks of F10 ratings + a fit per app | 4/5 the defaults just get smarter | **33/55 (60%)** |
| F55 | **(ours)** Feature-aware time box | 3/5 Cho 2021 feature-level regret; Lukoff | 4/5 groups are your real reason, the feed is the habit | 1/5 subtracts only | 2/5 detecting the feed screen is fragile, like Reels | 4/5 fairer to real use | **31/55 (56%)** |
| F34 | Urge-surf form | 2/5 one smoking study; mindfulness rarely beats active controls | 3/5 bored openings fit | 1/5 subtracts only | 4/5 one more form in F30 | 3/5 meditative prompts annoy some people | **28/55 (51%)** |
| F37 | Earn scroll time with exercise | ❌ **fails the gate** (P26) | — | — | — | — | — |

Proposed tiers, same cut-offs in percent as FEATURES-V1.md: **≥75% → next**, 60–74% → after,
<60% → later. Next: F26, F27, F60, F59, F29, F31, F63. After: F32, F61, F58, F35, F28, F57, F62, F56,
F33, F30, F36, F25. Later: F55, F34.

## What the table shows

- **The winners fill time; the rest only subtract.** F26, F27 and our own F60, F59 and F63 are the
  ones that score 4–5/5 on "fills the freed time", and four of them reach the "next" tier. Most of
  the industry, and most of F01–F24, subtract. This is the gap.
- **Our own ideas hold up.** F60 (real-world setup kit) ties F27 for second place. It's the cheapest
  way to act on the strongest principle we have (P1: change the situation). F59 (one-minute
  replacements) and F63 (freed-minutes ledger) also reach the "next" tier, and both are small
  extensions of things already planned (F26, A5).
- **F29 is the cheapest protection for everything else.** The substitution result is A-grade, and your
  baseline already has the leak (Chrome 6.4 h/week, Threads 4 h). Without it, every "minutes saved"
  chart overstates the win.
- **F31 ties F29 on points**, and wins on effort (it's copy inside the night gate). Its evidence comes
  from lab choices, not phone use. F61 (morning receipt) is its morning twin.
- **F32 is nearly free and should ship with whatever comes first.** It's mostly copy rules, and it
  protects every other feature from the "I failed, so why bother" spiral.
- **Sensitivity.** F26 stays first under every change tried. F27 and F60 are second and third under
  every weighting except one: if "fills the freed time" counts only ×1, F29 ties F60 for second
  (42/50) and F27 drops to fourth (41/50). If evidence counts only ×2, F59 and F31 (40/50) move above
  F29 (38/50).
- **F30 (rotating forms) scores low despite good evidence**, because rotation cost retention in
  HabitLab. Don't drop it: it's the fix for the week-3 plateau. Build it after the replacement
  features, and with the "why it changed" note that halved attrition. F62 (risk-weighted friction) is
  the cheaper way to slow habituation in the meantime.

## Recommendation

Build three small slices, with **F32**'s copy rules applied from the first one:
1. **F26 + F59, one-tap replacement sized to the moment**: it turns the gate's existing "just bored"
   path from a pause into a start. It's small, because `InsteadDefaults` and `Purpose.JUST_BORED`
   already exist, and F59 is just a duration tag on each Instead item.
2. **F29 group budget + leak watch**: it keeps the numbers honest before more friction is added.
3. **F27 + F60, weekly activity plan with a real-world setup kit**: it ships with the Sunday review
   (F11), uses the heatmap you already have, and ends with the 2-minute "set up the room" checklist.

Then **F31 + F61** as the night/morning pair when the night gate is next touched, and **F63** once A5
logging exists.

**Measure it.** Run each one as an A/B week against the baseline (F23). The outcome is unintended
minutes (G1) *plus* hours per G5 category, and "time in ungated apps" as the guard metric.
