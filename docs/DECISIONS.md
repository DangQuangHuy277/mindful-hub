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

## D-019: Theme: peach and lavender on white
2026-10-18. **Decided (your call).** You wanted the app cuter and brighter, with white as the main
color, not gray. Option A of three mockups (peach and lavender, matcha, moonlight), made always
light:
- **White background and white chart cards** with a soft peach outline. The app colors in the
  charts were tuned for white; on a peach card, yellow and gray drop to about 2:1.
- **Pastel cards for text only:** peach for the gate, lavender for the findings.
- **Readable accents.** The pastel peach is too faint for button text on white (2.9:1), so buttons
  use a deeper terracotta (5.0:1). Muted text is 5.6:1.
- **Softer shapes:** rounder cards (24 dp) and pill-shaped chips. A greeting that changes with the
  hour says "It's late. Sleep well" after midnight.
- **The gate overlay uses the same theme** (`theme/MindfulTheme.kt` in `:gate`), so pauses look
  like the app.
- **The phone's dark mode is ignored for now.** Charts read the theme, not the system
  (`isDarkTheme()`), so adding a dark option later is a theme change only.
- **No streaks or confetti:** an anti-doomscroll app shouldn't become an attention hook itself.

**Revised the same day (your call): blue replaces peach.** You prefer blue. The accent is
`#2766C2` (white text on it 5.6:1), pale blue `#E3F0FF` replaces the peach cards, and the warm
neutrals (outlines, the bedtime band) are now cool blue-grays. Lavender and mint stay. The app has
a mascot, a calm blue sprout with closed eyes: it's the launcher icon (adaptive, with a one-color
themed version) and it sits on top of the pause screen. Buttons are soft pills with an emoji and a
small squish when pressed. "Never mind" is the only solid blue button on the pause screen, and
"Continue anyway" stays a plain text link, so leaving always looks more inviting than staying.

## D-020: The gate steps aside for banking apps
2026-10-19. **Decided (your request).** Techcombank and other Vietnamese banking apps refuse to
run while a sideloaded accessibility service is on, so using the bank meant switching the gate
off in Settings and back on afterwards.
- **The gate switches itself off** (`disableSelf()`) when an app in `GateSettings.pauseFor` comes
  to the front: Techcombank, Vietcombank, BIDV, MoMo, ZaloPay, VNPAY, TCBS, VCBF.
