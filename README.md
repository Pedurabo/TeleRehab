# TeleRehab

TeleRehab is a native Android tele-rehabilitation application for therapist-managed rehabilitation programs, guided patient exercise sessions, progress monitoring, and reliable local-first session synchronization.

The project is built as a production-oriented Android portfolio application with an emphasis on clean architecture, offline resilience, role-based workflows, deterministic synchronization, and camera-assisted movement analysis.

## Current status

TeleRehab has a working end-to-end MVP with patient and therapist workflows validated on a physical Android device.

### Therapist workflow

- Email/password therapist authentication
- Therapist dashboard and sign out
- Therapist-managed patient provisioning
- Firebase Authentication accounts for patients
- Generated temporary patient credentials
- Therapist-to-patient relationships stored in Firestore
- Patient selection
- Exercise assignment creation and management
- Weekly adherence targets
- Recent patient session history
- Repetition-history visualization
- Knee-range progress visualization
- Per-assignment performance summaries
- Patient-scoped session and progress loading

### Patient workflow

- Email/password patient authentication
- Patient sign out and role switching
- Assigned-exercise dashboard
- Guided rehabilitation sessions
- Camera-based pose analysis
- Repetition tracking
- Knee-angle range tracking
- Recent session history
- Exercise-specific progress summaries
- Offline-first session persistence
- Background cloud synchronization
- Safe session completion
- Explicit interruption when leaving a guided session
- Interrupted-session recovery after app/process restart
- Protection against concurrent active sessions

## Rehabilitation exercise

The current guided rehabilitation exercise is **Seated Knee Extension**.

The standard assignment currently uses:

- 10 repetitions per session
- 3 sessions per week
- Flexed threshold at or below 100°
- Extended threshold at or above 160°

During a guided session, TeleRehab derives movement metrics locally from camera pose landmarks.

Raw rehabilitation video is not uploaded or stored by default.

## Session lifecycle

Exercise sessions are modeled explicitly as:

- `IN_PROGRESS`
- `COMPLETED`
- `INTERRUPTED`
- `CANCELLED`

Session lifecycle handling includes:

- prevention of concurrent patient sessions
- guarded completion and interruption transitions
- interruption on explicit guided-session exit
- interruption before patient sign out
- recovery of stale in-progress sessions after restart
- protection against finish/back race conditions

This keeps locally persisted session state consistent even when the normal happy path is interrupted.

## Synchronization

TeleRehab uses local persistence first and synchronizes terminal session state to Firestore through WorkManager.

The synchronization pipeline includes:

- network-constrained background work
- patient-scoped pending-session queries
- retry behavior when authentication is temporarily unavailable
- permanent-failure handling
- exclusion of active `IN_PROGRESS` sessions
- terminal-session synchronization only
- multi-batch draining for larger pending queues
- fresh synchronization requests when the patient experience loads
- idempotent remote writes using stable session identifiers

Local synchronization state is represented separately from the rehabilitation session lifecycle.

## Architecture

TeleRehab follows a layered, local-first architecture.

- **Domain layer** contains rehabilitation, assignment, authentication, session, and synchronization rules without depending directly on Compose, Room, Firebase, or CameraX.
- **Room** provides durable local assignment and session state.
- **WorkManager** performs resilient background synchronization.
- **Firebase Authentication** provides patient and therapist identity.
- **Cloud Firestore** provides shared therapist/patient state.
- **Jetpack Compose** provides the application UI.
- **CameraX** supplies camera frames for guided sessions.
- **ML Kit Pose Detection** provides body landmarks used by the movement-analysis pipeline.
- **Hilt** provides dependency injection.

Domain models, Room entities, and Firestore representations remain separated rather than sharing persistence-specific models across layers.

For the original product and architecture baseline, see:

`docs/architecture/MILESTONE_0_PRODUCT_ARCHITECTURE.md`

## Patient progress

Patient progress is derived from completed sessions and grouped by exercise assignment.

The dashboard includes:

- completed-session counts
- total repetitions
- latest repetition count
- repetition change from the previous session
- latest knee range
- knee-range change from the previous session
- recent repetition history
- recent knee-range history

Recent patient sessions are limited to locally completed sessions rather than mixing active or interrupted work into rehabilitation progress.

## Therapist progress

Therapists can review recent completed sessions for a selected patient and see:

- assignment-specific session history
- repetition history
- knee-range history
- latest-vs-previous trends
- weekly adherence against prescribed frequency

Patient selection and session loading are scoped by authenticated identity so one patient's rehabilitation history is not accidentally displayed for another.

## Security model

TeleRehab uses role-aware Firestore rules and authenticated identities.

- Patients access their own rehabilitation data.
- Therapists manage patients linked to their therapist account.
- Assignment writes are therapist-controlled.
- Session synchronization is scoped to the authenticated patient.
- Raw rehabilitation video is not stored in Firestore.

The current therapist-led patient provisioning flow is appropriate for the prototype. A production deployment would replace temporary credential handoff with a dedicated invitation and account-recovery flow.

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- Room
- WorkManager
- Coroutines / Flow
- Firebase Authentication
- Cloud Firestore
- CameraX
- ML Kit Pose Detection
- Hilt
- Gradle
- JUnit
- Firebase Emulator Suite

## Testing and validation

Development is milestone-driven and continuously validated with:

- JVM unit tests
- domain lifecycle tests
- synchronization regression tests
- ViewModel tests
- progress-calculation tests
- Firestore security-rule tests
- debug and release compilation
- physical-device patient testing
- physical-device therapist testing
- authentication and role-transition testing
- session completion/interruption/restart testing

Recent regression passes cover:

- patient sign-in and dashboard loading
- assignment loading
- guided-session start
- normal session completion
- recent-session refresh after completion
- patient progress refresh
- Back interruption from a guided session
- sign out during an active session
- stale-session recovery after restart
- therapist sign-in
- therapist patient selection
- patient switching
- assignment management
- therapist session history
- therapist progress rendering
- patient-scoped session synchronization
- pending-session batch draining

## Building

Run the local unit-test and debug build:

```text
gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Build the release variant:

```text
gradlew.bat :app:assembleRelease
```

The project currently produces an **unsigned release APK**. Release signing and distribution configuration are intentionally outside the current MVP scope.

## Current production gaps

The core rehabilitation workflow is implemented. Remaining work is mainly productionization rather than fundamental product behavior:

- production account invitation and recovery
- release signing and distribution configuration
- broader automated UI/instrumentation coverage
- accessibility validation
- broader device-compatibility validation
- additional rehabilitation exercises
- further camera-quality and movement-feedback refinement

## Scope

TeleRehab is a rehabilitation workflow and exercise-tracking project.

It is not intended to autonomously diagnose patients, prescribe treatment, or make general medical decisions.

---

Built as a hands-on Android engineering project focused on architecture, reliability, lifecycle correctness, synchronization, and real-world patient/therapist workflows.
