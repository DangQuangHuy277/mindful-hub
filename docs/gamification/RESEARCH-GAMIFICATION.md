# Research: gamification for the good side

Status: **proposed**, 2026-10-19. Nothing here is decided yet. This follows
[RESEARCH-TIME.md](../RESEARCH-TIME.md) (P23–P34, F25–F37) and asks: can game elements make you
*want* to do the good things (move, people, read, build, rest) and spend less time in the gated apps,
without the app becoming a new attention hook?

Sources marked † were checked online on 2026-10-19: authors, venue, sample size and the headline
numbers. "† (abstract)" means only the abstract, a publisher listing or the authors' own summary was
checked, not the full text. Unmarked sources are cited from memory. Check them before relying on them.
The evidence grades (A/B/C) are the ones defined in [RESEARCH.md](../RESEARCH.md).

New principles start at **P35**. New feature ideas start at **F38**. F38–F47 adapt elements other apps use; F48–F54 are our own, each built from a finding.

## Read this first: what gamification does *not* promise

- **The effect is small, and mostly lasts only while the game runs.** Mazeas et al. 2022† *JMIR*
  meta-analysis (16 RCTs, 2,407 people, A): gamified physical-activity programmes had g = 0.42 overall.
  But against a *non-gamified version of the same programme* it was only g = 0.23. After the programme
  ended (about 14 weeks of follow-up) it was g = 0.15. Sailer & Homner 2020† *Educational Psychology
  Review* meta-analysis (A): learning outcomes g = .49, but motivation g = .36 and behaviour g = .25,
  and the behaviour and motivation effects didn't hold up in the most rigorous studies.
- **Novelty fades fast.** Howe et al. 2016† *BMJ* (1,182 Pokémon GO players, B): +955 steps/day in the
  first week, back to baseline by week 6. Hamari, Koivisto & Sarsa 2014† (*HICSS* review, abstract):
  gamification effects depend on the context, on the user, and on novelty.
- **It can backfire.** Hanus & Fox 2015† *Computers & Education* (one semester, B): a mandatory badge
  system and leaderboard *lowered* intrinsic motivation, satisfaction and final exam scores compared
  with the same course without them. Diefenbach & Müssig 2019† *Int. J. Human-Computer Studies* (45
  Habitica users over 2 weeks, C, abstract): *every* participant hit counterproductive effects, such as
  being "punished" by the game in their most productive weeks.
- **What's left is still worth having.** g = 0.23 over a well-designed non-game programme is about the
  size of the better nudges. That's an add-on to F26–F29 from RESEARCH-TIME.md, not a replacement for
  them.

