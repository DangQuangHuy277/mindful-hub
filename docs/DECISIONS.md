# Decisions

Append-only. To change a decision, add a new entry that supersedes the old one.

## D-001: Facebook groups and pages are reached by gated direct visits (FB-1), with no ingestion
2026-10-17. **Decided.**

**Context.** Meta retired the Groups API on 2024-04-22. Scraping with your login breaks Facebook's
terms and risks the account. You said (interview Q4) you just want to read new posts at the end of
the day, when you're bored; it doesn't need to be instant.

**Options**
- **FB-1, gated direct visit.** The hub has a button for each group or page. It opens the exact URL,
  sorted by newest, inside a time box.
- **FB-2, capture while visiting.** A browser extension saves the posts you load so you can read them
  later in the hub. This is weaker now that you mostly use your phone.
- **FB-3, headless scraper with your login.** Fails the account-safety gate.

| Criterion (why it matters) | Weight | FB-1 gated visit | FB-2 capture |
|---|---|---|---|
| **Gate: account safety** (losing your FB account is unrecoverable) | must pass | ✅ a normal visit | ✅ low risk, but a terms grey area |
| Low temptation (the whole point) | ×3 | 3/5: inside FB, and Reels are one tap away | 4/5: you read in the calm hub |
| Information coverage (what drives FOMO) | ×2 | 4/5: an end-of-day read of new posts is exactly what you asked for | 4/5: only the posts you actually loaded |
| Maintenance (it has to work in month 6) | ×2 | 5/5: no parsing to break | 2/5: Facebook's page structure is obfuscated and changes often |
| Build effort (solo, Android-first) | ×1 | 5/5: links plus a timer | 1/5: browser extensions on Android are a weak path |
| **Weighted total** | | **32/40 (80%)** | **25/40 (63%)** |

**Decision.** FB-1, with **one evening "group time" window** (E7). Reels are blocked inside FB (E8).
**Revisit if** the weekly "missed anything?" answers (A4) keep naming group posts.

## D-002: No X API in v1; X is reached through gated links to your X Lists
2026-10-17. **Decided.** v1 must be free (interview Q5), and X reads now cost about $0.005 per
post. Barça talk and coding talk go into two X Lists. The hub links to them inside a time box.
Barça *news* comes from free Google News RSS instead.

## D-003: Platform is a native Android app first
2026-10-17. **Decided at the platform level** (interview Q1: Android, mostly on the phone).
Desktop feed-hiding (old E6) moves to Later. Stack details (Kotlin and Compose versus others, and
how app opens are intercepted) go to STACK-OPTIONS.md.

## D-004: The headline metric is unintended and regretted time, not total time
2026-10-17. **Decided** (interview Q8). Compulsive use predicts ill-being about 3× more strongly
than time spent (r = .29 vs .11, Cunningham 2021), and habitual use to pass the time is what feels
meaningless (Lukoff 2018). Total minutes stay on the dashboard as context and are never the goal.
See RESEARCH.md P14–P18.

## D-005: YouTube keeps working, but without a feed (superseded by D-008)
2026-10-17. **Decided** (interview round 2). You mostly watch game streamers, drama and
entertainment. Rest is a legitimate need (P18, P21). The *feed* is the problem (P4, P6, P14).
So:
- Turn watch history off, which makes YouTube disable the home feed (Z1).
- Autoplay off.
- Shorts blocked (E8).
- The gate asks you to name what you want to watch, plus a time box (E12).

Drama channels are left alone for now. If worth-it ratings for them come out low, the weekly review
can change that. Podcasts and coding talks are parked until later.

## D-006: Mobile Legends counts as rest, gated by matches and the clock, not a daily budget
2026-10-17. **Decided.** You play alone, to relax after work. Playtime itself shows no causal effect
on well-being (P21). Matches end on their own, and feeds don't. The two risks are the sleep window
(P19) and the rare slide into compulsive play (≈2–3%). So: "how many matches?" at the gate, a lock
during wind-down (S1), and your own worth-it data compared against feeds. "Games are better than
scrolling" is an inference, not a tested result.

## D-007: The sleep shift is gradual and anchored on wake time
2026-10-17. **Decided.** From about 02:00 → 09:45 to 00:00–00:30 → 08:00–08:30. The wake time moves
15 min earlier every 3 days, with morning light (P20). The same times hold on weekends, because
regularity predicts health better than duration (P19). Bedtime procrastination is the failure mode
the gate is built to catch (Kroese 2014). Melatonin and other medical options are out of scope;
ask a doctor about those if the plan stalls.

