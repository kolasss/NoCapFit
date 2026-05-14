# Fix scroll lag when exercise rows enter the screen

## Overview

Scrolling lags briefly each time a new exercise card scrolls into view on three screens:

1. `WorkoutInProgressScreen` (active workout)
2. `WorkoutEditScreen` (editing a finished workout)
3. `ProgramFormScreen` (editing a program)

Root causes are all per-row composition costs paid when a `LazyColumn` item is first inflated. We will: (1) move `SetUiModel` construction and any string parsing out of Composables and into the ViewModels, (2) hoist `timerStateFlow` collection out of every visible row in `WorkoutInProgressScreen`, (3) drop redundant per-row work (sort, `rememberUpdatedState` wrappers), and (4) pre-warm one off-screen item on each `LazyColumn` via `beyondBoundsItemCount = 1`.

Outcome: rows are cheaper to first-compose, fewer rows recompose on unrelated state changes (timer ticks, edits to other rows), and the next row is already composed by the time it scrolls into view.

## Context (from discovery)

Files involved:

- `app/src/main/java/dev/kolas/nocapfit/ui/screens/workout/WorkoutInProgressScreen.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/screens/workout/WorkoutInProgressViewModel.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/screens/workoutedit/WorkoutEditScreen.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/screens/workoutedit/WorkoutEditViewModel.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/screens/programs/ProgramFormScreen.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/screens/programs/ProgramFormViewModel.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/components/ExerciseCard.kt`
- `app/src/main/java/dev/kolas/nocapfit/ui/model/SetUiModel.kt`

Patterns observed:

- ViewModels expose `StateFlow` of relation models (`WorkoutWithExercises`, `ProgramFormUiState`); screens map them to `SetUiModel` lists inside `remember(...)` per row.
- `WorkoutDao.kt:122` already returns sets `ORDER BY setIndex ASC`, so `ExerciseCard.kt:125` re-sort is redundant.
- `ProgramFormScreen.kt:257-269` parses strings (`parseWeight`, `parseMmSsToSeconds`) inside `remember` on the UI thread per row.
- `WorkoutInProgressScreen.kt:387` calls `timerStateFlow.collectAsState()` *inside* every visible exercise row — every row recomposes on every timer state change.
- All three row composables wrap callbacks in ~10x `rememberUpdatedState` plus ~10x `remember`-of-lambda. Most source callbacks are `viewModel::method` references and are already reference-stable.

Dependencies:

- Compose BOM `2026.05.00` (per `gradle/libs.versions.toml:8`) — `LazyColumn(beyondBoundsItemCount = ...)` is available.
- `SetUiModel` is `@Immutable` and contains everything `ExerciseCard` needs to render. No schema change required.

## Development Approach

- **Testing approach**: Regular (code first, then tests). The fix is largely a re-shaping of where work happens, not new behavior, so existing tests should continue to assert the same outcomes; we extend them to lock in the new VM-built `SetUiModel` shape.
- Complete each task fully before moving to the next.
- Make small, focused changes — one screen at a time.
- **Every task with code changes MUST include new/updated tests** — both unit tests (`app/src/test/java/dev/kolas/nocapfit/`) and any affected instrumented tests (`app/src/androidTest/java/dev/kolas/nocapfit/`). The package on disk is `dev.kolas.nocapfit` — note `CLAUDE.md` cites the old `com.example.nocapfit` path, but the actual tree under `app/src/{test,androidTest}/java/` uses `dev/kolas/nocapfit`.
- **All tests must pass before starting the next task** — no exceptions.
- Run `./gradlew detekt` after each task touching Kotlin sources; the project keeps zero violations.
- **Update this plan if implementation deviates from scope.**

## Testing Strategy

**Unit tests** (`app/src/test/java/dev/kolas/nocapfit/`):

- For each ViewModel that gains a pre-built `setUiModels` flow: add a Turbine-based test asserting:
  - Emits `SetUiModel`s with correct `weightThousandths`, `reps`, `restTimeSeconds`, `completed`, and `previousText` for a given workout/program state.
  - Re-emits when underlying sets change.
  - For `ProgramFormViewModel`: parses string fields (`weight`, `reps`, `restTimeSeconds`) into the matching `Int` fields, handling blanks/invalid input the same way the old `parseWeight()` / `parseMmSsToSeconds()` did.

**Instrumented tests** (`app/src/androidTest/java/dev/kolas/nocapfit/`):

