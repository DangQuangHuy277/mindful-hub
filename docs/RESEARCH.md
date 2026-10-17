# Research

This is the evidence behind every requirement. Each principle has an ID (`P*`), and
[REQUIREMENTS.md](REQUIREMENTS.md) cites these IDs in its "Why" column.

Sources marked † were checked online on 2026-10-17, including the authors, venue, sample size and
headline numbers. Unmarked sources are well-established results cited from memory. Check an
unmarked source before you rely on it for anything that matters.

Evidence strength:
- **A**: a meta-analysis, a large RCT, or a large causal study.
- **B**: a single field experiment or a mid-sized RCT.
- **C**: a small HCI study, a qualitative study, or a design framework.

## Read this first: what the science does *not* promise

- **Average effects are small to moderate, and the experts still argue about them.** Quitting
  Facebook for 4 weeks raised subjective well-being, but by a modest amount (Allcott et al. 2020†,
  A). A pre-registered meta-analysis of 27 social-media reduction experiments found d = 0.088, and
  its CI barely included 0 (Ferguson 2024†). Other researchers contest it because it mixes 3-week
  field studies with 5-minute lab studies. A 2025 meta-analysis of 10 RCTs (N = 1,491) found that
  reducing social media lowered depressive symptoms, with g = 0.25 [0.10, 0.41] after correcting
  for publication bias (May, Malouff & Meynadier 2025†).
- **Published nudge effects are inflated.** Academic papers report nudge effects of 8.7 pp. Nudge
  units running the same kind of trial at scale got 1.4 pp (DellaVigna & Linos 2022†, A). Selective
  publication explains about 70% of the gap.
- **"Passive scrolling is bad, posting is good" is not established.** A review of 40 studies and 172
  associations found that most did not support that split (Valkenburg et al. 2022†, A–B).
- **"Willpower is a muscle that runs out" failed replication.** A 23-lab replication found
  d = 0.04, with a CI that includes 0 (Hagger et al. 2016†, A). Don't design around "save your
  willpower".

**What this means for the app:** expect smaller effects than the papers report, and measure
whether each feature works for *you* (P13). The app's own data outranks any single paper.