## Principles

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P35 | **Gamify the good side, keep it running, and expect a small effect** | Mazeas 2022† (A) and Sailer & Homner 2020† (A) above. Johnson et al. 2016† *Internet Interventions* systematic review (19 studies, B): 59% positive and 41% mixed for health and well-being, and results were clearer for health *behaviours* than for cognitive outcomes. Most studies didn't compare against a non-gamified version. Howe 2016† (B): novelty is gone in about 6 weeks | Game elements go on the *activities* (G5), never on the gated apps. They stay on permanently, not as a 4-week challenge. Test them like any feature: an A/B week against no game (F23, P13) |
| P36 | **Pick elements for the need they serve. Competence elements carry most of the value** | Sailer, Hense, Mayr & Mandl 2017† *Computers in Human Behavior* (randomised experiment, B, abstract): badges, leaderboards and **performance graphs** raised competence need satisfaction and perceived task meaningfulness. Avatars, a meaningful story and **teammates** raised relatedness. No element raised autonomy. Mekler et al. 2017† *CHB* (online experiment, B, abstract): points, levels and leaderboards raised the *quantity* of work, not its quality, and didn't change intrinsic motivation. They worked as progress indicators. Hamari 2017† *CHB* (field, 1,410 vs. 1,579 users, each over a year, B; a pre/post cohort comparison, not randomised): badges for real actions raised activity in a trading service. Sailer & Homner 2020†: a story ("game fiction") and social interaction strengthened *behavioural* effects, and competition *combined with* collaboration was most effective | Use progress you can see toward *your own* goal (graphs, bars), badges that name a real achievement, and a light story where there's a real journey, such as the sleep shift. Add a teammate only for relatedness (your girlfriend). No generic points that mean nothing outside the app |
| P37 | **Controlling, comparative or punishing elements backfire** | Hanus & Fox 2015† (B): mandatory badges + a leaderboard lowered motivation and grades. The authors point to social comparison and lost autonomy. Diefenbach & Müssig 2019† (C): Habitica's punishments made people feel punished at the wrong times, for example in busy, productive weeks when they didn't log. Schultz 2007 (RESEARCH-TIME P30): comparison with others creates a boomerang. Silverman & Barasch 2023 (P33): broken streaks lower later engagement. Deci, Koestner & Ryan 1999 (P26): expected tangible rewards undermine interest | Everything is optional and compared only with your own past. Not logging must never cost anything: no dying pets, no wilting, no lost levels. Counts are cumulative, so they can't break. There's no leaderboard against other people. This strengthens P12, P33, W3 and D-019 |
| P38 | **Borrow the behavioural-economics mechanics that worked with real stakes: loss framing, endowed progress, goal gradient** | Patel et al. 2016† *Annals of Internal Medicine* (RCT, 281 adults, 13 weeks, B): money given up front and *lost* for each missed day raised goal achievement. The same amount framed as a daily *gain*, or as a lottery, did no better than feedback alone. No arm had a lasting effect after the incentive ended (university summary). Patel et al. 2019† STEP UP (RCT, 602 adults, A–B; RESEARCH-TIME P30): a game with points that start full and get lost, levels, and social incentives raised steps, and competition kept an effect after the game ended. Patel et al. 2017† BE FIT (RCT, 200 adults in 94 families, B, abstract): the same kind of game, played as families, raised activity, and the gains lasted 3 months after it ended. Nunes & Drèze 2006† *J. Consumer Research* (car-wash field experiment, 300 cards, B, secondary sources): both cards needed 8 more washes, but the one that came as a 10-stamp card with 2 stamps already filled was completed 34% of the time, vs. 19% for an empty 8-stamp card. Kivetz, Urminsky & Zheng 2006 (*J. Marketing Research*; from memory): effort speeds up as the goal gets closer (goal gradient) | The good activities for the week are **pre-loaded** and expire on Sunday ("use it or lose it"). That puts the loss frame on *missed good things*, never on you or on scrolling. A new goal starts with real progress already credited (e.g. the Sunday plan itself counts). Progress bars show "2 to go", not "3 of 5" |
| P39 | **The game must not become the next feed** | G6 (the hub ≤25 min/day) and D-019 (no streaks, no confetti). Feeds keep you with unpredictable rewards, the variable-ratio schedule from operant conditioning (Skinner; from memory). A self-study of a gamified self-care pet app reportedly found early support turning into reward-driven habit (seen only in a review blog, not verified). Finch, Forest and Opal publish no outcome data (see below) | **No random rewards**, no loot, no surprise drops, no notifications from the game, and no "come back to tend your garden" pings. The game is *seen* on a widget and in the weekly review, and *played* by doing things off the phone. Time spent on game screens counts toward G6 |
| P40 | **A calm, glanceable display of the good things can sustain them** | Consolvo et al. 2008† UbiFit Garden (CHI/UbiComp, 3-month field experiment, n = 28, C; secondary sources): a garden on the phone's wallpaper, with a flower type per activity type and butterflies for goals met. People with the display kept their activity level through the holiday weeks, while the group without it dropped significantly. Forest and Flora (below) use a growing tree, but without published outcomes. A 2026 virtual-pet study ("Brainy", AVI, 17 users, 10 days, C; search summary only) raised awareness of use, but didn't measure reduction | A home-screen widget that shows the last 7 days of G5 activities as a small garden. Variety shows as different flowers (P18), so balance is visible at a glance. It grows. It never dies or wilts. A quiet week just shows fewer flowers |
| P41 | **A habit grows like a curve, not a chain, and one miss barely dents it** | Lally et al. 2010† *EJSP* (96 people, 84 days, B; RESEARCH.md P11): automaticity rose along an asymptotic curve, reaching 95% of its plateau after 18–254 days (the long end extrapolated). **Missing one opportunity did not materially affect habit formation**; secondary write-ups put the drop at about 0.29 points on the automaticity scale. Buyalskaya et al. 2023† *PNAS* (30,000+ gym-goers, 3,000+ hospital workers, A): gym habits took months, handwashing weeks. Verplanken & Roy 2016† *J. Environmental Psychology* (800 people, B, abstract): after a move, people were more open to new habits for about 3 months, but the effect was small | Show habit progress as a slowly rising curve that a single miss barely moves. That's the honest model, and it can't "break" like a streak. A habit is tied to a context (a time and place), because that's what the curve measures. Life changes (a new week of the sleep plan, travel, a new job) are good moments to start one |