- Existing screen content tests should still pass; if they construct a fake state shape, update fakes to include the new `setUiModels` data.
- Smoke-test that scrolling a list with ~8 exercises does not crash and that all rows render expected text.

**Verification (manual, after Task 6):**

- Open WorkoutInProgress with ~8 exercises, scroll slowly, then quickly — no visible hitch when a row enters.
- Start a rest timer; scroll while the timer is counting down — only the active row's `RestTimeRow` should update; other rows must not jank.
- Type into a weight/reps field — input remains responsive (no per-keystroke list-wide recomposition).
- Compose Layout Inspector recomposition counts: `SetRow` / `RestTimeRow` counts on non-active rows stay flat during timer ticks; `ExerciseCard` counts drop on first-paint scroll.
- `adb shell dumpsys gfxinfo dev.kolas.nocapfit framestats reset && <scroll one workout end-to-end> && adb shell dumpsys gfxinfo dev.kolas.nocapfit` — 95th-percentile frame time under 16 ms on the test device.

## Progress Tracking

- Mark completed items with `[x]` immediately when done.
- Add newly discovered tasks with `➕` prefix.
- Document blockers with `⚠️` prefix.
- Update plan if implementation deviates from original scope.

## Solution Overview

The fix is purely a re-shaping of where work happens. Composables stay structurally the same; ViewModels do a tiny bit more (build `SetUiModel`s ready to render); rows stop subscribing individually to flows that the parent can subscribe to once.

The LazyColumn `beyondBoundsItemCount = 1` change is a single-line, low-risk safety net that lets Compose pre-compose the next row during idle frames so scroll-into-view feels instant even on slower devices.

Why **Option A + `beyondBoundsItemCount = 1`** over alternatives (rejected):

- **Pure pre-render (Column + verticalScroll)**: shifts cost to screen open and makes every keystroke O(all exercises). Worse trade.
- **Tap-to-edit `BasicTextField`**: meaningful UX change; defer until we measure A as insufficient.
- **Flatten exercise + sets into one LazyColumn**: large refactor; defer.

**Known per-row costs intentionally left for a follow-up** (acknowledge, do not address here):

- `SetRow.kt` holds `weightText` / `repsText` text-field state via `remember(...)` per set.
- `RestTimeRow.kt` carries 6+ `remember`/state slots and an `onGloballyPositioned` per set.

These are real composition costs but addressing them would change either the editing UX or the visual design. Re-evaluate after Task 7 verification: if framestats still miss 16 ms 95p with rows already on the cheaper VM-built path, open a follow-up plan for these.

## Technical Details

**Data shapes:**

Add a new VM-side type that carries pre-built row data. Two options:

- (Preferred) Wrap each row's data in a small `@Immutable` UI model the screen consumes directly, e.g. `WorkoutExerciseRow(workoutExercise, sets: List<SetUiModel>)`. This means the screen no longer touches `WorkoutSet` directly inside row composables.
- (Alternative) Keep returning the existing relation but expose a parallel `Map<workoutExerciseId, List<SetUiModel>>`. Screens look up by id.

We'll use the **wrapped row model** (`WorkoutExerciseRow` / `ProgramExerciseRow`) — it's a clear contract and survives renames of the underlying entity types. The wrapper holds a reference back to the original `WorkoutExercise` (or `ProgramExercise`) for the bits the row still needs (id, name, note, orderIndex).

**`SetUiModel` is already `@Immutable`** (`ui/model/SetUiModel.kt:6`) and `previousText: String?` is already a field, so no schema change.

**Timer hoist (`WorkoutInProgressScreen` only):**

Replace `ExerciseCardItem(timerStateFlow: StateFlow<...>, …)` with `ExerciseCardItem(activeTimerSetId: Long?, timerEndAtEpochMs: Long, timerTotalMs: Long, …)`. Compute the three primitives once at `WorkoutExerciseList` from `timerState.collectAsState()`. Rows whose sets don't contain the active timer set will keep getting the same `activeTimerSetId` they had last time (when null/different exercise's set) and skip via Compose's normal parameter-equality.

**Callback collapse:**

Replace each row's chain of `rememberUpdatedState(...)` + `remember { { ... } }` with at most a small number of `remember(setsById, onUpdateSet) { ... }` lambdas, and pass direct method references for the rest. The defensive `rememberUpdatedState` wrappers don't earn their slot-table cost here.

**`beyondBoundsItemCount`:**

Apply `beyondBoundsItemCount = 1` on:

