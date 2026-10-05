# TeleRehab

TeleRehab is a native Android tele-rehabilitation application for structured patient exercise programs, therapist-managed assignments, guided camera sessions, progress tracking, and reliable local-first data handling.

The project is being built as a production-oriented Android portfolio project, with an emphasis on clean architecture, offline capability, secure role-based workflows, deterministic synchronization, and camera-based movement analysis.

## Current status

TeleRehab now has a working end-to-end patient/therapist flow.

### Therapist workflow

- Email/password therapist authentication
- Therapist dashboard with sign out
- Therapist-managed patient provisioning
- Stable Firebase Authentication accounts for patients
- One-time generated temporary patient credentials
- Therapist-to-patient relationships stored in Firestore
- Patient selection and assignment management
- Knee Flexion assignment creation
- Weekly adherence targets and therapist-facing progress summaries
- Session drill-down and recent patient session history

### Patient workflow

- Stable email/password patient authentication
- Patient sign out and role switching
- Assigned exercise dashboard
- Knee Flexion rehabilitation plan
- Session targets for repetitions and weekly frequency
- Guided exercise sessions
- Camera-based pose analysis
- Rep counting and flexion/extension threshold tracking
- Recent session history and progress comparison
- Offline-first assignment/session persistence
- Manual refresh and cloud synchronization

## Current rehabilitation slice

The first supported exercise is **Knee Flexion / Extension**.

A therapist can assign a target such as:

- 10 repetitions per session
- 3 sessions per week
- Flexion threshold at or below 90 degrees
- Extension threshold at or above 160 degrees

The patient can then perform a guided camera session while TeleRehab derives movement metrics locally. Raw video is not uploaded or stored by default.

## Architecture

TeleRehab follows a local-first architecture.

- **Domain layer** remains independent of Firebase, Room, Compose, and CameraX.
- **Room** provides durable local state for patient-facing workflows.
- **WorkManager** is used for resilient synchronization.
- **Firebase Authentication** provides stable identity.
- **Cloud Firestore** provides shared therapist/patient state.
- **CameraX + pose analysis** powers guided exercise tracking.
- Remote writes use stable client identifiers and idempotent upserts where appropriate.
- Domain models, Room entities, and Firestore DTOs remain separate.

Major concepts are separated around:

- Identity
- Rehabilitation assignments
- Session analysis
- Synchronization
- Therapist adherence and progress review

For the original product and architecture baseline, see:

`docs/architecture/MILESTONE_0_PRODUCT_ARCHITECTURE.md`

## Security model

TeleRehab uses role-aware Firestore rules.

- Patients can read their own profile and assigned rehabilitation data.
- Therapists can manage only patients linked to their therapist account.
- Patient profiles are provisioned together with the therapist/patient relationship.
- Assignment writes are therapist-controlled.
- Raw rehabilitation video is not stored in Firestore.

The current patient-provisioning flow is intentionally optimized for the prototype: the therapist creates the patient account and shares a temporary generated password. A production deployment would add a dedicated invitation/password-reset flow rather than treating temporary credentials as the long-term onboarding mechanism.

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
- ML-based pose tracking
- Hilt
- Gradle
- Firebase Emulator Suite for Firestore rules testing

## Engineering approach

Development is milestone-driven and validated continuously with:

- Kotlin compilation
- Unit tests
- Firestore security-rule tests
- Physical Android device testing
- Manual authentication and role-transition testing
- Guided-session validation
- Git history organized around small feature/refactor milestones

## Project direction

Next work focuses on strengthening the product beyond the first vertical slice:

- Cleaner patient onboarding and credential handoff
- Account/session lifecycle hardening
- More rehabilitation exercises
- Stronger adherence and progress visualization
- Improved pose-quality feedback
- More resilient background synchronization
- Expanded automated UI/instrumentation coverage
- Production-ready invitation and recovery flows

## Scope

TeleRehab is a rehabilitation workflow and exercise-tracking project. It is not intended to autonomously diagnose patients or make general medical decisions.

---

Built as a hands-on production Android engineering project focused on reliability, architecture, and real-world patient/therapist workflows.
