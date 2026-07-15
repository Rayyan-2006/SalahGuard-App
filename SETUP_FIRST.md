# One manual step before you open this in Android Studio

Everything in this project is fixed and merged **except one file I'm not able
to generate myself**: `gradle/wrapper/gradle-wrapper.jar`. It's a compiled
binary (not text), so I can't write it directly — but it's completely
generic and takes 30 seconds to add:

### Option A — let Android Studio fix it (try this first)
1. Open this `SalahGuard` folder in Android Studio (**Open**, not New Project).
2. If Android Studio shows a banner like *"Gradle wrapper is missing"* or
   *"Create Gradle wrapper?"* — click it. Done, skip Option B.

### Option B — copy the jar from any existing Android Studio project
1. In Android Studio: **File → New → New Project → Empty Activity** → create
   a throwaway project anywhere (name doesn't matter).
2. In that new project's folder, find `gradle/wrapper/gradle-wrapper.jar`.
3. Copy that one file into this project's `gradle/wrapper/` folder
   (next to the `gradle-wrapper.properties` I already included).
4. Open **this** SalahGuard project in Android Studio and let it sync.
   It'll download Gradle 8.7 automatically on first sync (needs internet).

You only ever need to do this once. Delete this file afterward.

---

## What I fixed in this version

1. **Missing Gradle wrapper** — your skeleton's `gradle/wrapper/` folder was
   completely empty (no `gradlew`, `gradlew.bat`, `gradle-wrapper.jar`,
   `gradle-wrapper.properties`). This is almost certainly why Android Studio
   couldn't sync/build at all. Added `gradle-wrapper.properties` pinned to
   Gradle 8.7 (matches your AGP 8.5.0 / Kotlin 1.9.24 setup).
2. **Merged the dark-theme update correctly** — the dark-theme zip was a
   *patch*, not a standalone project. It replaces `Color.kt`, `Theme.kt`,
   `Type.kt`, adds `Font.kt`, a new `HomeScreen.kt` + `HomeUiState.kt`, and
   3 new components (`BottomNavBar`, `PrayerCountdownCard`, `QuranVerseCard`).
   All merged in; I checked the whole codebase for leftover references to
   removed symbols (old `greeting` field, old `MinimalGold`/`MidnightBlue`
   colors) — none remain.
3. **Added `res/font/`** — didn't exist in the skeleton; the Poppins +
   Noto Naskh Arabic `.ttf` files now live there so `Font.kt`'s
   `R.font.poppins_light` etc. actually resolve.
4. **Added `material-icons-extended`** dependency (needed for the bottom
   nav bar icons — was missing from the base skeleton's `build.gradle.kts`).

Home screen now matches your reference mockup: greeting, live prayer
countdown card, today's intention, Quran verse of the day, bottom nav —
all in the dark ivory/forest-green/gold palette from the design doc.
