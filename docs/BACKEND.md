# FitTrack — Backend Design

## Overview

Single Ktor JVM monolith. All state lives in PostgreSQL. Firebase handles auth — the server only verifies tokens, never issues them.

**Module:** `:server`  
**Runtime:** Ktor 3.4 on Netty  
**Database:** PostgreSQL 16 via Exposed ORM + HikariCP  
**Auth:** Firebase Admin SDK (server-side ID token verification)  
**Serialization:** kotlinx.serialization JSON  
**DI:** Koin  

---

## Architecture

```
client ──► Ktor (Netty)
              │
              ├── plugins/Authentication   ← Firebase token → FirebasePrincipal
              ├── plugins/Serialization    ← kotlinx.json content negotiation
              ├── plugins/StatusPages      ← unified error responses
              │
              ├── feature/user             ← GET/PATCH /me
              ├── feature/exercise         ← GET /exercises
              ├── feature/workout          ← CRUD /workouts
              └── feature/sync             ← GET /sync/status
                        │
                        └── Repository interfaces
                                  │
                                  └── Exposed DSL ──► PostgreSQL
```

---

## Database Schema

```sql
CREATE TABLE users (
    id           TEXT        PRIMARY KEY,          -- Firebase UID
    name         TEXT        NOT NULL,
    email        TEXT        NOT NULL,
    photo_url    TEXT,
    units        TEXT        NOT NULL DEFAULT 'KG',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exercises (
    id                TEXT        PRIMARY KEY,     -- stable slug e.g. "bench_press"
    name              TEXT        NOT NULL,
    primary_muscle    TEXT        NOT NULL,
    secondary_muscles TEXT[]      NOT NULL DEFAULT '{}',
    equipment         TEXT        NOT NULL,
    movement_type     TEXT        NOT NULL,
    instructions      TEXT[]      NOT NULL DEFAULT '{}',
    is_custom         BOOLEAN     NOT NULL DEFAULT FALSE,
    catalog_version   INTEGER     NOT NULL DEFAULT 1,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE workouts (
    id               TEXT        PRIMARY KEY,
    user_id          TEXT        NOT NULL REFERENCES users(id),
    title            TEXT        NOT NULL,
    date             DATE        NOT NULL,
    started_at       BIGINT      NOT NULL,          -- epoch ms
    duration_seconds INTEGER     NOT NULL DEFAULT 0,
    total_volume_kg  NUMERIC(10,2) NOT NULL DEFAULT 0,
    deleted_at       TIMESTAMPTZ,                   -- soft delete
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exercise_entries (
    id            TEXT    PRIMARY KEY,
    workout_id    TEXT    NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id   TEXT    NOT NULL,
    exercise_name TEXT    NOT NULL,
    order_index   INTEGER NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE set_entries (
    id                  TEXT           PRIMARY KEY,
    exercise_entry_id   TEXT           NOT NULL REFERENCES exercise_entries(id) ON DELETE CASCADE,
    set_number          INTEGER        NOT NULL,
    reps                INTEGER        NOT NULL,
    weight_kg           NUMERIC(6,2)   NOT NULL,
    is_completed        BOOLEAN        NOT NULL,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workouts_user       ON workouts(user_id, started_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_exercise_entries_w  ON exercise_entries(workout_id, order_index);
CREATE INDEX idx_set_entries_e       ON set_entries(exercise_entry_id, set_number);
CREATE INDEX idx_exercises_version   ON exercises(catalog_version);
```

---

## API Contract

**Base URL:** `https://api.fittrack.app/v1`  
**Auth header:** `Authorization: Bearer <firebase_id_token>`  
All timestamps in responses are ISO-8601. All weights in **kg**.

### `GET /me`
Returns the authenticated user's profile. Creates the user row if it doesn't exist yet (first login).
```json
{
  "id": "firebase_uid",
  "name": "Harsh Ranjan",
  "email": "harsh@example.com",
  "photoUrl": "https://...",
  "units": "KG"
}
```