## How this fits with what's already decided

| Existing item | Effect | Why |
|---|---|---|
| D-019 "No streaks or confetti" | **Strengthened, and needs one clarification from you** | The evidence agrees: no streaks, no confetti, no random rewards (P37, P39). A calm garden widget (P40) is a display, not a hook, but it's closer to "game" than D-019 imagined. Your call |
| W3 no resetting streaks | **Strengthened** | Silverman & Barasch, Habitica (P37). Use cumulative counts instead |
| P26 no currency for good activities | **Holds** | Pre-loaded weekly quests (P38) are a loss frame on *doing good things*, not a currency you spend on scrolling |
| P30 a partner, no leaderboard | **Refined** | Collaboration plus a light competition is the strongest mix in the meta-analysis. A *shared* goal with your partner fits. A ranking against her doesn't (F42) |
| F13 / S5 sleep-shift plan | **Gets a story** | 6 steps toward 00:30 is already a quest with chapters (F44) |
| F27 weekly activity plan (RESEARCH-TIME) | **Becomes the game board** | The weekly quests (F39) *are* F27's planned activities, framed with a loss frame and endowed progress |

## What industry does with game elements

| App | Game element | Draws on | Outcome data |
|---|---|---|---|
| **Forest**† | A tree grows during a focus session and dies if you leave. Real trees get planted | Loss aversion (the company's framing); a punishing element (P37) | None. Only a 5-person usability test |
| **Flora**† | The same tree mechanic. Friends plant together and see who kills a tree. Optional **money pledge** when a tree dies | Loss aversion, social, a real stake | None found. Reviews only |
| **Hold**† | 10 points for every 20 phone-free minutes (07:00–23:00), redeemable for coffee or cinema; the timer resets if you leave | Tangible rewards (P26 risk) | None found. Press coverage, 2016–2019 |
| **Opal**† | Gems, focus score, streaks, a leaderboard | Points, competition | Self-reported survey only (RESEARCH-TIME) |
| **Roots**† | Streak with **cheat days**, a "balance score", custom icons | A repairable streak, which Silverman & Barasch support (P33) | "30%+ less screen time", a vendor claim |
| **Habitica** | An RPG of your to-do list: XP, gold, HP lost for missed dailies | Points, levels, punishment | Diefenbach & Müssig 2019†: counterproductive effects for every user studied |
| **Finch**† | A virtual pet bird that grows when you do self-care tasks | Relatedness, care | None peer-reviewed. App-store ratings only |
| **Duolingo** | Streaks, freezes, leagues | Streak + competition | Only conflicting second-hand churn numbers (RESEARCH-TIME) |

**Pattern:** the industry leans on punishment (dying trees, lost HP), raw-minute leaderboards and
random rewards. Those are exactly the elements P37 and P39 rule out. Nobody publishes outcome data.
The elements with evidence (competence feedback, loss-framed commitment, collaboration with one
person, a calm display) are cheap and rarely shipped.

## Ideas adapted from what others do

These take a known element and change it until it passes P37 and P39.

- **F38 Good-week garden (widget)**: A home-screen widget showing the last 7 days of G5 activities as
  flowers, one kind per category (move, people, read, build, rest). It grows and never dies. Tap it to
  open the week view. (P40, P36, P18)
- **F39 Weekly quests (use it or lose it)**: In the Sunday review, F27's 3–5 planned activities become
  this week's quests. They're pre-loaded on Monday and expire on Sunday. The plan itself counts as
  progress, so the week starts at 1 of 6. The bar says "2 to go". Unused quests vanish quietly with no
  penalty, and next week starts fresh. (P38, P28, P24)
- **F40 Cumulative milestones**: Badges that name real achievements and only count up, e.g. "10 bored
  opens turned into a walk", "First week with all five kinds", "20 nights in bed before plan" (total,
  not in a row). (P36, P37)
- **F41 Earned trust**: Meet your goals for 2 weeks and the gate offers to get lighter (shorter pause,
  bigger time box). You accept in the review. Slips quietly bring back the stricter settings. The
  reward is autonomy. (P36, P25; design reasoning)
- **F42 Co-op with your partner**: One shared weekly goal with your girlfriend (e.g. 2 walks together, 3
  phone-free dinners), with both of your ticks on one bar. Collaboration, no ranking. (P36, P30, P38)
- **F43 Personal best**: "Your best week for bedtime was 00:40 average. This week: 01:05 so far." It
  compares you only with yourself, with a goal-gradient bar. (P38, P24)
