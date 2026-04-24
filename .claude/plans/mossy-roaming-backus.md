# Fix Scroll Lag When Exercise Cards Enter/Exit Screen

## Context

Both WorkoutInProgressScreen and ProgramFormScreen lag when scrolling because every `ExerciseCardItem` recomputes its `setUiModels` whenever **any** exercise's previous-set data loads, plus un-memoized lambdas prevent Compose from skipping `ExerciseCard` recomposition.

## Root Causes

### 1. `previousSets` full-map dependency invalidates ALL cards

Both screens use:
```kotlin
val setUiModels = remember(sets, exId, previousSets) { ... }
```
`previousSets` is the entire `Map<Pair<Long, Int>, PreviousSetData>` for all exercises. When any entry changes (e.g., loading previous data for a newly visible exercise), the map reference changes and **every** card recomputes `setUiModels`.

### 2. Un-memoized lambdas in ExerciseCardItem

Inline lambdas like `{ onAddSet(id) }` create new function instances every recomposition, preventing Compose from skipping `ExerciseCard`.

## Fix 1: Per-exercise previous text extraction

**Strategy**: Extract a `List<String?>` of previous texts for only this exercise before computing `setUiModels`. Use that list as the `remember` key instead of the full map. `List<String?>.equals()` is structural, so if this exercise's data hasn't changed, the cached `setUiModels` is returned.

### WorkoutInProgressScreen.kt (lines 370-387)

Replace single `setUiModels` remember with two steps:
```kotlin
val prevTexts = remember(exId, sets, previousSets) {
    sets.map { ws ->
        if (exId != null) previousSets[exId to ws.setIndex]?.let { formatPreviousSet(it) } else null
    }
}
val setUiModels = remember(sets, prevTexts) {
    sets.mapIndexed { i, ws ->
        SetUiModel(..., previousText = prevTexts[i])
    }
}
```

### ProgramFormScreen.kt (lines 253-265)

Same pattern:
```kotlin
val prevTexts = remember(exId, exerciseEntry.sets, previousSets) {
    exerciseEntry.sets.indices.map { idx -> previousSets[exId to idx]?.let { formatPreviousSet(it) } }
}
val setUiModels = remember(exerciseEntry.sets, prevTexts) {
    exerciseEntry.sets.mapIndexed { setIndex, setEntry ->
        SetUiModel(..., previousText = prevTexts[setIndex])
    }
}
```

## Fix 2: Memoize lambda callbacks

### WorkoutInProgressScreen.kt (lines 394-424)

Wrap all lambdas passed to `ExerciseCard` in `remember` keyed on their captured values (`id`, `setsById`, `exId`).

### ProgramFormScreen.kt (lines 280-303)

Same pattern, keyed on `exerciseIndex`, `exerciseEntry.sets`, `exId`.

## Files to modify

- `app/src/main/java/com/example/nocapfit/ui/screens/workout/WorkoutInProgressScreen.kt`
- `app/src/main/java/com/example/nocapfit/ui/screens/programs/ProgramFormScreen.kt`

## Verification

1. `./gradlew build` compiles without errors
2. `./gradlew detekt` passes with zero violations
3. Manual test: open workout with 5+ exercises, scroll up/down -- should feel smooth without dropped frames when cards enter/exit
4. Manual test: edit program with 5+ exercises, same scroll test
