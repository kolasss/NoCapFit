# Plan: Workout Edit — Replace Autosave with Explicit Save

## Context
Currently, the workout edit screen autosaves every change (set weight/reps, toggle completion, add/remove exercises, reorder) directly to the database. Only the program name is buffered. The user wants all edits buffered in-memory and persisted only when tapping an explicit Save button.

## Approach
Stop observing the workout as a live Room Flow (which would overwrite local edits). Instead, load the workout once into a mutable in-memory snapshot, apply all edits to that snapshot, and persist the full snapshot on save.

## Changes

### 1. ViewModel — `WorkoutEditViewModel.kt` (major rewrite)

**Replace live Flow with mutable snapshot:**
- Remove `workout: StateFlow` backed by `getWithExercisesFlow` (live DB observation)
- On init, load workout once via `getWithExercises(workoutId)` into a `MutableStateFlow<WorkoutWithExercises?>`
- All edit methods mutate the in-memory snapshot instead of calling repository

**Edit methods — mutate snapshot only:**
- `updateProgramName(name)` — update `_programName` (already buffered, keep as-is)
- `updateSet(workoutSet)` — replace the set in the snapshot
- `toggleSetCompleted(workoutSet)` — flip completed flag in the snapshot
- `addSet(workoutExerciseId)` — append a new `WorkoutSet` to the exercise in the snapshot (use a temporary negative ID for new sets)
- `removeExercise(workoutExerciseId)` — remove the exercise from the snapshot
- `moveExercise(workoutExerciseId, direction)` — swap `orderIndex` values in the snapshot
- `addExercise(exerciseId, exerciseName)` — append a new `WorkoutExerciseWithSets` to the snapshot (use temporary negative IDs)

**New `save()` method:**
- Delete the existing workout's exercises/sets from DB (clean slate via a new DAO method)
- Re-insert the workout (update), exercises, and sets from the snapshot
- Navigate back after save completes (return a success signal)

**New DAO/Repository method needed:**
- `WorkoutDao.deleteExercisesForWorkout(workoutId: Long)` — deletes all `workout_exercises` for a workout (sets cascade-delete via FK)
- `WorkoutDao.saveWorkoutWithExercises(workout, exercises)` — transactional: update workout, delete old exercises, insert new exercises + sets (mirrors `ProgramDao.saveProgramWithExercises` pattern)
- Expose through `WorkoutRepository`

### 2. Screen — `WorkoutEditScreen.kt`
- Add a "Save" `TextButton` in the TopAppBar `actions` block
- `Save` calls `viewModel.save()`, then navigates back on completion
- Back button navigates back without saving (discard changes)
- Remove `saveProgramName()` call from back button handler

### 3. Data layer — `WorkoutDao.kt` + `WorkoutRepository.kt`
Add transactional save method:
```
@Transaction
suspend fun saveWorkoutEdits(
    workout: Workout,
    exercises: List<Pair<WorkoutExercise, List<WorkoutSet>>>
)
```
- Calls `update(workout)` for the workout itself (program name etc.)
- Calls `deleteExercisesForWorkout(workout.id)` to clear old data
- Re-inserts all exercises + sets with new IDs

Add `deleteExercisesForWorkout`:
```
@Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
suspend fun deleteExercisesForWorkout(workoutId: Long)
```

### 4. Tests — `WorkoutEditViewModelTest.kt`
Rewrite tests to verify:
- Edit methods mutate in-memory state (not repository calls)
- `save()` calls `workoutRepository.saveWorkoutEdits()` with correct data
- Back navigation doesn't persist changes

## Files to modify
- `app/src/main/java/com/example/nocapfit/data/db/dao/WorkoutDao.kt`
- `app/src/main/java/com/example/nocapfit/data/repository/WorkoutRepository.kt`
- `app/src/main/java/com/example/nocapfit/ui/screens/workoutedit/WorkoutEditViewModel.kt`
- `app/src/main/java/com/example/nocapfit/ui/screens/workoutedit/WorkoutEditScreen.kt`
- `app/src/test/java/com/example/nocapfit/ui/screens/workoutedit/WorkoutEditViewModelTest.kt`

## Verification
1. `./gradlew testDebugUnitTest --tests "com.example.nocapfit.ui.screens.workoutedit.WorkoutEditViewModelTest"`
2. `./gradlew detekt`
3. `./gradlew build`
