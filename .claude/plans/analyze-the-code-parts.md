# Architecture Audit

## Context

Analysis of NoCapFit (Kotlin / Jetpack Compose / Hilt / Room): does the architecture follow Android best practices? What code can be extracted? What classes should be decoupled? Read-only audit. Every finding cites a real file:line.

## Verdict

**Solid foundation, targeted issues.** Hilt DI, Repository layer, Compose state patterns, and navigation are done well. The main problems cluster around three themes: (1) `MainActivity` + `TimerCoordinator` doing too much, (2) per-VM boilerplate (profile fetch, weight formatting), (3) timer lifecycle ownership split across three classes.

## Strengths (don't change)

1. **Repository layer is clean** — ViewModels go through `WorkoutRepository`, `ExerciseRepository`, etc.; no DAO leakage into UI (exception: `BackupManager`, see C4).
2. **Hilt is correct** — `@Singleton` scoping, `@HiltViewModel`, `@ApplicationContext` used properly in DI modules.
3. **StateFlow idioms are consistent** — `MutableStateFlow` + `asStateFlow()`, `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), …)`.
4. **Navigation is decoupled** — `NavGraph` only routes by screen + `Long` IDs; no cross-VM reach-in; `hiltViewModel()` wires each screen independently.
5. **Existing extractions work** — `EmptyState`, `SetUiModel`, `ui/util/DateTimeFormatting.kt`, `util/Constants.kt`, `util/TimeFormat.kt` are reused properly.

## A. Architecture issues

### A1. `MainActivity` is a god-activity (HIGH)
- `MainActivity.kt:72–217` — `MainContent()` manually tracks 5 `mutableStateOf` values and runs three `LaunchedEffect` blocks in `MinimizedWorkoutEffects` (`:152–189`) that re-query `WorkoutRepository` to rehydrate that state.
- `MainActivity.kt:168` uses `LaunchedEffect(Unit)` — fires once, so a workout started after composition won't show in the minimized panel.
- **Why it matters:** state duplicated between `MainActivity` and the repository; imperative mutation; background changes don't propagate; untestable without the real DB.
- **Fix direction:** introduce a `MinimizedWorkoutViewModel` that exposes `StateFlow<Workout?>` from `workoutRepository.getActiveWorkoutFlow()`. `MainContent` collects it; no manual state vars, no imperative effects.

### A2. `TimerRepository` performs UI side effects (HIGH)
- `data/repository/TimerRepository.kt:34–71` — `completeTimer()` plays sounds, vibrates, posts notifications.
- **Why it matters:** pulls `Vibrator` / `NotificationManager` / `RingtoneManager` into the data layer; repository can't be tested without Android context; breaks single-responsibility.
- **Fix direction:** extract `TimerNotifier` (`@Singleton`) that owns sound/vibrate/notification. `TimerRepository` only updates DB state.

### A3. `TimerCoordinator` holds Context + `Dispatchers.Main` scope for DB work (MEDIUM)
- `service/TimerCoordinator.kt:30–33, 47` — `@Singleton` with `@ApplicationContext`, long-lived `CoroutineScope(Dispatchers.Main + SupervisorJob())` that is never cancelled.
- `service/TimerCoordinator.kt:52–55, 107–112, 115+` — launches DB reads (`reconstructState()`) and delays on Main.
- **Why it matters:** DB reads on the main thread risk jank/ANR; singleton + context is a common leak pattern.
- **Fix direction:** use `Dispatchers.IO` for the internal scope; switch to Main only for the final state update.

### A4. `RestTimerService` polls with a 1 s loop (MEDIUM)
- `service/RestTimerService.kt:73–85` — `while (true) { delay(...) }` against `System.currentTimeMillis()`.
- **Why it matters:** the `AlarmManager` exact alarm already fires `TimerCompletionReceiver`. Polling in the foreground service is redundant and drains battery.
- **Fix direction:** make the service a notification holder only.

