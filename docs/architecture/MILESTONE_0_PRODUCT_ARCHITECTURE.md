# TeleRehab — Milestone 0 Product and Architecture Baseline

## Status

Accepted baseline for initial implementation.

These decisions remain valid unless implementation evidence gives us a reason to revise them.

---

# 1. Product Goal

TeleRehab is a native Android tele-rehabilitation application supporting two primary roles:

- Patient
- Therapist

The first implementation vertical slice focuses on guided knee flexion/extension rehabilitation.

The project is being rebuilt with production-quality Android engineering practices rather than treating Firebase or an ML SDK as the application architecture.

---

# 2. Initial Patient Workflow

A patient can:

1. Authenticate.
2. View assigned rehabilitation exercises.
3. Read exercise instructions.
4. Start a rehabilitation session.
5. Perform an exercise using the device camera.
6. Receive simple real-time movement feedback.
7. Complete the session without requiring network connectivity.
8. Review their exercise history and progress.
9. Allow completed session data to synchronize in the background.

A successfully completed rehabilitation session must not depend on Firebase or network availability.

---

# 3. Initial Therapist Workflow

A therapist can:

1. Authenticate.
2. View patients assigned to them.
3. Create or assign rehabilitation programs.
4. Configure exercise assignments.
5. Review patient adherence.
6. Review completed exercise-session metrics.
7. Adjust future rehabilitation assignments.

---

# 4. Explicitly Out of Scope for V1

The first release will not include:

- video calling
- therapist/patient chat
- billing
- prescription management
- hospital/EHR integration
- social functionality
- autonomous diagnosis
- generative medical diagnosis or treatment decisions

TeleRehab may measure observable exercise execution.

It must not present its movement heuristics as medical diagnosis.

---

# 5. First Vertical Slice

The first supported exercise is:

Knee flexion / extension.

This exercise is intentionally chosen because it provides a useful engineering path through:

- CameraX lifecycle handling
- pose estimation
- hip/knee/ankle landmark processing
- knee-angle calculation
- tracking confidence
- repetition state detection
- range-of-motion measurement
- session persistence
- offline operation
- background synchronization
- therapist review

The application should complete this vertical slice before attempting broad exercise coverage.

---

# 6. Architectural Direction

The system follows these conceptual layers:

UI / Compose

↓

Presentation

↓

Domain

↓

Repository contracts

↓

Data implementations

↓

Room / Firebase / CameraX / pose engine

Firebase must remain an infrastructure implementation detail.

Firebase SDK types must not become domain or presentation models.

---

# 7. Core Domain Areas

The first version separates responsibilities into four broad domain areas:

## Identity

Responsible for:

- authenticated identity
- application role
- patient/therapist identity relationships

Authentication proves identity.

Authentication alone does not grant access to clinical or rehabilitation data.

---

## Rehabilitation

Responsible for:

- rehabilitation programs
- exercise definitions
- exercise assignments
- schedules
- exercise-session relationships

---

## Session Analysis

Responsible for:

- movement observations
- joint measurements
- tracking confidence
- exercise state machines
- repetition results
- session metrics

Camera and pose SDK-specific classes must not leak into the rehabilitation domain.

---

## Synchronization

Responsible for:

- pending local changes
- synchronization attempts
- retries
- remote acknowledgement
- conflict behavior
- synchronization status

Synchronization state must remain separate from exercise-session state.

---

# 8. Initial Domain Concepts

The initial model contains concepts including:

- UserAccount
- RehabProgram
- ExerciseDefinition
- ExerciseAssignment
- ExerciseSession
- RepResult
- SessionMetrics
- TherapistReview

These are domain concepts.

They are not Room entities and they are not Firestore DTOs.

---

# 9. Exercise Definitions and Assignments

ExerciseDefinition describes reusable exercise knowledge.

Examples include:

- exercise name
- instructions
- movement type
- tracking configuration
- exercise-analysis strategy

ExerciseAssignment describes what a therapist requested for a specific patient.

Examples include:

- patient
- exercise definition
- target repetitions
- target sets
- hold duration
- schedule
- active period
- assignment status

Changing a patient's assignment must not mutate the reusable global exercise definition.

---

# 10. Exercise Session

ExerciseSession represents one patient attempt at an assigned exercise.

Initial session lifecycle:

NOT_STARTED

↓

IN_PROGRESS

↓

COMPLETED

Alternative endings may include:

- CANCELLED
- INTERRUPTED

Synchronization failure is not an exercise-session state.

For example:

session.status = COMPLETED

sync.status = PENDING

is valid.

---

# 11. Repetition Detection

Knee-flexion repetition detection will use an explicit state machine rather than a single threshold counter.

Conceptual flow:

WAITING_FOR_START

↓

EXTENDED

↓

FLEXING

↓

FLEXED

↓

EXTENDING

↓

EXTENDED

↓

REP COMPLETE

Angle thresholds and hysteresis values are exercise-analysis configuration.