## Principles

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P1 | **Change the situation; don't rely on willpower** | Duckworth, Milkman & Laibson 2018† *PSPI* review (A): strategies that change the situation (obstacles, removing cues, commitments) are more reliable than willpower. Hofmann et al. 2012† (205 adults, 7,827 experience-sampling reports, B): people high in self-control report *fewer* temptations, because they avoid them | Defaults and friction decided in advance, not resisted in the moment. This matters most because your self-discipline is weak right now |
| P2 | **Self-control problems are real, and people underestimate them** | Allcott, Gentzkow & Song 2022† *AER* "Digital Addiction" (RCT, A): self-control problems cause **31%** of social media use, and letting people set limits on future use substantially reduced use. Temporary incentives had persistent effects, so use is habit-forming | Let calm-you precommit to limits that bind tempted-you. Make limits easy to set and hard to undo on impulse |
| P3 | **Friction at the moment of opening works, but wears off** | Grüning, Riedel & Lorenz-Spreen 2023† *PNAS* (280 users, 6 weeks, B): a pop-up with a short wait and a reflection message led users to dismiss **36%** of opens, and users tried to open target apps **37%** less often. Meinhardt et al. 2026† *IMWUT* (104 users, 7 days, B): pop-up effectiveness decayed quickly, and for high-impulsivity users an explicit pop-up worked better than subtle gradual friction | Intercept opens of the bad apps with an intention prompt and a delay. Vary or escalate it to fight habituation, and keep it explicit rather than subtle |
| P4 | **Remove the feed but keep the information** | Lyngs et al. 2020† CHI (58 students, 6 weeks, C): removing the Facebook newsfeed and showing goal reminders both helped people stay on task. The costs were FOMO (newsfeed removed) and annoyance (reminders). Allcott et al. 2020† (A): deactivation also *reduced news knowledge* | Block the feed, but deliver what you actually came for somewhere calm. Otherwise FOMO pulls you back |
| P5 | **Batch updates; don't stream them, and don't cut them to zero** | Fitz et al. 2019† *Computers in Human Behavior* (237 people, field RCT, B): notifications batched **3×/day** improved attention, mood, productivity and sense of control. **No notifications at all** brought few of those benefits and raised anxiety and FOMO | 2–3 scheduled digests a day with nothing in between, plus a predictable "next digest at 18:00" so you don't feel cut off |
| P6 | **Scroll loops end because of the outside world, rarely the app** | Rixen et al. 2023† MobileHCI (46 people, 1 week, C): most reasons to stop a session came from outside the app. They describe inner loops (within a session) nested in an outer, habitual loop | A built-in end (the feed is finite and says "done") plus time boxes for the inner loop, and trigger planning for the outer loop (P9) |
| P7 | **Engagement ranking amplifies negativity and outrage** | Robertson et al. 2023† *Nature Human Behaviour* (~105k headline variants from Upworthy A/B tests, A): each negative word raised click-through by **2.3%**. Rathje, Van Bavel & van der Linden 2021† *PNAS* (2.7M posts, A): each word about the political out-group raised the odds of a share by **67%** | Never rank by engagement. Rank by *your* stated interests, down-weight outrage, optionally rewrite headlines neutrally, and show no like or share counts |
| P8 | **Personal contact with close people is the good part** | Burke & Kraut 2016† *JCMC* (1,910 users, longitudinal, B): well-being rose with *composed, targeted* messages from *strong ties*. Broadcast viewing and one-click likes showed no such benefit. Caveat: see Valkenburg 2022 above | Keep messaging and real participation easy, and gate broadcast browsing |
| P9 | **If-then plans** | Gollwitzer & Sheeran 2006† (94 tests, A): implementation intentions ("If situation X, then I do Y") had **d = 0.65** on goal attainment | Guided setup of if-then plans for your real triggers (bed, toilet, waiting, a break while coding), shown at the moment of the trigger |
| P10 | **Track progress, record it, report it** | Harkin et al. 2016† *Psychological Bulletin* meta-analysis (A): monitoring goal progress **d = 0.40**. The effect was larger when progress was physically recorded and reported (d = 0.47) than when it was unreported (d = 0.19) | A weekly review that is written down, and an optional accountability partner who sees a summary |
| P11 | **Habits take weeks to months, and the time varies a lot** | Lally et al. 2010† *EJSP* (96 participants, B): median **66 days** to automaticity, with a range of **18–254** days | Judge the app over 8–12 weeks, not 1. Never promise "21 days". Expect relapses and design for them |
| P12 | **Support autonomy, competence and relatedness (self-determination theory)** | Ryan & Deci 2000 (A); Peters, Calvo & Ryan 2018† *Frontiers in Psychology*, the METUX design framework (C) | You write the rules (autonomy). No shaming, no punishing streaks. Show real progress (competence) and keep human connection (relatedness) |
| P13 | **Expect small effects and measure them on yourself** | See "Read this first" above. Single-person (n-of-1) experiments, such as alternating A and B weeks, are a standard method for personal decisions | Record a baseline first. Let you run 1–2 week A/B experiments on features, and show honest charts with uncertainty |

## Causal evidence that the harm is real

- Allcott, Braghieri, Eichmeyer & Gentzkow 2020† *AER* 110(3): a 4-week Facebook deactivation RCT.
  It ↑ subjective well-being and ↑ offline time with family and friends, ↓ news knowledge and ↓
  polarization. It also caused a *large persistent reduction* in Facebook use after the experiment
  ended, which suggests habit, not value, was holding people there.
- Braghieri, Levy & Makarin 2022† *AER* 112(11): Facebook's staggered rollout across US colleges
  raised reported hopelessness by about 0.1 SD. The authors' evidence points to unfavourable social
  comparison as the mechanism.
- Hunt et al. 2018† *J. Social & Clinical Psychology* (143 students, 3 weeks): limiting social media
  to 30 min/day reduced loneliness and depression. It is a small study, so treat it as supportive,
  not decisive.

## Is less screen time good? Is more reading or coding better?

