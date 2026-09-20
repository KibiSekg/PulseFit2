# PulseFit (OPSC6312)

Android app matching the Planning & Design doc: Kotlin, Firebase (Auth + Firestore),
Room for offline-first local storage, WorkManager for background sync.

## What's implemented

| Area | Status |
|---|---|
| Firebase Auth (email/password + Google SSO) | Working |
| Firestore user profile | Working |
| Room offline-first workout/meal logging + WorkManager sync | Working |
| Dashboard: greeting, XP/level, daily calories | Working |
| Adaptive Weather Routing (OpenWeatherMap) | Working — needs your API key |
| Diet: log meals, daily calorie total | Working |
| Exercise catalog | Working — reads Firestore `exercises` collection (empty until seeded or wired to ExerciseDB) |
| Map: live location, start/stop workout timer | Working (basic) |
| Route polyline via Google Maps Directions API | TODO — see `MapFragment.kt` |
| Gamified Route Milestones (badges/XP on waypoints) | TODO — see `MapFragment.kt` / `data/model/Gamification.kt` |
| Edamam nutrition auto-lookup on meal entry | TODO — see `DietFragment.kt` (manual entry works now) |
| Push notifications (FCM) | Scaffolded — `PulseFitMessagingService` needs implementing |
| Custom REST backend (Node/Express on Cloud Run) | Not included — this app talks to Firestore directly, a simpler valid alternative for a student project. Swap `data/repository/*` to call your own API instead if the brief requires it. |

## Setup

1. **Open in Android Studio** (Hedgehog/2023.1+ recommended). Let it prompt to install
   the Gradle wrapper if asked, or run `gradle wrapper` once if you have Gradle installed
   locally — the wrapper jar isn't bundled in this handoff.

2. **Firebase project**
   - Create a project at https://console.firebase.google.com
   - Add an Android app with package name `com.pulsefit.app`
   - Enable **Authentication** → Email/Password and Google sign-in methods
   - Enable **Firestore Database** (start in test mode for development)
   - Download `google-services.json` and replace the placeholder at `app/google-services.json`

3. **API keys** — copy `local.properties.example` to `local.properties` in the project
   root and fill in each key (OpenWeatherMap, Google Maps, Edamam, ExerciseDB). This file
   is git-ignored, so keys never get committed.

4. **Google Sign-In** needs two extra things:
   - Your app's SHA-1 fingerprint registered in the Firebase console (Project settings ->
     Your apps -> Add fingerprint). Get it with `./gradlew signingReport`.
   - After enabling the Google provider under Authentication > Sign-in method, copy the
     **Web client ID** shown there into `GOOGLE_WEB_CLIENT_ID` in `local.properties`.

5. Build and run. First launch goes to the Register/Login screen; after auth it lands
   on the Dashboard.

## Architecture notes

- **Offline-first**: `WorkoutRepository` / `MealRepository` always write to Room first
  (so logging never blocks on network), then `SyncWorker` pushes rows marked
  `pendingSync` to Firestore on a 15-minute cycle or when you tap "Force manual cloud
  sync" in Settings.
- **Data models** (`data/model/`) mirror the Data Schema table in the planning doc field-for-field.
- **MVVM**: each screen has a Fragment + ViewModel; ViewModels needing Room take
  `AppDatabase` via `DbViewModelFactory` (see `ui/common/`).
- Firestore layout used by the repositories:
  ```
  users/{uid}                          -> User
  users/{uid}/workouts/{workoutId}     -> WorkoutLog
  users/{uid}/meals/{mealId}           -> MealLog
  users/{uid}/routes/{routeId}         -> Route
  users/{uid}/achievements/{id}        -> Achievement
  exercises/{exerciseId}               -> Exercise (shared catalog)
  challenges/{challengeId}             -> Challenge (shared/global)
  ```

## Known gaps to finish for submission

- Wire the ExerciseDB and Edamam Retrofit services (follow the pattern in
  `data/remote/WeatherApiService.kt` — same shape, different base URL/DTOs).
- Draw the route polyline in `MapFragment` using the Directions API response, and
  check it against `DashboardViewModel`'s weather risk before confirming a route.
- Implement `PulseFitMessagingService.onMessageReceived` to post local notifications.
- Seed the `exercises` and `challenges` Firestore collections (a one-time admin script
  or manual entries in the Firebase console both work for a coursework submission).

## Troubleshooting

- **"Module was compiled with an incompatible version of Kotlin"** — Firebase's Android
  SDKs moved to a Kotlin-first build (dropping the `-ktx` modules) starting with BOM
  v34.0.0, and current Firebase releases are compiled with a newer Kotlin compiler than
  older project templates default to. This project is pinned to Kotlin 2.3.10 for that
  reason. If you bump the Firebase BOM further and hit this error again, bump the Kotlin
  version in the root `build.gradle` to match, or just accept Android Studio's "Upgrade
  Kotlin/AGP/Gradle" prompt if one appears on sync.
- **kapt fails processing Room entities with "Provided Metadata instance has version
  X.Y.0, while maximum supported version is 2.0.0"** — kapt's annotation processor
  bundles its own (infrequently updated) copy of `kotlinx-metadata-jvm`, which caps out
  reading Kotlin metadata around version 2.0. Once your Kotlin version climbs past that
  (as it did here, at 2.3.10), kapt can no longer parse your own data classes. This
  project uses **KSP** for Room instead (see the `ksp` plugin and `ksp
  'androidx.room:room-compiler'` line in `app/build.gradle`) specifically to avoid this
  — KSP reads Kotlin symbols directly rather than through that bundled parser, so it
  doesn't hit the same ceiling.
- **`NoSuchMethodError: addKspConfigurations` when applying the KSP plugin** — recent
  KSP releases (2.3.11+) call an AGP API that doesn't exist before AGP 8.12.0. This
  project is pinned to AGP 8.12.0 + Gradle 8.13 (AGP 8.12's own documented minimum) for
  that reason. If a future KSP bump raises that floor again, check KSP's release notes
  for its current minimum AGP version and match it, along with whatever Gradle version
  that AGP release requires.
- **`kspDebugKotlin` fails with "unexpected jvm signature V" inside a Room DAO query
  processor** — Room 2.6.1 has a known bug in its KSP2 codegen for suspend DAO query
  methods, fixed from 2.7.2 onward. This project is pinned to Room 2.8.5 for that
  reason (`room-runtime`, `room-ktx`, and the `ksp 'androidx.room:room-compiler'` line
  in `app/build.gradle` all need to move together).
