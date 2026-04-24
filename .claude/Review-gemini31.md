Here is a summary of the performance bottlenecks discovered in your app that are causing the scrolling lag, along with a proposed implementation plan.

The `Explore` subagent identified that the root causes of the lag are:
1. **Timer State Cascading Recomposition**: Every second, the global timer state updates (`timerEndAtEpochMs` and `timerTotalMs`), forcing every single `ExerciseCardItem` and all of their sets to recompose, even when they aren't the active timer. 
2. **Excessive Compose Animations**: `RestTimeRow.kt` computes remaining time locally for every set and runs a color/progress layout rebuild constantly.
3. **Unstable Collections**: Data classes like `WorkoutWithExercises`, `WorkoutExerciseWithSets`, and `ProgramForm` state use `List<T>`. Compose treats `List` as unstable, leading to unnecessary recompositions when a single item changes.
4. **Inefficient Sorting & Reallocation**: `ExerciseCard.kt` dynamically re-sorts its nested sets on every render instead of receiving pre-sorted data. `ProgramFormViewModel` fully copies its lists on trivial changes (like adding a set).

I've saved a detailed plan to `/memories/session/plan.md`. Here's the plan to resolve these issues:

## Plan: Optimize Compose Scrolling Performance

**Steps**
1. **Stabilize Data Models**: Replace `List<T>` with `ImmutableList<T>` (from `kotlinx.collections.immutable`) in core UI state data classes to make them `@Stable` to Compose. *(Note: This depends on adding the `kotlinx-collections-immutable` dependency if it's not present)*.
2. **Isolate Timer State**: Stop propagating `timerEndAtEpochMs` and `timerTotalMs` down through every `ExerciseCardItem`. Pass these values exclusively to the currently active timer set, or pass a lambda/`State` to defer reading the changing values until the innermost component (e.g., `RestTimeRow`).
3. **Optimize Set Sorting**: Move dynamic set sorting (e.g. `sets.sortedBy { it.setIndex }`) into the ViewModel or repository mapping layer so the UI consistently receives pre-sorted `ImmutableList`s.
4. **Optimize `RestTimeRow`**: Refactor the background progress animation to read from a lambda or a derived state applied purely to the graphics layer (e.g., using `drawBehind { ... }` with a state read directly inside the block), avoiding recomposition of the whole row on every tick.
5. **Optimize `ProgramFormViewModel`**: Ensure list updates use persistent collections or optimize the copy operations to preserve reference equality for unmodified items.

**Relevant files**
- WorkoutInProgressScreen.kt — Update timer state propagation.
- app/src/main/java/com/example/nocapfit/ui/screens/workout/ExerciseCard.kt — Remove rendering-time sorting, accept `ImmutableList`.
- RestTimeRow.kt — Optimize background/progress drawing to skip recomposition.
- State classes (e.g., `WorkoutWithExercises.kt`, `ProgramFormState.kt`) — Convert `List`s to `ImmutableList`s.
- app/src/main/java/com/example/nocapfit/ui/screens/program/ProgramFormViewModel.kt — Optimize state updates.

**Verification**
1. Run app and start a workout with multiple exercises.
2. Start a rest timer.
3. Scroll quickly up and down; verify frame rate is smooth and no visible hanging occurs.
4. Use Android Studio Layout Inspector (with Recomposition Counts enabled) to verify that ticking the timer only affects the active `RestTimeRow` and does not recompose the entire `LazyColumn`.

**Further Considerations**
1. The `kotlinx.collections.immutable` library may need to be added to `libs.versions.toml` and build.gradle.kts if not already included. Should I check if it's there and add it?
