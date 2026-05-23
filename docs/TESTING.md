# FitTrack — Unit Test Reference

All tests live in `shared/src/commonTest` and run against the JVM target via:

```bash
./gradlew :shared:jvmTest
```

**157 tests · 0 failures**

---

## Table of Contents

1. [Test infrastructure](#test-infrastructure)
2. [Fakes and helpers](#fakes-and-helpers)
3. [Use case tests](#use-case-tests)
   - [CalculateStreakUseCase](#calculatestreakusecase-7-tests)
   - [CalculateWeeklyWorkoutsUseCase](#calculateweeklyworkoutsusecase-9-tests)
   - [ValidateWorkoutUseCase](#validateworkoutusecase-11-tests)
4. [ViewModel tests](#viewmodel-tests)
   - [AuthViewModel](#authviewmodel-16-tests)
   - [HomeViewModel](#homeviewmodel-19-tests)
   - [WorkoutDetailViewModel](#workoutdetailviewmodel-5-tests)
   - [ExercisesViewModel](#exercisesviewmodel-13-tests)
   - [RecordViewModel](#recordviewmodel-28-tests)
   - [ProfileViewModel](#profileviewmodel-17-tests)
5. [Repository tests](#repository-tests)
   - [AuthRepositoryImpl](#authrepositoryimpl-14-tests)
   - [UserRepositoryImpl](#userrepositoryimpl-8-tests)
6. [Mapper tests](#mapper-tests-10-tests)

---

## Test infrastructure

### Dependencies

| Library | Purpose |
|---|---|
| `kotlin-test` | `@Test`, `assertEquals`, `assertIs`, etc. |
| `kotlinx-coroutines-test` | `runTest`, `UnconfinedTestDispatcher`, `advanceUntilIdle` |
| `turbine` | `Flow.test {}`, `awaitItem()` — clean reactive assertions |

### Dispatcher setup

Every ViewModel and repository test class sets the Main dispatcher to an `UnconfinedTestDispatcher` so `viewModelScope` coroutines run eagerly on the test thread:

```kotlin
@BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
@AfterTest  fun tearDown() = Dispatchers.resetMain()
```

`UnconfinedTestDispatcher` causes launched coroutines to run synchronously without suspending, making state assertions deterministic without explicit delays.

### In-memory database

Repository tests use the real `FitTrackDatabase` backed by an in-memory SQLite driver (resolved via the `expect/actual` `DatabaseFactory`). Each test creates a fresh isolated database:

```kotlin
val db = DatabaseFactory().create()  // in-memory, discarded after test
```

---

## Fakes and helpers

**File:** `shared/src/commonTest/kotlin/com/harsh/fittrack/fakes/Fakes.kt`

All test doubles are hand-written fakes — no mocking framework is needed or used. KMP `commonTest` runs across multiple targets, and library mocking frameworks (Mockito, MockK) don't support the common source set.

### Domain fixtures

| Function | Returns |
|---|---|
| `testUser` | A `User` with id `user-1`, name `John Doe` |
| `testWorkout(...)` | A completed `Workout` with configurable id, date, volume |
| `testExercise(...)` | An `Exercise` with configurable id, name, muscle, movement |
| `testExerciseEntry(...)` | An `ExerciseEntry` pointing to a workout |
| `testSetEntry(...)` | A `SetEntry` with configurable reps/weight |
| `testExerciseWithSets(...)` | Wraps an entry + its sets |

### Fake repositories

| Fake | Implements | Key controls |
|---|---|---|
| `FakeAuthRepository` | `AuthRepository` | `loginResult`, `registerResult`, `signOutCalled`, `lastLoginEmail`, `lastRegisterName/Email` |
| `FakeUserRepository` | `UserRepository` | `setUnitsCalls` list, `emit(user?)` to push flow updates |
| `FakeWorkoutRepository` | `WorkoutRepository` | `activeWorkout`, per-user/per-id StateFlows, captured call args |
| `FakeExerciseRepository` | `ExerciseRepository` | Filters in-memory list; `emit(list)` replaces the list |
| `FakeApi` | `FitTrackApi` | Configurable response per endpoint; `patchMeThrows` flag; captured args |

### FakeClock

Implements `Clock`; constructed with a fixed year/month/day/hour. Call `setHour(h)` to change the hour within a test.

---

## Use case tests

Use cases are pure functions — no coroutines, no fakes required.

### CalculateStreakUseCase — 7 tests

**File:** `…/domain/usecase/CalculateStreakUseCaseTest.kt`

Tests the unbroken chain of daily workout dates ending on or before today.

| Test | What it verifies |
|---|---|
| `returns 0 when no workouts` | Empty list → 0 |
| `returns 1 when only today` | Single workout today → 1 |
| `returns correct streak for consecutive days ending today` | 3 consecutive days → 3 |
| `counts streak ending yesterday when today has no workout` | Streak still alive if today is not over |
| `streak breaks on gap` | Non-consecutive dates reset the chain |
| `ignores incomplete workouts` | `isCompleted = false` entries are skipped |
| `multiple workouts same day count as one day in streak` | Duplicate dates deduplicated before counting |

### CalculateWeeklyWorkoutsUseCase — 9 tests

**File:** `…/domain/usecase/CalculateWeeklyWorkoutsUseCaseTest.kt`

Tests counting completed workouts within the current Mon–Sun calendar week.

| Test | What it verifies |
|---|---|
| `returns 0 when no workouts` | Empty list → 0 |
| `counts workouts within Mon-Sun week` | Includes Mon–today, excludes previous Sunday |
| `excludes incomplete workouts` | `isCompleted = false` excluded |
| `returns 0 when all workouts are in past weeks` | Prior-week workouts not counted |
| `returns 7 when workout every day of week` | Full week → 7 |
| `today is Monday - only Monday workout counts` | `daysFromMonday = 0` edge case |
| `today is Sunday - full Mon–Sun week counted` | Last day of week boundary |
| `multiple workouts on same day each count separately` | Each workout record counted individually |
| `future workouts beyond today are excluded` | `date > today` filtered out |

### ValidateWorkoutUseCase — 11 tests

**File:** `…/domain/usecase/ValidateWorkoutUseCaseTest.kt`

Tests the three validation rules: must have exercises, each exercise must have sets, each set must have reps > 0.

| Test | What it verifies |
|---|---|
| `returns Valid for a well-formed workout` | Happy path |
| `returns NoExercises error when list is empty` | Empty exercises list |
| `returns NoSets error when exercise has empty set list` | Exercise with no sets; error carries exercise name |
| `returns EmptySet error when set has 0 reps` | Zero reps → EmptySet error |
| `accumulates multiple errors across exercises` | NoSets + EmptySet collected together |
| `set with negative reps is also invalid` | Negative reps caught by `reps <= 0` |
| `reps of 1 is the minimum valid value` | Boundary: reps=1 passes validation |
| `multiple sets in one exercise - only invalid set produces error` | Only the bad set flagged, valid ones pass |
| `all sets in exercise invalid produces error for each set` | Each invalid set generates its own error |
| `multiple exercises with mixed validity accumulates all errors` | Valid exercise doesn't block error collection from others |
| `valid workout with many exercises and sets returns Valid` | 5 exercises × 4 sets → Valid |

---

## ViewModel tests

All ViewModel tests follow the same pattern:
1. Create fakes with the desired initial state
2. Create the ViewModel (its `init` coroutines run immediately under `UnconfinedTestDispatcher`)
3. Call `advanceUntilIdle()` to drain any remaining coroutine work
4. Assert on `vm.state.value`, or use Turbine's `state.test {}` block for multi-step flow assertions

### AuthViewModel — 16 tests

**File:** `…/feature/auth/AuthViewModelTest.kt`

Covers state transitions driven by `AuthRepository.currentUser` flow and the `login`/`register`/`signOut`/`clearError` methods.

| Test | What it verifies |
|---|---|
| `initial state is Loading before user flow emits` | Settled state without user → SignedOut |
| `state is SignedOut when repository emits null user` | Null user → SignedOut |
| `state is SignedIn when repository has existing user` | Non-null initial user → SignedIn |
| `state transitions to SignedIn when user emitted after SignedOut` | Late user emission → SignedIn |
| `state transitions to SignedOut when user revoked` | User → null → SignedOut |
| `login success navigates to SignedIn` | Success path: Loading → SignedIn |
| `login failure sets Error state with message` | Failure path: Loading → Error(message) |
| `login failure uses fallback message when exception has no message` | Exception with null message → "Login failed" |
| `login trims whitespace from email` | Leading/trailing spaces stripped before forwarding |
| `register success navigates to SignedIn` | Success path mirrors login |
| `register failure sets Error state` | Failure path mirrors login |
| `register trims whitespace from name and email` | Both fields stripped |
| `clearError transitions Error to SignedOut` | Error → SignedOut |
| `clearError is a no-op when not in Error state` | SignedIn unchanged by clearError |
| `signOut delegates to repository` | `signOutCalled = true` |
| `signOut clears user state` | SignedIn → SignedOut after sign-out |

### HomeViewModel — 19 tests

**File:** `…/feature/home/HomeViewModelTest.kt`

Covers the derived stats (streak, weekly count, totals), greeting logic, and firstName extraction.

| Test | What it verifies |
|---|---|
| `initial state has isLoading true before data arrives` | Before user emits, state is initial |
| `state populated with user name and greeting` | Happy path |
| `firstName uses first word of full name` | "Mary Jane Watson" → "Mary" |
| `greeting is Good morning at 9am` | Hour 5–11 range |
| `greeting is Good afternoon at 14` | Hour 12–17 range |
| `greeting is Good evening at 20` | Hour 18+ range |
| `greeting at midnight is Good evening` | Hour 0 falls into evening |
| `greeting at noon (12) is Good afternoon` | Boundary: noon = afternoon |
| `greeting at 17 is Good afternoon` | Upper edge of afternoon range |
| `greeting at 18 is Good evening` | Lower edge of evening range |
| `totalWorkouts counts only completed workouts` | Incomplete workouts excluded |
| `recentWorkouts is capped at 10` | 15 workouts → 10 returned |
| `streakDays computed from workouts and today` | Delegates to CalculateStreakUseCase |
| `workoutsThisWeek computed from workouts and today` | Delegates to CalculateWeeklyWorkoutsUseCase |
| `empty workout list shows zeros` | No workouts → all stats zero |
| `no user results in empty state` | Null user → empty firstName |
| `firstName with single-word name uses the full name` | "Cher" → "Cher" |
| `firstName ignores extra leading and trailing spaces` | Trimmed before split |
| `state updates live when user emits new value` | Flow re-emission triggers state update |

### WorkoutDetailViewModel — 5 tests

**File:** `…/feature/home/WorkoutDetailViewModelTest.kt`

Tests the single-workout observer, including state updates and nulling out on deletion.

| Test | What it verifies |
|---|---|
| `initial state is loading with null details` | No data initially |
| `state reflects workout details when available` | Details emitted by repository |
| `state updates when workout details change` | Null → non-null live update |
| `state clears when workout is deleted` | Non-null → null live update |
| `multiple exercises and sets are all present in state` | Nested data structure preserved |

### ExercisesViewModel — 13 tests

**File:** `…/feature/exercises/ExercisesViewModelTest.kt`

Tests `stateIn(WhileSubscribed)` with search query and muscle group filter. A background subscriber is kept active to prevent the shared flow from going cold.

| Test | What it verifies |
|---|---|
| `initial state has empty query and null muscle group` | Default state |
| `all exercises returned when no filter applied` | Empty query + null muscle = all |
| `setQuery filters results by name` | Partial match |
| `setQuery is case-insensitive` | Uppercase query matches |
| `setQuery with empty string returns all exercises` | Clearing query resets filter |
| `setMuscleGroup filters by primary muscle` | Only matching primary muscle returned |
| `setMuscleGroup null clears filter and returns all` | Clearing filter resets results |
| `strengthCount and cardioCount based on full unfiltered list` | Counts never change when filter is active |
| `query and muscle group can be combined` | AND logic: both conditions applied |
| `empty repository produces zero counts` | No exercises → zero everywhere |
| `state updates when repository emits new exercises` | Live data propagation |
| `active muscle group reflected in state` | `activeMuscleGroup` field tracks filter |
| `query text reflected in state` | `query` field tracks search string |

### RecordViewModel — 28 tests

**File:** `…/feature/record/RecordViewModelTest.kt`

The most thoroughly tested ViewModel — covers the full workout recording lifecycle.

**suggestedTitle (4 tests)**

| Test | What it verifies |
|---|---|
| `suggestedTitle is Morning Workout at 9am` | Hour 5–11 |
| `suggestedTitle is Afternoon Workout at 13` | Hour 12–16 |
| `suggestedTitle is Evening Workout at 18` | Hour 17–20 |
| `suggestedTitle is Night Workout at 23` | Hour 21–4 |

**startOrResumeWorkout (2 tests)**

| Test | What it verifies |
|---|---|
| `startOrResumeWorkout restores active workout from repository` | hasStarted=true, exercises populated |
| `startOrResumeWorkout does nothing when no active workout` | workoutId stays null |

**startWorkout (3 tests)**

| Test | What it verifies |
|---|---|
| `startWorkout creates workout and transitions hasStarted` | Repository called, state updated |
| `startWorkout uses blank title fallback from suggestedTitle` | Empty title replaced with suggested |
| `startWorkout without userId is a no-op` | No userId set → workoutId remains null |

**renameTitle (2 tests)**

| Test | What it verifies |
|---|---|
| `renameTitle updates title in state` | Immediate state mutation |
| `renameTitle calls repository when workoutId is set` | `renameWorkout` called with correct args |

**addExercise (3 tests)**

| Test | What it verifies |
|---|---|
| `addExercise appends exercise with initial set to state` | Exercise + set-1 with reps=0 added |
| `addExercise works without a persisted workout (local mode)` | Works before workout is created |
| `addExercise uses exerciseId as name when exercise not found` | Fallback name |

**addSet (3 tests)**

| Test | What it verifies |
|---|---|
| `addSet appends set with incremented setNumber` | setNumber auto-increments |
| `addSet copies weight and reps from previous set` | Last set values are pre-filled |
| `addSet on unknown entryId leaves state unchanged` | No match → no-op |

**updateSet (2 tests)**

| Test | What it verifies |
|---|---|
| `updateSet replaces correct set in state` | Correct set updated in nested structure |
| `updateSet calls repository` | `workoutRepository.updateSet` called with updated set |

**finish (5 tests)**

| Test | What it verifies |
|---|---|
| `finish returns false and sets NoExercises error when no exercises` | Validation blocks finish |
| `finish returns false when exercise has no sets with reps` | Default reps=0 fails validation |
| `finish returns true and sets isCompleting when valid` | Valid workout proceeds |
| `finish with valid workout calls finishWorkout on repository` | Repository notified with duration |
| `finish clears previous validation errors on valid workout` | Error list reset on success |

**clearValidationErrors + discard (4 tests)**

| Test | What it verifies |
|---|---|
| `clearValidationErrors empties error list` | Errors wiped on demand |
| `discard resets state to defaults` | Fresh RecordState |
| `discard with workoutId calls discardWorkout on repository` | Repository cleaned up |
| `discard without workoutId does not call repository` | No-op when nothing was persisted |

### ProfileViewModel — 17 tests

**File:** `…/feature/profile/ProfileViewModelTest.kt`

Covers stat computation, units toggling, and sign-out.

| Test | What it verifies |
|---|---|
| `state has user when signed in` | User populated from repository |
| `state has null user when signed out` | Null user → null in state |
| `isLoading is false after data loads` | Loading flag cleared |
| `totalWorkouts counts only completed workouts` | Incomplete workouts excluded |
| `totalVolumeKg sums volume from completed workouts only` | Incomplete volume excluded |
| `totalVolumeThisMonthKg sums only workouts in current month` | Cross-month filter |
| `streakDays calculated from workout history` | Delegates to CalculateStreakUseCase |
| `default units comes from user profile` | LBS user → LBS in state |
| `toggleUnits switches KG to LBS` | State updated |
| `toggleUnits switches LBS to KG` | Round-trip |
| `toggleUnits calls setUnits on user repository` | Repository notified |
| `toggleUnits can be called multiple times` | Multiple toggles tracked |
| `signOut calls authRepository signOut` | Auth repository delegated |
| `totalVolumeKg is zero when all workouts have zero volume` | Handles zero-volume workouts |
| `totalVolumeThisMonthKg excludes workouts from a different year same month` | Year boundary check |
| `state reflects no workouts for a different user` | User ID scoped correctly |
| `toggleUnits with null user state still toggles correctly` | Units default KG when user=null |

---

## Repository tests

Repository tests use the real `FitTrackDatabase` (in-memory SQLite) and a `FakeApi`. This validates the integration between the repository logic, the SQL queries, and the domain mapper layer.

### AuthRepositoryImpl — 14 tests

**File:** `…/data/repository/AuthRepositoryImplTest.kt`

Tests the JWT session lifecycle: init from persisted token, login/register, and sign-out.

| Test | What it verifies |
|---|---|
| `init with no stored token - currentUser is null and tokenStore is empty` | Cold start |
| `init with stored token but missing user row - currentUser is null` | Token loaded, user not emitted (no DB row) |
| `init with stored token and valid user row - currentUser emits user` | Full restore from DB |
| `init with stored token and LBS units restores correct units` | Units persisted correctly |
| `isSignedIn returns false when no token` | Unsigned state |
| `isSignedIn returns true when token is present` | Signed-in state |
| `login success returns user and stores token` | JWT in TokenStore, user returned |
| `login success emits user on currentUser flow` | Flow emits after login |
| `login with null API response returns failure` | API returns null → Result.failure |
| `login with unknown units string falls back to KG` | Defensive units parsing in storeSession |
| `register success returns user and stores token` | Mirrors login success path |
| `register with null API response returns failure` | Mirrors login failure path |
| `signOut clears token store and emits null user` | TokenStore cleared, null emitted |
| `signOut when already signed out is a no-op` | Double sign-out doesn't throw |

### UserRepositoryImpl — 8 tests

**File:** `…/data/repository/UserRepositoryImplTest.kt`

Tests the user observation flow (which depends on the auth user) and units mutation.

| Test | What it verifies |
|---|---|
| `observeUser emits null when auth user is null` | Signed-out → null flow |
| `observeUser emits user from DB when auth user exists` | DB row mapped to domain User |
| `observeUser emits null when auth user exists but DB row is absent` | Missing row → null |
| `observeUser switches to null when user signs out` | Auth flow change propagates |
| `setUnits updates the units column in DB` | SQL update executed |
| `setUnits calls API patchMe with correct units string` | API notified |
| `setUnits KG calls API with KG string` | Correct string for KG |
| `setUnits does not throw when API fails` | Network errors swallowed silently |

---

## Mapper tests — 10 tests

**File:** `…/data/local/mapper/MappersTest.kt`

Tests the bidirectional conversion between SQLDelight entities and domain models (exercises, workouts, sets, users).

---

## Coverage notes

### What is tested
- All 6 ViewModels, including every public method and state transition
- All 3 UseCases with boundary and edge cases
- `AuthRepositoryImpl` and `UserRepositoryImpl` with a real in-memory database
- Domain → entity and entity → domain mappers

### What is not tested
- **`ExerciseRepositoryImpl`**: The `init` block spawns a `CoroutineScope(Dispatchers.Default)` which runs outside the test scope, making `advanceUntilIdle()` ineffective. Testing would require either refactoring to inject the scope or using a delay-based approach. The filtering logic is covered indirectly via `FakeExerciseRepository` used in all ViewModel tests.
- **`WorkoutRepositoryImpl`**: Has the same `CoroutineScope` issue for the remote sync. Core CRUD logic is covered indirectly via `FakeWorkoutRepository`.
- **`FitTrackApiImpl`**: Ktor HTTP client integration — covered by running the server and exercising via curl or integration tests.
- **Compose UI**: `LoginScreen`, `HomeScreen`, etc. — UI tests would require a Compose test runner (Android Instrumented Tests or iOS XCTest).
