# Refactor Plan — Prioritized Shortlist

## Context

Follow-up to the NoCapFit architecture audit. This plan implements the 6 highest-impact items from the shortlist. Each step is self-contained and landable as a single commit; later steps assume earlier ones are merged.

Non-goals (audit items deferred): A5 (DB seeding), A6 (UseCase layer), A7/C6 (UI-model extraction), A8 (blanket error handling), B5/B6 (minor style items), C4 (BackupManager refactor), C5 (TimerScheduler interface).

## Ordering rationale

1 → 6 → 3 are low-risk mechanical refactors that clear duplication with zero behavior change. Do those first. 2 → 5 touch the timer subsystem; 2 prepares for 5 (`TimerNotifier` becomes the single place completion side effects live, which makes 5 trivial). 4 is the biggest UI refactor and independent — sequence it last.

---

## Step 1 — `CurrentProfileHolder` singleton

**Problem:** 7 ViewModels repeat the same `_profileId` + `init { profileRepository.getDefault() }` block. Duplication + N DB round-trips per app session.

**Files to create**
- `app/src/main/java/com/example/nocapfit/data/session/CurrentProfileHolder.kt`

**New class**
```kotlin
@Singleton
class CurrentProfileHolder @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationScope private val scope: CoroutineScope, // or make it lazy
) {
    val profileId: StateFlow<Long?> = flow { emit(profileRepository.getDefault()?.id) }
        .stateIn(scope, SharingStarted.Eagerly, null)
}
```
Simpler alternative (no app-scope DI needed): expose `suspend fun requireProfileId(): Long` that memoizes the first lookup. Pick whichever is simpler given existing DI wiring — there is currently no app-scope `CoroutineScope` binding, so the `suspend` memoization form is lower-friction.

**Files to modify** (replace `_profileId` + `profileRepository` injection with `currentProfileHolder`):
- `ui/screens/workout/WorkoutInProgressViewModel.kt:61–64`
- `ui/screens/addworkout/AddWorkoutViewModel.kt:60–63`
- `ui/screens/workoutedit/WorkoutEditViewModel.kt:51–54`
- `ui/screens/programs/ProgramFormViewModel.kt:80–83`
- `ui/screens/programs/ProgramListViewModel.kt:36–41`
- `ui/screens/exercises/ExerciseListViewModel.kt:45–48`
- `ui/screens/workouthistory/WorkoutHistoryViewModel.kt:44–49`

**Pattern change per VM** — the common shape becomes:
```kotlin
val programs = currentProfileHolder.profileId
    .filterNotNull()
    .flatMapLatest { programRepository.getAllWithExercises(it) }
    .stateIn(...)
```
Remove the local `_profileId` MutableStateFlow and the init block entirely where possible.

**Verification:** existing VM unit tests still compile after swapping `mockk<ProfileRepository>` → `mockk<CurrentProfileHolder>`. Launch each screen in the emulator; data should load as before.

---

## Step 2 — Extract `TimerNotifier` out of `TimerRepository`

**Problem:** `data/repository/TimerRepository.kt:34–71` plays sound, vibrates, and posts notifications — UI concerns in the data layer. Repository can't be unit-tested without Android context.

**Files to create**
- `app/src/main/java/com/example/nocapfit/service/TimerNotifier.kt`

**New class**
```kotlin
@Singleton
class TimerNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferences: ThemePreferences,
) {
    suspend fun notifyCompletion() {
        playSound()
        vibrate()
        postCompletionNotification()
    }
    // private helpers: move the three try-blocks from TimerRepository verbatim.
}
```
Move constants `TIMER_CHANNEL_ID` / `TIMER_NOTIFICATION_ID` to `TimerNotifier` (or keep in `TimerCoordinator.companion`; update `RestTimerService.CHANNEL_ID` reference).

**Files to modify**
- `data/repository/TimerRepository.kt` — delete the three try-blocks (lines 38–68); `completeTimer()` becomes a thin wrapper returning `rowsAffected != 0`. Drop the `@ApplicationContext context` and `themePreferences` injections.
- `service/TimerCompletionReceiver.kt:29–38` — inject `TimerNotifier`; after `timerRepository.completeTimer(timerId)` returns true, call `timerNotifier.notifyCompletion()`.
- `service/RestTimerService.kt:80–83` — same addition.

**Verification:** start a workout → complete a set → sound + vibration + completion notification still fire. Cancel mid-timer → no notification. Repository test no longer needs `Context`.

---

## Step 3 — Extract `ConfirmDialog` + `InputDialog`

**Problem:** `AlertDialog(…)` hand-rolled in 8 files — at least 4 are identical-shape "confirm destructive action" prompts, and at least one is a text-input prompt.