- **F44 Sleep-shift quest**: The 6-step sleep-shift plan (S5, F13) told as a short journey with
  chapters: "Chapter 3 of 6: wake at 09:00". A step that didn't hold repeats the chapter, with no
  failure screen. (P36 story, P38 goal gradient, P19, P20)
- **F45 Earn screen time** (Clearspace, Hold): out, see the gate. (P26)
- **F46 A pet or tree that suffers when you scroll** (Forest, Flora, Habitica): out, see the gate. (P37)
- **F47 Leaderboard with friends** (Opal, Jomo, Duolingo leagues): out, see the gate. (P37, P30)

## Our own ideas, built from the research

No app I checked does these. Each one starts from a finding and asks "what would a game element
look like if it were built *on* this?" The chain is shown as **finding → mechanic**.

- **F48 Roots, not streaks.** *Lally 2010: habits grow on a curve and one miss barely matters (P41).
  Silverman & Barasch: broken streaks make people quit (P33).* → Each habit you're building ("walk
  after dinner", "book in bed") shows **root depth**: an estimate of how automatic it has become,
  computed from Lally's curve (fast at first, slowing toward a plateau). A missed day takes off a
  sliver, not everything. The screen says something like "about 40% rooted; typical habits take 2–8
  months". It replaces the streak with the true shape of habit formation, so there's nothing to break.
- **F49 Comeback bloom.** *Milkman 2021: of 54 programmes, the best was a small bonus for coming back
  after one missed workout, which disappeared after a second miss (P26).* → When you skip one planned
  activity, a **comeback quest** appears for the next 48 hours. Doing it grows a special flower in the
  garden. A second miss means it quietly goes away. No penalty, no message about failing. It turns the
  slip, the moment people usually quit, into the most rewarding moment of the week.
- **F50 Left-early leaf.** *Lukoff 2018, Cho 2021: value drains within a session (P23). Rixen 2023:
  sessions almost never end by your own choice (P6).* → If you close a gated app **before** your time
  box ends, you get a leaf: the one game reward on the gated side, and it rewards *stopping*, the
  hardest act. To keep it from being farmed, it only counts against the default box for that purpose
  (F25), not one you just inflated.
