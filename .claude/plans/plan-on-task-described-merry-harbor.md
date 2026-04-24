# Rest timer: ceil remaining time

## Context

The rest timer currently rounds **down** (floor) when converting the remaining
milliseconds to seconds, because `remainingMs / MILLIS_PER_SECOND` is integer
division. That means a timer that still has 0.9s left displays `00:00`, and the
progress bar fill lags the underlying value.

The user wants the remaining time to be **ceiled** instead:

| remaining            | display |
|----------------------|---------|
| 1s 40ms              | `00:02` |
| 1s 600ms             | `00:02` |
| 0s 900ms             | `00:01` |
| 0s 0ms               | `00:00` |

The same rounding must drive the **in-app UI** (rest-time row + workout top bar)
**and** the **Android notification** (countdown text + progress bar).

User confirmed in planning Q&A:
- Progress bar fills as time passes: `fill = 1 - ceil(remainingMs/1000) / totalSeconds`.
- Format is `MM:SS` (two-digit minutes).

## Approach

Introduce a shared util and route every remaining-ms → display / fill site
through it. Switch the notification from Android's built-in chronometer to an
explicit content text that we update every second (the service already runs a
per-second loop that calls `notificationManager.notify`).

### 1. New shared util — `app/src/main/java/com/example/nocapfit/util/TimeFormat.kt`

```kotlin
package com.example.nocapfit.util

fun ceilSecondsFromMs(remainingMs: Long): Int {
    if (remainingMs <= 0) return 0
    return ((remainingMs + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()
}

fun formatMmSs(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val mins = s / SECONDS_PER_MINUTE
    val secs = s % SECONDS_PER_MINUTE
    return "%02d:%02d".format(mins, secs)
}
```

Uses existing constants in `util/Constants.kt` (`MILLIS_PER_SECOND`,
`SECONDS_PER_MINUTE`).

### 2. Replace display sites (three of them)

**`ui/components/RestTimeRow.kt`**
- Delete the private `formatMmSs` (line 230) — it now lives in util and changes
  from `%d:%02d` → `%02d:%02d`.
- `RestTimerCountdown` (line 221–228): replace `remainingMs / MILLIS_PER_SECOND`
  + `formatMmSs(totalSecs.toInt())` with
  `formatMmSs(ceilSecondsFromMs(remainingMs))`.
- `RestTimeRowContent` (line 182) still formats `restTimeSeconds` (the
  pre-timer value) — it keeps using the now-shared `formatMmSs` from util.
  This changes the display of the per-set rest time from `1:30` to `01:30`;
  that's acceptable given the user's confirmed MM:SS preference.

**`ui/screens/workout/WorkoutInProgressScreen.kt`**
- `TopBarTitle` (line 524–547): replace the inline `timerSecs`/`mins`/`secs`
  block + `"%d:%02d".format(mins, secs)` with
  `formatMmSs(ceilSecondsFromMs(timerRemainingMs))`.

**`service/RestTimerService.kt`** — see item 4.

### 3. Replace progress-bar fill sites (two of them)

Both sites currently compute
`fill = 1f - remainingMs.toFloat() / totalMs`. Change to snap to whole-second
ceil values so the bar matches the displayed number:

```kotlin
val totalSec = (timerTotalMs / MILLIS_PER_SECOND).toInt()
val fill = if (totalSec > 0) {
    (1f - ceilSecondsFromMs(remainingMs).toFloat() / totalSec).coerceIn(0f, 1f)
} else 0f
```

- `ui/components/RestTimeRow.kt:80-84` (`fillProgress`, drawn into `drawRect`).
- `ui/screens/workout/WorkoutInProgressScreen.kt:454-458` (`timerProgress`,
  fed into `LinearProgressIndicator`).

### 4. Notification rewrite — `service/RestTimerService.kt`

In `buildNotification()` (lines 94–131):

- Replace `remainingSeconds = (remainingMs / MILLIS_PER_SECOND).toInt()` with
  `val remainingSeconds = ceilSecondsFromMs(remainingMs)`.
- Keep `setProgress(totalSeconds, totalSeconds - remainingSeconds, false)` — it
  now inherits the ceil value automatically.
- Remove `setUsesChronometer(...)` (line 121), `setChronometerCountDown(true)`
  (line 122), and `setWhen(endAtEpochMs)` (line 120). These drove Android's
  built-in chronometer text, which floors its own rounding and ignores our
  helper.
- Add `.setContentText(formatMmSs(remainingSeconds))` so the notification
  shows the same ceil'd MM:SS text as the in-app timer. The existing
  per-second `notify()` loop (lines 72–78) continues refreshing the text.

### 5. Tests — `app/src/test/java/com/example/nocapfit/util/TimeFormatTest.kt` (new)

Cover the helpers directly:
- `ceilSecondsFromMs`: `0L → 0`, `1L → 1`, `999L → 1`, `1000L → 1`, `1001L → 2`,
  `1040L → 2`, `1600L → 2`, `900L → 1`, negative (`-5L → 0`).
- `formatMmSs`: `0 → "00:00"`, `5 → "00:05"`, `59 → "00:59"`, `60 → "01:00"`,
  `90 → "01:30"`, `600 → "10:00"`, negative (`-3 → "00:00"`).

Existing tests (`RestTimeRowKtTest.kt`, `TimerCoordinatorTest.kt`) stay green
as-is — they don't cover the display/fill logic.

## Critical files to modify

- `app/src/main/java/com/example/nocapfit/util/TimeFormat.kt` (new)
- `app/src/main/java/com/example/nocapfit/ui/components/RestTimeRow.kt`
- `app/src/main/java/com/example/nocapfit/ui/screens/workout/WorkoutInProgressScreen.kt`
- `app/src/main/java/com/example/nocapfit/service/RestTimerService.kt`
- `app/src/test/java/com/example/nocapfit/util/TimeFormatTest.kt` (new)

## Verification

1. `./gradlew detekt testDebugUnitTest` — must be clean; new
   `TimeFormatTest` passes.
2. Manual, on device/emulator — start a rest timer with a short duration
   (e.g. 3s) and confirm:
   - The row countdown ticks `00:03 → 00:02 → 00:01 → 00:00` (each value
     visible for roughly a full second, no `00:00` stall at the end).
   - The row's coloured fill bar and the top-bar `LinearProgressIndicator`
     advance in whole-second steps in lockstep with the displayed number.
   - The Android notification shows the same `MM:SS` text and its progress
     bar advances in the same steps.
3. Edge case — let the timer hit zero: display reads `00:00`, fill reaches
   exactly 100%, notification shows `00:00`, then the alarm fires and the
   service stops.
