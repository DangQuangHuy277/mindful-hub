# Baseline: phone usage before any intervention

Source: `adb shell dumpsys usagestats` from a Galaxy S22 Ultra (SM-S908N, Android 15, One UI 7),
pulled 2026-10-18 00:36 (+07). The raw dump stays in the session scratchpad, not in this repo.

**What the dump can and can't give.** You get event-level timestamps for **only the last 24 h**,
plus per-app totals for the current week, month and year. The app's own logger (A1) has to build
the longer, hour-by-hour history. Whether `UsageStatsManager.queryEvents` from an app reaches
further back is still to be checked in the stack spike.

## Totals per app

| App | This week (Mon 12/10 → Sat night 17/10, ≈5.5 days) | Last 20 days (28/9 → 17/10) | Per day (20-day avg) |
|---|---|---|---|
| Facebook | 9.8 h · 126 opens | **34.7 h · 495 opens** | **1.7 h · 25 opens** |
| Chrome | 6.4 h · 96 opens | 12.4 h · 312 opens | 0.6 h. *Contents unknown: Facebook web? reading?* |
| X | 4.3 h · 44 opens | 12.0 h · 124 opens | 0.6 h |
| Mobile Legends | 2.8 h · 4 opens | 10.6 h · 18 opens | 0.5 h, in **long sessions** (~35 min per open) |
| Threads | 1.3 h | 8.6 h | 0.4 h |
| Messenger | 3.2 h · 81 opens | 8.4 h · 364 opens | 0.4 h · 18 opens |
| YouTube | 2.6 h | 8.4 h | 0.4 h |
| Substack | 2.6 h | 6.1 h | 0.3 h |
| Instagram | 0.6 h | 0.8 h | small |

## Last 24 h, hour by hour (Fri 17/10 00:36 → Sat 18/10 00:36; minutes in each hour)

```
hour          00  01  02  03 …… 12  13  14  15  16  17  18  19  20  21  22  23   total
Facebook       8  60  58  18      21  27   6   .   .   .   .   .   5   1   5   2   211 m · 68 opens
Messenger      .   .   .   .       .   .   .   .   .   .   .   .   7  46  20   2    75 m
Substack       .   .   .   .       .   .   .   .   9  23   1   .   1   .  15  14    62 m
YouTube        .   .   .   .       .   3  22   .   .   .   .   .   .   .   7   4    35 m
Instagram      .   .   .   .       1  30   2   .   .   .   .   .   .   .   .   .    33 m
Threads        .   .   .   .       .   .   .   .   .   .   .   .  20   .   .   .    20 m
unlocks        5   .   1   .       1   1   1   2   3   3   4   5   4   1   1   3    35
```

Nothing happened between 03:20 and 12:00, which is when you slept.

## What the data says

1. **Bed is the problem, as you suspected.** 2 h 24 m of Facebook between 00:36 and 03:20, with
   uninterrupted 60-minute blocks. This is the strongest target for S1 (wind-down) and E3 (time box).
2. **The Facebook app sent you no notifications, yet you opened it 68 times.** You open it yourself,
   by habit. That's exactly the situation the open-time gate was tested on (P3), and notification
   batching wouldn't help with Facebook.
3. **Messenger interrupted you 93 times in 24 h.** That's probably mostly real people, your
   girlfriend included. Batching (P5) could help here, but it would need an allowlist so her
   messages always come through.
4. **Substack, the app you called healthy, is already 62 min a day.** The hub doesn't need to
   replace it, only to link to it calmly.
5. **Chrome at 6.4 h this week is unexplained.** If that's Facebook in the browser, the gate has to
   cover browser domains as well.

Caveats: this is one Saturday plus partial totals, so it's a snapshot, not a baseline. A1 still
needs a full logged week.