- **F51 Calibration game.** *Allcott 2022: people keep underestimating their future use, by about 4%
  (P29). Mekler 2017: points push people to produce more of whatever is counted (P36).* → On Sunday
  you guess next week's unintended minutes and G5 hours. The score is **how close you were**, not how
  low. Because accuracy is the target, there's no incentive to fudge logs, and you learn how well you
  know yourself. (This replaces RESEARCH-TIME's F36.)
- **F52 Seasons.** *Howe 2016: novelty fades in about 6 weeks. Anderson/Vance: warnings that change
  appearance resist habituation (P32). Dai 2014: people start goals at temporal landmarks (P28).* →
  Each month is a season. On the 1st you pick a theme (e.g. "read season"), quests lean that way, and
  the garden's art changes. It refreshes the novelty every 4 weeks, before it fades, at a natural fresh
  start.
- **F53 Postcard to tomorrow-you.** *Rösch 2022: vividly imagining a specific, positive future makes
  choices more patient (g = .44, P29). Bed is your #1 trigger (BASELINE).* → At the night gate, you
  pick or write a one-line postcard: "Tomorrow at 8:30: coffee on the balcony, then a walk". It's on
  your morning screen. The postcards collect into a small album of mornings you looked forward to.
- **F54 Fresh-window starter.** *Verplanken & Roy 2016: after a big change, habits are open to
  revision for about 3 months. Dai 2014: Mondays and new months.* → When a new chapter of the sleep
  plan starts, a month begins, or you tap "something changed" (travel, new job), the app suggests
  attaching **one** new habit to that moment. It plants it in F48 with a context: "after the new wake
  time: 10-min walk".

**One idea we rejected on purpose.** *"Seeds for redirected urges"*: every bored open you turn into
an Instead action earns a seed. It sounds good, but it rewards *opening Facebook* (you need the open
to earn the seed). That's Goodhart's law: once a measure becomes a target, it stops being a good
measure. Rewards go on the activity done (F38, F39), never on the trigger. This becomes a gate rule
below.

## Scoring

The criteria are those from RESEARCH-TIME.md plus one new one, because the risk here is specific.
Weights are **proposed**.

| Criterion | Why it matters | Weight |
|---|---|---|
| Evidence | Does science say this element changes behaviour? (P*) | ×3 |
| Fit to your data | Does it hit your real patterns (bed, bored openings, the sleep shift)? | ×3 |
| Fills the freed time | Does it pull you toward G5 activities, not just away from feeds? | ×2 |
| Build effort (5 = easy) | You're solo. It builds on F27 and A5, which don't exist yet | ×2 |
| Low annoyance (5 = low) | Pressure and expiry can feel like nagging | ×1 |
| Low hook risk (5 = none) (new) | The game must not become a reason to open the phone (P39, G6, D-019) | ×2 |

Gate (must pass, not weighted): free, no risk to your accounts, **no punishment or shaming, no
currency spent on scrolling, no ranking against other people, no random rewards, and no reward for
opening a gated app**. F45, F46, F47 and the rejected "seeds" idea fail it. The other 14 pass. Max
score = 65. Ideas marked **(ours)** are the ones built from the research above, not from an existing app.

| # | Idea | Evidence ×3 | Fit ×3 | Fills time ×2 | Effort ×2 | Annoyance ×1 | Hook risk ×2 | **Weighted total** |
|---|---|---|---|---|---|---|---|---|
| F48 | **(ours)** Roots, not streaks | 4/5 Lally 2010 curve and "one miss doesn't matter"; Buyalskaya 2023; broken-streak studies. The meter itself is untested | 4/5 you need patience over months, and slips will happen | 4/5 every root is a G5 habit | 4/5 one formula + a context per habit | 5/5 it can't break | 5/5 it moves slowly, so there's nothing to check | **55/65 (85%)** |
| F49 | **(ours)** Comeback bloom | 4/5 Milkman 2021 megastudy: the best of 54 arms. That was money and gym visits, not a flower | 4/5 your self-discipline is weak now, so slips will come | 4/5 the comeback is a G5 activity | 5/5 one rule inside F39 | 4/5 kind; no failure message | 4/5 the 48-h window adds a little pull | **54/65 (83%)** |
| F39 | Weekly quests (use it or lose it) | 4/5 Patel 2016 loss framing; Nunes & Drèze endowed progress; STEP UP design | 4/5 turns F27's plan into something you can win | 5/5 every quest is a G5 activity | 4/5 a layer on top of F27 | 3/5 expiry can feel like pressure | 4/5 lives in the review and the widget; no pings | **53/65 (82%)** |
| F44 | Sleep-shift quest | 3/5 story strengthens behaviour (Sailer & Homner); goal gradient; P19/P20 for sleep itself | 5/5 sleep is your #1 problem, and S5 already has 6 steps | 3/5 sleep counts as rest | 4/5 a story skin on an existing plan | 4/5 repeated chapters, no failure screens | 5/5 nothing to check at night | **52/65 (80%)** |
| F53 | **(ours)** Postcard to tomorrow-you | 3/5 EFT meta-analysis g = .44, but from lab choices | 5/5 bed is your #1 trigger | 3/5 points the night toward sleep and a good morning | 4/5 one screen at the night gate + one in the morning | 4/5 optional; one line | 4/5 the album is mild collecting | **50/65 (77%)** |
| F38 | Good-week garden widget | 3/5 UbiFit (n = 28, 3 months); competence display (Sailer 2017) | 4/5 fits the cute look you chose (D-019) | 5/5 every flower is a G5 activity | 3/5 widget + art + A5 logging | 5/5 never dies | 4/5 a widget is glanceable, but could invite checking | **50/65 (77%)** |
| F42 | Co-op with your partner | 4/5 BE FIT families lasted 3 months; competition + collaboration best (Sailer & Homner); NUGU | 3/5 you said self first; she's already in your daily life | 5/5 the people + move categories, together | 3/5 sharing between two phones | 3/5 shared goals can create friction | 4/5 low, but it's social | **48/65 (74%)** |
| F43 | Personal best | 3/5 goal gradient; hard goals (Epton); self-referenced avoids the boomerang | 4/5 bedtime and unintended minutes are your metrics | 2/5 mostly measures the subtractive side | 4/5 one comparison per metric | 4/5 a bad week shows a gap | 5/5 nothing to tend | **47/65 (72%)** |
| F40 | Cumulative milestones | 3/5 Hamari 2017 field badges; competence (Sailer 2017); but Mekler: quantity, not motivation | 3/5 general | 3/5 several badges reward G5 actions | 5/5 counters + a list | 5/5 never lost | 4/5 collecting can turn into its own goal | **47/65 (72%)** |
| F51 | **(ours)** Calibration game | 3/5 Allcott projection bias; task-focused feedback (Kluger). The game itself is untested | 3/5 a weekly review add-on | 2/5 also asks you to guess your G5 hours | 5/5 two guesses + one score | 4/5 once a week | 5/5 nothing to check | **46/65 (71%)** |
| F54 | **(ours)** Fresh-window starter | 3/5 Verplanken & Roy (N = 800, small effect); Dai 2014 fresh start | 2/5 no big life change is planned, though the sleep chapters count | 4/5 every suggestion is a G5 habit | 4/5 Monday, month and chapter triggers + one button | 4/5 one suggestion at a time | 5/5 none | **45/65 (69%)** |
| F52 | **(ours)** Seasons | 3/5 polymorphic warnings, rotation (Kovacs), fresh start; rotation also cost retention | 3/5 general | 4/5 the theme steers quests toward a G5 category | 3/5 new art and themes every month | 4/5 a choice once a month | 4/5 new art is a mild pull | **44/65 (68%)** |
| F50 | **(ours)** Left-early leaf | 2/5 design reasoning from P23 and Rixen; no test | 5/5 your 60-min FB blocks at night are exactly this | 1/5 subtracts only | 4/5 one check when a gated session ends | 4/5 a small win | 3/5 the only reward linked to a gated app; farming risk | **41/65 (63%)** |
| F41 | Earned trust | 2/5 design reasoning; Kovacs 2021: people drift to easier settings | 4/5 your self-discipline is weak now, so being trusted later means something | 1/5 subtracts only | 3/5 adaptive gate settings | 4/5 it feels like a reward | 5/5 nothing to tend | **40/65 (62%)** |
| F45 | Earn screen time | ❌ fails the gate (P26) | — | — | — | — | — | — |
| F46 | A pet or tree that suffers | ❌ fails the gate (P37) | — | — | — | — | — | — |
| F47 | Leaderboard with friends | ❌ fails the gate (P37) | — | — | — | — | — | — |
| — | Seeds for redirected urges | ❌ fails the gate (it rewards opening a gated app) | — | — | — | — | — | — |

## What the table shows

- **Our own ideas take the top two places.** F48 (roots) and F49 (comeback) come straight from two
  findings no app uses: habits grow on a curve that one miss barely dents (Lally), and the moment
  after a slip is where a small reward works best (Milkman). Both fix the weak spot of every gamified
  app: what happens on a bad day.
- **The best layer is a combination, not one feature.** F39 (quests) is the board, F49 is a rule inside
  it, F48 replaces streaks, and F38 (garden) is the display. Together they cover competence, a loss
  frame on missed good things, and a kind response to slips.
- **The night ideas pair up.** F53 (postcard) is the playful version of RESEARCH-TIME's F31
  (tomorrow-you at night). Build them as one screen. F44 (sleep quest) belongs with the sleep plan.
- **F50 (left-early leaf) is the only reward on the gated side, and it scores low for that reason.**
  It's worth an n-of-1 test later, not a default.
- **Co-op (F42) has the best social evidence**, but it depends on your girlfriend wanting it. Ask her
  before building it.
- **Sensitivity.** F48 and F49 are the top two in every weighting tried. If hook risk counts only ×1,
  they tie (50/60). The same four (F48, F49, F39, F44) are on top in every weighting, except that F38
  ties F44 for fourth when "fills the freed time" counts ×3. F41 is always last.

## Recommendation

Build one small "good-week" layer:
1. **F39 weekly quests**, with **F49 comeback bloom** as a rule inside it. Needs F27 (the weekly
   activity plan).
2. **F48 roots** as the habit view, replacing any notion of a streak. Needs each habit stored with its
   context (time and place).
3. **F38 garden widget** as the display. Needs A5 (one-tap activity logging).

Then, when those parts of the app are next touched: **F53 + RESEARCH-TIME's F31** as one night-gate
screen, and **F44** on the sleep plan.

**Rules for every game element** (from P37, P39 and the rejected "seeds" idea): compare only with
yourself, count up and never reset, never punish a quiet week, no random rewards, no game
notifications, never reward opening a gated app, and game screens count toward G6.

**Measure it.** Run 2 weeks with the layer and 2 weeks without (F23). Compare G5 hours, unintended
minutes (G1) and time in the hub (G6). If the effect is gone by week 6, as with Pokémon GO, keep only
what still moves G5. Seasons (F52) are the planned answer to that decay if it shows up.