## D-008: YouTube keeps watch history and recommendations (supersedes D-005)
2026-10-18. **Decided** (your call). You value YouTube's personalization ("many good videos
come up"). Rules that feel too strict get abandoned. In Lyngs 2020, people found goal reminders
annoying and felt FOMO when the feed was removed, and autonomy matters (P12). So:
- History and recommendations stay on.
- Autoplay off.
- Shorts blocked.
- The gate asks for an intention and a time box (15/30/60 min).
- The home feed is hidden **only during the wind-down window** (S1).

**Revisit if** unintended YouTube minutes don't drop after 3–4 weeks (G1).

## D-009: Pull public FB and X content through hosted scrapers (Apify), keep gated links as fallback
2026-10-18. **Proposed. Waiting on you** (are the groups public? are you OK with the ~$5/month free cap?).

**Context.** You want the hub to be a real central place to read posts, like the Vietnamese
"Curiosity Pocket" side project, which scrapes X and news but not Meta. Facebook and X content was
out of reach under D-001 and D-002.

**Options**
- **C1, compliant only.** RSS and official free APIs, with FB and X reached through gated links (the
  current plan).
- **C2, hosted scrapers for public content.** Apify actors fetch public FB groups and pages and X
  Lists on a schedule. The phone pulls the datasets through Apify's API. Your accounts are never
  used. Apify's free plan gives $5/month of credit; the FB groups scraper costs $2.60 per 1k posts
  and X scrapers $0.15–0.40 per 1k tweets. **Private groups can't be fetched.**
- **C3, self-run logged-in scrapers.** Playwright with your FB cookies, or twscrape for X. ❌ Fails
  the account-safety gate (N2). twscrape needs real accounts, and its own docs and guides report
  suspensions.
- **C4, paid official X API.** ❌ Fails the "free v1" gate (about $30/month for 2 Lists).