### `PATCH /me`
Update mutable profile fields.
```json
{ "units": "LBS" }
```
Returns the updated user.

---

### `GET /exercises?sinceVersion=0`
Returns exercises added or changed after `sinceVersion`, plus the current max version.
Client should persist `latestVersion` and use it as `sinceVersion` on the next call.
```json
{
  "latestVersion": 4,
  "exercises": [
    {
      "id": "bench_press",
      "name": "Bench Press",
      "primaryMuscle": "CHEST",
      "secondaryMuscles": ["ARMS", "SHOULDERS"],
      "equipment": "BARBELL",
      "movementType": "COMPOUND",
      "instructions": ["Step 1...", "Step 2..."],
      "catalogVersion": 1
    }
  ]
}
```

---

### `GET /workouts?cursor=<cursor>&limit=20`
Cursor-based pagination, most-recent-first. Cursor encodes `startedAt` epoch ms as base64.
```json
{
  "nextCursor": "MTc0NzIwOTYwMDAwMA==",
  "workouts": [
    {
      "id": "uuid",
      "title": "Morning Workout (May 14)",
      "date": "2026-05-14",
      "startedAt": 1747209600000,
      "durationSeconds": 3120,
      "totalVolumeKg": 4250.0,
      "exercises": [
        {
          "id": "uuid",
          "exerciseId": "bench_press",
          "exerciseName": "Bench Press",
          "orderIndex": 0,
          "sets": [
            { "id": "uuid", "setNumber": 1, "reps": 8, "weightKg": 80.0, "isCompleted": true }
          ]
        }
      ]
    }
  ]
}
```

### `POST /workouts`
Sync a completed workout. Idempotent — if the `id` already exists, returns `200 OK` with the existing record.
Body matches the workout object above. Returns `201 Created`.

### `GET /workouts/{id}`
Single workout. Returns `404` if not found or soft-deleted.

### `DELETE /workouts/{id}`
Soft-deletes the workout (sets `deleted_at`). Returns `204 No Content`.

---

### `GET /sync/status`
Bootstrap call on fresh install — returns the user's latest synced workout cursor and current exercise catalog version.
```json
{
  "workoutCursor": "MTc0NzIwOTYwMDAwMA==",
  "catalogVersion": 4
}
```

---

## Error Format

All errors follow RFC 9457 Problem Details:
```json
{
  "type": "https://api.fittrack.app/errors/not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "Workout abc123 does not exist or has been deleted."
}
```

---

## Project Structure

```
server/src/main/kotlin/com/harsh/fittrack/
├── Application.kt
├── di/AppModule.kt
├── plugins/
│   ├── Authentication.kt   ← Firebase bearer token validation
│   ├── Database.kt         ← HikariCP + Exposed setup + schema creation
│   ├── Routing.kt          ← Mounts all feature routes
│   ├── Serialization.kt    ← kotlinx.json content negotiation
│   └── StatusPages.kt      ← Unified error responses
├── domain/
│   ├── model/              ← Server-side domain models
│   └── repository/         ← Repository interfaces
├── data/
│   ├── table/              ← Exposed table definitions
│   └── repository/         ← Exposed implementations
└── feature/
    ├── user/UserRoutes.kt
    ├── exercise/ExerciseRoutes.kt
    ├── workout/WorkoutRoutes.kt
    └── sync/SyncRoutes.kt
```

---

## Running Locally

```bash
# Start PostgreSQL
docker run -d --name fittrack-db \
  -e POSTGRES_DB=fittrack \
  -e POSTGRES_USER=fittrack \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 postgres:16

# Set env vars
export DB_URL=jdbc:postgresql://localhost:5432/fittrack
export DB_USER=fittrack
export DB_PASSWORD=secret
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-service-account.json

./gradlew :server:run
```

## Docker

```bash
docker build -t fittrack-server .
docker run -p 8080:8080 \
  -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... \
  -e GOOGLE_APPLICATION_CREDENTIALS=/creds.json \
  -v /path/to/creds.json:/creds.json \
  fittrack-server
```
