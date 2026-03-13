# NoCapFit

A fitness tracking Android app for managing exercises, workout programs, and logging workouts with built-in rest timers.

## Features

- **Exercise Library** — Create and manage custom exercises with descriptions and tags
- **Workout Programs** — Build reusable programs with exercises, sets, reps, weight, and rest times
- **Live Workouts** — Start workouts from programs, track set completion in real time, and add exercises on the fly
- **Rest Timers** — Automatic rest timer with foreground notification, sound, and vibration on completion; survives app backgrounding
- **Workout History** — Browse and review past workouts with full detail
- **Minimized Workout** — Navigate the app freely during a workout with a floating mini panel to return
- **Theming** — Material 3 dynamic colors with light/dark/system theme support

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material Design 3), no XML layouts
- **Architecture**: Single Activity, MVVM with ViewModels + StateFlow
- **DI**: Hilt
- **Database**: Room (with KSP)
- **Preferences**: DataStore
- **Navigation**: Jetpack Navigation Compose
- **Timer**: Foreground Service + AlarmManager + BroadcastReceiver

## Requirements

- Android Studio (latest stable)
- Android SDK 36 (Android 15+)
- JDK 17 (bundled with Android Studio)

## Building

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew build           # Build debug APK
./gradlew test            # Run unit tests
```

Or open the project in Android Studio and run directly on an emulator or device.

## Project Structure

```
app/src/main/java/com/example/nocapfit/
├── data/
│   ├── db/           # Room database, entities, DAOs, relations
│   ├── preferences/  # DataStore theme preferences
│   └── repository/   # Repository layer wrapping DAOs
├── di/               # Hilt modules (database, preferences)
├── service/          # Rest timer service, coordinator, broadcast receiver
└── ui/
    ├── components/   # Reusable Compose components
    ├── navigation/   # Screen routes and NavGraph
    ├── screens/      # Feature screens with ViewModels
    └── theme/        # Material 3 theming
```