| Criterion (why it matters) | Weight | C1 compliant only | C2 hosted scrapers |
|---|---|---|---|
| **Gate: no risk to your accounts** | must pass | ✅ | ✅ your accounts aren't involved |
| **Gate: $0 in v1** | must pass | ✅ | ✅ if volume stays under the $5 credit (it's tight, so it needs a cap) |
| Coverage of what you actually come for | ×3 | 2/5: no FB or X content in the hub | 4/5: public groups, pages and X Lists. Not private groups |
| Keeps you out of the feed apps | ×3 | 2/5: you still open FB and X, gated | 4/5: you read in the calm hub |
| Cost headroom | ×2 | 5/5: $0 | 3/5: the $5 credit is tight for 2 groups + 2 pages + 2 Lists |
| Reliability | ×2 | 5/5: links never break | 3/5: actors break when FB or X change; Apify maintains them |
| Build effort | ×1 | 5/5 | 4/5: one JSON API |
| **Weighted total** | | **37/55 (67%)** | **40/55 (73%)** |

**What the table shows.** C2 wins on what you actually want (FB and X content without opening
FB or X). C1 wins on robustness. The winner flips to C1 only if reliability counts ×3 and coverage
×2 (C1 40/60 vs C2 39/60). **Recommendation:** C2 for public sources, with C1's gated links kept as the fallback for
private groups and for when an actor breaks.

**Legal note (US case law, not legal advice).** Courts found logged-off scraping of *public* data
did not breach Meta's terms (Meta v. Bright Data, 2024). X's suit against Bright Data was
dismissed the same year. This is personal, non-commercial use, with no login involved.

## D-010: D-009 accepted in part: Apify for public pages and X; private groups get scheduled group time
2026-10-18. **Decided.** Your groups are **private**, so no safe scraper can reach them. You're
fine checking them briefly, "even just weekends or days Barça plays". So:
- **Public FB pages** (BarcaVietnamExpress, Coviahe) and **X** (Lists, or accounts until the Lists
  exist) come in through Apify (C2). The free plan has a hard cap of $5/month (`maxMonthlyUsageUsd = 5`,
  checked on the account). Every run also sets `maxTotalChargeUsd`.
- **Private groups** use gated group time (E7), by default **only on weekends and Barça match days**,
  20 minutes, opened sorted by newest.
- The Apify token lives in the gitignored `mindful-hub/.env`.

**Test run (2026-10-18), about $0.07 spent:**
- `apify/facebook-posts-scraper` returned text, time, media and link for both pages. Free-tier
  price: **$0.005 per post**.
- BarcaVietnamExpress posted **10 times in 3.2 h** during the Betis match (live score updates). Coviahe
  posts about **9 per day**.
- `apidojo/tweet-scraper` works ($0.0004 per tweet), but returned 10 when asked for 20.

**Budget plan (≈$2.70/month of the $5):**
- FB: 2 pages × the 5 newest posts × 1 evening run a day ≈ 300 posts ≈ $1.50.
- X: ≈100 tweets a day ≈ 3,000 ≈ $1.20.
- Live-match post bursts are skipped on purpose: scores come from the fixtures source, and a finite
  digest is the point (P6).

## D-011: X comes through twitterapi.io (proposed), and "For You" is replaced by our own recommender
2026-10-18. **Proposed. Waiting on you** (it costs about $1/month out of pocket, which breaks "v1 is $0").

**Findings that changed D-010.** On Apify's free plan, `apidojo/tweet-scraper` runs in **demo
mode**: at most 10 items per run, 5 runs a month, and no API use. Every test returned exactly 10.
`kaitoeasyapi` also restricts free users. So the free $5 Apify credit covers Facebook pages, **not X**.

**Can we get a recommendation feed?**
- No for Facebook's News Feed: there's no API.
- No for X's "For You": the official API doesn't offer it. Only the *Following* timeline, in time
  order, is available, at $0.005 per post. Getting "For You" would mean scraping with your login,
  which fails N2.
- So the hub builds its own, which the research favours anyway. Twitter's engagement-ranked
  timeline amplified out-group hostility, and users didn't prefer it. Ranking by users' *stated*
  preferences reduced that content (Milli et al. 2025, P22).

**Options for X**
- **X-A, twitterapi.io.** Pay-as-you-go, $0.15 per 1k tweets, an API key and no OAuth, free trial
  credit when you sign up. Covers users' recent tweets, search and lists. A third party, and
  scraping-based.
- **X-B, official X API.** $0.005 per post read. The only legal source of your Following timeline.
  About $22/month at 150 posts a day.
- **X-D, gated links to your X Lists.** $0, but you're inside the X app with "For You" one tap away.

Weights are proposed. Cost is ×3 because you've stressed "free".

| Criterion (why it matters) | Weight | X-A twitterapi.io | X-B official API | X-D gated links |
|---|---|---|---|---|
| **Gate: no risk to your accounts** | must pass | ✅ | ✅ | ✅ |
| Coverage | ×3 | 4/5: any public account, list or search; no "For You" | 5/5: adds your Following timeline | 2/5: nothing in the hub |
| Keeps you out of the X app | ×3 | 4/5 | 4/5 | 1/5 |
| Cost | ×3 | 4/5: ≈$0.70–1/month | 1/5: ≈$22/month | 5/5: $0 |
| Reliability | ×2 | 3/5: third party, could break or shut down | 5/5: official | 5/5 |
| Build effort | ×1 | 4/5: REST + key | 3/5: OAuth | 5/5 |
| **Weighted total** | | **46/60 (77%)** | **43/60 (72%)** | **39/60 (65%)** |

**What the table shows.** X-A wins mostly on cost. If cost only counts ×1, X-B wins (41 vs 38
out of 50). If reliability counts ×3, it's nearly a tie (49 vs 48 out of 65). **Recommendation:**
X-A, with X-D as the automatic fallback whenever X-A fails.

**Our recommender (C10)**
- **Candidates**: your seed accounts; accounts the seeds retweet, quote or reply to; topic searches
  ("La Masia", "Barça tactics", "Hibernate", "Claude Code").
- **Ranking**: your 👍/👎 and per-account scores. Engagement counts only filter out spam and are
  never shown.
- **Discovery slot**: 2 of the 15 digest items come from new accounts. Each week, the review
  suggests up to 3 accounts to add.

## D-012: Official APIs checked. Facebook: none usable. X: usable, and competitive if volume is kept small
2026-10-18. **Finding. The choice between X-A and X-B is waiting on you.**

**Facebook: no official route for a personal app.**
- Groups API: retired 2024-04-22.
- Reading Pages you don't admin needs the **Page Public Content Access** feature: App Review,
  **business verification**, and possibly extra contracts. A personal app wouldn't realistically
  get it.
- CrowdTangle shut down on 2024-08-14. Its replacement, the Meta Content Library, is only for
  researchers at qualified academic or non-profit institutions.
- So Facebook stays on Apify (public pages) plus group time (private groups).

**X: the official API works for us** (docs.x.com, checked today):
- $0.005 per post read and $0.010 per user lookup.
- The same post read twice in one UTC day is charged once.
- You can set a **maximum spend per billing cycle**.
- There's no free tier for new developers.

Volume is the lever:
- Fetch each account's timeline **excluding replies and retweets**.
- Use `since_id`, so only new posts are fetched.
- Fetch once a day, and leave very high-volume accounts out (e.g. @FabrizioRomano).

That should land around **30–50 posts a day ≈ $4.50–7.50 a month**. This is an estimate; measure it
in week 1.

**Re-scoring D-011's X-B at that volume** (cost 1 → 2/5): 15 + 12 + 6 + 10 + 3 = **46/60, tied with
X-A (46/60).** What breaks the tie is what you value:
- **X-B, official:** legitimate, reliable, and includes your Following timeline. About $5–8 a month,
  with a hard spend cap.
- **X-A, twitterapi.io:** about 30× cheaper (≈$0.20–1 a month), but a scraping-based third party that
  could break or disappear.

## D-013: Crawling ourselves: not for X; maybe later for public FB pages
2026-10-18. **Finding, plus a recommendation.**

**Tests run today, logged out with plain HTTP:**
- X's embed endpoint `syndication.twitter.com/srv/timeline-profile` returned **429** (twice).
- `x.com/<user>` returned only the JavaScript shell, with no posts.
- Facebook `m.` and `mbasic.` returned **400**.
- Facebook's page plugin (`/plugins/page.php`) returned 200, but its posts are rendered by
  JavaScript, so there was no post text in the HTML.

So any self-built crawler would need a headless browser. I didn't run that test: installing
Playwright and Chromium was blocked by this session's permission check, so it waits for your OK.

**Options**
- **X, logged out:** anonymous access was shut down (guest tokens are gone, and the public Nitter
  instances are gone). Not viable.
- **X or FB, logged in with your account:** ❌ fails N2.
- **X or FB with a second "reader" account:** breaks the platforms' rules against fake and duplicate
  accounts. Guides report such accounts get suspended within days to weeks, and the scrapers break
  every 2–4 weeks. Not recommended.
- **FB public pages, headless browser, logged out:** possibly viable, untested. It's free but
  high-maintenance. Worth trying **only if** Apify's free credit stops covering the pages.
- **On-device capture:** the app saves posts you actually see during group time. No extra requests
  and no account risk, but it only captures what you scrolled past. Low value.

**Recommendation:** don't crawl X ourselves. Use the official X API or twitterapi.io (D-012). For FB
pages, keep Apify for now, with a headless crawler as plan B.

## D-014: No logged-in crawling. Facebook stays Apify + group time. No ongoing free X read API exists
2026-10-18. **Facebook: decided (your call). X: waiting on you.**

**Facebook.** No logged-in crawl. Public pages come through Apify (free credit). Private groups
get group time on weekends and match days (D-010).

**X free-tier check (2026-10-18)**

| Route | Free? | Checked |
|---|---|---|
| Official X API | No ongoing free reads. **$20 free credit after your first credit purchase**, plus a match of your first auto-recharge up to $50. Your own posts, bookmarks and owned lists cost $0.001 each ("owned reads") | docs.x.com |
| twitterapi.io | Trial credit on signup (amount not stated), then $0.15 per 1k tweets | their pricing page |
| SocialCrawl | 100 credits, one time | their site |
| Apify X actors (`tweet-scraper`, `twitter-scraper-lite`) | Demo mode only for free users: 10 items per run, 5 runs a month | actor pages + our own runs |
| Nitter mirrors | Dead: xcancel 451, nitter.net 410, others unreachable | tested today |

**Practical reading.** The official API's $20 bonus covers about 4,000 posts, which is **≈2–3
months** at 30–50 posts a day. After that it's about $5–8 a month, unless you switch to twitterapi.io
(about $1 a month). Bookmarks at $0.001 could also feed the read-later queue (C7): bookmark on your
phone, read in the hub.

## D-015: v1 costs $0. X stays as gated links to your Lists; the X API waits until v1 proves useful
2026-10-18. **Decided (your call).** Free is a hard requirement for v1. Paying is reconsidered
only after v1 is built and has proven useful. This decides D-011, D-012 and D-014 for v1:
- **X-D:** X is reached through gated links to your "Barça" and "Coding" X Lists. You state an
  intention and pick a time box (E1–E3). There's no X content in the hub.
- **Facebook:** public pages through Apify's free credit (D-010), private groups in group time.
- **Later**, if v1 earns it: the official X API (start with the $20 bonus credit) or twitterapi.io.
  The X fetcher is designed as a swappable module, so adding it doesn't need a redesign.

**Re-scoring F19 (fetched digest).** Without X, its fit drops from 5 to 4. New score: 3·3 + 4·3 +
3·2 + 4·1 = **31/45 → v1.1**. v1.0 keeps F17, the calm launcher with links, so the central screen
still exists.

## D-016: Facebook first. It gets the strict rules; X gets a lighter gate
2026-10-18. **Decided (your call, backed by your data).** You see X as less harmful than Facebook.
No study compares the two head to head, but your usage supports putting Facebook first: FB
1.7 h/day and 25 self-started opens a day versus X 0.6 h/day, plus 2 h 24 m of FB after midnight.
The causal harm evidence is also Facebook-specific (Allcott 2020, Braghieri 2022). So in v1.0:
- **Facebook, Instagram, Threads:** full gate (E1–E5), daily budget, escalating delay, wind-down
  lock, Reels blocked.
- **X:** intention + time box only, with no budget at first. Tightened only if the weekly review
  shows a lot of unintended X time.

Stack choices are proposed in STACK-OPTIONS.md (S1–S9) and still wait on you.

## D-017: Stack chosen
2026-10-18. **Decided.** You accepted the STACK-OPTIONS.md recommendations and you're fine with Kotlin:
- Kotlin + Jetpack Compose, with a pure-Kotlin `:core` module so it's ready for KMP later.
- AccessibilityService gate, with UsageStatsManager as the logger and cross-check.
- No server in v1.0. On-device fetching (C1) in v1.1; the Cloudflare Workers trigger is in S3.
- Room + DataStore, manual DI, Vico, `adb install`, Auto Backup.

Architecture: ARCHITECTURE.md.

## D-018: The usage screen leads with findings in words, and views are switchable
2026-10-18. **Decided (your call).** You found the hour × day heatmap hard to learn from. The
research agrees on why:
- **Shades are the weakest encoding.** Color saturation ranks near the bottom of Cleveland &
  McGill's accuracy ranking (1984); position and length on a shared baseline rank at the top.
- **Charts alone rarely lead to reflection.** It has to be prompted (Choe 2017). In a CHI 2024
  study (Strömel, n = 273), written descriptions next to charts led to more reflection than charts alone.
- **Plain cost framing works but stresses.** In TimeAware (Kim 2016), only the distraction-framed
  version improved behavior, and it raised stress. So a win is always stated as well.

Six options were scored (A insight cards 90%, D week trend 83%, F app scoreboard 82%, C hour bars
77%, B day timeline 70%, E fix the heatmap 48%). **Chosen: A + D + C** as the default "Insights"
view:
- 3–6 findings in plain sentences (`:core` `UsageInsights`): after-bedtime minutes, the first app
  after waking, the week-over-week change, the longest session, the peak hour, today's pause wins.
- Per-day bars, with the after-bedtime part on the baseline and last week as an outline.
- Average minutes per hour, with the bedtime band shaded.
- Gated apps are counted by default; "All apps" and single apps are filters.

**Revised the same day (your call):** the charts come first and the text second, because you read
the charts faster. Both charts are stacked by app: the top 5 apps plus "Other", in fixed colors per
app (Facebook is always blue). Tapping an app in either legend shows only that app in both charts.
Color now means the app, so before/after bedtime is shown by the shaded band and in the tap
details instead of by bar color.

**Views are switchable.** Each view implements `UsageView` and is listed in `UsageViews.all`;
the tab row shows whatever is registered. The heatmap stays as the second view. B (day timeline)
and F (scoreboard, once budgets exist in F04) are candidates for later views.
