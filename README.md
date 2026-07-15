# SalahGuard — Project Skeleton (Step 1: Architecture)

This is a working Android Studio project skeleton for SalahGuard, built exactly
to the tech stack and philosophy in the vision document: Kotlin, Jetpack
Compose, Material 3, MVVM, Clean Architecture, Repository pattern, Hilt, Room,
Coroutines/Flow, Navigation Compose.

## How to open it

1. Install **Android Studio** (Koala or newer).
2. `File > Open` and select this `SalahGuard/` folder.
3. Let Gradle sync (it will download dependencies the first time — needs internet).
4. Run on an emulator or device (min SDK 26 / Android 8.0+).

You should see: Splash screen (1.2s) → Home screen showing "Peace be with you."
That confirms the full chain works end to end:
`MainActivity → NavHost → HomeScreen → HomeViewModel (Hilt) → GetTodayPrayersUseCase → PrayerRepository → Room`

## Folder structure (Clean Architecture, 3 layers)

```
data/            <- HOW: Room entities, DAOs, repository implementations
  local/
    entity/      PrayerEntity (Room table)
    dao/         PrayerDao (queries)
  repository/    PrayerRepositoryImpl (maps entities <-> domain models)

domain/          <- WHAT: pure Kotlin, zero Android/Room imports
  model/         Prayer, PrayerName, PrayerStatus
  repository/    PrayerRepository interface (dependency inversion point)
  usecase/       GetTodayPrayersUseCase (one class per business action)

presentation/    <- UI: Compose screens + ViewModels (MVVM)
  theme/         Color.kt, Type.kt, Theme.kt — your exact palette from the doc
  navigation/    SalahGuardDestination (routes), SalahGuardNavHost (graph)
  screens/
    splash/      SplashScreen
    home/        HomeScreen, HomeViewModel, HomeUiState
  components/    (empty — reusable buttons/cards go here in Step 2)

di/              Hilt modules: DatabaseModule, RepositoryModule
```

**The dependency rule:** `presentation` depends on `domain`, `data` depends on
`domain`, but `domain` depends on nothing. This is why `domain/model/Prayer.kt`
has no Room annotations — only `data/local/entity/PrayerEntity.kt` does, and
`PrayerRepositoryImpl` translates between them. This means you could delete
Room entirely and swap in a different database without touching a single
ViewModel or Composable.

## Already implemented (so it's not just empty folders)

- Full Hilt DI graph (App → Database → DAO → Repository → UseCase → ViewModel)
- Room database with a `prayers` table
- One real feature: fetching today's prayers as a `Flow`, ready for `HomeScreen`
  to collect once you wire the UI to actually display them
- Your exact color palette (Warm Ivory, Pearl White, Soft Sand, Stone, Deep
  Emerald, Minimal Gold, Midnight Blue) plus the 5 time-of-day tint colors
  from Section 17, ready to use
- Navigation graph with placeholders for Onboarding, Prayer, Reflection,
  Quran, Settings — add one `composable(...)` block per screen as you build it

## Deliberate placeholders (next steps, per the doc's build order)

1. **Design System** — build reusable components in `presentation/components/`
   (SalahCard, PrimaryButton, StreakBadge, etc.) using the theme colors, plus
   the slow-fade/breathing-background motion described in Section 16.
2. **Navigation polish** — add Onboarding, Prayer, Reflection, Quran, Settings
   routes to `SalahGuardNavHost.kt`.
3. **Real screens**, one at a time, in the order the doc specifies.

## A note on scope

This skeleton deliberately implements only one vertical slice (prayer
tracking) all the way through every layer, rather than stubbing out every
feature shallowly. That's intentional — it gives you one proven, working
pattern to copy for Quran progress, reflections, streaks, etc., rather than
ten half-finished ones.