- **A small watcher switches it back on.** `GatePauseService` (a foreground service with a "Gate
  paused for Techcombank" notification) reads usage events once a second. The gate comes back at
  once if you open a gated app, after 2 s on the home screen, or after 60 s in any other app (so
  the bank's own camera or OTP hop doesn't trip its check when you return). Locking the phone
  inside the bank keeps the pause.
- **Switching back on needs `WRITE_SECURE_SETTINGS`**, which Android only grants over adb, once.
  Without it the gate still steps aside, and a notification takes you to Settings to turn it on.
  With it, the "Gate is off" card turns the gate on in one tap.
- **Tested on the S22 (2026-10-19):** the gate switches off 26 ms after the bank's window event
  and back on about 3 s after you reach the home screen. Facebook is prompted normally afterwards.
- **Techcombank also refuses USB debugging** ("This device is in Android Debug Bridge mode"), even
  with the gate fully off, so the bank side can't be tested over adb.

**Revised the same day: bank shortcuts, because the first open closed Techcombank.** You saw
Techcombank close itself on the first open (the gate then switched off, so the second open
worked). It checks as its process starts, and the gate only hears about it from the bank's first
window, about 600 ms later (VNPAY cold start on the S22: process at +0.03 s, window event at
+0.64 s). Nothing earlier is visible: One UI's launcher sends no click event for icon taps, and
the system splash window isn't in the accessibility window list (both checked on the device). So:
- **Mindful Hub's icon now carries the banks as shortcuts** (long-press it: Techcombank, VCB
  Digibank, BIDV SmartBanking, MoMo first; more on One UI's full list). Drag one to the home screen
  in place of the bank's own icon. Tapping it switches the gate off, waits until Android has
  unbound it, then starts the bank (measured: gate off 130 ms before the bank's process started).
- **Opening a bank any other way** (its own icon, recents, a notification) still pauses on the
  window event as before, so a cold start of Techcombank can still close once.
- If the shortcut starts the bank but the bank never comes to the front, the watcher turns the
  gate back on after 15 s.
- **Checked against Techcombank (2026-10-19, logs from your test):** opened from its own icon, its
  process started at 01:21:07.42 (Samsung starts it on touch), the gate paused at 08.34, and
  Techcombank finished its task and killed its own process at 08.50. Opened through the shortcut,
  the gate was off before the process started, and Techcombank stayed open. It got as far as its
  separate USB-debugging check, which it only shows once the accessibility check has passed.
- **Dragging the shortcut out of the long-press menu was easy to miss.** In your next test,
  all three opens still came from Techcombank's own icon (folder, suggested apps row), and no
  Mindful Hub shortcut was pinned. The usage screen now has a card, "Open banks with the gate
  already off", with one "Add" button per bank you used this week. It uses Android's pin request
  and goes away once each shortcut is on the home screen. One UI marks pinned shortcuts with
  Mindful Hub's sprout, so they're easy to tell from the bank's own icon.
- **Why Techcombank's own icon worked after you reinstalled it:** the race flipped. Reinstalled
  at 01:40:50, it runs uncompiled (`verify`, before: `speed-profile`), so it starts slower. Times
  are from its process start on a tap of its own icon:

  | | Bank `onCreate` | Gate off | Result |
  |---|---|---|---|
  | Before reinstall (01:21) | +0.63 s | +0.92 s | killed itself at +1.08 s |
  | After reinstall (01:45, 01:46) | +1.21 s | +0.70 s | stayed open |

  Android compiles apps in the background (idle and charging), so expect the own icon to start
  closing again. The shortcut doesn't depend on this race.
- **VCB Digibank, same race, bigger gap (2026-10-19 20:49):** opened from its own icon, its login
  screen was created at +1.55 s and the gate stepped aside at +2.12 s. VCB had already found the
  gate and sent you to Accessibility settings. Through the shortcut, the gate was off before VCB's
  process started, and VCB showed only its Developer options / USB debugging warning (the test
  had USB debugging on). So VCB needs its sprout icon too; the card offers it.

## D-021: v1.0 "good side" approvals; every new feature has a switch
2026-10-19. **Decided (your call).** After RESEARCH-TIME.md, the gamification research and
OPTIONS.md:
- **Approved for v1.0:** F32 (copy rules, comeback), F10 (worth-it rating), F26 + F59 (one-tap
  Instead, sized to the moment), A5 (activity log), F11 + F12 (weekly review + WHO-5), F27 + F60
  (weekly plan + setup kit), F29 (feeds budget + leak watch), F03 (wind-down lock), F31 + F53
  (tomorrow-you postcard).
- **Trial (built so you can try them, not approved yet):** F13 + F44 + F61 (sleep-shift quest and
  morning receipt), F17 + F18 (calm launcher and group time).
- **Every new feature has a switch in Settings** and its own code, so an unsure one can be turned
  off now and deleted later. Switching off a *restriction* (F29 budget, F03 wind-down) applies the
  next day, the same rule as E4 and S4 (P2). Everything else switches immediately.
- **F10, your change:** leaving a gated app on your own before the time box ends never prompts. It
  counts as a "left early" win in the findings and the review, with a small toast as you leave.
- **F03, your concern:** you sometimes scroll to fall asleep. That needs its own research before
  the lock is tightened. Until then, F03 has an optional "one 10-minute scroll in bed" setting.
- **No paper books yet (F27, F60):** don't delay the feature. Daytime reading can be on the phone
  (Substack, an e-book). In bed, the phone is the problem: reading a light-emitting e-reader before
  bed delayed sleep and the body clock compared with a printed book (Chang et al. 2015, *PNAS*,
  n = 12, randomised crossover, 4 h a night, so a 20-minute read is a much smaller dose). So the bed
  slot suggests an audiobook or podcast with the screen off, an e-ink reader, or one cheap paperback.
- **The backlog holds approved work only, as GitHub issues** with `v1.0`/`v1.1` milestones. The
  docs keep the research and OPTIONS.md keeps the candidates. Waiting on `gh` being installed.

## D-022: Messenger is not gated
2026-10-19. **Decided (your call).** Waiting 2 s every time someone messages you was too
annoying. `com.facebook.orca` is out of
`DEFAULT_PROFILES`; the MESSAGING profile stays for any chat app you want gated later, and its
tests now set Messenger up explicitly. Facebook (the feed) is still STRICT.

## D-023: Facebook doesn't ask about messages
2026-10-19. **Decided (your call).** Messages go through Messenger, so on Facebook "Message
someone" and "See something someone sent me" were almost never the real reason. Facebook now
offers: check a group or page · post something · honestly, just bored. Instagram and Threads keep
both messaging reasons, since their DMs are in the app. YouTube drops "See something someone sent
me" too: a video someone sent is "Watch a specific video", which it already offers. Older Facebook rows logged with those two
purposes stay as they are.

## D-024: The app behind the gate goes quiet
2026-10-19. **Decided (your call).** The gate covered the screen, but the video underneath kept
playing out loud, and "never mind" sent a still-playing YouTube home, which Android turns into a
picture-in-picture window. While any gate screen is up, the gate now holds short-term audio focus,
so players pause. An app that is still starting takes focus back when its video starts, so the
gate asks again (twice), then mutes media volume as a last resort. It lets go 0.7 s after the
screen closes, so "never mind" is already home by then. Music you're listening to pauses for the
few seconds the gate is up. No switch (your call): it's part of the gate, always on, not a feature
to try.
Checked on the S22 (Android 15): focus is granted, YouTube pauses, never mind lands on the home
screen with no small window. YouTube doesn't start again by itself after you continue: tap play.
Follow-up, same day: "never mind" still left a small window. The screen closed before Home, so
YouTube got focus for a moment and asked for audio; when the gate let go 0.7 s later YouTube
resumed from the background ("transient resume") and went picture-in-picture. Now, when the gate
sends you home, it goes Home first while the gate still covers the app, then turns the pause into
a stop (a moment of permanent focus, so the player stops for good) and lets go. Continuing into the
app still hands focus back normally. Side effect: music playing behind a gated app also stops when
you close it via the gate. YouTube also sends a window event ~1 s after going home; for 2 s after
the gate sends you home, such an event counts only if the window list agrees the app is on top
(a real reopen still prompts, checked). Checked on the S22: Android logs "entering autoPip failed".

## D-025: The "just bored" list fits your day and learns what you pick
2026-10-19. **Decided (your call, after an interview).** The list was the same at the office as
on a Sunday. Now:
- **Your week** (Settings → Your week; defaults from the interview): office Mon–Fri 10:00–19:00
  (your hours move, so they're editable), lunch 11:45–13:00 (team lunch can start 11:45), dinner
  19:30–20:30. Weekends and outside work hours count as free time.
- **At work** the only time chip is 1–5 min: stretch / water / a floor of stairs, slow breaths,
  look at something far away. **At lunch** (up to ~30 min): eyes closed 15 min, one saved
  article, eat with the team with your phone in your pocket, plus the quick ones. **Free time**:
  push-ups + plank (5 min), a 15-min bodyweight circuit, lc-trainer, the next coding step,
  reading, calling someone. Night is unchanged (D-021).
- **Exercise timing.** No push-ups or circuit from the start of a meal until an hour after it,
  or in the last 90 min before bedtime (with 02:00 bed, until about 00:30). Evidence: a
  meta-analysis of 23 studies found evening exercise doesn't harm sleep; only *vigorous* exercise
  ending within ~1 h of bed *might* (Stutz, Eiholzer & Spengler 2019, *Sports Medicine*
  49:269–287, healthy adults, tentative signal), so 90 min keeps a margin. The after-meal hour is
  common guidance, not a trial. A light walk is the opposite case: walking after eating flattens
  blood sugar (Buffey et al. 2022, *Sports Medicine*, 7 studies, read via news coverage). Short
  bouts count: "exercise snacks" of ≤5 min at least twice a day improved fitness (BJSM 2025
  meta-analysis, 11 RCTs, 414 adults, read via the BJSM blog).
- **Learning** (always on, no switch: your call, a small part of the list). Each item shown is logged in `instead_offer`
  (schema v5) with the prompt it was on, the day part, the time chip and its position; picking
  marks it. The order is the pick rate over 30 days with one pick and one skip already counted,
  so a few offers barely move anything, and nothing is ever hidden.

## D-026: Sleep tracking starts from phone use, not an in-bed tracker
2026-10-20. **Decided (your call).** Research: [sleep/RESEARCH.md](sleep/RESEARCH.md).
- **No in-bed tracker for now.** You used Sleep Cycle and Sleep as Android before and they didn't
  help you manage sleep. That matches the evidence: phone-on-mattress tracking tells sleep from lying
  awake poorly and can't stage sleep (P42, P44), and your problem is *when* you go to bed, which
  those apps don't see. If we ever want a check on F64, a tracker's nights can be read through
  Health Connect (H1 in OPTIONS.md).
- **F64 phone-down night log (approved).** Each morning: phone down → up, short checks in between
  (with the app used longest), glances, shutdowns, and the first app of the morning (G7). Labelled
  "as the phone saw it", never "sleep". Rules v1 (`NightRules`): use = unlocked or ≥ 60 s screen-on;
  night = gap covering most of the planned bed→wake time, ≥ 3 h; uses ≤ 20 min before the planned
  wake with ≥ 45 min of rest after them are interruptions. Run over the real history (the 11 nights
  of 9–19 Oct) it found every night, phone down between 00:40 and 04:54 and up between 06:31 and
  12:35, with three 1.5–2 h stretches before midnight kept as split-night stretches.
- **Football nights.** You sometimes watch a match at 02:00–04:00, and the long-term ideal is sleep
  ~23:00–02:00, the match, then sleep again; for now the plan is to shift sleep gradually first.
  So every other phone-down stretch ≥ 90 min before the wake is kept (a split night stays visible),
  and F65 has an optional "⚽ planned late night" tag so match nights can be counted apart.
- **F65 morning check-in (approved).** Rested 1–5 and time to fall asleep (4 buckets), 04:00–14:00,
  skippable with no reminder. It comes first: the F61 receipt and the F64 card wait until it's
  answered or skipped, so the numbers can't colour the answer (P45, P48).
- **Research data.** `night_log` (schema v6) stores the facts with `rules_version` and `settled`;
  `night_card_shown` logs each showing; `sleep_checkin` keeps asked, answered and skipped. The usage
  import now also copies `SCREEN_ON`, `SHUTDOWN` and `STARTUP` (backfilled ~14 days on the first
  run), and a shutdown now ends an app session, as screen-off already did.
- **Not decided yet:** your work days (for F66's social jetlag); whether the phone charges in the bedroom; dropping F69–F71.

## D-027: Match nights are planned a day ahead
2026-10-20. **Decided (your call: "implement the exception night").** F72 (ours), Settings → Match
nights, with its own switch.
- **Plan it a day ahead.** A match night loosens the wind-down lock, so adding one starts at the next
  04:00 like every other loosening (P2); removing one applies at once. Settings only offers the next
  seven nights from tomorrow, so nothing looks planned for tonight that won't apply.
- **What it opens:** during the match plus 15 min either side, YouTube, the feeds (Facebook,
  Instagram, Threads) and X get the normal pause instead of the lock: you want to see what people
  say at half time and maybe join in (your call, 2026-10-20). Only YouTube gets an "Until the match
  ends" box; the feeds keep their short boxes and the feeds budget still counts. Games stay locked,
  and the lock is back when the window ends. Your usage history from 9–19 Oct shows no match watched on the phone in
  the likely windows, so this mostly matters if you stream on YouTube; other streaming apps aren't
  gated anyway.
- **What it doesn't change:** bedtime and wake time. Keeping the wake time after a late night holds
  the sleep shift in place (anchoring the wake time is the core of S5, P19, P20); the quest's
  chapters keep their days.
- **Data:** the `matchNights` setting is versioned by day like every setting; the next morning F65's
  "planned late night" tag starts ticked (you can untick it) and F64's card names the match. Gate
  opens during the window are ordinary `gate_event` rows, joinable to the window by time.
- **Later, maybe:** your ideal of sleeping ~23:00–02:00 before a match, then sleeping again. F64
  already keeps both stretches; a "first sleep" wind-down before the match isn't built.

## D-028: Energy tracking starts phone-only: no watch, no work data for now
2026-10-23. **Decided (your call: "I don't think we will buy a watch or track the work information yet").**
Research: [energy/RESEARCH.md](energy/RESEARCH.md) §9.
- **Parked, not dropped:** H2 (wearable reader), F82 (work-PC focus log), F83 (work-tracker
  import), F84 (calendar meeting load), F86 (typing speed on the PC). Any of them can come back with
  a new decision.
- **What's left is the phone plus your own answers**: F64 phone-down night log and F65 morning
  check-in (built), phone use by hour (built), F77 steps, F74 intake taps, F73 alertness and mood
  check-ins, F76 end-of-day card, the weekly WHO-5 (built), and F75 reaction-time check.
- **What this costs:** sleep stays measured as the phone-down window plus your rested rating (P42),
  and there's no night-recovery signal unless F85 (camera HRV) is built later. F76 is your own
  rating of the workday; nothing is read from work systems.
- **Why F75 matters more now:** without a watch, the 3-minute reaction-time check is the only
  objective measure of sharpness, and P50 says ratings alone miss sleep debt.
- **Not decided yet:** which F73–F81 features to build (none are approved yet).

## D-029: No energy or food taps during work hours; food logged after the fact
2026-10-23. **Decided (your call: "I don't want to tap the phone when I am in the workday (lunch is
ok)"; you rarely drink coffee or alcohol, and snack when you have time).** Refines D-028.
- **Quiet work hours.** The energy and food features never prompt, notify or ask for a tap during
  the work part of the day in `Schedule` (10:00–19:00 on work days, see `core/DayParts.kt`). Lunch
  (11:45–13:00) is allowed.
- **The day's touch points:** morning check-in (F65, built) · lunch card: alertness + mood, and a
  lunch-walk offer (F73, F81) · after-work card: alertness + mood now, focus, main thing done,
  afternoon break taken, lunch size, snacks (F76 with F73 and F74) · wind-down: "could you switch
  off?" (F76) · optional 3-min reaction test (F75), outside work hours at the same time each day.
- **Food:** no caffeine or alcohol buttons; F79 (caffeine cut-off) isn't needed. Snacks are asked in
  the after-work card by part of the day (morning, afternoon, evening) and whether they were sweet,
  so there are no exact times. A rare coffee or drink can still be added there.
- **What this costs:** the afternoon is only seen from the after-work card, a same-day recall rather
  than an in-the-moment rating, and it tends to remember the end of the afternoon more than the dip
  (from memory: the peak-end effect). The mid-afternoon break nudge moves to lunch.
- **Passive data still covers work hours:** phone use by hour (pickups during work are a distraction
  signal you don't have to report) and F77 steps.

## D-030: The morning check-in gets one notification after the first unlock
2026-10-23. **Decided (your call: "notification after first unlock").** Changes F65's "no reminder"
from D-026.
- **Why:** since the F65 install (2026-10-20), the app was opened only in the evening and at night,
  so the check-in, which only appears on Today between 04:00 and 14:00, was never shown and
  `sleep_checkin` stayed empty.
- **The rule:** one quiet notification after the first unlock of the logical day, between 04:00
  and the start of work hours (D-029), once a day, never repeated. It clears itself when work
  starts or when the check-in is answered or skipped. Tapping it opens the check-in.
- **Data:** the notification's posting, tap and expiry are logged with the check-in's night, so "never
  asked" and "ignored" stay apart.
- **Also proposed (not approved yet):** the F75 reaction test runs right after the check-in, as a
  mini-game whose timing rules stay identical to PVT-B. Evidence: [energy/RESEARCH.md](energy/RESEARCH.md) §10.
- **Built (2026-10-23):** the nudge, and F75 as a trial with its own switch ("Morning reaction
  check"). Correction: PVT-B's wait is 1–4 s from each response (including 1 s of feedback), not
  2–10 s (that's the 10-minute PVT); the code follows the paper (`Pvtb` in `core/Reaction.kt`).
  Your usual = the median of your last 14 mornings; "about your usual" is ±8%, our own choice.- **Length (your call, 2026-10-23): 3 minutes every morning, reviewed after one week.** Shorter fixed
  versions aren't validated; the adaptive PVT-BA (~1 min 43 s) gives only three grades; fewer
  mornings would keep quality. Comparison: [energy/RESEARCH.md](energy/RESEARCH.md) §10.
- **Testing aid:** `MorningDebug.kt` (debug builds only, sender needs DUMP) posts a test nudge and
  opens a practice run that saves nothing, so the phone can be checked at any hour (A6).

## D-031: One "Last night" card with a timeline (F64 + F61)
2026-10-24. **Decided (your call: option A).** You didn't remember F61: it came from the screen-time
research (RESEARCH-TIME.md, 2026-10-19, an idea of ours scored 71%) and went in as a trial with D-021,
never reviewed. It and F64 both said "Last night" but measured different things.
- **One card:** a timeline of the planned night against the phone-down stretch, a tick per check,
  then four tiles: down and up (against the plan), checks, and F61's "Night feeds · trial". With F61
  off, the fourth tile is the first app of the morning. Each switch still controls its own part.
- **F61's window fixed:** it counted gated minutes from bedtime only until 04:00, the day boundary;
  now it runs from the planned bedtime until the phone came back up (F64), for last night and the
  6 nights it's compared with.
- **Fix:** the card no longer shows between 00:00 and 04:00, when "last night" was the night before
  that one. It shows from 04:00 to 14:00, like the check-in.
- **Test noise:** `night_card_shown` ids 2–6 (2026-10-24 00:02–00:12) were Claude's test launches,
  not you. `DEBUG_PREVIEW_MORNING` (debug builds, see `MorningDebug.kt`) now previews the card without
  logging a showing.

- **Incident, fixed:** on 2026-10-23 at 23:59, Claude's automated test taps for the F75 practice run
  kept firing after the run ended and pressed the quest's "stay on this chapter" twice
  (`sleepQuestHoldDays` = 6). Reset to 0 for the same day (2026-10-24 00:19, before the 04:00 day
  boundary), so the plan is as before: chapter 2, bed 01:45, wake 09:30. The setting's `setAt`
  shows the reset time; the value for 2026-10-23 is 0, as if nothing happened.

## D-032: The sleep shift shows itself: in the Last night card, and on the lock (trial)
2026-10-24. **Decided (your call: M1 and M4, M4 as a trial; card design D1, expanding in place).**
You hadn't noticed the quest, though it moves the times of the wind-down lock you met 1–8 times a
night. Options and scores: chat of 2026-10-24 (M1 85%, M4 83%, evening card 73%, notification 68%;
designs D1 88%, D3 83%, D4 68%, D2 65%).
- **M1:** once started, the quest lives in the morning's Last night card: a chip "Chapter 2/7 ·
  tonight 01:45" (marked "new" when a step moves tonight), and in the expanded card tonight's bed and
  wake, days left, the light tip, and "Last step didn't stick? Stay on this chapter", next to last
  night's facts. The separate quest card only shows to start the quest, or when the Last night card
  is off.
- **D1 design:** collapsed, the card is two thin bars (plan, phone down), one line ("02:25 → 09:54 ·
  40 m after plan · no checks") and chips. A tap opens it in place: hours, legend, the four tiles and
  the quest strip. Less text than the first merged card, which you found odd.
- **M4 (trial, its own switch "Sleep shift on the lock screen"):** the wind-down lock adds one line,
  "Sleep shift, chapter 2 of 7: these times move 15 min earlier every 3 days." Exposure is the
  existing `gate_event` LOCK row plus the versioned switch. Not yet seen on the phone: it needs a
  real wind-down lock.
- **Later, if still missed:** a quiet notification before wind-down (M3), not built.- **Fix (2026-10-24, your report):** the old quest card came back from 00:00 to 04:00 and after
  14:00, whenever the Last night card was hidden. Once started, the quest no longer has its own card
  at any hour; it shows in the morning card and on the lock. The old card stays only to start the
  quest, or when both Last night switches (F64, F61) are off.- **Morning only, on purpose (your call, 2026-10-24):** the Last night card stays 04:00–14:00, so
  last night's numbers aren't in front of you at bedtime (sleep/RESEARCH.md §6: data away from
  bedtime, rate first; Gavriloff 2018 on negative sleep feedback). The D-030 notification now says
  where its tap leads: "Two taps on how you slept, then last night at a glance and the 3-minute
  reaction check."

