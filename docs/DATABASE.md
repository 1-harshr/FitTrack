# FitTrack — Database Design

**Engine:** SQLite via [SQLDelight 2.0.2](https://cashapp.github.io/sqldelight/)  
**Platforms:** Android (`AndroidSqliteDriver`) · iOS (`NativeSqliteDriver`)  
**File name:** `fittrack.db`  
**Schema version:** 1

---

## Entity-Relationship Overview

```
UserEntity
    │
    └──< WorkoutEntity (userId)
              │
              └──< ExerciseEntryEntity (workoutId)
                        │
                        └──< SetEntryEntity (exerciseEntryId)
```

All foreign keys use `ON DELETE CASCADE`, so deleting a workout removes all its exercises and sets; deleting an exercise entry removes all its sets.

---

## Tables

### `UserEntity`

Stores the authenticated user's profile. One row per Firebase UID.

| Column     | Type    | Constraints          | Notes                          |
|------------|---------|----------------------|--------------------------------|
| `id`       | TEXT    | PK, NOT NULL         | Firebase UID                   |
| `name`     | TEXT    | NOT NULL             | Display name                   |
| `email`    | TEXT    | NOT NULL             |                                |
| `photoUrl` | TEXT    | nullable             | Remote avatar URL              |
| `units`    | TEXT    | NOT NULL DEFAULT 'KG'| `'KG'` or `'LBS'`             |

**Queries:** `upsert`, `selectById`, `setUnits`, `deleteAll`

---

### `WorkoutEntity`

One row per workout session. Rows are created when a workout starts (`isCompleted = 0`) and updated when it finishes (`isCompleted = 1`).

| Column           | Type    | Constraints             | Notes                                      |
|------------------|---------|-------------------------|--------------------------------------------|
| `id`             | TEXT    | PK, NOT NULL            | Random UUID                                |
| `userId`         | TEXT    | NOT NULL                | Ref → `UserEntity.id`                      |
| `title`          | TEXT    | NOT NULL                | User-editable, defaults to time-based name |
| `date`           | TEXT    | NOT NULL                | ISO-8601 local date e.g. `"2026-05-14"`    |
| `startedAt`      | INTEGER | NOT NULL                | Epoch ms — used for precise ordering       |
| `durationSeconds`| INTEGER | NOT NULL DEFAULT 0      | Set on finish                              |
| `totalVolumeKg`  | REAL    | NOT NULL DEFAULT 0.0    | Sum of (weight × reps) set on finish       |
| `isCompleted`    | INTEGER | NOT NULL DEFAULT 0      | Boolean: `0` = active, `1` = done         |

**Index:** `workout_user_started (userId, startedAt DESC)` — powers the home feed and streak queries.

**Queries:** `insert`, `updateTitle`, `markCompleted`, `delete`, `selectAllForUser`, `selectActiveForUser`, `selectById`

> **Active workout invariant:** at most one row per user has `isCompleted = 0` at any time. `selectActiveForUser` uses `LIMIT 1` as a safety net.

---

### `ExerciseEntryEntity`

A single exercise within a workout. Preserves the order exercises were added.

| Column          | Type    | Constraints  | Notes                                           |
|-----------------|---------|--------------|-------------------------------------------------|
| `id`            | TEXT    | PK, NOT NULL | Random UUID                                     |
| `workoutId`     | TEXT    | NOT NULL, FK | → `WorkoutEntity.id` CASCADE DELETE             |
| `exerciseId`    | TEXT    | NOT NULL     | Key into the static `ExerciseCatalog`           |
| `exerciseName`  | TEXT    | NOT NULL     | Name snapshot — survives catalog changes        |
| `orderIndex`    | INTEGER | NOT NULL     | 0-based position within the workout             |

**Index:** `exercise_entry_workout (workoutId, orderIndex)` — covers `selectForWorkout` and `nextOrderIndex`.

**Queries:** `insert`, `delete`, `selectForWorkout`, `nextOrderIndex`

> `exerciseName` is snapshotted from the catalog at log time so historical workouts remain accurate even if the catalog is updated.

---

### `SetEntryEntity`

One row per set within an exercise entry. Weight is always stored in kg; the UI converts to lbs for display when the user's units preference is `'LBS'`.

| Column            | Type    | Constraints  | Notes                                  |
|-------------------|---------|--------------|----------------------------------------|
| `id`              | TEXT    | PK, NOT NULL | Random UUID                            |
| `exerciseEntryId` | TEXT    | NOT NULL, FK | → `ExerciseEntryEntity.id` CASCADE DELETE |
| `setNumber`       | INTEGER | NOT NULL     | 1-based, sequential within an entry   |
| `reps`            | INTEGER | NOT NULL     |                                        |
| `weight`          | REAL    | NOT NULL     | Always kg                              |
| `isCompleted`     | INTEGER | NOT NULL     | Boolean: `0` / `1`                    |

**Index:** `set_entry_exercise (exerciseEntryId, setNumber)` — covers `selectForExerciseEntry` and `nextSetNumber`.

**Queries:** `insert`, `update`, `delete`, `selectForExerciseEntry`, `nextSetNumber`

---

## Design Decisions

| Decision | Rationale |
|---|---|
| `startedAt` (epoch ms) alongside `date` (ISO string) | `date` is used for calendar/streak logic; `startedAt` gives precise ordering when multiple workouts happen the same day |
| `totalVolumeKg` stored on `WorkoutEntity` | Avoids re-aggregating across all sets on every home screen load; computed once at finish time |
| `exerciseName` snapshotted on `ExerciseEntryEntity` | The exercise catalog is static but may be updated; snapshots ensure historical accuracy |
| All IDs are `TEXT` (UUID) | Consistent with Firebase document IDs; simplifies future sync |
| Weight stored in kg only | Single source of truth; unit conversion happens in the UI layer based on `UserEntity.units` |
| Cascade deletes on all FKs | Simplifies discard-workout logic — one `DELETE` on `WorkoutEntity` cleans up the entire tree |

---

## SQLDelight File Layout

```
shared/src/commonMain/sqldelight/com/harsh/fittrack/db/
├── User.sq
├── Workout.sq
├── ExerciseEntry.sq
└── SetEntry.sq
```

Generated interface: `com.harsh.fittrack.db.FitTrackDatabase`  
Platform drivers: `shared/src/androidMain/.../DatabaseFactory.android.kt` · `shared/src/iosMain/.../DatabaseFactory.ios.kt`
