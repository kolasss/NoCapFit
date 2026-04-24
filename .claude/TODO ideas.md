# NoCapFit — TODO

## Testing

- [ ] Add `WorkoutHistoryViewModelTest`
- [ ] Add `WorkoutDetailViewModelTest`
- [ ] Add repository unit tests (`ProfileRepository`, `TimerRepository`)
- [ ] Add UI tests for remaining screens (`ExerciseDetailContent`, `ProgramFormContent`, `AddWorkoutContent`, `WorkoutInProgressContent`, `WorkoutDetailContent`)

## Features

### Analytics & Progress Tracking
- [ ] Workout statistics screen (volume, frequency, streaks)
- [ ] Personal records tracking per exercise
- [ ] Progress charts/graphs over time
- [ ] Body metrics tracking (bodyweight, measurements)

### Workout Enhancements
- [ ] Workout notes/comments per session
- [ ] RPE (Rate of Perceived Exertion) tracking per set
- [ ] Adjust rest timer duration during active workout
- [ ] Superset / circuit support
- [ ] Warm-up set type (distinguished from working sets)

### Program Enhancements
- [ ] Duplicate/clone a program
- [ ] Progressive overload suggestions (auto-increment weight/reps)
- [ ] Program scheduling (assign programs to days of the week)

### Data Management
- [ ] Export workouts to CSV

### Exercise Library
- [ ] Exercise tags/categories filter on exercise list
- [ ] Exercise history (past sets/weights for a given exercise)

### UX
- [ ] Reorder exercises during active workout (drag & drop)
- [ ] Swipe-to-delete sets during workout
- [ ] Onboarding flow for first-time users

## CI/CD

- [ ] Upload debug APK as build artifact
- [ ] Add code coverage reporting
- [ ] Run instrumented tests in CI (Android emulator)
