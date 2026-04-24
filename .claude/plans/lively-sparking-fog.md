# Plan: Exercise title click navigates to Exercise Detail from Workout In Progress

## Context

During an active workout, tapping an exercise title should navigate to the Exercise Detail screen, and pressing back should return to the workout. The `ExerciseCard` component already supports an `onExerciseTitleClick` callback (renders title in primary color when set), but the Workout In Progress screen doesn't wire it up.

## Changes

### 1. Thread `onExerciseTitleClick` through the composable chain

**File:** `app/src/main/java/com/example/nocapfit/ui/screens/workout/WorkoutInProgressScreen.kt`

- **`WorkoutInProgressScreen`** (line 70): Pass a lambda to `WorkoutContent` that calls `navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))`. Only navigate when `exerciseId` is non-null (exercises added by name without a DB match have `exerciseId = null`).

- **`WorkoutContent`** (line 206): Add `onExerciseTitleClick: (Long) -> Unit` parameter, pass it down to `WorkoutExerciseList`.

- **`WorkoutExerciseList`** (line 260): Add `onExerciseTitleClick: (Long) -> Unit` parameter, pass it down to `ExerciseCardItem`.

- **`ExerciseCardItem`** (line 344): Add `onExerciseTitleClick: ((Long) -> Unit)? = null` parameter. Wire it to `ExerciseCard`'s existing `onExerciseTitleClick` param:
  ```kotlin
  onExerciseTitleClick = exId?.let { { onExerciseTitleClick?.invoke(it) } }
  ```
  This makes the title clickable only when the exercise has a DB ID (`exId != null`).

### 2. Back navigation

No changes needed — `ExerciseDetailScreen` already uses the standard top bar back button that calls `navController.popBackStack()`, which will return to the workout screen.

## Verification

1. `./gradlew detekt` — passes with zero violations
2. `./gradlew build` — compiles successfully
3. Manual: start a workout, tap an exercise title, verify Exercise Detail screen opens, press back, verify return to workout with state preserved
