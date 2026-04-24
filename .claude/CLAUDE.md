# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NoCapFit is an Android fitness tracking app built with Kotlin and Jetpack Compose (Material Design 3). Single-module, Compose-only UI (no XML layouts).

- **Package**: `com.example.nocapfit`
- **Min/Target/Compile SDK**: 36 (Android 15+)
- **Java target**: 17
- **Kotlin**: 2.3.20
- **AGP**: 9.1.0
- **KSP**: 2.3.6 (KSP2 — standalone version, not tied to Kotlin version)

## Build Commands

```bash
./gradlew build                    # Build debug APK
./gradlew assembleRelease          # Build release APK
./gradlew test                     # Run unit tests
./gradlew connectedAndroidTest     # Run instrumented tests (requires device/emulator)
./gradlew testDebugUnitTest --tests "com.example.nocapfit.SomeTest"  # Run a single test class
./gradlew detekt                   # Run Detekt static analysis and formatting
./gradlew clean                    # Clean build artifacts
```

## Linting

Detekt with the formatting ruleset (wraps ktlint) and Compose rules (`io.nlopez.compose.rules`). Config at `config/detekt/detekt.yml`, overrides on top of defaults (`buildUponDefaultConfig = true`). `autoCorrect = true` auto-fixes formatting issues on each run.

Key Compose-specific config: `@Composable` functions allow PascalCase naming, relaxed `LongParameterList`/`LongMethod` thresholds, and `ModifierMissing` is enforced. All new code must pass `./gradlew detekt` with zero violations — there is no baseline file.

## Architecture

### Layers

1. **UI Layer** — Compose screens + `@HiltViewModel` ViewModels using `StateFlow` for state
2. **Repository Layer** — `@Singleton` classes wrapping DAOs, injected into ViewModels
3. **Data Layer** — Room database (9 entities, 5 DAOs, 4 relation types), DataStore for preferences
4. **Service Layer** — Foreground service + BroadcastReceiver + `TimerCoordinator` singleton for rest timers

### Navigation

`MainActivity` (single activity, `@AndroidEntryPoint`) hosts a `NavHost` with 9 screens defined in `Screen` sealed class. Routes with parameters use `NavType.LongType`. MainActivity also manages a minimized workout panel overlay — when a workout is in progress and the user navigates away, a `MiniWorkoutPanel` appears above the bottom nav.

### Data Conventions

- **Weight**: stored as `Int` thousandths (e.g., 75500 = 75.5 kg). Convert with `ui/util/WeightFormatting.kt` — `formatWeightDisplay()` (strips trailing zeros, for read-only) / `formatWeightInput()` (forced `.0`, for edit fields) / `parseWeight()`.
- **Rest time**: stored as seconds `Int`. Displayed as MM:SS using `parseMmSsToSeconds()` / `secondsToMmSsDigits()` in `RestTimeRow.kt`.
- **Active workout**: a `Workout` with `endTime = null`. Finishing sets `endTime`; cancelling deletes the workout.

### Timer System

`TimerCoordinator` (singleton) manages rest timer state as `StateFlow<TimerUiState>` (Idle/Running/Finished). On set completion, it: inserts `ActiveTimer` in Room → schedules `AlarmManager` exact alarm → starts `RestTimerService` foreground service. `TimerCompletionReceiver` handles alarm broadcast, calls `timerRepository.completeTimer()` (plays sound/vibrates/shows notification). On app relaunch, `reconstructState()` recovers running timers from the database.

### Dependency Injection

Hilt throughout. `DatabaseModule` provides Room DB + all 5 DAOs. `PreferencesModule` provides `DataStore<Preferences>`. Database callback auto-creates a default `Profile` on first creation.

### Profiles

All user data (exercises, programs, workouts) is scoped to a `Profile` via foreign keys. `profileRepository.getDefault()` is called in ViewModel init blocks to get the current profile ID.

## Testing

### Unit Tests

- **JUnit 4** + **MockK** (`relaxUnitFun = true` for suspend mocks) + **Turbine** (Flow testing) + **kotlinx-coroutines-test**
- `MainDispatcherRule` (`TestWatcher`) swaps `Dispatchers.Main` with `UnconfinedTestDispatcher` — required for all ViewModel tests
- `SavedStateHandle` constructed with maps for testing ViewModels that read nav args
- Pattern: `coEvery`/`coVerify` for suspend functions, `every` for regular functions, `match {}` for argument assertions
- Tests mirror source layout under `app/src/test/java/com/example/nocapfit/`

### Compose UI Tests (Instrumented)

- **Compose UI Test** (`androidx.compose.ui.test.junit4`) — `createComposeRule()` for screen-level tests
- Tests call `internal` content composables directly with plain data + callback lambdas — no Hilt or ViewModel mocking needed
- `ComposeTestUtil.kt` provides `setThemedContent` extension wrapping content in `NoCapFitTheme(dynamicColor = false)`
- Screen composables follow a two-tier pattern: public top-level (wires ViewModel) and `internal` `*Content` (takes data + callbacks). Tests target the `internal` layer.
- Tests mirror source layout under `app/src/androidTest/java/com/example/nocapfit/`
- Run with `./gradlew connectedAndroidTest` (requires device/emulator)

## Key Paths

- App build config: `app/build.gradle.kts`
- Version catalog: `gradle/libs.versions.toml`
- DI modules: `di/DatabaseModule.kt`, `di/PreferencesModule.kt`
- Navigation: `ui/navigation/Screen.kt`, `ui/navigation/NavGraph.kt`
- Timer system: `service/TimerCoordinator.kt`, `service/RestTimerService.kt`, `service/TimerCompletionReceiver.kt`
- Theme: `ui/theme/`
- Unit tests: `app/src/test/java/com/example/nocapfit/`
- Compose UI tests: `app/src/androidTest/java/com/example/nocapfit/`

All source paths are relative to `app/src/main/java/com/example/nocapfit/` unless noted otherwise.
