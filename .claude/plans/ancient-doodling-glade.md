# Apply Scroll Lag Optimizations to Workout Edit and Program Form Screens

## Context

We applied several performance optimizations to the workout-in-progress screen. Most are already inherited via shared components (ExerciseCard, SetRow, RestTimeRow, CompactInput). The only remaining change is the `PreviousSetLookup` @Immutable wrapper for the program form screen.

## What's already inherited (no changes needed)

- **Tap-to-edit CompactInput** — global in `CompactInput.kt`
- **drawBehind backgrounds** — in `SetRow.kt` and `RestTimeRow.kt` (shared)
- **Box+clip instead of Surface** — in `SetRow.kt` and `RestTimeRow.kt` (shared)
- **Deferred DropdownMenu** — in `ExerciseCard.kt` (shared)
- **WorkoutEditScreen** — uses no `previousSets` Map, no timer state. Fully optimized via shared components.

## Change needed: ProgramFormScreen `previousSets` Map → `PreviousSetLookup`

The program form screen still passes a raw unstable `Map<Pair<Long, Int>, PreviousSetData>` through its composable tree.

### PreviousSetLookup — add `toMutableMap()` method

`ProgramFormViewModel` calls `_previousSets.value.toMutableMap()` (line 215) to incrementally add data. Need to expose this on the wrapper.

**File:** `app/src/main/java/com/example/nocapfit/ui/model/SetUiModel.kt`

### ProgramFormViewModel — change types

**File:** `app/src/main/java/com/example/nocapfit/ui/screens/programs/ProgramFormViewModel.kt`
- Line 76-77: `_previousSets` and `previousSets` types → `PreviousSetLookup`
- Line 198: wrap `map` in `PreviousSetLookup(map)`
- Line 215: `_previousSets.value.toMutableMap()` (needs `toMutableMap()` on PreviousSetLookup)
- Line 217: wrap `map` in `PreviousSetLookup(map)`
- Add import for `PreviousSetLookup`

### ProgramFormScreen — change parameter types

**File:** `app/src/main/java/com/example/nocapfit/ui/screens/programs/ProgramFormScreen.kt`
- Line 117: `previousSets: Map<...>` → `PreviousSetLookup`
- Line 242: same
- Update import

## Verification

1. `./gradlew detekt` — zero violations
2. `./gradlew testDebugUnitTest` — all tests pass
