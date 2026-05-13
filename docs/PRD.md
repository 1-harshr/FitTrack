# FitTrack — Product Requirements Document
**Version:** 1.0 — MVP
**Status:** Draft
**Last updated:** 2026-05-13
---
## Table of Contents
1. [Product Overview](#1-product-overview)
2. [Goals](#2-goals)
3. [Target Users](#3-target-users)
4. [Scope](#4-scope)
5. [Tech Stack](#5-tech-stack)
6. [Navigation Architecture](#6-navigation-architecture)
7. [Screen Specifications](#7-screen-specifications)
8. [Data Models](#8-data-models)
9. [Design System](#9-design-system)
10. [Non-Functional Requirements](#10-non-functional-requirements)
11. [Phase Roadmap](#11-phase-roadmap)
12. [Success Criteria](#12-success-criteria)
---
## 1. Product Overview
FitTrack is a cross-platform fitness tracking mobile application built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). It enables users to log workouts, track sets and reps inline, browse a static exercise library, and view their workout history — all from a clean, dark-themed mobile interface.
The MVP focuses on a single core loop:
> **Log a workout → see progress → come back tomorrow.**
Everything outside this loop is deferred to a later phase.
---
## 2. Goals
### Primary
- Deliver a smooth, low-friction workout logging experience
- Demonstrate clean KMP + CMP architecture
- Run reliably on Android and iOS
- Support CRUD for workouts with local persistence
### Secondary
- Offline-first data access
- Firebase Auth for persistent sessions
- Future-ready architecture for cloud sync and social features
---
## 3. Target Users
### Primary
- Gym-goers who want a fast, no-fuss way to log workouts
- Developers or recruiters evaluating KMP architecture
### Secondary
- Users who want to browse exercise instructions
- Users tracking simple personal progress over time
---
## 4. Scope
### MVP — Included
| Area | Features |
|---|---|
| Auth | Google OAuth, Apple OAuth (via Firebase), persistent session, logout |
| Home | Workout history feed, streak, weekly count, total workouts |
| Record workout | Create workout, add exercises (bottom sheet), inline set logging (reps + weight per set), live timer, finish + completion screen |
| Exercise library | Browse static catalog, search, filter by muscle group, exercise detail page |
| Profile | User name, stats (workouts, streak, total volume), units toggle (kg/lbs), logout |
| Persistence | Local SQLDelight storage, Firebase Auth session |
### MVP — Explicitly Excluded
- Rest timer
- Workout templates
- Personal records / PRs
- Progress charts
- Social feed, likes, comments
- Cloud sync / Firestore
- AI recommendations
- Wearable integration
- Exercise GIFs or video
- Workout notes
- Email / password auth (no credentials stored in app)
- Push notifications
---
## 5. Tech Stack
| Layer | Technology |
|---|---|
| UI | Compose Multiplatform (CMP) |
| Shared logic | Kotlin Multiplatform (KMP) |
| Auth | Firebase Auth — Google OAuth + Apple Sign In |
| Local storage | SQLDelight |
| Architecture | MVVM — UI → ViewModel → Repository → Data Source |
| Platforms | Android, iOS |
### Architecture Layers
```
UI Layer (Compose Multiplatform)
    ↓
ViewModel (shared, KMP)
    ↓
Repository (shared, KMP)
    ↓
Local Data Source (SQLDelight)
Remote Data Source (Firebase OAuth)
```
### Shared vs Platform-Specific
**Shared (KMP):**
- ViewModels
- Repositories
- Data models
- Validation logic
- Business logic
**Platform-specific:**
- Firebase Auth native SDK integration
- Platform navigation hooks
---
## 6. Navigation Architecture
### Structure
```
Splash Screen
    ↓
Login Screen (Google OAuth + Apple OAuth)
    ↓ (on success)
Tab Bar ─────────────────────────────────────────────────
│                                                        │
├── Home Tab          ├── Record Tab     ├── Exercises Tab     ├── Profile Tab
│   └── Workout       │   └── Active     │   └── Exercise      │   └── Units
│       Detail            Workout            Detail (browse)       toggle
│       (read only)       ├── Add Exercise
│                         │   Bottom Sheet
│                         │   └── Exercise Detail
│                         │       (sheet context)
│                         └── Workout Complete
│                               ↓ (save)
└────────────────────────── Home Feed ←──────────────────┘
```
### Navigation Rules
| Rule | Detail |
|---|---|
| Tab bar visibility | Persistent on all main tab screens. Hidden on auth screens and Workout Complete |
| Back navigation | Standard back stack per tab. Tabs do not share back stacks |
| Exercise detail context | Same screen, different back destination — pass `source: BROWSE or SHEET` param |
| Workout complete | Full-screen, no tab bar. Save navigates to Home. Discard returns to Record |
| Logout | Revokes Firebase OAuth token, clears local session, navigates to Login, clears back stack |
### Tab Bar
| Tab | Icon | Label |
|---|---|---|
| 1 | Home | Home |
| 2 | Plus | Record |
| 3 | Search | Exercises |
| 4 | User | Profile |
Active tab indicated by lime green dot below icon.
---
## 7. Screen Specifications
---
### 7.1 Splash Screen
**Purpose:** App entry point. Show branding while Firebase Auth checks for a persisted session.
**Behaviour:**
- Display app icon + name
- Check Firebase Auth session state
- Check Firebase Auth session state on launch
- If valid OAuth session exists → navigate to Home (tab bar)
- If no session → navigate to Login
- Transition is automatic, no user action required
**Elements:**
- App icon (lightning bolt in lime green rounded square)
- App name "FitTrack"
- Loading indicator
---
### 7.2 Login Screen
**Purpose:** Authenticate users via Firebase OAuth. No email or password. First-time OAuth flow creates an account automatically — there is no separate sign up screen.
**Elements:**
- App icon + app name "FitTrack" centered
- Tagline: "track every rep" (muted)
- "Continue with Google" button (Google brand icon + label)
- "Continue with Apple" button (Apple brand icon + label, dark fill)
**Button styling:**
- Google button: white background, Google icon left, dark text, full width
- Apple button: black background, Apple icon left, white text, full width
- Both buttons: 52dp height, radius-md, full width, stacked with 12dp gap
**Auth behaviour:**
- Tapping either button → launches native OAuth flow (Google / Apple sign in sheet)
- On success (new user) → Firebase creates account, name + email pulled from OAuth profile automatically → navigate to Home
- On success (returning user) → restores session → navigate to Home
- On failure / cancel → returns to Login screen, show brief error toast if provider returned an error
- Firebase session persists — user will not see Login again unless they explicitly log out
**Apple Sign In — platform rule:**
- Required by App Store guidelines when any third-party OAuth is offered
- Must be present on iOS build
- Can be omitted on Android build
**No validation required** — provider handles all credential validation.
---
### 7.3 Sign Up Screen
**Removed.** OAuth-only auth means there is no separate sign up screen. First-time sign in via Google or Apple automatically creates a Firebase account. Name and email are pulled from the OAuth provider profile. No user input required.
---
### 7.4 Home Screen
**Purpose:** Self feed showing workout history and quick stats. Primary landing screen after login.
**Elements:**
**Top bar:**
- Greeting: "Good morning / afternoon / evening" (time-based)
- User first name
- Avatar circle with user initials
**Stats row (3 cards):**
- Workout streak (days) with fire emoji
- Workouts this week (count)
- Total workouts (count)
**Recent workouts feed:**
- Stacked workout cards, newest first
- Each card shows:
  - Workout title (bold)
  - Date badge (today = lime green, past = dark)
  - Exercise summary (e.g. "Bench · OHP · Triceps · 4 exercises")
  - Duration and total volume (kg or lbs based on units setting)
- Tapping a card → navigates to Workout Detail
**Empty state:**
- Shown when user has no workouts logged
- Message: "No workouts yet. Hit the + tab to start your first one."
**Streak logic:**
- Streak increments when user logs at least one workout per calendar day
- Streak breaks if a day passes with no workout logged
- Rest days do not maintain streak
---
### 7.5 Workout Detail Screen
**Purpose:** Read-only view of a completed past workout.
**Access:** Tap a workout card on the Home feed.
**Elements:**
- Back arrow → Home
- Workout title
- Date and duration
- Total volume
- Exercise list:
  - Exercise name
  - Set rows: set number · reps · weight
- No edit capability in MVP
---
### 7.6 Record Workout Screen (Active Workout)
**Purpose:** Core workout logging screen. Starts when user taps the Record tab.
**Behaviour on tab tap:**
- If no active workout: starts a new workout immediately (auto-title: "Workout — [date]", editable)
- If an active workout exists (user navigated away mid-session): resumes it
- Live timer starts on screen open and runs in background while user navigates other tabs
**Elements:**
**Top bar:**
- Workout title (editable inline, tapping opens a text field)
- Live workout timer (format: HH:MM:SS, lime green pill)
**Exercise sections (one per added exercise):**
- Exercise name as section header
- Set rows (inline, one row per set):
  - Set number (muted)
  - Reps input box (numeric)
  - Weight input box (numeric, respects units setting)
  - Circular checkmark button (tap to mark set complete — lime green fill when done)
- "+ add set" text link below the last set row
**Below all exercises:**
- "+ add exercise" centered text link → opens Add Exercise bottom sheet
**Bottom:**
- "Finish workout" primary button → navigates to Workout Complete
**Empty state (no exercises added yet):**
- Prompt: "Tap '+ add exercise' to start building your workout"
---
### 7.7 Add Exercise Bottom Sheet
**Purpose:** Search and add exercises to the active workout. Presented as a modal bottom sheet over the Record screen.
**Trigger:** Tap "+ add exercise" on the Record screen.
**Elements:**
- Sheet handle bar at top
- Header: "Add exercise"
- Search input (full width, autofocused on open)
- Muscle group filter pills (horizontal scroll):
  - All · Chest · Back · Legs · Arms · Shoulders · Core
  - One active at a time, defaults to All
- Exercise list (filtered by search + active pill):
  - Each row: icon square · exercise name · muscle group + equipment · "+ Add" button
  - Tapping "+ Add" adds exercise to active workout and dismisses sheet
  - Tapping the row (not "+ Add") → pushes to Exercise Detail (sheet context)
**Dismiss:**
- Swipe down or tap outside → returns to active workout with no changes
- Adding an exercise → auto-dismisses and scrolls to added exercise in workout
---
### 7.8 Exercise Library Screen
**Purpose:** Browse and search the full static exercise catalog.
**Elements:**
- Header: "Exercises"
- Search input (full width)
- Muscle group filter pills (horizontal scroll):
  - All · Chest · Back · Legs · Arms · Shoulders · Core
- Exercise list (filtered results):
  - Each row: icon square · exercise name · muscle group + equipment · chevron right
  - Tapping a row → navigates to Exercise Detail (browse context)
**Static catalog scope (MVP):**
- Minimum 50 exercises covering all major muscle groups
- Each exercise has: name, primary muscle group, secondary muscles, equipment type, written instructions
---
### 7.9 Exercise Detail Screen
**Purpose:** Show full exercise information. Appears in two contexts.
**Contexts:**
| Context | Source | Back destination |
|---|---|---|
| Browse | Exercise Library tab | Exercise Library |
| Sheet | Add Exercise bottom sheet | Add Exercise bottom sheet |
**Elements:**
- Back arrow (destination depends on context)
- Exercise name (header)
- Demo image placeholder (static image in MVP, no GIF)
- Tag pills: primary muscle · secondary muscle · equipment · movement type
- Section "Instructions": written step-by-step text
- Section "Muscles worked":
  - Primary muscle card (lime green label)
  - Secondary muscle card (muted label)
- "Add to workout" primary button (only shown in sheet context — adds exercise and dismisses sheet back to active workout)
---
### 7.10 Workout Complete Screen
**Purpose:** Celebration and summary screen shown after tapping "Finish workout". Full screen, no tab bar.
**Elements:**
- Trophy icon in lime green circle
- "Workout done!" heading
- Workout name (subtext)
- Stats row (3 cards):
  - Duration
  - Exercise count
  - Total volume (kg or lbs)
- Summary section — one card per exercise:
  - Exercise name · set count
  - Best set (e.g. "85 kg × 10")
- "Save workout" primary button → saves to SQLDelight, navigates to Home
- "Discard" ghost button → discards workout, navigates to Home (with confirmation dialog)
---
### 7.11 Profile Screen
**Purpose:** User info, stats summary, settings, and logout.
**Elements:**
**User section:**
- Avatar circle (initials)
- Full name
- Email address
**Stats row (3 cards):**
- Total workouts
- Current streak (with fire emoji)
- Total volume lifted (all time)
**Settings section:**
- Units row: "kg" or "lbs" — tapping toggles between the two. All weight values in app update immediately.
- Notifications row (no-op in MVP, navigates nowhere — placeholder for Phase 2)
**Logout:**
- "Log out" row in red
- Tapping clears Firebase session, navigates to Login, clears back stack
---
## 8. Data Models
### User
```kotlin
data class User(
    val id: String,          // Firebase UID
    val name: String,        // pulled from OAuth provider profile
    val email: String,       // pulled from OAuth provider profile
    val photoUrl: String?,   // profile photo URL from OAuth provider (optional)
    val units: Units         // KG or LBS, default KG
)
enum class Units { KG, LBS }
```
> No password stored. Authentication is fully delegated to Firebase OAuth (Google / Apple). Firebase UID is the stable identifier used to key all local and future remote data.
### Workout
```kotlin
data class Workout(
    val id: String,
    val userId: String,
    val title: String,
    val date: LocalDate,
    val durationSeconds: Long,
    val isCompleted: Boolean
)
```
### ExerciseEntry
```kotlin
data class ExerciseEntry(
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val orderIndex: Int      // position in workout
)
```
### SetEntry
```kotlin
data class SetEntry(
    val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double,      // always stored in kg, converted on display
    val isCompleted: Boolean
)
```
### Exercise (static catalog)
```kotlin
data class Exercise(
    val id: String,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup>,
    val equipment: Equipment,
    val movementType: MovementType,
    val instructions: List<String>
)
enum class MuscleGroup {
    CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, GLUTES, CALVES
}
enum class Equipment {
    BARBELL, DUMBBELL, CABLE, MACHINE, BODYWEIGHT, KETTLEBELL, BAND
}
enum class MovementType {
    COMPOUND, ISOLATION, CARDIO
}
```
### Key model decisions
- **Weight always stored in kg.** Display layer converts to lbs using `weight * 2.20462` when units = LBS. This prevents data inconsistency if user switches units.
- **SetEntry is a child of ExerciseEntry** — not flat on ExerciseEntry. Each set can have independent reps and weight, supporting progressive overload tracking.
- **durationSeconds on Workout** — stored as seconds elapsed. UI formats to HH:MM.
- **isCompleted on SetEntry** — drives the checkmark state in the UI without requiring a separate status table.
---
## 9. Design System
### Color Tokens
| Token | Hex | Usage |
|---|---|---|
| `color-background` | `#0a0a0a` | Screen background |
| `color-surface` | `#1a1a1a` | Cards, nav bar |
| `color-surface-raised` | `#1e1e1e` | Input fields, inner cards |
| `color-border` | `#2a2a2a` | Card borders, dividers |
| `color-accent` | `#a3e635` | Active states, primary buttons, key metrics, CTAs |
| `color-accent-subtle` | `#1a2a0a` | Accent background tint (e.g. timer pill) |
| `color-text-primary` | `#ffffff` | Primary text |
| `color-text-secondary` | `#888888` | Muted text, subtitles |
| `color-text-tertiary` | `#555555` | Hints, timestamps |
| `color-danger` | `#e24b4a` | Destructive actions (logout, discard) |
| `color-accent-text` | `#0a0a0a` | Text on lime green backgrounds |
### Typography
| Style | Size | Weight | Usage |
|---|---|---|---|
| `heading-xl` | 22sp | 700 | Screen headers |
| `heading-lg` | 18sp | 700 | Section headers, workout titles |
| `heading-md` | 15sp | 600 | Card titles, exercise names |
| `body` | 14sp | 400 | Body text, instructions |
| `caption` | 12sp | 400 | Muted subtitles, timestamps |
| `metric` | 24sp | 700 | Stats numbers (streak, volume) |
| `label` | 11sp | 500 | Section labels, pill text |
### Spacing Scale
| Token | Value | Usage |
|---|---|---|
| `space-xs` | 4dp | Icon gaps, tight internal padding |
| `space-sm` | 8dp | Component internal padding |
| `space-md` | 12dp | Between cards, row gaps |
| `space-lg` | 16dp | Screen horizontal padding |
| `space-xl` | 24dp | Section gaps |
### Border Radius
| Token | Value | Usage |
|---|---|---|
| `radius-sm` | 6dp | Pills, chips, small badges |
| `radius-md` | 8dp | Input fields, set rows |
| `radius-lg` | 12dp | Cards, bottom sheet |
| `radius-xl` | 20dp | Stat cards |
| `radius-full` | 9999dp | Avatar circles, FAB, checkmark buttons |
### Component Specs
#### Cards
```
background:  color-surface (#1a1a1a)
border:      0.5dp solid color-border (#2a2a2a)
radius:      radius-lg (12dp)
padding:     12dp vertical, 14dp horizontal
```
#### Input Fields
```
background:  color-surface-raised (#1e1e1e)
border:      0.5dp solid color-border (#2a2a2a)
radius:      radius-md (8dp)
height:      48dp
padding:     0 12dp
left icon:   color-text-tertiary, 18sp
```
#### Primary Button
```
background:  color-accent (#a3e635)
text color:  color-accent-text (#0a0a0a)
radius:      radius-md (8dp)
height:      52dp
font:        heading-md, weight 600
full width
```
#### Ghost Button
```
background:  transparent
border:      0.5dp solid color-border (#2a2a2a)
text color:  color-text-secondary (#888888)
radius:      radius-md (8dp)
height:      48dp
full width
```
#### Stat Card
```
background:  color-surface (#1a1a1a)
border:      0.5dp solid color-border (#2a2a2a)
radius:      radius-xl (20dp)
padding:     12dp
number:      metric style, color-accent (#a3e635)
label:       caption style, color-text-tertiary
```
#### Set Row
```
height:      44dp
set number:  caption, color-text-tertiary, width 32dp
reps box:    set-box style, flex 1
weight box:  set-box style, flex 1
checkmark:   32dp circle, border color-accent, fill color-accent when done
```
#### Bottom Navigation
```
background:  color-surface (#1a1a1a)
border-top:  0.5dp solid color-border (#2a2a2a)
height:      60dp
icon size:   22sp
active icon: color-accent (#a3e635)
inactive:    color-text-tertiary (#555555)
active dot:  4dp circle, color-accent, below active icon
```
#### Muscle Group Pills
```
background (inactive): color-surface-raised (#1e1e1e)
background (active):   color-accent (#a3e635)
text (inactive):       color-text-secondary (#888888)
text (active):         color-accent-text (#0a0a0a)
radius:                radius-full
padding:               6dp vertical, 14dp horizontal
font:                  label style
```
### Animation
| Interaction | Animation |
|---|---|
| Screen transitions | Slide in from right (push), slide out to right (pop) |
| Bottom sheet | Slide up from bottom, 300ms ease-out |
| Card tap | Scale to 0.97, 100ms |
| Set checkmark | Fill scale from 0 → 1, 150ms spring |
| Tab switch | Crossfade, 200ms |
| Stat cards on load | Fade in staggered, 100ms delay per card |
Keep animations subtle. No heavy motion systems.
---
## 10. Non-Functional Requirements
### Performance
- Screen transitions under 300ms
- Workout logging interactions under 100ms (no perceived lag on set checkmark)
- Smooth 60fps scrolling on workout feed and exercise list
### Reliability
- No data loss on app restart or crash — workouts saved incrementally to SQLDelight
- Offline-first — all core features work without network (except initial login)
- Firebase session persists indefinitely until user logs out
### Maintainability
- All business logic in shared KMP module — no duplication between platforms
- ViewModels in shared module, observe as StateFlow in Compose UI
- Repository pattern — UI never talks directly to data source
### Scalability
- Data model supports future Firestore sync without schema migration
- Weight stored in kg internally — units conversion is a display concern only
- `source` param pattern on Exercise Detail enables context-aware navigation
### Accessibility
- Minimum touch target size: 44dp × 44dp
- Text contrast ratio: minimum 4.5:1 on all surfaces
- All interactive elements have content descriptions
---
## 11. Phase Roadmap
### Phase 1 — MVP (current)
- Firebase Auth (email/password)
- Local workout CRUD
- Inline set logging
- Static exercise catalog
- Home feed
- Profile + units setting
### Phase 2 — Polish + Retention
- Rest timer (between sets, configurable duration)
- Workout templates (save and reuse)
- Exercise search improvements (equipment filter, movement type filter)
- Personal records (auto-detect new PRs on save)
- Firestore cloud sync
- Email / password auth (no credentials stored in app)
- Basic progress charts (volume over time)
### Phase 3 — Growth
- Progress charts (per exercise, over time)
- Body weight tracking
- Workout notes
- Share workout (image card export)
- Push notifications (streak reminders)
### Phase 4 — Intelligence
- AI-generated workout plans
- Smart volume recommendations
- Adaptive workout scheduling
### Phase 5 — Platform
- Apple Health integration
- Google Health Connect integration
- Wearable support (Apple Watch, Wear OS)
---
## 12. Success Criteria
### MVP is successful if:
| Criterion | Measure |
|---|---|
| Core loop works | User can log a full workout (create → add exercises → log sets → complete) without errors |
| Cross-platform | App runs on Android and iOS with identical behaviour |
| Auth works | User can sign in via Google or Apple OAuth and stay logged in across app restarts |
| Data persists | Workout history survives app close, restart, and device reboot |
| UI feels polished | Dark theme, lime green accents, smooth transitions — consistent across screens |
| Architecture is clean | Shared KMP logic, MVVM pattern, no platform-specific business logic |
| Exercise library | At least 50 exercises, all browsable and searchable |
| Performance | No jank, no loading spinners on local data, transitions under 300ms |
---
*FitTrack PRD v1.0 — MVP*
*Generated from product brainstorm session, May 2026*