### A5. Room DB seeding via raw SQL in `onCreate` callback (MEDIUM)
- `di/DatabaseModule.kt:37–93` — hand-written SQL inserts default profile/exercises/programs using hardcoded IDs (e.g. `profileId = 1L`).
- **Fix direction:** inject a `DatabaseInitializer` that runs once (DataStore flag) and delegates to DAOs / existing `DefaultExercises.kt` + `DefaultPrograms.kt`.

### A6. No domain / UseCase layer for multi-repo orchestration (MEDIUM)
- e.g. `AddWorkoutViewModel.createWorkoutFromProgram()` (`:69–103`) and `WorkoutInProgressViewModel.addExerciseFromDb()` (`:154–181`) contain cross-repository business logic that belongs in a UseCase.
- **Fix direction:** `CreateWorkoutFromProgramUseCase(programRepo, workoutRepo)` with `suspend operator fun invoke(programId, profileId): Long`.

### A7. Room entities used directly as UI state (LOW–MEDIUM)
- VMs import `WorkoutSet`, `ProgramExerciseSet`, `Exercise` directly (`ui/screens/programs/ProgramFormViewModel.kt:6–9`, `ui/screens/addworkout/AddWorkoutViewModel.kt:5–11`).
- `ui/model/SetUiModel.kt` exists but the pattern isn't applied consistently.
- **Fix direction:** add UI models for entities that actually change shape in UI. Leave as-is for simple read-only displays.

### A8. `viewModelScope.launch { }` blocks have no error handling (LOW)
- e.g. `WorkoutInProgressViewModel.completeSet()` (`:93–106`).
- **Why it matters:** a failed DAO call silently desyncs UI from DB.
- **Fix direction:** `try/catch` and surface errors via a `SharedFlow<UiEvent>` or nullable error in state.

## B. Duplication to extract

### B1. `formatWeight()` copy-pasted 4× (HIGH)
- `ui/screens/programs/ProgramFormViewModel.kt:269–278`
- `ui/components/SetRow.kt:160–167`
- `ui/screens/workoutdetail/WorkoutDetailScreen.kt:365–368`
- `ui/screens/exercises/ExerciseDetailScreen.kt:278–281`

Note: `SetRow` signature is `formatWeight(kg: Double)`, others take `Int thousandths`. Canonical API should take `Int`; callers convert.

**Extract to:** `ui/util/WeightFormatting.kt` with `formatWeight(thousandths: Int)` + `parseWeight(text: String)`. Also deduplicates `parseWeight` at `ProgramFormViewModel.kt:281–284` and `SetRow.kt:71–76`.

### B2. Profile-ID fetch boilerplate in every VM (HIGH)
7 ViewModels repeat the same `init { viewModelScope.launch { _profileId.value = profileRepository.getDefault().id } }` block:
- `ui/screens/workout/WorkoutInProgressViewModel.kt:61–64`
- `ui/screens/addworkout/AddWorkoutViewModel.kt:60–63`
- `ui/screens/workoutedit/WorkoutEditViewModel.kt:51–54`
- `ui/screens/programs/ProgramFormViewModel.kt:80–83`
- `ui/screens/exercises/ExerciseListViewModel.kt:45–48`
- `ui/screens/programs/ProgramListViewModel.kt:36–39`
- `ui/screens/workouthistory/WorkoutHistoryViewModel.kt:44–47`

**Extract to:** a `CurrentProfileHolder` `@Singleton` that exposes `StateFlow<Long?>` (lazy, loaded once). Same root cause as C2.

### B3. Hand-rolled confirmation `AlertDialog`s (MEDIUM)
`AlertDialog` hand-rolled in 8 files; at least 4 are identical-shape "confirm destructive action":
- `ui/screens/workoutdetail/WorkoutDetailScreen.kt:101–118` (delete workout)
- `ui/screens/programs/ProgramListScreen.kt:119–132` (delete program)
- `ui/components/ExerciseCard.kt:~360` (remove exercise)
- `ui/screens/settings/SettingsScreen.kt:~175` (import data)

**Extract to:** `ui/components/ConfirmDialog.kt` — `ConfirmDialog(title, message, confirmLabel, onConfirm, onDismiss)`.