**Files to create**
- `app/src/main/java/com/example/nocapfit/ui/components/ConfirmDialog.kt`
- `app/src/main/java/com/example/nocapfit/ui/components/InputDialog.kt`

**Signatures**
```kotlin
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    dismissLabel: String = "Cancel",
    destructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
)

@Composable
fun InputDialog(
    title: String,
    initialValue: String,
    label: String,
    confirmLabel: String = "Save",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
)
```

**Files to modify** (replace inline `AlertDialog` with `ConfirmDialog`):
- `ui/screens/workoutdetail/WorkoutDetailScreen.kt:100–120` (delete workout)
- `ui/screens/programs/ProgramListScreen.kt:118–139` (delete program)
- `ui/components/ExerciseCard.kt:~360` (remove exercise)
- `ui/screens/settings/SettingsScreen.kt:~175` (import data confirm)
- Audit `WorkoutInProgressScreen.kt`, `AddWorkoutScreen.kt`, `ExerciseDetailScreen.kt` — convert where the pattern matches.

Replace `SaveAsProgramDialog` (`WorkoutDetailScreen.kt:131–163`) with `InputDialog`.

**Leave alone:** `RestTimeForAllDialog.kt` — has custom UI (MM:SS fields); not a simple prompt.

**Verification:** instrumented tests (`app/src/androidTest/…`) for each converted dialog — ensure labels and confirm callbacks fire. Manual: delete a workout / program; save-as-program flow.

---

## Step 4 — Hoist minimized-workout state out of `MainActivity`

**Problem:** `MainActivity.kt:72–189` — 5 imperative state vars + 3 `LaunchedEffect` blocks to keep the mini-panel in sync with the DB. `LaunchedEffect(Unit)` at line 168 means a workout started in the background after composition won't show.

**Prereq change in data layer**
- `data/db/dao/WorkoutDao.kt:75` currently only exposes `suspend fun getActiveWorkout(): Workout?`. Add:
  ```kotlin
  @Query("SELECT * FROM workouts WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
  fun getActiveWorkoutFlow(): Flow<Workout?>
  ```
- `data/repository/WorkoutRepository.kt:33` — expose the Flow.

**Files to create**
- `app/src/main/java/com/example/nocapfit/ui/screens/main/ActiveWorkoutViewModel.kt`

**New VM**
```kotlin
@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    val activeWorkout: StateFlow<Workout?> =
        workoutRepository.getActiveWorkoutFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
```

**Files to modify**
- `MainActivity.kt` — delete the 4 `rememberSaveable` vars (lines 77–80), delete `MinimizedWorkoutEffects` entirely (152–189), delete the `onMinimizeWorkout = {...}` write-through logic on `NavGraph` (141–147). `MainContent` obtains `val vm: ActiveWorkoutViewModel = hiltViewModel()` and derives:
  ```kotlin
  val active by vm.activeWorkout.collectAsState()
  val isMinimized = active != null && !isOnWorkoutScreen
  ```
  `onMinimizeWorkout` in `NavGraph` just becomes `navController.navigate(WorkoutHistory)` — the DB is already the source of truth.
- `ui/navigation/NavGraph.kt` — `onMinimizeWorkout` signature changes to `() -> Unit` (drop the `Long` param).
- Remove `@Inject lateinit var workoutRepository` from `MainActivity`.

**Verification:**
- Start workout → navigate away → mini-panel appears.
- Close workout from detail/edit screen → mini-panel disappears without manual `onClear`.
- Kill & relaunch app with active workout → panel appears immediately (currently works via the `LaunchedEffect(Unit)` — Flow version preserves this).
- Start workout from deep-link / notification resume → panel appears (currently broken — this is the bug fix).

---

## Step 5 — Unify timer completion path

**Problem:** `RestTimerService.kt:73–85` polls in a `while(true) { delay(...) }` loop, then calls `timerRepository.completeTimer()` + `timerCoordinator.onTimerCompleted()`. `TimerCompletionReceiver.kt:29–38` does the same from the alarm. Safe today (`completeIfRunning` is atomic) but redundant and battery-wasteful.

**Target flow after refactor**
- `TimerCoordinator.startTimer()` — schedules alarm + starts service. No change.
- `RestTimerService` — starts foreground, posts/refreshes the progress notification, then **just waits** to be stopped. No completion logic. Cancel action routes through `TimerCoordinator.cancelTimer()` (already the case).
- `TimerCompletionReceiver.onReceive()` — sole completion path: `timerRepository.completeTimer(id)` → `timerNotifier.notifyCompletion()` (from Step 2) → `timerCoordinator.onTimerCompleted(id)` → `context.stopService(Intent(…, RestTimerService::class.java))`.

