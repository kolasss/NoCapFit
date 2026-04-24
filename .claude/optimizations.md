# Compose Performance Optimizations

Optimizations applied to fix scroll lag on LazyColumn screens (workout in progress, program form, workout edit). The lag occurred when exercise cards entered the viewport.

## 1. Tap-to-edit TextFields

**Problem:** `BasicTextField` is the heaviest standard composable — initializes text layout, cursor, selection, focus, IME, and keyboard on every composition. With 9 per exercise card (2 per set row + 1 per rest time row), initial composition cost was massive.

**Fix:** `CompactInput.kt` renders a lightweight `Text` + `Box` by default. `BasicTextField` only inflates when the user taps the field. Focus is auto-requested via `FocusRequester`, and the field reverts to display mode on focus loss. Uses `hasFocusedOnce` guard to prevent premature dismissal.

**File:** `ui/components/CompactInput.kt`

## 2. StateFlow per-item instead of timer param drilling

**Problem:** Timer state (`activeTimerSetId`, `timerEndAtEpochMs`, `timerTotalMs`) was collected at the top level and drilled through every composable. Every timer tick (1/sec) caused ALL exercise cards to recompose.

**Fix:** Pass `viewModel.timerState` (the `StateFlow` reference) down to each `ExerciseCardItem`. Each item calls `collectAsState()` independently and derives whether it has the active timer via `remember`. Cards without the timer get `null` and skip recomposition.

**File:** `ui/screens/workout/WorkoutInProgressScreen.kt`

## 3. @Immutable wrapper for unstable Map parameters

**Problem:** `Map<Pair<Long, Int>, PreviousSetData>` is not Compose-stable, so any composable receiving it can never be skipped.

**Fix:** Created `@Immutable data class PreviousSetLookup` wrapping the map. Exposes `operator fun get`, `isEmpty()`, and `toMutableMap()`. Used in both `WorkoutInProgressViewModel` and `ProgramFormViewModel`.

**File:** `ui/model/SetUiModel.kt`

## 4. drawBehind for background colors (no animateColorAsState)

**Problem:** `animateColorAsState` creates animation coroutines even when idle — 6 per card (2 per set: SetRow + RestTimeRow). Also reads in composition phase, forcing recomposition.

**Fix:** Removed `animateColorAsState` entirely. Resolve color directly in `Modifier.drawBehind { drawRect(color) }` — no animation infrastructure, reads only in draw phase.

**File:** `ui/components/SetRow.kt`, `ui/components/RestTimeRow.kt`

## 5. Box+clip instead of Surface

**Problem:** Material3 `Surface` handles elevation, content color propagation via CompositionLocals, indication — all unnecessary overhead when using transparent color.

**Fix:** Replaced `Surface(color = Color.Transparent, shape = RoundedCornerShape(8.dp))` with `Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)))`.

**File:** `ui/components/SetRow.kt`, `ui/components/RestTimeRow.kt`

## 6. Deferred DropdownMenu composition

**Problem:** `DropdownMenu` creates Popup infrastructure, position provider, and all `DropdownMenuItem` composables even when `expanded = false` — ~20-25 wasted nodes per card.

**Fix:** Guard with `if (showMenu) { DropdownMenu(expanded = true, ...) { ... } }` so the menu tree is only composed when opened.

**File:** `ui/components/ExerciseCard.kt`

## 7. Strong skipping handles lambda memoization

**Note:** Kotlin 2.0.20+ enables strong skipping by default, which auto-wraps all lambdas in `remember`. Manual `remember` wrappers on lambdas are redundant and add overhead. Pass inline lambdas directly.

## Key principle

The lag was caused by **initial composition cost** (too many heavy composables per card entering viewport), not recomposition. The biggest wins came from eliminating `BasicTextField` on scroll-in (#1) and reducing composable node count per card (#4, #5, #6).
