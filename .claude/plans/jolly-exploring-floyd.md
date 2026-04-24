# Workout history: group by month instead of date

## Context

The workout history screen currently groups workouts by full date (e.g., "Wednesday, 27 March 2024"). The user wants grouping by month instead (e.g., "March 2024") so there are fewer section headers and the list feels less fragmented.

## Changes

### 1. Add `formatMonth()` to DateTimeFormatting.kt
**File:** `app/src/main/java/com/example/nocapfit/ui/util/DateTimeFormatting.kt`

Add a new function that returns `"March 2024"` format (full month name, no abbreviation — per user's date formatting preferences):
```kotlin
fun formatMonth(epochMs: Long): String {
    val ldt = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = ldt.month.name.lowercase()
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    return "$month ${ldt.year}"
}
```

### 2. Change grouping key in WorkoutHistoryScreen.kt
**File:** `app/src/main/java/com/example/nocapfit/ui/screens/workouthistory/WorkoutHistoryScreen.kt`

- Line 48: Add import for `formatMonth`
- Line 66: Change `formatDate(it.workout.startTime)` → `formatMonth(it.workout.startTime)`

### 3. Update tests in WorkoutHistoryContentTest.kt
**File:** `app/src/androidTest/java/com/example/nocapfit/ui/screens/workouthistory/WorkoutHistoryContentTest.kt`

- Change import from `formatDate` to `formatMonth`
- Replace all `formatDate(...)` calls with `formatMonth(...)` (used in 5 test methods to build the `grouped` map and assert header text)

## Verification

1. `./gradlew detekt` — zero violations
2. `./gradlew assembleDebug` — builds
3. Manual: open History tab, confirm workouts are grouped under month headers like "March 2024"