### B4. Input-text `AlertDialog` pattern (MEDIUM)
- `ui/screens/workoutdetail/WorkoutDetailScreen.kt:132–160` (`SaveAsProgramDialog`) — ~25 lines of `AlertDialog` + `OutlinedTextField`.

**Extract to:** `ui/components/InputDialog.kt` — generic save-with-text-input.

### B5. Inconsistent text-field component (LOW)
- `SetRow.kt` uses `CompactInput`; `ExerciseFormScreen.kt` and `SaveAsProgramDialog` use raw `OutlinedTextField`.
- Not strictly duplication, but a convention question.

### B6. Repeated "update item at index in list inside state" (LOW)
- `ui/screens/programs/ProgramFormViewModel.kt:154–176` — `addSet`, `removeSet`, `updateSet` all do `_uiState.update { it.copy(list = it.list.toMutableList().also { … }) }`.

**Extract to:** `util/StateListExt.kt` with `fun <T> List<T>.updateAt(index: Int, transform: (T) -> T): List<T>`. Marginal gain.

### Already clean (don't re-extract)
- Date/time formatting — `ui/util/DateTimeFormatting.kt`.
- MM:SS rest time — `parseMmSsToSeconds` / `secondsToMmSsDigits` in `ui/components/RestTimeRow.kt`.
- `EmptyState`, `SetUiModel`, `util/Constants.kt`.

## C. Decoupling opportunities

### C1. `MainActivity` ↔ workout domain (HIGH)
Same symptom as A1. Hoist into `MinimizedWorkoutViewModel` + `MiniWorkoutPanelHost`; `MainActivity` passes navigation callbacks only.

### C2. Profile lookup repeated across 7 VMs (HIGH)
Same symptom as B2. `CurrentProfileHolder` singleton exposing a shared `StateFlow<Profile>` simultaneously removes duplication and decouples VMs from `ProfileRepository` for the common-case read.

### C3. Timer lifecycle split across 3 classes (HIGH)
`TimerCoordinator` + `RestTimerService` + `TimerCompletionReceiver` each can "complete" a timer. Two completion paths; safe today only because `completeIfRunning` DAO method is atomic. **Fix direction:** coordinator = single owner. Service = notification surface. Receiver = "alarm fired → tell coordinator."

### C4. `BackupManager` reaches directly into Room internals (MEDIUM)
- `data/backup/BackupManager.kt:77` calls `database.workoutDao().getActiveWorkout()` — bypasses `WorkoutRepository`.
- `data/backup/BackupManager.kt:30` uses `database.openHelper.writableDatabase` for raw VACUUM / file-copy SQL.
- **Fix direction:** route via repositories; extract a `DatabaseExporter` for file/VACUUM mechanics.

### C5. `TimerCoordinator` tightly bound to `AlarmManager` + `Context` (MEDIUM)
- `service/TimerCoordinator.kt:137–181` — alarm scheduling + `PendingIntent` + service startup all inlined.
- **Fix direction:** extract `TimerScheduler` interface; primary impl uses `AlarmManager`.

### C6. Entities used as UI state (LOW–MEDIUM)
Same as A7.

### Already well-decoupled
- Navigation layer (`NavGraph`, `Screen` sealed class).
- Repository interfaces for Workout/Program/Exercise/Profile (only `BackupManager` bypasses them).
- `SetUiModel` showing the entity→UI conversion pattern is understood.

## Prioritized shortlist

Ranked by impact-to-effort:

1. **`CurrentProfileHolder` singleton** — kills B2 duplication across 7 VMs and C2 coupling in one shot.
2. **Move notification side effects out of `TimerRepository`** (A2) into a `TimerNotifier`.
3. **Extract `ConfirmDialog` + `InputDialog`** (B3, B4).
4. **Hoist minimized workout state out of `MainActivity`** (A1 / C1).
5. **Unify timer completion path** (A4 / C3).
6. **Extract `formatWeight` / `parseWeight` to `ui/util/WeightFormatting.kt`** (B1).

Items A5, A6, A7, A8, B5, B6, C4, C5, C6 are real issues but lower priority; tackle opportunistically.