This answers interview Q8. These principles (P14–P18) decide **what the app measures and
celebrates**.

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P14 | **Total minutes are a weak signal. Compulsion and loss of control are the strong one** | Orben & Przybylski 2019† *Nature Human Behaviour* (~355k adolescents, A): technology use explains **at most 0.4%** of the variance in well-being. Cunningham, Hudson & Harkness 2021† meta-analysis (62 studies, 451k people, A): depressive symptoms correlate **r = .29 with *problematic* use**, but only **r = .11 with time spent**. Lukoff et al. 2018† *IMWUT* (86,402 logged sessions, B): sessions driven by *habit, to pass the time* and passive social media felt the least meaningful, and people described a loss of autonomy | The headline metric is **unintended and regretted time**, not total minutes. Total minutes are shown only as context |
| P15 | **Cutting back helps on average, and limiting works at least as well as quitting** | May et al. 2025† (10 RCTs, A): limiting use g = **0.33**, full abstinence g = 0.15 (not significant). The difference between the two wasn't significant either. Castelo et al. 2025† *PNAS Nexus* (467 randomized, crossover, B): 2 weeks with mobile internet blocked cut screen time from **314 to 161 min/day** and improved well-being (dz = 0.45), mental health (dz = 0.56) and sustained attention (dz = 0.23). Caveats: only 25.5% fully complied (the intention-to-treat results still held), participants were motivated volunteers, and there was no blinding | Aim for *less and intentional*, not zero. Castelo's baseline (5.2 h/day) is close to yours, so it is a realistic reference point |
| P16 | **What the time replaces matters as much as the time itself** | Castelo 2025†: freed time went to in-person socializing, exercise, nature and hobbies. Allcott 2020†: more offline time with family and friends. **Sleep**: He et al. 2020† *PLoS ONE* pilot RCT (n = 38, C): no phone in the 30 min before bed for 4 weeks shortened time to fall asleep, lengthened sleep, and improved mood and working memory. **Exercise**: Singh et al. 2023† *BJSM* umbrella review (1,039 trials, A): medium effects on depression (−0.43) and anxiety (−0.42). **Walking**: Bratman et al. 2015† *PNAS* (n = 38, B): a 90-min nature walk reduced rumination. Oppezzo & Schwartz 2014† (n = 176, B): walking increased creative idea generation | Protect sleep first (bed is your #1 trigger). Track what the time goes to, not just what it comes from |
| P17 | **Games: playtime matters less than why you play** | Johannes, Vuorre, Magnusson & Przybylski 2022† *Royal Society Open Science* (38,935 players, 6 weeks of telemetry from game publishers, A): little to no causal link between playtime and well-being, and motivation played a role. From memory, to verify before quoting: feeling you *have to* play (daily quests, rank decay) was linked to lower well-being | Don't budget games like feeds. Ask "how many matches, and do I want to?", and keep games out of bedtime |
| P18 | **More of a "good" activity isn't automatically better** | Curran et al. 2015† meta-analysis on passion (94 studies, A): *harmonious* passion (chosen freely, fits with the rest of life) goes with positive affect, flow and performance. *Obsessive* passion (feeling compelled) adds negative affect and rumination. Reading fiction: Dodell-Feder & Tamir 2018† meta-analysis: small gain in social cognition, g ≈ .15. Recovery research (Sonnentag; from memory): detaching mentally from work in your free time predicts well-being | Celebrate variety and balance (move, people, read, build, rest), not "max productive hours". Doing nothing on a walk counts |

## Sleep and games (interview round 2)

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P19 | **Regular sleep, and a bed that's for sleep** | Windred et al. 2023† *Sleep* (60,977 UK Biobank participants, accelerometers, A): sleep *regularity* predicted all-cause mortality better than sleep *duration*. The top four regularity quintiles had a 20–48% lower risk than the bottom one. Kroese et al. 2014† *Frontiers* (N = 177, B): **bedtime procrastination** (going to bed late with nothing forcing it) goes with low self-regulation and insufficient sleep. Stimulus-control meta-analysis 2023† *J Sleep Research* (PMID 37496454, A): "use the bed only for sleep" improved sleep compared with passive controls, but barely compared with active treatments | Same wake time every day, weekends too. The phone stays out of bed. A wind-down period where bedtime procrastination usually happens |
| P20 | **Shift the body clock gradually, and use morning light** | Burgess, Eastman et al. 2003† *J Biological Rhythms* (lab study, B): moving sleep 1 h/day earlier for 3 days advanced the body clock 0.6 h in dim light, 1.5 h with intermittent bright light and 2.1 h with continuous bright morning light. Real life has less light and less control, so go slower: 15–30 min every 2–3 days (a conservative assumption, not a tested number) | A sleep-shift plan that moves wake and bed times step by step, plus a "get outside light" prompt after waking |
| P21 | **Games vs feeds: games are probably the better rest, but that's inferred, not tested head to head** | Johannes, Vuorre & Przybylski 2021† *RSOS* (telemetry from EA and Nintendo games, B): a *small positive* relationship between playtime and well-being, with need satisfaction and motivation related to well-being independently of playtime. Johannes et al. 2022† (38,935 players): no causal effect of playtime either way. For feeds, by contrast: problematic social media use r = .29 with depression (P14), and passive social media was rated meaningless (Lukoff 2018). **Design reasoning, not a study:** a match has a natural end, a feed doesn't (P6). **Risk:** gaming disorder affects about 2–3% of people (Stevens et al. 2021† meta-analysis, 53 studies, 226k people: 3.05%, or 1.96% in the best-sampled studies) | Count games as "rest" in G5. Gate them by *matches* and *time of day*, not by a daily budget. Keep them out of the sleep window. Compare worth-it ratings for games and feeds in your own data (P13) |

## Ranking (added 2026-10-18)

| ID | Principle | Evidence (strength) | What it requires from the app |
|---|---|---|---|
| P22 | **Rank by what you say you value, not by what grabs you** | Milli, Carroll, Wang, Pandey, Zhao & Dragan 2025† *PNAS Nexus* (N = 806 Twitter users, pre-registered audit, Feb 2023, B): compared with a time-ordered feed, Twitter's engagement ranking amplified emotional, out-group-hostile content that made users feel worse. Users did **not** prefer the algorithm's political picks. Ranking by *stated* preferences reduced angry and hostile content, but risked reinforcing views users already held | The hub's recommender learns from explicit 👍/👎 and topics you choose. It keeps a discovery slot so it doesn't become an echo chamber |

## Data-source findings (checked 2026-10-17)

| Source | Status | Notes |
|---|---|---|
| Facebook groups | **No API.** Meta retired the Groups API on 2024-04-22† | Reading group posts needs your logged-in browser. Scraping breaks Facebook's terms and can get your account checkpointed or banned |
| Facebook public groups (hosted) | Apify's official Facebook Groups Scraper† covers **public groups only**, needs no login, and costs **$2.60 per 1k posts**. The Apify free plan gives **$5/month** of credit, no card needed† | Private groups still need your login, so they stay gated links (D-009) |
| Facebook public pages (hosted) | ✅ Tested 2026-10-18: `apify/facebook-posts-scraper` returns text, time, media, link and engagement fields (engagement is hidden per W2). Free-tier price **$0.005 per post** | BarcaVietnamExpress posts in bursts during matches (10 posts in 3.2 h) |
| X via Apify, free plan | ❌ Checked 2026-10-18: `apidojo/tweet-scraper` is **demo mode** for free users (10 items per run, 5 runs a month, no API). `kaitoeasyapi` also restricts free users | X needs a different provider (D-011) |
| X via twitterapi.io | **$0.15 per 1k tweets**, pay-as-you-go, `x-api-key` header, free trial credit on signup (amount not stated) | Not tested yet. Needs your signup |
| X (hosted) | Apify X scrapers, including list scrapers†, cost **$0.15–0.40 per 1k tweets** | Third-party scrapers break when X changes. Public Nitter instances are gone (2026) |
| X (self-run) | twscrape still works but needs real X accounts. Guides report accounts lasting days to weeks before suspension (vendor claims, unverified) | Fails N2 |
| Facebook Pages (official) | The Graph API's **Page Public Content Access** needs App Review, business verification and possibly contracts†. The Meta Content Library is for academic and non-profit researchers only†. CrowdTangle shut down 2024-08-14† | No official route for a personal app (D-012) |
| Facebook Pages | The API needs app review | Same constraint in practice. Pages that also post to a website or other platforms can be followed there instead |
| X (Twitter) | Pay-per-use since 2026-02-06†: **$0.005 per post read**, $0.010 per user lookup. **Same post in one UTC day is charged once.** You can set a spend cap. No free tier (docs.x.com, checked 2026-10-18) | This comes from third-party pricing summaries. Confirm in the X Developer Console before relying on it. 200 posts/day ≈ $30/month |
| Google Trends Vietnam | ✅ `trends.google.com/trending/rss?geo=VN` returned 200 | Returned real trending items today, e.g. "giá xăng" and "lịch bóng đá hôm nay" |
| VnExpress | ✅ `vnexpress.net/rss/*.rss` returned 200 | Mainstream Vietnamese news |
| Hacker News | ✅ Firebase API and Algolia API both returned 200 | No key needed |
| Substack | One RSS feed per publication (`<pub>.substack.com/feed`) | Not probed yet |
| Football (Barça) news | ✅ Google News RSS (`news.google.com/rss/search?q=Barcelona+when:1d&hl=vi&gl=VN&ceid=VN:vi`) returned Vietnamese Barça news | Free, no key |
| Football fixtures and results | football-data.org returned 403 without a token | A free tier needs a registered key. Not checked yet |
| YouTube home feed | ✅ YouTube's own feature (2023†): with **watch history off** and no significant past history, the home feed is disabled. Search, subscriptions and the channels you follow still work | Removes the feed for free, with no code (P4) |
| Android usage history | ✅ Checked on the phone (Android 15, One UI 7): `dumpsys usagestats` gives **24 h of events** (app resumed and paused, unlock, notification interruption) plus weekly, monthly and yearly totals per app | Longer hour-by-hour history needs the app's own logger. See BASELINE.md |
| Facebook group sorted by newest | `?sorting_setting=CHRONOLOGICAL` redirects to login when not logged in | Check on your phone while logged in |