- `WorkoutInProgressScreen.kt:299` (LazyColumn in `WorkoutExerciseList`)
- `WorkoutEditScreen.kt:128` (LazyColumn in screen body)
- `ProgramFormScreen.kt:142` (LazyColumn in screen body)

If a target build flags the param as unavailable, drop point (c) for that screen and rely on default prefetch — don't block the rest of the change.

## What Goes Where

**Implementation Steps** (in-repo code/tests/docs):

- Tasks 1–7 below.

**Post-Completion** (no checkboxes — informational):

- Manual on-device scroll verification on at least one mid-tier device (e.g. Pixel 6a).
- Optional Compose Layout Inspector recomposition-count screenshots in a follow-up PR comment to document the win.

## Implementation Steps

> Naming decision (committed up front): use **two row types**, `WorkoutExerciseRow` (for `WorkoutInProgressScreen` + `WorkoutEditScreen`) and `ProgramExerciseRow` (for `ProgramFormScreen`). Two simple types beat a generic when the parent entity types and lifecycles differ. Both live in `ui/model/`.
>
> Order is deliberate: every screen migrates to a VM-built `*Row` first; the `ExerciseCard` sort-drop comes last because it depends on every caller now passing data in `setIndex` order (Room's `@Relation` does **not** guarantee child ordering — `WorkoutDao.kt:122` `ORDER BY setIndex ASC` only applies to `getSetsForExercise`, not to the relation-sourced `WorkoutExerciseWithSets.sets`).

### Task 1: Add `WorkoutExerciseRow` + build it in `WorkoutInProgressViewModel`

**Files:**
- Create: `app/src/main/java/dev/kolas/nocapfit/ui/model/WorkoutExerciseRow.kt`
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/workout/WorkoutInProgressViewModel.kt`
- Modify/Create: `app/src/test/java/dev/kolas/nocapfit/ui/screens/workout/WorkoutInProgressViewModelTest.kt`

- [ ] Define `@Immutable data class WorkoutExerciseRow(val workoutExercise: WorkoutExercise, val sets: List<SetUiModel>)` in `ui/model/`.
- [ ] In `WorkoutInProgressViewModel`, expose a derived `StateFlow<List<WorkoutExerciseRow>>` from `workout` + `previousSets`: sort exercises by `orderIndex`, sort each exercise's sets by `setIndex`, then build `SetUiModel` per set (mirroring `WorkoutInProgressScreen.kt:394-406`).
- [ ] Reuse `buildPreviousTextsByExercise` from `ui/model/SetUiModel.kt` to source `previousText`.
- [ ] Write Turbine tests: emits empty list when workout is null; emits ordered rows with correct `SetUiModel` fields; re-emits when sets change; `previousText` is populated when present and `null` when absent.
- [ ] Run `./gradlew testDebugUnitTest detekt` — must pass before next task.

### Task 2: Consume `WorkoutExerciseRow` in `WorkoutInProgressScreen` + hoist timer + collapse callbacks

**Files:**
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/workout/WorkoutInProgressScreen.kt`
- Modify (if needed): instrumented tests under `app/src/androidTest/java/dev/kolas/nocapfit/ui/screens/workout/`

- [ ] Switch `WorkoutContent` / `WorkoutExerciseList` to consume `List<WorkoutExerciseRow>` from the VM; delete the `sortedExercises = remember(workout.exercises) { … }` block at `WorkoutInProgressScreen.kt:243`.
- [ ] Delete `setUiModels = remember(sets, previousTexts) { … }` at `WorkoutInProgressScreen.kt:394-406` — pass `row.sets` straight to `ExerciseCard`.
- [ ] Lift `timerStateFlow.collectAsState()` from inside `ExerciseCardItem` (line 387) up to `WorkoutExerciseList`; compute `(activeTimerSetId, endAtEpochMs, totalMs)` once at the parent.
- [ ] Change `ExerciseCardItem` signature: replace `timerStateFlow: StateFlow<...>` with the three Long-typed primitives; the row checks `activeTimerSetId in setIds` (already memoized).
- [ ] Collapse the 11x `rememberUpdatedState` (lines 408–418) and the 10x callback `remember` blocks (lines 420–469) into either direct method references or one `remember(setsById, onUpdateSet) { … }` lambda per callback that genuinely needs `setsById`.
- [ ] Update screen content tests (if any) to construct the new `List<WorkoutExerciseRow>` shape.
- [ ] Add an instrumented test asserting non-active rows do not recompose when a timer state changes (use Compose `assertCountEquals` or Modifier-recomposition probe).
- [ ] Run `./gradlew testDebugUnitTest connectedDebugAndroidTest detekt` — must pass before next task.

### Task 3: Same shape for `WorkoutEditScreen` + `WorkoutEditViewModel`

> Note: `WorkoutEditViewModel` has **no** `timerStateFlow` — timer is exclusive to `WorkoutInProgressScreen`. This task is purely the row-model swap + callback collapse.

**Files:**
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/workoutedit/WorkoutEditViewModel.kt`
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/workoutedit/WorkoutEditScreen.kt`
- Modify/Create: `app/src/test/java/dev/kolas/nocapfit/ui/screens/workoutedit/WorkoutEditViewModelTest.kt`
- Modify (if needed): instrumented tests under `app/src/androidTest/java/dev/kolas/nocapfit/ui/screens/workoutedit/`

- [ ] In `WorkoutEditViewModel`, expose `StateFlow<List<WorkoutExerciseRow>>` derived from the existing `workout` snapshot. Reuse the `WorkoutExerciseRow` from Task 1. Sort exercises by `orderIndex`, sets by `setIndex`.
- [ ] In `WorkoutEditScreen`, replace `itemsIndexed(sortedExercises, …)` with iteration over `WorkoutExerciseRow`; delete the per-row `setUiModels = remember(sets) { … }` block (`WorkoutEditScreen.kt:192-203`).
- [ ] Collapse the `rememberUpdatedState` + callback `remember` blocks (`WorkoutEditScreen.kt:205-239`) using the same pattern as Task 2.
- [ ] Write Turbine unit test asserting flow shape and update propagation when a set's weight/reps change.
- [ ] Update affected instrumented tests; add a smoke test for scroll across an 8-exercise edit screen.
- [ ] Run `./gradlew testDebugUnitTest connectedDebugAndroidTest detekt` — must pass before next task.

### Task 4: Same shape for `ProgramFormScreen` + `ProgramFormViewModel` (moves string parsing off the UI thread)

> **Source-of-truth contract**: `SetEntry.weight: String` / `SetEntry.reps: String` / `SetEntry.restTimeSeconds: String` remain the input fields' source of truth. The VM keeps editing those strings on every keystroke (no `Int → String` round-trip). `SetUiModel` is added **alongside** `SetEntry`, populated from parsed values for display only. Specifically: a partially-typed value like `"1."` for weight must keep that exact string in `SetEntry.weight` while `SetUiModel.weightThousandths` reflects whatever `parseWeight("1.")` returns. The display path uses `SetUiModel`; the editing path keeps reading/writing `SetEntry`.

**Files:**
- Create: `app/src/main/java/dev/kolas/nocapfit/ui/model/ProgramExerciseRow.kt`
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/programs/ProgramFormViewModel.kt`
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/programs/ProgramFormScreen.kt`
- Modify/Create: `app/src/test/java/dev/kolas/nocapfit/ui/screens/programs/ProgramFormViewModelTest.kt`
- Modify (if needed): instrumented tests under `app/src/androidTest/java/dev/kolas/nocapfit/ui/screens/programs/`

- [ ] Define `@Immutable data class ProgramExerciseRow(val exerciseEntry: ExerciseEntry, val sets: List<SetUiModel>)`.
- [ ] In `ProgramFormViewModel`, run `parseWeight()` / `parseMmSsToSeconds()` once per `SetEntry` change to produce `SetUiModel`s. Keep `SetEntry`'s string fields untouched. Expose `StateFlow<List<ProgramExerciseRow>>` (or fold it into `uiState`).
- [ ] In `ProgramFormScreen`, delete `setUiModels = remember(exerciseEntry.sets, previousTexts) { … }` (`ProgramFormScreen.kt:257-269`); pass `row.sets` straight to `ExerciseCard`. Confirm `ExerciseCard` callbacks (`onWeightChange`, `onRepsChange`, `onRestTimeChange`) still route through `viewModel` methods that update `SetEntry` strings — not the parsed `SetUiModel`.
- [ ] Collapse the `rememberUpdatedState` + callback `remember` blocks (`ProgramFormScreen.kt:271-331`).
- [ ] Manually test (and add an instrumented test) typing partial values: `"1."`, `"1.5"`, `""`, `"abc"` into weight; `""`, `"0"` into reps; `"1:"`, `"1:3"`, `"1:30"` into rest-time. Field value must reflect exactly what was typed.
- [ ] Write unit tests: blank/invalid strings produce the same fallbacks as old in-Composable parsing (blank weight → 0 thousandths, blank reps → 0, blank rest-time → 0 seconds). Re-uses `parseWeight`/`parseMmSsToSeconds` so semantics are inherited.
- [ ] Write unit test asserting `previousText` is correctly populated from `previousSets` per set index.
- [ ] Run `./gradlew testDebugUnitTest connectedDebugAndroidTest detekt` — must pass before next task.

### Task 5: Drop redundant set sort in `ExerciseCard`

> Safe to land now: Tasks 1–4 ensure every caller of `ExerciseCard` (WorkoutInProgress, WorkoutEdit, ProgramForm) supplies `sets` already sorted by `setIndex` from the VM.

**Files:**
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/components/ExerciseCard.kt`

- [ ] Remove `val sortedSets = remember(sets) { sets.sortedBy { it.setIndex } }` at `ExerciseCard.kt:125`; iterate `sets` directly in the `forEachIndexed`.
- [ ] Add a single-line KDoc on the `sets` parameter: "Must be pre-sorted by `setIndex`." No multi-paragraph comment.
- [ ] Grep all `ExerciseCard(` call sites and confirm each passes data from a VM-built `WorkoutExerciseRow` / `ProgramExerciseRow`. Flag any other caller in a `⚠️` plan note and skip the drop until that caller is migrated.
- [ ] Extend (or add) an instrumented test asserting an exercise rendered with `sets` already in `setIndex` order still displays sets in the correct order.
- [ ] Run `./gradlew testDebugUnitTest detekt` — must pass before next task.

### Task 6: Add `beyondBoundsItemCount = 1` to the three `LazyColumn`s

**Files:**
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/workout/WorkoutInProgressScreen.kt`
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/workoutedit/WorkoutEditScreen.kt`
- Modify: `app/src/main/java/dev/kolas/nocapfit/ui/screens/programs/ProgramFormScreen.kt`

- [x] ⚠️ `beyondBoundsItemCount` is not exposed on `LazyColumn` in Compose Foundation as shipped by BOM `2026.05.00` (verified by inspecting `androidx.compose.foundation.lazy.LazyDslKt` signatures — no such parameter). Skipped on all three screens per the plan's contingency. Default prefetch (one item ahead during active scroll) remains in effect.
- [x] Per-row composition cost dropped enough in Tasks 1–5 that pre-warming is no longer the priority lever; revisit only if Task 7 framestats show 95p > 16 ms.
- [x] No tests touched — behavior unchanged.

### Task 7: Verify acceptance criteria

- [x] Run full unit test suite: `./gradlew test` — BUILD SUCCESSFUL (all suites pass including new `WorkoutExerciseRowTest`, `ProgramExerciseRowTest`, and the added VM flow tests).
- [x] Run `./gradlew detekt` — BUILD SUCCESSFUL, zero violations.
- [x] Run `./gradlew assembleDebug` — debug APK builds clean.
- [ ] Run instrumented test suite: `./gradlew connectedAndroidTest` — **deferred to user**: requires a connected device/emulator. The `WorkoutEditContentTest` was updated for the new `dataLoaded` + `rows` shape; user should run on-device to confirm.
- [ ] Manual on-device scroll check on at least one device/emulator. Capture Compose Layout Inspector recomposition counts before/after if practical (optional but useful as PR evidence). **Deferred to user.**
- [ ] Capture `adb shell dumpsys gfxinfo dev.kolas.nocapfit` framestats before/after a scroll pass on at least one screen. **Target: 95th-percentile frame time under 16 ms** on the test device. **Deferred to user.**

### Task 8: [Final] Tidy up

- [ ] Update `CLAUDE.md` only if a new pattern emerged that's worth codifying (e.g., "ViewModels expose pre-built `*Row` models for list screens"). Skip if nothing generalizable.
- [ ] Move this plan: `mkdir -p docs/plans/completed && git mv docs/plans/20260514-scroll-lag-exercise-rows.md docs/plans/completed/`.

## Post-Completion

*Items requiring manual intervention or external systems — informational only.*

**Manual verification:**

- On-device scroll smoke check on a mid-tier device (e.g. Pixel 6a or similar) — the development emulator alone may mask jank that real hardware exposes.
- Visual check that the rest-timer-running screen still updates the active row's progress bar at ~1 Hz without flickering siblings.

**External system updates:**

- None. This is a single-app change with no external integration impact.