**Files to modify**
- `service/RestTimerService.kt:72–85` — replace the `while` loop with a self-refresh via `AlarmManager`-triggered notification updates, OR simpler: keep a `delay(1000)` loop that only refreshes the notification (not completion), and exits when `remaining <= 0` without calling `completeTimer`. Completion is the receiver's job. Service stops itself after the final refresh.
- `service/TimerCompletionReceiver.kt` — add `context.stopService(Intent(context, RestTimerService::class.java))` after the coordinator call.

**Risk:** process-killed-while-foreground-service-running edge case. The alarm survives process death (that's why it was chosen); the receiver still fires even if the service was killed. Keep behavior equivalent by always stopping the service from the receiver path.

**Verification:**
- Start timer, wait through completion → notification updates smoothly every second, then "Rest Complete" fires from the receiver.
- Start timer, background the app, wait → completion still fires (alarm path).
- Start timer, cancel via notification skip action → service stops, no completion notification.
- Start timer, force-stop app, wait → alarm fires receiver, completion sound/vibration still play (Step 2 made this possible).

---

## Step 6 — Extract `WeightFormatting` utility

**Problem:** `formatWeight` exists in 4 places with inconsistent signatures (some take `Int thousandths`, `SetRow.kt:160–166` takes `Double kg`); `parseWeight` repeats in 2.

**Files to create**
- `app/src/main/java/com/example/nocapfit/ui/util/WeightFormatting.kt`

**Canonical API**
```kotlin
// Input is always the canonical unit: Int thousandths.
fun formatWeight(thousandths: Int): String { /* from ProgramFormViewModel:269–278 */ }

// String from text field → thousandths.
fun parseWeight(input: String): Int { /* from ProgramFormViewModel:281–284 */ }
```

**Files to modify**
- `ui/screens/programs/ProgramFormViewModel.kt:268–285` — delete `companion object` block; callers import top-level functions. Update CLAUDE.md reference ("Convert with `ProgramFormViewModel.formatWeight()`") — note the path change.
- `ui/components/SetRow.kt:160–166` — delete local `formatWeight(Double)`. Callers passing `Double` convert once: `formatWeight((kg * WEIGHT_MULTIPLIER).roundToInt())`. Also replace the inline `(parsed * WEIGHT_MULTIPLIER).toInt()` at line 75 with `parseWeight(newValue)`.
- `ui/screens/workoutdetail/WorkoutDetailScreen.kt:365–368` — delete local copy, import.
- `ui/screens/exercises/ExerciseDetailScreen.kt:278–281` — delete local copy, import.

**Also update CLAUDE.md** `Data Conventions` section to reference the new location.

**Verification:** unit tests for `ProgramFormViewModel.formatWeight`/`parseWeight` move verbatim to `WeightFormattingTest.kt`. Visual check: weight displays identically on program form, set row, workout detail, exercise detail.

---

## Critical files summary

| Step | Primary files touched |
| --- | --- |
| 1 | `data/session/CurrentProfileHolder.kt` (new); 7 VMs |
| 2 | `service/TimerNotifier.kt` (new); `data/repository/TimerRepository.kt`, `service/TimerCompletionReceiver.kt`, `service/RestTimerService.kt` |
| 3 | `ui/components/ConfirmDialog.kt` + `InputDialog.kt` (new); 5–7 screens |
| 4 | `ui/screens/main/ActiveWorkoutViewModel.kt` (new); `MainActivity.kt`, `ui/navigation/NavGraph.kt`, `data/db/dao/WorkoutDao.kt`, `data/repository/WorkoutRepository.kt` |
| 5 | `service/RestTimerService.kt`, `service/TimerCompletionReceiver.kt` |
| 6 | `ui/util/WeightFormatting.kt` (new); `ProgramFormViewModel`, `SetRow`, `WorkoutDetailScreen`, `ExerciseDetailScreen`; `CLAUDE.md` |

## Verification (end-to-end)

After each step (or in a single test pass at the end):

1. `./gradlew detekt` — zero violations (CLAUDE.md requires it).
2. `./gradlew test` — unit tests pass; update mocks for Step 1 (`CurrentProfileHolder` instead of `ProfileRepository`) and Step 2 (drop Context from `TimerRepository` tests).
3. `./gradlew connectedAndroidTest` — instrumented tests for dialog conversions in Step 3.
4. Manual on emulator/device:
   - Full workout flow: start → complete sets (timer + sound) → finish. Verifies Steps 2, 5, 6.
   - Navigate away mid-workout → mini-panel → resume. Verifies Step 4.
   - Delete workout / delete program / import data prompts. Verifies Step 3.
   - Cold-start app with running timer from before kill. Verifies Step 5 edge case.
