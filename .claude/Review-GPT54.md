## Plan: Shared ExerciseCard Optimization

Center the work on the shared card stack instead of treating workout and program form separately. The common bottleneck is the ExerciseCard composition path in ExerciseCard.kt, with supporting cost in SetRow.kt and RestTimeRow.kt. That same stack is used by WorkoutInProgressScreen.kt, WorkoutEditScreen.kt, and ProgramFormScreen.kt, so the optimization task should migrate all three consumers together.

**Steps**
1. Baseline all three screens with recomposition highlighting and frame timing to separate card-entry cost from repeated recomposition after state changes.
2. Redesign the shared ExerciseCard API so it does less composition-time work: callers should provide pre-sorted set models, and timer-related inputs should be narrowed so non-active rows do not inherit timer churn.
3. Optimize shared rows in SetRow.kt and RestTimeRow.kt, especially background animation behavior and active-timer update scope.
4. Migrate the three consumers to the new shared boundary in parallel:
   Workout in progress: isolate timer observation in WorkoutInProgressScreen.kt.
   Program form: reduce state fan-out in ProgramFormViewModel.kt and tighten SetUiModel construction in ProgramFormScreen.kt.
   Workout edit: align WorkoutEditScreen.kt with the same optimized shared API.
5. Review item identity and list stability across consumers, especially the program-form key strategy in ProgramFormScreen.kt, since `exercise.id` can be unsafe if duplicates are allowed.
6. Re-run the same scroll-enter-viewport scenario on all three screens and compare recomposition counts and frame pacing against baseline.

**Relevant files**
- ExerciseCard.kt — primary shared optimization target
- SetRow.kt — row-level composition cost
- RestTimeRow.kt — timer-row invalidation scope
- SetUiModel.kt — shared UI model identity and precomputed data
- WorkoutInProgressScreen.kt — timer-heavy consumer
- WorkoutEditScreen.kt — third ExerciseCard consumer
- ProgramFormScreen.kt — program-form consumer and key strategy
- ProgramFormViewModel.kt — state churn source

**Verification**
1. Workout-in-progress: with a running timer, confirm only the active rest-time row updates while scrolling.
2. Workout edit: confirm newly visible cards compose faster after the shared card changes.
3. Program form: confirm add/remove/reorder plus scrolling no longer trigger broad card invalidation.
4. Run `./gradlew testDebugUnitTest` after implementation and manually verify on a device or emulator.