They will not be hard-coded blindly before physical-device testing.

---

# 12. Camera and Pose Pipeline

Conceptual processing pipeline:

Camera frame

↓

Pose detector

↓

Body landmarks

↓

Movement observations

↓

Joint measurements

↓

Exercise-specific state machine

↓

Rep result

↓

Session result

Pose-estimation technology detects landmarks.

TeleRehab domain logic interprets exercise movement.

---

# 13. Camera Privacy

Raw rehabilitation video will not be stored or uploaded by default.

Camera frames are processed locally.

Normal session persistence should favor derived data such as:

- repetitions
- range of motion
- duration
- tracking-confidence statistics
- completion state
- timestamps
- session metrics

Any future feature involving stored patient imagery or recordings requires a separate explicit product/privacy decision.

---

# 14. Local-First Persistence

Patient session completion must work offline.

Conceptual write path:

Exercise completion

↓

Room transaction

↓

session persisted locally

↓

UI considers session complete

↓

sync state = PENDING

↓

WorkManager synchronization

↓

remote acknowledgement

↓

sync state = SYNCED

Network connectivity must not participate in the exercise-state machine.

---

# 15. Identifier Strategy

Records that may be created offline must receive globally unique IDs locally before synchronization.

A network retry for the same session ID must not create duplicate sessions remotely.

Synchronization must therefore be designed to be idempotent.

---

# 16. Firebase Responsibilities

Firebase is infrastructure, not application architecture.

Expected initial responsibilities:

Firebase Authentication
- authenticated identity

Cloud Firestore
- shared patient/therapist state
- synchronized rehabilitation records

Firebase Cloud Messaging
- relevant notifications when introduced

Firebase Emulator Suite
- development and integration testing

Cloud Functions or Cloud Run
- server-controlled operations if required

Firebase-specific SDK types remain inside the data/infrastructure implementation.

---

# 17. Local Android Responsibilities

Room
- durable local application/session state

WorkManager
- reliable deferred synchronization

CameraX
- camera lifecycle and frame acquisition

Pose engine
- body-landmark estimation

Kotlin domain logic
- movement interpretation
- repetition detection
- exercise rules

Jetpack Compose
- UI

---

# 18. Repository Boundary

The Android application should depend conceptually on interfaces such as:

- SessionRepository
- ProgramRepository
- AssignmentRepository
- UserRepository

The presentation/domain layers must not depend directly on:

- FirebaseFirestore
- FirebaseAuth
- DocumentSnapshot
- CollectionReference
- Room DAO implementation details

---

# 19. Persistence Model Separation

The project will maintain explicit separation between:

Domain Model

Room Entity

Firestore DTO

Mappings between these representations are deliberate architecture.

Storage schemas must not dictate the domain model.

---

# 20. Application Packaging

The first release uses one Android application containing both:

- patient workflows
- therapist workflows

Navigation becomes role-aware after authentication.

Separate patient and clinician applications are not required for the first version.

The architecture should not prevent a future split if business requirements justify it.

---

# 21. Security

Firebase Authentication establishes identity.

Authorization must additionally enforce:

- role
- ownership
- patient/therapist relationship
- resource-level access

Firestore security rules are treated as production code.

Unauthorized access paths must eventually be tested.

---

# 22. Quality Rules

The project follows these engineering rules:

1. main remains known-good.
2. Milestones have explicit acceptance conditions.
3. Firebase dependencies do not leak into domain or presentation layers.
4. Domain behavior is unit tested where practical.
5. Camera behavior is verified on a physical Android device.
6. Offline behavior is a first-class requirement.
7. Failure paths are tested rather than ignored.
8. Commits represent coherent engineering changes.
9. Build success alone does not imply milestone completion.
10. Manual device verification and automated instrumentation testing remain separate activities.

---

# 23. Initial Technology Direction

Current intended stack:

- Kotlin
- Jetpack Compose
- CameraX
- Room
- WorkManager
- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Messaging
- Firebase Emulator Suite
- dependency injection
- MediaPipe or ML Kit pose estimation after evaluation

The exact pose implementation is intentionally not locked yet.

We will select it based on the requirements of the exercise-analysis pipeline rather than preference alone.

---

# 24. Milestone 0 Acceptance Criteria

Milestone 0 is complete when:

- product boundary is documented
- patient workflow is documented
- therapist workflow is documented
- first vertical slice is selected
- domain boundaries are documented
- local-first behavior is established
- Firebase responsibilities are bounded
- camera privacy decision is documented
- synchronization principles are documented
- quality expectations are documented
- this decision record is committed to main

No Android application implementation is required for Milestone 0.

---

# 25. Next Milestone

Milestone 1 will establish the Android foundation.

It will include, in controlled steps:

- Android project creation
- package/application identity
- baseline Gradle configuration
- Compose application shell
- dependency-injection foundation
- architectural package/module boundaries
- physical-device installation
- baseline tests
- clean repository verification

Firebase feature implementation will not be the first step.
